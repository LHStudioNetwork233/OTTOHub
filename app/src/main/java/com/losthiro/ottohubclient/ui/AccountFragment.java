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
import androidx.recyclerview.widget.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.utils.*;
import org.json.*;
import java.util.*;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.adapter.*;
import android.util.*;
import androidx.swiperefreshlayout.widget.*;
import java.util.concurrent.*;
import android.widget.*;
import android.view.View.*;
import android.graphics.drawable.*;
import com.losthiro.ottohubclient.*;
import com.losthiro.ottohubclient.view.drawer.*;

public class AccountFragment extends Fragment {
	public static final String TAG = "Account";
	private static final Handler uiThread = new Handler(Looper.getMainLooper());
	private static final Semaphore request = new Semaphore(1);
	private Context ctx;
	private Account current;
	private RecyclerView historyView;
	private SwipeRefreshLayout historyRefresh;

	public static AccountFragment newInstance() {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		AccountFragment accountPage = new AccountFragment();
		accountPage.setArguments(arg);
		return accountPage;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: Implement this method
		View parent = inflater.inflate(R.layout.fragment_account, container, false);
		ctx = parent.getContext();
		historyView = parent.findViewById(R.id.user_history_list);
		historyRefresh = parent.findViewById(R.id.user_history_refresh);
        SlideDrawerManager drawer = SlideDrawerManager.getInstance();
        drawer.registerDrawer(drawer.getLastParent(), (ImageButton) getView().findViewById(R.id.main_slide_bar));
        drawer.setOnAccountChangeListener(ctx, new AccountManager.AccountListener() {
                @Override
                public void onCurrentChange(Account newCurrent) {
                    // TODO: Implement this method
                    current = newCurrent;
                    updateUI();
                }
            });
        GridLayoutManager layout = new GridLayoutManager(ctx, 2);
        layout.setInitialPrefetchItemCount(6);
        layout.setItemPrefetchEnabled(true);
        historyView.setLayoutManager(layout);
        historyView.setItemViewCacheSize(20);
        historyView.setDrawingCacheEnabled(true);
        historyView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        historyView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView view, int state) {
                    super.onScrollStateChanged(view, state);
                    if (state == RecyclerView.SCROLL_STATE_IDLE) {
                        int itemCount = view.getLayoutManager().getItemCount();
                        int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
                        if (lastPos >= itemCount - 1) {
                            RecyclerView.Adapter adapter = view.getAdapter();
                            if (adapter != null && adapter instanceof VideoAdapter) {
                                ((VideoAdapter) adapter).startLoading();
                            }
                            request(false);
                        }
                    }
                }
            });
        historyRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Toast.makeText(ctx, R.string.loading, Toast.LENGTH_SHORT).show();
                    request(true);
                }
            });
		AccountManager manager = AccountManager.getInstance(ctx);
		current = manager.getAccount();
		return parent;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onViewCreated(view, savedInstanceState);
		updateUI();
	}

	private void updateUI() {
		View view = getView();
		ImageDownloader.loader((ImageView) view.findViewById(R.id.main_user_cover), current.getCoverURI());
		ImageDownloader.loader((ImageView) view.findViewById(R.id.main_user_avatar), current.getAvatarURI());
		TextView userName = view.findViewById(R.id.main_user_name);
		userName.setText(current.getName());
		((TextView) view.findViewById(R.id.main_user_info)).setText(StringUtils.strCat(
				new Object[]{"UID: ", current.getUID(), " 性别: ", current.getSex(), " 注册日: ", current.getTime()}));
		View parent = (View) userName.getParent();
		parent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ctx, AccountDetailActivity.class);
				i.putExtra("uid", current.getUID());
				i.putExtra("avatar", current.getAvatarURI());
				startActivity(i);
			}
		});
		HashMap<String, Integer> levelMap = current.getLevel();
		GradientDrawable bg = new GradientDrawable();
		bg.setCornerRadius(8f);
		TextView levelText = view.findViewById(R.id.main_user_level);
		for (HashMap.Entry<String, Integer> entry : levelMap.entrySet()) {
			levelText.setText(entry.getKey());
			bg.setColor(entry.getValue());
		}
		levelText.setBackgroundDrawable(bg);
		HonourAdapter honour = new HonourAdapter(ctx, Arrays.asList(current.getHonours()));
		honour.setHiddenDef(false);
		RecyclerView honourList = view.findViewById(R.id.main_user_honours);
		honourList.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false));
		honourList.setAdapter(honour);
		String videoCount = StringUtils.strCat(new Object[]{current.getVideoCount(), System.lineSeparator(), "视频"});
		String blogCount = StringUtils.strCat(new Object[]{current.getBlogCount(), System.lineSeparator(), "动态"});
		String followingCount = StringUtils
				.strCat(new Object[]{current.getFollowingCount(), System.lineSeparator(), "关注"});
		String fansCount = StringUtils.strCat(new Object[]{current.getFansCount(), System.lineSeparator(), "粉丝"});
		int color = ResourceUtils.getColor(R.color.colorAccent);
		new ClientString(videoCount).colorTo((TextView) view.findViewById(R.id.main_user_video_count), color);
		new ClientString(blogCount).colorTo((TextView) view.findViewById(R.id.main_user_blog_count), color);
		new ClientString(followingCount).colorTo((TextView) view.findViewById(R.id.main_user_following_count), color);
		new ClientString(fansCount).colorTo((TextView) view.findViewById(R.id.main_user_fans_count), color);
		request(true);
	}

	private void request(final boolean isRefresh) {
		try {
			if (!request.tryAcquire()) {
				return;
			}
			AccountManager manager = AccountManager.getInstance(ctx);
			if (!manager.isLogin()) {
				return;
			}
			NetworkUtils.getNetwork.getNetworkJson(
					APIManager.ProfileURI.getNetworkHistoryURI(manager.getAccount().getToken()),
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
									JSONArray video = json.optJSONArray("video_list");
									final List<Video> data = new ArrayList<>();
									for (int i = 0; i < video.length(); i++) {
										data.add(new Video(ctx, video.optJSONObject(i), Video.VIDEO_DEF));
									}
									uiThread.post(new Runnable() {
										@Override
										public void run() {
											RecyclerView.Adapter adapter = historyView.getAdapter();
											if (adapter == null) {
												adapter = new VideoAdapter(ctx, data);
												historyView.setAdapter(adapter);
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
									historyRefresh.setRefreshing(false);
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
							historyRefresh.setRefreshing(false);
						}
					});
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		} finally {
			request.release();
		}
	}
}

