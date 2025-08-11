package de.blinkt.openvpn.ac4_screenlock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import de.blinkt.openvpn.ac4_screenlock.pinlock.PinLockActivity
import de.blinkt.openvpn.ac4_screenlock.pinlock.PinStorageManager
import de.blinkt.openvpn.ac4_screenlock.util.TwoFactorAuthManager
import de.blinkt.openvpn.ac4_screenlock.util.LockReasonManager

class LockStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("allinsafescreenlock", "[screenlock test] 리시버 onReceive 실행")
        when(intent.action){
            Intent.ACTION_SCREEN_ON->{
                // 잠금 화면 띄우기
                // 원래 있던 로직 가져오기
                Log.d("allinsafescreenlock", "[screenlock test] Intent.ACTION_SCREEN_ON 감지")
//                val is2FA = TwoFactorAuthManager.is2FAEnabled(context)
                val hasPin = PinStorageManager.isPinSet(context) // <- 얘만 사용, PinActivity에서 불러옴
//                val hasReason = LockReasonManager.hasReason(context)

//                Log.d("PinFlowCheck", "📡 브로드캐스트 수신 → 2FA: $is2FA, PinSet: $hasPin, HasReason: $hasReason")
//                if (is2FA && hasPin && hasReason) {
                if (hasPin) { //우선 테스트를 위해 이것만 체크
                    val pinIntent = Intent(context, PinLockActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    Log.d("allinsafescreenlock", "[screenlock test] PinActivity 실행")
                    context.startActivity(pinIntent)
                } else {
                    Log.d("allinsafescreenlock", "[screenlock test]🔕 잠금 설정 안됨")
                    //todo 잠금화면 설정하는 화면으로
                }
            }
        }

        // 25.08.10 테스트를 위해 주석처리
//        if (intent.action == Intent.ACTION_USER_PRESENT) {
//            val is2FA = TwoFactorAuthManager.is2FAEnabled(context)
//            val hasPin = PinStorageManager.isPinSet(context)
//            val hasReason = LockReasonManager.hasReason(context)
//
//            Log.d("PinFlowCheck", "📡 브로드캐스트 수신 → 2FA: $is2FA, PinSet: $hasPin, HasReason: $hasReason")
//
//            if (is2FA && hasPin && hasReason) {
//                val pinIntent = Intent(context, PinLockActivity::class.java).apply {
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                }
//                context.startActivity(pinIntent)
//            } else {
//                Log.d("PinFlowCheck", "🔕 조건 불충족 → 인증 화면 띄우지 않음")
//            }
//        }
    }
}
