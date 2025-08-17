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

/**
 * @Author Hiro
 * @Date 2025/06/06 15:01
 */
public class SettingsActivity extends BasicActivity {
	public static final String TAG = "SettingsActivity";
	private LogView main;
	private EditText text;
	private PopupWindow window;
	private BottomDialog resetDialog;
	private TextView registerCallback;
	private EditText resetInputEmail;
	private EditText resetInputVerify;
	private EditText resetInputPassword;
	private EditText resetInputpw;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "setting activity create");
		setContentView(R.layout.activity_settings);
		View content = findViewById(android.R.id.content);
		Switch sw = content.findViewWithTag("show_log_switch");
		Switch auto = content.findViewWithTag("auto_login_switch");
		TextView tv = content.findViewWithTag("device_info");
		text = content.findViewWithTag("text_input");
		final AccountManager manager = AccountManager.getInstance(this);
		String pack = ApplicationUtils.getPackage(this);
		Object[] infos = {"android版本: android", DeviceUtils.getAndroidVersion(), "\nSDK版本: ",
				DeviceUtils.getAndroidSDK(), "\n设备型号: ", DeviceUtils.getDeviceModel(), "\n屏幕尺寸: ",
				DeviceUtils.getWindowHeight(this), "x", DeviceUtils.getWindowWidth(this), "\n内存大小: ",
				DeviceUtils.getAvailableMemory(this), "\n应用包名: ", pack, "\n应用名: ", ApplicationUtils.getName(this, pack),
				"\n版本号: ", ApplicationUtils.getVersionCode(this, pack), "\n当前版本: ",
				ApplicationUtils.getVersionName(this, pack), "\n目标SDK: ", ApplicationUtils.getTargetSDK(this, pack),
				"\n最小支持SDK: ", ApplicationUtils.getMinSDK(this, pack), "\n最大JVM内存: ", SystemUtils.getJVMmaxMemory(),
				"\n当前时间戳: ", SystemUtils.getTime(), "\n当前时间: ", SystemUtils.getDate("yyyy_MM_dd_HH_mm_ss")};
		tv.setText(StringUtils.strCat(infos));
		sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					window.showAtLocation(getWindow().getDecorView(), 51, 0, 0);
					return;
				}
				window.dismiss();
			}
		});
		auto.setChecked(manager.isAuto());
		auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Toast.makeText(getApplication(), "自动登录已更新为" + isChecked, Toast.LENGTH_SHORT).show();
				manager.setAuto(isChecked);
			}
		});
		Account current = manager.getAccount();
		if (!manager.isLogin() && current == null) {
			((TextView) findViewById(R.id.account_info)).setText("当前未登录");
			return;
		}
		loadUI(current);
		initWindow();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent last = Client.getLastActivity();
		if (last != null && Client.isFinishingLast(last)) {
			Client.removeActivity();
			startActivity(last);
		}
		if (main != null) {
			main.stopLogging();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO: Implement this method
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
			Uri uri = data.getData();
			Account current = AccountManager.getInstance(this).getAccount();
			new ImageUploader(this, "https://api.ottohub.cn/module/creator/update_cover.php",
					APIManager.CreatorURI.getUpdateCoverURI(current.getToken()), new NetworkUtils.HTTPCallback() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								if (json.optString("status", "error").equals("success")) {
									Toast.makeText(getApplication(), "发送成功，已送往审核", Toast.LENGTH_SHORT).show();
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
							String msg = null;
							switch (cause) {
								case "error_file" :
									msg = "文件格式错误，请正确选择图片";
									break;
								case "file_not_found" :
									msg = "没有选择图片文件";
									break;
								case "too_big_file" :
									msg = "文件太大啦~请选择1MB以内大小的图片";
									break;
							}
							if (msg != null) {
								Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
							}
						}
					}).execute(uri);
		}
	}

	private void loadUI(Account current) {
		NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getUserProfileURI(current.getToken()),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(final String content) {
						// TODO: Implement this method
						try {
							JSONObject json = new JSONObject(content);
							if (json.optString("status", "error").equals("success")) {
								final JSONObject profile = json.optJSONObject("profile");
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// TODO: Implement this method
										Toast.makeText(getApplication(), content, Toast.LENGTH_SHORT).show();
										Object[] text = {"UID: ", profile.opt("uid"), "\n邮箱: ", profile.opt("email"),
												"\n注册日: ", profile.opt("time"), "\n称号: ", profile.opt("honour")};
										((TextView) findViewById(R.id.account_info)).setText(StringUtils.strCat(text));
										EditText phone = findViewById(R.id.account_phone);
										phone.setVisibility(View.VISIBLE);
										phone.setText(profile.optString("phone", ""));
										phone.setOnEditorActionListener(new OnEditorActionListener() {
											@Override
											public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
												// TODO: Implement this method
												if (actionId == EditorInfo.IME_ACTION_DONE) {
													onPhoneChange(v.getText().toString());
												}
												return false;
											}
										});
										EditText qq = findViewById(R.id.account_qq);
										qq.setVisibility(View.VISIBLE);
										qq.setText(profile.optString("qq"));
										qq.setOnEditorActionListener(new OnEditorActionListener() {
											@Override
											public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
												// TODO: Implement this method
												if (actionId == EditorInfo.IME_ACTION_DONE) {
													onQQChange(v.getText().toString());
												}
												return false;
											}
										});
										EditText name = findViewById(R.id.account_name);
										name.setVisibility(View.VISIBLE);
										name.setText(profile.optString("username"));
										name.setOnEditorActionListener(new OnEditorActionListener() {
											@Override
											public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
												// TODO: Implement this method
												if (actionId == EditorInfo.IME_ACTION_DONE) {
													onNameChange(v.getText().toString());
												}
												return false;
											}
										});
										EditText sex = findViewById(R.id.account_sex);
										sex.setVisibility(View.VISIBLE);
										sex.setText(profile.optString("sex"));
										sex.setOnEditorActionListener(new OnEditorActionListener() {
											@Override
											public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
												// TODO: Implement this method
												if (actionId == EditorInfo.IME_ACTION_DONE) {
													onSexChange(v.getText().toString());
												}
												return false;
											}
										});
										EditText intro = findViewById(R.id.account_intro);
										intro.setVisibility(View.VISIBLE);
										intro.setText(profile.optString("intro"));
										intro.setOnEditorActionListener(new OnEditorActionListener() {
											@Override
											public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
												// TODO: Implement this method
												if (actionId == EditorInfo.IME_ACTION_DONE) {
													onIntroChange(v.getText().toString());
												}
												return false;
											}
										});
										Button editPw = findViewById(R.id.settings_edit_pw);
										editPw.setVisibility(View.VISIBLE);
										editPw.setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View v) {
												// TODO: Implement this method
												editPwDia();
											}
										});
										Button editCover = findViewById(R.id.settings_edit_cover);
										editCover.setVisibility(View.VISIBLE);
										editCover.setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View v) {
												// TODO: Implement this method
												Intent i = new Intent(Intent.ACTION_PICK,
														MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
												startActivityForResult(i, IMAGE_REQUEST_CODE);
												Toast.makeText(getApplication(), "选取封面文件，文件大小不超过1MB",
														Toast.LENGTH_SHORT).show();
											}
										});
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

	private void editPwDia() {
		View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_reset, null);
		resetDialog = new BottomDialog(this, inflate);
		resetInputEmail = inflate.findViewWithTag("email_input_edittext");
		resetInputVerify = inflate.findViewWithTag("verify_input_edittext");
		resetInputPassword = inflate.findViewWithTag("password_input_edittext");
		resetInputpw = inflate.findViewWithTag("register_input_edittext");
		registerCallback = inflate.findViewWithTag("register_error_text");
		resetInputpw.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				registerCallback.setVisibility(View.GONE);
			}
		});
		inflate.findViewWithTag("pw_input_switch").setOnClickListener(new OnClickListener() {
			private boolean isShow;

			@Override
			public void onClick(View v) {
				if (isShow) {
					((ImageButton) v).setImageResource(R.drawable.ic_hide_black);
					resetInputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					resetInputPassword.setSelection(resetInputPassword.getText().length());
					isShow = false;
					return;
				}
				((ImageButton) v).setImageResource(R.drawable.ic_show_black);
				resetInputPassword.setInputType(InputType.TYPE_CLASS_TEXT);
				resetInputPassword.setSelection(resetInputPassword.getText().length());
				isShow = true;
			}
		});
		inflate.findViewWithTag("pw_input_switch2").setOnClickListener(new OnClickListener() {
			private boolean isShow;

			@Override
			public void onClick(View v) {
				if (isShow) {
					((ImageButton) v).setImageResource(R.drawable.ic_hide_black);
					resetInputpw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					resetInputpw.setSelection(resetInputpw.getText().length());
					isShow = false;
					return;
				}
				((ImageButton) v).setImageResource(R.drawable.ic_show_black);
				resetInputpw.setInputType(InputType.TYPE_CLASS_TEXT);
				resetInputpw.setSelection(resetInputpw.getText().length());
				isShow = true;
			}
		});
		resetDialog.show();
	}

	private void onPhoneChange(final String phone) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
		Account current = AccountManager.getInstance(this).getAccount();
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
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// TODO: Implement this method
										Toast.makeText(getApplication(), "资料更新成功", Toast.LENGTH_SHORT).show();
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

	private void onQQChange(final String qq) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("确定更新QQ吗？");
		builder.setMessage(StringUtils.strCat("将会更新为", qq));
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

	private void qqRequest(String qq) {
		Account current = AccountManager.getInstance(this).getAccount();
		if (current == null) {
			return;
		}
		try {
			long num = Long.parseLong(qq);
			NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getQQEditURI(current.getToken(), num),
					new NetworkUtils.HTTPCallback() {
						@Override
						public void onSuccess(String content) {
							// TODO: Implement this method
							try {
								JSONObject json = new JSONObject(content);
								if (json.optString("status", "error").equals("success")) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											// TODO: Implement this method
											Toast.makeText(getApplication(), "资料更新成功", Toast.LENGTH_SHORT).show();
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
		} catch (NumberFormatException e) {
			Toast.makeText(getApplication(), "请输入正确的数字", Toast.LENGTH_SHORT).show();
		}
	}

	private void onNameChange(final String name) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("确定更新昵称吗？");
		builder.setMessage(StringUtils.strCat("将会更新为", name));
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				nameRequest(name);
                dia.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void nameRequest(String name) {
		Account current = AccountManager.getInstance(this).getAccount();
		if (current == null) {
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getNameEditURI(current.getToken(), name),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						// TODO: Implement this method
						try {
							JSONObject json = new JSONObject(content);
							if (json.optString("status", "error").equals("success")) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// TODO: Implement this method
										Toast.makeText(getApplication(), "资料更新成功", Toast.LENGTH_SHORT).show();
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

	private void onSexChange(final String sex) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
		Account current = AccountManager.getInstance(this).getAccount();
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
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// TODO: Implement this method
										Toast.makeText(getApplication(), "资料更新成功", Toast.LENGTH_SHORT).show();
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

	private void onIntroChange(final String intro) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("确定更新电话吗？");
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
		Account current = AccountManager.getInstance(this).getAccount();
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
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// TODO: Implement this method
										Toast.makeText(getApplication(), "资料更新成功", Toast.LENGTH_SHORT).show();
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

	private void initWindow() {
		main = new LogView(this);
		main.startLogging();
		window = new PopupWindow(-1, -1);
		window.setContentView(main);
		window.setTouchable(false);
		window.setFocusable(false);
	}

	private void send(long uid) {
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
		NetworkUtils.getNetwork.getNetworkJson(
				APIManager.MessageURI.getSendMessageURI(current.getToken(), uid, text.getText().toString()),
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

	public void reset(View v) {
		final String email = resetInputEmail.getText().toString();
		String verify = resetInputVerify.getText().toString();
		final String password = resetInputPassword.getText().toString();
		String pw = resetInputpw.getText().toString();
		if (!password.equals(pw)) {
			registerCallback.setVisibility(View.VISIBLE);
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(APIManager.AccountURI.getPasswordResetURI(email, verify, password, pw),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(final String content) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (content == null || content.isEmpty()) {
									return;
								}
								try {
									JSONObject root = new JSONObject(content);
									String status = root.optString("status", "error");
									if (status.equals("error")) {
										String message = root.optString("message", "棍母");
										if (message.equals("email_unexist")) {
											Toast.makeText(getApplication(), "你还没账号呢，快去注册一个", Toast.LENGTH_SHORT)
													.show();
										}
										if (message.equals("error_verification_code")) {
											Toast.makeText(getApplication(), "baka! 说了去邮箱看验证码阿喂", Toast.LENGTH_SHORT)
													.show();
										}
										return;
									}
									Toast.makeText(getApplication(), "走位成功~OTTOHub─=≡Σ((( つ•̀ω•́)つ衝刺",
											Toast.LENGTH_SHORT).show();
									resetDialog.dismiss();
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

	public void verify(View v) {
		String email = resetInputEmail.getText().toString();
		if (email.isEmpty()) {
			((Button) v).setText("拉了胯");
			Toast.makeText(getApplication(), "铸币吧你邮箱是棍母啊", Toast.LENGTH_SHORT).show();
			return;
		}
		((Button) v).setText("走位中...");
		String uri = APIManager.AccountURI.getPasswordResetVerifyURI(email);
		NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(final String content) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (content == null || content.isEmpty()) {
							return;
						}
						try {
							JSONObject root = new JSONObject(content);
							String status = root.optString("status", "error");
							if (status.equals("error")) {
								String message = root.optString("message", "棍母");
								if (message.equals("email_exist")) {
									Toast.makeText(getApplication(), "给我两个一样的邮箱干什么玩意啊", Toast.LENGTH_SHORT).show();
								}
								if (message.equals("email_unexist")) {
									Toast.makeText(getApplication(), "你还没账号呢快去注册一个", Toast.LENGTH_SHORT).show();
								}
								if (message.equals("error_email")) {
									Toast.makeText(getApplication(), "baka! 是邮箱吗你就发", Toast.LENGTH_SHORT).show();
								}
								return;
							}
							Toast.makeText(getApplication(), "已发送~记得检查邮箱或垃圾邮件哦qwq", Toast.LENGTH_SHORT).show();
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

	public void saveLog(View v) {
		Object[] name = {
				FileUtils.getStorage(this, null),
				"OTTOHub_runlog_", SystemUtils.getDate("yyyy_MM_dd_HH_mm_ss_"), SystemUtils.getTime(), ".log"};
		if (main != null) {
			main.saveLog(StringUtils.strCat(name));
			Log.i(TAG, "log save success");
		}
	}

	public void sendWeb(View v) {
		send(1);
	}

	public void sendApp(View v) {
		send(5788);
	}

	public void group(View v) {
		Toast.makeText(getApplication(), "捣乱的别来，只欢迎对OTTOHub开发感兴趣的", Toast.LENGTH_SHORT).show();
		SystemUtils.loadUri(this,
				"http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=q8rrsHOdfjuh53isV_itsduydqiWgyUK&authKey=Dlb5xiPw0nwjBP%2BYg0rRv%2BBVRjLrS3Ogzzo2PuItgKJyOeVU6PP5qBhnY%2B72HomV&noverify=0&group_code=1039356559");
	}

	public void quit(View v) {
		finish();
	}
}

