/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient;

import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.*;
import com.losthiro.ottohubclient.*;
import com.losthiro.ottohubclient.adapter.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.utils.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

public class FansActivity extends MainActivity {
	private static final Semaphore request = new Semaphore(1);
	private long uid;
	private int maxCount = 12;
	private SwipeRefreshLayout refresh;
	private RecyclerView view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fans);
		if (!NetworkUtils.isNetworkAvailable(this)) {
			Toast.makeText(getApplication(), "唉服务器怎么似了", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent i = getIntent();
		uid = i.getLongExtra("uid", 0);
		int fansCount = i.getIntExtra("fans_count", 0);
		if (uid == 0 || fansCount == 0) {
			AccountManager manager = AccountManager.getInstance(this);
			if (!manager.isLogin()) {
				Toast.makeText(getApplication(), "那我缺的登录这一块", Toast.LENGTH_SHORT).show();
				return;
			}
			Account current = manager.getAccount();
			uid = current.getUID();
			fansCount = current.getFansCount();
		}
		GridLayoutManager layout = new GridLayoutManager(this, 1);
		layout.setInitialPrefetchItemCount(6);
		layout.setItemPrefetchEnabled(true);
		view = findViewById(R.id.fans_list);
		view.setLayoutManager(layout);
		if (fansCount >= 12) {
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
		} else {
			maxCount = fansCount;
		}
		refresh = findViewById(R.id.refresh);
		refresh.setRefreshing(true);
		refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				// TODO: Implement this method
				Toast.makeText(getApplication(), "走位中...", Toast.LENGTH_SHORT).show();
				request(true);
			}
		});
		request(true);
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

	private void request(final boolean isRefresh) {
		try {
			if (!request.tryAcquire()) {
				return;
			}
			AccountManager manager = AccountManager.getInstance(this);
			if (!manager.isLogin()) {
				return;
			}
			NetworkUtils.getNetwork.getNetworkJson(APIManager.FollowingURI.getFanListURI(uid, 0, maxCount),
					new NetworkUtils.HTTPCallback() {
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
									JSONArray video = json.optJSONArray("user_list");
									final List<SearchContent> data = new ArrayList<>();
									for (int i = 0; i < video.length(); i++) {
										data.add(new SearchContent(new User(video.optJSONObject(i))));
									}
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if (view.getAdapter() == null) {
												view.setAdapter(new SearchAdapter(FansActivity.this, data));
												return;
											}
											if (isRefresh) {
												((SearchAdapter) view.getAdapter()).setData(data);
											} else {
												((SearchAdapter) view.getAdapter()).addNewData(data);
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

