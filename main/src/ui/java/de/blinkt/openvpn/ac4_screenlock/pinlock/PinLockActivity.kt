package de.blinkt.openvpn.ac4_screenlock.pinlock

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.blinkt.openvpn.ac4_screenlock.Ac4_01_screenlock_main
import de.blinkt.openvpn.ac4_screenlock.util.LockReasonManager
import de.blinkt.openvpn.databinding.OldAc000MainInitMainBinding
import de.blinkt.openvpn.databinding.ActivityPinLockBinding

class PinLockActivity : AppCompatActivity() {

    private lateinit var edtPin: EditText
    private lateinit var btnSubmit: Button
    private lateinit var binding: ActivityPinLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPinLockBinding.inflate(layoutInflater)

        // ✅ 전체 화면 띄우기 (상태바 숨기기)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // ✅ 잠금화면 위에서 띄우기 (화면 꺼진 상태에서도 켜지도록)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        setContentView(binding.root)

//        edtPin = findViewById(R.id.et_pin)
//        btnSubmit = findViewById(R.id.btn_submit)
        edtPin = binding.etPin
        btnSubmit = binding.btnSubmit

        btnSubmit.setOnClickListener {
            val inputPin = edtPin.text.toString()

            if (PinStorageManager.isPinCorrect(this, inputPin)) {
                Toast.makeText(this, "✅ 인증 성공", Toast.LENGTH_SHORT).show()

                // ✅ 잠금 사유 제거
                LockReasonManager.clearReason(this)

                // ✅ MainActivity로 이동 (기존 백스택 제거)
                val intent = Intent(this, Ac4_01_screenlock_main::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(this, "❌ PIN이 올바르지 않습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ 뒤로가기 버튼 차단
    @Suppress("MissingSuperCall")
    override fun onBackPressed() {}

    // ✅ 홈 버튼 및 최근앱 버튼 눌렀을 때 재진입
    override fun onUserLeaveHint() {
        val intent = Intent(this, PinLockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }
}
