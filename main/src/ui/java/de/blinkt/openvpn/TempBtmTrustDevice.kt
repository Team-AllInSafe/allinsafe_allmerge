package de.blinkt.openvpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.blinkt.openvpn.ac2_btmanage.BtmViewDeviceAdapter
import de.blinkt.openvpn.ac2_btmanage.Device
import de.blinkt.openvpn.ac2_btmanage.DeviceType
import de.blinkt.openvpn.databinding.Ais22BtmViewDeviceBinding


class TempBtmTrustDevice : ComponentActivity() {
    lateinit var binding: Ais22BtmViewDeviceBinding
    private lateinit var deviceAdapter: BtmViewDeviceAdapter
    private val trustedDevices = getTrustedDevices()
    private val blockedDevices = getBlockedDevices()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= Ais22BtmViewDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerviewDevices.layoutManager = LinearLayoutManager(this)

        // 초기 화면에는 신뢰 기기 목록을 보여줍니다.
        deviceAdapter = BtmViewDeviceAdapter(trustedDevices)
        binding.recyclerviewDevices.adapter = deviceAdapter

        binding.btnBack.setOnClickListener{
            finish()
        }
        binding.topbarTrustBackground.setOnClickListener{
            deviceAdapter.updateData(trustedDevices)
        }
        binding.topbarBlockBackground.setOnClickListener{
            deviceAdapter.updateData(blockedDevices)
        }

    }
    // 신뢰 기기 데이터를 가져오는 가상의 함수입니다.
    private fun getTrustedDevices(): List<Device> {
        return listOf(
            // DeviceType.TRUSTED 속성을 추가합니다.
            Device("Device A", "11:22:33:44:55:66", DeviceType.TRUSTED),
            Device("Device B", "AA:BB:CC:DD:EE:FF", DeviceType.TRUSTED)
        )
    }

    // 차단 기기 데이터를 가져오는 가상의 함수입니다.
    private fun getBlockedDevices(): List<Device> {
        return listOf(
            // DeviceType.BLOCKED 속성을 추가합니다.
            Device("Device C", "00:11:22:33:44:55", DeviceType.BLOCKED),
            Device("Device D", "99:88:77:66:55:44", DeviceType.BLOCKED)
        )
    }
    fun showTrustDevice(binding: Ais22BtmViewDeviceBinding){

    }
    fun showBlockDevice(binding: Ais22BtmViewDeviceBinding){

    }
}