package de.blinkt.openvpn.ac0_patternpin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.blinkt.openvpn.ac1_applock.AppLockAccessibilityService
import de.blinkt.openvpn.databinding.Ais08LockscreenPatternVerifyBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.math.BigInteger

class Ac0_05_pattern_verify : ComponentActivity() {
    private lateinit var binding: Ais08LockscreenPatternVerifyBinding
    private lateinit var patternView: PatternLockView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUserId: String? = null
    private var isSettingMode = true // true: 패턴 설정, false: 패턴 검증
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=Ais08LockscreenPatternVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid

        patternView = binding.patternView

        patternView.setOnPatternListener(object : PatternLockView.OnPatternListener {
            override fun onPatternComplete(pattern: List<Int>) {
                verifyPattern(pattern)
                //여기 finish를 넣으면 인증 성공 여부와 무관하게 그냥 종료됩니다.
                //finish()
            }
        })
        //checkExistingPattern()
    }
//    private fun checkExistingPattern() {
//        currentUserId?.let { userId ->
//            lifecycleScope.launch {
//                try {
//                    val document = firestore.collection("User").document(userId).get().await()
//                    val patternData = document.get("pattern") as? List<*>
//
//                    if (patternData != null && patternData.isNotEmpty()) {
//                        val hasPattern = patternData[0] as? Boolean ?: false
//                        isSettingMode = !hasPattern
//
//                        if (isSettingMode) {
//                            Toast.makeText(this@Ac0_05_pattern_verify, "새 패턴을 설정하세요", Toast.LENGTH_LONG).show()
//                        } else {
//                            Toast.makeText(this@Ac0_05_pattern_verify, "패턴을 입력하여 인증하세요", Toast.LENGTH_SHORT).show()
//                            Toast.makeText(this@Ac0_05_pattern_verify," ",Toast.LENGTH_SHORT)
//                        }
//                    }
//                } catch (e: Exception) {
//                    Toast.makeText(this@Ac0_05_pattern_verify, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
    private fun verifyPattern(inputPattern: List<Int>) {
        currentUserId?.let { userId ->
            lifecycleScope.launch {
                try {
                    val document = firestore.collection("User").document(userId).get().await()
                    val patternData = document.get("pattern") as? List<Long>

                    if (patternData != null && patternData.isNotEmpty()) {
                        val hasPattern = patternData[0] == 1L

                        if (!hasPattern) {
                            Toast.makeText(this@Ac0_05_pattern_verify, "설정된 패턴이 없습니다", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this@Ac0_05_pattern_verify, "패턴 인증 성공!", Toast.LENGTH_SHORT).show()
                            // 여기서 다음 화면으로 이동하거나 추가 작업 수행
                            //AppLockAccessibilityService.isLockActivityRunning=false
                            //어플 완전 종료인데 이거하면 뭐 어뗗게 되는거지
                            getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
                                .edit().putLong("lastUnlockTime", System.currentTimeMillis()).apply()
//                            finishAffinity();
//                            System.runFinalization();
//                            System.exit(0);
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
                        } else {
                            Toast.makeText(this@Ac0_05_pattern_verify, "잘못된 패턴입니다", Toast.LENGTH_SHORT).show()
                        }
                    }

                    patternView.clearPattern()

                } catch (e: Exception) {
                    Toast.makeText(this@Ac0_05_pattern_verify, "패턴 검증 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}