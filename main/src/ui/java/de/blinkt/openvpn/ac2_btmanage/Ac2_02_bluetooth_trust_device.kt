package de.blinkt.openvpn.ac2_btmanage

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import de.blinkt.openvpn.databinding.Ais22BtmViewDeviceBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class DeviceType {
    TRUSTED, BLOCKED
}

data class Device(
    val name: String,
    val address: String,
    val type: DeviceType
)

class Ac2_02_bluetooth_trust_device : ComponentActivity() {

    private lateinit var binding: Ais22BtmViewDeviceBinding
    private val prefsName = "prefs"
    private val trustedKey = "trusted"
    private val blockedKey = "blocked"

    // 1. 어댑터와 데이터 목록을 클래스 멤버 변수로 선언
    private lateinit var deviceAdapter: BtmViewDeviceAdapter
    private var trustedDeviceList = mutableListOf<Device>()
    private var blockedDeviceList = mutableListOf<Device>()
    private var currentDisplayType = DeviceType.TRUSTED // 현재 보여주는 목록 타입을 추적

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager).adapter
    }

    // UI 실시간 갱신을 위한 리시버
    private val bondStateReceiver = object : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                syncAndLoadDeviceLists() // 페어링 상태 변경 시, 목록을 다시 로드하여 UI에 반영
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Ais22BtmViewDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 이 함수를 호출하도록 구조화
        setupRecyclerView()
        setupClickListeners()
    }
    private fun setupRecyclerView() {
        // 어댑터 생성 시, 빈 MutableList의 타입을 <Device>로 명확히 지정합니다.
        deviceAdapter = BtmViewDeviceAdapter(mutableListOf<Device>()) { device, position ->
            // 두 번째 인수인 삭제 콜백 람다는 그대로 둡니다.
            handleDeviceDeletion(device, position)
        }

        binding.recyclerviewDevices.layoutManager = LinearLayoutManager(this)
        binding.recyclerviewDevices.adapter = deviceAdapter
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(bondStateReceiver, filter)
        syncAndLoadDeviceLists() // 화면에 진입할 때마다 최신 목록 로드
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(bondStateReceiver)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.topbarTrustBackground.setOnClickListener {
            currentDisplayType = DeviceType.TRUSTED
            deviceAdapter.updateData(trustedDeviceList) // 신뢰 목록으로 UI 갱신
        }
        binding.topbarBlockBackground.setOnClickListener {
            currentDisplayType = DeviceType.BLOCKED
            deviceAdapter.updateData(blockedDeviceList) // 차단 목록으로 UI 갱신
        }
    }
// in Ac2_02_bluetooth_trust_device.kt

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleDeviceDeletion(device: Device, position: Int) {
        val friendlyName = device.name
        when (device.type) {
            DeviceType.TRUSTED -> {
                lifecycleScope.launch {
                    val unpairSuccess = unpairDevice(device.address)
                    if (unpairSuccess) {
                        removeFromPreferences(trustedKey, device.address)

                        // ✅ Activity의 원본 데이터 리스트에서 아이템 제거
                        if (position < trustedDeviceList.size) {
                            trustedDeviceList.removeAt(position)
                        }

                        // ✅ 어댑터의 UI에서 아이템 제거
                        deviceAdapter.removeItem(position)
                        Toast.makeText(this@Ac2_02_bluetooth_trust_device, "$friendlyName 의 연결을 끊고 신뢰 목록에서 해제했습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@Ac2_02_bluetooth_trust_device, "기기 페어링 해제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            DeviceType.BLOCKED -> {
                removeFromPreferences(blockedKey, device.address)

                // ✅ Activity의 원본 데이터 리스트에서 아이템 제거
                if (position < blockedDeviceList.size) {
                    blockedDeviceList.removeAt(position)
                }

                // ✅ 어댑터의 UI에서 아이템 제거
                deviceAdapter.removeItem(position)
                Toast.makeText(this, "[$friendlyName] 은(는) 더 이상 차단되지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun syncAndLoadDeviceLists() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return

        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val storedTrusted = prefs.getStringSet(trustedKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val bondedNow = bluetoothAdapter?.bondedDevices?.map { it.address }?.toSet() ?: emptySet()

        // 시스템 설정에서 페어링이 해제된 경우, 앱의 신뢰 목록에서도 동기화
        val unbondedExternally = storedTrusted - bondedNow
        if (unbondedExternally.isNotEmpty()) {
            storedTrusted.removeAll(unbondedExternally)
            prefs.edit().putStringSet(trustedKey, storedTrusted).apply()
        }

        loadDeviceLists()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun loadDeviceLists() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return

        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val allBondedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()

        // 1. 실제 신뢰 기기 목록을 불러옵니다.
        val trustedAddresses = prefs.getStringSet(trustedKey, emptySet()) ?: emptySet()
        trustedDeviceList = trustedAddresses.mapNotNull { addr ->
            allBondedDevices.find { it.address == addr }?.let {
                Device(it.name ?: "알 수 없는 기기", it.address, DeviceType.TRUSTED)
            }
        }.toMutableList()

        // 2. 실제 차단 기기 목록을 불러옵니다.
        val blockedAddresses = prefs.getStringSet(blockedKey, emptySet()) ?: emptySet()
        val realBlockedList = blockedAddresses.map { addr ->
            Device("차단된 기기", addr, DeviceType.BLOCKED)
        }.toMutableList()

        // 3. ✅ 발표 시나리오를 위한 더미 차단 기기 데이터를 생성합니다.
        val dummyBlockedDevices = listOf(
            Device("의심스러운 마우스", "AA:11:BB:22:CC:33", DeviceType.BLOCKED),
            Device("악성 키보드", "DE:AD:BE:EF:00:00", DeviceType.BLOCKED),
            Device("알 수 없는 장치", "00:00:00:00:00:00", DeviceType.BLOCKED)
        )

        // 4. ✅ 실제 차단 목록과 더미 목록을 합쳐 최종 차단 목록을 완성합니다.
        blockedDeviceList.clear()
        blockedDeviceList.addAll(realBlockedList)
        blockedDeviceList.addAll(dummyBlockedDevices)

        // 5. 현재 선택된 탭에 맞춰 UI를 갱신합니다.
        val listToDisplay = if (currentDisplayType == DeviceType.TRUSTED) trustedDeviceList else blockedDeviceList
        deviceAdapter.updateData(listToDisplay)
    }

    private fun removeFromPreferences(key: String, address: String) {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val set = prefs.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        if (set.remove(address)) {
            prefs.edit().putStringSet(key, set).apply()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun unpairDevice(address: String): Boolean = withContext(Dispatchers.IO) {
        val deviceToUnpair = bluetoothAdapter?.bondedDevices?.find { it.address == address }

        if (deviceToUnpair != null) {
            try {
                // 페어링 해제 메서드 호출
                val method = deviceToUnpair.javaClass.getMethod("removeBond")
                return@withContext method.invoke(deviceToUnpair) as? Boolean ?: false
            } catch (e: Exception) {
                Log.e("TrustDevice", "페어링 해제 실패: $address", e)
                return@withContext false
            }
        }
        return@withContext true // 이미 페어링 목록에 없으면 성공으로 간주
    }
}