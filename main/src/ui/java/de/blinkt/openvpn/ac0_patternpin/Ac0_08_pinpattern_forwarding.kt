package com.naver.appLock.ac0_patternpin

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Ac0_08_pinpattern_forwarding : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUserId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid

        currentUserId?.let { userId ->
            lifecycleScope.launch {
                try {
                    val document = firestore.collection("User").document(userId).get().await()
                    val pinArray=document.get("pin") as MutableList<Any?>?
                    val patternArray=document.get("pattern") as? List<Long>
                    val isPinSet=pinArray?.get(0) as? Long
                    val isPatternSet= patternArray?.get(0)?.toInt()

                    when {
                        isPinSet == 1L -> {
                            val intent = Intent(this@Ac0_08_pinpattern_forwarding, Ac0_07_pin_verify::class.java)
                            startActivity(intent)
                            finish()
                        }
                        isPatternSet == 1 -> {
                            val intent = Intent(this@Ac0_08_pinpattern_forwarding, Ac0_05_pattern_verify::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else -> {
                            // pin, pattern 둘 다 0인 경우: 둘 다 설정하지 않음
                            Toast.makeText(
                                this@Ac0_08_pinpattern_forwarding,
                                "pin 또는 패턴을 설정하고 시도해주십시오 pin:${isPinSet}, pattern: ${isPatternSet}",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@Ac0_08_pinpattern_forwarding, "잠금 화면 연결에 실패했습니다 : ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}