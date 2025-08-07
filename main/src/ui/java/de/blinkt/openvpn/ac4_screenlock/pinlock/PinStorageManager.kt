package de.blinkt.openvpn.ac4_screenlock.pinlock

import android.content.Context
import android.content.SharedPreferences


object PinStorageManager {

    private const val PREF_NAME = "pin_prefs"
    private const val KEY_PIN = "user_pin"

    // pin 저장
    fun savePin(context: Context, pin: String) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    // 입력된 PIN이 저장된 PIN과 일치하는지 확인
    fun isPinCorrect(context: Context, input: String): Boolean {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedPin = prefs.getString(KEY_PIN, null)
        return savedPin == input
    }

    // PIN이 설정되어 있는지 여부 반환
    fun isPinSet(context: Context): Boolean {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.contains(KEY_PIN)
    }

    // 저장된 PIN 삭제
    fun clearPin(context: Context) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PIN).apply()
    }
}
