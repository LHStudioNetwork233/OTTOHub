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
import androidx.viewpager.widget.*;
import androidx.fragment.app.*;
import com.losthiro.ottohubclient.adapter.page.*;
import com.losthiro.ottohubclient.ui.*;
import com.losthiro.ottohubclient.ui.VideoInfoFragment.*;
import com.losthiro.ottohubclient.impl.*;

/**
 * @Author Hiro
 * @Date 2025/05/23 00:54
 */
public class PlayerActivity extends BasicActivity implements VideoInfoFragment.OnRequestVideoListener, ScreenRotate.OrientationChangeListener {
	public static final String TAG = "PlayerActivity";
	private static final Semaphore request = new Semaphore(1);
	private static final Handler mainHandler = new Handler(Looper.getMainLooper());
	private static ClientVideoView main;
	private static IDanmakuView danmaku;
	private Button[] pages;
	private ViewPager videoPage;
	private BottomDialog danmakuDia;
	private long vid;
	private long uid;
	private boolean isLocal;
	private boolean isOnInfo = true;
	private boolean isFullScreen = false;
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
		Intent i = getIntent();
		if (savedInstanceState != null) {
			videoHeight = savedInstanceState.getInt("video_height");
		}
		String root = i.getStringExtra("root_path");
		try {
			String idStr = i.getData().getQueryParameter("vid");
			if (idStr == null) {
				throw new Exception();
			}
			vid = Long.parseLong(idStr);
			//isLocal = false;
		} catch (Exception unuse) {
			vid = i.getLongExtra("vid", 0);
			isLocal = root != null && !root.isEmpty();
		}
		if (vid == 0) {
			return;
		}
        ScreenRotate.getInstance(getApplication()).setOrientationChangeListener(this);
		Bundle callback = new Bundle();
		callback.putParcelable("play_callback", getIntent());
		videoPage = findViewById(R.id.video_player_page);
		videoPage.setAdapter(initPage(vid, root));
		videoPage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
		pages = new Button[2];
		pages[0] = findViewById(R.id.video_detail_info_btn);
		pages[1] = findViewById(R.id.video_detail_comment_btn);
		for (int p = 0; p < 2; p++) {
			if (isLocal) {
				pages[p].setVisibility(View.GONE);
				continue;
			}
			final int index = p;
			pages[p].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (index == videoPage.getCurrentItem()) {
						return;
					}
					videoPage.setCurrentItem(index, true);
					updatePage(index);
				}
			});
		}
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
	}

	@Override
	protected void onPause() {
		super.onPause();
        ScreenRotate.getInstance(this).stop();
		if (danmaku != null && danmaku.isPrepared()) {
			danmaku.pause();
		}
		if (main != null
				&& !ClientSettings.getInstance().getBoolean(ClientSettings.SettingPool.PLAYER_BACKGROUND_PLAY)) {
			main.goOnPlayOnPause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
        ScreenRotate.getInstance(this).start(this);
		if (danmaku != null && danmaku.isPrepared() && danmaku.isPaused()) {
			danmaku.resume();
		}
		if (main != null
				&& !ClientSettings.getInstance().getBoolean(ClientSettings.SettingPool.PLAYER_BACKGROUND_PLAY)) {
			main.goOnPlayOnResume();
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
	}

	@Override
	public void onBackPressed() {
		if (main.backPress()) {
			return;
		}
		if (System.currentTimeMillis() - firstBackTime > 2000) {
			Toast.makeText(this, "再按一次返回键退出播放", Toast.LENGTH_SHORT).show();
			firstBackTime = System.currentTimeMillis();
			if (videoPage == null) {
				return;
			}
			PagerAdapter adapter = videoPage.getAdapter();
			if (adapter == null) {
				return;
			}
			if (adapter instanceof PagesAdapter) {
				Fragment view = ((PagesAdapter) adapter).getItem(videoPage.getCurrentItem());
				if (view instanceof CommentFragment) {
					((CommentFragment) view).onBack();
				}
			}
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
		outState.putInt("video_height", videoHeight);
	}

    @Override
    public void orientationChange(int orientation) {
        if (!ClientSettings.getInstance().getBoolean(ClientSettings.SettingPool.PLAYER_AUTO_FULLSCREEN)) {
            return;
        }
        if (Jzvd.CURRENT_JZVD != null
            && (main.state == Jzvd.STATE_PLAYING || main.state == Jzvd.STATE_PAUSE)
            && main.screen != Jzvd.SCREEN_TINY) {
            if (orientation >= 45 && orientation <= 315 && main.screen == Jzvd.SCREEN_NORMAL) {
                toFullScreen(ScreenRotate.orientationDirection);
            } else if (((orientation >= 0 && orientation < 45) || orientation > 315) && main.screen == Jzvd.SCREEN_FULLSCREEN) {
                toNormalScreen();
            }
        }
    }
    
	@Override
	public void onSuccess(final VideoInfoFragment.VideoInfo current, String title, long id) {
		// TODO: Implement this method
		uid = id;
		main.init(current, title, isLocal);
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
				JSONArray array = null;
				try {
					array = current.getDanmakuList(PlayerActivity.this);
				} catch (JSONException unuse) {
				}
				loadDanmaku(array);
				main.startVideoAfterPreloading();
			}
		});
	}

	private FragmentStatePagerAdapter initPage(long vid, String rootPath) {
		PagesAdapter data = new PagesAdapter(this);
		VideoInfoFragment info;
		if (isLocal) {
			info = VideoInfoFragment.newInstance(rootPath, vid);
		} else {
			info = VideoInfoFragment.newInstance(vid);
		}
		info.setOnRequestVideoListener(this);
		data.addItem(info);
		if (!isLocal) {
			data.addItem(CommentFragment.newInstance(vid, Comment.TYPE_VIDEO));
		}
		return data;
	}

	private void loadDanmaku(JSONArray array) {
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
		if (isLocal && array != null) {
			danmaku.prepare(new ClientDanmakuParser(array), controller);
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
	}

	private void updatePage(int index) {
		for (int i = 0; i < pages.length; i++) {
			pages[i].setTextColor(i == index
					? ResourceUtils.getColor(R.color.colorAccent)
					: ResourceUtils.getColor(R.color.text_color));
		}
	}
    
    private void toFullScreen(float x) {
        if (main != null && main.screen != Jzvd.SCREEN_FULLSCREEN) {
            if ((System.currentTimeMillis() - Jzvd.lastAutoFullscreenTime) > 2000) {
                main.autoFullscreen(x);
                Jzvd.lastAutoFullscreenTime = System.currentTimeMillis();
            }
        }
    }
    
    private void toNormalScreen() {
        if (main != null && main.screen == Jzvd.SCREEN_FULLSCREEN) {
            main.autoQuitFullscreen();
        }
    }

	public void switchDanmaku(View v) {
		if (danmaku != null) {
			if (danmaku.getView().getVisibility() == View.VISIBLE) {
				((Button) v).setTextColor(Color.GRAY);
				danmaku.setVisibility(View.GONE);
				return;
			}
			((Button) v).setTextColor(ResourceUtils.getColor(R.color.colorAccent));
			danmaku.setVisibility(View.VISIBLE);
		}
	}

	public void userDetail(View v) {
		if (isLocal) {
			return;
		}
		Intent i = new Intent(PlayerActivity.this, AccountDetailActivity.class);
		i.putExtra("uid", uid);
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
		if (danmakuDia == null) {
			danmakuDia = new BottomDialog(this, inflate);
		}
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
		main.goOnPlayOnPause();
		danmaku.pause();
		danmakuType = "scroll";
		currentColor = Color.WHITE;
		currentSize = 10;
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
		danmakuDia.show();
		int color = danmakuPicker.getColor();
		String rgb = StringUtils.convertToRGB(color).toUpperCase(Locale.getDefault());
		pickerHex.setText(StringUtils.strCat("#", rgb));
		pickerHex.setTextColor(color);
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
		String uri = APIManager.DanmakuURI.getSendURI(vid, current.getToken(), danmakuContent, pos, danmakuType, rgb,
				currentSize + "px", render);
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
						final BaseDanmaku data = controller.mDanmakuFactory.createDanmaku(type);
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
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO: Implement this method
								danmaku.addDanmaku(data);
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
			public void onFailed(final String cause) {
				// TODO: Implement this method
				Log.e("Network", cause);
			}
		});
		danmaku.resume();
		main.goOnPlayOnResume();
	}

	public void sendComment(View v) {
		PagesAdapter adapter = (PagesAdapter) videoPage.getAdapter();
		if (adapter == null) {
			return;
		}
		Fragment current = adapter.getItem(videoPage.getCurrentItem());
		if (current instanceof CommentFragment) {
			((CommentFragment) current).sendComment();
		}
	}

	public void downloadVideo(View v) {
		PagesAdapter adapter = (PagesAdapter) videoPage.getAdapter();
		if (adapter == null) {
			return;
		}
		Fragment current = adapter.getItem(videoPage.getCurrentItem());
		if (current instanceof VideoInfoFragment) {
			((VideoInfoFragment) current).saveVideo();
		}
	}

	public void reportVideo(View v) {
		if (isLocal) {
			return;
		}
		AlertDialog dialog = new AlertDialog.Builder(this).setTitle("确认举报？").setMessage("OV" + vid)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dia, int which) {
						reportCurrent();
						dia.dismiss();
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
			NetworkUtils.getNetwork.getNetworkJson(APIManager.ModerationURI.getReportVideoURI(vid, token),
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
}

