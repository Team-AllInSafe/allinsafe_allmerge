package com.naver.appLock.ac4_screenlock.pinlock

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.naver.appLock.databinding.ActivityPinSetupBinding


class PinSetupActivity : AppCompatActivity() {

    private lateinit var etPin: EditText
    private lateinit var etConfirmPin: EditText
    private lateinit var btnSetPin: Button
    private lateinit var binding: ActivityPinSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPinSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        etPin = findViewById(R.id.et_pin)
//        etConfirmPin = findViewById(R.id.et_confirm_pin)
//        btnSetPin = findViewById(R.id.btn_set_pin)

        etPin = binding.etPin
        etConfirmPin = binding.etConfirmPin
        btnSetPin = binding.btnSetPin

        btnSetPin.setOnClickListener {
            val pin = etPin.text.toString()
            val confirmPin = etConfirmPin.text.toString()

            when {
                pin.length < 4 -> {
                    Toast.makeText(this, "PIN은 최소 4자 이상이어야 합니다", Toast.LENGTH_SHORT).show()
                }
                pin != confirmPin -> {
                    Toast.makeText(this, "PIN이 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    PinStorageManager.savePin(this, pin)
                    Toast.makeText(this, "✅ PIN이 설정되었습니다", Toast.LENGTH_SHORT).show()
                    finish()  // 설정 완료 후 종료
                }
            }
        }
    }

    @Suppress("MissingSuperCall")
    override fun onBackPressed() {
        // 뒤로가기 금지 (원한다면 허용해도 됨)
    }
}
