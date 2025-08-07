package de.blinkt.openvpn.ac0_login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import de.blinkt.openvpn.ac0_main.Ac0_00_main_init_main
import de.blinkt.openvpn.ac1_applock.Ac1_01_applock_init_main
import de.blinkt.openvpn.databinding.Ac001LoginBinding

class Ac0_01_login : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding:Ac001LoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=Ac001LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Auth 인스턴스 초기화
        auth = FirebaseAuth.getInstance()
        // 이미 로그인된 사용자가 있는지 확인
        val currentUser = auth.currentUser



        binding.btnLogin.setOnClickListener {
            val email = binding.etInputId.text.toString().trim()
            val password = binding.etInputPw.text.toString().trim()
            if (currentUser != null) {
                //이미 로그인되어 있으면 메인 액티비티로 이동
                Toast.makeText(this,"이미 로그인되어있습니다. ${currentUser.email}",Toast.LENGTH_SHORT).show()
                nextActivity()
                finish()
            }
            else{
                //currentuser가 null인 경우 : 로그인을 수행해야 하는 경우
                if (validateInput(email, password)) {
                    loginUser(email, password)
                }
            }

        }
        binding.btnRegister.setOnClickListener {
            val intent= Intent(this, Ac0_02_register::class.java)
            startActivity(intent)
        }
//        binding.tempPass.setOnClickListener {
//            //새로 만든 기능/액티비티 디버깅용으로 로그인 패스하기위해 만든거지 데모에선 절대 뭐 있으면 안됩니다!
////            val intent = Intent(this, Ac0_05_pattern_verify::class.java)
////            startActivity(intent)
//        }

    }
    //초기 화면 변경 이전 코드(기능 메인 화면)
//    private lateinit var binding:Ac001MainInitMainBinding
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding=Ac001MainInitMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        binding.spoofingDetectButton.setOnClickListener {
//            val intent= Intent(this, Ac5_01_spoofingdetect_init_main::class.java)
//            startActivity(intent)
//        }
//    }
    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                false
            }
            password.length < 6 -> {
                Toast.makeText(this, "비밀번호는 6자리 이상이어야 합니다", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    val user = auth.currentUser
                    Toast.makeText(this, "로그인 성공: ${user?.email}", Toast.LENGTH_SHORT).show()

                    // 메인 액티비티로 이동
                    nextActivity()
//                    val intent = Intent(this@Ac0_01_login, Ac0_04_pattern_setup::class.java)
//                    startActivity(intent)
                    finish()
                } else {
                    // 로그인 실패
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
    }
    private fun nextActivity(){
        val intent= Intent(this@Ac0_01_login, Ac0_00_main_init_main::class.java)
        startActivity(intent)
    }
}