/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.ui;
import androidx.fragment.app.*;
import android.os.Bundle;
import android.view.*;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.impl.*;
import android.content.*;
import com.losthiro.ottohubclient.utils.*;
import org.json.*;
import android.util.*;
import android.os.Handler;
import android.os.Looper;
import com.losthiro.ottohubclient.adapter.model.*;
import java.util.*;
import com.losthiro.ottohubclient.adapter.*;
import androidx.recyclerview.widget.*;
import java.io.*;
import android.widget.*;
import android.view.View.*;
import android.graphics.*;
import com.losthiro.ottohubclient.*;

public class VideoInfoFragment extends Fragment {
	public final static String TAG = "VideoInfo";
	private static final Handler uiThread = new Handler(Looper.getMainLooper());
	private static final HashMap<String, Object> valueMap = new HashMap<>();
	private VideoInfo current;
	private OnRequestVideoListener mListener;
	private RecyclerView videoList;
	private TextView likeCountView;
	private TextView favouriteView;

	public static VideoInfoFragment newInstance(long vid) {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		arg.putLong("vid", vid);
		VideoInfoFragment infoPage = new VideoInfoFragment();
		infoPage.setArguments(arg);
		return infoPage;
	}

	public static VideoInfoFragment newInstance(String mainfest, long vid) {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		arg.putString("mainfest", mainfest);
		arg.putLong("vid", vid);
		arg.putBoolean("is_local", true);
		VideoInfoFragment infoPage = new VideoInfoFragment();
		infoPage.setArguments(arg);
		return infoPage;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: Implement this method
		View root = inflater.inflate(R.layout.fragment_video_info, container, false);
		Context c = root.getContext();
		videoList = root.findViewById(R.id.video_detail_list);
		videoList.setLayoutManager(new GridLayoutManager(c, 1));
		likeCountView = root.findViewById(R.id.video_like_count);
		favouriteView = root.findViewById(R.id.video_favorite_count);
		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onViewCreated(view, savedInstanceState);
		final Context ctx = requireActivity();
		Bundle arg = getArguments();
		if (arg == null) {
			return;
		}
		final long vid = arg.getLong("vid");
		if (arg.getBoolean("is_local")) {
			current = new VideoInfo(arg.getString("mainfest"), vid);
			loadUI(vid, true);
		} else {
			AccountManager manager = AccountManager.getInstance(ctx);
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
							uiThread.post(new Runnable() {
								@Override
								public void run() {
									loadUI(vid, false);
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
										videos.add(new Video(ctx, video.optJSONObject(i), Video.VIDEO_DETAIL));
									}
									uiThread.post(new Runnable() {
										@Override
										public void run() {
											VideoAdapter adapter = new VideoAdapter(ctx, videos);
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

	private void loadUI(final long vid, boolean isLocal) {
		Context ctx = requireActivity();
		View root = getView();
		JSONObject mainfest = null;
		try {
			mainfest = current.getInfos(ctx);
		} catch (Exception e) {
			Log.i(TAG, "play using network mode");
		}
		String userIntro = isLocal ? mainfest.optString("user_intro", "大家好啊，我是电棍") : current.getUserIntro();
		String type = isLocal ? mainfest.optString("type", "其他") : current.getType();
		String[] data = new String[3];
		if (!isLocal) {
			data = current.getDataCount();
		}
		valueMap.put("vid", vid);
		valueMap.put("uid", isLocal ? mainfest.optLong("uid", -1) : current.getUID());
		valueMap.put("title", isLocal ? mainfest.optString("title", "大家好啊，今天来点大家想看的东西") : current.getTitle());
		valueMap.put("time", isLocal ? mainfest.optString("time", "2009-04-09 00:00:00") : current.getTime());
		valueMap.put("user_name", isLocal ? mainfest.optString("user_name", "棍母") : current.getUserName());
		valueMap.put("view_count", isLocal ? mainfest.optString("view_count", "0播放") : data[0]);
		valueMap.put("like_count", isLocal ? mainfest.optString("like_count", "0获赞") : data[1]);
		valueMap.put("favorite_count", isLocal ? mainfest.optString("favorite_count", "0冷藏") : data[2]);
		valueMap.put("is_local", isLocal);
		mListener.onSuccess(current, valueMap.get("title").toString(), valueMap.get("uid"));
		int color = ResourceUtils.getColor(R.color.colorSecondary);
		String info = StringUtils.strCat(
				new Object[]{valueMap.get("time"), " - ", valueMap.get("view_count"), " - ", type, " - OV", vid});
		final String intro = isLocal ? mainfest.optString("intro", "打野的走位我就觉得你妈逼离谱") : current.getIntro();
		((TextView) root.findViewById(R.id.video_detail_info)).setText(info);
		final ClientString newContent = new ClientString(intro);
		TextView introText = root.findViewById(R.id.video_detail_intro);
		newContent.load(introText, true);
		introText.setOnClickListener(new OnClickListener() {
			private boolean isOpen;

			@Override
			public void onClick(View v) {
				isOpen = !isOpen;
				newContent.load((TextView) v, isOpen);
			}
		});
		((TextView) root.findViewById(R.id.video_detail_title)).setText((CharSequence) valueMap.get("title"));
		likeCountView.setText((CharSequence) valueMap.get("like_count"));
		favouriteView.setText((CharSequence) valueMap.get("favorite_count"));
		((TextView) root.findViewById(R.id.videos_detail_user_name)).setText((CharSequence) valueMap.get("user_name"));
		((TextView) root.findViewById(R.id.video_detail_user_intro)).setText(userIntro);
		((ImageButton) root.findViewWithTag("1")).setColorFilter(color);
		((ImageButton) root.findViewWithTag("2")).setColorFilter(color);
		List<String> tagList = isLocal ? getLocalTags(mainfest.optJSONArray("tags")) : Arrays.asList(current.getTags());
		if (tagList.size() > 0) {
			HonourAdapter adapter = new HonourAdapter(ctx, tagList);
			adapter.setUsingSearch(true);
			RecyclerView tags = root.findViewById(R.id.video_detail_tags);
			tags.setVisibility(View.VISIBLE);
			tags.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false));
			tags.setAdapter(adapter);
		}
		ImageView user = root.findViewById(R.id.video_detail_user_icon);
		if (isLocal) {
			user.setImageBitmap(BitmapFactory.decodeFile(current.getAvatar()));
		} else {
			ImageDownloader.loader(user, current.getAvatar());
			ImageButton likeBtn = root.findViewById(R.id.video_like_btn);
			likeBtn.setColorFilter(current.isLike()
					? ResourceUtils.getColor(R.color.colorAccent)
					: ResourceUtils.getColor(R.color.colorSecondary));
			likeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					likeCurrent(v);
				}
			});
			ImageButton favouriteBtn = root.findViewById(R.id.video_favorite_btn);
			favouriteBtn.setColorFilter(current.isFavorite()
					? ResourceUtils.getColor(R.color.colorAccent)
					: ResourceUtils.getColor(R.color.colorSecondary));
			favouriteBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					favouriteCurrent(v);
				}
			});
			Button followingBtn = root.findViewById(R.id.video_detail_following_user);
			followingBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					followingUser(v);
				}
			});
			setFollowingStatus(followingBtn, valueMap.get("uid"));
		}
	}

	private List<String> getLocalTags(JSONArray data) {
		int max = data.length();
		List<String> list = new ArrayList<>(max);
		for (int i = 0; i < max; i++) {
			String str = data.optString(i);
			if (!str.isEmpty()) {
				list.add(str);
			}
		}
		return list;
	}

	private void likeCurrent(final View v) {
		final Context ctx = getContext();
		AccountManager manager = AccountManager.getInstance(ctx);
		if (!manager.isLogin()) {
			Toast.makeText(ctx, "没登录喜欢牛魔", Toast.LENGTH_SHORT).show();
			return;
		}
		long vid = current.getVID();
		String token = manager.getAccount().getToken();
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
								uiThread.post(new Runnable() {
									@Override
									public void run() {
										((ImageButton) v).setColorFilter(isLike
												? ResourceUtils.getColor(R.color.colorAccent)
												: ResourceUtils.getColor(R.color.colorSecondary));
										likeCountView.setText(likeCount + "获赞");
										String msg = isLike ? "点赞成功" : "取消点赞成功";
										Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
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
		final Context ctx = getContext();
		AccountManager manager = AccountManager.getInstance(ctx);
		if (!manager.isLogin()) {
			Toast.makeText(ctx, "没登录点牛魔", Toast.LENGTH_SHORT).show();
			return;
		}
		long vid = current.getVID();
		String token = manager.getAccount().getToken();
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
								uiThread.post(new Runnable() {
									@Override
									public void run() {
										((ImageButton) v).setColorFilter(isFav
												? ResourceUtils.getColor(R.color.colorAccent)
												: ResourceUtils.getColor(R.color.colorSecondary));
										favouriteView.setText(fCount + "冷藏");
										String msg = isFav ? "冷藏成功" : "取消冷藏成功";
										Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
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

	private void followingUser(final View v) {
		following(valueMap.get("uid"), v);
	}

	public static void following(long uid, final View v) {
		AccountManager manager = AccountManager.getInstance(v.getContext());
		if (!manager.isLogin()) {
			Toast.makeText(v.getContext(), "那我缺的登录这一块", Toast.LENGTH_SHORT).show();
			return;
		}
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
								uiThread.post(new Runnable() {
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
					}
				});
	}

	private void saveMainfest(File dir) throws Exception {
		Context ctx = getContext();
		JSONObject mainData = new JSONObject();
		mainData.put("vid", current.getVID());
		mainData.put("uid", valueMap.get("uid"));
		mainData.put("title", valueMap.get("title"));
		mainData.put("user_name", valueMap.get("user_name"));
		mainData.put("intro", current.getIntro());
		mainData.put("user_intro", current.getUserIntro());
		mainData.put("type", current.getType());
		mainData.put("time", valueMap.get("time"));
		mainData.put("category", current.getCategory());
		mainData.put("view_count", valueMap.get("view_count"));
		mainData.put("like_count", valueMap.get("like_count"));
		mainData.put("favorite_count", valueMap.get("favourite_count"));
		String[] tagsData = current.getTags();
		JSONArray tags = new JSONArray(tagsData);
		mainData.put("tags", tags);
		String name = "mainfest.json";
		String fPath = StringUtils.strCat(new String[]{dir.toString(), File.separator, name});
		if (!new File(dir, name).exists()) {
			FileUtils.createFile(ctx, fPath, mainData.toString(4));
			return;
		}
		FileUtils.writeFile(ctx, fPath, mainData.toString(4));
	}

	public long getUID() {
		if ((boolean) valueMap.get("is_local")) {
			return valueMap.get("uid");
		}
		return current.getUID();
	}

	public void saveVideo() {
		final Context ctx = getContext();
		if ((boolean) valueMap.get("is_local")) {
			Toast.makeText(ctx, "非网络视频不能下载到本地(｡･ω･｡)", Toast.LENGTH_SHORT).show();
			return;
		}
		String destDir = FileUtils.getStorage(ctx, BasicActivity.PATH_SAVE);
		if (!new File(destDir).exists()) {
			FileUtils.createDir(destDir);
		}
		long vid = valueMap.get("vid");
		String randomName = StringUtils.strCat(
				new Object[]{"OV", vid, "-", SystemUtils.getDate("yyyy-MM-dd-HH-mm-ss-"), SystemUtils.getTime()});
		String realDir = StringUtils.strCat(destDir, randomName);
		final File dir = new File(realDir);
		if (!dir.exists()) {
			FileUtils.createDir(realDir);
		}
		try {
			Toast.makeText(ctx, "正在保存请稍候", Toast.LENGTH_SHORT).show();
			saveMainfest(dir);
			NetworkUtils.getNetwork.getNetworkJson(APIManager.DanmakuURI.getListURI(vid),
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
									FileUtils.createFile(ctx, f.getPath(), danmakuData.toString(4));
									return;
								}
								FileUtils.writeFile(ctx, f.getPath(), danmakuData.toString(4));
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
					Toast.makeText(ctx, "视频保存成功~冲刺冲刺", Toast.LENGTH_SHORT).show();
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

	public void setOnRequestVideoListener(OnRequestVideoListener listener) {
		mListener = listener;
	}

	public static interface OnRequestVideoListener {
		void onSuccess(VideoInfo current, String title, long uid);
	}

	public static class VideoInfo {
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

		public String getTitle() {
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

		public String getTime() {
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

		public String getUserName() {
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

		private String getCount(String strCount) {
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

