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

/**
 * @Author Hiro
 * @Date 2025/06/06 15:01
 */
public class SettingsActivity extends BasicActivity {
	public static final String TAG = "SettingsActivity";
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
		main.setAdapter(new SettingsAdapter(this, initSettings()));
		initWindow();
	}

	@Override
	protected void onDestroy() {
		try {
			ClientSettings.getInstance().release();
		} catch (Exception e) {
			Log.e(TAG, "release setting failed", e);
		}
		super.onDestroy();
		Intent last = Client.getLastActivity();
		if (last != null && Client.isFinishingLast(last)) {
			Client.removeActivity();
			startActivity(last);
		}
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

	private List<SettingBasic> initSettings() {
		final ClientSettings setting = ClientSettings.getInstance();
		List<SettingBasic> data = new ArrayList<>();
		data.add(new SettingTitle("客户端设置"));
		SettingToggle autoLogin = new SettingToggle("自动登录", "每次打开时自动登录上次的账号",
				setting.getBoolean(ClientSettings.SettingPool.ACCOUNT_AUTO_LOGIN));
		autoLogin.setIcon(R.drawable.ic_login_black);
		autoLogin.setOnToggleChangeListener(new SettingToggle.OnToggleChangeListener() {
			@Override
			public void onChange(boolean isToggle) {
				// TODO: Implement this method
				setting.putValue(ClientSettings.SettingPool.ACCOUNT_AUTO_LOGIN, isToggle);
			}
		});
		data.add(autoLogin);
		SettingToggle removeAccount = new SettingToggle("账号移除", "退出登录时移除列表中的当前账号",
				setting.getBoolean(ClientSettings.SettingPool.ACCOUNT_AUTO_REMOVE));
		removeAccount.setIcon(R.drawable.ic_remove_account);
		removeAccount.setOnToggleChangeListener(new SettingToggle.OnToggleChangeListener() {
			@Override
			public void onChange(boolean isToggle) {
				// TODO: Implement this method
				setting.putValue(ClientSettings.SettingPool.ACCOUNT_AUTO_REMOVE, isToggle);
			}
		});
		data.add(removeAccount);
		SettingToggle permissionCheck = new SettingToggle("权限检查", "每次启动时检查权限，而不仅是第一次启动时检查",
				setting.getBoolean(ClientSettings.SettingPool.SYSTEM_CHECK_PERMISSION));
		permissionCheck.setIcon(R.drawable.ic_inspection_black);
		permissionCheck.setOnToggleChangeListener(new SettingToggle.OnToggleChangeListener() {
			@Override
			public void onChange(boolean isToggle) {
				// TODO: Implement this method
				setting.putValue(ClientSettings.SettingPool.SYSTEM_CHECK_PERMISSION, isToggle);
			}
		});
		data.add(permissionCheck);
		SettingToggle useMarkdown = new SettingToggle("解析Markdown格式", "关闭后将不会处理markdown格式的内容",
				setting.getBoolean(ClientSettings.SettingPool.MSG_MARKDOWN_SURPPORT));
		useMarkdown.setIcon(R.drawable.ic_mark_black);
		useMarkdown.setOnToggleChangeListener(new SettingToggle.OnToggleChangeListener() {
			@Override
			public void onChange(boolean isToggle) {
				// TODO: Implement this method
				setting.putValue(ClientSettings.SettingPool.MSG_MARKDOWN_SURPPORT, isToggle);
			}
		});
		data.add(useMarkdown);
		SettingToggle backgroundPlay = new SettingToggle("后台播放", "当应用切换到后台时，不停止视频播放",
				setting.getBoolean(ClientSettings.SettingPool.PLAYER_BACKGROUND_PLAY));
		backgroundPlay.setIcon(R.drawable.ic_video_black);
		backgroundPlay.setOnToggleChangeListener(new SettingToggle.OnToggleChangeListener() {
			@Override
			public void onChange(boolean isToggle) {
				// TODO: Implement this method
				setting.putValue(ClientSettings.SettingPool.PLAYER_BACKGROUND_PLAY, isToggle);
			}
		});
		data.add(backgroundPlay);
		SettingEdittext storageEdit = new SettingEdittext("自定义存储(实验性)", "自定义APP外部数据存储目录(仅Android10及以下可用)",
				"在此输入完整外部路径...", setting.getString(ClientSettings.SettingPool.SYSTEM_STORAGE_EDIT));

		storageEdit.setOnTextChangeListener(new SettingEdittext.OnTextChangeListener() {
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
		data.add(storageEdit);
		SettingAction cleanCache = new SettingAction("清理缓存", "点击清理APP缓存目录", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				cleanCache(true);
			}
		});
		cleanCache.setIcon(R.drawable.ic_clear_black);
		data.add(cleanCache);
		SettingAction settingReset = new SettingAction("重置设置", "手动将所有设置项恢复为默认设置", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				Toast.makeText(getApplication(), "操作成功", Toast.LENGTH_SHORT).show();
				setting.reset();
			}
		});
		settingReset.setIcon(R.drawable.ic_reset_black);
		data.add(settingReset);
		SettingAction importVideo = new SettingAction("导入视频", "手动导入已缓存视频", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				i.setType("application/zip");
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				startActivityForResult(i, FILE_REQUEST_CODE);
			}
		});
		importVideo.setIcon(R.drawable.ic_import_black);
		data.add(importVideo);
		SettingAction importBackground = new SettingAction("导入启动图", "自定义APP启动页面的背景图片", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, IMAGE_REQUEST_CODE);
			}
		});
		importBackground.setIcon(R.drawable.ic_background_black);
		data.add(importBackground);
		SettingAction checkPermission = new SettingAction("申请权限", "手动申请应用所需权限", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				checkPermission(SettingsActivity.this);
			}
		});
		checkPermission.setIcon(R.drawable.ic_permission_black);
		data.add(checkPermission);
		SettingAction group = new SettingAction("加入群聊", "点击加入APP开发交流", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				SystemUtils.loadUri(getApplication(),
						"http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=q8rrsHOdfjuh53isV_itsduydqiWgyUK&authKey=Dlb5xiPw0nwjBP%2BYg0rRv%2BBVRjLrS3Ogzzo2PuItgKJyOeVU6PP5qBhnY%2B72HomV&noverify=0&group_code=1039356559");
			}
		});
		group.setIcon(R.drawable.ic_qgroup_black);
		data.add(group);
		SettingAction feedback = new SettingAction("反馈", "向软件作者反馈bug", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				feedbackDia();
			}
		});
		feedback.setIcon(R.drawable.ic_report_black);
		data.add(feedback);
		SettingAction themeSwitch = new SettingAction("主题切换", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				themeDia();
			}
		});
		themeSwitch.setIcon(R.drawable.ic_platte_black);
		data.add(themeSwitch);
		SettingAction share = new SettingAction("分享软件", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_TEXT,
						"OTTOHub邀请你来体验APP端啦~\n点击下方链接下载↓\nhttps://www.123pan.com/s/fqQojv-oyZJH.html");
				startActivity(Intent.createChooser(i, "share"));
			}
		});
		share.setIcon(R.drawable.ic_share_black);
		data.add(share);
		SettingAction logout = new SettingAction("退出登录", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				Toast.makeText(getApplication(), "退出登录成功", Toast.LENGTH_SHORT).show();
				AccountManager.getInstance(getApplication()).logout();
			}
		});
		logout.setIcon(R.drawable.ic_logout_black);
		data.add(logout);
		SettingAction appinfo = new SettingAction("关于我们", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				startActivity(new Intent(SettingsActivity.this, InfoActivity.class));
			}
		});
		appinfo.setIcon(R.drawable.ic_info_black);
		data.add(appinfo);
		return data;
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

	private void storageDia(final String newPath) {
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

	private void send(String text) {
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			Client.saveActivity(getIntent());
			startActivity(new Intent(this, LoginActivity.class));
			return;
		}
		Account current = manager.getAccount();
		if (current == null) {
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(APIManager.MessageURI.getSendMessageURI(current.getToken(), 5788, text),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						if (content == null || content.isEmpty()) {
							onFailed("content is empty");
							return;
						}
						try {
							final JSONObject detail = new JSONObject(content);
							String status = detail.optString("status", "error");
							if (status.equals("success")) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(getApplication(), "反馈成功，请注意收件箱是否收到回信", Toast.LENGTH_SHORT)
												.show();
									}
								});
								return;
							}
							onFailed(detail.optString("message", "error"));
						} catch (JSONException e) {
							onFailed(e.toString());
						}
					}

					@Override
					public void onFailed(String cause) {
						Log.e("Network", cause);
					}
				});
	}

	private void feedbackDia() {
		final EditText text = new EditText(this);
		text.setHint("在此处输入反馈内容...");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("发送反馈");
		builder.setCancelable(false);
		builder.setView(text);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				send(text.getText().toString());
				dia.dismiss();
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

	public static void checkPermission(final Activity a) {
		PermissionHelper.requestPermissions(a,
				new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"},
				new PermissionHelper.PermissionCallback() {
					@Override
					public void onAllGranted() {
						Toast.makeText(a, "权限授予成功", Toast.LENGTH_SHORT).show();
						Client.initSettings(a);
					}

					@Override
					public void onDeniedWithNeverAsk() {
						Toast.makeText(a, "权限已拒绝(后续可在设置重新授予)", Toast.LENGTH_SHORT).show();
					}
				});
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
}

