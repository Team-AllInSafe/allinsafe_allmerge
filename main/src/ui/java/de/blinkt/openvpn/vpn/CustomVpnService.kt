package de.blinkt.openvpn.vpn

import android.content.Intent
import android.net.VpnService
import android.os.*
import de.blinkt.openvpn.detection.SpoofingDetectionManager
import de.blinkt.openvpn.detection.common.AlertManager
import de.blinkt.openvpn.detection.arpdetector.ArpSpoofingDetector
import de.blinkt.openvpn.detection.common.LogManager
import de.blinkt.openvpn.detection.dns.DnsSpoofingDetector
import java.io.FileInputStream
import java.nio.ByteBuffer

class CustomVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var packetCaptureThread: Thread? = null
    private var isCapturing = false
    private var detectionManager: SpoofingDetectionManager? = null
    private val buffer = ByteBuffer.allocate(32767)

    companion object {
        // 🔹 가장 최근에 수신한 패킷을 외부에서 읽을 수 있도록 저장
        private var latestPacket: ByteArray? = null

        // 🔹 외부 탐지 매니저에서 호출하여 최근 패킷 1개를 가져감
        fun getLatestPacket(): ByteArray? {
            val packet = latestPacket
            latestPacket = null // 중복 분석 방지 위해 1회 사용 후 삭제
            return packet
        }
    }

    // 🔹 서비스가 시작될 때 호출됨
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogManager.log("VPN", "VPN 서비스 시작 요청")
        startVpnSafely()
        return START_STICKY
    }

    // 🔹 VPN 인터페이스를 설정하고 탐지기 초기화
    private fun startVpnSafely() {
        try {
            stopVpn()

            val fd = Builder()
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .establish()

            if (fd == null) {
                LogManager.log("VPN", "VPN 인터페이스 생성 실패")
                stopSelf()
                return
            }

            vpnInterface = fd
            LogManager.log("VPN", "VPN 인터페이스 설정 완료")

            // 🔹 탐지기 및 매니저 구성
            val alertManager = AlertManager()
            val arpDetector = ArpSpoofingDetector(alertManager)
            val dnsDetector = DnsSpoofingDetector(alertManager)

            detectionManager = SpoofingDetectionManager(
                arpDetector = arpDetector,
                dnsDetector = dnsDetector,
                alertManager = alertManager
            )

            // 🔹 인터페이스 안정화 후 패킷 캡처 시작
            Handler(Looper.getMainLooper()).postDelayed({
                if (vpnInterface != null) {
                    LogManager.log("VPN", "인터페이스 안정화 완료, 패킷 캡처 시작")
                    startPacketCapture()
                }
            }, 300)

        } catch (e: Exception) {
            LogManager.log("VPN", "VPN 시작 중 오류: ${e.message}")
            stopSelf()
        }
    }

    // 🔹 VPN 인터페이스로부터 실시간 패킷을 읽어오는 스레드 실행
    private fun startPacketCapture() {
        if (isCapturing) {
            LogManager.log("VPN", "이미 캡처 중")
            return
        }
        isCapturing = true

        packetCaptureThread = Thread {
            try {
                val fd = vpnInterface?.fileDescriptor ?: return@Thread
                val inputStream = FileInputStream(fd)
                LogManager.log("VPN", "패킷 캡처 스레드 시작")

                while (isCapturing) {
                    val length = inputStream.read(buffer.array())
                    if (length > 0) {
                        val packetData = buffer.array().copyOf(length)

                        // ✅ 최근 패킷 저장 (외부 탐지기에서 접근 가능)
                        latestPacket = packetData

                        // ✅ 동시에 기존 방식도 유지
                        detectionManager?.analyzePacket(packetData)

                        detectionManager?.startDetection(packetData)
                    }
                }
            } catch (e: Exception) {
                LogManager.log("VPN", "캡처 중 오류: ${e.message}")
            }
        }
        packetCaptureThread?.start()
    }

    // 🔹 캡처 스레드 중단
    private fun stopPacketCapture() {
        isCapturing = false
        packetCaptureThread?.interrupt()
        packetCaptureThread = null
    }

    // 🔹 VPN 인터페이스 종료
    private fun stopVpn() {
        stopPacketCapture()
        vpnInterface?.close()
        vpnInterface = null
        LogManager.log("VPN", "VPN 인터페이스 종료")
    }

    // 🔹 서비스가 종료될 때 호출
    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        LogManager.log("VPN", "VPN 서비스 종료")
    }

    // 🔹 바인딩은 사용하지 않음
    override fun onBind(intent: Intent?): IBinder? = null
}
