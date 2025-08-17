/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.ui;
import com.losthiro.ottohubclient.R;
import androidx.fragment.app.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.content.*;
import android.widget.*;
import android.view.View.*;
import com.losthiro.ottohubclient.impl.*;
import androidx.recyclerview.widget.*;
import com.losthiro.ottohubclient.*;
import com.losthiro.ottohubclient.utils.*;
import androidx.swiperefreshlayout.widget.*;
import java.util.concurrent.*;
import org.json.*;
import java.util.*;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.adapter.*;
import android.util.*;
import android.graphics.*;

public class BlogsFragment extends Fragment {
	public static final String TAG = "Blogs";
	private static final Handler uiThread = new Handler(Looper.getMainLooper());
	private static final Semaphore request = new Semaphore(1);
	private Context ctx;
	private int categoryIndex = 0;
	private SwipeRefreshLayout blogRefresh;
	private RecyclerView blogList;
	private BlogAdapter adapter;
	private ImageView userMain;
	private TextView[] categorys;

	public static BlogsFragment newInstance() {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		BlogsFragment blogPage = new BlogsFragment();
		blogPage.setArguments(arg);
		return blogPage;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: Implement this method
		View parent = inflater.inflate(R.layout.fragment_blogs, container, false);
		ctx = parent.getContext();
		userMain = parent.findViewById(R.id.main_user_avatar);
		blogRefresh = parent.findViewById(R.id.blog_refresh);
		blogList = parent.findViewById(R.id.blogs_list);
        blogRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Toast.makeText(ctx, R.string.loading, Toast.LENGTH_SHORT).show();
                    requestCategory(true);
                }
            });
        GridLayoutManager layout = new GridLayoutManager(ctx, 1);
        layout.setInitialPrefetchItemCount(6);
        layout.setItemPrefetchEnabled(true);
        blogList.setItemViewCacheSize(20);
        blogList.setDrawingCacheEnabled(true);
        blogList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        blogList.setLayoutManager(layout);
        blogList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView view, int state) {
                    super.onScrollStateChanged(view, state);
                    if (state == RecyclerView.SCROLL_STATE_IDLE) {
                        int itemCount = view.getLayoutManager().getItemCount();
                        int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
                        if (lastPos >= itemCount - 1) {
                            if (adapter != null) {
                                adapter.startLoading();
                            }
                            requestCategory(false);
                        }
                    }
                }
            });
		return parent;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onViewCreated(view, savedInstanceState);
		if (!NetworkUtils.isNetworkAvailable(ctx)) {
			Toast.makeText(ctx, R.string.error_network, Toast.LENGTH_SHORT).show();
		}
		blogRefresh.setRefreshing(true);
		final AccountManager manager = AccountManager.getInstance(ctx);
		userMain.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (manager.isLogin()) {
					Intent i = new Intent(ctx, AccountDetailActivity.class);
					i.putExtra("uid", manager.getAccount().getUID());
					requireActivity().startActivity(i);
				} else {
					requireActivity().startActivityForResult(new Intent(ctx, LoginActivity.class),
							BasicActivity.LOGIN_REQUEST_CODE);
				}
			}
		});
		if (manager.isLogin()) {
			AccountCallback(manager.getAccount());
		}
		initCategoryView();
		requestCategory(true);
	}

	@Override
	public void onResume() {
		// TODO: Implement this method
		super.onResume();
		AccountManager manager = AccountManager.getInstance(ctx);
		if (userMain != null && manager.isLogin()) {
			ImageDownloader.loader(userMain, manager.getAccount().getAvatarURI());
		}
	}

	private void requestCategory(boolean isRefresh) {
		String uri = APIManager.BlogURI.getRandomBlogURI(12);
		int index = categoryIndex;
		if (index == 1) {
			uri = APIManager.BlogURI.getNewBlogURI(0, 12);
		}
		if (index >= 2 && index < 5) {
			int mode = -1;
			if (index == 2) {
				mode = APIManager.BlogURI.WEEK;
			}
			if (index == 3) {
				mode = APIManager.BlogURI.MONTH;
			}
			if (index == 4) {
				mode = APIManager.BlogURI.QUARTERLY;
			}
			uri = APIManager.BlogURI.getPopularBlogURI(mode, 0, 12);
		}
		request(uri, isRefresh);
	}

	private void request(String uri, final boolean isRefresh) {
		try {
			if (!request.tryAcquire()) {
				return;
			}
			if (isRefresh) {
				blogRefresh.setRefreshing(true);
			}
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
							JSONArray blog = json.optJSONArray("blog_list");
							final List<Blog> data = new ArrayList<>();
							for (int i = 0; i < blog.length(); i++) {
								data.add(new Blog(blog.optJSONObject(i)));
							}
							uiThread.post(new Runnable() {
								@Override
								public void run() {
									if (adapter == null) {
										adapter = new BlogAdapter(ctx, data);
										blogList.setAdapter(adapter);
										return;
									}
									if (isRefresh) {
										adapter.setData(data);
									} else {
										adapter.addNewData(data);
										adapter.stopLoading();
									}
								}
							});
							blogRefresh.setRefreshing(false);
							return;
						}
						onFailed(content);
					} catch (JSONException e) {
						onFailed(e.toString());
					}
				}

				@Override
				public void onFailed(String cause) {
					Log.e("Network", cause);
					blogRefresh.setRefreshing(false);
				}
			});
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		} finally {
			request.release();
		}
	}

	private void initCategoryView() {
		View contentView = getView();
		final int categoryCount = 5;
		categorys = new TextView[categoryCount];
		categorys[0] = contentView.findViewWithTag("recommend");
		categorys[1] = contentView.findViewWithTag("new");
		categorys[2] = contentView.findViewWithTag("week");
		categorys[3] = contentView.findViewWithTag("month");
		categorys[4] = contentView.findViewWithTag("quarterly");
		for (int i = 0; i < categoryCount; i++) {
			final int index = i;//防止越权
			categorys[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (categoryIndex == index) {
						return;
					}
					for (int i = 0; i < categoryCount; i++) {
						TextView current = categorys[i];
						current.setBackgroundResource(i == index ? R.drawable.btn_bg : R.drawable.btn_empty_bg);
						current.setTextColor(i == index ? Color.WHITE : ResourceUtils.getColor(R.color.colorSecondary));
					}
					requestCategory(true);
					categoryIndex = index;
				}
			});
		}
	}

	public void AccountCallback(Account a) {
		ImageDownloader.loader(userMain, a.getAvatarURI());
	}
}

