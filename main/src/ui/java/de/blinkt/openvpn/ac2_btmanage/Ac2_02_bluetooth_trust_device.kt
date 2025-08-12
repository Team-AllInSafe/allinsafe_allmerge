package de.blinkt.openvpn.ac2_btmanage // 패키지 이름 일치

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import de.blinkt.openvpn.databinding.Ais22BtmViewDeviceBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//data class TrustedDevice(val address: String, val name: String, val isConnected: Boolean = false)
//// 기기 유형을 정의하는 enum 클래스
enum class DeviceType {
    TRUSTED, BLOCKED
}

// 기기 정보를 담는 데이터 클래스
data class Device(
    val name: String,
    val address: String,
    val type: DeviceType // 기기 유형을 나타내는 속성 추가
)
@OptIn(ExperimentalMaterial3Api::class)
class Ac2_02_bluetooth_trust_device : ComponentActivity() {
    lateinit var binding: Ais22BtmViewDeviceBinding
    private val prefs = "prefs"
    private val trustedKey = "trusted"
    private val blockedKey = "blocked"

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        bluetoothManager.adapter
    }

//    private var trustedDevices by mutableStateOf<List<TrustedDevice>>(emptyList())
//    private var blockedDevices by mutableStateOf<List<TrustedDevice>>(emptyList())
    private var trustedDevices by mutableStateOf<List<Device>>(emptyList())
    private var blockedDevices by mutableStateOf<List<Device>>(emptyList())

    private val bondStateReceiver = object : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                syncAndLoadDeviceLists()
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= Ais22BtmViewDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerviewDevices.layoutManager = LinearLayoutManager(this)
        lateinit var deviceAdapter: BtmViewDeviceAdapter
        trustedDevices = trustedDevices
        blockedDevices = blockedDevices

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
//        setContent {
//            MaterialTheme {
//                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
//                    TrustDeviceScreen(
//                        trustedDevices = trustedDevices,
//                        blockedDevices = blockedDevices,
//                        onRemoveTrusted = { addr ->
//                            lifecycleScope.launch {
//                                val unpairSuccess = unpairDevice(addr)
//                                if (unpairSuccess) {
//                                    removeFromTrustedList(addr)
//                                    loadDeviceLists()
//                                    Toast.makeText(this@Ac2_02_bluetooth_trust_device, "기기를 목록에서 제거했습니다.", Toast.LENGTH_SHORT).show()
//                                } else {
//                                    Toast.makeText(this@Ac2_02_bluetooth_trust_device, "기기 페어링 해제에 실패했습니다.", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                        },
//                        onRemoveBlocked = { addr ->
//                            removeFromBlockedList(addr)
//                            loadDeviceLists()
//                        }
//                    )
//                }
//            }
//        }
    }
    // 차단 기기 데이터를 가져오는 가상의 함수입니다.
//    private fun getBlockedDevices(): List<Device> {
//        return listOf(
//            // DeviceType.BLOCKED 속성을 추가합니다.
//            Device("Device C", "00:11:22:33:44:55", DeviceType.BLOCKED),
//            Device("Device D", "99:88:77:66:55:44", DeviceType.BLOCKED)
//        )
//    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(bondStateReceiver, filter)
        syncAndLoadDeviceLists()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(bondStateReceiver)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun unpairDevice(address: String): Boolean = withContext(Dispatchers.IO) {
        val deviceToUnpair = bluetoothAdapter?.bondedDevices?.find { it.address == address }

        if (deviceToUnpair != null) {
            try {
                val method = deviceToUnpair.javaClass.getMethod("removeBond")
                val success = method.invoke(deviceToUnpair) as? Boolean ?: false
                return@withContext success
            } catch (e: Exception) {
                Log.e("TrustDevice", "페어링 해제 실패: $address", e)
                return@withContext false
            }
        }
        return@withContext true // 페어링 목록에 없으면 이미 해제된 것으로 간주
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun syncAndLoadDeviceLists() {
        val prefs = getSharedPreferences(prefs, MODE_PRIVATE)
        val storedTrusted = prefs.getStringSet(trustedKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val bondedNow = bluetoothAdapter?.bondedDevices?.map { it.address }?.toSet() ?: emptySet()

        val unbondedExternally = storedTrusted - bondedNow
        if (unbondedExternally.isNotEmpty()) {
            storedTrusted.removeAll(unbondedExternally)
        }

        val newOnes = bondedNow - storedTrusted
        if (newOnes.isNotEmpty()) {
            storedTrusted.addAll(newOnes)
        }

        prefs.edit().putStringSet(trustedKey, storedTrusted).apply()
        loadDeviceLists()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun loadDeviceLists() {
        val prefs = getSharedPreferences(prefs, MODE_PRIVATE)
        val trusted = prefs.getStringSet(trustedKey, emptySet()) ?: emptySet()
        //실제 데이터 대신 더미 사용
        //val blocked = prefs.getStringSet(blockedKey, emptySet()) ?: emptySet()
        val bonded = bluetoothAdapter?.bondedDevices ?: emptySet()

        val trustedList = trusted.mapNotNull { addr ->
            bonded.find { it.address == addr }?.let {
                //TrustedDevice(it.address, it.name ?: "알 수 없는 기기", true)
                Device(it.name ?: "알 수 없는 기기", it.address, DeviceType.TRUSTED)
            }
        }
        //여기에 더미데이터 넣을려했다가 취소함
//        val blockedList = blocked.map { addr ->
//            //TrustedDevice(addr, "알 수 없는 기기", false)
//            //임의의 데이터
//            //근데 너무 더미데이터인거 티나나?
//            Device("악성 기기", "00:11:22:33:44:55", DeviceType.BLOCKED)
//            //Device("Device D", "99:88:77:66:55:44", DeviceType.BLOCKED)
//        }
        // 더미 차단 기기 목록 생성
        val dummyBlockedDevices = listOf(
            Device("차단된 기기 Alpha", "00:AA:BB:CC:DD:EE", DeviceType.BLOCKED),
            Device("차단된 기기 Beta", "FF:11:22:33:44:55", DeviceType.BLOCKED),
            Device("악성 기기 Gamma", "DE:AD:BE:EF:00:00", DeviceType.BLOCKED)
        )

        // UI 상태 업데이트
        trustedDevices = trustedList
        blockedDevices = dummyBlockedDevices
    }

    private fun removeFromTrustedList(address: String) {
        val prefs = getSharedPreferences(prefs, MODE_PRIVATE)
        val set = prefs.getStringSet(trustedKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        if (set.remove(address)) {
            prefs.edit().putStringSet(trustedKey, set).apply()
        }
    }

    private fun removeFromBlockedList(address: String) {
        val prefs = getSharedPreferences(prefs, MODE_PRIVATE)
        val set = prefs.getStringSet(blockedKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        if (set.remove(address)) {
            prefs.edit().putStringSet(blockedKey, set).apply()
        }
    }

    // 이 부분은 추후 Figma 디자인에 맞춰 수정
//    @Composable
//    private fun TrustDeviceScreen(
//        trustedDevices: List<TrustedDevice>,
//        blockedDevices: List<TrustedDevice>,
//        onRemoveTrusted: (String) -> Unit,
//        onRemoveBlocked: (String) -> Unit
//    ) {
//        var selectedTab by remember { mutableStateOf(0) }
//
//        Column(Modifier.fillMaxSize()) {
//            TopAppBar(
//                title = { Text("기기 관리", fontWeight = FontWeight.Bold) },
//                navigationIcon = {
//                    IconButton(onClick = { finish() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
//                    }
//                }
//            )
//
//            TabRow(selectedTab, Modifier.fillMaxWidth()) {
//                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
//                    Text("신뢰 기기 (${trustedDevices.size})", modifier = Modifier.padding(16.dp))
//                }
//                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
//                    Text("차단 기기 (${blockedDevices.size})", modifier = Modifier.padding(16.dp))
//                }
//            }
//
//            val list = if (selectedTab == 0) trustedDevices else blockedDevices
//            val removeCallback = if (selectedTab == 0) onRemoveTrusted else onRemoveBlocked
//            val emptyMsg = if (selectedTab == 0) "등록된 신뢰 기기가 없습니다." else "차단된 기기가 없습니다."
//
//            if (list.isEmpty()) {
//                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    Text(emptyMsg, fontSize = 16.sp, color = Color.Gray)
//                }
//            } else {
//                LazyColumn(
//                    Modifier.fillMaxSize(),
//                    contentPadding = PaddingValues(16.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    items(list, key = { it.address }) { dev ->
//                        DeviceRow(dev) { removeCallback(dev.address) }
//                    }
//                }
//            }
//        }
//    }
//
//    @Composable
//    private fun DeviceRow(device: TrustedDevice, onRemove: () -> Unit) {
//        Card(
//            Modifier
//                .fillMaxWidth()
//                .padding(vertical = 4.dp),
//            elevation = CardDefaults.cardElevation(2.dp)
//        ) {
//            Row(
//                Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(device.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
//                    Spacer(Modifier.height(4.dp))
//                    Text(device.address, fontSize = 12.sp, color = Color.Gray)
//                    if (device.isConnected) {
//                        Text("연결됨", fontSize = 12.sp, color = Color(0xFF00C853))
//                    }
//                }
//                IconButton(onClick = onRemove) {
//                    Icon(Icons.Default.Delete, contentDescription = "제거", tint = Color.Gray)
//                }
//            }
//        }
//    }
}