package com.losthiro.ottohubclient.utils;

/**
 * @Author Hiro
 * @Date 2025/06/12 03:08
 */
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.content.Context;
import android.app.Notification;

public class NotificationUtils {
    public static final String TAG = "NotificationUtils";

    public static Notification.Builder createChannel(Context c, String id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(id, "OTTOHub Channel", importance);
            channel.setDescription("OTTOHub Client Helper");
            NotificationManager notificationManager = c.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        return new Notification.Builder(c, id);
    }


}
