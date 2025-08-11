package de.blinkt.openvpn.ac4_screenlock


import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import de.blinkt.openvpn.ac4_screenlock.util.LockReasonManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import de.blinkt.openvpn.databinding.Ais41ScreenlockMainBinding
import de.blinkt.openvpn.R

class Ac4_01_screenlock_main : ComponentActivity() {

    private lateinit var dpm: DevicePolicyManager
    private lateinit var compName: ComponentName
//    private lateinit var binding: OldAc401ScreenlockMainBinding
    private lateinit var binding: Ais41ScreenlockMainBinding
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getPermissionOverlayWindow(this)
                startLockService()
            } else {
                Log.d("allinsafescreenlock","화면 잠금 권한 얻기 실패")
                Toast.makeText(this,"권한을 허용해주세요",Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding= OldAc401ScreenlockMainBinding.inflate(layoutInflater)
        binding= Ais41ScreenlockMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //sharedPreference로 앱 활성화 여부를 가져옵니다.
        val pref = getSharedPreferences("AppPref", MODE_PRIVATE)
        var isAppActive=pref.getBoolean("screenlock_onoff", false) //만약 저장된 값이 없을경우(초기상태) false입니다.

        setContentView(binding.root)
        if (isAppActive){
            //기능이 실행중(중지버튼 떠야함)
            setUiActive(binding)
        }
        else{
            //기능 실행 전(시작버튼 떠야함)
            setUiInactive(binding)
        }
        binding.btnBack.setOnClickListener{
            finish()
        }
        // 화면 잠금 시작 버튼
        binding.btnOnoffScreenlock.setOnClickListener {
            Log.d("allinsafescreenlock","onoff 버튼 클릭")
            if (isAppActive){
                //기능 중지
                // 1. sharedPreference에 앱 비활성화 저장
                pref.edit().putBoolean("screenlock_onoff", false).apply()
                // 2. ui 기능 중지 상태로 바꿈
                setUiInactive(binding)
                // 3. vpn 멈춤(진짜 기능
                stopLockService()
            }
            else{
                //기능 실행
                // 1. sharedPreference에 앱 활성화 저장
                pref.edit().putBoolean("screenlock_onoff", true).apply()
                // 2. ui 기능 작동 상태로 바꿈
                setUiActive(binding)
                // 3. vpn 시작(진짜 기능
                LockReasonManager.saveReason(this,"자동 잠금 테스트")
                // 권한 받고 받으면 실행
                requestNotificationPermission()
                // 권한 얻기. 권한 있으면 넘어감
//            getPermissionOverlayWindow(this)
//            startLockService()
            }
        }

//        // 화면 잠금 기능 끄기
//        binding.btnStartScreenlock.setOnClickListener {
//            stopLockService()
////            binding.btnStopScrlock.visibility=View.GONE
////            binding.btnStartScreenlock.visibility=View.VISIBLE
//        }

        // 기존 로직 25.08.11 주석
        /*
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, MyDeviceAdminReceiver::class.java)
        val btnRequestAdmin = binding.btnRequestAdmin
        val btnLockNow = binding.btnLockNow
        val switch2FA = binding.switch2fa
        val btnSetPin = binding.btnSetPin

        // 🔹 초기 스위치 & 버튼 상태
        val initialScreenLockEnabled = TwoFactorAuthManager.isScreenLockEnabled(this)
        val initial2FAEnabled = TwoFactorAuthManager.is2FAEnabled(this)

        Log.d("PinFlowCheck", "초기 상태 → ScreenLock: $initialScreenLockEnabled, 2FA: $initial2FAEnabled")

        // 🔹 초기 스위치 & 버튼 상태
        switch2FA.isChecked = TwoFactorAuthManager.isScreenLockEnabled(this)
        TwoFactorAuthManager.set2FAEnabled(this, switch2FA.isChecked)  // ✅ 이 줄 추가!!
        btnSetPin.visibility = if (switch2FA.isChecked) View.VISIBLE else View.GONE


        // 🔹 관리자 권한 요청
        btnRequestAdmin.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "기기 잠금 권한을 부여해주세요.")
            startActivity(intent)
        }

        // 🔹 수동 잠금
        btnLockNow.setOnClickListener {
            if (dpm.isAdminActive(compName)) {
                // 🔐 잠금 사유 저장
                LockReasonManager.saveReason(this, "수동 잠금")

                // 📱 실제 잠금 수행
                dpm.lockNow()
            } else {
                Toast.makeText(this, "관리자 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }


        // 🔹 2차 인증 사용 여부 토글
        switch2FA.setOnCheckedChangeListener { _, isChecked ->
            Log.d("PinFlowCheck", "스위치 클릭됨 → isChecked: $isChecked")
            TwoFactorAuthManager.setScreenLockEnabled(this, isChecked)
            TwoFactorAuthManager.set2FAEnabled(this, isChecked)
            btnSetPin.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // 🔹 PIN 설정 화면 이동
        btnSetPin.setOnClickListener {
            startActivity(Intent(this, PinSetupActivity::class.java))
        }
        */
    }

    fun setUiInactive(binding: Ais41ScreenlockMainBinding){
        //바꿀점 : 상단 활성화, 버튼 배경, 버튼 글씨
        binding.topActive.visibility=View.INVISIBLE
        binding.btnOnoffScreenlock.background=getDrawable(R.drawable.ais_round_mint_full)
        binding.btnOnoffScreenlock.text="화면잠금 실행하기"
    }
    fun setUiActive(binding: Ais41ScreenlockMainBinding){
        //바꿀점 : 상단 활성화, 버튼 배경, 버튼 글씨
        binding.topActive.visibility= View.VISIBLE
        binding.btnOnoffScreenlock.background=getDrawable(R.drawable.ais_round_white_full)
        binding.btnOnoffScreenlock.text="화면잠금 중단하기"
    }

    override fun onResume() {
        super.onResume()

        /*
        val is2FA = TwoFactorAuthManager.is2FAEnabled(this)
        val hasPin = PinStorageManager.isPinSet(this)
        val hasReason = LockReasonManager.hasReason(this)

        Log.d("PinFlowCheck", "onResume 상태 → 2FA: $is2FA, PinSet: $hasPin, HasReason: $hasReason")

        if (is2FA && hasPin && hasReason) {
            Log.d("PinFlowCheck", "🔐 조건 만족 → 인증 화면 실행")
            val intent = Intent(this, PinLockActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        } else {
            Log.d("PinFlowCheck", "❌ 조건 불충족 → 인증 화면 안뜸")
        }
        */
    }

    fun getPermissionOverlayWindow(context: Context){
        if(!Settings.canDrawOverlays(context)) {
            Log.d("allinsafescreen","다른 앱 위 뜨기 권한 없음")
            val intent=Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:"+context.packageName))
            context.startActivity(intent)
        }else{ // 권한 있으면
            Log.d("allinsafescreen","다른 앱 위 뜨기 권한 있음")
//            startLockService()
        }
    }

    // 알림표시 권한 받기
    fun requestNotificationPermission() {
        Log.d("allinsafescreenlock","화면 잠금 권한 얻기 시작 requestNotificationPermission")
        // 안드로이드 13 (API 33) 이상 버전에서만 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 권한이 이미 허용되었는지 확인
            val permissionState = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (permissionState != PackageManager.PERMISSION_GRANTED) {
                // 권한이 없으면 요청 다이얼로그 실행
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }else{
                Log.d("allinsafescreenlock", " 알림 권한 있음")
                // 다른 앱 위에 띄우기 권한 없으면 받게 하기
                getPermissionOverlayWindow(this)
                // 서비스 시작
                startLockService()
            }
        }
    }

    // 서비스 시작 함수
    private fun startLockService(){
        val serviceIntent = Intent(this, LockScreenService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    // 서비스 종료 함수
    private fun stopLockService(){
        val serviceIntent = Intent(this, LockScreenService::class.java)
        stopService(serviceIntent)
    }
}
