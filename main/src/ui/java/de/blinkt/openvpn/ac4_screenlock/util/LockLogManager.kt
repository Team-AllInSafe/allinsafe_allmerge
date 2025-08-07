package de.blinkt.openvpn.ac4_screenlock.util

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LockLogManager {

    fun log(context: Context, reason: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "$timestamp - 잠금 사유: $reason"
        Log.d("LockLog", logEntry)

        // 향후 Firebase 업로드 또는 파일 저장을 위한 포맷
        // 예: 저장 예정 => context.filesDir.resolve("lock_logs.txt").appendText(logEntry + "\n")
    }
}
