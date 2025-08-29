package com.losthiro.ottohubclient;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.losthiro.ottohubclient.adapter.model.Comment;
import com.losthiro.ottohubclient.adapter.CommentAdapter;
import com.losthiro.ottohubclient.adapter.HonourAdapter;
import com.losthiro.ottohubclient.adapter.model.Video;
import com.losthiro.ottohubclient.adapter.VideoAdapter;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.impl.ClientString;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.impl.danmaku.ClientDanmakuParser;
import com.losthiro.ottohubclient.service.PlayerService;
import com.losthiro.ottohubclient.utils.DeviceUtils;
import com.losthiro.ottohubclient.utils.FileUtils;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import com.losthiro.ottohubclient.utils.StringUtils;
import com.losthiro.ottohubclient.utils.SystemUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.*;
import android.view.*;
import android.animation.*;
import com.losthiro.ottohubclient.view.*;
import android.content.res.*;
import java.util.*;
import android.text.*;
import com.losthiro.ottohubclient.adapter.*;
import android.widget.*;
import android.widget.SeekBar.*;
import android.widget.AdapterView.*;
import android.graphics.drawable.*;
import com.losthiro.ottohubclient.view.dialog.*;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.utils.*;
import cn.jzvd.*;

/**
 * @Author Hiro
 * @Date 2025/05/23 00:54
 */
public class PlayerActivity extends BasicActivity {
	public static final String TAG = "PlayerActivity";
	private static final Semaphore request = new Semaphore(1);
	private static final List<Comment> commentList = new ArrayList<>();
	private static final Handler mainHandler = new Handler(Looper.getMainLooper());
	private static final InfoParser parser = new InfoParser();
	public static EditText commentEdit;
	private static JzvdStd main;
	private static IDanmakuView danmaku;
	private Intent player;
	private VideoInfo current;
	private SwipeRefreshLayout commentRefresh;
	private RecyclerView commentView;
	private TextView likeCountView;
	private TextView favouriteView;
	private boolean isDanmakuOpen = true;
	private boolean isOnInfo = true;
	private boolean isFullScreen = false;
	private int videoPosition = 0;
	private int videoHeight;
	private int currentColor;
	private long firstBackTime;
	private DanmakuContext controller;
	private ClientDanmakuParser danmakuParser;
	private int currentSize;
	private String danmakuType;
	private EditText danmakuEdit;
	private EditText danmakuRender;
	private EditText pickerHex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		long id;
		Intent i = getIntent();
		if (savedInstanceState != null) {
			videoPosition = savedInstanceState.getInt("video_current");
			videoHeight = savedInstanceState.getInt("video_height");
		}
		Uri data = i.getData();
		try {
			String idStr = data.getQueryParameter("vid");
			if (idStr == null) {
				throw new Exception();
			}
			id = Long.parseLong(idStr);
			//parser.isLocal = false;
		} catch (Exception unuse) {
			id = i.getLongExtra("vid", -1);
			parser.isLocal = i.getBooleanExtra("is_local", false);
		}
		final Context c = getApplication();
		final Handler h = new Handler();
		final RecyclerView videoList = findViewById(R.id.video_detail_list);
		videoList.setLayoutManager(new GridLayoutManager(c, 1));
		final long vid = id;
		if (vid == -1) {
			return;
		}
		Bundle callback = new Bundle();
		callback.putParcelable("play_callback", getIntent());
		player = new Intent(this, PlayerService.class);
		player.putExtra("is_local", parser.isLocal);
		player.putExtra("play_callback", callback);
		if (parser.isLocal) {
			current = new VideoInfo(i.getStringExtra("root_path"), vid);
			loadUI(vid);
		} else {
			AccountManager manager = AccountManager.getInstance(this);
			String uri = manager.isLogin()
					? APIManager.VideoURI.getVideoDetail(manager.getAccount().getToken(), vid)
					: APIManager.VideoURI.getVideoDetail(vid);
			NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
				@Override
				public void onSuccess(String content) {
					if (content == null || content.isEmpty()) {
						return;
					}
					try {
						JSONObject root = new JSONObject(content);
						if (root == null) {
							return;
						}
						if (root.optString("status", "error").equals("success")) {
							current = new VideoInfo(root, vid);
							h.post(new Runnable() {
								@Override
								public void run() {
									loadUI(vid);
								}
							});
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailed(String cause) {
					Log.e("Network", cause);
				}
			});
			NetworkUtils.getNetwork.getNetworkJson(APIManager.VideoURI.getRandomVideoURI(12),
					new NetworkUtils.HTTPCallback() {
						@Override
						public void onSuccess(String content) {
							if (content == null || content.isEmpty()) {
								return;
							}
							try {
								JSONObject root = new JSONObject(content);
								if (root == null) {
									return;
								}
								if (root.optString("status", "error").equals("success")) {
									JSONArray video = root.optJSONArray("video_list");
									final List<Video> videos = new ArrayList<>();
									for (int i = 0; i < video.length(); i++) {
										videos.add(new Video(c, video.optJSONObject(i), Video.VIDEO_DETAIL));
									}
									h.post(new Runnable() {
										@Override
										public void run() {
											VideoAdapter adapter = new VideoAdapter(PlayerActivity.this, videos);
											videoList.setAdapter(adapter);
										}
									});
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onFailed(String cause) {
							Log.e("Network", cause);
						}
					});
		}
	}

	@Override
	protected void onPause() {
		player.putExtra("current_pos", main.getCurrentPositionWhenPlaying());
		startForegroundService(player);
		super.onPause();
		if (danmaku != null && danmaku.isPrepared()) {
			danmaku.pause();
		}
		if (main != null) {
			main.goOnPlayOnPause();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		//		if (main != null) {
		//			main.mediaInterface.seekTo(PlayerService.getCurrent());
		//			main.mediaInterface.start();
		//			stopService(player);
		//		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (danmaku != null && danmaku.isPrepared() && danmaku.isPaused()) {
			danmaku.resume();
		}
		if (main != null) {
			main.seekToManulPosition = PlayerService.getCurrent();
			main.goOnPlayOnResume();
			stopService(player);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (danmaku != null) {
			danmaku.release();
			danmaku = null;
		}
		main.releaseAllVideos();
		Intent last = Client.getLastActivity();
		if (last != null && Client.isFinishingLast(last)) {
			Client.removeActivity();
			startActivity(last);
		}
		stopService(player);
	}

	@Override
	public void onBackPressed() {
		if (main.backPress()) {
			return;
		}
		if (System.currentTimeMillis() - firstBackTime > 2000) {
			Toast.makeText(this, "再按一次返回键退出播放", Toast.LENGTH_SHORT).show();
			if (commentView != null) {
				CommentAdapter adapter = (CommentAdapter) commentView.getAdapter();
				if (adapter != null) {
					adapter.onBack(Comment.TYPE_VIDEO);
				}
			}
			firstBackTime = System.currentTimeMillis();
			return;
		}
		super.onBackPressed();
		if (danmaku != null) {
			danmaku.release();
			danmaku = null;
		}
		Intent last = Client.getLastActivity();
		if (last != null && Client.isFinishingLast(last)) {
			Client.removeActivity();
			startActivity(last);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("video_current", videoPosition);
		outState.putInt("video_height", videoHeight);
	}

	private void loadUI(final long vid) {
		JSONObject mainfest = null;
		try {
			mainfest = current.getInfos(this);
		} catch (JSONException e) {
			Log.i(TAG, "play using network mode");
		}
		String userIntro = parser.isLocal ? mainfest.optString("user_intro", "大家好啊，我是电棍") : current.getUserIntro();
		String type = parser.isLocal ? mainfest.optString("type", "其他") : current.getType();
        String[] data = new String[3];
        if(!parser.isLocal){
            data = current.getDataCount();
        }
		parser.vid = vid;
        parser.uid = parser.isLocal ? mainfest.optLong("uid", -1) : current.getUID();
		parser.title = parser.isLocal ? mainfest.optString("title", "大家好啊，今天来点大家想看的东西") : current.getTitle();
		parser.time = parser.isLocal ? mainfest.optString("time", "2009-04-09 00:00:00") : current.getTime();
		parser.name = parser.isLocal ? mainfest.optString("user_name", "棍母") : current.getUserName();
		parser.view = parser.isLocal ? mainfest.optString("view_count", "0播放") : data[0];
		parser.like = parser.isLocal ? mainfest.optString("like_count", "0获赞") : data[1];
		parser.favourite = parser.isLocal ? mainfest.optString("favorite_count", "0冷藏") : data[2];
		player.putExtra("video_source", current.getVideo());
		int color = ResourceUtils.getColor(R.color.colorSecondary);
		String info = StringUtils.strCat(new Object[]{parser.time, " - ", parser.view, " - ", type, " - OV", vid});
		final String intro = parser.isLocal ? mainfest.optString("intro", "打野的走位我就觉得你妈逼离谱") : current.getIntro();
		((TextView) findViewById(R.id.video_detail_info)).setText(info);
		final ClientString newContent = new ClientString(intro);
		TextView introText = findViewById(R.id.video_detail_intro);
		newContent.load(introText, true);
		introText.setOnClickListener(new OnClickListener() {
			private boolean isOpen;

			@Override
			public void onClick(View v) {
				isOpen = !isOpen;
				newContent.load((TextView) v, isOpen);
			}
		});
		((TextView) findViewById(R.id.video_detail_title)).setText(parser.title);
		likeCountView = findViewById(R.id.video_like_count);
		likeCountView.setText(parser.like);
		favouriteView = findViewById(R.id.video_favorite_count);
		favouriteView.setText(parser.favourite);
		((TextView) findViewById(R.id.videos_detail_user_name)).setText(parser.name);
		((TextView) findViewById(R.id.video_detail_user_intro)).setText(userIntro);
		((ImageButton) findViewById(android.R.id.content).findViewWithTag("1")).setColorFilter(color);
		((ImageButton) findViewById(android.R.id.content).findViewWithTag("2")).setColorFilter(color);
		List<String> tagList = parser.isLocal
				? getLocalTags(mainfest.optJSONArray("tags"))
				: Arrays.asList(current.getTags());
		if (tagList.size() > 0) {
			HonourAdapter adapter = new HonourAdapter(this, tagList);
			adapter.setUsingSearch(true);
			RecyclerView tags = findViewById(R.id.video_detail_tags);
			tags.setVisibility(View.VISIBLE);
			tags.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
			tags.setAdapter(adapter);
		}
		ImageView user = findViewById(R.id.video_detail_user_icon);
		commentView = findViewById(R.id.video_comment_list);
		commentRefresh = findViewById(R.id.video_refresh);
		if (parser.isLocal) {
			user.setImageBitmap(BitmapFactory.decodeFile(current.getAvatar()));
		} else {
			ImageDownloader.loader(user, current.getAvatar());
			commentView.setLayoutManager(new GridLayoutManager(this, 1));
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
						if (lastPos >= itemCount - 1 && itemCount >= 12) {
							loadComment(vid, false);
						}
					}
				}
			});
			commentRefresh.setVisibility(View.GONE);
			commentRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					Toast.makeText(getApplication(), "正在刷新", Toast.LENGTH_SHORT).show();
					loadComment(vid, true);
				}
			});
			ImageButton likeBtn = findViewById(R.id.video_like_btn);
			likeBtn.setColorFilter(current.isLike()
					? ResourceUtils.getColor(R.color.colorAccent)
					: ResourceUtils.getColor(R.color.colorSecondary));
			likeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					likeCurrent(v);
				}
			});
			ImageButton favouriteBtn = findViewById(R.id.video_favorite_btn);
			favouriteBtn.setColorFilter(current.isFavorite()
					? ResourceUtils.getColor(R.color.colorAccent)
					: ResourceUtils.getColor(R.color.colorSecondary));
			favouriteBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					favouriteCurrent(v);
				}
			});
			Button followingBtn = findViewById(R.id.video_detail_following_user);
			followingBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					followingUser(v);
				}
			});
			setFollowingStatus(followingBtn, parser.uid);
			loadComment(vid, true);
		}
		final ScrollView detailView = findViewById(R.id.video_detail_view);
		final Button infoBtn = findViewById(R.id.video_detail_info_btn);
		final Button commentBtn = findViewById(R.id.video_detail_comment_btn);
		if (parser.isLocal) {
			commentBtn.setVisibility(View.GONE);
			infoBtn.setVisibility(View.GONE);
		}
		commentBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isOnInfo) {
					((Button) v).setTextColor(isOnInfo
							? ResourceUtils.getColor(R.color.colorAccent)
							: ResourceUtils.getColor(R.color.text_color));
					infoBtn.setTextColor(isOnInfo
							? ResourceUtils.getColor(R.color.text_color)
							: ResourceUtils.getColor(R.color.colorAccent));
					detailView.setVisibility(View.GONE);
					commentRefresh.setVisibility(View.VISIBLE);
					View parent = (View) commentEdit.getParent();
					parent.setVisibility(View.VISIBLE);
					isOnInfo = !isOnInfo;
				}
			}
		});
		infoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isOnInfo) {
					return;
				}
				((Button) v).setTextColor(isOnInfo
						? ResourceUtils.getColor(R.color.text_color)
						: ResourceUtils.getColor(R.color.colorAccent));
				commentBtn.setTextColor(isOnInfo
						? ResourceUtils.getColor(R.color.colorAccent)
						: ResourceUtils.getColor(R.color.text_color));
				detailView.setVisibility(View.VISIBLE);
				commentRefresh.setVisibility(View.GONE);
				View parent = (View) commentEdit.getParent();
				parent.setVisibility(View.GONE);
				isOnInfo = !isOnInfo;
			}
		});
		commentEdit = findViewById(R.id.video_comment_edit);
		View parent = (View) commentEdit.getParent();
		parent.setVisibility(isOnInfo ? View.GONE : View.VISIBLE);
		danmakuEdit = findViewById(R.id.video_danmaku_edit);
		danmaku = findViewById(R.id.main_danmaku);
		danmaku.setCallback(new DrawHandler.Callback() {
			@Override
			public void prepared() {
				danmaku.start();
			}

			@Override
			public void updateTimer(DanmakuTimer timer) {
			}

			@Override
			public void danmakuShown(BaseDanmaku danmaku) {
			}

			@Override
			public void drawingFinished() {
			}
		});
		main = findViewById(R.id.main_video_view);
		main.setUp(current.getVideo(), parser.title);
		ImageDownloader.loader(main.posterImageView, current.getCover());
		main.post(new Runnable() {
			@Override
			public void run() {
				View parent = (View) main.getParent();
				if (parent != null) {
					videoHeight = main.getHeight();
					ViewGroup.LayoutParams params = parent.getLayoutParams();
					params.height = videoHeight;
					parent.setLayoutParams(params);
				}
				loadDanmaku(vid);
			}
		});
	}

	private static List<String> getLocalTags(JSONArray data) {
		int max = data.length();
		List<String> list = new ArrayList<>(max);
		for (int i = 0; i < max; i++) {
			list.add(data.optString(i, "棍母"));
		}
		return list;
	}

	private void loadDanmaku(long vid) {
		HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
		maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_LR, 5);
		HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
		overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_LR, true);
		overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
		controller = DanmakuContext.create();
		controller.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3);
		controller.setDuplicateMergingEnabled(false);
		controller.setScrollSpeedFactor(1.2f);
		controller.setScaleTextSize(1.2f);
		controller.setMaximumLines(maxLinesPair);
		controller.preventOverlapping(overlappingEnablePair);
		controller.setDanmakuMargin(40);
		if (parser.isLocal) {
			try {
				JSONArray array = current.getDanmakuList(this);
				danmaku.prepare(new ClientDanmakuParser(array), controller);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			NetworkUtils.getNetwork.getNetworkJson(APIManager.DanmakuURI.getListURI(vid),
					new NetworkUtils.HTTPCallback() {
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
								String status = root.optString("status", "error");
								if (status.equals("error")) {
									onFailed(root.optString("message", "error 404 gunmu"));
									return;
								}
								mainHandler.post(new Runnable() {
									@Override
									public void run() {
										danmakuParser = new ClientDanmakuParser(root.optJSONArray("data"));
										danmaku.prepare(danmakuParser, controller);
									}
								});
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
		main.startVideo();
	}

	public static void setFollowingStatus(final Button followingBtn, long uid) {
		AccountManager manager = AccountManager.getInstance(followingBtn.getContext());
		if (!manager.isLogin()) {
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(
				APIManager.FollowingURI.getFollowStatusURI(uid, manager.getAccount().getToken()),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
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
								String status = root.optString("follow_status", null);
								final int following = status == null
										? root.optInt("follow_status", 0)
										: Integer.parseInt(status);
								mainHandler.post(new Runnable() {
									@Override
									public void run() {
										if (following == 0) {
											followingBtn.setText("自恋");
											return;
										}
										if (following == 2) {
											followingBtn.setText("取消灌注");
											return;
										}
										if (following == 3) {
											followingBtn.setText("回关粉丝");
											return;
										}
										if (following == 4) {
											followingBtn.setText("分手");
											return;
										}
									}
								});
							}
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

	private void likeCurrent(final View v) {
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			Toast.makeText(getApplication(), "没登录喜欢牛魔", Toast.LENGTH_SHORT).show();
			return;
		}
		long vid = current.getVID();
		String token = manager.getAccount().getToken();
		final Handler h = new Handler(getMainLooper());
		NetworkUtils.getNetwork.getNetworkJson(APIManager.EngagementURI.getLikeVideoURI(vid, token),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
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
								final boolean isLike = root.optInt("if_like", 0) == 1;
								String count = root.optString("like_count", null);
								final int likeCount = count == null
										? root.optInt("like_count", -1)
										: Integer.parseInt(count);
								h.post(new Runnable() {
									@Override
									public void run() {
										((ImageButton) v).setColorFilter(isLike
												? ResourceUtils.getColor(R.color.colorAccent)
												: ResourceUtils.getColor(R.color.colorSecondary));
										likeCountView.setText(likeCount + "获赞");
										String msg = isLike ? "点赞成功" : "取消点赞成功";
										Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
									}
								});
							}
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

	private void favouriteCurrent(final View v) {
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			Toast.makeText(getApplication(), "没登录点牛魔", Toast.LENGTH_SHORT).show();
			return;
		}
		long vid = current.getVID();
		String token = manager.getAccount().getToken();
		final Handler h = new Handler(getMainLooper());
		NetworkUtils.getNetwork.getNetworkJson(APIManager.EngagementURI.getFavouriteVideoURI(vid, token),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
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
								final boolean isFav = root.optInt("if_favorite", 0) == 1;
								String count = root.optString("like_favorite", null);
								final int fCount = count == null
										? root.optInt("like_favorite", -1)
										: Integer.parseInt(count);
								h.post(new Runnable() {
									@Override
									public void run() {
										((ImageButton) v).setColorFilter(isFav
												? ResourceUtils.getColor(R.color.colorAccent)
												: ResourceUtils.getColor(R.color.colorSecondary));
										favouriteView.setText(fCount + "冷藏");
										String msg = isFav ? "冷藏成功" : "取消冷藏成功";
										Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
									}
								});
							}
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

	private void loadComment(final long vid, final boolean isRefresh) {
		try {
			if (!request.tryAcquire()) {
				return;
			}
			if (isRefresh) {
				commentList.clear();
			}
			AccountManager manager = AccountManager.getInstance(this);
			String uri = manager.isLogin()
					? APIManager.CommentURI.getVideoCommentURI(vid, 0, manager.getAccount().getToken(), 0, 12)
					: APIManager.CommentURI.getVideoCommentURI(vid, 0, 0, 12);
			final Handler h = new Handler(Looper.getMainLooper());
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
								Comment currentComment = new Comment(getApplication(), array.optJSONObject(i), vid,
										Comment.TYPE_VIDEO);
								CommentAdapter adapter = (CommentAdapter) commentView.getAdapter();
								if (adapter == null || !adapter.isCommentExists(currentComment)) {
									data.add(currentComment);
								}
							}
							h.post(new Runnable() {
								@Override
								public void run() {
									if (commentList.isEmpty()) {
										commentList.addAll(data);
										commentView
												.setAdapter(new CommentAdapter(PlayerActivity.this, commentList, true));
										return;
									}
									if (isRefresh) {
										commentList.addAll(data);
										commentView.getAdapter().notifyDataSetChanged();
									} else {
										((CommentAdapter) commentView.getAdapter()).addNewData(data);
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

	public void switchDanmaku(View v) {
		if (danmaku != null) {
			if (isDanmakuOpen) {
				((Button) v).setTextColor(Color.GRAY);
				danmaku.setVisibility(View.GONE);
				isDanmakuOpen = false;
				return;
			}
			((Button) v).setTextColor(ResourceUtils.getColor(R.color.colorAccent));
			danmaku.setVisibility(View.VISIBLE);
			isDanmakuOpen = true;
		}
	}

	private void followingUser(final View v) {
		following(parser.uid, v);
	}

	public static void following(long uid, final View v) {
		AccountManager manager = AccountManager.getInstance(v.getContext());
		if (!manager.isLogin()) {
			Toast.makeText(v.getContext(), "那我缺的登录这一块", Toast.LENGTH_SHORT).show();
			return;
		}
		final Handler mainHandler = new Handler(Looper.getMainLooper());
		NetworkUtils.getNetwork.getNetworkJson(
				APIManager.FollowingURI.getFollowURI(uid, manager.getAccount().getToken()),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
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
								String status = root.optString("follow_status", null);
								final int following = status == null
										? root.optInt("follow_status", 0)
										: Integer.parseInt(status);
								mainHandler.post(new Runnable() {
									@Override
									public void run() {
										Button btn = (Button) v;
										switch (following) {
											case 0 :
												Toast.makeText(v.getContext(), "你太自恋了一边玩去", Toast.LENGTH_SHORT).show();
												break;
											case 1 :
												Toast.makeText(v.getContext(), "取关成功", Toast.LENGTH_SHORT).show();
												btn.setText("灌注主播");
												break;
											case 2 :
												Toast.makeText(v.getContext(), "成功把主播灌成奶油泡芙了", Toast.LENGTH_SHORT)
														.show();
												btn.setText("取消灌注");
												break;
											case 3 :
												Toast.makeText(v.getContext(), "分手成功", Toast.LENGTH_SHORT).show();
												btn.setText("回关粉丝");
												break;
											case 4 :
												Toast.makeText(v.getContext(), "结婚成功♡", Toast.LENGTH_SHORT).show();
												btn.setText("分手");
												break;
										}
									}
								});
							}
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

	public void userDetail(View v) {
		if (parser.isLocal) {
			Toast.makeText(getApplication(), "离线视频不能进用户主页，抱歉我偷工减料了^_^", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent i = new Intent(PlayerActivity.this, AccountDetailActivity.class);
		i.putExtra("uid", parser.uid);
		Client.saveActivity(getIntent());
		startActivity(i);
	}

	public void sendDialog(View v) {
		if (!AccountManager.getInstance(this).isLogin()) {
			Toast.makeText(getApplication(), "那我缺的登录这一块", Toast.LENGTH_SHORT).show();
			return;
		}
		if (danmakuEdit.getText().toString().isEmpty()) {
			Toast.makeText(getApplication(), "不能发送棍母消息", Toast.LENGTH_SHORT).show();
			return;
		}
		final View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_color_picker, null);
		BottomDialog loginDialog = new BottomDialog(this, inflate);
		SeekBar sizeProgress = inflate.findViewWithTag("danmaku_size");
		sizeProgress.setMin(10);
		sizeProgress.setMax(50);
		sizeProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO: Implement this method
				if (fromUser) {
					((TextView) inflate.findViewWithTag("size_current")).setText(progress + "px");
					currentSize = progress;
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO: Implement this method
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO: Implement this method
			}
		});
		main.mediaInterface.pause();
		danmakuRender = inflate.findViewWithTag("danmaku_render");
		pickerHex = inflate.findViewWithTag("current_color");
		final ClientColorPicker danmakuPicker = inflate.findViewWithTag("color_picker_view");
		danmakuPicker.setColor(currentColor, true);
		danmakuPicker.setOnColorChangedListener(new ClientColorPicker.OnColorChangedListener() {
			@Override
			public void onColorChanged(int color) {
				// TODO: Implement this method
				String rgb = StringUtils.convertToRGB(color).toUpperCase(Locale.getDefault());
				pickerHex.setText(StringUtils.strCat("#", rgb));
				pickerHex.setTextColor(color);
				currentColor = color;
			}
		});
		pickerHex.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// TODO: Implement this method
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO: Implement this method
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO: Implement this method
				try {
					int color = Color.parseColor(s.toString());
					danmakuPicker.setColor(color);
					pickerHex.setTextColor(color);
				} catch (Exception e) {
					return;
				}
			}
		});
		((TextView) inflate.findViewWithTag("danmaku_content")).setText(danmakuEdit.getText());
		loginDialog.show();
		int color = danmakuPicker.getColor();
		String rgb = StringUtils.convertToRGB(color).toUpperCase(Locale.getDefault());
		pickerHex.setText(StringUtils.strCat("#", rgb));
		pickerHex.setTextColor(color);
		Toast.makeText(getApplication(), "color=" + currentColor, Toast.LENGTH_SHORT).show();
	}

	public void switchType(final View v) {
		final HashMap<String, String> map = new HashMap<>();
		map.put("滚动", "scroll");
		map.put("底部", "bottom");
		map.put("顶部", "top");
		List<String> names = new ArrayList<>();
		for (String name : map.keySet()) {
			names.add(name);
		}
		ArrayAdapter<String> name = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
		ListView view = new ListView(this);
		final PopupWindow window = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, true);
		window.setTouchable(true);
		window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		view.setLayoutParams(
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		view.setAdapter(name);
		view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO: Implement this method
				String current = (String) parent.getItemAtPosition(position);
				((Button) v).setText(current);
				danmakuType = map.get(current);
				window.dismiss();
			}
		});
		window.showAsDropDown(v, 0, view.getHeight());
	}

	public void sendDanmaku(View v) {
		Account current = AccountManager.getInstance(this).getAccount();
		if (current == null) {
			return;
		}
		final double pos = main.mediaInterface.getCurrentPosition() / 1000;
		String rgb = StringUtils.convertToRGB(currentColor).toUpperCase(Locale.getDefault());
		final String danmakuContent = danmakuEdit.getText().toString();
		final String render = danmakuRender.getText().toString();
		String uri = APIManager.DanmakuURI.getSendURI(parser.vid, current.getToken(), danmakuContent, pos, danmakuType,
				StringUtils.strCat("#", rgb), currentSize + "px", render);
		NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(String content) {
				// TODO: Implement this method
				try {
					JSONObject json = new JSONObject(content);
					if (json.optString("status", "error").equals("success")) {
						int type = BaseDanmaku.TYPE_SCROLL_LR;
						if (danmakuType.equals("bottom")) {
							type = BaseDanmaku.TYPE_FIX_BOTTOM;
						}
						if (danmakuType.equals("top")) {
							type = BaseDanmaku.TYPE_FIX_TOP;
						}
						BaseDanmaku data = controller.mDanmakuFactory.createDanmaku(type);
						if (data == null || danmaku == null) {
							return;
						}
						data.text = danmakuContent;
						data.padding = 5;
						data.priority = 1;
						data.setTime((long) pos);
						data.textSize = currentSize * (danmakuParser.getDisplayer().getDensity() - 0.6f);
						data.textColor = currentColor;
						data.textShadowColor = currentColor <= Color.BLACK ? Color.WHITE : Color.BLACK;
						danmaku.addDanmaku(data);
						main.mediaInterface.start();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO: Implement this method
								Toast.makeText(getApplication(), "发送成功", Toast.LENGTH_SHORT).show();
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

	private void sendComment(long parent) {
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			Toast.makeText(this, "没登录发牛魔", Toast.LENGTH_SHORT).show();
			return;
		}
		String content = commentEdit.getText().toString();
		if (content.isEmpty()) {
			Toast.makeText(this, "你发的是棍母", Toast.LENGTH_SHORT).show();
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(APIManager.CommentURI.getCommentVideoURI(current.getVID(), parent,
				manager.getAccount().getToken(), content), new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						if (content.isEmpty() || content == null) {
							onFailed("empty content");
							return;
						}
						try {
							final JSONObject root = new JSONObject(content);
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									commentRefresh.setRefreshing(true);
									String status = root.optString("status", "error");
									if (status.equals("success")) {
										int callback = root.optInt("if_get_experience", 0);
										String msg = "评论发送成功~";
										if (callback == 1) {
											msg = msg + "经验+3";
										}
										Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
										commentEdit.setText("");
										loadComment(current.getVID(), true);
										return;
									}
									String message = root.optString("message", "error");
									if (message.equals("content_too_long")) {
										Toast.makeText(getApplication(), "不许你发小作文", Toast.LENGTH_SHORT).show();
									}
									if (message.equals("content_too_short")) {
										Toast.makeText(getApplication(), "才发两三个字是什么意思啊", Toast.LENGTH_SHORT).show();
									}
									if (message.equals("error_parent")) {
										Toast.makeText(getApplication(), "这个嘛...目前还没有楼中楼中楼功能哦", Toast.LENGTH_SHORT)
												.show();
									}
									if (message.equals("warn")) {
										Toast.makeText(getApplication(), "冰不许爆(把你违禁词删了)", Toast.LENGTH_SHORT).show();
									}
									onFailed(message);
								}
							});
						} catch (JSONException e) {
							onFailed(e.toString());
						}
					}

					@Override
					public void onFailed(final String cause) {
						Log.e("Network", cause);
					}
				});
	}

	public void sendComment(View v) {
		CommentAdapter adapter = (CommentAdapter) commentView.getAdapter();
		if (adapter == null) {
			return;
		}
		Comment c = adapter.getCurrent();
		long parent = c == null ? 0 : c.getCID();
		sendComment(parent);
	}

	public void downloadVideo(View v) {
		if (parser.isLocal) {
			Toast.makeText(getApplication(), "非网络视频不能下载到本地(｡･ω･｡)", Toast.LENGTH_SHORT).show();
			return;
		}
		String destDir = FileUtils.getStorage(this, PATH_SAVE);
		if (!new File(destDir).exists()) {
			FileUtils.createDir(destDir);
		}
		String randomName = StringUtils.strCat(new Object[]{"OV", current.getVID(), "-",
				SystemUtils.getDate("yyyy-MM-dd-HH-mm-ss-"), SystemUtils.getTime()});
		String realDir = StringUtils.strCat(destDir, randomName);
		final File dir = new File(realDir);
		if (!dir.exists()) {
			FileUtils.createDir(realDir);
		}
		try {
			Toast.makeText(getApplication(), "正在保存请稍候", Toast.LENGTH_SHORT).show();
			saveMainfest(dir);
			NetworkUtils.getNetwork.getNetworkJson(APIManager.DanmakuURI.getListURI(current.getVID()),
					new NetworkUtils.HTTPCallback() {
						@Override
						public void onSuccess(final String content) {
							if (content == null || content.isEmpty()) {
								onFailed("empty content");
								return;
							}
							try {
								JSONObject root = new JSONObject(content);
								if (root == null) {
									onFailed("null json");
									return;
								}
								JSONArray danmakuData = root.optJSONArray("data");
								String name = "danmaku_config.json";
								File f = new File(dir, name);
								if (!f.exists()) {
									FileUtils.createFile(getApplication(), f.getPath(), danmakuData.toString(4));
									return;
								}
								FileUtils.writeFile(getApplication(), f.getPath(), danmakuData.toString(4));
							} catch (JSONException e) {
								onFailed(e.toString());
							}
						}

						@Override
						public void onFailed(String cause) {
							Log.e("Network", cause);
						}
					});
			Runnable callback = new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplication(), "视频保存成功~冲刺冲刺", Toast.LENGTH_SHORT).show();
				}
			};
			String avatar = current.getAvatar();
			String cover = current.getCover();
			String video = current.getVideo();
			NetworkUtils.getNetwork.download(avatar, new File(dir, "user_avatar").getPath());
			NetworkUtils.getNetwork.download(cover, new File(dir, "cover").getPath());
			NetworkUtils.getNetwork.download(video, new File(dir, "video").getPath(), callback);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	private void saveMainfest(File dir) throws Exception {
		JSONObject mainData = new JSONObject();
		mainData.put("vid", current.getVID());
		mainData.put("uid", parser.uid);
		mainData.put("title", parser.title);
		mainData.put("user_name", parser.name);
		mainData.put("intro", current.getIntro());
		mainData.put("user_intro", current.getUserIntro());
		mainData.put("type", current.getType());
		mainData.put("time", parser.time);
		mainData.put("category", current.getCategory());
		mainData.put("view_count", parser.view);
		mainData.put("like_count", parser.like);
		mainData.put("favorite_count", parser.favourite);
		String[] tagsData = current.getTags();
		JSONArray tags = new JSONArray(tagsData);
		mainData.put("tags", tags);
		String name = "mainfest.json";
		String fPath = StringUtils.strCat(new String[]{dir.toString(), File.separator, name});
		if (!new File(dir, name).exists()) {
			FileUtils.createFile(this, fPath, mainData.toString(4));
			return;
		}
		FileUtils.writeFile(this, fPath, mainData.toString(4));
	}

	public void reportVideo(View v) {
		if (parser.isLocal) {
			return;
		}
		AlertDialog dialog = new AlertDialog.Builder(this).setTitle("确认举报？").setMessage("OV" + current.getVID())
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dia, int which) {
						reportCurrent();
					}
				}).setNegativeButton(android.R.string.cancel, null).create();
		dialog.show();
	}

	private void reportCurrent() {
		try {
			if (!request.tryAcquire()) {
				return;
			}
			String token = AccountManager.getInstance(this).getAccount().getToken();
			NetworkUtils.getNetwork.getNetworkJson(APIManager.ModerationURI.getReportVideoURI(current.getVID(), token),
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
									Toast.makeText(getApplication(), "举报成功", Toast.LENGTH_SHORT).show();
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
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		} finally {
			request.release();
		}
	}

	private static class InfoParser {
		long uid;
		long vid;
		boolean isLocal;
		String title;
		String time;
		String name;
		String view;
		String like;
		String favourite;
	}

	private class VideoInfo {
		private long id;
		private boolean isLocal;
		private JSONObject main;
		private String local;

		private VideoInfo(JSONObject root, long vid) {
			id = vid;
			main = root;
			isLocal = false;
		}

		private VideoInfo(String rootPath, long vid) {
			id = vid;
			local = rootPath;
			isLocal = true;
		}

		public long getVID() {
			return id;
		}
        
        public long getUID() {
            String strID = main.optString("uid");
            return strID.isEmpty() ? main.optLong("uid", -1) : Long.parseLong(strID);
        }

		public boolean isLocal() {
			return isLocal;
		}
        
        public String getTitle(){
            return main.optString("title", "大家好啊，今天来点大家想看的东西");
        }

		public String getIntro() {
			return main.optString("intro", "大家好啊，我是电棍");
		}

		public String getType() {
			String stringType = main.optString("type");
			int type = stringType.isEmpty() ? main.optInt("type", -1) : Integer.parseInt(stringType);
			switch (type) {
				case 1 :
					return "转载 - 侵权必删";
				case 2 :
					return "自制";
			}
			return "其他";
		}
        
        public String getTime(){
            String format = "yyyy-MM-dd HH:mm:ss";
            String def = SystemUtils.getDate(format);
            if (main == null) {
                return def;
            }
            String video = main.optString("time", def);
            long time = SystemUtils.getTime() - SystemUtils.getTime(video, format);
            if (time >= 0 && time <= 999) {
                return "刚刚发布";
            }
            if (time > 1000 && time <= 60000) {
                return time / 1000 + "秒前";
            }
            if (time > 60000 && time <= 3600000) {
                return time / 60000 + "分钟前";
            }
            if (time > 3600000 && time <= 216000000) {
                return time / 3600000 + "小时前";
            }
            if (time > 216000000 && time < 6048000000L) {
                return time / 216000000 + "天前";
            }
            return video;
        }

		public String getCategory() {
			String stringCat = main.optString("category");
			int category = stringCat.isEmpty() ? main.optInt("type", -1) : Integer.parseInt(stringCat);
			switch (category) {
				case APIManager.VideoURI.CATEGORY_OTHER :
					return "其他";
				case APIManager.VideoURI.CATEGORY_FUN :
					return "鬼畜";
				case APIManager.VideoURI.CATEGORY_MAD :
					return "音MAD";
				case APIManager.VideoURI.CATEGORY_VOCALOID :
					return "人力VOCALOID";
				case APIManager.VideoURI.CATEGORY_THEATER :
					return "剧场";
				case APIManager.VideoURI.CATEGORY_GAME :
					return "游戏";
				case APIManager.VideoURI.CATEGORY_OLD :
					return "怀旧";
			}
			return "棍母";
		}

		public String[] getTags() {
			return main.optString("tag", "#棍母").split("#");
		}
        
        public String getUserName(){
            return main.optString("username", "棍母");
        }

		public String getUserIntro() {
			return main.optString("userintro", "我是电棍");
		}
        
        public String[] getDataCount() {
            String[] data = new String[3];
            data[0] = StringUtils.strCat(getCount(main.optString("view_count")), "播放");
            data[1] = StringUtils.strCat(getCount(main.optString("like_count")), "获赞");
            data[2] = StringUtils.strCat(getCount(main.optString("favorite_count")), "冷藏");
            return data;
        }

		public boolean isLike() {
			return main.optInt("if_like", 0) != 0;
		}

		public boolean isFavorite() {
			return main.optInt("if_favorite", 0) != 0;
		}

		public JSONObject getInfos(Context c) throws JSONException {
			File danmaku = new File(local, "mainfest.json");
			return new JSONObject(FileUtils.readFile(c, danmaku.getPath()));
		}

		public JSONArray getDanmakuList(Context c) throws JSONException {
			File danmaku = new File(local, "danmaku_config.json");
			return new JSONArray(FileUtils.readFile(c, danmaku.getPath()));
		}

		public String getVideo() {
			return isLocal ? new File(local, "video").getPath() : main.optString("video_url", null);
		}

		public String getCover() {
			return isLocal ? new File(local, "cover").getPath() : main.optString("cover_url", null);
		}

		public String getAvatar() {
			return isLocal ? new File(local, "user_avatar").getPath() : main.optString("avatar_url", null);
		}
        
        private String getCount(String strCount){
            String def = StringUtils.toStr(0);
            if (main == null || strCount.isEmpty()) {
                return def;
            }
            long count = Long.parseLong(strCount);
            if (count >= 1000) {
                return count / 1000 + "k";
            }
            if (count >= 10000) {
                return count / 10000 + "w";
            }
            return strCount;
        }
	}
}

