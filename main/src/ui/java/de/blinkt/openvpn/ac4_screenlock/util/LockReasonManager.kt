package com.naver.appLock.ac4_screenlock.util

import android.content.Context
import android.content.SharedPreferences

object LockReasonManager {

    private const val PREF_NAME = "lock_reason_prefs"
    private const val KEY_REASON = "last_lock_reason"

    fun saveReason(context: Context, reason: String) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_REASON, reason).apply()
    }

    fun getReason(context: Context): String? {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_REASON, null)
    }

    fun clearReason(context: Context) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_REASON).apply()
    }

    fun hasReason(context: Context): Boolean {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.contains(KEY_REASON)
    }
}
