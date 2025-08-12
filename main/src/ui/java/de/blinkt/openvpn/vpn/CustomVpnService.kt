package de.blinkt.openvpn.vpn

import android.content.Intent
import android.net.VpnService
import android.os.*
import android.util.Log
import de.blinkt.openvpn.classforui.SpoofingDetectingStatusManager.isCapturing
import de.blinkt.openvpn.classforui.SpoofingDetectingStatusManager.spoofingEnd
import de.blinkt.openvpn.detection.SpoofingDetectionManager
import de.blinkt.openvpn.detection.common.AlertManager
import de.blinkt.openvpn.detection.arpdetector.ArpSpoofingDetector
import de.blinkt.openvpn.detection.common.LogManager
import de.blinkt.openvpn.detection.dns.DnsSpoofingDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.coroutines.cancellation.CancellationException

class CustomVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var packetCaptureThread: Thread? = null
    private var packetCaptureJob: Job? = null
//    private var isCapturing = false
    private var detectionManager: SpoofingDetectionManager? = null
    private val buffer = ByteBuffer.allocate(32767)

    //25.08.09 스레드 대신 scope를 사용한 메모리 친화적 멀티태스킹 ^^
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

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
        // 한번 실행됨
//        Log.d("allinsafe", "[spoofing] customvpnservice onStartCommand 실행")

        LogManager.log("VPN", "VPN 서비스 시작 요청")
        startVpnSafely()
        return START_STICKY
    }

    // 🔹 VPN 인터페이스를 설정하고 탐지기 초기화
    private fun startVpnSafely() {
        // 한번 실행됨
//        Log.d("allinsafe", "[spoofing] onStartCommand->startVpnSafely 실행")
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

        packetCaptureJob=scope.launch(Dispatchers.IO){
            // 25.08.09 5초가 지났다면 밑의 타이머 코루틴으로 인해 stopvpn이 실행되면서 iscapture false되어 while문 끝남

            // inputStream.read에서 패킷을 너무 오래 기다리면 성능이 떨어질 수 있음, io 전용 스레드 풀(여러 스레드의 묶음)에게 맡김
            try {
                val fd = vpnInterface?.fileDescriptor ?: return@launch
                val inputStream = FileInputStream(fd)
                LogManager.log("VPN", "패킷 캡처 스레드 시작")
                while (isCapturing) {
                    val length = inputStream.read(buffer.array()) // 패킷 올때까지 블로킹
                    if (length > 0) {
                        val packetData = buffer.array().copyOf(length)

                        // ✅ 최근 패킷 저장 (외부 탐지기에서 접근 가능)
                        latestPacket = packetData

                        // ✅ 동시에 기존 방식도 유지
                        detectionManager?.analyzePacket(packetData)

                        //25.08.09 기존에 아래 함수에서 카운트하던 타이머를 밑에 scope 코루틴으로 빼버림
//                        detectionManager?.startDetection(packetData)
                    }
                }
            } catch (e: Exception) {
                LogManager.log("VPN", "캡처 중 오류: ${e.message}")
            }
        }
        //5초 타이머
        scope.launch {
            delay(5000L)
            // 5초가 지났는데도 isCapturing이 여전히 true라면 타임아웃으로 간주
            if (isCapturing) {
                LogManager.log("allinsafeSpoofing", "5초 타임아웃! stopvpn!")
                try {
                    // scope 끄고,vpn 닫고, isCapturing false로
                    stopVpn()
                    // ui 화면을 다루기 때문에 main에서 하게 함
                    withContext(Dispatchers.Main){
                        spoofingEnd()
                    }

                } catch (e: IOException) {
                    // 이미 닫혔을 경우 등
                    LogManager.log("allinsafespoofing","뭔가 스푸핑 탐지 타이머가 잘못 끝남")
                }
            }

        }


        // 기존 패킷 감지 로직(스레드 사용)
//        packetCaptureThread = Thread {
//            try {
//                val fd = vpnInterface?.fileDescriptor ?: return@Thread
//                val inputStream = FileInputStream(fd)
//                LogManager.log("VPN", "패킷 캡처 스레드 시작")
//
//                while (isCapturing) {
//                    val length = inputStream.read(buffer.array()) //25.08.09 혹시 이거 blocking인가? ㅇㅇ 패킷 올때까지
//                    if (length > 0) {
//                        val packetData = buffer.array().copyOf(length)
//
//                        // ✅ 최근 패킷 저장 (외부 탐지기에서 접근 가능)
//                        latestPacket = packetData
//
//                        // ✅ 동시에 기존 방식도 유지
//                        detectionManager?.analyzePacket(packetData)
//
//                        //여기 부분이 문제였음
//                        Log.d("allinsafe", "[spoofing] onStartCommand->startVpnSafely->startPacketCapture->startDetection(while문 스레드) 실행")
//                        detectionManager?.startDetection(packetData)
//
//                        // 25.08.09 startDetection 안에서 탐지가 끝나면 isCapturing=false 되게 해놓음
//                    }
//                }
//            } catch (e: Exception) {
//                LogManager.log("VPN", "캡처 중 오류: ${e.message}")
//            }
//        }
//        packetCaptureThread?.start()
    }

    // 🔹 캡처 스레드 중단
    private fun stopPacketCapture() {
        isCapturing = false
        packetCaptureThread?.interrupt()
        packetCaptureThread = null

        packetCaptureJob?.cancel()
        packetCaptureJob=null
    }


//     🔹 VPN 인터페이스 종료
    private fun stopVpn() {
         stopPacketCapture()
//        vpnInterface?.close()
        if (vpnInterface!=null){
            vpnInterface?.close()
        }
        vpnInterface = null
        LogManager.log("VPN", "VPN 인터페이스 종료")
    }

    // 🔹 서비스가 종료될 때 호출
    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        job.cancel() // scope 종료
        LogManager.log("VPN", "VPN 서비스 종료")
    }

    // 🔹 바인딩은 사용하지 않음
    override fun onBind(intent: Intent?): IBinder? = null
}
