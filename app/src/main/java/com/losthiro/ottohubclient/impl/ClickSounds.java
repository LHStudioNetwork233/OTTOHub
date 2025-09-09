/**
 * @Author Hiro
 * @Date 2025/09/08 19:25
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.impl;
import android.media.*;
import android.content.*;
import com.losthiro.ottohubclient.*;

public class ClickSounds {
	public static void playSound(int id, Context context) {
		if (id == 0 || !ClientSettings.getInstance().getBoolean(ClientSettings.SettingPool.SYSTEM_CLICK_SOUND, true)) {
			return;
		}
		AudioAttributes.Builder builder = new AudioAttributes.Builder();
		builder.setLegacyStreamType(3);
		SoundPool.Builder soundBuilder = new SoundPool.Builder();
		soundBuilder.setMaxStreams(5);
		soundBuilder.setAudioAttributes(builder.build());
		SoundPool sound = soundBuilder.build();
		sound.load(context, id, 1);
		sound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool sp, int i, int i2) {
				sp.play(1, 1.0f, 1.0f, 0, 0, 1.0f);
			}
		});
	}

	public static void playSound(Context ctx) {
		playSound(R.raw.button, ctx);
	}
}

