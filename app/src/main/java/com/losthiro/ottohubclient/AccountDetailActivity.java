package com.losthiro.ottohubclient;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.losthiro.ottohubclient.adapter.model.Blog;
import com.losthiro.ottohubclient.adapter.BlogAdapter;
import com.losthiro.ottohubclient.adapter.HonourAdapter;
import com.losthiro.ottohubclient.adapter.model.Video;
import com.losthiro.ottohubclient.adapter.VideoAdapter;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.impl.ClientString;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.DeviceUtils;
import com.losthiro.ottohubclient.utils.FileUtils;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.provider.*;
import android.net.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.adapter.*;
import android.widget.*;
import android.graphics.drawable.*;
import android.graphics.*;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.utils.*;
import androidx.viewpager.widget.*;
import com.losthiro.ottohubclient.adapter.page.*;
import androidx.fragment.app.*;
import com.losthiro.ottohubclient.ui.*;
import android.app.AlertDialog;
import android.content.*;
import com.losthiro.ottohubclient.crashlogger.*;

/**
 * @Author Hiro
 * @Date 2025/06/01 14:54
 */
public class AccountDetailActivity extends BasicActivity {
	public static final String TAG = "AccountDetailActivity";
	private long uid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_detail);
		Intent i = getIntent();
		FragmentManager manager = getSupportFragmentManager();
		Fragment userPage = manager.findFragmentById(R.id.user_page);
		uid = i.getLongExtra("uid", 0);
		if (userPage == null) {
			userPage = UserFragment.newInstance(uid);
			FragmentTransaction transacte = manager.beginTransaction();
			transacte.add(R.id.user_page, userPage);
			transacte.commit();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO: Implement this method
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// TODO: Implement this method
		super.onBackPressed();
		Fragment userPage = getSupportFragmentManager().findFragmentById(R.id.user_page);
		if (userPage instanceof UserFragment) {
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO: Implement this method
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && data != null) {
			Uri uri = data.getData();
            Toast.makeText(getApplication(), uri.toString(), Toast.LENGTH_SHORT).show();
			Account current = AccountManager.getInstance(this).getAccount();
			if (requestCode == IMAGE_REQUEST_CODE) {
				new ImageUploader(this, "https://api.ottohub.cn/module/creator/update_avatar.php",
						APIManager.CreatorURI.getUpdateAvatarURI(current.getToken()), new NetworkUtils.HTTPCallback() {
							@Override
							public void onSuccess(String content) {
								// TODO: Implement this method
								try {
                                    Toast.makeText(getApplication(), content, Toast.LENGTH_SHORT).show();
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
								NetworkException.getInstance(getApplication()).handlerError(cause);
							}
						}).execute(uri);
			}
		}
	}

	public void editName(String name) {
		Context ctx = getApplication();
		if (name.isEmpty()) {
			Toast.makeText(ctx, "不能输入棍母名字", Toast.LENGTH_SHORT).show();
			return;
		}
		AccountManager manager = AccountManager.getInstance(ctx);
		if (!manager.isLogin()) {
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(
				APIManager.ProfileURI.getNameEditURI(manager.getAccount().getToken(), name),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						// TODO: Implement this method
						if (content == null || content.isEmpty()) {
							onFailed("empty content");
							return;
						}
						try {
							JSONObject json = new JSONObject(content);
							if (json == null) {
								onFailed("null json");
								return;
							}
							String status = json.optString("status", "error");
							if (status.equals("success")) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(getApplication(), "名称更新成功", Toast.LENGTH_SHORT).show();
									}
								});
								return;
							}
							onFailed(json.optString("message"));
						} catch (JSONException e) {
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

	public void setUserTitle(String title) {
		((TextView) findViewById(R.id.user_name)).setText(StringUtils.strCat(title, "的主页"));
	}

	public void loadUserProfile() {
		AccountManager account = AccountManager.getInstance(this);
		FragmentManager manager = getSupportFragmentManager();
		Fragment userPage = manager.findFragmentById(R.id.user_page);
		FragmentTransaction transacte = manager.beginTransaction();
		Fragment newFragment = userPage;
		if (userPage instanceof UserFragment) {
			newFragment = ProfileFragment.newInstance(uid);
		} else if (account.isLogin()) {
			newFragment = UserFragment.newInstance(uid);
		}
		transacte.replace(R.id.user_page, newFragment);
		transacte.addToBackStack(null);
		transacte.commit();
	}

	public void loadFavoriteVideo(View v) {
		Fragment userPage = getSupportFragmentManager().findFragmentById(R.id.user_page);
		if (userPage instanceof UserFragment) {
			((UserFragment) userPage).loadFavouriteVideo();
		}
	}

	public void loadFavoriteBlog(View v) {
		Fragment userPage = getSupportFragmentManager().findFragmentById(R.id.user_page);
		if (userPage instanceof UserFragment) {
			((UserFragment) userPage).loadFavouriteBlog();
		}
	}

	public void quit(View v) {
		Fragment userPage = getSupportFragmentManager().findFragmentById(R.id.user_page);
		if (userPage instanceof UserFragment) {
			finish();
			return;
		}
		loadUserProfile();
	}
}

