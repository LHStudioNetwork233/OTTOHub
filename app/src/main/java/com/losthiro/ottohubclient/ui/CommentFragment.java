/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.ui;
import android.os.*;
import android.view.*;
import androidx.fragment.app.*;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.*;
import com.losthiro.ottohubclient.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.utils.*;
import org.json.*;
import com.losthiro.ottohubclient.adapter.model.*;
import java.util.*;
import com.losthiro.ottohubclient.adapter.*;
import android.util.*;
import java.util.concurrent.*;
import android.widget.*;
import android.content.*;
import android.graphics.*;

public class CommentFragment extends Fragment {
	public final static String TAG = "Comments";
	private static final Handler uiThread = new Handler(Looper.getMainLooper());
	private static final Semaphore request = new Semaphore(1);
	private SwipeRefreshLayout commentRefresh;
	private RecyclerView commentView;
	private int offset;

	public static CommentFragment newInstance(long id, int type) {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		arg.putLong("id", id);
		arg.putInt("type", type);
		CommentFragment commentPage = new CommentFragment();
		commentPage.setArguments(arg);
		return commentPage;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: Implement this method
		View root = inflater.inflate(R.layout.fragment_comment_list, container, false);
		commentRefresh = root.findViewById(R.id.refresh);
		commentView = root.findViewById(R.id.comment_list);
		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onViewCreated(view, savedInstanceState);
		final Context ctx = getContext();
		GridLayoutManager layout = new GridLayoutManager(ctx, 1);
		layout.setInitialPrefetchItemCount(6);
		layout.setItemPrefetchEnabled(true);
		commentView.setLayoutManager(layout);
		commentView.setItemViewCacheSize(20);
		commentView.setDrawingCacheEnabled(true);
		commentView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		commentView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView view, int state) {
				super.onScrollStateChanged(view, state);
				if (state == RecyclerView.SCROLL_STATE_IDLE) {
					int itemCount = view.getLayoutManager().getItemCount();
					int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
					if (lastPos >= itemCount - 1) {
						RecyclerView.Adapter adapter = view.getAdapter();
						if (adapter != null && adapter instanceof CommentAdapter) {
							((CommentAdapter) adapter).startLoading();
						}
						offset = offset + 12;
						loadComment(false);
					}
				}
			}
		});
		commentRefresh.setRefreshing(true);
		commentRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				Toast.makeText(ctx, R.string.loading, Toast.LENGTH_SHORT).show();
				offset = 0;
				loadComment(true);
			}
		});
		loadComment(true);
	}

	public void loadComment(final boolean isRefresh) {
		try {
			if (!request.tryAcquire()) {
				return;
			}
			Bundle arg = getArguments();
			if (arg == null) {
				return;
			}
			final Context ctx = getContext();
			final long id = arg.getLong("id");
			final int type = arg.getInt("type");
			AccountManager manager = AccountManager.getInstance(ctx);
			String v = manager.isLogin()
					? APIManager.CommentURI.getVideoCommentURI(id, 0, manager.getAccount().getToken(), offset, 12)
					: APIManager.CommentURI.getVideoCommentURI(id, 0, offset, 12);
			String b = manager.isLogin()
					? APIManager.CommentURI.getBlogCommentURI(id, 0, manager.getAccount().getToken(), offset, 12)
					: APIManager.CommentURI.getBlogCommentURI(id, 0, offset, 12);
			String uri = type == Comment.TYPE_BLOG ? b : v;
			NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
				@Override
				public void onSuccess(final String content) {
					if (content == null || content.isEmpty()) {
						onFailed("empty content");
						return;
					}
					try {
						final JSONObject root = new JSONObject(content);
						if (root == null) {
							onFailed("null json");
							return;
						}
						if (root.optString("status", "error").equals("success")) {
							JSONArray array = root.optJSONArray("comment_list");
							final List<Comment> data = new ArrayList<>();
							for (int i = 0; i < array.length(); i++) {
								Comment currentComment = new Comment(ctx, array.optJSONObject(i), id, type);
								CommentAdapter adapter = (CommentAdapter) commentView.getAdapter();
								if (adapter == null || !adapter.isCommentExists(currentComment)) {
									data.add(currentComment);
								}
							}
							uiThread.post(new Runnable() {
								@Override
								public void run() {
									RecyclerView.Adapter adapter = commentView.getAdapter();
									if (adapter == null) {
										CommentAdapter comment = new CommentAdapter(getActivity(), getChildFragmentManager(), data, null);
										commentView.setAdapter(comment);
										return;
									}
									if (isRefresh) {
										((CommentAdapter) adapter).setData(data);
									} else {
										((CommentAdapter) adapter).addNewData(data);
										((CommentAdapter) adapter).stopLoading();
									}
								}
							});
							commentRefresh.setRefreshing(false);
						}
					} catch (JSONException e) {
						onFailed(e.toString());
					}
				}

				@Override
				public void onFailed(String cause) {
					Log.e("Network", cause);
					commentRefresh.setRefreshing(false);
				}
			});
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		} finally {
			request.release();
		}
	}
}

