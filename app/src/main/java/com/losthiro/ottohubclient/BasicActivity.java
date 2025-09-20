package com.losthiro.ottohubclient;

import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.lang.ref.WeakReference;
import com.losthiro.ottohubclient.impl.UploadManager;
import java.io.*;
import android.os.Build;
import android.app.AlertDialog;
import com.losthiro.ottohubclient.utils.*;
import android.widget.*;
import com.losthiro.ottohubclient.impl.*;
import org.json.*;
import java.util.concurrent.*;
import android.view.*;
import android.content.*;
import android.graphics.*;
import android.content.res.*;

public class BasicActivity extends AppCompatActivity {
	public static final int LOGIN_REQUEST_CODE = 114;
	public static final int IMAGE_REQUEST_CODE = 514;
	public static final int VIDEO_REQUEST_CODE = 1919;
	public static final int AVATAR_REQUEST_CODE = 810;
	public static final int FILE_REQUEST_CODE = 233;

	public static final long VIDEO_SIZE = 200 * 1024 * 1024;
	public static final long COVER_SIZE = 1024 * 1024;

	public static final String OLD_STORAGE = "/sdcard/OTTOHub/";
	public static final String PATH_DRAFT = "/draft/";
	public static final String PATH_SAVE = "/save/";
	public static final String FILE_MSG_DRAFT = "config/content_cache.json";
	public static final String FILE_SEARCH_HISTORY = "config/history_search.json";
	public static final String FILE_DEF_DICTIONARY = "config/rng_danmaku_config.json";

	private static final Semaphore request = new Semaphore(1);
	private static String currentStorage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Main", getClass().getName() + "has create");
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onPostCreate(savedInstanceState);
		View decor = getWindow().getDecorView();
		decor.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
			@Override
			public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
				// TODO: Implement this method
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
					int top = insets.getInsets(WindowInsets.Type.statusBars()).top;
					int bottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
						v.setPadding(0, top, 0, bottom);
					}
				}
				v.setFitsSystemWindows(true);
				return insets;
			}
		});
		Configuration config = getResources().getConfiguration();
		if (ClientSettings.getInstance().getBoolean(ClientSettings.SettingPool.SYSTEM_USE_DECOR)
				|| config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setFullscreen(true);
		} else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			setFullscreen(false);
		}
	}

	@Override
	public void onLowMemory() {
		// TODO: Implement this method
		super.onLowMemory();
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		build.setCancelable(false);
		build.setTitle("爆破预警");
		build.setMessage("内存已经不够啦，为了使APP正常运行，请留出足够内存哦");
		build.setPositiveButton(android.R.string.ok, null);
		build.create().show();
		cleanCache(false);
	}

	public void cleanCache(boolean show) {
		FileUtils.clearDir(FileUtils.getStorage(getApplication(), null), new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				// TODO: Implement this method
				return pathname.isFile() && pathname.getName().endsWith(".log");
			}
		});
		String msg = SystemUtils.clearCache(getApplication()) ? "缓存清理成功" : "缓存清理失败";
		if (show) {
			Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
		}
	}

	public static String getCurrentStorage() {
		if (currentStorage == null) {
			currentStorage = ClientSettings.getInstance().getString(ClientSettings.SettingPool.SYSTEM_STORAGE_EDIT,
					OLD_STORAGE);
		}
		return currentStorage;
	}

	public static boolean setCurrentStorage(String newStorage) {
		File path = new File(newStorage);
		boolean isSuccess = DeviceUtils.getAndroidSDK() < Build.VERSION_CODES.R && path.exists() && path.isDirectory();
		if (isSuccess) {
			currentStorage = newStorage;
			ClientSettings.getInstance().putValue(ClientSettings.SettingPool.SYSTEM_STORAGE_EDIT, newStorage);
		}
		return isSuccess;
	}

	public void checkServer(final Runnable callback) {
		try {
			if (!request.tryAcquire()) {
				return;
			}
			if (!NetworkUtils.isNetworkAvailable(this)) {
				Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
				return;
			}
			NetworkUtils.getNetwork.getNetworkJson(APIManager.SystemURI.getVersionURI(),
					new NetworkUtils.HTTPCallback() {
						@Override
						public void onSuccess(String content) {
							if (content == null || content.isEmpty()) {
								onFailed("empty content");
								return;
							}
							try {
								JSONObject json = new JSONObject(content);
								if (json == null) {
									onFailed("null json");
									return;
								}
								String status = json.optString("status", "error");
								if (status.equals("success")) {
									return;
								}
								onFailed(json.optString("message"));
							} catch (JSONException e) {
								onFailed(e.toString());
							}
						}

						@Override
						public void onFailed(final String cause) {
							Log.e("Network", cause);
							runOnUiThread(callback);
						}
					});
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		} finally {
			request.release();
		}
	}

	private void setFullscreen(boolean isEnable) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
			int fullscreen = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
			getWindow().getDecorView().setSystemUiVisibility(isEnable ? fullscreen : View.SYSTEM_UI_FLAG_VISIBLE);
		} else {
			int views = WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars();
			WindowInsetsController controller = getWindow().getInsetsController();
			if (controller != null) {
				if (isEnable) {
					controller.hide(views);
				} else {
					controller.show(views);
				}
			}
		}
	}
}

