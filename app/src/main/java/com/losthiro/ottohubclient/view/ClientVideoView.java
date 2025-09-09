/**
 * @Author Hiro
 * @Date 2025/09/08 15:02
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.view;
import cn.jzvd.JzvdStd;
import cn.jzvd.Jzvd;
import android.content.*;
import android.util.AttributeSet;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.ui.*;
import com.losthiro.ottohubclient.R;
import android.widget.*;
import android.content.pm.*;
import android.graphics.drawable.*;
import com.losthiro.ottohubclient.utils.*;
import android.graphics.*;
import android.content.res.*;

public class ClientVideoView extends JzvdStd {
	private VideoInfoFragment.VideoInfo currentVideo;
	private boolean isLocal;

	public ClientVideoView(Context context) {
		super(context);
	}

	public ClientVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init(VideoInfoFragment.VideoInfo video, String title, boolean localMode) {
		if (video == null) {
			return;
		}
		currentVideo = video;
		isLocal = localMode;
		setUp(video.getVideo(), title, SCREEN_NORMAL);
		setVideoImageDisplayType(ClientSettings.getInstance().getInt(ClientSettings.SettingPool.PLAYER_IMAGE_DISPLAY));
        initView();
		startPreloading();
	}

	private void initView() {
		if (currentVideo == null) {
			return;
		}
		String source = currentVideo.getCover();
		if (isLocal) {
			Bitmap localMap = BitmapFactory.decodeFile(source);
			if (localMap != null) {
				posterImageView.setImageBitmap(localMap);
			}
		} else {
			ImageDownloader.loader(posterImageView, source);
		}
		posterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		Drawable progress = bottomProgressBar.getProgressDrawable();
		progress.setColorFilter(ResourceUtils.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
		bottomProgressBar.setProgressDrawable(progress);
		Drawable controlProgress = progressBar.getProgressDrawable();
		if (controlProgress instanceof LayerDrawable) {
			Drawable progressView = ((LayerDrawable) controlProgress).findDrawableByLayerId(android.R.id.progress);
			if (progressView != null) {
				progressView.setColorFilter(ResourceUtils.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
			}
		}
		progressBar.setProgressDrawable(controlProgress);
	}

	@Override
	public void onVideoSizeChanged(int width, int height) {
		// TODO: Implement this method
		super.onVideoSizeChanged(width, height);
		if (width > 0 && height > 0) {
			if (height > width) {
				Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			} else {
				Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			}
		}
	}

	@Override
	public void onCompletion() {
		// TODO: Implement this method
		if (screen == SCREEN_FULLSCREEN
				&& !ClientSettings.getInstance().getBoolean(ClientSettings.SettingPool.PLAYER_AUTO_QUIT)) {
			onStateAutoComplete();
		} else {
			super.onCompletion();
		}
		//JZUtils.setRequestedOrientation(getContext(), FULLSCREEN_ORIENTATION);
	}
}

