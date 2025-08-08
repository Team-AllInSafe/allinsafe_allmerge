package de.blinkt.openvpn.detection

import android.content.Intent
import android.util.Log
import de.blinkt.openvpn.Ac5_03_spoofingdetect_completed
import de.blinkt.openvpn.classforui.SpoofingDetectingStatusManager
import de.blinkt.openvpn.classforui.SpoofingDetectingStatusManager.completedPageStart
import de.blinkt.openvpn.classforui.SpoofingDetectingStatusManager.spoofingEnd
import de.blinkt.openvpn.detection.arpdetector.ArpData
import de.blinkt.openvpn.detection.arpdetector.ArpSpoofingDetector
import de.blinkt.openvpn.detection.common.AlertManager
import de.blinkt.openvpn.detection.common.LogManager
import de.blinkt.openvpn.detection.dns.DnsSpoofingDetector
import java.nio.ByteBuffer

class SpoofingDetectionManager(
    val arpDetector: ArpSpoofingDetector,
    val dnsDetector: DnsSpoofingDetector,
    private val alertManager: AlertManager
) {
    @Volatile
    private var isDetecting = false

    private val detectionTimeoutMillis = 5_000L // 5초 제한
    private var detectionStartTime: Long = 0

    fun startDetection(packetSource: ByteArray) {
//        packetSource: () -> ByteArray?
        if (isDetecting) {
            //Log.d("SpoofingManager", "탐지 중복 방지: 이미 실행 중")
            return
        }

        isDetecting = true
        detectionStartTime = System.currentTimeMillis()
        //25.06.24
//        LogManager.log("SpoofingManager", "탐지 시작")

        Thread {
            while (isDetecting) {
                val elapsed = System.currentTimeMillis() - detectionStartTime
                //Log.d("spoofing_count","$elapsed 초 경과")
                if (elapsed >= detectionTimeoutMillis) {

                    stopDetection()
                    //break
                    Log.d("allinsafe","[spoofing] while문이 자꾸 돌아가유")
                    // 2025.08.08 5초가 지나면 탐지가 중단되도록 해놓았지만, 스레드가 죽지않고 while문은 계속 돌아간다 -> startDetection을 자꾸 부르는 존재가 있음
                    spoofingEnd()
                    //?
                    //2025.08.08 원래 (!isDetecting) 조건인데, 이러면 false여야만 isDetecting가 false가 되는게 아닌가?
//                    if (isDetecting) {
//                        isDetecting = false
//                    }
                    // 25.08.08 제미나이가 if문 쓸 필요없다고 지적하니 이제서야 필요없다는걸 깨달음
                    isDetecting = false
                    //25.06.24
//                    LogManager.log("SpoofingManager", "탐지 종료")

                    //2025.08.08 while문 조건 탈출을 기다릴 필요없이 바로 나가기 추가
                    break
                    
                }

                val packetData = packetSource
                if (packetData != null) {
                    analyzePacket(packetData)
                }

                Thread.sleep(100) // 탐지 간격 (CPU 과부하 방지)

            }
        }.start()
    }

    fun stopDetection() {
        if (!isDetecting) return
        isDetecting = false
        //25.06.24
//        LogManager.log("SpoofingManager", "탐지 종료")
    }

    fun isDetectionRunning(): Boolean = isDetecting

    fun analyzePacket(packetData: ByteArray) {
        val buffer = ByteBuffer.wrap(packetData)

        if (isArpPacket(buffer)) {
            val arpData = ArpData.fromPacket(packetData)
            val isSpoofed = arpData != null && arpDetector.analyzePacket(arpData)
            if (isSpoofed) {
                alertManager.sendAlert(
                    severity = "CRITICAL",
                    title = "ARP 스푸핑 감지",
                    message = "IP: ${arpData!!.senderIp}, 변조된 MAC: ${arpData.senderMac}"
                )
                SpoofingDetectingStatusManager.arpSpoofingCompleted("CRITICAL")
            }
        }

        if (isDnsPacket(buffer)) {
            dnsDetector.processPacket(buffer)
        }
    }

    private fun isArpPacket(buffer: ByteBuffer): Boolean {
        return try {
            buffer.rewind()
            val ethTypeOffset = 12
            val etherType = buffer.getShort(ethTypeOffset).toInt() and 0xFFFF
            etherType == 0x0806
        } catch (e: Exception) {
            LogManager.log("SpoofingManager", "ARP 판별 실패: ${e.message}")
            false
        }
    }

    private fun isDnsPacket(buffer: ByteBuffer): Boolean {
        return try {
            buffer.rewind()
            val version = (buffer.get(0).toInt() shr 4) and 0xF
            val protocol = if (version == 4) buffer.get(9).toInt() and 0xFF else buffer.get(6).toInt() and 0xFF
            val srcPort = if (version == 4) buffer.getShort(20).toInt() and 0xFFFF else buffer.getShort(40).toInt() and 0xFFFF
            val dstPort = if (version == 4) buffer.getShort(22).toInt() and 0xFFFF else buffer.getShort(42).toInt() and 0xFFFF
            protocol == 17 && (srcPort == 53 || dstPort == 53)
        } catch (e: Exception) {
            LogManager.log("SpoofingManager", "DNS 판별 실패: ${e.message}")
            false
        }
    }
}
