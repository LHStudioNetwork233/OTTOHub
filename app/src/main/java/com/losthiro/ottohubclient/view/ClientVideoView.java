/**
 * @Author Hiro
 * @Date 2025/09/08 15:02
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.view;
import cn.jzvd.JzvdStd;
import cn.jzvd.Jzvd;
import android.content.*;
import android.util.AttributeSet;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.ui.*;
import com.losthiro.ottohubclient.R;
import android.widget.*;
import android.content.pm.*;
import android.graphics.drawable.*;
import com.losthiro.ottohubclient.utils.*;
import android.graphics.*;
import android.content.res.*;
import master.flame.danmaku.danmaku.model.android.*;
import com.losthiro.ottohubclient.impl.danmaku.*;
import java.util.*;
import master.flame.danmaku.danmaku.model.*;
import master.flame.danmaku.ui.widget.DanmakuView;
import master.flame.danmaku.controller.*;
import android.view.*;
import cn.jzvd.*;
import android.webkit.*;
import android.view.GestureDetector.*;
import android.view.View.*;

public class ClientVideoView extends JzvdStd implements PopupWindow.OnDismissListener, DrawHandler.Callback {
	public static final int MODE_PAUSE = 0; //播完暂停(无合集视频暂时是这个类型)
	public static final int MODE_AUTO = 1; //顺序播放
	public static final int MODE_LOOP = 2; //列表循环
	public static final int MODE_RESTART = 3; //单视频循环
	public static final int MODE_RANDOM = 4; //随机播放

	public static final float MAX_SPEED = 2.0f;
	public static final float MIN_SPEED = 0.5f;

	private static final float[] speedList = {0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f};
	//private static final float[] startTouchPoint = new float[2];

	public TextView mSpeedText;
	public TextView mTopLongClickText;
	public TextView mVideoShare;
	public ImageView mLockScreenBtn;
	public ImageView mLastVideoBtn;
	public ImageView mNextVideoBtn;
	public DanmakuView mDanmakuView;
	public PopupWindow mDanmakuInfo;
	private DanmakuContext controller;
	private ClientDanmakuParser danmakuParser;
	private VideoListListener collectionListener;
	private DanmakuListener danmakuListener;
	private GestureDetector gesture;
	private String mScriptTag = "ClientVideoBridge";
	private boolean isLocal;
	private boolean isPosterShow;
	private boolean isAutoQuit;
	private boolean isLockScreen;
	private boolean isLongClick;
	private float currentSpeed;
	private int themeColor;
	private int playMode;
	private int count;
	private int index;
	private int speedIndex = 2;

	public ClientVideoView(Context context) {
		super(context);
	}

	public ClientVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public int getLayoutId() {
		// TODO: Implement this method
		return R.layout.view_video_player;
	}

	@Override
	public void init(Context context) {
		// TODO: Implement this method
		super.init(context);
		setFocusable(true);
		setClickable(true);
		setLongClickable(true);
		mSpeedText = findViewById(R.id.video_speed);
		mSpeedText.setOnClickListener(this);
		mLockScreenBtn = findViewById(R.id.lock_screen);
		mLockScreenBtn.setOnClickListener(this);
		mLastVideoBtn = findViewById(R.id.last);
		mLastVideoBtn.setOnClickListener(this);
		mNextVideoBtn = findViewById(R.id.next);
		mNextVideoBtn.setOnClickListener(this);
		mDanmakuInfo = new PopupWindow(LayoutInflater.from(context).inflate(R.layout.dialog_danmaku_info, this, false),
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
		mDanmakuInfo.setTouchable(true);
		mDanmakuInfo.setOnDismissListener(this);
		mDanmakuView = findViewById(R.id.main_danmaku);
		HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
		maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_LR, 5);
		HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
		overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_LR, false);
		overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
		controller = DanmakuContext.create();
		controller.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3);
		controller.setDuplicateMergingEnabled(true);
		controller.setScrollSpeedFactor(1.2f);
		controller.setScaleTextSize(1.2f);
		controller.setMaximumLines(maxLinesPair);
		controller.preventOverlapping(overlappingEnablePair);
		controller.setDanmakuMargin(40);
		mDanmakuView.setCallback(this);
		mDanmakuView.enableDanmakuDrawingCache(true);
		mTopLongClickText = findViewById(R.id.long_click_view);
		mVideoShare = findViewById(R.id.video_share);
		gesture = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				// TODO: Implement this method
				boolean value = false;
				if (isLockScreen || screen != SCREEN_FULLSCREEN) {
					return value;
				}
				long currentPosition = getCurrentPositionWhenPlaying();
				if (e.getX() < getWidth() / 5) {
					//快退（15S）
					long quickRetreatProgress = currentPosition - 15 * 1000;
					if (quickRetreatProgress > 0) {
						mediaInterface.seekTo(quickRetreatProgress);
					} else {
						mediaInterface.seekTo(0);
					}
					value = true;
				} else if (e.getX() > (getWidth() / 5) * 4) {
					long duration = getDuration();
					//快进（15S）
					long fastForwardProgress = currentPosition + 15 * 1000;
					if (duration > fastForwardProgress) {
						mediaInterface.seekTo(fastForwardProgress);
					} else {
						mediaInterface.seekTo(duration);
					}
					value = true;
				}
				return value;
			}

			@Override
			public boolean onSingleTapConfirmed(final MotionEvent e) {
				//手动实现弹幕点击事件
				//因为弹幕烈焰使的弹幕视图好像拦截了父视图的点击事件
				//直接设置弹幕视图点击事件会有问题
				boolean value = false;
				if ((isLockScreen || screen != SCREEN_FULLSCREEN)
						&& (mDanmakuView == null || danmakuListener == null)) {
					//不进行拦截的条件
					return value;
				}
				IDanmakus danmakuList = mDanmakuView.getCurrentVisibleDanmakus();
				if (danmakuList == null) {
					return value;
				}
				if (!danmakuList.isEmpty()) {
					danmakuList.forEachSync(new IDanmakus.DefaultConsumer<BaseDanmaku>() {
						@Override
						public int accept(BaseDanmaku danmaku) {
							if (danmaku != null) {
								Rect bounds = new Rect();
								long danmakuId = danmaku.userId;
								int x = (int) (e.getX() - mDanmakuView.getXOff());
								int y = (int) (e.getY() - mDanmakuView.getYOff());
								int offsetX = (int) (e.getX() + mDanmakuView.getXOff());
								int offsetY = (int) (e.getY() + mDanmakuView.getYOff());
								bounds.set((int) danmaku.getLeft(), (int) danmaku.getTop(), (int) danmaku.getRight(),
										(int) danmaku.getBottom());
								//计算点击的位置是否是弹幕，是什么弹幕
								//同时还要判断一下弹幕ID(API操作需要这个)
								if (bounds.intersect(x, y, offsetX, offsetY) && danmakuId != 0) {
									showDanmakuInfo(danmaku, (int) e.getX(), (int) e.getY());
									//return ACTION_BREAK;
								}
							}
							return ACTION_CONTINUE;
						}
					});
				}
				return value;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				if (isLockScreen || screen != SCREEN_FULLSCREEN) {
					return;
				}
				mTopLongClickText.setVisibility(View.VISIBLE);
				mediaInterface.setSpeed(speedList[speedList.length - 1]);
				isLongClick = true;
			}
		});
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO: Implement this method
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				//startTouchPoint[0] = event.getX();
				//startTouchPoint[1] = event.getY();
				if (isLock()) {
					return true;
				}
				break;
			case MotionEvent.ACTION_MOVE :
				if (isLock()) {
					return true;
				}
				if (mProgressDialog != null || mVolumeDialog != null || mBrightnessDialog != null) {
					loadViewTheme();
				}
				break;
			case MotionEvent.ACTION_UP :
				if (isLock()) {
					if (event.getX() == event.getRawX() || event.getY() == event.getRawY()) {
						startDismissControlViewTimer();
						onClickUiToggle();
						bottomProgressBar.setVisibility(VISIBLE);
					}
					return true;
				}
				if (isLongClick) {
					mTopLongClickText.setVisibility(View.INVISIBLE);
					mediaInterface.setSpeed(speedList[speedIndex]);
					isLongClick = false;
				}
				break;
		}
		if (gesture.onTouchEvent(event)) {
			return true;
		}
		return super.onTouch(v, event);
	}

	@Override
	public void onClickUiToggle() {
		// TODO: Implement this method
		super.onClickUiToggle();
		if (screen == SCREEN_FULLSCREEN) {
			if (!isLockScreen) {
				if (bottomContainer.getVisibility() == View.VISIBLE) {
					mLockScreenBtn.setVisibility(View.VISIBLE);
				} else {
					mLockScreenBtn.setVisibility(View.GONE);
				}
			} else {
				if ((int) mLockScreenBtn.getTag() == 1) {
					bottomProgressBar.setVisibility(GONE);
					if (mLockScreenBtn.getVisibility() == View.GONE) {
						mLockScreenBtn.setVisibility(View.VISIBLE);
					} else {
						mLockScreenBtn.setVisibility(View.GONE);
					}
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO: Implement this method
		switch (v.getId()) {
			case R.id.video_speed ://播放速度切换控件
				speedIndex++;
				speedIndex = speedIndex % speedList.length;
				float now = speedList[speedIndex];
				mediaInterface.setSpeed(now);
				mSpeedText.setText(StringUtils.strCat(StringUtils.toStr(now), "X"));
				jzDataSource.objects[0] = speedIndex;
				break;
			case R.id.lock_screen ://锁屏按钮控件
				if (screen == SCREEN_FULLSCREEN) {
					mLockScreenBtn.setTag(1);
					if (!isLockScreen) {
						isLockScreen = true;
						JZUtils.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
						mLockScreenBtn.setImageResource(R.drawable.ic_lock);
						dissmissControlView();
					} else {
						JZUtils.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
						isLockScreen = false;
						mLockScreenBtn.setImageResource(R.drawable.ic_unlock);
						bottomContainer.setVisibility(VISIBLE);
						bottomProgressBar.setVisibility(GONE);
						topContainer.setVisibility(VISIBLE);
						startButton.setVisibility(VISIBLE);
					}
				}
				break;
			case R.id.last ://上一个视频
				if (collectionListener != null) {
					clickLast();
				}
				break;
			case R.id.next ://下一个视频
				if (collectionListener != null) {
					clickNext();
				}
				break;
			case R.id.danmaku_copy ://复制弹幕文本
				Object tag = v.getTag();
				if (tag != null && tag instanceof CharSequence) {
					String text = tag.toString();
					if (!text.isEmpty()) {
						((ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE)).setText(text);
						Toast.makeText(getContext(), "弹幕复制成功~", Toast.LENGTH_SHORT).show();
						mDanmakuInfo.dismiss();
					}
				}
				break;
			case R.id.danmaku_delete ://删除已发弹幕
				Object delete = v.getTag();
				if (delete != null && delete instanceof Integer) {
					final long id = Integer.valueOf(delete);
					if (id != 0 && danmakuListener != null) {
						mDanmakuView.getCurrentVisibleDanmakus()
								.forEachSync(new IDanmakus.DefaultConsumer<BaseDanmaku>() {
									@Override
									public int accept(BaseDanmaku danmaku) {
										if (danmaku != null && danmaku.userId == id) {
											danmaku.isLive = true;
											mDanmakuView.removeAllLiveDanmakus();
											danmakuListener.onDelete(id);
										}
										return ACTION_CONTINUE;
									}
								});
						mDanmakuInfo.dismiss();
					}
				}
				break;
			case R.id.danmaku_report ://举报弹幕
				Object report = v.getTag();
				if (report != null && report instanceof Integer) {
					long id = Integer.valueOf(report);
					if (id != 0 && danmakuListener != null) {
						danmakuListener.onReport(id);
						mDanmakuInfo.dismiss();
					}
				}
				break;
			default :
				super.onClick(v);//其他控件点击事件交给vdview处理
				break;
		}
	}

	@Override
	public void onVideoSizeChanged(int width, int height) {
		// TODO: Implement this method
		super.onVideoSizeChanged(width, height);
		if (width > 0 && height > 0) {
			if (height > width) {
				Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			} else {
				Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			}
		}
	}

	@Override
	public void onCompletion() {
		// TODO: Implement this method
		boolean hasNext = count - 1 > index;
		boolean hasLast = index > 0;
		switch (playMode) {
			case MODE_PAUSE :
				if (screen == SCREEN_FULLSCREEN && !isAutoQuit) {
					onStateAutoComplete();
				} else {
					super.onCompletion();
				}
				break;
			case MODE_AUTO :
				if (hasNext && collectionListener != null) {
					clickNextDelay();
				} else {
					super.onCompletion();
				}
				break;
			case MODE_LOOP :
				if (collectionListener != null) {
					clickNextDelay();
				} else {
					super.onCompletion();
				}
				break;
			case MODE_RESTART :
				startVideoDelay();
				break;
			case MODE_RANDOM :
				if (collectionListener != null && (hasNext || hasLast)) {
					clickRandomDelay();
				} else {
					startVideoDelay();
				}
				break;
		}
		JZUtils.clearSavedProgress(getContext(), jzDataSource.getCurrentUrl());
		//JZUtils.setRequestedOrientation(getContext(), FULLSCREEN_ORIENTATION);
	}

	@Override
	public void onStatePreparing() {
		// TODO: Implement this method
		super.onStatePreparing();
		if (mDanmakuView.isPrepared()) {
			mDanmakuView.restart();
		}
		if (danmakuParser != null) {
			mDanmakuView.prepare(danmakuParser, controller);
		}
	}

	@Override
	public void onStatePlaying() {
		// TODO: Implement this method
		super.onStatePlaying();
		if (mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
			mDanmakuView.resume();
		}
	}

	@Override
	public void onStatePause() {
		// TODO: Implement this method
		super.onStatePause();
		if (mDanmakuView.isPrepared()) {
			mDanmakuView.pause();
		}
	}

	@Override
	public void onStateError() {
		// TODO: Implement this method
		super.onStateError();
		mDanmakuView.release();
	}

	@Override
	public void onStateAutoComplete() {
		// TODO: Implement this method
		super.onStateAutoComplete();
		mDanmakuInfo.dismiss();
		mDanmakuView.stop();
		mDanmakuView.clear();
		mDanmakuView.clearDanmakusOnScreen();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO: Implement this method
		super.onStopTrackingTouch(seekBar);
		mDanmakuView.seekTo(mSeekTimePosition);
	}

	@Override
	public void setScreenNormal() {
		// TODO: Implement this method
		super.setScreenNormal();
		mSpeedText.setVisibility(View.GONE);
		mVideoShare.setVisibility(View.GONE);
		mLockScreenBtn.setVisibility(View.GONE);
	}

	@Override
	public void setScreenFullscreen() {
		// TODO: Implement this method
		super.setScreenFullscreen();
		mSpeedText.setVisibility(View.VISIBLE);
		mVideoShare.setVisibility(View.VISIBLE);
		mLockScreenBtn.setImageResource(R.drawable.ic_unlock);
		mLockScreenBtn.setVisibility(View.VISIBLE);
		if (jzDataSource.objects == null) {
			Object[] object = {2};
			jzDataSource.objects = object;
			speedIndex = 2;
		} else {
			speedIndex = jzDataSource.objects[0];
		}
		if (speedIndex == 2) {
			mSpeedText.setText("倍速");
		} else {
			mSpeedText.setText(StringUtils.strCat(StringUtils.toStr(speedList[speedIndex]), "X"));
		}
	}

	@Override
	public void changeUiToPauseShow() {
		// TODO: Implement this method
		super.changeUiToPauseShow();
		if (isPosterShow) {
			posterImageView.setVisibility(View.VISIBLE);
		}
		if (isLockScreen) {
			bottomContainer.setVisibility(GONE);
			topContainer.setVisibility(GONE);
			startButton.setVisibility(GONE);
			mLastVideoBtn.setVisibility(GONE);
			mNextVideoBtn.setVisibility(GONE);
		}
	}

	@Override
	public void changeUiToPauseClear() {
		// TODO: Implement this method
		super.changeUiToPauseClear();
		if (isPosterShow) {
			posterImageView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void changeUiToPlayingShow() {
		// TODO: Implement this method
		super.changeUiToPlayingShow();
		if (isPosterShow) {
			posterImageView.setVisibility(View.VISIBLE);
		}
		if ((index > 0 || count - 1 > index) && collectionListener != null) {
			mLastVideoBtn.setVisibility(VISIBLE);
			mNextVideoBtn.setVisibility(VISIBLE);
		}
		if (screen == SCREEN_FULLSCREEN) {
			bottomProgressBar.setVisibility(GONE);
			if (isLockScreen) {
				topContainer.setVisibility(GONE);
				bottomContainer.setVisibility(GONE);
				startButton.setVisibility(GONE);
				mLastVideoBtn.setVisibility(GONE);
				mNextVideoBtn.setVisibility(GONE);
			} else {
				topContainer.setVisibility(VISIBLE);
				bottomContainer.setVisibility(VISIBLE);
				startButton.setVisibility(VISIBLE);
			}
		}
	}

	@Override
	public void changeUiToPlayingClear() {
		// TODO: Implement this method
		super.changeUiToPlayingClear();
		if (isPosterShow) {
			posterImageView.setVisibility(View.VISIBLE);
		}
		if (screen == SCREEN_FULLSCREEN) {
			bottomProgressBar.setVisibility(GONE);
			mLockScreenBtn.setVisibility(View.GONE);
			mLastVideoBtn.setVisibility(GONE);
			mNextVideoBtn.setVisibility(GONE);
		}
	}

	@Override
	public void dissmissControlView() {
		// TODO: Implement this method
		super.dissmissControlView();
		post(new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				if (screen == SCREEN_FULLSCREEN) {
					mLockScreenBtn.setVisibility(View.GONE);
					bottomProgressBar.setVisibility(View.GONE);
				}
				mLastVideoBtn.setVisibility(GONE);
				mNextVideoBtn.setVisibility(GONE);
			}
		});
	}

	@Override
	public void onDismiss() {
		// TODO: Implement this method
		if (state == STATE_PAUSE) {
			goOnPlayOnResume();//如果此时videoview是暂停状态，别忘记启用播放
		}
		mDanmakuView.resume();
	}

	@Override
	public void gotoFullscreen() {
		// TODO: Implement this method
		super.gotoFullscreen();
		titleTextView.setVisibility(View.VISIBLE);
	}

	@Override
	public void gotoNormalScreen() {
		// TODO: Implement this method
		super.gotoNormalScreen();
		titleTextView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void prepared() {
		// TODO: Implement this method
		mDanmakuView.start(mSeekTimePosition);
	}

	@Override
	public void updateTimer(DanmakuTimer timer) {
		// TODO: Implement this method

	}

	@Override
	public void danmakuShown(BaseDanmaku danmaku) {
		// TODO: Implement this method
	}

	@Override
	public void drawingFinished() {
		// TODO: Implement this method
	}

	public void init(VideoInfoFragment.VideoInfo video, String title, boolean localMode) {
		if (video == null) {
			return;
		}
		isLocal = localMode;
		init(video.getVideo(), video.getCover(), title);
	}

	public void init(String videoUri, String coverUri, String title) {
		if (title == null) {
			title = "棍母";
		}
		setUp(videoUri, title, SCREEN_NORMAL);
		initView(coverUri);
		startPreloading();
	}

	public void initDanmaku(ClientDanmakuParser parser) {
		danmakuParser = parser;
	}

	public void setDefaultSpeed(float speed) {
		if (speed >= MIN_SPEED && speed <= MAX_SPEED) {
			currentSpeed = speed;
		}
	}

	public void setShowPosterEnable(boolean enabled) {
		isPosterShow = enabled;
	}

	public void setAutoQuit(boolean enabled) {
		isAutoQuit = enabled;
	}

	public void setVideoCollection(int current, int max, VideoListListener listener) {
		index = current;
		count = max;
		collectionListener = listener;
	}

	public void setDanmakuClickable(DanmakuListener listener) {
		danmakuListener = listener;
	}

	public void setThemeColor(int color) {
		themeColor = color;
		loadViewTheme();
	}

	public void setPlayMode(int mode) {
		playMode = mode;
	}

	public void setMuteEnable(boolean enabled) {
		if (mediaInterface != null) {
			float volume = enabled ? 0 : 1;
			mediaInterface.setVolume(volume, volume);
		}
	}

	public void setWebViewBridge(WebView view, String name) {
		mScriptTag = name;
		view.getSettings().setJavaScriptEnabled(true);
		view.addJavascriptInterface(new ViewBridge(this, view), name);
	}

	public void setWebViewBridge(WebView view) {
		setWebViewBridge(view, mScriptTag);
	}

	public void addNewDanmaku(int type, String content, int size, int color) {
		BaseDanmaku data = controller.mDanmakuFactory.createDanmaku(type);
		if (data == null) {
			return;
		}
		data.text = content;
		data.padding = 5;
		data.priority = 1;
		data.setTime(mSeekTimePosition);
        data.userId = (int)mSeekTimePosition;//还没想好，先以当前时间做ID吧
		data.textSize = size * (danmakuParser.getDisplayer().getDensity() - 0.6f);
		data.textColor = color;
		data.textShadowColor = color <= Color.BLACK ? Color.WHITE : Color.BLACK;
		mDanmakuView.addDanmaku(data);
	}

	public void showDanmaku() {
		mDanmakuView.showAndResumeDrawTask(mSeekTimePosition);
	}

	public void hideDanmaku() {
		mDanmakuView.hideAndPauseDrawTask();
	}

	public void release() {
		mDanmakuView.release();
		mDanmakuView = null;
		releaseAllVideos();
	}

	public boolean isShowDanmaku() {
		return mDanmakuView.isShown();
	}

	public String getScriptTag() {
		return mScriptTag;
	}

	protected void clickNext() {
		index++;
		index = index % count;
		collectionListener.onClickIndex(this, index);
	}

	protected void clickLast() {
		index--;
		index = (index + count) % count;
		collectionListener.onClickIndex(this, index);
	}

	private void clickNextDelay() {
		Toast.makeText(getContext(), "五秒钟后播放下一集", Toast.LENGTH_SHORT).show();
		postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				clickNext();
			}
		}, 5000L);
		onStateAutoComplete();
	}

	private void clickRandomDelay() {
		Toast.makeText(getContext(), "五秒钟后继续播放", Toast.LENGTH_SHORT).show();
		postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				int rng = StringUtils.rng(0, count - 1);
				if (rng == index) {
					rng = StringUtils.rng(0, count - 1); //重新取随机数，确保不会重复播放
				}
				collectionListener.onClickIndex(ClientVideoView.this, rng);
				index = rng;
			}
		}, 5000L);
		onStateAutoComplete();
	}

	private void initView(String source) {
		if (source == null) {
			return;
		}
		if (isLocal) {
			Bitmap localMap = BitmapFactory.decodeFile(source);
			if (localMap != null) {
				posterImageView.setImageBitmap(localMap);
			}
		} else {
			ImageDownloader.loader(posterImageView, source);
		}
		titleTextView.setVisibility(View.INVISIBLE);
		posterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		loadViewTheme();
	}

	private void loadViewTheme() {
		if (themeColor == 0) {
			return;
		} //为一些控件修改主题色
		bottomProgressBar.setProgressDrawable(color(bottomProgressBar.getProgressDrawable()));
		progressBar.setProgressDrawable(color(progressBar.getProgressDrawable()));
		if (mProgressDialog != null) {
			ProgressBar progress = mProgressDialog.findViewById(R.id.duration_progressbar);
			progress.setProgressDrawable(color(progress.getProgressDrawable()));
			((TextView) mProgressDialog.findViewById(R.id.tv_current)).setTextColor(themeColor);
		}
		if (mVolumeDialog != null) {
			ProgressBar volume = mVolumeDialog.findViewById(R.id.volume_progressbar);
			volume.setProgressDrawable(color(volume.getProgressDrawable()));
		}
		if (mBrightnessDialog != null) {
			ProgressBar light = mBrightnessDialog.findViewById(R.id.brightness_progressbar);
			light.setProgressDrawable(color(light.getProgressDrawable()));
		}
	}

	private void showDanmakuInfo(BaseDanmaku danmaku, int x, int y) {
		//设置内容
		View root = mDanmakuInfo.getContentView();
		if (root == null) {
			return;
		}
		ImageButton copyBtn = root.findViewById(R.id.danmaku_copy);
		copyBtn.setColorFilter(Color.WHITE);
		copyBtn.setTag(danmaku.text);
		copyBtn.setOnClickListener(this);
		ImageButton deleteBtn = root.findViewById(R.id.danmaku_delete);
		deleteBtn.setColorFilter(Color.WHITE);
		deleteBtn.setTag(danmaku.userId);
		deleteBtn.setOnClickListener(this);
		ImageButton reportBtn = root.findViewById(R.id.danmaku_report);
		reportBtn.setColorFilter(Color.WHITE);
		reportBtn.setTag(danmaku.userId);
		reportBtn.setOnClickListener(this);
		mDanmakuInfo.showAtLocation(mDanmakuView, Gravity.NO_GRAVITY, x, y);
		mDanmakuView.pause();
	}

	private void startVideoDelay() {
		Toast.makeText(getContext(), "五秒后重新播放", Toast.LENGTH_SHORT).show();
		postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				startVideo();
			}
		}, 5000L);//五秒后跳转，可以新增view显示逻辑
		onStateAutoComplete();
	}

	private Drawable color(Drawable progress) {//专门针对progressbar修改颜色
		if (progress instanceof LayerDrawable) {
			Drawable progressView = ((LayerDrawable) progress).findDrawableByLayerId(android.R.id.progress);
			if (progressView != null) {
				progressView.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);
			}
		}
		return progress;
	}

	private boolean isLock() {
		return screen == SCREEN_FULLSCREEN && isLockScreen;
	}

	public static class ViewBridge {
		private ClientVideoView mVideoView;
		private WebView mContainer;

		public ViewBridge(ClientVideoView view, WebView container) {
			mVideoView = view;
			mContainer = container;
		}

		@JavascriptInterface
		public void addVideoViewToHTML(final int width, final int height, final int top, final int left) {//自定义JS代码将该view添加到web
			if (mVideoView == null) {
				return;
			}
			mVideoView.post(new Runnable() {//保证在主线程中
				@Override
				public void run() {
					// TODO: Implement this method
					Context ctx = mVideoView.getContext();
					ViewGroup.LayoutParams ll = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(ll);
					layoutParams.y = JZUtils.dip2px(ctx, top);
					layoutParams.x = JZUtils.dip2px(ctx, left);
					layoutParams.height = JZUtils.dip2px(ctx, height);
					layoutParams.width = JZUtils.dip2px(ctx, width);
					LinearLayout linearLayout = new LinearLayout(ctx);
					linearLayout.addView(mVideoView);
					mContainer.addView(linearLayout, layoutParams);
				}
			});
		}
	}

	public static interface VideoListListener {
		void onClickIndex(ClientVideoView view, int index);
	}

	public static interface DanmakuListener {
		void onDelete(long id);
		void onReport(long id);
	}
}

