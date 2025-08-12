package de.blinkt.openvpn

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.blinkt.openvpn.ac2_btmanage.BtmViewDeviceAdapter
import de.blinkt.openvpn.ac2_btmanage.Device
import de.blinkt.openvpn.ac2_btmanage.DeviceType
import de.blinkt.openvpn.databinding.Ais22BtmViewDeviceBinding


class TempBtmTrustDevice : ComponentActivity() {
    private lateinit var binding: Ais22BtmViewDeviceBinding
    private lateinit var deviceAdapter: BtmViewDeviceAdapter

    // 1. get...() 함수로 받은 List를 수정 가능한 MutableList로 변환하여 저장합니다.
    private val trustedDevices = getTrustedDevices().toMutableList()
    private val blockedDevices = getBlockedDevices().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Ais22BtmViewDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerviewDevices.layoutManager = LinearLayoutManager(this)

        // 2. 어댑터 생성 시, MutableList와 함께 삭제 동작을 정의한 람다 함수를 전달합니다.
        // 초기 화면은 신뢰 기기 목록을 보여줍니다.
        deviceAdapter = BtmViewDeviceAdapter(trustedDevices) { device, position ->
            // --- 삭제 버튼 클릭 시 실행될 로직 ---
            val deviceTypeString = if (device.type == DeviceType.TRUSTED) {
                // 원본 데이터 리스트에서 아이템을 제거합니다.
                trustedDevices.remove(device)
                "신뢰"
            } else {
                blockedDevices.remove(device)
                "차단"
            }

            // 현재 어댑터가 보여주는 목록을 다시 설정하여 UI를 갱신합니다.
            // 어댑터의 removeItem을 호출하여 부드러운 애니메이션 효과를 줍니다.
            deviceAdapter.removeItem(position)

            Toast.makeText(this, "${device.name} (${deviceTypeString} 기기)가 등록 해제되었습니다.", Toast.LENGTH_SHORT).show()
            // ------------------------------------
        }

        binding.recyclerviewDevices.adapter = deviceAdapter

        // --- 클릭 리스너 설정 ---
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.topbarTrustBackground.setOnClickListener {
            deviceAdapter.updateData(trustedDevices) // 신뢰 목록으로 데이터 교체
        }
        binding.topbarBlockBackground.setOnClickListener {
            deviceAdapter.updateData(blockedDevices) // 차단 목록으로 데이터 교체
        }
    }

    // 신뢰 기기 데이터를 가져오는 가상의 함수
    private fun getTrustedDevices(): List<Device> {
        return listOf(
            Device("내 갤럭시 버즈", "11:22:33:44:55:66", DeviceType.TRUSTED),
            Device("사무실 스피커", "AA:BB:CC:DD:EE:FF", DeviceType.TRUSTED)
        )
    }

    // 차단 기기 데이터를 가져오는 가상의 함수
    private fun getBlockedDevices(): List<Device> {
        return listOf(
            Device("알 수 없는 헤드셋", "00:11:22:33:44:55", DeviceType.BLOCKED),
            Device("수상한 장치", "99:88:77:66:55:44", DeviceType.BLOCKED)
        )
    }
}