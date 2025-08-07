package com.naver.appLock.ac4_screenlock.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.widget.Toast
import com.naver.appLock.ac4_screenlock.MyDeviceAdminReceiver

object LockManager {

    fun lockNow(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val compName = ComponentName(context, MyDeviceAdminReceiver::class.java)

        if (dpm.isAdminActive(compName)) {
            dpm.lockNow()
        } else {
            Toast.makeText(context, "잠금을 위해 관리자 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }
}
