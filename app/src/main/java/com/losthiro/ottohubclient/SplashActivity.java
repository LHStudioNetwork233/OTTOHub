package com.losthiro.ottohubclient;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import com.losthiro.ottohubclient.adapter.model.Video;
import com.losthiro.ottohubclient.adapter.VideoAdapter;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import com.losthiro.ottohubclient.utils.SystemUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.losthiro.ottohubclient.impl.AccountManager;
import android.graphics.drawable.*;
import com.losthiro.ottohubclient.utils.*;
import android.graphics.*;
import com.losthiro.ottohubclient.impl.*;
import androidx.appcompat.app.*;
import android.view.*;
import android.widget.*;
import java.io.*;

/**
 * @Author Hiro
 * @Date 2025/05/21 14:14
 */
public class SplashActivity extends BasicActivity {
	public static final String TAG = "SplashActivity";
	private final int max = 150;
	private int time;
	private ProgressBar progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int theme = ClientSettings.getInstance().getInt(ClientSettings.SettingPool.SYSTEM_SWITCH_THEME,
				AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		AppCompatDelegate.setDefaultNightMode(theme);
        checkServer(new Runnable(){
                @Override
                public void run() {
                    // TODO: Implement this method
                    AccountManager.getInstance(getApplication()).autoLogin();
                }
            });
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
        progress = findViewById(R.id.splash_progress);
        progress.setMax(max);
        Drawable d = progress.getProgressDrawable();
        d.setColorFilter(ResourceUtils.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        progress.setProgressDrawable(d);
        ObjectAnimator anim = ObjectAnimator.ofFloat(progress, "alpha", 0f, 1f);
        anim.setDuration(1000L);
        anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator a) {
                    startClient();
                    a.cancel();
                }
            });
		anim.start();
        ClientSettings setting = ClientSettings.getInstance();
		String path = setting.getString(ClientSettings.SettingPool.SYSTEM_SPLASH_BG);
		if (path != null) {
			File f = new File(path);
			if (f.exists() && f.isFile()) {
				ImageView bg = findViewById(R.id.splash_bg);
				bg.setImageBitmap(BitmapFactory.decodeFile(path));
                return;
			}
            setting.putValue(ClientSettings.SettingPool.SYSTEM_SPLASH_BG, null);
		}
	}

	private void startClient() {
		final Handler timer = new Handler();
		timer.postDelayed(new Runnable() {
			@Override
			public void run() {
				progress.setProgress(time);
				if (time >= max) {
					SystemUtils.loadActivity(SplashActivity.this, MainActivity.class);
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

