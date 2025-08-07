package de.blinkt.openvpn.ac4_screenlock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import de.blinkt.openvpn.ac4_screenlock.pinlock.PinLockActivity
import de.blinkt.openvpn.ac4_screenlock.pinlock.PinStorageManager
import de.blinkt.openvpn.ac4_screenlock.util.TwoFactorAuthManager
import de.blinkt.openvpn.ac4_screenlock.util.LockReasonManager

class LockStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            val is2FA = TwoFactorAuthManager.is2FAEnabled(context)
            val hasPin = PinStorageManager.isPinSet(context)
            val hasReason = LockReasonManager.hasReason(context)

            Log.d("PinFlowCheck", "📡 브로드캐스트 수신 → 2FA: $is2FA, PinSet: $hasPin, HasReason: $hasReason")

            if (is2FA && hasPin && hasReason) {
                val pinIntent = Intent(context, PinLockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(pinIntent)
            } else {
                Log.d("PinFlowCheck", "🔕 조건 불충족 → 인증 화면 띄우지 않음")
            }
        }
    }
}
