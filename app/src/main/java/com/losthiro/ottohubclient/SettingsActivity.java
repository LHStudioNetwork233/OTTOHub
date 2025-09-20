package com.losthiro.ottohubclient;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import com.losthiro.ottohubclient.adapter.model.Account;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.utils.ApplicationUtils;
import com.losthiro.ottohubclient.utils.DeviceUtils;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import com.losthiro.ottohubclient.utils.StringUtils;
import com.losthiro.ottohubclient.utils.SystemUtils;
import com.losthiro.ottohubclient.view.LogView;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.Toast;
import android.os.Build;
import android.text.*;
import android.widget.TextView.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import android.app.*;
import android.animation.*;
import android.provider.*;
import android.net.*;
import com.losthiro.ottohubclient.impl.*;
import android.content.*;
import com.losthiro.ottohubclient.view.dialog.*;
import com.losthiro.ottohubclient.utils.*;
import androidx.recyclerview.widget.*;
import com.losthiro.ottohubclient.adapter.setting.*;
import com.losthiro.ottohubclient.adapter.model.*;
import java.util.*;
import android.graphics.drawable.*;
import androidx.core.graphics.drawable.*;
import java.io.*;
import androidx.appcompat.app.AppCompatDelegate;
import android.os.Handler;
import android.database.*;
import cn.jzvd.*;
import com.losthiro.ottohubclient.view.window.*;
import org.json.*;
import com.losthiro.ottohubclient.function.*;

/**
 * @Author Hiro
 * @Date 2025/06/06 15:01
 */
public class SettingsActivity extends BasicActivity {
	public static final String TAG = "SettingsActivity";
	private static final HashMap<String, Object> callbacks = new HashMap<>();
	private RecyclerView main;
	private LogView mainLog;
	private PopupWindow window;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "setting activity create");
		setContentView(R.layout.activity_settings);
		main = findViewById(R.id.settings_list);
        main.setLayoutManager(new GridLayoutManager(this, 1));
        main.setHasFixedSize(true);
        main.setDrawingCacheEnabled(true);
		main.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		//InfoWindow.getInstance(this);
		//request();
		initSettings();
		initWindow();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onPostCreate(savedInstanceState);
        List<SettingBasic> settings = initSetting();
		main.setAdapter(new SettingsAdapter(this, settings));
        main.setItemViewCacheSize(settings.size() - 1);
	}

	@Override
	protected void onDestroy() {
		try {
			ClientSettings.getInstance().release();
		} catch (Exception e) {
			Log.e(TAG, "release setting failed", e);
		}
		super.onDestroy();
		if (mainLog != null) {
			mainLog.stopLogging();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO: Implement this method
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && data != null) {
			Uri uri = data.getData();
			if (uri == null) {
				return;
			}
			if (requestCode == FILE_REQUEST_CODE) {
				final File f = getRealFile(this, uri);
				if (f == null) {
					return;
				}
				if (!f.isFile() || !f.getName().endsWith(".zip") || !f.getName().startsWith("OV")) {
					Toast.makeText(this, "格式错误，请重新选择", Toast.LENGTH_SHORT).show();
					return;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("确定导入文件？");
				builder.setCancelable(false);
				builder.setMessage("要求.zip格式，内部结构要与缓存视频的格式一致，开头大写ov+ov号");
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dia, int which) {
						FileUtils.ZIPUtils.unzipFile(SettingsActivity.this, f.toString(),
								FileUtils.getStorage(SettingsActivity.this, PATH_SAVE));
						Toast.makeText(getApplication(), "导入成功(如果不显示或出现异常就是格式不符合)", Toast.LENGTH_SHORT).show();
						dia.dismiss();
					}
				});
				builder.setNegativeButton(android.R.string.cancel, null);
				builder.create().show();
			}
			if (requestCode == IMAGE_REQUEST_CODE) {
				final String path = getMediaPath(uri);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("确定导入背景？");
				builder.setCancelable(false);
				builder.setMessage(path);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dia, int which) {
						ClientSettings.getInstance().putValue(ClientSettings.SettingPool.SYSTEM_SPLASH_BG, path);
						Toast.makeText(getApplication(), "导入成功(原图片被删除后，该设置会失效)", Toast.LENGTH_SHORT).show();
						dia.dismiss();
					}
				});
				builder.setNegativeButton(android.R.string.cancel, null);
				builder.create().show();
			}
		}
	}

	private void initSettings() {
		callbacks.putIfAbsent(Logout.TAG, new Logout(getApplication()));
		callbacks.putIfAbsent(ClientSettings.SettingPool.SYSTEM_STORAGE_EDIT,
				new SettingEdittext.OnTextChangeListener() {
					@Override
					public void onChange(String newText) {
						// TODO: Implement this method
						if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
							Toast.makeText(getApplication(), "不支持的android版本，外部存储被限制访问", Toast.LENGTH_SHORT).show();
							return;
						}
						storageDia(newText);
					}
				});
		callbacks.putIfAbsent(ClearCache.TAG, new ClearCache(this));
		callbacks.putIfAbsent(Import.TAG_VIDEO, new Import(this, FILE_REQUEST_CODE));
		callbacks.putIfAbsent(Import.TAG_IMAGE, new Import(this, IMAGE_REQUEST_CODE));
		callbacks.putIfAbsent(ClientSettings.SettingPool.PLAYER_IMAGE_DISPLAY, new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				displayDia();
			}
		});
		callbacks.putIfAbsent(ClientSettings.SettingPool.SYSTEM_SWITCH_THEME, new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					themeDia();
				} else {
                    Toast.makeText(getApplication(), "旧版Android不支持切换暗色模式", Toast.LENGTH_SHORT).show();
                }
			}
		});
		callbacks.putIfAbsent(RequestPermission.TAG, new RequestPermission(this));
		callbacks.putIfAbsent(Reset.TAG, new Reset(getApplication()));
		callbacks.putIfAbsent(UriLoader.TAG_GROUP, new UriLoader(getApplication(),
				"http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=q8rrsHOdfjuh53isV_itsduydqiWgyUK&authKey=Dlb5xiPw0nwjBP%2BYg0rRv%2BBVRjLrS3Ogzzo2PuItgKJyOeVU6PP5qBhnY%2B72HomV&noverify=0&group_code=1039356559"));
		callbacks.putIfAbsent(BugReport.TAG, new BugReport(this));
		callbacks.putIfAbsent(Share.TAG,
				new Share(this, "OTTOHub邀请你来体验APP端啦~\n点击下方链接下载↓\nhttps://www.123pan.com/s/fqQojv-oyZJH.html"));
		callbacks.putIfAbsent(ActivityLoader.TAG_INFO, new ActivityLoader(this, InfoActivity.class));
	}

	private List<SettingBasic> initSetting() {
		List<SettingBasic> data = new ArrayList<>();
		String content = FileUtils.readFile(this, R.raw.ui_categories);
		if (content.isEmpty()) {
			return data;
		}
		try {
			JSONObject json = new JSONObject(content);
			JSONArray categories = json.optJSONArray("categories");
			if (categories == null) {
				return data;
			}
			for (int i = 0; i < categories.length(); i++) {
				int id = ResourceUtils.getResID(StringUtils.strCat("ui_", categories.optString(i)), "raw");
				JSONObject subUI = new JSONObject(FileUtils.readFile(this, id));
				data.add(new SettingTitle(subUI.optString("name", "客户端设置")));
				if (subUI.has("items")) {
					data.addAll(readType(subUI, 0));
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "load json ui failed ", e);
		}
		return data;
	}

	private List<SettingBasic> readType(JSONObject json, int count) throws Exception {
		List<SettingBasic> currentData = new ArrayList<>();
		if (count > 3) {
			return currentData;
		}
		JSONArray items = json.optJSONArray("items");
		for (int i = 0; i < items.length(); i++) {
			JSONObject item = items.optJSONObject(i);
			if (!item.has("type") || !item.has("path")) {
				throw new IllegalArgumentException("not found argument \"type\" or \"path\"! ");
			}
			ClientSettings setting = ClientSettings.getInstance();
			final String path = item.optString("path");
			String title = readJSONString(item.optString("title"));
			String msg = readJSONString(item.optString("msg"));
			Object callback = callbacks.get(path);
			SettingBasic current = null;
			switch (item.optString("type")) {
				case "action" :
					if (callback instanceof Runnable) {
						current = new SettingAction(title, msg, (Runnable) callback);
					}
					break;
				case "toggle" :
					if (item.has("items")) {
						current = new SettingToggle(title, msg, readType(items.optJSONObject(i), count++));
					} else {
						current = new SettingToggle(title, msg, setting.getBoolean(path));
					}
					((SettingToggle) current).setOnToggleChangeListener(new SettingToggle.OnToggleChangeListener() {
						@Override
						public void onChange(boolean isToggle) {
							// TODO: Implement this method
							ClientSettings.getInstance().putValue(path, isToggle);
						}
					});
					break;
				case "slider" :
					break;
				case "color" :
					break;
				case "edit" :
					current = new SettingEdittext(title, msg, item.optString("hint"), setting.getString(path));
					String btnText = item.optString("btn_text");
					if (!btnText.isEmpty()) {
						((SettingEdittext) current).setBtnText(btnText);
					}
					if (callback instanceof SettingEdittext.OnTextChangeListener) {
						((SettingEdittext) current)
								.setOnTextChangeListener((SettingEdittext.OnTextChangeListener) callback);
					}
					break;
			}
			if (current != null) {
				String icon = item.optString("icon");
				int id = ResourceUtils.getResID(icon, "drawable");
				current.setIcon(id);
				current.setTag(path);
				currentData.add(current);
			}
		}
		return currentData;
	}

	private String readJSONString(String content) {
		int id = ResourceUtils.getResID(content, "string");
		if (id == 0) {
			return content;
		}
		return getString(id);
	}

	private File getRealFile(Context context, Uri uri) {
		File file = null;
		if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
			file = new File(uri.getPath());
		} else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
			ContentResolver contentResolver = context.getContentResolver();
			Cursor cursor = contentResolver.query(uri, null, null, null, null);
			if (cursor.moveToFirst()) {
				String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
				try {
					InputStream is = contentResolver.openInputStream(uri);
					File cache = new File(context.getExternalCacheDir().getAbsolutePath(), displayName);
					FileOutputStream fos = new FileOutputStream(cache);
					FileUtils.copyFile(is, fos);
					file = cache;
					fos.close();
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return file;
	}

	private String getMediaPath(Uri uri) {
		String path = null;
		Cursor cursor = null;
		try {
			String[] projection = {MediaStore.Images.Media.DATA};
			cursor = getContentResolver().query(uri, projection, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				path = cursor.getString(columnIndex);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return path;
	}

	private void initWindow() {
		mainLog = new LogView(this);
		mainLog.startLogging();
		window = new PopupWindow(-1, -1);
		window.setContentView(main);
		window.setTouchable(false);
		window.setFocusable(false);
	}

	//	private void request() {
	//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	//		builder.setTitle("需要权限");
	//		builder.setMessage("请给予应用悬浮窗权限，否则无法启用一些内容");
	//		builder.setCancelable(false);
	//		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	//			@Override
	//			public void onClick(DialogInterface dialog, int which) {
	//				
	//			}
	//		});
	//		builder.setNegativeButton(android.R.string.cancel, null);
	//		builder.create().show();
	//	}

	private void displayDia() {
		int currentTheme = ClientSettings.getInstance().getInt(ClientSettings.SettingPool.PLAYER_IMAGE_DISPLAY,
				Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER);
		int index = 0;
		switch (currentTheme) {
			case Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT :
				index = 1;
				break;
			case Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP :
				index = 2;
				break;
			case Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL :
				index = 3;
				break;
		}
		final CharSequence[] chars = {"自适应(推荐)", "填充(会拉伸)", "裁剪", "原始"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("切换播放器缩放模式");
		builder.setCancelable(false);
		builder.setSingleChoiceItems(chars, index, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO: Implement this method
				int mode = Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER;
				switch (which) {
					case 1 :
						mode = Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT;
						break;
					case 2 :
						mode = Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP;
						break;
					case 3 :
						mode = Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL;
				}
				ClientSettings setting = ClientSettings.getInstance();
				setting.putValue(ClientSettings.SettingPool.PLAYER_IMAGE_DISPLAY, mode);
				Toast.makeText(getApplication(), "操作成功", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void storageDia(final String newPath) {
		if (getCurrentStorage().equals(newPath)) {
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("设置新存储位置");
		builder.setCancelable(false);
		builder.setMessage("请选择一种移动现有配置文件的方法，选择完之后将会开始转移文件");
		builder.setPositiveButton("复制(仍然保留原地址的文件)", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				if (BasicActivity.setCurrentStorage(newPath)) {
					Toast.makeText(getApplication(), "操作成功", Toast.LENGTH_SHORT).show();
					FileUtils.copyFile(SettingsActivity.this, BasicActivity.getCurrentStorage(), newPath);
				} else {
					Toast.makeText(getApplication(), "Android版本过高或出现未知错误", Toast.LENGTH_SHORT).show();
				}
				dia.dismiss();
			}
		});
		builder.setNeutralButton("移动(复制完之后删除原地址的所有文件)", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO: Implement this method
				if (BasicActivity.setCurrentStorage(newPath)) {
					Toast.makeText(getApplication(), "操作成功", Toast.LENGTH_SHORT).show();
					FileUtils.moveFile(SettingsActivity.this, BasicActivity.getCurrentStorage(), newPath);
				} else {
					Toast.makeText(getApplication(), "Android版本过高或出现未知错误", Toast.LENGTH_SHORT).show();
				}
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void themeDia() {
		int currentTheme = ClientSettings.getInstance().getInt(ClientSettings.SettingPool.SYSTEM_SWITCH_THEME,
				AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		int index = 2;
		switch (currentTheme) {
			case AppCompatDelegate.MODE_NIGHT_YES :
				index = 1;
				break;
			case AppCompatDelegate.MODE_NIGHT_NO :
				index = 0;
				break;
		}
		final CharSequence[] chars = {"亮色模式", "暗色模式", "跟随系统"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("主题切换");
		builder.setCancelable(false);
		builder.setSingleChoiceItems(chars, index, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO: Implement this method
				switchDia(chars[which].toString(), which);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void switchDia(String text, final int index) {
		final Runnable callback = new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				SystemUtils.restart(SettingsActivity.this);
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(StringUtils.strCat("确定切换到:", text));
		builder.setMessage("这将会重启应用，请做好准备");
		builder.setCancelable(false);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO: Implement this method
				switchTheme(index, callback);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void switchTheme(int current, Runnable callback) {
		try {
			int newTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
			switch (current) {
				case 0 :
					newTheme = AppCompatDelegate.MODE_NIGHT_NO;
					break;
				case 1 :
					newTheme = AppCompatDelegate.MODE_NIGHT_YES;
					break;
			}
			ClientSettings setting = ClientSettings.getInstance();
			setting.putValue(ClientSettings.SettingPool.SYSTEM_SWITCH_THEME, newTheme);
			setting.release();
			AppCompatDelegate.setDefaultNightMode(newTheme);
			Toast.makeText(getApplication(), "主题设置成功，即将重启", Toast.LENGTH_SHORT).show();
			new Handler().postDelayed(callback, 1000L);
		} catch (Exception e) {
			Log.e(TAG, "switch theme error", e);
		}
	}

	public void saveLog(View v) {
		Object[] name = {FileUtils.getStorage(this, null), "OTTOHub_runlog_",
				SystemUtils.getDate("yyyy_MM_dd_HH_mm_ss_"), SystemUtils.getTime(), ".log"};
		if (mainLog != null) {
			mainLog.saveLog(StringUtils.strCat(name));
			Log.i(TAG, "log save success");
		}
	}

	public void quit(View v) {
		finish();
	}

	public static void permission() {
		Object callback = callbacks.get(RequestPermission.TAG);
		if (callback != null && callback instanceof Runnable) {
			((Runnable) callback).run();
		}
	}
}

