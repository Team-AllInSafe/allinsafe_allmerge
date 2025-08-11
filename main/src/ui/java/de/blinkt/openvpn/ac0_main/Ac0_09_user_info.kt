package de.blinkt.openvpn.ac0_main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.blinkt.openvpn.R
import de.blinkt.openvpn.ac0_patternpin.Ac0_04_pattern_setup
import de.blinkt.openvpn.ac0_patternpin.Ac0_05_pattern_verify
import de.blinkt.openvpn.ac0_patternpin.Ac0_06_pin_setup
import de.blinkt.openvpn.ac0_patternpin.Ac0_08_pinpattern_forwarding
import de.blinkt.openvpn.databinding.OldAc009UserInfoBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Ac0_09_user_info : ComponentActivity() {
    lateinit var binding: OldAc009UserInfoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUserId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= OldAc009UserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid

        //ui에 현재 계정 정보 불러오기
        currentUserId?.let { userId ->
            lifecycleScope.launch {
                try {
                    val document = firestore.collection("User").document(userId).get().await()
                    val pinArray = document.get("pin") as MutableList<Any?>?
                    val patternArray = document.get("pattern") as? List<Long>
                    val isPinSet=pinArray?.get(0) as? Long
                    val isPatternSet= patternArray?.get(0)?.toLong()
                    var lockMethod = "init"
                    binding.tvEmail.text=document.get("email") as String
                    binding.tvUsername.text=document.get("username") as String
                    if(isPinSet==1L){
                        lockMethod="PIN"
                    }
                    else if(isPatternSet==1L){
                        lockMethod="PATTERN"
                    }
                    else{
                        lockMethod="error"
                    }
                    binding.tvLockmethod.text=lockMethod

                } catch (e: Exception) {
                    Toast.makeText(this@Ac0_09_user_info, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.backButton.setOnClickListener {
            finish()
        }
        binding.btnPinSet.setOnClickListener {
            val intent = Intent(this@Ac0_09_user_info, Ac0_06_pin_setup::class.java)
            startActivity(intent)
        }
        binding.btnPatternSet.setOnClickListener {
            val intent = Intent(this@Ac0_09_user_info, Ac0_04_pattern_setup::class.java)
            startActivity(intent)
        }
    }
}