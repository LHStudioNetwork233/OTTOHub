package com.losthiro.ottohubclient;
import android.os.Bundle;
import android.os.Build;
import android.view.*;
import android.content.*;
import android.widget.*;
import android.graphics.*;
import androidx.swiperefreshlayout.widget.*;
import androidx.recyclerview.widget.*;
import java.util.concurrent.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.utils.*;
import org.json.*;
import java.util.*;
import com.losthiro.ottohubclient.adapter.*;
import android.util.*;
import android.view.View.*;
import java.io.File;
import java.io.*;
import com.losthiro.ottohubclient.adapter.model.*;

/**
 * @Author Hiro
 * @Date 2025/06/13 19:18
 */
public class UploadManagerActivity extends BasicActivity {
	public static final String TAG = "UploadManagerActivity";
	private static final Semaphore request = new Semaphore(1);
    private static final HashMap<Integer, Integer> offsetMap = new HashMap<>();
	private int currentCategory;
	private SwipeRefreshLayout refresh;
	private RecyclerView view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_manager);
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			Toast.makeText(getApplication(), "那我缺的登录这一块", Toast.LENGTH_SHORT).show();
		}
		GridLayoutManager layout = new GridLayoutManager(this, 1);
		layout.setInitialPrefetchItemCount(6);
		layout.setItemPrefetchEnabled(true);
		view = findViewById(R.id.upload_list);
		view.setLayoutManager(layout);
		view.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView view, int state) {
				super.onScrollStateChanged(view, state);
				if (state == RecyclerView.SCROLL_STATE_IDLE) {
					int itemCount = view.getLayoutManager().getItemCount();
					int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
					if (lastPos >= itemCount - 1 && itemCount >= 12) {
						request(false);
					}
				}
			}
		});
		refresh = findViewById(R.id.refresh);
		refresh.setRefreshing(true);
		refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				// TODO: Implement this method
				Toast.makeText(getApplication(), R.string.loading, Toast.LENGTH_SHORT).show();
				request(true);
			}
		});
        Toast.makeText(getApplication(), "长按稿件删除", Toast.LENGTH_SHORT).show();
		request(true);
		initCategoryView();
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
		finish();
	}

	private void initCategoryView() {
		View parent = findViewById(android.R.id.content);
		final TextView[] categoryView = new TextView[3];
		categoryView[0] = parent.findViewWithTag("video");
		categoryView[1] = parent.findViewWithTag("blog");
		categoryView[2] = parent.findViewWithTag("local");
		for (int i = 0; i < 3; i++) {
			final int index = i;
			categoryView[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO: Implement this method
					if (index == currentCategory) {
						return;
					}
					for (int i = 0; i < 3; i++) {
						categoryView[i].setTextColor(i == index ? ResourceUtils.getColor(R.color.colorAccent) : ResourceUtils.getColor(R.color.colorSecondary));
					}
					currentCategory = index;
					request(true);
				}
			});
		}
	}

	private void request(final boolean isRefresh) {
		try {
			if (!request.tryAcquire()) {
				return;
			}
			if (currentCategory == 2) {
				String path = StringUtils.strCat(DeviceUtils.getAndroidSDK() >= Build.VERSION_CODES.R
						? getExternalFilesDir(null).toString()
						: "/sdcard/OTTOHub", "/save/");
				if (!new File(path).exists()) {
					FileUtils.createDir(path);
				}
				List<ManagerModel> localData = new ArrayList<>();
				List<File> list = FileUtils.listFile(path, new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return dir.isDirectory() && name.startsWith("OV");
					}
				});
				for (File v : list) {
					localData.add(new ManagerModel(v, ManagerModel.TYPE_VIDEO));
				}
				if (view.getAdapter() == null) {
					view.setAdapter(new ManagerAdapter(UploadManagerActivity.this, localData));
				} else if (isRefresh) {
					((ManagerAdapter) view.getAdapter()).setData(localData);
				} else {
					((ManagerAdapter) view.getAdapter()).addNewData(localData);
				}
				refresh.setRefreshing(false);
				return;
			}
            int index = currentCategory;
            int current = offsetMap.getOrDefault(index, 0);
            if (isRefresh) {
                offsetMap.put(index, 0);
            } else {
                offsetMap.put(index, current + 12);
            }
			AccountManager manager = AccountManager.getInstance(this);
			if (!manager.isLogin()) {
				return;
			}
			String token = manager.getAccount().getToken();
			String uri = currentCategory == 0
					? APIManager.ProfileURI.getVideosManageURI(token, current, 12)
					: APIManager.ProfileURI.getBlogsManageURI(token, current, 12);
			NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
				@Override
				public void onSuccess(String content) {
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
							JSONArray video = json.optJSONArray(currentCategory == 0 ? "video_list" : "blog_list");
							final List<ManagerModel> data = new ArrayList<>();
							for (int i = 0; i < video.length(); i++) {
								int type = currentCategory == 0 ? ManagerModel.TYPE_VIDEO : ManagerModel.TYPE_BLOG;
								data.add(new ManagerModel(video.optJSONObject(i), type));
							}
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (view.getAdapter() == null) {
										view.setAdapter(new ManagerAdapter(UploadManagerActivity.this, data));
										return;
									}
									if (isRefresh) {
										((ManagerAdapter) view.getAdapter()).setData(data);
									} else {
										((ManagerAdapter) view.getAdapter()).addNewData(data);
									}
								}
							});
							refresh.setRefreshing(false);
							return;
						}
						onFailed(content);
					} catch (Exception e) {
						onFailed(e.toString());
					}
				}

				@Override
				public void onFailed(String cause) {
					Log.e("Network", cause);
					refresh.setRefreshing(false);
				}
			});
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		} finally {
			request.release();
		}
	}

	public void quit(View v) {
		finish();
	}
}

