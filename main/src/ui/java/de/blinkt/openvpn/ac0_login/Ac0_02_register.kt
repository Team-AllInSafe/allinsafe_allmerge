package de.blinkt.openvpn.ac0_login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import de.blinkt.openvpn.databinding.OldAc002RegisterBinding

class Ac0_02_register : ComponentActivity() {
    private lateinit var binding: OldAc002RegisterBinding

    //Firebase를 사용하는 권한
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OldAc002RegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase 코드
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        //DatabaseReference reference = database.getReference("Users")


        binding.btnRegister.setOnClickListener {
            val id = binding.etInputId.text.toString()
            val pw = binding.etInputPw.text.toString()
            val username = binding.etInputUsername.text.toString()
            //id, pw, 이메일 칸이 비어있지 않은지 체크한다.
            if (validateInput(id, pw, username)) {
                // TODO: 회원정보를 firebase 에 등록한다. 이때, 패턴, pin의 초기값(전부 0으로 세팅)도 함께 등록한다.
                createAccount(id, pw, username)
                //회원가입을 완료하고 로그인 화면으로 돌아간다.
                finish()
            }
        }
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(email: String, password: String, username: String): Boolean {
        return when {
            email.isEmpty() -> {
                Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                false
            }

            password.isEmpty() -> {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                Toast.makeText(this," ",Toast.LENGTH_LONG).show()
                false
            }

            username.isEmpty() -> {
                Toast.makeText(this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
                false
            }

            else -> true
        }
    }

    private fun createAccount(email: String, password: String, username: String) {
        checkUsernameAvailability(username) { isAvailable ->
            if (isAvailable) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // 회원가입 성공
                            val user = auth.currentUser

                            // 사용자 프로필 업데이트 (이름 설정)
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build()

                            user?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        // Firestore에 추가 사용자 정보 저장
                                        saveUserProfileToFirestore(user.uid, email, password, username)
                                    }
                                }
                        } else {
                            // 회원가입 실패
                            Toast.makeText(
                                this, "회원가입 실패: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "이미 사용중인 사용자명입니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkUsernameAvailability(username: String, callback: (Boolean) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.isEmpty)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "사용자명 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    private fun saveUserProfileToFirestore(userId: String, email: String, password: String, username: String) {
        // pattern 배열 생성: [false, 0, 0, 0, ..., 0] (boolean 1개 + int 16개)
        val pattern = mutableListOf<Any>().apply {
            add(0) // 첫 번째 요소는 boolean false
            repeat(16) { add(0) } // 나머지 16개는 int 0
        }

        // pin 배열 생성: [false, ""]
        val pin = listOf(0, "")

        // 현재 시간을 타임스탬프로 생성
        val registerDate = com.google.firebase.Timestamp.now()

        // Firestore에 저장할 사용자 데이터 Map
        val userData = hashMapOf(
            "email" to email,
            "password" to password,
            "pattern" to pattern,
            "pin" to pin,
            "registerDate" to registerDate,
            "role" to 1,
            "username" to username
        )

        // Firestore에 데이터 저장
        firestore.collection("User")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show()
                // 회원가입 완료 후 로그인 화면으로 이동
                finish() //
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "사용자 정보 저장 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}


