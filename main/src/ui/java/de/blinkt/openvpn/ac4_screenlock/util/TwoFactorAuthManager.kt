package de.blinkt.openvpn.ac4_screenlock.util

import android.content.Context
import android.content.SharedPreferences
import de.blinkt.openvpn.ac4_screenlock.pinlock.PinStorageManager

object TwoFactorAuthManager {
    private const val PREF_NAME = "TwoFactorAuthPrefs"
    private const val KEY_2FA_ENABLED = "is_2fa_enabled"
    private const val KEY_SCREEN_LOCK_ENABLED = "is_screen_lock_enabled"

    // ✅ 2FA 설정 상태 저장
    fun set2FAEnabled(context: Context, enabled: Boolean) {
        val prefs = getPrefs(context)
        prefs.edit().putBoolean(KEY_2FA_ENABLED, enabled).apply()
    }

    // ✅ 2FA 설정 상태 확인
    fun is2FAEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_2FA_ENABLED, false)
    }

    // ✅ PIN 존재 여부 확인 (PinStorageManager 활용)
    fun hasPin(context: Context): Boolean {
        return PinStorageManager.isPinSet(context)
    }

    // ✅ 화면잠금 기능 설정 저장
    fun setScreenLockEnabled(context: Context, enabled: Boolean) {
        val prefs = getPrefs(context)
        prefs.edit().putBoolean(KEY_SCREEN_LOCK_ENABLED, enabled).apply()
    }

    // ✅ 화면잠금 기능 설정 상태 확인
    fun isScreenLockEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SCREEN_LOCK_ENABLED, true) // 기본값 true
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}
