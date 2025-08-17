package com.losthiro.ottohubclient.impl;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.losthiro.ottohubclient.adapter.model.Account;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import com.losthiro.ottohubclient.utils.SystemUtils;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Author Hiro
 * @Date 2025/05/23 21:53
 */
public class AccountManager {
	public static final String TAG = "AccountManager";
	private static final HashMap<Account, String> data = new HashMap<>();
	private static AccountManager INSTANCE;
	private Context main;
	private SharedPreferences prefs;
	private boolean isAuto = true;
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
		for (HashMap.Entry<Account, String> account : data.entrySet()) {
			Account a = account.getKey();
			if (a.isCurrent()) {
				return a;
			}
		}
		return null;
	}

	public Account getAccount(int pos) {
		int i = 0;
		for (HashMap.Entry<Account, String> account : data.entrySet()) {
			Account a = account.getKey();
			if (i == pos) {
				return a;
			} else {
				i++;
			}
		}
		return null;
	}

	public boolean isLogin() {
		if (data.isEmpty()) {
			return false;
		}
		for (HashMap.Entry<Account, String> account : data.entrySet()) {
			Account current = account.getKey();
			if (current.isCurrent()) {
				return true;
			}
		}
		return false;
	}

	public boolean isAuto() {
		return isAuto;
	}

	public void login(Account a, String password) {
		if (data.size() >= 5) {
			Toast.makeText(main, "关联账户最多只能有五个哦", Toast.LENGTH_SHORT).show();
			return;
		}
		data.put(a, password);
	}
    
    public void logout(){
        for (HashMap.Entry<Account, String> account : data.entrySet()) {
            Account current = account.getKey();
            if (current.isCurrent()) {
                current.setCurrent(false);
                return;
            }
		}
    }

	public void setAuto(boolean v) {
		isAuto = v;
	}

	public void removeAccount(int index) {
		data.remove(index);
	}

	public int accountCount() {
		return data.size();
	}

	public void resetLogin() {
		Toast.makeText(main, "登录状态失效，请重新登录", Toast.LENGTH_SHORT).show();
		prefs.edit().putBoolean("login_saved", false).apply();
	}

	public void autoLogin() {
		boolean isSaved = prefs.getBoolean("login_saved", false);
		if (isSaved && isAuto) {
			String content = prefs.getString("accounts", null);
			if (content == null) {
				resetLogin();
				return;
			}
			try {
				JSONArray set = new JSONArray(content);
				for (int i = 0; i < set.length(); i++) {
					JSONObject j = set.optJSONObject(i);
					long uid = j.optLong("uid", -1);
					final String pw = j.optString("user_password");
					final boolean isCurrent = j.optBoolean("is_current", false);
					if (pw == null) {
						resetLogin();
						break;
					}
					NetworkUtils.getNetwork.getNetworkJson(APIManager.AccountURI.getLoginURI(uid, pw),
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
											loadUserDetail(uid, token, pw, isCurrent);
										}
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
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(main, e.toString(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void saveAccounts() {
		JSONArray array = new JSONArray();
		for (HashMap.Entry<Account, String> entry : data.entrySet()) {
			Account a = entry.getKey();
			String pw = entry.getValue();
			JSONObject account = new JSONObject();
			try {
				account.put("uid", a.getUID());
				account.put("user_password", pw);
				account.put("is_current", a.isCurrent());
				array.put(account);
			} catch (JSONException e) {
				Toast.makeText(main, e.toString(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				break;
			}
		}
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString("accounts", array.toString());
		//edit.putString("user_password", password);
		//edit.putLong("key_last_update", SystemUtils.getTime());
		edit.putBoolean("login_saved", true);
		edit.commit();
	}

	private void loadUserDetail(long uid, final String token, final String pw, final boolean isCurrent) {
		NetworkUtils.getNetwork.getNetworkJson(APIManager.UserURI.getUserDetail(uid), new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(final String content) {
				if (content == null || content.isEmpty()) {
					onFailed("empty content");
					return;
				}
				try {
					Account a = new Account(main, new JSONObject(content), token);
					a.setCurrent(isCurrent);
					login(a, pw);
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

