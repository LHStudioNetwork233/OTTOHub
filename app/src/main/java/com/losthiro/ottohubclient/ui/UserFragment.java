/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.ui;
import androidx.fragment.app.*;
import android.os.Bundle;
import android.os.Handler;
import java.util.concurrent.*;
import android.os.Looper;
import com.losthiro.ottohubclient.R;
import android.view.*;
import com.losthiro.ottohubclient.utils.*;
import com.losthiro.ottohubclient.impl.*;
import org.json.*;
import android.util.*;
import android.widget.*;
import java.util.*;
import android.view.View.*;
import java.io.*;
import android.graphics.drawable.*;
import android.graphics.*;
import android.content.*;
import com.losthiro.ottohubclient.*;
import com.losthiro.ottohubclient.adapter.*;
import androidx.recyclerview.widget.*;
import android.provider.*;
import androidx.viewpager.widget.*;
import com.losthiro.ottohubclient.adapter.page.*;
import androidx.swiperefreshlayout.widget.*;
import com.losthiro.ottohubclient.adapter.model.*;
import android.app.AlertDialog;
import com.losthiro.ottohubclient.crashlogger.*;

public class UserFragment extends Fragment {
	public final static String TAG = "User";
	private final static Handler uiThread = new Handler(Looper.getMainLooper());
	private ViewPager userPager;
	private TextView[] main;

	public static UserFragment newInstance(long uid) {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		arg.putLong("uid", uid);
		UserFragment userPage = new UserFragment();
		userPage.setArguments(arg);
		return userPage;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: Implement this method
		return inflater.inflate(R.layout.fragment_user, container, false);
	}

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onViewCreated(view, savedInstanceState);
        TextView name = view.findViewById(R.id.main_user_name);
		if (Client.rngFun <= 9 || Client.rngFun >= 99) {
			name.setText("baka");
            //你在试图找什么？你这个baka (
		}
		final long uid = getArguments().getLong("uid", 0);
		NetworkUtils.getNetwork.getNetworkJson(APIManager.UserURI.getUserDetail(uid), new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(String content) {
				if (content == null || content.isEmpty()) {
					onFailed("empty content");
					return;
				}
				try {
					final JSONObject json = new JSONObject(content);
					if (json == null) {
						onFailed("null json");
						return;
					}
					String status = json.optString("status", "error");
					if (status.equals("success")) {
						uiThread.post(new Runnable() {
							@Override
							public void run() {
								// TODO: Implement this method
								initUI(new UserInfo(json, uid), view);
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
                NetworkException.getInstance(getContext()).handlerError(cause);
			}
		});
	}

	private void initUI(final UserInfo current, View view) {
		ImageView cover = view.findViewById(R.id.main_user_cover);
		cover.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				ImageViewerFragment.newInstance(current.getCoverURI()).show(getFragmentManager(), "cover");
			}
		});
		ImageView avatar = view.findViewById(R.id.main_user_avatar);
		avatar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				ImageViewerFragment.newInstance(current.getAvatarURI()).show(getFragmentManager(), "avatar");
			}
		});
		ImageDownloader.loader(avatar, current.getAvatarURI());
		ImageDownloader.loader(cover, current.getCoverURI());
		FragmentActivity a = requireActivity();
		if (a instanceof AccountDetailActivity) {
			((AccountDetailActivity) a).setUserTitle(current.getUser());
		}
		TextView name = view.findViewById(R.id.main_user_name);
		name.setText(current.getUser());
		name.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				editNameDialog(current.getUser());
			}
		});
		int now = current.getExp();
		int max = getMax(now);
		((TextView) view.findViewById(R.id.account_exp_text))
				.setText(StringUtils.strCat(new Object[]{"EXP: ", now, File.separator, max}));
		ProgressBar exp = view.findViewById(R.id.account_exp_progress);
		exp.setMax(max);
		exp.setProgress(now > max ? max : now);
		Drawable bgExp = exp.getProgressDrawable();
		TextView levelText = view.findViewById(R.id.main_user_level);
		HashMap<String, Integer> levelMap = current.getLevel();
		GradientDrawable bg = new GradientDrawable();
		bg.setCornerRadius(8f);
		for (HashMap.Entry<String, Integer> entry : levelMap.entrySet()) {
			levelText.setText(entry.getKey());
			bg.setColor(entry.getValue());
			bgExp.setColorFilter(entry.getValue(), PorterDuff.Mode.SRC_IN);
		}
		levelText.setBackgroundDrawable(bg);
		levelText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				Intent i = new Intent(getContext(), BlogDetailActivity.class);
				i.putExtra("bid", 65L);
				requireActivity().startActivity(i);
			}
		});
		exp.setProgressDrawable(bgExp);
		String userInfo = StringUtils.strCat(
				new Object[]{"UID: ", current.getID(), " - 性别: ", current.getSex(), " - 注册日: ", current.getTime()});
		((TextView) view.findViewById(R.id.main_user_detail)).setText(userInfo);
		((TextView) view.findViewById(R.id.main_user_intro)).setText(current.getIntro());
		HonourAdapter adapter = new HonourAdapter(getContext(), Arrays.<String>asList(current.getHonours()));
		adapter.setHiddenDef(false);
		RecyclerView honourList = view.findViewById(R.id.main_user_honours);
		honourList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
		honourList.setAdapter(adapter);
		String videoCount = StringUtils.strCat(new Object[]{current.getVideoCount(), System.lineSeparator(), "视频"});
		String blogCount = StringUtils.strCat(new Object[]{current.getBlogCount(), System.lineSeparator(), "动态"});
		String followingCount = StringUtils
				.strCat(new Object[]{current.getFollowingCount(), System.lineSeparator(), "关注"});
		String fansCount = StringUtils.strCat(new Object[]{current.getFansCount(), System.lineSeparator(), "粉丝"});
		TextView suscribeView = view.findViewById(R.id.main_user_following_count);
		suscribeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				Intent i = new Intent(getContext(), SuscribeActivity.class);
				i.putExtra("uid", current.getID());
				i.putExtra("sus_count", current.getFollowingCount());
				requireActivity().startActivity(i);
			}
		});
		TextView fansView = view.findViewById(R.id.main_user_fans_count);
		fansView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				Intent i = new Intent(getContext(), FansActivity.class);
				i.putExtra("uid", current.getID());
				i.putExtra("fans_count", current.getFansCount());
				requireActivity().startActivity(i);
			}
		});
		int color = ResourceUtils.getColor(R.color.colorAccent);
		new ClientString(videoCount).colorTo((TextView) view.findViewById(R.id.main_user_video_count), color);
		new ClientString(blogCount).colorTo((TextView) view.findViewById(R.id.main_user_blog_count), color);
		new ClientString(followingCount).colorTo(suscribeView, color);
		new ClientString(fansCount).colorTo(fansView, color);
		userPager = view.findViewById(R.id.user_detail_pager);
		userPager.setAdapter(initPager(current.getID()));
		userPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int p) {
				// TODO: Implement this method
			}

			@Override
			public void onPageScrolled(int p, float p1, int p2) {
				// TODO: Implement this method
			}

			@Override
			public void onPageSelected(int p) {
				// TODO: Implement this method
				updatePage(p);
			}
		});
		initCategoryView(current.getID());
		initIfLogin(current, view);
	}

	private PagerAdapter initPager(long uid) {
		// TODO: Implement this method
		PagesAdapter data = new PagesAdapter(this);
		data.addItem(UserVideo.newInstance(uid));
		data.addItem(UserBlog.newInstance(uid));
		data.addItem(UserFavourite.newInstance(uid));
		data.addItem(UserVideo.newInstance(uid, true));
		return data;
	}

	private void initIfLogin(final UserInfo info, View view) {
		long uid = info.getID();
		Button followingBtn = view.findViewById(R.id.following_user);
		followingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				followingUser(v, info);
			}
		});
		Account account = AccountManager.getInstance(getContext()).getAccount();
		if (account == null) {
			Log.i(TAG, "not login");
			return;
		}
		setFollowingStatus(followingBtn, uid);
		if (uid == account.getUID()) {
			ImageView avatarEdit = view.findViewById(R.id.avatar_edit);
			avatarEdit.setVisibility(View.VISIBLE);
			avatarEdit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO: Implement this method
					Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					requireActivity().startActivityForResult(i, BasicActivity.IMAGE_REQUEST_CODE);
					Toast.makeText(getContext(), "请选择头像文件，大小不超过1MB", Toast.LENGTH_SHORT).show();
				}
			});
			ImageView profileBtn = view.findViewById(R.id.profile_btn);
			profileBtn.setVisibility(View.VISIBLE);
			profileBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO: Implement this method
					FragmentActivity a = requireActivity();
					if (a instanceof AccountDetailActivity) {
						((AccountDetailActivity) a).loadUserProfile();
					}
				}
			});
		}
	}

	private void initCategoryView(long uid) {
		boolean isMe = false;
		View parent = getView();
		main = new TextView[4];
		main[0] = parent.findViewWithTag("videos");
		main[1] = parent.findViewWithTag("blogs");
		main[2] = parent.findViewWithTag("favorite");
		main[3] = parent.findViewWithTag("local");
		final int color = ResourceUtils.getColor(R.color.colorAccent);
		final GradientDrawable bg = new GradientDrawable();
		bg.setColor(Color.TRANSPARENT);
		bg.setStroke(2, color);
		bg.setShape(GradientDrawable.RECTANGLE);
		AccountManager manager = AccountManager.getInstance(getContext());
		if (manager.isLogin()) {
			isMe = uid == manager.getAccount().getUID();
		}
		for (int i = 0; i < 4; i++) {
			final int currentIndex = i;
			main[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int categoryIndex = userPager.getCurrentItem();
					if (categoryIndex == currentIndex) {
						return;
					}
					updatePage(currentIndex);
					userPager.setCurrentItem(currentIndex, true);
				}
			});
			if (!isMe && i > 1) {
				main[i].setVisibility(View.GONE);
			}
			if (userPager.getCurrentItem() == i) {
				main[i].setCompoundDrawables(null, null, null, bg);
			}
		}
	}

	private void updatePage(int currentIndex) {
		int color = ResourceUtils.getColor(R.color.colorAccent);
		GradientDrawable bg = new GradientDrawable();
		bg.setColor(Color.TRANSPARENT);
		bg.setStroke(2, color);
		bg.setShape(GradientDrawable.RECTANGLE);
		for (int i = 0; i < 4; i++) {
			if (i == currentIndex) {
				main[i].setTextColor(color);
				main[i].setCompoundDrawables(null, null, null, bg);
			} else {
				main[i].setTextColor(ResourceUtils.getColor(R.color.colorSecondary));
				main[i].setCompoundDrawables(null, null, null, null);
			}
		}
	}

	private void followingUser(final View v, final UserInfo current) {
		Context ctx = getContext();
		AccountManager manager = AccountManager.getInstance(ctx);
		if (!manager.isLogin()) {
			Toast.makeText(ctx, "那我缺的登录这一块", Toast.LENGTH_SHORT).show();
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
								uiThread.post(new Runnable() {
									@Override
									public void run() {
										Button btn = (Button) v;
										Context ctx = getContext();
										String fansCount = StringUtils.strCat(
												new Object[]{current.getFansCount(), System.lineSeparator(), "粉丝"});
										int color = ResourceUtils.getColor(R.color.colorAccent);
										switch (following) {
											case 0 :
												Toast.makeText(ctx, "你太自恋了一边玩去", Toast.LENGTH_SHORT).show();
												break;
											case 1 :
												Toast.makeText(ctx, "取关成功", Toast.LENGTH_SHORT).show();
												fansCount = StringUtils.strCat(new Object[]{current.getFansCount() - 1,
														System.lineSeparator(), "粉丝"});
												new ClientString(fansCount).colorTo(
														(TextView) getView().findViewById(R.id.main_user_fans_count),
														color);
												btn.setText("灌注主播");
												break;
											case 2 :
												Toast.makeText(ctx, "成功把主播灌成奶油泡芙了", Toast.LENGTH_SHORT).show();
												fansCount = StringUtils.strCat(new Object[]{current.getFansCount() + 1,
														System.lineSeparator(), "粉丝"});
												new ClientString(fansCount).colorTo(
														(TextView) getView().findViewById(R.id.main_user_fans_count),
														color);
												btn.setText("取消灌注");
												break;
											case 3 :
												Toast.makeText(ctx, "分手成功", Toast.LENGTH_SHORT).show();
												fansCount = StringUtils.strCat(new Object[]{current.getFansCount() - 1,
														System.lineSeparator(), "粉丝"});
												new ClientString(fansCount).colorTo(
														(TextView) getView().findViewById(R.id.main_user_fans_count),
														color);
												btn.setText("回关粉丝");
												break;
											case 4 :
												Toast.makeText(ctx, "结婚成功♡", Toast.LENGTH_SHORT).show();
												fansCount = StringUtils.strCat(new Object[]{current.getFansCount() + 1,
														System.lineSeparator(), "粉丝"});
												new ClientString(fansCount).colorTo(
														(TextView) getView().findViewById(R.id.main_user_fans_count),
														color);
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
                        NetworkException.getInstance(getContext()).handlerError(cause);
					}
				});
	}

	private void setFollowingStatus(final Button followingBtn, long uid) {
		AccountManager manager = AccountManager.getInstance(getContext());
		if (!manager.isLogin()) {
			Log.i(TAG, "request following status failed: not login");
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
								uiThread.post(new Runnable() {
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
                        NetworkException.getInstance(getContext()).handlerError(cause);
					}
				});
	}

	private int getMax(int now) {
		if (now >= 50 && now < 499) {
			return 499;
		} else if (now >= 500 && now < 999) {
			return 999;
		} else if (now >= 1000 && now < 2999) {
			return 2999;
		} else if (now >= 3000 && now < 7999) {
			return 7999;
		} else if (now >= 8000 && now < 14999) {
			return 14999;
		} else if (now >= 15000 && now < 29999) {
			return 29999;
		} else if (now >= 30000 && now < 79999) {
			return 79999;
		} else if (now < 50) {
			return 49;
		}
		return 80000;
	}

	private void editNameDialog(String old) {
		Context ctx = getContext();
		final EditText edit = new EditText(ctx);
		edit.setHint("在此输入你的新名字...");
		edit.setText(old);
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setCancelable(false);
		builder.setTitle("用户改名");
		builder.setMessage("是否修改名称？");
		builder.setView(edit);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				FragmentActivity a = requireActivity();
				if (a instanceof AccountDetailActivity) {
					((AccountDetailActivity) a).editName(edit.getText().toString());
				}
				dia.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	public void loadFavouriteVideo() {
		PagerAdapter adapter = userPager.getAdapter();
		if (adapter instanceof PagesAdapter) {
			Fragment current = ((PagesAdapter) adapter).getItem(userPager.getCurrentItem());
			if (current instanceof UserFavourite) {
				((UserFavourite) current).loadVideo();
			}
		}
	}

	public void loadFavouriteBlog() {
		PagerAdapter adapter = userPager.getAdapter();
		if (adapter instanceof PagesAdapter) {
			Fragment current = ((PagesAdapter) adapter).getItem(userPager.getCurrentItem());
			if (current instanceof UserFavourite) {
				((UserFavourite) current).loadBlog();
			}
		}
	}

	public static class UserVideo extends Fragment {
		public static final String SUB_TAG = "UserVideo";
		private final static Handler uiThread = new Handler(Looper.getMainLooper());
		private static final Semaphore request = new Semaphore(1);
		private SwipeRefreshLayout refresh;
		private RecyclerView list;
		private int offset;

		public static UserVideo newInstance(long uid) {
			return newInstance(uid, false);
		}

		public static UserVideo newInstance(long uid, boolean isLocal) {
			Bundle arg = new Bundle();
			arg.putString("tag", SUB_TAG);
			arg.putLong("uid", uid);
			arg.putBoolean("isLocal", isLocal);
			UserVideo videoPage = new UserVideo();
			videoPage.setArguments(arg);
			return videoPage;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			// TODO: Implement this method
			View root = inflater.inflate(R.layout.fragment_user_list, container, false);
			GridLayoutManager layout = new GridLayoutManager(getContext(), 1);
			layout.setInitialPrefetchItemCount(6);
			layout.setItemPrefetchEnabled(true);
			list = root.findViewById(R.id.user_list);
			list.setLayoutManager(layout);
			list.setItemViewCacheSize(20);
			list.setDrawingCacheEnabled(true);
			list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
			list.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(RecyclerView view, int state) {
					super.onScrollStateChanged(view, state);
					if (state == RecyclerView.SCROLL_STATE_IDLE) {
						int itemCount = view.getLayoutManager().getItemCount();
						int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
						if (lastPos >= itemCount - 1) {
							RecyclerView.Adapter adapter = list.getAdapter();
							if (adapter != null && adapter instanceof VideoAdapter) {
								((VideoAdapter) adapter).startLoading();
							}
							offset = offset + 12;
							request(false);
						}
					}
				}
			});
			refresh = root.findViewById(R.id.refresh);
			refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					// TODO: Implement this method
					Toast.makeText(getContext(), R.string.loading, Toast.LENGTH_SHORT).show();
					offset = 0;
					request(true);
				}
			});
			return root;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			// TODO: Implement this method
			super.onViewCreated(view, savedInstanceState);
			refresh.setRefreshing(true);
			request(true);
		}

		private void request(final boolean isRefresh) {
			try {
				if (!request.tryAcquire()) {
					return;
				}
				Bundle arg = getArguments();
				if (arg == null) {
					return;
				}
				final Context ctx = getContext();
				long uid = arg.getLong("uid", 0);
				boolean isLocal = arg.getBoolean("isLocal", false);
				AccountManager manager = AccountManager.getInstance(ctx);
				if (!manager.isLogin()) {
					return;
				}
				if (isLocal) {
					requestLocal(ctx);
					return;
				}
				NetworkUtils.getNetwork.getNetworkJson(APIManager.VideoURI.getUserVideo(uid, offset, 12),
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
												RecyclerView.Adapter adapter = list.getAdapter();
												if (adapter == null) {
													adapter = new VideoAdapter(ctx, data);
													list.setAdapter(adapter);
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
										refresh.setRefreshing(false);
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
                                NetworkException.getInstance(getContext()).handlerError(cause);
								refresh.setRefreshing(false);
							}
						});
			} catch (Exception e) {
				Thread.currentThread().interrupt();
			} finally {
				request.release();
			}
		}

		private void requestLocal(Context ctx) {
			String path = FileUtils.getStorage(ctx, BasicActivity.PATH_SAVE);
			if (!new File(path).exists()) {
				FileUtils.createDir(path);
			}
			List<Video> localData = new ArrayList<>();
			List<File> data = FileUtils.listFile(path, new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return dir.isDirectory() && name.startsWith("OV");
				}
			});
			for (File v : data) {
				localData.add(new Video(ctx, v, Video.VIDEO_DEF));
			}
			list.setAdapter(new VideoAdapter(ctx, localData));
			refresh.setRefreshing(false);
		}
	}

	public static class UserBlog extends Fragment {
		public static final String SUB_TAG = "UserBlog";
		private final static Handler uiThread = new Handler(Looper.getMainLooper());
		private static final Semaphore request = new Semaphore(1);
		private SwipeRefreshLayout refresh;
		private RecyclerView list;
		private int offset;

		public static UserBlog newInstance(long uid) {
			Bundle arg = new Bundle();
			arg.putString("tag", SUB_TAG);
			arg.putLong("uid", uid);
			UserBlog blogPage = new UserBlog();
			blogPage.setArguments(arg);
			return blogPage;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			// TODO: Implement this method
			View root = inflater.inflate(R.layout.fragment_user_list, container, false);
			GridLayoutManager layout = new GridLayoutManager(getContext(), 1);
			layout.setInitialPrefetchItemCount(6);
			layout.setItemPrefetchEnabled(true);
			list = root.findViewById(R.id.user_list);
			list.setLayoutManager(layout);
			list.setItemViewCacheSize(20);
			list.setDrawingCacheEnabled(true);
			list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
			list.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(RecyclerView view, int state) {
					super.onScrollStateChanged(view, state);
					if (state == RecyclerView.SCROLL_STATE_IDLE) {
						int itemCount = view.getLayoutManager().getItemCount();
						int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
						if (lastPos >= itemCount - 1) {
							RecyclerView.Adapter adapter = list.getAdapter();
							if (adapter != null && adapter instanceof VideoAdapter) {
								((VideoAdapter) adapter).startLoading();
							}
							offset = offset + 12;
							request(false);
						}
					}
				}
			});
			refresh = root.findViewById(R.id.refresh);
			refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					// TODO: Implement this method
					Toast.makeText(getContext(), R.string.loading, Toast.LENGTH_SHORT).show();
					offset = 0;
					request(true);
				}
			});
			return root;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			// TODO: Implement this method
			super.onViewCreated(view, savedInstanceState);
			refresh.setRefreshing(true);
			request(true);
		}

		private void request(final boolean isRefresh) {
			try {
				if (!request.tryAcquire()) {
					return;
				}
				Bundle arg = getArguments();
				if (arg == null) {
					return;
				}
				final Context ctx = getContext();
				long uid = arg.getLong("uid", 0);
				AccountManager manager = AccountManager.getInstance(ctx);
				if (!manager.isLogin()) {
					return;
				}
				NetworkUtils.getNetwork.getNetworkJson(APIManager.BlogURI.getUserBlogURI(uid, offset, 12),
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
										JSONArray video = json.optJSONArray("blog_list");
										final List<Blog> data = new ArrayList<>();
										for (int i = 0; i < video.length(); i++) {
											data.add(new Blog(video.optJSONObject(i)));
										}
										uiThread.post(new Runnable() {
											@Override
											public void run() {
												RecyclerView.Adapter adapter = list.getAdapter();
												if (adapter == null) {
													adapter = new BlogAdapter(ctx, data);
													list.setAdapter(adapter);
													return;
												}
												if (adapter instanceof BlogAdapter) {
													BlogAdapter instance = (BlogAdapter) adapter;
													if (isRefresh) {
														instance.setData(data);
													} else {
														instance.addNewData(data);
														instance.stopLoading();
													}
												}
											}
										});
										refresh.setRefreshing(false);
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
                                NetworkException.getInstance(getContext()).handlerError(cause);
								refresh.setRefreshing(false);
							}
						});
			} catch (Exception e) {
				Thread.currentThread().interrupt();
			} finally {
				request.release();
			}
		}
	}

	public static class UserFavourite extends Fragment {
		public static final String SUB_TAG = "UserBlog";
		private final static Handler uiThread = new Handler(Looper.getMainLooper());
		private static final Semaphore request = new Semaphore(1);
		private static final HashMap<Integer, Integer> offsetMap = new HashMap<>();
		private SwipeRefreshLayout refresh;
		private RecyclerView vlist;
		private RecyclerView blist;
		private ImageView vIcon;
		private ImageView bIcon;
		private boolean showBlog;
		private boolean showVideo;

		public static UserFavourite newInstance(long uid) {
			Bundle arg = new Bundle();
			arg.putString("tag", SUB_TAG);
			arg.putLong("uid", uid);
			UserFavourite favPage = new UserFavourite();
			favPage.setArguments(arg);
			return favPage;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			// TODO: Implement this method
			View root = inflater.inflate(R.layout.fragment_user_favourite, container, false);
			GridLayoutManager layout = new GridLayoutManager(getContext(), 1);
			layout.setInitialPrefetchItemCount(6);
			layout.setItemPrefetchEnabled(true);
			vlist = root.findViewById(R.id.videos_list);
			vlist.setLayoutManager(layout);
			vlist.setItemViewCacheSize(20);
			vlist.setDrawingCacheEnabled(true);
			vlist.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
			vlist.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(RecyclerView view, int state) {
					super.onScrollStateChanged(view, state);
					if (state == RecyclerView.SCROLL_STATE_IDLE) {
						int itemCount = view.getLayoutManager().getItemCount();
						int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
						if (lastPos >= itemCount - 1) {
							RecyclerView.Adapter adapter = vlist.getAdapter();
							if (adapter != null && adapter instanceof VideoAdapter) {
								((VideoAdapter) adapter).startLoading();
							}
							offsetMap.put(0, offsetMap.getOrDefault(0, 0) + 12);
							request(false);
						}
					}
				}
			});
			GridLayoutManager layout2 = new GridLayoutManager(getContext(), 1);
			layout2.setInitialPrefetchItemCount(6);
			layout2.setItemPrefetchEnabled(true);
			blist = root.findViewById(R.id.blogs_list);
			blist.setLayoutManager(layout2);
			blist.setItemViewCacheSize(20);
			blist.setDrawingCacheEnabled(true);
			blist.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
			blist.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(RecyclerView view, int state) {
					super.onScrollStateChanged(view, state);
					if (state == RecyclerView.SCROLL_STATE_IDLE) {
						int itemCount = view.getLayoutManager().getItemCount();
						int lastPos = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
						if (lastPos >= itemCount - 1) {
							RecyclerView.Adapter adapter = blist.getAdapter();
							if (adapter != null && adapter instanceof BlogAdapter) {
								((BlogAdapter) adapter).startLoading();
							}
							offsetMap.put(1, offsetMap.getOrDefault(1, 0) + 12);
							request(false);
						}
					}
				}
			});
			int color = ResourceUtils.getColor(R.color.colorSecondary);
			vIcon = root.findViewWithTag("favorite_video_status");
			bIcon = root.findViewWithTag("favorite_blog_status");
			vIcon.setColorFilter(color);
			bIcon.setColorFilter(color);
			refresh = root.findViewById(R.id.refresh);
			refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					// TODO: Implement this method
					Toast.makeText(getContext(), R.string.loading, Toast.LENGTH_SHORT).show();
					if (showVideo) {
						offsetMap.put(0, 0);
					}
					if (showBlog) {
						offsetMap.put(1, 0);
					}
					request(true);
				}
			});
			return root;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			// TODO: Implement this method
			super.onViewCreated(view, savedInstanceState);
			refresh.setRefreshing(true);
			request(true);
		}

		private void request(final boolean isRefresh) {
			try {
				if (!request.tryAcquire()) {
					return;
				}
				Bundle arg = getArguments();
				if (arg == null) {
					return;
				}
				final Context ctx = getContext();
				long uid = arg.getLong("uid", 0);
				AccountManager manager = AccountManager.getInstance(ctx);
				if (!manager.isLogin()) {
					return;
				}
				Account current = manager.getAccount();
				if (current.getUID() != uid) {
					return;
				}
				if (showBlog) {
					NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getFavoriteBlogsURI(current.getToken(),
							offsetMap.getOrDefault(1, 0), 12), new NetworkUtils.HTTPCallback() {
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
											JSONArray video = json.optJSONArray("blog_list");
											final List<Blog> data = new ArrayList<>();
											for (int i = 0; i < video.length(); i++) {
												data.add(new Blog(video.optJSONObject(i)));
											}
											uiThread.post(new Runnable() {
												@Override
												public void run() {
													RecyclerView.Adapter adapter = blist.getAdapter();
													if (adapter == null) {
														adapter = new BlogAdapter(ctx, data);
														blist.setAdapter(adapter);
														return;
													}
													if (adapter instanceof BlogAdapter) {
														BlogAdapter instance = (BlogAdapter) adapter;
														if (isRefresh) {
															instance.setData(data);
														} else {
															instance.addNewData(data);
															instance.stopLoading();
														}
													}
												}
											});
											refresh.setRefreshing(false);
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
                                    NetworkException.getInstance(getContext()).handlerError(cause);
									refresh.setRefreshing(false);
								}
							});
				}
				if (showVideo) {
					NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getFavoriteVideosURI(
							current.getToken(), offsetMap.getOrDefault(0, 0), 12), new NetworkUtils.HTTPCallback() {
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
												data.add(new Video(getContext(), video.optJSONObject(i),
														Video.VIDEO_DEF));
											}
											uiThread.post(new Runnable() {
												@Override
												public void run() {
													RecyclerView.Adapter adapter = vlist.getAdapter();
													if (adapter == null) {
														adapter = new VideoAdapter(ctx, data);
														vlist.setAdapter(adapter);
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
											refresh.setRefreshing(false);
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
                                    NetworkException.getInstance(getContext()).handlerError(cause);
									refresh.setRefreshing(false);
								}
							});
				}
				if (!showBlog && !showVideo && isRefresh) {
					refresh.setRefreshing(false);
				}
			} catch (Exception e) {
				Thread.currentThread().interrupt();
			} finally {
				request.release();
			}
		}

		public void loadVideo() {
			showVideo = !showVideo;
			vlist.setVisibility(showVideo ? View.VISIBLE : View.GONE);
			vIcon.setImageResource(showVideo ? R.drawable.ic_down : R.drawable.ic_up);
			if (showVideo) {
				request(true);
			}
		}

		public void loadBlog() {
			showBlog = !showBlog;
			blist.setVisibility(showBlog ? View.VISIBLE : View.GONE);
			bIcon.setImageResource(showBlog ? R.drawable.ic_down : R.drawable.ic_up);
			if (showBlog) {
				request(true);
			}
		}
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
			return root.optString("avatar_url");
		}

		public String getCoverURI() {
			return root.optString("cover_url");
		}

		public int getVideoCount() {
			if (root == null) {
				return -1;
			}
			String stringID = root.optString("video_num");
			return stringID == null || stringID.isEmpty() ? root.optInt("video_num", 0) : Integer.parseInt(stringID);
		}

		public int getBlogCount() {
			if (root == null) {
				return -1;
			}
			String stringID = root.optString("blog_num");
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

