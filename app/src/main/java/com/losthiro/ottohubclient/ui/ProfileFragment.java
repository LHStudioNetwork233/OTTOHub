/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.ui;
import androidx.fragment.app.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.app.AlertDialog;
import android.view.*;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.utils.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.adapter.model.*;
import android.util.*;
import org.json.*;
import androidx.recyclerview.widget.*;
import android.content.*;
import com.losthiro.ottohubclient.adapter.setting.*;
import java.util.*;
import android.widget.*;
import com.losthiro.ottohubclient.*;

public class ProfileFragment extends Fragment {
	public static final String TAG = "Profile";
	private static final Handler uiThread = new Handler(Looper.getMainLooper());
    private Runnable action;

	public static ProfileFragment newInstance(long uid) {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		arg.putLong("uid", uid);
		ProfileFragment profilePage = new ProfileFragment();
		profilePage.setArguments(arg);
		return profilePage;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: Implement this method
        action = new Runnable() {
            @Override
            public void run() {
                // TODO: Implement this method
                Toast.makeText(getContext(), "资料更新成功", Toast.LENGTH_SHORT).show();
            }
        };
		return inflater.inflate(R.layout.fragment_profile, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onViewCreated(view, savedInstanceState);
		Account current = AccountManager.getInstance(getContext()).getAccount();
		NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getUserProfileURI(current.getToken()),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						// TODO: Implement this method
						if (content == null) {
							onFailed("content==null");
							return;
						}
						try {
							final JSONObject json = new JSONObject(content);
							if (json.optString("status", "error").equals("success")) {
								uiThread.post(new Runnable() {
									@Override
									public void run() {
										// TODO: Implement this method
										initUI(json.optJSONObject("profile"));
									}
								});
								return;
							}
							onFailed(json.optString("message"));
						} catch (Exception e) {
							onFailed(e.toString());
						}
					}

					@Override
					public void onFailed(String cause) {
						// TODO: Implement this method
						Log.e("Network", cause);
					}
				});
	}

	private void initUI(JSONObject root) {
		final Context ctx = getContext();
		final String email = root.optString("email");
		final RecyclerView main = getView().findViewById(R.id.profile_list);
		main.setLayoutManager(new GridLayoutManager(ctx, 1));
		List<SettingBasic> profile = new ArrayList<>();
		profile.add(new SettingTitle("详细资料"));
		profile.add(new SettingAction("UID", root.opt("uid").toString(), null));
		profile.add(new SettingAction("邮箱", email, null));
		profile.add(new SettingAction("注册时间", root.optString("time"), null));
		profile.add(new SettingAction("称号", root.optString("honour"), null));
		profile.add(new SettingTitle(""));
		profile.add(new SettingTitle("个人资料"));
		List<SettingBasic> updatePassword = new ArrayList<>();
		SettingEdittext edit = new SettingEdittext("输入新密码", "新密码不能与原来的密码一致，也不能太过简单", "在此输入新密码...");
		edit.setTag("pw_edit");
		updatePassword.add(edit);
		SettingEdittext verify = new SettingEdittext("输入验证码", "点击旁边确定获取验证码", "在此输入验证码...");
		verify.setBtnText("获取验证码");
		verify.setTag("verify");
		verify.setOnTextChangeListener(new SettingEdittext.OnTextChangeListener() {
			@Override
			public void onChange(String newText) {
				// TODO: Implement this method
				verify(email);
			}
		});
		updatePassword.add(verify);
		updatePassword.add(new SettingAction("确定更新密码", "点我确认更新", new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				SettingsAdapter adapter = (SettingsAdapter) main.getAdapter();
				if (adapter == null) {
					return;
				}
				SettingBasic newPassword = adapter.getItem("pw_edit");
				SettingBasic verify = adapter.getItem("verify");
				if (newPassword == null || verify == null) {
					return;
				}
				if (newPassword instanceof SettingEdittext && verify instanceof SettingEdittext) {
					updatePassword(((SettingEdittext) newPassword).getContent(),
							((SettingEdittext) verify).getContent(), email);
				}
			}
		}));
		profile.add(new SettingToggle("更新密码", "点击打开密码更新", updatePassword));
		SettingEdittext editPhone = new SettingEdittext("手机号", "当前账号绑定的手机号", "在此处输入新号码...", root.optString("phone"));
		editPhone.setOnTextChangeListener(new SettingEdittext.OnTextChangeListener() {
			@Override
			public void onChange(String newText) {
				// TODO: Implement this method
				try {
					if (newText.isEmpty() || newText.length() < 11) {
						throw new NumberFormatException();
					}
					long phone = Long.parseLong(newText);
					onPhoneChange(StringUtils.toStr(phone));
				} catch (NumberFormatException e) {
					Toast.makeText(ctx, "手机号打的有问题", Toast.LENGTH_SHORT).show();
				}
			}
		});
		profile.add(editPhone);
		SettingEdittext editQQ = new SettingEdittext("QQ", "当前账号绑定的QQ", "在此处输入新QQ号...", root.optString("qq"));
		editQQ.setOnTextChangeListener(new SettingEdittext.OnTextChangeListener() {
			@Override
			public void onChange(String newText) {
				// TODO: Implement this method
				try {
					if (newText.isEmpty()) {
						throw new NumberFormatException();
					}
					long qq = Long.parseLong(newText);
					onQQChange(qq);
				} catch (NumberFormatException e) {
					Toast.makeText(ctx, "QQ号打的有问题", Toast.LENGTH_SHORT).show();
				}
			}
		});
		profile.add(editQQ);
		SettingEdittext editName = new SettingEdittext("名称", "当前账号的用户名", "在此处输入新名字...", root.optString("username"));
		editName.setOnTextChangeListener(new SettingEdittext.OnTextChangeListener() {
			@Override
			public void onChange(String newText) {
				// TODO: Implement this method
				onNameChange(newText);
			}
		});
		profile.add(editName);
		SettingEdittext editSex = new SettingEdittext("性别", "(小简介罢了)", "在此处输入新内容...", root.optString("sex"));
		editSex.setOnTextChangeListener(new SettingEdittext.OnTextChangeListener() {
			@Override
			public void onChange(String newText) {
				// TODO: Implement this method
				onSexChange(newText);
			}
		});
		profile.add(editSex);
		SettingEdittext editIntro = new SettingEdittext("个人简介", "当前账号的个人简介", "在此处输入新内容...", root.optString("intro"));
		editIntro.setOnTextChangeListener(new SettingEdittext.OnTextChangeListener() {
			@Override
			public void onChange(String newText) {
				// TODO: Implement this method
				onIntroChange(newText);
			}
		});
		profile.add(editIntro);
		main.setAdapter(new SettingsAdapter(ctx, profile));
	}

	private void verify(String email) {
		String uri = APIManager.AccountURI.getPasswordResetVerifyURI(email);
		NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(final String content) {
				uiThread.post(new Runnable() {
					@Override
					public void run() {
						if (content == null || content.isEmpty()) {
							return;
						}
						try {
							Context ctx = getContext();
							JSONObject root = new JSONObject(content);
							String status = root.optString("status", "error");
							if (status.equals("error")) {
								String message = root.optString("message", "棍母");
								if (message.equals("email_exist")) {
									Toast.makeText(ctx, "给我两个一样的邮箱干什么玩意啊", Toast.LENGTH_SHORT).show();
								}
								if (message.equals("email_unexist")) {
									Toast.makeText(ctx, "你还没账号呢快去注册一个", Toast.LENGTH_SHORT).show();
								}
								if (message.equals("error_email")) {
									Toast.makeText(ctx, "baka! 是邮箱吗你就发", Toast.LENGTH_SHORT).show();
								}
								if (message.equals("error_qq_email")) {
									Toast.makeText(ctx, "QQ邮箱只支持QQ号+@qq.com的格式捏", Toast.LENGTH_SHORT).show();
								}
								return;
							}
							Toast.makeText(ctx, "已发送~记得检查邮箱或垃圾邮件哦qwq", Toast.LENGTH_SHORT).show();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
			}

			@Override
			public void onFailed(String cause) {
				Log.e(TAG, cause);
			}
		});
	}

	private void updatePassword(String newPassword, String verify, String email) {
		Context ctx = getContext();
		AccountManager manager = AccountManager.getInstance(ctx);
		if (newPassword.isEmpty() && newPassword.length() < 6) {
			Toast.makeText(ctx, "密码不安全，请增加密码长度", Toast.LENGTH_SHORT).show();
			return;
		}
		if (manager.contains(newPassword)) {
			Toast.makeText(ctx, "不能设置为已有关联账号的密码", Toast.LENGTH_SHORT).show();
			return;
		}
		if (verify.isEmpty()) {
			Toast.makeText(ctx, "验证码不能为棍母", Toast.LENGTH_SHORT).show();
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(
				APIManager.AccountURI.getPasswordResetURI(email, verify, newPassword, newPassword),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(final String content) {
						uiThread.post(new Runnable() {
							@Override
							public void run() {
								if (content == null || content.isEmpty()) {
									return;
								}
								try {
									Context ctx = getContext();
									JSONObject root = new JSONObject(content);
									String status = root.optString("status", "error");
									if (status.equals("error")) {
										String message = root.optString("message", "棍母");
										if (message.equals("email_unexist")) {
											Toast.makeText(ctx, "你还没账号呢，快去注册一个", Toast.LENGTH_SHORT).show();
										}
										if (message.equals("error_verification_code")) {
											Toast.makeText(ctx, "baka! 说了去邮箱看验证码阿喂", Toast.LENGTH_SHORT).show();
										}
										return;
									}
									Toast.makeText(ctx, "走位成功~OTTOHub─=≡Σ((( つ•̀ω•́)つ衝刺", Toast.LENGTH_SHORT).show();
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						});
					}

					@Override
					public void onFailed(String cause) {
						Log.e(TAG, cause);
					}
				});
	}
    
    private void onNameChange(final String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("确定更新用户名吗？");
        builder.setMessage(StringUtils.strCat("将会更新为", name));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    FragmentActivity a = requireActivity();
                    if (a instanceof AccountDetailActivity) {
                        ((AccountDetailActivity) a).editName(name);
                    }
                    dia.dismiss();
                }
            });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
	}

	private void onPhoneChange(final String phone) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("确定更新电话吗？");
		builder.setMessage(StringUtils.strCat("将会更新为", phone));
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				phoneRequest(phone);
				dia.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void phoneRequest(String phone) {
		Account current = AccountManager.getInstance(getContext()).getAccount();
		if (current == null) {
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getPhoneEditURI(current.getToken(), phone),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						// TODO: Implement this method
						try {
							JSONObject json = new JSONObject(content);
							if (json.optString("status", "error").equals("success")) {
								uiThread.post(action);
								return;
							}
							onFailed(json.optString("message"));
						} catch (Exception e) {
							onFailed(e.toString());
						}
					}

					@Override
					public void onFailed(String cause) {
						// TODO: Implement this method
						Log.e("Network", cause);
					}
				});
	}

	private void onQQChange(final long qq) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("确定更新QQ吗？");
		builder.setMessage(StringUtils.strCat("将会更新为", StringUtils.toStr(qq)));
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				qqRequest(qq);
				dia.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void qqRequest(long qq) {
		Account current = AccountManager.getInstance(getContext()).getAccount();
		if (current == null) {
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getQQEditURI(current.getToken(), qq),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						// TODO: Implement this method
						try {
							JSONObject json = new JSONObject(content);
							if (json.optString("status", "error").equals("success")) {
								uiThread.post(action);
								return;
							}
							onFailed(json.optString("message"));
						} catch (Exception e) {
							onFailed(e.toString());
						}
					}

					@Override
					public void onFailed(String cause) {
						// TODO: Implement this method
						Log.e("Network", cause);
					}
				});
	}

	private void onSexChange(final String sex) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("确定更新性别吗？");
		builder.setMessage(StringUtils.strCat("将会更新为", sex));
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				sexRequest(sex);
				dia.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void sexRequest(String sex) {
		Account current = AccountManager.getInstance(getContext()).getAccount();
		if (current == null) {
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getSexEditURI(current.getToken(), sex),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						// TODO: Implement this method
						try {
							JSONObject json = new JSONObject(content);
							if (json.optString("status", "error").equals("success")) {
								uiThread.post(action);
								return;
							}
							onFailed(json.optString("message"));
						} catch (Exception e) {
							onFailed(e.toString());
						}
					}

					@Override
					public void onFailed(String cause) {
						// TODO: Implement this method
						Log.e("Network", cause);
					}
				});
	}

	private void onIntroChange(final String intro) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("确定更新简介吗？");
		builder.setMessage(StringUtils.strCat("将会更新为", intro));
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				introRequest(intro);
				dia.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void introRequest(String intro) {
		Account current = AccountManager.getInstance(getContext()).getAccount();
		if (current == null) {
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getIntroEditURI(current.getToken(), intro),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						// TODO: Implement this method
						try {
							JSONObject json = new JSONObject(content);
							if (json.optString("status", "error").equals("success")) {
								uiThread.post(action);
								return;
							}
							onFailed(json.optString("message"));
						} catch (Exception e) {
							onFailed(e.toString());
						}
					}

					@Override
					public void onFailed(String cause) {
						// TODO: Implement this method
						Log.e("Network", cause);
					}
				});
	}
}

