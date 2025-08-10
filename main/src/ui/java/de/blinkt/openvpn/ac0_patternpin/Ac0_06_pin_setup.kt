package de.blinkt.openvpn.ac0_patternpin

import android.R
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.blinkt.openvpn.databinding.Ais05LockscreenPinSettingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class Ac0_06_pin_setup : ComponentActivity() {
    lateinit var binding: Ais05LockscreenPinSettingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUserId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= Ais05LockscreenPinSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid

        var passwordVisible=false
        binding.btnBack.setOnClickListener {
            //설정 종료
            finish()
        }
        binding.btnSetPin.setOnClickListener {
            registerPin()
        }
        binding.btnViewPin.setOnClickListener {
            if (passwordVisible) {
                // 비밀번호를 가리는 로직
                binding.inputPin.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                // TODO: 눈깔 아이콘 눈 감기기
                //원래 이것까지 해야하는데 지금은 R.drawable이 안됨 ;;
                //binding.btnViewPin.setImageResource(R.drawable.)
            } else {
                // 비밀번호를 평문으로 보이게 하는 로직
                binding.inputPin.inputType = InputType.TYPE_CLASS_TEXT
                // TODO: 눈깡 아이콘 눈 띄우기
            }
            passwordVisible = !passwordVisible
        }
    }

    private fun registerPin() {
        val pin: String? = binding.inputPin.getText().toString().trim()

        // 입력값 검증
        if (TextUtils.isEmpty(pin)) {
            Toast.makeText(this, "PIN을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (pin!!.length < 4) {
            Toast.makeText(this, "PIN은 4자리 이상이어야 합니다", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase에 PIN 저장
        savePinToFirebase(pin)
    }

    private fun savePinToFirebase(pin: String?) {
        //val userId = auth.getCurrentUser()!!.getUid()


//        // pin 배열: [등록여부(boolean), PIN값(string)]
//        val pinArray = Arrays.asList<Any?>(true, pin)
//
//        val data: MutableMap<String?, Any?> = HashMap<String?, Any?>()
//        data.put("pin", pinArray)

        currentUserId?.let { userId ->
            lifecycleScope.launch {
                try {
                    // pin 배열: [등록여부(boolean), PIN값(string)]
                    val pinArray = listOf(1, pin)
                    // 패턴 전부 초기화
                    val patternData = MutableList(17) { 0 } // 인덱스 0 + 16개 점
                    // Firebase에 저장
                    firestore.collection("User").document(userId)
                        .update("pattern", patternData)
                        .await()
                    firestore.collection("User").document(userId) //이거는 될라나? 잘 모르겠네
                        .update("pin", pinArray)
                        .await()
                    withContext(Dispatchers.Main) { //이거는 정확히 언제 실행되는거지?
                        if (!isFinishing && !isDestroyed) {
                            Toast.makeText(this@Ac0_06_pin_setup, "핀이 설정되었습니다 : ${pin}", Toast.LENGTH_SHORT).show()

                            // 액티비티 전환
//                            val intent = Intent(this@Ac0_04_pattern_setup, Ac0_05_pattern_verify::class.java)
//                            startActivity(intent)
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@Ac0_06_pin_setup, "패턴 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}