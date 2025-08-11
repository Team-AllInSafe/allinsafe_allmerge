/*
 * Copyright (c) 2012-2025 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.ac4_screenlock

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import de.blinkt.openvpn.ac4_screenlock.receiver.LockStatusReceiver

// 화면을 띄워주는 역할을 하는 서비스입니다.
// 서비스에서 이 일을 수행하기 때문에, 앱이 꺼져있어도 돌아갑니다.
// 서비스가 메모리 회수당하지 않게, 고정 알림창을 띄워놓습니다.
class LockScreenService : Service() {
    private val CHANNEL_ID = "allinSafe_screenLock_channel"
    private val lockReceiver = LockStatusReceiver()
    private var isRegistered =false

    // 서비스 시작
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("allinsafescreenlock", "[screenlock test] LockScreenService 서비스 시작")

        val NOTIFICATION_ID = 1
        // 알림 채널 생성
        NotificationHelper.createNotificationChannel(this) //여러번 호출돼도 최초 이후 무시됨
        // Notification 객체 받기
        val notification = NotificationHelper.showBasicNotification(this,"화면 잠금 사용 중","AllinSafe 화면 잠금 기능이 실행 중입니다")
        // 포그라운드 실행 & 고정 알림 띄우기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Log.d("allinsafescreenlock", "[screenlock test] 빌드 버전 분기 진입")
            val serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE; // todo 구글스토어에 올릴 때 소명 필요
            startForeground(NOTIFICATION_ID, notification,serviceType)
        }else{
            startForeground(NOTIFICATION_ID, notification)
        }
//        startForeground(NOTIFICATION_ID, notification)
        // 리시버(화면 켜짐 감지기) 시작
        startReceiver()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startReceiver(){
        Log.d("allinsafescreenlock", "[screenlock test] startReceiver 리시버 시작")
        // 이 신호만 받을거에요~ 선언
        val screenStateFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON) // 화면이 켜졌을 때
        }
        registerReceiver(lockReceiver,screenStateFilter)
        isRegistered=true
    }
    private fun stopLockReceiver() {
        Log.d("allinsafescreenlock", "[screenlock test] startReceiver 리시버 끝")
        if (isRegistered){
            unregisterReceiver(lockReceiver)
        }
    }

    override fun onDestroy() {
        stopLockReceiver()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        //바인드 사용 안함
        return null
    }

}