package com.losthiro.ottohubclient;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.*;
import com.losthiro.ottohubclient.adapter.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.utils.*;
import java.util.concurrent.*;
import android.util.*;
import org.json.*;
import java.util.*;
import android.content.*;

/**
 * @Author Hiro
 * @Date 2025/06/13 19:20
 */
public class AuditActivity extends MainActivity {
	public static final String TAG = "AuditActivity";
	private static final Semaphore request = new Semaphore(1);
	private Account current;
    private boolean isFirst = true;
	private int categoryIndex = 0;
	private RecyclerView auditList;
	private SwipeRefreshLayout auditRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audit);
		if (!NetworkUtils.isNetworkAvailable(this)) {
			Toast.makeText(getApplication(), "唉网络怎么似了", Toast.LENGTH_SHORT).show();
			return;
		}
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			Toast.makeText(getApplication(), "那我缺的登录这块", Toast.LENGTH_SHORT).show();
			return;
		}
		current = manager.getAccount();
		GridLayoutManager layout = new GridLayoutManager(this, 1);
		layout.setInitialPrefetchItemCount(6);
		layout.setItemPrefetchEnabled(true);
		auditList = findViewById(R.id.audit_list);
		auditList.setLayoutManager(layout);
		auditList.setOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView view, int status) {
				super.onScrollStateChanged(view, status);
				if (status == RecyclerView.SCROLL_STATE_IDLE) {
					int itemCount = view.getLayoutManager().getItemCount();
					int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
					if (lastPos >= itemCount - 1 && itemCount > 12) {
						request(false);
					}
				}
			}
		});
		auditRefresh = findViewById(R.id.refresh);
		auditRefresh.setRefreshing(true);
		auditRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				Toast.makeText(getApplication(), "走位中...", Toast.LENGTH_SHORT).show();
				request(true);
			}
		});
		initCategoryView();
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

	private void initCategoryView() {
		View parent = findViewById(android.R.id.content);
		final TextView[] main = new TextView[4];
		main[0] = parent.findViewWithTag("video");
		main[1] = parent.findViewWithTag("blog");
		main[2] = parent.findViewWithTag("avatar");
		main[3] = parent.findViewWithTag("cover");
		for (int i = 0; i < 4; i++) {
			final int currentIndex = i;
			main[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (categoryIndex == currentIndex) {
						return;
					}
					for (int i = 0; i < 4; i++) {
						main[i].setTextColor(i == currentIndex ? Color.WHITE : Color.BLACK);
						main[i].setBackgroundResource(i == currentIndex ? R.drawable.btn_bg : R.drawable.btn_empty_bg);
					}
					categoryIndex = currentIndex;
					request(true);
				}
			});
		}
	}

	private void request(final boolean isRefresh) {
		try {
			if (!request.tryAcquire() || current == null) {
				return;
			}
			String token = current.getToken();
			String[] uris = {APIManager.VideoURI.getAuditVideo(token, 0, 12),
					APIManager.BlogURI.getAuditBlogURI(token, 0, 12),
					APIManager.ProfileURI.getAuditAvatarURI(token, 0, 12),
					APIManager.ProfileURI.getAuditCoverURI(token, 0, 12)};
			NetworkUtils.getNetwork.getNetworkJson(uris[categoryIndex], new NetworkUtils.HTTPCallback() {
				@Override
				public void onSuccess(String content) {
					if (content == null) {
						onFailed("content = null");
						return;
					}
					try {
						JSONObject json = new JSONObject(content);
						if (json.optString("status", "error").equals("success")) {
							String[] names = {"video_list", "blog_list", "avatar_list", "cover_list"};
							final List<AuditModel> data = new ArrayList<>();
							JSONArray jsonData = json.optJSONArray(names[categoryIndex]);
							for (int i = 0; i < jsonData.length(); i++) {
								data.add(new AuditModel(jsonData.optJSONObject(i), categoryIndex));
							}
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									auditRefresh.setRefreshing(false);
									if (auditList.getAdapter() == null) {
										auditList.setAdapter(new AuditAdapter(AuditActivity.this, data));
										return;
									}
									if (isRefresh) {
										auditList.getAdapter().notifyDataSetChanged();
									} else {
										((AuditAdapter) auditList.getAdapter()).addNewData(data);
									}
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
                    if(cause.equals("not_reviewer")&&isFirst){
                        runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    Toast.makeText(getApplication(), "只有审核员才可查看", Toast.LENGTH_SHORT).show();
                                }
                            });
                        isFirst = false;
                    }
					Log.e("Network", cause);
					auditRefresh.setRefreshing(false);
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

