package com.losthiro.ottohubclient.service;
import android.app.Service;
import android.os.IBinder;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.net.Uri;
import android.util.Log;
import com.losthiro.ottohubclient.PlayerActivity;
import android.app.PendingIntent;
import android.app.Notification;
import android.os.Build;
import com.losthiro.ottohubclient.R;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.widget.MediaController;
import android.view.View;
import android.view.SurfaceView;
import android.os.Binder;
import com.losthiro.ottohubclient.utils.NotificationUtils;
import android.annotation.*;
import android.os.Bundle;
import android.widget.RemoteViews;
import androidx.core.app.ServiceCompat;

/**
 * @Author Hiro
 * @Date 2025/06/04 11:37
 */
public class PlayerService extends Service {
    public static final String TAG = "PlayerService";
    private static MediaPlayer videoPlayer;
    private PowerManager.WakeLock wakeLock;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }
        Bundle call=intent.getBundleExtra("play_callback");
        if (call == null) {
            return START_NOT_STICKY;
        }
        Intent callback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            callback = call.getParcelable("play_callback", Intent.class);
        } else {
            callback = call.getParcelable("play_callback");
        }
        if (callback == null) {
            return START_NOT_STICKY;
        }
        PendingIntent pending;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pending = PendingIntent.getActivity(this, 0, callback, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pending = PendingIntent.getActivity(this, 0, callback, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Notification.Builder builder=NotificationUtils.createChannel(this, "VIDEO_CHANNEL");
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle("视频播放中");
        builder.setContentText("点击查看详情");
        builder.setContentIntent(pending);
        initPlayer(intent);
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, builder.build(), 2);
        } else {
            startForeground(1, builder.build());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OTTOHub:player_wakelock");
            wakeLock.acquire();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private void initPlayer(final Intent info) {
        videoPlayer = new MediaPlayer();
        try {
            String source=info.getStringExtra("video_source");
            if (info.getBooleanExtra("is_local", false)) {
                videoPlayer.setDataSource(source);
            } else {
                videoPlayer.setDataSource(this, Uri.parse(source));
            }
            videoPlayer.prepareAsync();
            videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.seekTo(info.getIntExtra("current_pos", 0));
                        mp.start();
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "player error", e);
        }
    }

    public static int getCurrent() {
        if (videoPlayer == null) {
            return 0;
        }
        return videoPlayer.getCurrentPosition();
    }
}
