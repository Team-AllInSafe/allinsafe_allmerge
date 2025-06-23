package de.blinkt.openvpn.detection.common

import android.os.Handler
import android.os.Looper
import android.util.Log

object LogManager {
    private var logMessages = mutableListOf<String>()
    private val observers = mutableListOf<(List<String>) -> Unit>()
    fun log(tag: String, message: String) {
        Handler(Looper.getMainLooper()).post {
            // ✅ UI 변경은 메인 스레드에서만
            val formatted = "[$tag] $message"
            logMessages.add(formatted)
            Log.d(tag, message)
            notifyObservers()
        }

    }

    fun getLogs(): List<String> = logMessages
//    fun addLog(log: String) {
//        logMessages.add(log)
//        notifyObservers()
//    }

    fun addObserver(observer: (List<String>) -> Unit) {
        observers.add(observer)
        observer(logMessages) // 초기 로그 전달
    }

    private fun notifyObservers() {
        observers.forEach { it(logMessages) }
    }
}
