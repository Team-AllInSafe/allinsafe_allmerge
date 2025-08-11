package de.blinkt.openvpn.ac0_patternpin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.blinkt.openvpn.databinding.OldAc003PatternCreateBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Arrays

class Ac0_04_pattern_setup : ComponentActivity() {
private lateinit var binding: OldAc003PatternCreateBinding
    private lateinit var patternView: PatternLockView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUserId: String? = null
    private var isSettingMode = true // true: 패턴 설정, false: 패턴 검증

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=OldAc003PatternCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid

        // PatternView 초기화
        patternView = binding.patternView

        // 패턴 입력 완료 리스너 설정
        patternView.setOnPatternListener(object : PatternLockView.OnPatternListener {
            override fun onPatternComplete(pattern: List<Int>) {
//                if (isSettingMode) {
//                    setPattern(pattern)
//                } else {
//                    verifyPattern(pattern)
//                }
                setPattern(pattern)
                finish()
            }
        })

        // 현재 모드 확인 (기존 패턴이 있는지 체크)
        checkExistingPattern()
    }

    private fun checkExistingPattern() {
        currentUserId?.let { userId ->
            lifecycleScope.launch {
                try {
                    val document = firestore.collection("User").document(userId).get().await()
                    val patternData = document.get("pattern") as? List<*>

                    if (patternData != null && patternData.isNotEmpty()) {
                        val hasPattern = patternData[0] as? Boolean ?: false
                        isSettingMode = !hasPattern

                        if (isSettingMode) {
                            Toast.makeText(this@Ac0_04_pattern_setup, "새 패턴을 설정하세요", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@Ac0_04_pattern_setup, "패턴을 입력하여 인증하세요", Toast.LENGTH_SHORT).show()
                            Toast.makeText(this@Ac0_04_pattern_setup," ",Toast.LENGTH_SHORT)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@Ac0_04_pattern_setup, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setPattern(pattern: List<Int>) {
        currentUserId?.let { userId ->
            lifecycleScope.launch {
                try {
                    // 패턴 데이터 생성 (인덱스 0: true, 나머지: ,./방문 순서)
                    val patternData = MutableList(17) { 0 } // 인덱스 0 + 16개 점
                    patternData[0] = 1 // boolean true를 1로 표현

                    // 방문 순서 설정 (1~16)
                    pattern.forEachIndexed { index, dotIndex ->
                        patternData[dotIndex + 1] = index + 1
                    }

                    // Firebase에 저장
                    firestore.collection("User").document(userId)
                        .update("pattern", patternData)
                        .await()
                    //pin 초기화
                    val pinArray = Arrays.asList<Any?>(0, "")
                    firestore.collection("User").document(userId) //이거는 될라나? 잘 모르겠네
                        .update("pin", pinArray)
                        .await()
                    withContext(Dispatchers.Main) {
                        if (!isFinishing && !isDestroyed) {
                            Toast.makeText(this@Ac0_04_pattern_setup, "패턴이 설정되었습니다", Toast.LENGTH_SHORT).show()
                            isSettingMode = false
                            patternView.clearPattern()

                            // 액티비티 전환
//                            val intent = Intent(this@Ac0_04_pattern_setup, Ac0_05_pattern_verify::class.java)
//                            startActivity(intent)
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@Ac0_04_pattern_setup, "패턴 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun verifyPattern(inputPattern: List<Int>) {
        currentUserId?.let { userId ->
            lifecycleScope.launch {
                try {
                    val document = firestore.collection("User").document(userId).get().await()
                    val patternData = document.get("pattern") as? List<Long>

                    if (patternData != null && patternData.isNotEmpty()) {
                        val hasPattern = patternData[0] == 1L

                        if (!hasPattern) {
                            Toast.makeText(this@Ac0_04_pattern_setup, "설정된 패턴이 없습니다", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        // 저장된 패턴 복원
                        val savedPattern = mutableListOf<Int>()
                        val visitOrder = mutableMapOf<Int, Int>()

                        // 방문 순서 매핑
                        for (i in 1 until patternData.size) {
                            val order = patternData[i].toInt()
                            if (order > 0) {
                                visitOrder[order] = i - 1 // 인덱스를 0부터 시작하도록 조정
                            }
                        }

                        // 순서대로 정렬하여 패턴 복원
                        visitOrder.toSortedMap().values.forEach { dotIndex ->
                            savedPattern.add(dotIndex)
                        }

                        // 패턴 비교
                        if (inputPattern == savedPattern) {
                            Toast.makeText(this@Ac0_04_pattern_setup, "패턴 인증 성공!", Toast.LENGTH_SHORT).show()
                            // 여기서 다음 화면으로 이동하거나 추가 작업 수행
                        } else {
                            Toast.makeText(this@Ac0_04_pattern_setup, "잘못된 패턴입니다", Toast.LENGTH_SHORT).show()
                        }
                    }

                    patternView.clearPattern()

                } catch (e: Exception) {
                    Toast.makeText(this@Ac0_04_pattern_setup, "패턴 검증 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}