package de.blinkt.openvpn.detection.common

import android.util.Log

class AlertManager {

    fun sendAlert(severity: String, title: String, message: String) {
        //Log.e("AlertManager", "[$severity] $title -> $message")
        LogManager.log("AlertManager", "[$severity] $title -> $message")
    }

    fun sendAlert(ip: String) {
        sendAlert("CRITICAL", "ARP 스푸핑 감지", "IP: $ip")
    }
}