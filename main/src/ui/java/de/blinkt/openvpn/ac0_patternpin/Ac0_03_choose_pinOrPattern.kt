package de.blinkt.openvpn.ac0_patternpin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import de.blinkt.openvpn.databinding.OldAc003ChoosePinOrPatternBinding

//test용 임시 액티비티이고 본 데모에선 활용하지 않을 예정입니다.
class Ac0_03_choose_pinOrPattern : ComponentActivity() {
    private lateinit var binding: OldAc003ChoosePinOrPatternBinding
    private lateinit var auth: FirebaseAuth
//    private lateinit var database: LoginDatabase
    //private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OldAc003ChoosePinOrPatternBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

//        // Room Database 초기화
//        database = Room.databaseBuilder(
//            applicationContext,
//            LoginDatabase::class.java,
//            "app_database"
//        ).build()
//
//        // 로그인된 사용자 ID 받기
//        currentUserId = intent.getStringExtra("USER_ID") ?: ""
//
//        if (currentUserId.isEmpty()) {
//            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
//            finish()
//            return
//        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSelectPattern.setOnClickListener {
            val intent = Intent(this, Ac0_04_pattern_setup::class.java)
            //intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
        }

        binding.btnSelectPin.setOnClickListener {
            val intent = Intent(this, Ac0_06_pin_setup::class.java)
            //intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
        }

        binding.btnVerifyPattern.setOnClickListener {
            val intent = Intent(this, Ac0_05_pattern_verify::class.java)
            //intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
        }

        binding.btnVerifyPin.setOnClickListener {
            val intent = Intent(this, Ac0_07_pin_verify::class.java)
            startActivity(intent)
        }
        binding.btnVerifySomething.setOnClickListener {
            val intent = Intent(this, Ac0_08_pinpattern_forwarding::class.java)
            startActivity(intent)
        }
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            finish()
        }
    }
}