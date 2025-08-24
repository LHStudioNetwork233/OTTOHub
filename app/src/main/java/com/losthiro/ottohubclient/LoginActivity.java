package com.losthiro.ottohubclient;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.losthiro.ottohubclient.adapter.model.Account;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import java.util.HashMap;
import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import org.json.JSONException;
import org.json.JSONObject;
import com.losthiro.ottohubclient.impl.danmaku.DefDanmakuManager;
import android.widget.ImageButton;
import android.graphics.Color;
import android.text.InputType;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.view.dialog.*;

/**
 * @Author Hiro
 * @Date 2025/05/23 09:41
 */
public class LoginActivity extends BasicActivity {
	public static final String TAG = "LoginActivity";
	private Intent loginAction;
	private IDanmakuView bg;
	private BottomDialog loginDialog;
	private BottomDialog registerDialog;
	private BottomDialog resetDialog;
	private View callLDialog;
	private Button quit;
	private Button register;
	private EditText loginInputUID;
	private TextView loginCallback;
	private EditText loginInputPW;
	private EditText registerInputEmail;
	private EditText registerInputVerify;
	private EditText registerInputPassword;
	private EditText registerInputpw;
	private TextView registerCallback;
	private EditText resetInputEmail;
	private EditText resetInputVerify;
	private EditText resetInputPassword;
	private EditText resetInputpw;
	private TextView userName;
	private ImageView userAvatar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AccountManager manager = AccountManager.getInstance(this);
		setContentView(R.layout.activity_login);
		userName = findViewById(R.id.login_user_name);
		userAvatar = findViewById(R.id.login_user_avatar);
		quit = findViewById(R.id.login_quit_view);
		quit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quit();
			}
		});
		ImageButton btn = findViewById(R.id.quit);
		btn.setColorFilter(Color.BLACK);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quit();
			}
		});
		HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
		maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_LR, 5);
		HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
		overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_LR, true);
		overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
		final DanmakuContext controller = DanmakuContext.create();
		controller.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3);
		controller.setDuplicateMergingEnabled(false);
		controller.setScrollSpeedFactor(1.2f);
		controller.setScaleTextSize(1.2f);
		controller.setMaximumLines(maxLinesPair);
		controller.preventOverlapping(overlappingEnablePair);
		controller.setDanmakuMargin(40);
		bg = findViewById(R.id.login_danmaku);
		bg.setCallback(new DrawHandler.Callback() {
			@Override
			public void prepared() {
				bg.start();
			}

			@Override
			public void updateTimer(DanmakuTimer timer) {
			}

			@Override
			public void danmakuShown(BaseDanmaku danmaku) {
			}

			@Override
			public void drawingFinished() {
			}
		});
		bg.prepare(DefDanmakuManager.getInstance(this).createParser(), controller);
		register = findViewById(R.id.login_register_btn);
		register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				registerDialog(v);
			}
		});
		if (manager.isLogin()) {
			userName.setText("账号玩腻了? 秽土转生试试");
			register.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bg != null) {
			bg.release();
			bg = null;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (bg != null) {
			bg.stop();
			bg = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (bg != null && bg.isPrepared() && bg.isPaused()) {
			bg.resume();
			bg = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (bg != null && bg.isPrepared()) {
			bg.pause();
			bg = null;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		quit();
	}

	private void quit() {
		if (loginAction != null) {
			setResult(RESULT_OK, loginAction);
		} else {
			setResult(RESULT_CANCELED);
		}
		finish();
	}

	public void loginDialog(View v) {
		if (registerDialog != null) {
			registerDialog.dismiss();
		}
		if (resetDialog != null) {
			resetDialog.dismiss();
		}
		View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_login, null);
		if (loginDialog == null) {
			callLDialog = v;
			loginDialog = new BottomDialog(this, inflate);
			loginInputUID = inflate.findViewWithTag("uid_input_edittext");
			loginInputPW = inflate.findViewWithTag("pw_input_edittext");
			loginCallback = inflate.findViewWithTag("pw_input_callback");
			loginInputPW.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					loginCallback.setVisibility(View.GONE);
				}
			});
			inflate.findViewWithTag("pw_input_switch").setOnClickListener(new OnClickListener() {
				private boolean isShow;

				@Override
				public void onClick(View v) {
					if (isShow) {
						loginInputPW.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
						loginInputPW.setSelection(loginInputPW.getText().length());
						((ImageButton) v).setImageResource(R.drawable.ic_hide_black);
						isShow = false;
						return;
					}
					((ImageButton) v).setImageResource(R.drawable.ic_show_black);
					loginInputPW.setInputType(InputType.TYPE_CLASS_TEXT);
					loginInputPW.setSelection(loginInputPW.getText().length());
					isShow = true;
				}
			});
		}
		loginDialog.show();
	}

	public void login(View v) {
		if (loginDialog != null && loginDialog.isShowing()) {
			loginDialog.dismiss();
		}
		String account = loginInputUID.getText().toString();
		final String pw = loginInputPW.getText().toString();
		login(APIManager.AccountURI.getLoginURI(account, pw), pw);
	}

	private void login(String uri, final String pw) {
		NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(String content) {
				if (content == null || content.isEmpty()) {
					return;
				}
				try {
					final JSONObject root = new JSONObject(content);
					String status = root.optString("status", "error");
					if (status.equals("success")) {
						String uri = root.optString("avatar_url", "null");
						String token = root.optString("token", "null");
						String stringID = root.optString("uid", "-1");
						long uid = stringID == null || stringID.isEmpty()
								? root.optLong("uid", -1)
								: Long.parseLong(stringID);
						updateUI(pw, uid, uri, token);
					} else if (root.optString("message", "棍母").equals("error_password") && loginCallback != null) {
						loginCallback.setVisibility(View.VISIBLE);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailed(String cause) {
				Log.e("Network", cause);
			}
		});
	}

	private void updateUI(final String password, long uid, final String uri, final String token) {
		NetworkUtils.getNetwork.getNetworkJson(APIManager.UserURI.getUserDetail(uid), new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(final String content) {
				if (content == null || content.isEmpty()) {
					return;
				}
				try {
					JSONObject root = new JSONObject(content);
					final Account a = new Account(LoginActivity.this, root, token);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (!uri.equals("null")) {
								ImageDownloader.loader(userAvatar, uri);
							}
							if (callLDialog != null) {
								callLDialog.setVisibility(View.GONE);
							}
							register.setVisibility(View.GONE);
							quit.setVisibility(View.VISIBLE);
							userName.setText("欢迎回来~ " + a.getName());
							loginAction = new Intent();
							loginAction.putExtra("password", password);
							loginAction.putExtra("content", content);
							loginAction.putExtra("token", a.getToken());
						}
					});
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailed(String cause) {
				Log.e("Network", cause);
			}
		});
	}

	public void registerDialog(View v) {
		if (loginDialog != null) {
			loginDialog.dismiss();
		}
		if (resetDialog != null) {
			resetDialog.dismiss();
		}
		View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);
		if (registerDialog == null) {
			registerDialog = new BottomDialog(this, inflate);
			registerInputEmail = inflate.findViewWithTag("email_input_edittext");
			registerInputVerify = inflate.findViewWithTag("verify_input_edittext");
			registerInputPassword = inflate.findViewWithTag("password_input_edittext");
			registerInputpw = inflate.findViewWithTag("register_input_edittext");
			registerCallback = inflate.findViewWithTag("register_error_text");
			registerInputpw.addTextChangedListener(new TextWatcher() {
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
						registerInputPassword
								.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
						registerInputPassword.setSelection(registerInputPassword.getText().length());
						isShow = false;
						return;
					}
					((ImageButton) v).setImageResource(R.drawable.ic_show_black);
					registerInputPassword.setInputType(InputType.TYPE_CLASS_TEXT);
					registerInputPassword.setSelection(registerInputPassword.getText().length());
					isShow = true;
				}
			});
			inflate.findViewWithTag("pw_input_switch2").setOnClickListener(new OnClickListener() {
				private boolean isShow;

				@Override
				public void onClick(View v) {
					if (isShow) {
						((ImageButton) v).setImageResource(R.drawable.ic_hide_black);
						registerInputpw
								.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
						registerInputpw.setSelection(registerInputpw.getText().length());
						isShow = false;
						return;
					}
					((ImageButton) v).setImageResource(R.drawable.ic_show_black);
					registerInputpw.setInputType(InputType.TYPE_CLASS_TEXT);
					registerInputpw.setSelection(registerInputpw.getText().length());
					isShow = true;
				}
			});
		}
		registerDialog.show();
	}

	public void register(View v) {
		final String email = registerInputEmail.getText().toString();
		String verify = registerInputVerify.getText().toString();
		final String password = registerInputPassword.getText().toString();
		String pw = registerInputPassword.getText().toString();
		if (!password.equals(pw)) {
			registerCallback.setVisibility(View.VISIBLE);
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(APIManager.AccountURI.getRegisterURI(email, verify, password, pw),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
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
								if (message.equals("error_verification_code")) {
									Toast.makeText(getApplication(), "baka! 说了去邮箱看验证码阿喂", Toast.LENGTH_SHORT).show();
								}
								return;
							}
							Toast.makeText(getApplication(), "走位成功~OTTOHub─=≡Σ((( つ•̀ω•́)つ衝刺", Toast.LENGTH_SHORT)
									.show();
							registerDialog.dismiss();
							login(APIManager.AccountURI.getLoginURI(email, password), password);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFailed(String cause) {
						Log.e(TAG, cause);
					}
				});
	}

	public void sendVerify(View v) {
		verify(v, false);
	}

	public void sendResetVerify(View v) {
		verify(v, true);
	}

	private void verify(final View v, boolean isReset) {
		String email = isReset ? resetInputEmail.getText().toString() : registerInputEmail.getText().toString();
		if (email.isEmpty()) {
			((Button) v).setText("拉了胯");
			Toast.makeText(getApplication(), "铸币吧你邮箱是棍母啊", Toast.LENGTH_SHORT).show();
			return;
		}
		((Button) v).setText("走位中...");
		String uri = isReset
				? APIManager.AccountURI.getPasswordResetVerifyURI(email)
				: APIManager.AccountURI.getRegisterVerifyURI(email);
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
                                if (message.equals("error_qq_email")) {
                                    Toast.makeText(getApplication(), "QQ邮箱只支持QQ号+@qq.com的格式捏", Toast.LENGTH_SHORT).show();
								}
                                ((Button) v).setText("拉了胯");
								return;
							}
							Toast.makeText(getApplication(), "已发送~记得检查邮箱或垃圾邮件哦qwq", Toast.LENGTH_SHORT).show();
						} catch (JSONException e) {
							onFailed(e.toString());
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

	public void resetDialog(View v) {
		if (loginDialog != null) {
			loginDialog.dismiss();
		}
		if (registerDialog != null) {
			registerDialog.dismiss();
		}
		View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_reset, null);
		if (resetDialog == null) {
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
						resetInputPassword
								.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
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
		}
		resetDialog.show();
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
									login(APIManager.AccountURI.getLoginURI(email, password), password);
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
}

