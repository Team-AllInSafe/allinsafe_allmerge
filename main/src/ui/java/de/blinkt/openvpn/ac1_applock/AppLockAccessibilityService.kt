package com.naver.appLock.ac1_applock

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.naver.appLock.ac0_patternpin.Ac0_08_pinpattern_forwarding

class AppLockAccessibilityService : AccessibilityService() {
    companion object {
        var lockedPackageList: List<String> = emptyList()
        var onoff=false
        var isLockActivityRunning = false
        fun loadLockedApps(context: Context): List<String> {
            val prefs = context.getSharedPreferences("AppPref", MODE_PRIVATE)
            return prefs.getStringSet("locked_apps", emptySet())?.toList() ?: emptyList()
        }
        fun loadOnoff(context: Context): Boolean {
            val prefs = context.getSharedPreferences("AppPref", MODE_PRIVATE)
            return prefs.getBoolean("onoff", false)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        onoff=loadOnoff(this)
        lockedPackageList=loadLockedApps(this).toList()

//        Log.d("sua", "서비스 연결됨")
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//        Log.d("sua","onAccessibilityEvent 실행됨")
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
//            Log.d("sua", "Foreground package: $packageName")

            //잠금 해제 후 10초간은 앱 상태가 바뀌어도 잠금화면이 나오지 않습니다.
            //이를 위해 sharedPreferences로 lastUnlockTime을 관리합니다.
            val prefs = getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
            val lastUnlockTime = prefs.getLong("lastUnlockTime", 0L)
            val now = System.currentTimeMillis()
            if (now - lastUnlockTime < 10_000) {
                return // 아직 쿨다운 중이면 잠금을 무시
            }

            // 차단 대상 앱이면 차단 알람 화면으로 전환
            if(onoff){
                if (packageName in lockedPackageList) {
                    //기존 알람 화면
//                    val intent = Intent(this, AlarmActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (!isLockActivityRunning){
                        isLockActivityRunning=true
//                        val intent= Intent(this@AppLockAccessibilityService,
////                            Ac0_08_pinpattern_forwarding::class.java)
////                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
////                        startActivity(intent)
                        val intent = Intent(
                            this@AppLockAccessibilityService,
                            Ac0_08_pinpattern_forwarding::class.java
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra("TARGET_PACKAGE_NAME", packageName) // 외부 앱 패키지
                        }
                        startActivity(intent)
                    }
                    }

            }else{
                Log.d("sua", "[onAccessibilityEvent] onoff is $onoff")
            }
        }
    }
    override fun onInterrupt() {
        // override 용
    }

    fun loadCheckedApps(context: Context): Set<String> {
        val prefs = context.getSharedPreferences("AppPref", MODE_PRIVATE)
        return prefs.getStringSet("locked_apps", emptySet()) ?: emptySet()
    }


}