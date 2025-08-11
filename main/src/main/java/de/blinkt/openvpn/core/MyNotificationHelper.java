package de.blinkt.openvpn.core;

//public class MyNotificationHelper {
//}

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import de.blinkt.openvpn.R;

public final class MyNotificationHelper {

    private static final String CHANNEL_ID = "allinSafe_screenLock_channel";
    private static final String CHANNEL_NAME = "AllinSafe Notifications";

    // 생성자를 private으로 막아서 외부에서 객체 생성을 방지합니다 (싱글턴 패턴).
    private MyNotificationHelper() {}

    /**
     * 알림 채널을 생성합니다.
     * Android 8.0 (Oreo) 이상에서만 필요하며, 여러 번 호출해도 한 번만 생성됩니다.
     * @param context 애플리케이션 또는 서비스의 Context
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("This is my app's notification channel.");

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 포그라운드 서비스에 사용할 기본 Notification 객체를 생성하여 반환합니다.
     * @param context 애플리케이션 또는 서비스의 Context
     * @param title 알림의 제목
     * @param content 알림의 내용
     * @return 생성된 Notification 객체
     */
    public static Notification showBasicNotification(Context context, String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // 앱 아이콘 설정
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)      // 사용자가 밀어서 지울 수 없게 설정
                .setAutoCancel(false); // 사용자가 탭해도 사라지지 않게 설정

        return builder.build();
    }
}