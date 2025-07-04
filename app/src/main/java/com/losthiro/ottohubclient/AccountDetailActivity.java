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
import com.losthiro.ottohubclient.adapter.Blog;
import com.losthiro.ottohubclient.adapter.BlogAdapter;
import com.losthiro.ottohubclient.adapter.HonourAdapter;
import com.losthiro.ottohubclient.adapter.Video;
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

/**
 * @Author Hiro
 * @Date 2025/06/01 14:54
 */
public class AccountDetailActivity extends MainActivity {
	public static final String TAG = "AccountDetailActivity";
	private static final Handler mainHandler = new Handler(Looper.getMainLooper());
	private static final Semaphore request = new Semaphore(1);
	private final List<Video> videos = new ArrayList<>();
	private final List<Blog> blogs = new ArrayList<>();
	private static int categoryIndex;
	private UserInfo current;
	private VideoAdapter video;
	private BlogAdapter blog;
	private LinearLayout onloadView;
	private LinearLayout favoriteCategory;
	private SwipeRefreshLayout refresh;
	private RecyclerView view;
	private RecyclerView blogView;
	private RecyclerView videoView;
	private ImageView blogStatus;
	private ImageView videoStatus;
	private boolean blogShow;
	private boolean videoShow;
	private long uid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_detail);
		Intent i = getIntent();
		uid = i.getLongExtra("uid", 0);
		NetworkUtils.getNetwork.getNetworkJson(APIManager.UserURI.getUserDetail(uid), new NetworkUtils.HTTPCallback() {
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
						current = new UserInfo(json, uid);
						mainHandler.post(new Runnable() {
							@Override
							public void run() {
								initUI();
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

	@Override
	protected void onDestroy() {
		// TODO: Implement this method
		Intent last = Client.getLastActivity();
		if (last != null && Client.isFinishingLast(last)) {
			Client.removeActivity();
			startActivity(last);
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
		if (resultCode == RESULT_OK && data != null) {
			Uri uri = data.getData();
			Account current = AccountManager.getInstance(this).getAccount();
			if (requestCode == IMAGE_REQUEST_CODE) {
				new ImageUploader(this, "https://api.ottohub.cn/module/creator/update_avatar.php",
						APIManager.CreatorURI.getUpdateAvatarURI(current.getToken()), new NetworkUtils.HTTPCallback() {
							@Override
							public void onSuccess(String content) {
								// TODO: Implement this method
								try {
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
								String msg = null;
								switch (cause) {
									case "error_file" :
										msg = "文件格式错误，请正确选择图片";
										break;
									case "file_not_found" :
										msg = "没有选择图片文件";
										break;
									case "too_big_file" :
										msg = "文件太大啦~请选择1MB以内大小的图片";
										break;
								}
								if (msg != null) {
									Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
								}
							}
						}).execute(uri);
			}
		}
	}

	private void initUI() {
		View content = findViewById(android.R.id.content);
		ImageDownloader.loader((ImageView) findViewById(R.id.main_user_avatar), current.getAvatarURI());
		ImageDownloader.loader((ImageView) findViewById(R.id.main_user_cover), current.getCoverURI());
		((TextView) findViewById(R.id.user_name)).setText(StringUtils.strCat(current.getUser(), "的主页"));
		((TextView) findViewById(R.id.main_user_name)).setText(current.getUser());
        int now = current.getExp();
        int max = 49;
        if (now >= 50 && now < 499) {
            max = 499;
        } else if (now >= 500 && now < 999) {
            max = 999;
        } else if (now >= 1000 && now < 2999) {
            max = 2999;
        } else if (now >= 3000 && now < 7999) {
            max = 7999;
        } else if (now >= 8000 && now < 14999) {
            max = 14999;
        } else if (now >= 15000 && now < 29999) {
            max = 29999;
        } else if (now >= 30000 && now < 79999) {
            max = 79999;
        } else if (now >= 80000){
            max = 80000;
        }
        ((TextView) findViewById(R.id.account_exp_text)).setText(StringUtils.strCat(new Object[]{"EXP: ", now, File.separator, max}));
        ProgressBar exp = findViewById(R.id.account_exp_progress);
        exp.setMax(max);
        exp.setProgress(now);
        Drawable bgExp=exp.getProgressDrawable();
		TextView levelText = findViewById(R.id.main_user_level);
		HashMap<String, Integer> levelMap = current.getLevel();
		GradientDrawable bg = new GradientDrawable();
		bg.setCornerRadius(8f);
		for (HashMap.Entry<String, Integer> entry : levelMap.entrySet()) {
			levelText.setText(entry.getKey());
			bg.setColor(entry.getValue());
            bgExp.setColorFilter(entry.getValue(), PorterDuff.Mode.SRC_IN);
		}
		levelText.setBackgroundDrawable(bg);
        exp.setProgressDrawable(bgExp);
		String userInfo = StringUtils.strCat(
				new Object[]{"UID: ", current.getID(), " - 性别: ", current.getSex(), " - 注册日: ", current.getTime()});
		((TextView) findViewById(R.id.main_user_detail)).setText(userInfo);
		((TextView) findViewById(R.id.main_user_intro)).setText(current.getIntro());
		HonourAdapter adapter = new HonourAdapter(this, Arrays.asList(current.getHonours()));
		adapter.setHiddenDef(false);
		RecyclerView honourList = findViewById(R.id.main_user_honours);
		honourList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		honourList.setAdapter(adapter);
		String videoCount = StringUtils.strCat(new Object[]{current.getVideoCount(), System.lineSeparator(), "视频"});
		String blogCount = StringUtils.strCat(new Object[]{current.getBlogCount(), System.lineSeparator(), "动态"});
		String followingCount = StringUtils
				.strCat(new Object[]{current.getFollowingCount(), System.lineSeparator(), "关注"});
		String fansCount = StringUtils.strCat(new Object[]{current.getFansCount(), System.lineSeparator(), "粉丝"});
		TextView suscribeView = findViewById(R.id.main_user_following_count);
		suscribeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				Intent i = new Intent(AccountDetailActivity.this, SuscribeActivity.class);
				i.putExtra("uid", current.getID());
				i.putExtra("sus_count", current.getFollowingCount());
				Client.saveActivity(getIntent());
				startActivity(i);
			}
		});
		TextView fansView = findViewById(R.id.main_user_fans_count);
		fansView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				Intent i = new Intent(AccountDetailActivity.this, FansActivity.class);
				i.putExtra("uid", current.getID());
				i.putExtra("fans_count", current.getFansCount());
				Client.saveActivity(getIntent());
				startActivity(i);
			}
		});
		new ClientString(videoCount).colorTo((TextView) findViewById(R.id.main_user_video_count), 0xff88d9fa);
		new ClientString(blogCount).colorTo((TextView) findViewById(R.id.main_user_blog_count), 0xff88d9fa);
		new ClientString(followingCount).colorTo(suscribeView, 0xff88d9fa);
		new ClientString(fansCount).colorTo(fansView, 0xff88d9fa);
		onloadView = findViewById(R.id.more_onload);
		favoriteCategory = findViewById(R.id.favorite_categorys);
		refresh = findViewById(R.id.refresh);
		refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				Toast.makeText(getApplication(), "走位中...", Toast.LENGTH_SHORT).show();
				request(true);
			}
		});
		GridLayoutManager layout = new GridLayoutManager(AccountDetailActivity.this, 1);
		layout.setInitialPrefetchItemCount(6);
		layout.setItemPrefetchEnabled(true);
		view = findViewById(R.id.user_list);
		view.setLayoutManager(layout);
		view.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView view, int state) {
				super.onScrollStateChanged(view, state);
				if (state == RecyclerView.SCROLL_STATE_IDLE) {
					int itemCount = view.getLayoutManager().getItemCount();
					int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
					if (lastPos >= itemCount - 1 && (videos.size() >= 12 || blogs.size() >= 12)) {
						onloadView.setVisibility(View.VISIBLE);
						request(false);
					}
				}
			}
		});
		blogView = findViewById(R.id.blog_list);
		blogView.setLayoutManager(new GridLayoutManager(this, 1));
		blogView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView view, int state) {
				super.onScrollStateChanged(view, state);
				if (state == RecyclerView.SCROLL_STATE_IDLE) {
					int itemCount = view.getLayoutManager().getItemCount();
					int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
					if (lastPos >= itemCount - 1 && itemCount >= 12) {
						onloadView.setVisibility(View.VISIBLE);
						request(false);
					}
				}
			}
		});
		videoView = findViewById(R.id.video_list);
		videoView.setLayoutManager(new GridLayoutManager(this, 1));
		videoView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView view, int state) {
				super.onScrollStateChanged(view, state);
				if (state == RecyclerView.SCROLL_STATE_IDLE) {
					int itemCount = view.getLayoutManager().getItemCount();
					int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
					if (lastPos >= itemCount - 1 && itemCount >= 12) {
						onloadView.setVisibility(View.VISIBLE);
						request(false);
					}
				}
			}
		});
		videoStatus = content.findViewWithTag("favorite_video_status");
		blogStatus = content.findViewWithTag("favorite_blog_status");
		videoStatus.setColorFilter(Color.BLACK);
		blogStatus.setColorFilter(Color.BLACK);
		Button followingBtn = findViewById(R.id.following_user);
		followingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				followingUser(v);
			}
		});
        if (uid == AccountManager.getInstance(this).getAccount().getUID()) {
            ImageView avatarEdit = findViewById(R.id.avatar_edit);
            avatarEdit.setVisibility(View.VISIBLE);
            avatarEdit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: Implement this method
                        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, IMAGE_REQUEST_CODE);
                        Toast.makeText(getApplication(), "请选择头像文件，大小不超过1MB", Toast.LENGTH_SHORT).show();
                    }
                });
		}
		setFollowingStatus(followingBtn);
		initCategoryView();
		request(true);
	}

	private void initCategoryView() {
		View parent = findViewById(android.R.id.content);
		final TextView[] main = new TextView[4];
		main[0] = parent.findViewWithTag("videos");
		main[1] = parent.findViewWithTag("blogs");
		main[2] = parent.findViewWithTag("favorite");
		main[3] = parent.findViewWithTag("local");
		final int color = Color.parseColor("#88d9fa");
		final GradientDrawable bg = new GradientDrawable();
		bg.setColor(Color.TRANSPARENT);
		bg.setStroke(2, color);
		bg.setShape(GradientDrawable.RECTANGLE);
		main[0].setCompoundDrawables(null, null, null, bg);
		for (int i = 0; i < 4; i++) {
			final int currentIndex = i;
			main[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (categoryIndex == currentIndex) {
						return;
					}
					for (int i = 0; i < 4; i++) {
						if (i == currentIndex) {
							main[i].setTextColor(color);
							main[i].setCompoundDrawables(null, null, null, bg);
						} else {
							main[i].setTextColor(Color.BLACK);
							main[i].setCompoundDrawables(null, null, null, null);
						}
					}
					categoryIndex = currentIndex;
					request(true);
				}
			});
			if (!isMyAccount() && i > 1) {
				main[i].setVisibility(View.GONE);
			}
		}
	}

	private boolean isMyAccount() {
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			return false;
		}
		return uid == manager.getAccount().getUID();
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
			if (categoryIndex < 2) {
				blogShow = false;
				videoShow = false;
				String uri = categoryIndex == 1
						? APIManager.BlogURI.getUserBlogURI(current.getID(), 0, 12)
						: APIManager.VideoURI.getUserVideo(current.getID(), 0, 12);
				NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
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
								JSONArray data = root.optJSONArray(categoryIndex == 1 ? "blog_list" : "video_list");
								final List<Video> vData = new ArrayList<>();
								final List<Blog> bData = new ArrayList<>();
								for (int i = 0; i < data.length(); i++) {
									JSONObject json = data.optJSONObject(i);
									if (categoryIndex == 1) {
										Blog currentBlog = new Blog(json);
										if (blog == null || !blog.isExists(currentBlog)) {
											bData.add(currentBlog);
										}
									} else {
										Video currentVideo = new Video(getApplication(), json, Video.VIDEO_DETAIL);
										if (video == null || !video.isExists(currentVideo)) {
											vData.add(currentVideo);
										}
									}
								}
								mainHandler.post(new Runnable() {
									@Override
									public void run() {
										refresh.setRefreshing(false);
										onloadView.setVisibility(View.GONE);
										view.setVisibility(View.VISIBLE);
										favoriteCategory.setVisibility(View.GONE);
										if (view.getAdapter() == null || categoryIndex != 1) {
											blogs.clear();
											if (!isRefresh && current.getVideoCount() > 12) {
												((VideoAdapter) view.getAdapter()).addNewData(vData);
												return;
											}
											if (videos.isEmpty()) {
												videos.addAll(vData);
												video = new VideoAdapter(AccountDetailActivity.this, videos);
											}
											view.setAdapter(video);
											return;
										} else {
											videos.clear();
											if (!isRefresh && current.getBlogCount() > 12) {
												((BlogAdapter) view.getAdapter()).addNewData(bData);
												return;
											}
											if (blogs.isEmpty()) {
												blogs.addAll(bData);
												blog = new BlogAdapter(AccountDetailActivity.this, blogs);
											}
											view.setAdapter(blog);
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
						refresh.setRefreshing(false);
						Log.e("Network", cause);
					}
				});
			} else if (categoryIndex == 2) {
				view.setVisibility(View.GONE);
				favoriteCategory.setVisibility(View.VISIBLE);
				blogView.setVisibility(blogShow ? View.VISIBLE : View.GONE);
				videoView.setVisibility(videoShow ? View.VISIBLE : View.GONE);
				blogStatus.setImageResource(blogShow ? R.drawable.ic_down : R.drawable.ic_up);
				videoStatus.setImageResource(videoShow ? R.drawable.ic_down : R.drawable.ic_up);
				String token = manager.getAccount().getToken();
				if (blogShow) {
					NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getFavoriteBlogsURI(token, 0, 12),
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
											JSONArray data = root.optJSONArray("blog_list");
											final List<Blog> bData = new ArrayList<>();
											for (int i = 0; i < data.length(); i++) {
												bData.add(new Blog(data.optJSONObject(i)));
											}
											mainHandler.post(new Runnable() {
												@Override
												public void run() {
													refresh.setRefreshing(false);
													onloadView.setVisibility(View.GONE);
													if (blogView.getAdapter() == null) {
														blogView.setAdapter(
																new BlogAdapter(AccountDetailActivity.this, bData));
													} else if (isRefresh) {
														blogView.getAdapter().notifyDataSetChanged();
													} else {
														((BlogAdapter) blogView.getAdapter()).addNewData(bData);
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
									refresh.setRefreshing(false);
									Log.e("Network", cause);
								}
							});
				}
				if (videoShow) {
					NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getFavoriteVideosURI(token, 0, 12),
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
											JSONArray data = root.optJSONArray("video_list");
											final List<Video> vData = new ArrayList<>();
											for (int i = 0; i < data.length(); i++) {
												vData.add(new Video(AccountDetailActivity.this, data.optJSONObject(i),
														Video.VIDEO_DETAIL));
											}
											mainHandler.post(new Runnable() {
												@Override
												public void run() {
													refresh.setRefreshing(false);
													onloadView.setVisibility(View.GONE);
													if (videoView.getAdapter() == null) {
														videoView.setAdapter(
																new VideoAdapter(AccountDetailActivity.this, vData));
													} else if (isRefresh) {
														videoView.getAdapter().notifyDataSetChanged();
													} else {
														((VideoAdapter) videoView.getAdapter()).addNewData(vData);
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
									refresh.setRefreshing(false);
									Log.e("Network", cause);
								}
							});
				}
			} else {
				blogShow = false;
				videoShow = false;
				videos.clear();
				String path = StringUtils.strCat(DeviceUtils.getAndroidSDK() >= Build.VERSION_CODES.R
						? getExternalFilesDir(null).toString()
						: "/sdcard/OTTOHub", "/save/");
				if (!new File(path).exists()) {
					FileUtils.createDir(path);
				}
				List<Video> localData = new ArrayList<>();
				List<File> list = FileUtils.listFile(path, new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return dir.isDirectory() && name.startsWith("OV");
					}
				});
				for (File v : list) {
					localData.add(new Video(this, v, Video.VIDEO_DEF));
				}
				view.setVisibility(View.VISIBLE);
				favoriteCategory.setVisibility(View.GONE);
				if (!isRefresh && current.getVideoCount() > 12) {
					((VideoAdapter) view.getAdapter()).addNewData(localData);
					return;
				}
				if (videos.isEmpty()) {
					videos.addAll(localData);
					video = new VideoAdapter(AccountDetailActivity.this, videos);
				}
				view.setAdapter(video);
				refresh.setRefreshing(false);
			}
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		} finally {
			request.release();
		}
	}

	private void followingUser(final View v) {
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			Toast.makeText(getApplication(), "那我缺的登录这一块", Toast.LENGTH_SHORT).show();
			return;
		}
		NetworkUtils.getNetwork.getNetworkJson(
				APIManager.FollowingURI.getFollowURI(current.getID(), manager.getAccount().getToken()),
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
										String fansCount = StringUtils.strCat(
												new Object[]{current.getFansCount(), System.lineSeparator(), "粉丝"});
										switch (following) {
											case 0 :
												Toast.makeText(getApplication(), "你太自恋了一边玩去", Toast.LENGTH_SHORT)
														.show();
												break;
											case 1 :
												Toast.makeText(getApplication(), "取关成功", Toast.LENGTH_SHORT).show();
												fansCount = StringUtils.strCat(new Object[]{current.getFansCount() - 1,
														System.lineSeparator(), "粉丝"});
												new ClientString(fansCount).colorTo(
														(TextView) findViewById(R.id.main_user_fans_count), 0xff88d9fa);
												btn.setText("灌注主播");
												break;
											case 2 :
												Toast.makeText(getApplication(), "成功把主播灌成奶油泡芙了", Toast.LENGTH_SHORT)
														.show();
												fansCount = StringUtils.strCat(new Object[]{current.getFansCount() + 1,
														System.lineSeparator(), "粉丝"});
												new ClientString(fansCount).colorTo(
														(TextView) findViewById(R.id.main_user_fans_count), 0xff88d9fa);
												btn.setText("取消灌注");
												break;
											case 3 :
												Toast.makeText(getApplication(), "分手成功", Toast.LENGTH_SHORT).show();
												fansCount = StringUtils.strCat(new Object[]{current.getFansCount() - 1,
														System.lineSeparator(), "粉丝"});
												new ClientString(fansCount).colorTo(
														(TextView) findViewById(R.id.main_user_fans_count), 0xff88d9fa);
												btn.setText("回关粉丝");
												break;
											case 4 :
												Toast.makeText(getApplication(), "结婚成功♡", Toast.LENGTH_SHORT).show();
												fansCount = StringUtils.strCat(new Object[]{current.getFansCount() + 1,
														System.lineSeparator(), "粉丝"});
												new ClientString(fansCount).colorTo(
														(TextView) findViewById(R.id.main_user_fans_count), 0xff88d9fa);
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

	private void setFollowingStatus(final Button followingBtn) {
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			return;
		}
		final Handler mainHandler = new Handler(getMainLooper());
		NetworkUtils.getNetwork.getNetworkJson(
				APIManager.FollowingURI.getFollowStatusURI(current.getID(), manager.getAccount().getToken()),
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

	public void loadFavoriteVideo(View v) {
		if (videoShow) {
			videoStatus.setImageResource(R.drawable.ic_up);
			videoView.setVisibility(View.GONE);
			videoShow = false;
			return;
		}
		videoShow = true;
		request(true);
	}

	public void loadFavoriteBlog(View v) {
		if (blogShow) {
			blogStatus.setImageResource(R.drawable.ic_up);
			blogView.setVisibility(View.GONE);
			blogShow = false;
			return;
		}
		blogShow = true;
		request(true);
	}

	public void quit(View v) {
		finish();
	}

	private static class UserInfo {
		private JSONObject root;
		private long id;

		private UserInfo(JSONObject main, long uid) {
			root = main;
			id = uid;
		}

		public long getID() {
			return id;
		}

		public String getUser() {
			return root.optString("username", "棍母");
		}

		public String getIntro() {
			return root.optString("intro", "哈姆");
		}

		public String getSex() {
			return root.optString("sex", "道理");
		}

		public String getTime() {
			return root.optString("time", "0月0日");
		}

		public String[] getHonours() {
			return root.optString("honour", "吉吉国民").split(",");
		}

		public int getExp() {
			return root.optInt("experience", 0);
		}

		public HashMap<String, Integer> getLevel() {
			int exp = getExp();
			HashMap<String, Integer> levelMap = new HashMap<>();
			if (exp >= 0 && exp < 49) {
				levelMap.put("ZERO", 0xff6D6D6D);
			} else if (exp >= 50 && exp < 499) {
				levelMap.put("UNO", 0xffC1C1C1);
			} else if (exp >= 500 && exp < 999) {
				levelMap.put("DUE", 0xff73D858);
			} else if (exp >= 1000 && exp < 2999) {
				levelMap.put("TRE", 0xff6779A9);
			} else if (exp >= 3000 && exp < 7999) {
				levelMap.put("QUATTRO", 0xff52ABD5);
			} else if (exp >= 8000 && exp < 14999) {
				levelMap.put("CINQUE", 0xffDC6C6B);
			} else if (exp >= 15000 && exp < 29999) {
				levelMap.put("SEI", 0xffB070C7);
			} else if (exp >= 30000 && exp < 79999) {
				levelMap.put("SETTE", 0xff000000);
			} else {
				levelMap.put("OTTO", 0xffDAD55D);
			}
			return levelMap;
		}

		public String getAvatarURI() {
			return root.optString("avatar_url", "null");
		}

		public String getCoverURI() {
			return root.optString("cover_url", "null");
		}

		public int getVideoCount() {
			if (root == null) {
				return -1;
			}
			String stringID = root.optString("video_num", "0");
			return stringID == null || stringID.isEmpty() ? root.optInt("video_num", 0) : Integer.parseInt(stringID);
		}

		public int getBlogCount() {
			if (root == null) {
				return -1;
			}
			String stringID = root.optString("blog_num", "0");
			return stringID == null || stringID.isEmpty() ? root.optInt("blog_num", 0) : Integer.parseInt(stringID);
		}

		public int getFollowingCount() {
			return root.optInt("followings_count", 0);
		}

		public int getFansCount() {
			return root.optInt("fans_count", 0);
		}
	}
}

