//조건2:잠금 해제 실패 횟수 확인을 기능을 위한 리시버.
//받아오는 관리자 권한 역시 횟수 확인을 위한 것이다.

package com.naver.appLock.ac4_screenlock

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.naver.appLock.ac4_screenlock.util.LockLogManager
import com.naver.appLock.ac4_screenlock.util.LockManager
import com.naver.appLock.ac4_screenlock.util.LockReasonManager

class MyDeviceAdminReceiver : DeviceAdminReceiver() {

    // ✅ 관리자 권한 부여됨
    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "관리자 권한이 부여되었습니다", Toast.LENGTH_SHORT).show()
    }

    // ✅ 관리자 권한 해제됨
    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "관리자 권한이 해제되었습니다", Toast.LENGTH_SHORT).show()
    }

    // ✅ 시스템으로부터 어떤 브로드캐스트든 올 경우 확인용
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MyDeviceAdminReceiver", "📡 onReceive 호출됨! action = ${intent.action}")
        super.onReceive(context, intent)
    }

    // ✅ 잠금 해제 실패 감지
    override fun onPasswordFailed(context: Context, intent: Intent) {
        Log.d("MyDeviceAdminReceiver", "❌ 잠금 해제 실패 감지됨")

        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
        val compName = getComponentName(context)
        val failedCount = dpm.getCurrentFailedPasswordAttempts()

        Log.d("MyDeviceAdminReceiver", "❌ 실패 횟수: $failedCount")

        val threshold = 5
        if (failedCount >= threshold) {
            val reason = "잠금 해제 실패 ${failedCount}회 감지"
            Log.d("MyDeviceAdminReceiver", "🚨 기준 초과! 사유: $reason")

            LockReasonManager.saveReason(context, reason)
            LockLogManager.log(context, reason)
            LockManager.lockNow(context)

            Log.d("MyDeviceAdminReceiver", "🔒 기기 잠금 실행됨")
        }
    }

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, MyDeviceAdminReceiver::class.java)
        }
    }
}
