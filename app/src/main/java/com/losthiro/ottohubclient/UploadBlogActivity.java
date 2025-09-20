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
import java.net.*;
import java.nio.charset.*;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.view.dialog.*;
import android.text.*;
import androidx.fragment.app.*;
import android.app.AlertDialog;
import com.losthiro.ottohubclient.ui.*;
import com.losthiro.ottohubclient.crashlogger.*;

/**
 * @Author Hiro
 * @Date 2025/06/13 19:15
 */
public class UploadBlogActivity extends BasicActivity
		implements
			NetworkUtils.HTTPCallback,
			CompoundButton.OnCheckedChangeListener {
	public static final String TAG = "UploadBlogActivity";
	private LimitEditFragment blogEdit;
	private EditText uriEdit;
	private AlertDialog dialog;

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
		blogEdit = LimitEditFragment.newInstance(8888, "说点什么吧...");
		blogEdit.setRetainInstance(true);
		FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
		tran.add(R.id.blog_upload_title, LimitEditFragment.newInstance(44, "写一个好的标题..."));
		tran.add(R.id.blog_upload_content, blogEdit);
		tran.commit();
		dialog = new UploadDialog(this).create();
		uriEdit = findViewById(R.id.blog_upload_uri);
		Switch mode = findViewById(R.id.blog_upload_switch);
		mode.setOnCheckedChangeListener(this);
		loadCurrent();
	}

	@Override
	protected void onDestroy() {
		// TODO: Implement this method
		if (ClientSettings.getInstance().getBoolean(ClientSettings.SettingPool.MSG_AUTO_SAVE)) {
			save();
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
			loadMarker(data.getData());
		}
	}

	@Override
	public void onSuccess(String reslutURI) {
		// TODO: Implement this method
		try {
			JSONObject json = new JSONObject(reslutURI);
			String msg = json.optString("message");
			if (!json.optBoolean("status", false)) {
				onFailed(msg);
				return;
			}
			JSONObject data = json.optJSONObject("data");
			if (data == null) {
				onFailed(msg);
				return;
			}
			JSONObject links = data.optJSONObject("links");
			if (links == null) {
				onFailed(msg);
				return;
			}
			String currentContent = blogEdit.getText();
			Object[] array = {currentContent, System.lineSeparator(), links.optString("markdown"),
					System.lineSeparator()};
			String newContent = StringUtils.strCat(array);
			blogEdit.setText(newContent);
			dialog.dismiss();
		} catch (Exception e) {
			onFailed(e.toString());
		}
	}

	@Override
	public void onFailed(String message) {
		// TODO: Implement this method
		Log.e("Network", message);
		dialog.dismiss();
		Toast.makeText(getApplication(), "上传失败，请检查是否已连接网络，如一切正常请上报至开发者", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO: Implement this method
		FragmentManager manager = getSupportFragmentManager();
		Fragment layout = manager.findFragmentById(R.id.blog_upload_content);
		if (layout == null) {
			return;
		}
		Fragment title = manager.findFragmentById(R.id.blog_upload_title);
		if (title == null) {
			return;
		}
		View titleView = title.getView();
		FragmentTransaction tran = manager.beginTransaction();
		if (isChecked && layout instanceof LimitEditFragment) {
			BlogPreviewFragment preview = BlogPreviewFragment.newInstance(((LimitEditFragment) title).getText(),
					blogEdit.getText());
			tran.replace(R.id.blog_upload_content, preview);
			tran.addToBackStack(null);
			titleView.setVisibility(View.GONE);
		} else {
			manager.popBackStack();
			titleView.setVisibility(View.VISIBLE);
		}
		tran.commit();
	}

	private void loadMarker(Uri uri) {
		if (!NetworkUtils.isNetworkAvailable(this)) {
			Toast.makeText(getApplication(), "唉服务器怎么似了", Toast.LENGTH_SHORT).show();
			return;
		}
		new ImageMarker(this, this).execute(uri);
		dialog.show();
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
										blogEdit.setText(json.optString("content", ""));
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
                        NetworkException.getInstance(getApplication()).handlerError(cause);
					}
				});
	}

	private void save() {
		Account a = AccountManager.getInstance(this).getAccount();
		if (a == null) {
			return;
		}
		NetworkUtils.postNetwork.postJSON("https://api.ottohub.cn/module/creator/save_blog.php",
				APIManager.CreatorURI.getSaveBlogURI(a.getToken(), blogEdit.getText()),
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
                        NetworkException.getInstance(getApplication()).handlerError(cause);
					}
				});
	}

	private void send() {
		Account a = AccountManager.getInstance(this).getAccount();
		if (a == null) {
			Toast.makeText(getApplication(), "那我缺的登录这一块", Toast.LENGTH_SHORT).show();
			return;
		}
		LimitEditFragment title = (LimitEditFragment) getSupportFragmentManager()
				.findFragmentById(R.id.blog_upload_title);
		final AlertDialog dialog = new UploadDialog(this).create();
		NetworkUtils.postNetwork.postJSON("https://api.ottohub.cn/module/creator/submit_blog.php",
				APIManager.CreatorURI.getSubmitBlogURI(a.getToken(), title.getText(), blogEdit.getText()),
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
										dialog.dismiss();
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
                        NetworkException.getInstance(getApplication()).handlerError(cause);
					}
				});
		dialog.show();
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

	public void addImageURI(View v) {
		String uri = uriEdit.getText().toString();
		if (uri.isEmpty()) {
			Toast.makeText(this, "不能输入棍母uri", Toast.LENGTH_SHORT).show();
			return;
		}
		String currentContent = blogEdit.getText();
		Object[] array = {currentContent, System.lineSeparator(), "![](", uri, ")", System.lineSeparator()};
		String newContent = StringUtils.strCat(array);
		blogEdit.setText(newContent);
	}
}

