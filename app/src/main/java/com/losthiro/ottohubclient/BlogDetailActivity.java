package com.losthiro.ottohubclient;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.adapter.model.Comment;
import com.losthiro.ottohubclient.adapter.CommentAdapter;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.impl.ClientString;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import com.losthiro.ottohubclient.utils.StringUtils;
import com.losthiro.ottohubclient.utils.SystemUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.losthiro.ottohubclient.view.drawer.SlideDrawerManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import com.losthiro.ottohubclient.impl.WebBean;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.WebChromeClient;
import com.losthiro.ottohubclient.view.ClientWebView;
import com.losthiro.ottohubclient.adapter.*;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.utils.*;
import android.net.*;
import androidx.fragment.app.*;
import com.losthiro.ottohubclient.ui.*;
import com.losthiro.ottohubclient.crashlogger.*;

/**
 * @Author Hiro
 * @Date 2025/05/29 10:38
 */
public class BlogDetailActivity extends BasicActivity {
	public static final String TAG = "BlogDetailActivity";
	private static final Semaphore request = new Semaphore(1);
	private BlogInfo current;
	private long firstBackTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blog_detail);
		long id;
		Intent i = getIntent();
		Uri data = i.getData();
		try {
			String idStr = data.getQueryParameter("bid");
			if (idStr == null) {
				throw new Exception();
			}
			id = Long.parseLong(idStr);
		} catch (Exception unuse) {
			id = i.getLongExtra("bid", 0);
		}
		final long bid = id;
		if (bid == 0) {
			return;
		}
		if (!NetworkUtils.isNetworkAvailable(this)) {
			Toast.makeText(getApplication(), "那我缺的网络这块谁来给我补上啊", Toast.LENGTH_SHORT).show();
			return;
		}
		FragmentManager fragmanager = getSupportFragmentManager();
        FragmentTransaction transacte = fragmanager.beginTransaction();
		Fragment commentPage = fragmanager.findFragmentById(R.id.comment_page);
        final Fragment comment = CommentFragment.newInstance(bid, Comment.TYPE_BLOG);
		if (commentPage == null) {
			transacte.add(R.id.comment_page, comment);
		}
        Fragment editPage = fragmanager.findFragmentById(R.id.comment_edit_view);
        if (editPage == null) {
            CommentEditFragment edit = CommentEditFragment.newInstance(bid, 0, Comment.TYPE_BLOG);
            edit.setSendCallback(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: Implement this method
                        ((CommentFragment)comment).loadComment(true);
                    }
                });
            transacte.add(R.id.comment_edit_view, edit);
        }
        transacte.commit();
		AccountManager manager = AccountManager.getInstance(this);
		String uri = manager.isLogin()
				? APIManager.BlogURI.getBlogDetailURI(bid, manager.getAccount().getToken())
				: APIManager.BlogURI.getBlogDetailURI(bid);
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
						current = new BlogInfo(root, bid);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								loadUI();
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
                NetworkException.getInstance(getApplication()).handlerError(cause);
			}
		});

	}

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - firstBackTime > 2000) {
			Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
			firstBackTime = System.currentTimeMillis();
			return;
		}
		SlideDrawerManager manager = SlideDrawerManager.getInstance();
		View parent = manager.getLastParent();
		manager.registerDrawer(parent, (ImageButton) parent.findViewById(R.id.main_slide_bar));
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		SlideDrawerManager manager = SlideDrawerManager.getInstance();
		View parent = manager.getLastParent();
		manager.registerDrawer(parent, (ImageButton) parent.findViewById(R.id.main_slide_bar));
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int who, int targetFragment, Intent requestCode) {
		super.onActivityResult(who, targetFragment, requestCode);
		if (who == LOGIN_REQUEST_CODE) {
			String password = requestCode.getStringExtra("password");
			String content = requestCode.getStringExtra("content");
			String token = requestCode.getStringExtra("token");
			try {
				View parent = findViewById(android.R.id.content);
				SlideDrawerManager manager = SlideDrawerManager.getInstance();
				manager.registerDrawer(parent, (ImageButton) parent.findViewById(R.id.main_slide_bar));
				Account a = new Account(this, new JSONObject(content), token);
				AccountManager.getInstance(this).login(a, password);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void loadUI() {
		final TextView likeCount = findViewById(R.id.blog_like_count);
		likeCount.setText(current.getLikeCount());
		final TextView favoriteCount = findViewById(R.id.blog_favorite_count);
		favoriteCount.setText(current.getFavoriteCount());
		ImageButton like = findViewById(R.id.blog_like_btn);
		ImageButton favorite = findViewById(R.id.blog_favorite_btn);
		like.setColorFilter(current.isLike()
				? ResourceUtils.getColor(R.color.colorAccent)
				: ResourceUtils.getColor(R.color.colorSecondary));
		favorite.setColorFilter(current.isLike()
				? ResourceUtils.getColor(R.color.colorAccent)
				: ResourceUtils.getColor(R.color.colorSecondary));
		like.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				likeCurrent(v, likeCount);
			}
		});
		favorite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				favouriteCurrent(v, favoriteCount);
			}
		});
		View content = findViewById(android.R.id.content);
		SlideDrawerManager manager = SlideDrawerManager.getInstance();
		manager.registerDrawer(content, (ImageButton) content.findViewById(R.id.main_slide_bar));
		manager.setOnAccountChangeListener(this, new AccountManager.AccountListener() {
			@Override
			public void onCurrentChange(Account newCurrent) {
				ImageButton like = findViewById(R.id.blog_like_btn);
				ImageButton favorite = findViewById(R.id.blog_favorite_btn);
				like.setColorFilter(current.isLike()
						? ResourceUtils.getColor(R.color.colorAccent)
						: ResourceUtils.getColor(R.color.colorSecondary));
				favorite.setColorFilter(current.isLike()
						? ResourceUtils.getColor(R.color.colorAccent)
						: ResourceUtils.getColor(R.color.colorSecondary));
			}
		});
		ImageView avatar = findViewById(R.id.user_avatar);
		avatar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(BlogDetailActivity.this, AccountDetailActivity.class);
				i.putExtra("uid", current.getUID());
				startActivity(i);
			}
		});
		ImageDownloader.loader(avatar, current.getAvatarURI());
		((TextView) findViewById(R.id.user_name)).setText(current.getUser());
		((TextView) findViewById(R.id.blog_title)).setText(current.getTitle());
		String info = StringUtils
				.strCat(new Object[]{current.getTime(), " - ", current.getViewCount(), " - OB", current.getID()});
		((TextView) findViewById(R.id.blog_info)).setText(info);
		ClientWebView dataView = findViewById(R.id.blog_content_view);
        dataView.setTextData(current.getContent());
        dataView.setFragmentManager(getSupportFragmentManager());
        dataView.load();
		int color = ResourceUtils.getColor(R.color.colorSecondary);
		((ImageButton) content.findViewWithTag("1")).setColorFilter(color);
		((ImageButton) content.findViewWithTag("2")).setColorFilter(color);
	}

	private void likeCurrent(final View v, final TextView likeCountView) {
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			Toast.makeText(getApplication(), "没登录喜欢牛魔", Toast.LENGTH_SHORT).show();
			return;
		}
		long bid = current.getID();
		String token = manager.getAccount().getToken();
		final Handler h = new Handler(getMainLooper());
		NetworkUtils.getNetwork.getNetworkJson(APIManager.EngagementURI.getLikeBlogURI(bid, token),
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
										? root.optInt("like_count", 0)
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
                        NetworkException.getInstance(getApplication()).handlerError(cause);
					}
				});
	}

	private void favouriteCurrent(final View v, final TextView favouriteView) {
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			Toast.makeText(getApplication(), "没登录点牛魔", Toast.LENGTH_SHORT).show();
			return;
		}
		long bid = current.getID();
		String token = manager.getAccount().getToken();
		final Handler h = new Handler(getMainLooper());
		NetworkUtils.getNetwork.getNetworkJson(APIManager.EngagementURI.getFavoriteBlogURI(bid, token),
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
										? root.optInt("like_favorite", 0)
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
                        NetworkException.getInstance(getApplication()).handlerError(cause);
					}
				});
	}

	private void reportCurrent() {
		try {
			if (!request.tryAcquire()) {
				return;
			}
			String token = AccountManager.getInstance(this).getAccount().getToken();
			NetworkUtils.getNetwork.getNetworkJson(APIManager.ModerationURI.getReportBlogURI(current.getID(), token),
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
                            NetworkException.getInstance(getApplication()).handlerError(cause);
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

	public void shareBlog(View v) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_TEXT,
				"OTTOHub邀请你来看 " + current.getUser() + " 发布的动态\n" + "https://m.ottohub.cn/b/" + current.getID());
		startActivity(Intent.createChooser(i, "share"));
	}

	public void reportBlog(View v) {
		AlertDialog dialog = new AlertDialog.Builder(this).setTitle("确认举报？").setMessage("OB" + current.getID())
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dia, int which) {
						reportCurrent();
					}
				}).setNegativeButton(android.R.string.cancel, null).create();
		dialog.show();
	}

	public void switchAccountDia(View v) {
		SlideDrawerManager.getInstance().showAccountSwitch(v);
	}

	public void addAccount(View v) {
		startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST_CODE);
	}

	private static class BlogInfo {
		private JSONObject main;
		private long id;

		private BlogInfo(JSONObject root, long bid) {
			main = root;
			id = bid;
		}

		private String getCount(String type) {
			String def = StringUtils.toStr(0);
			if (main == null) {
				return def;
			}
			String strCount = main.optString(type, null);
			long count = Long.parseLong(strCount);
			if (count >= 1000) {
				return count / 1000 + "k";
			}
			if (count >= 10000) {
				return count / 10000 + "w";
			}
			return strCount;
		}

		public long getID() {
			return id;
		}

		public long getUID() {
			String uid = main.optString("uid", null);
			return uid == null ? main.optLong("uid", -1) : Long.parseLong(uid);
		}

		public String getUser() {
			return main.optString("username", "棍母");
		}

		public String getTitle() {
			return main.optString("title", "哈姆");
		}

		public String getContent() {
			return main.optString("content", "填词时间...");
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

		public String getLikeCount() {
			return getCount("like_count") + "获赞";
		}

		public String getFavoriteCount() {
			return getCount("favorite_count") + "冷藏";
		}

		public String getViewCount() {
			return getCount("view_count") + "浏览";
		}

		public boolean isLike() {
			return main.optInt("if_like", 0) != 0;
		}

		public boolean isFavorite() {
			return main.optInt("if_favorite", 0) != 0;
		}

		public String getAvatarURI() {
			return main.optString("avatar_url", null);
		}
	}
}

