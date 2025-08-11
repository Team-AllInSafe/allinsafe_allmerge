/*
 * Copyright (c) 2012-2025 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.ac4_screenlock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.blinkt.openvpn.R

object NotificationHelper {
    private const val CHANNEL_ID = "allinSafe_screenLock_channel"
    private const val CHANNEL_NAME = "AllinSafe Notifications"

    // 알림을 띄우기 위해서는 채널이란걸 하나씩 만들어야 함
    // 채널 만들기
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "This is my app's notification channel."
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 알림이 필요할 때마다 이 함수를 호출
    fun showBasicNotification(context: Context, title: String, content: String) : Notification {
//        val notificationId = 1 // 알림마다 고유 ID 부여

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            //.setSmallIcon(R.drawable.allinsafeicon) //todo 앱이름 바뀌면 수정 필요
            .setSmallIcon(R.mipmap.ic_launcher) //왜 이걸해도 앱 아이콘이 안 보이지?
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)      // 사용자가 밀어서 지울 수 없게 설정
            .setAutoCancel(false)  // 사용자가 탭해도 사라지지 않게 설정
            // => 앱이 계속 포그라운드에 있게 해줌 => 화면 잠금 서비스가 종료되지 않게 함

        return builder.build()
        // 알림 표시 => 일회성 알림 표시. 우리는 포그라운드로 계속 있는 알림이기에 service에서 알림 띄움
//        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

}