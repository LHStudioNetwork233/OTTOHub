package com.losthiro.ottohubclient.impl;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.losthiro.ottohubclient.adapter.model.Account;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import com.losthiro.ottohubclient.utils.SystemUtils;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.losthiro.ottohubclient.view.drawer.*;
import android.util.Base64;
import java.util.*;

/**
 * @Author Hiro
 * @Date 2025/05/23 21:53
 */
public class AccountManager {
	public static final String TAG = "AccountManager";
	private static final Handler uiThread = new Handler(Looper.getMainLooper());
	private static final HashMap<Long, String> data = new HashMap<>();
	private static AccountManager INSTANCE;
	private Context main;
	private SharedPreferences prefs;
	private Runnable callback;
	private Account current;
	private int keyUpdateTime = 30;

	private AccountManager(Context ctx) {
		main = ctx;
		prefs = ctx.getSharedPreferences("Account", Context.MODE_PRIVATE);
	}

	public static final synchronized AccountManager getInstance(Context ctx) {
		if (INSTANCE == null) {
			INSTANCE = new AccountManager(ctx);
		}
		return INSTANCE;
	}

	public static interface AccountListener {
		void onCurrentChange(Account newCurrent);
	}

	public Account getAccount() {
		return current;
	}

	public long getAccountID(int pos) {
		int index = 0;
		for (Long key : data.keySet()) {
			if (index == pos) {
				return key;
			}
			index++;
		}
		return -1;
	}

	public boolean isLogin() {
		return current != null;
	}

	public void login(long uid, final String password) {
		NetworkUtils.getNetwork.getNetworkJson(APIManager.AccountURI.getLoginURI(uid, password),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						if (content == null || content.isEmpty()) {
							onFailed("empty content");
							return;
						}
						try {
							final JSONObject root = new JSONObject(content);
							String status = root.optString("status", "error");
							if (status.equals("success")) {
								String token = root.optString("token");
								String stringID = root.optString("uid");
								long uid = stringID == null || stringID.isEmpty()
										? root.optLong("uid", -1)
										: Long.parseLong(stringID);
								loadUserDetail(uid, token, Base64.encodeToString(password.getBytes(), Base64.DEFAULT));
								return;
							}
							onFailed(root.optString("message"));
						} catch (JSONException e) {
							onFailed(e.toString());
						}
					}

					@Override
					public void onFailed(final String cause) {
						Log.e("Network", cause);
					}
				});
	}

	public void login(long uid) {
		if (!isLogin() || uid != current.getUID()) {
			login(uid, new String(Base64.decode(data.get(uid), Base64.DEFAULT)));
		}
	}

	public void login(Account a, String pw) {
		current = a;
		data.put(a.getUID(), Base64.encodeToString(pw.getBytes(), Base64.DEFAULT));
		callback.run();
	}

	public void logout() {
		if (ClientSettings.getInstance().getBoolean(ClientSettings.SettingPool.ACCOUNT_AUTO_REMOVE)) {
			data.remove(current.getUID());
		}
		current = null;
	}

	public boolean contains(String newPassword) {
		return data.containsValue(newPassword);
	}

	public void removeAccount(int index) {
		int pos = 0;
		for (Long key : data.keySet()) {
			if (pos == index) {
				data.remove(key);
				return;
			}
			pos++;
		}
	}

	public int accountCount() {
		return data.size();
	}

	public void resetLogin() {
		Toast.makeText(main, "登录状态失效，请重新登录", Toast.LENGTH_SHORT).show();
		//		prefs.edit().putBoolean("login_saved", false).apply();
	}

	public void setLoginCallback(Runnable listener) {
		callback = listener;
	}

	public void autoLogin() {
		//boolean isSaved = prefs.getBoolean("login_saved", false);
		if (ClientSettings.getInstance().getBoolean(ClientSettings.SettingPool.ACCOUNT_AUTO_LOGIN)) {
			Set<String> set = prefs.getStringSet("accounts", null);
			if (set == null) {
				resetLogin();
				return;
			}
			try {
				for (String account : set) {
					JSONObject j = new JSONObject(account);
					long uid = j.optLong("uid", -1);
					final String pw = j.optString("user_password");
					final boolean isCurrent = j.optBoolean("is_current", false);
					if (pw == null) {
						resetLogin();
						break;
					}
					if (isCurrent) {
						login(uid, new String(Base64.decode(pw, Base64.DEFAULT)));
					} else {
						data.put(uid, pw);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(main, e.toString(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void saveAccounts() {
		Set<String> set = new HashSet<>();
		for (HashMap.Entry<Long, String> entry : data.entrySet()) {
			long uid = entry.getKey();
			String pw = entry.getValue();
			JSONObject account = new JSONObject();
			try {
				boolean isCurrent = false;
				if (isLogin()) {
					isCurrent = current.getUID() == uid;
				}
				account.put("uid", uid);
				account.put("user_password", pw);
				account.put("is_current", isCurrent);
				set.add(account.toString());
			} catch (JSONException e) {
				Toast.makeText(main, e.toString(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				break;
			}
		}
		SharedPreferences.Editor edit = prefs.edit();
		edit.putStringSet("accounts", set);
		//edit.putString("user_password", password);
		//edit.putLong("key_last_update", SystemUtils.getTime());
		//		edit.putBoolean("login_saved", true);
		edit.commit();
	}

	private void loadUserDetail(long uid, final String token, final String pw) {
		NetworkUtils.getNetwork.getNetworkJson(APIManager.UserURI.getUserDetail(uid), new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(final String content) {
				if (content == null || content.isEmpty()) {
					onFailed("empty content");
					return;
				}
				try {
					current = new Account(main, new JSONObject(content), token);
					data.put(current.getUID(), pw);
					uiThread.post(callback);
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

	private boolean isNeedUpdate() {
		long lastUpdate = prefs.getLong("key_last_update", 0);
		long time = SystemUtils.getTime() - lastUpdate;
		return time > keyUpdateTime * 86400000;
	}
}

