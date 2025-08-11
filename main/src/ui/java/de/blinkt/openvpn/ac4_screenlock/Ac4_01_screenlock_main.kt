package de.blinkt.openvpn.ac4_screenlock


import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import de.blinkt.openvpn.ac4_screenlock.pinlock.PinLockActivity
import de.blinkt.openvpn.ac4_screenlock.pinlock.PinSetupActivity
import de.blinkt.openvpn.ac4_screenlock.pinlock.PinStorageManager
import de.blinkt.openvpn.ac4_screenlock.util.LockReasonManager
import de.blinkt.openvpn.ac4_screenlock.util.TwoFactorAuthManager
import android.util.Log
import androidx.activity.ComponentActivity
import de.blinkt.openvpn.databinding.OldAc401ScreenlockMainBinding

class Ac4_01_screenlock_main : ComponentActivity() {

    private lateinit var dpm: DevicePolicyManager
    private lateinit var compName: ComponentName
    private lateinit var binding: OldAc401ScreenlockMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= OldAc401ScreenlockMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, MyDeviceAdminReceiver::class.java)

//        val btnRequestAdmin = findViewById<Button>(R.id.btn_request_admin
//        val btnLockNow = findViewById<Button>(R.id.btn_lock_now)
//        val switch2FA = findViewById<Switch>(R.id.switch_2fa)
//        val btnSetPin = findViewById<Button>(R.id.btn_set_pin)
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
    }

    override fun onResume() {
        super.onResume()

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
    }
}
