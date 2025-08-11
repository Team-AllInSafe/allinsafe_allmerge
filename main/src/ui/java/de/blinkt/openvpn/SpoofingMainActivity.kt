package de.blinkt.openvpn

import android.content.Intent
import android.net.VpnService
import android.os.*
import androidx.activity.ComponentActivity
import de.blinkt.openvpn.classforui.SpoofingDetectingStatusManager
import de.blinkt.openvpn.detection.SpoofingDetectionManager
import de.blinkt.openvpn.detection.arpdetector.ArpSpoofingDetector
import de.blinkt.openvpn.detection.common.AlertManager
import de.blinkt.openvpn.detection.dns.DnsSpoofingDetector
import de.blinkt.openvpn.detection.packettest.DummyPacketInjector
import de.blinkt.openvpn.vpn.CustomVpnService
import de.blinkt.openvpn.databinding.OldAc501SpoofingdetectInitMainBinding

class SpoofingMainActivity : ComponentActivity() {
    private lateinit var binding: OldAc501SpoofingdetectInitMainBinding
    private lateinit var detectionManager: SpoofingDetectionManager

    private val vpnRequestCode = 1000
    private var insertArpDummyPacket = false
    private var insertDnsDummyPacket = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 초기 설정
        val alertManager = AlertManager()
        val arpDetector = ArpSpoofingDetector(alertManager)
        val dnsDetector = DnsSpoofingDetector(alertManager)
        detectionManager = SpoofingDetectionManager(arpDetector, dnsDetector, alertManager)

        DummyPacketInjector.arp_init(arpDetector)
        DummyPacketInjector.dns_init(dnsDetector)

        SpoofingDetectingStatusManager.init(this)

        binding = OldAc501SpoofingdetectInitMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔙 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            finish()
        }

        // ▶️ 탐지 시작 버튼
        binding.btnDetectStart.setOnClickListener {
            // 더미 패킷 삽입 예약
            if (insertArpDummyPacket) {
                DummyPacketInjector.injectDummyArpData()
            }
            if (insertDnsDummyPacket) {
                DummyPacketInjector.injectDummyDnsPacket()
            }

            // VPN 권한 요청
            startVpnService()
        }

        // 🔹 더미 패킷 삽입 예약
        binding.btnArpDummyPacket.setOnClickListener {
            insertArpDummyPacket = true
        }

        binding.btnDnsDummyPacket.setOnClickListener {
            insertDnsDummyPacket = true
        }

        // 📜 탐지 기록 보기
        binding.btnShowDetectHistory.setOnClickListener {
            val intent = Intent(this, Ac5_04_spoofingdetect_detect_history::class.java)
            startActivity(intent)
        }
    }

    private fun startVpnService() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, vpnRequestCode)
        } else {
            onActivityResult(vpnRequestCode, RESULT_OK, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == vpnRequestCode && resultCode == RESULT_OK) {
            // VPN 서비스 시작
            val serviceIntent = Intent(this, CustomVpnService::class.java)
            startService(serviceIntent)

            // 탐지 진행 화면으로 이동 (탐지 시작은 Ac5_02에서 수행)
            val intent = Intent(this, Ac5_02_spoofingdetect_process::class.java)
            startActivity(intent)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
