package de.blinkt.openvpn.classforui

import android.app.Application
import de.blinkt.openvpn.classforui.SpoofingDetectingStatusManager
import de.blinkt.openvpn.core.ICSOpenVPNApplication

//class MyApp: ICSOpenVPNApplication(){
class MyApp: Application(){
    override fun onCreate() {
        super.onCreate()
        SpoofingDetectingStatusManager.init(this)
    }
}