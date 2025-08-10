package de.blinkt.openvpn.ac0_patternpin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.blinkt.openvpn.R
import de.blinkt.openvpn.ac1_applock.AppLockAccessibilityService
import de.blinkt.openvpn.databinding.Ais07LockscreenPinVerifyBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Ac0_07_pin_verify : ComponentActivity() {
    private lateinit var binding: Ais07LockscreenPinVerifyBinding
    private lateinit var patternView: PatternLockView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUserId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= Ais07LockscreenPinVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid

        var passwordVisible=false

//        binding.backButton.setOnClickListener {
//            finish()
//        }
        binding.btnVerifyPin.setOnClickListener {
            verifyPin()
        }
        binding.btnViewPin.setOnClickListener {
            if (passwordVisible) {
                // 비밀번호를 가리는 로직
                binding.inputPin.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnViewPin.setImageResource(R.drawable.ais_ic_eye_off)
            } else {
                // 비밀번호를 평문으로 보이게 하는 로직
                binding.inputPin.inputType = InputType.TYPE_CLASS_TEXT
                binding.btnViewPin.setImageResource(R.drawable.ais_ic_eye_on)
            }
            passwordVisible = !passwordVisible
        }
    }

    private fun verifyPin(){
        val inputPin=binding.inputPin.text.toString().trim()
        currentUserId?.let { userId ->
            lifecycleScope.launch {
                try {
                    val document = firestore.collection("User").document(userId).get().await()
                    val pinArray = document.get("pin") as MutableList<Any?>?
                    val isPinSet = pinArray?.get(0) as? Long
                    val savedPin=pinArray?.get(1)

                    if (pinArray == null || isPinSet == 0L) {
                        Toast.makeText(this@Ac0_07_pin_verify,"현재 pin이 설정되어있지 않습니다", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    withContext(Dispatchers.Main) { //이거는 정확히 언제 실행되는거지?
                        if(inputPin.equals(savedPin)==true){
                            Toast.makeText(this@Ac0_07_pin_verify,"pin 일치", Toast.LENGTH_SHORT).show()
                            //AppLockAccessibilityService.isLockActivityRunning=false
                            //어플 완전 종료인데 이거하면 뭐 어뗗게 되는거지
                            getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
                                .edit().putLong("lastUnlockTime", System.currentTimeMillis()).apply()
////                            finishAffinity();
////                            System.runFinalization();
////                            System.exit(0);
//                            finish()
                            AppLockAccessibilityService.isLockActivityRunning = false
                            val targetApp = intent.getStringExtra("TARGET_PACKAGE_NAME")
                            if (!targetApp.isNullOrEmpty()) {
                                val launchIntent = packageManager.getLaunchIntentForPackage(targetApp)?.apply {
                                    addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                                    )
                                }
                                if (launchIntent != null) {
                                    startActivity(launchIntent)
                                }
                            }
                            finish()
                        }
                        else{
                            Toast.makeText(this@Ac0_07_pin_verify,"pin 불일치", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@Ac0_07_pin_verify, "핀 검증 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}