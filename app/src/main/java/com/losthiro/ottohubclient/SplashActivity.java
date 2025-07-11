package com.losthiro.ottohubclient;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import com.losthiro.ottohubclient.adapter.Video;
import com.losthiro.ottohubclient.adapter.VideoAdapter;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import com.losthiro.ottohubclient.utils.SystemUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.losthiro.ottohubclient.impl.AccountManager;

/**
 * @Author Hiro
 * @Date 2025/05/21 14:14
 */
public class SplashActivity extends MainActivity {
    public static final String TAG = "SplashActivity";
    private final int max=200;
    private int time;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progress = findViewById(R.id.splash_progress);
        progress.setMax(max);
        AccountManager.getInstance(this).autoLogin();
        ObjectAnimator anim = ObjectAnimator.ofFloat(progress, "alpha", 0f, 1f);
        anim.setDuration(1000L);
        anim.addListener(new AnimatorListenerAdapter(){
                @Override
                public void onAnimationEnd(Animator a) {
                    startClient();
                    a.cancel();
                }
            });
        anim.start();
    }

    private void startClient() {
        final Handler timer = new Handler();
        timer.postDelayed(new Runnable(){
                @Override
                public void run() {
                    progress.setProgress(time);
                    if (time >= max) {
                        SystemUtils.loadActivity(SplashActivity.this, VideosActivity.class);
                        timer.removeCallbacks(this);
                        finish();
                        return;
                    }
                    time++;
                    timer.postDelayed(this, 1);
                }
            }, 1);
    }
}
