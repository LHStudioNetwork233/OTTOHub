/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.ui;
import com.losthiro.ottohubclient.R;
import androidx.fragment.app.*;
import android.view.*;
import android.os.*;
import androidx.recyclerview.widget.*;
import android.content.*;
import androidx.swiperefreshlayout.widget.*;
import android.widget.*;
import androidx.viewpager.widget.*;
import android.view.View.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.*;
import com.losthiro.ottohubclient.utils.*;
import org.json.*;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.adapter.page.*;
import android.util.*;
import java.util.concurrent.*;
import com.losthiro.ottohubclient.adapter.*;
import android.graphics.*;
import android.graphics.drawable.*;
import com.losthiro.ottohubclient.view.drawer.*;
import java.util.List;
import java.util.ArrayList;
import androidx.lifecycle.*;

public class VideosFragment extends Fragment {
	public final static String TAG = "Videos";
	private final static Handler uiThread = new Handler(Looper.getMainLooper());
	private static final Semaphore request = new Semaphore(1);
	private static Runnable mCallback;
	private Context ctx;
	private boolean isAuto = true;
	private int categoryIndex = 0;
	private RecyclerView videoList;
	private SwipeRefreshLayout videoRefresh;
	private ViewPager popularList;
	private ImageView userAvatar;
	private TextView countView;
	private TextView[] categorys;

	public static VideosFragment newInstance() {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		VideosFragment videoPage = new VideosFragment();
		videoPage.setArguments(arg);
		return videoPage;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: Implement this method
		View parent = inflater.inflate(R.layout.fragment_videos, container, false);
		ctx = parent.getContext();
		popularList = parent.findViewById(R.id.popular_list);
		videoList = parent.findViewById(R.id.videos_list);
		videoRefresh = parent.findViewById(R.id.video_refresh);
		userAvatar = parent.findViewById(R.id.main_user_avatar);
		countView = parent.findViewById(R.id.main_message_count);
		GridLayoutManager layout = new GridLayoutManager(ctx, 2);
		layout.setInitialPrefetchItemCount(6);
		layout.setItemPrefetchEnabled(true);
		videoList.setLayoutManager(layout);
		videoList.setItemViewCacheSize(20);
		videoList.setDrawingCacheEnabled(true);
		videoList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		videoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView view, int state) {
				super.onScrollStateChanged(view, state);
				if (state == RecyclerView.SCROLL_STATE_IDLE) {
					int itemCount = view.getLayoutManager().getItemCount();
					int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
					if (lastPos >= itemCount - 1) {
						RecyclerView.Adapter adapter = videoList.getAdapter();
						if (adapter != null && adapter instanceof VideoAdapter) {
							((VideoAdapter) adapter).startLoading();
						}
						requestCategory(false);
					}
				}
			}
		});
		videoRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				Toast.makeText(ctx, R.string.loading, Toast.LENGTH_SHORT).show();
				requestCategory(true);
			}
		});
		userAvatar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AccountManager manager = AccountManager.getInstance(ctx);
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
		SlideDrawerManager slide = SlideDrawerManager.getInstance();
		slide.registerDrawer(slide.getLastParent(), (ImageButton) parent.findViewById(R.id.main_slide_bar));
		slide.setOnAccountChangeListener(ctx, new AccountManager.AccountListener() {
			@Override
			public void onCurrentChange(Account newCurrent) {
				// TODO: Implement this method
				ImageDownloader.loader(userAvatar, newCurrent.getAvatarURI());
				if (mCallback != null) {
					mCallback.run();
				}
			}
		});
		return parent;
	}

	@Override
	public void onViewCreated(View parent, Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onViewCreated(parent, savedInstanceState);
		AccountManager manager = AccountManager.getInstance(ctx);
		if (manager.isLogin()) {
			String uri = APIManager.MessageURI.getNewMessageURI(manager.getAccount().getToken());
			ImageDownloader.loader(userAvatar, manager.getAccount().getAvatarURI());
			initMessageView(uri);
		} else if (!NetworkUtils.isNetworkAvailable(ctx)) {
			Toast.makeText(ctx, R.string.error_network, Toast.LENGTH_SHORT).show();
		} else {
			manager.resetLogin();
		}
		videoRefresh.setRefreshing(true);
		initPopularList(parent);
		initCategoryView();
		requestCategory(true);
	}

	@Override
	public void onResume() {
		// TODO: Implement this method
		super.onResume();
		AccountManager manager = AccountManager.getInstance(ctx);
		if (userAvatar != null && manager.isLogin()) {
			ImageDownloader.loader(userAvatar, manager.getAccount().getAvatarURI());
		}
	}

	private void initCategoryView() {
		View contentView = getView();
		final int categoryCount = 13;
		categorys = new TextView[categoryCount];
		categorys[0] = contentView.findViewWithTag("recommend");
		categorys[1] = contentView.findViewWithTag("new");
		categorys[2] = contentView.findViewWithTag("week");
		categorys[3] = contentView.findViewWithTag("month");
		categorys[4] = contentView.findViewWithTag("quarterly");
		categorys[5] = contentView.findViewWithTag("1");
		categorys[6] = contentView.findViewWithTag("2");
		categorys[7] = contentView.findViewWithTag("3");
		categorys[8] = contentView.findViewWithTag("4");
		categorys[9] = contentView.findViewWithTag("5");
		categorys[10] = contentView.findViewWithTag("6");
		categorys[11] = contentView.findViewWithTag("7");
		categorys[12] = contentView.findViewWithTag("0");
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
					((View) popularList.getParent()).setVisibility(index > 0 ? View.GONE : View.VISIBLE);
					categoryIndex = index;
					requestCategory(true);
				}
			});
		}
	}

	private void initPopularList(final View parent) {
		NetworkUtils.getNetwork.getNetworkJson(APIManager.SystemURI.getSlideURI(), new NetworkUtils.HTTPCallback() {
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
						JSONArray video = json.optJSONArray("slides");
						final List<JSONObject> data = new ArrayList<>();
						for (int i = 0; i < video.length(); i++) {
							data.add(video.optJSONObject(i));
						}
						uiThread.post(new Runnable() {
							@Override
							public void run() {
								PopularAdapter popular = new PopularAdapter(ctx, data);
								popularList.setAdapter(popular);
								updatePopular(parent);
							}
						});
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
			}
		});
	}

	private void updatePopular(View parent) {
		if (parent == null) {
			return;
		}
		final TextView currentTitle = parent.findViewById(R.id.list_popular_title);
		popularList.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int p, float p1, int p2) {
				PopularAdapter popular = (PopularAdapter) popularList.getAdapter();
				currentTitle.setText(popular.getTitle(p));
			}

			@Override
			public void onPageScrollStateChanged(int p) {
			}

			@Override
			public void onPageSelected(int p) {
			}
		});
		uiThread.post(new Runnable() {
			@Override
			public void run() {
				if (isAuto) {
					nextPopular();
					uiThread.postDelayed(this, 5000L);
				} else {
					uiThread.removeCallbacks(this);
				}
			}
		});
	}

	private void requestCategory(boolean isRefresh) {
		String uri = APIManager.VideoURI.getRandomVideoURI(12);
		int index = categoryIndex;
		if (index == 1) {
			uri = APIManager.VideoURI.getNewVideoURI(0, 12);
		}
		if (index >= 2 && index < 5) {
			int mode = -1;
			if (index == 2) {
				mode = APIManager.VideoURI.WEEK;
			}
			if (index == 3) {
				mode = APIManager.VideoURI.MONTH;
			}
			if (index == 4) {
				mode = APIManager.VideoURI.QUARTERLY;
			}
			uri = APIManager.VideoURI.getPopularVideoURI(mode, 0, 12);
		}
		if (index >= 5 && index < categorys.length - 1) {
			uri = APIManager.VideoURI.getCategoryVideoURI(index - 4, 12);
		}
		if (index == categorys.length - 1) {
			uri = APIManager.VideoURI.getCategoryVideoURI(APIManager.VideoURI.CATEGORY_OTHER, 12);
		}
		request(uri, isRefresh);
	}

	private void request(String uri, final boolean isRefresh) {
		try {
			if (!request.tryAcquire()) {
				return;
			}
			if (!NetworkUtils.isNetworkAvailable(ctx)) {
				Toast.makeText(ctx, R.string.error_network, Toast.LENGTH_SHORT).show();
				return;
			}
			if (isRefresh) {
				videoRefresh.setRefreshing(true);
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
							JSONArray video = json.optJSONArray("video_list");
							final List<Video> data = new ArrayList<>();
							for (int i = 0; i < video.length(); i++) {
								data.add(new Video(ctx, video.optJSONObject(i), Video.VIDEO_DEF));
							}
							uiThread.post(new Runnable() {
								@Override
								public void run() {
									RecyclerView.Adapter adapter = videoList.getAdapter();
									if (adapter == null) {
										adapter = new VideoAdapter(ctx, data);
										videoList.setAdapter(adapter);
										return;
									}
									if (adapter instanceof VideoAdapter) {
										VideoAdapter instance = (VideoAdapter) adapter;
										if (isRefresh) {
											instance.setData(data);
										} else {
											instance.addNewData(data);
											instance.stopLoading();
										}
									}
								}
							});
							videoRefresh.setRefreshing(false);
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
					videoRefresh.setRefreshing(false);
				}
			});
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		} finally {
			request.release();
		}
	}

	public void initMessageView(String token) {
		NetworkUtils.getNetwork.getNetworkJson(APIManager.MessageURI.getNewMessageURI(token),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						if (content == null || content.isEmpty()) {
							onFailed("content is empty!");
							return;
						}
						try {
							JSONObject data = new JSONObject(content);
							if (data.optString("status", "error").equals("success")) {
								String num = data.optString("new_message_num", null);
								final int msgCount = num == null
										? data.optInt("new_message_num", 0)
										: Integer.parseInt(num);
								uiThread.post(new Runnable() {
									@Override
									public void run() {
										GradientDrawable bg = new GradientDrawable();
										bg.setColor(Color.RED);
										bg.setShape(GradientDrawable.OVAL);
										bg.setCornerRadius(20);
										countView.setText(msgCount > 99 ? "99+" : StringUtils.toStr(msgCount));
										countView.setBackground(bg);
										countView.setVisibility(msgCount > 0 ? View.VISIBLE : View.GONE);
									}
								});
							}
						} catch (JSONException e) {
							onFailed(e.toString());
						}
					}

					@Override
					public void onFailed(String cause) {
						Log.e(TAG, cause);
					}
				});
	}

	public void AccountCallback(Account a) {
		ImageDownloader.loader(userAvatar, a.getAvatarURI());
		SlideDrawerManager manager = SlideDrawerManager.getInstance();
		manager.registerDrawer(manager.getLastParent(), (ImageButton) getView().findViewById(R.id.main_slide_bar));
		initMessageView(a.getToken());
	}

	public void lastPopular() {
		PopularAdapter popular = (PopularAdapter) popularList.getAdapter();
		int max = popular.getCount();
		int current = popularList.getCurrentItem();
		current--;
		popularList.setCurrentItem((current + max) % max, true);
	}

	public void nextPopular() {
		PopularAdapter popular = (PopularAdapter) popularList.getAdapter();
		int max = popular.getCount();
		int current = popularList.getCurrentItem();
		current++;
		popularList.setCurrentItem(current % max, true);
	}

	public static void setOnAccountChangeListener(Runnable callback) {
		mCallback = callback;
	}
}

