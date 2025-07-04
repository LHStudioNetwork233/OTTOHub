package com.losthiro.ottohubclient;
import android.content.*;
import android.view.*;
import android.os.Bundle;
import android.widget.*;
import com.losthiro.ottohubclient.utils.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.view.*;
import android.provider.*;
import android.net.*;
import android.database.*;
import java.io.*;
import org.json.*;
import android.util.*;
import com.losthiro.ottohubclient.adapter.*;
import android.app.*;
import java.net.*;
import java.nio.charset.*;

/**
 * @Author Hiro
 * @Date 2025/06/13 19:15
 */
public class UploadBlogActivity extends MainActivity {
	public static final String TAG = "UploadBlogActivity";
	private EditText titleEdit;
	private EditText contentEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_blog);
		if (!NetworkUtils.isNetworkAvailable(this)) {
			Toast.makeText(getApplication(), "唉服务器怎么死了", Toast.LENGTH_SHORT).show();
			return;
		}
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			Toast.makeText(getApplication(), "那我缺的登录这一块", Toast.LENGTH_SHORT).show();
			return;
		}
		titleEdit = findViewById(R.id.blog_upload_title);
		contentEdit = findViewById(R.id.blog_upload_content);
		final View parent = findViewById(R.id.blog_upload_view);
		Switch mode = findViewById(R.id.blog_upload_switch);
		mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO: Implement this method
				if (isChecked) {
					titleEdit.setVisibility(View.GONE);
					contentEdit.setVisibility(View.GONE);
					parent.setVisibility(View.VISIBLE);
					((TextView) findViewById(R.id.blog_title)).setText(titleEdit.getText());
					((ClientWebView) findViewById(R.id.blog_content_view))
							.loadTextData(contentEdit.getText().toString());
				} else {
					titleEdit.setVisibility(View.VISIBLE);
					contentEdit.setVisibility(View.VISIBLE);
					parent.setVisibility(View.GONE);
				}
			}
		});
		loadCurrent();
	}

	@Override
	protected void onDestroy() {
		// TODO: Implement this method
		Intent i = Client.getLastActivity();
		if (i != null) {
			Client.removeActivity();
			startActivity(i);
		}
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// TODO: Implement this method
		super.onBackPressed();
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO: Implement this method
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
			Uri uri = data.getData();
			new ImageMarker(this, new NetworkUtils.HTTPCallback() {
				@Override
				public void onSuccess(String reslutURI) {
					// TODO: Implement this method
					try {
						JSONObject json = new JSONObject(reslutURI);
						JSONObject data = json.optJSONObject("data");
						if (data == null) {
							onFailed("data==null");
							return;
						}
						String name = data.optString("name", "");
						String uri = data.optString("url");
						String currentContent = contentEdit.getText().toString();
						Object[] array = {currentContent, System.lineSeparator(), "![", name, "](", uri, ")"};
						String newContent = StringUtils.strCat(array);
						contentEdit.setText(newContent);
					} catch (Exception e) {
						onFailed(e.toString());
					}
				}

				@Override
				public void onFailed(String message) {
					// TODO: Implement this method
					Log.e("Network", message);
					Toast.makeText(getApplication(), "上传失败，请检查是否已连接网络，如一切正常请上报至开发者", Toast.LENGTH_SHORT).show();
				}
			}).execute(uri);
		}
	}

	private void loadCurrent() {
		Account a = AccountManager.getInstance(this).getAccount();
		if (a == null) {
			return;
		}
		NetworkUtils.postNetwork.postJSON("https://api.ottohub.cn/module/creator/load_blog.php",
				APIManager.CreatorURI.getLoadBlog(a.getToken()), new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						// TODO: Implement this method
						try {
							final JSONObject json = new JSONObject(content);
							if (json.optString("status", "error").equals("success")) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// TODO: Implement this method
										contentEdit.setText(json.optString("content", ""));
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
						Log.e("Metwork", cause);
					}
				});
	}

	private void save() {
		Account a = AccountManager.getInstance(this).getAccount();
		if (a == null) {
			return;
		}
		NetworkUtils.postNetwork.postJSON("https://api.ottohub.cn/module/creator/save_blog.php",
				APIManager.CreatorURI.getSaveBlogURI(a.getToken(), contentEdit.getText().toString()),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						// TODO: Implement this method
						try {
							final JSONObject json = new JSONObject(content);
							if (json.optString("status", "error").equals("success")) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// TODO: Implement this method
										Toast.makeText(getApplication(), "保存成功，下次打开自动加载", Toast.LENGTH_SHORT).show();
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
					public void onFailed(final String cause) {
						// TODO: Implement this method
						Log.e("Network", cause);
					}
				});
	}

	private void send() {
		Account a = AccountManager.getInstance(this).getAccount();
		if (a == null) {
			return;
		}
		NetworkUtils.postNetwork.postJSON("https://api.ottohub.cn/module/creator/submit_blog.php", APIManager.CreatorURI
				.getSubmitBlogURI(a.getToken(), titleEdit.getText().toString(), contentEdit.getText().toString()),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						// TODO: Implement this method
						try {
							final JSONObject json = new JSONObject(content);
							if (json.optString("status", "error").equals("success")) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// TODO: Implement this method
										String status = json.optString("if_add_experience", null);
										int type = status == null
												? json.optInt("if_add_experience")
												: Integer.parseInt(status);
										Toast.makeText(getApplication(), type == 1 ? "经验+20~冲刺冲刺" : "动态发布成功~冲刺冲刺",
												Toast.LENGTH_SHORT).show();
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

	public void quit(View v) {
		finish();
	}

	public void sendBlog(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("确定发布动态吗？");
		builder.setMessage("建议先查看渲染效果是否正确再发布");
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				send();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	public void saveCurrent(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("确定保存草稿？");
		builder.setMessage("下次打开会加载你当前写下的内容");
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				save();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	public void addImage(View v) {
		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, IMAGE_REQUEST_CODE);
	}
}

