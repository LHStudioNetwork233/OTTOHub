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
		SettingAction cleanCache = new SettingAction("清理缓存", "点击清理APP缓存目录", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				FileUtils.clearDir(FileUtils.getStorage(getApplication(), null), new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						// TODO: Implement this method
						return pathname.isFile() && pathname.getName().endsWith(".log");
					}
				});
				String msg = SystemUtils.clearCache(getApplication()) ? "缓存清理成功" : "缓存清理失败";
				Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
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

	private void initWindow() {
		mainLog = new LogView(this);
		mainLog.startLogging();
		window = new PopupWindow(-1, -1);
		window.setContentView(main);
		window.setTouchable(false);
		window.setFocusable(false);
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
    
    private void switchDia(String text, final int index){
        final Runnable callback = new Runnable(){
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
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
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

