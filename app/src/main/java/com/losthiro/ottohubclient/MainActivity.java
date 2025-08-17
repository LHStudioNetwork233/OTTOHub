package com.losthiro.ottohubclient;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.losthiro.ottohubclient.adapter.model.Account;
import com.losthiro.ottohubclient.adapter.model.Video;
import com.losthiro.ottohubclient.adapter.VideoAdapter;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.FileUtils;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import com.losthiro.ottohubclient.utils.SystemUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.losthiro.ottohubclient.view.ClientDrawerLayout;
import android.view.LayoutInflater;
import android.graphics.drawable.ColorDrawable;
import androidx.drawerlayout.widget.DrawerLayout;
import android.widget.ListView;
import com.losthiro.ottohubclient.adapter.menu.ImageAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Adapter;
import com.losthiro.ottohubclient.adapter.menu.ImageItem;
import android.graphics.drawable.Drawable;
import android.graphics.BitmapFactory;
import com.losthiro.ottohubclient.impl.danmaku.DefDanmakuManager;
import androidx.viewpager.widget.ViewPager;
import com.losthiro.ottohubclient.adapter.page.PopularAdapter;
import com.losthiro.ottohubclient.view.drawer.SlideDrawerManager;
import android.graphics.drawable.GradientDrawable;
import com.losthiro.ottohubclient.utils.StringUtils;
import com.losthiro.ottohubclient.view.window.AccountSwitchWindow;
import android.content.SharedPreferences;
import com.losthiro.ottohubclient.impl.PermissionHelper;
import com.losthiro.ottohubclient.view.window.*;
import android.widget.*;
import java.util.*;
import android.view.*;
import com.losthiro.ottohubclient.utils.*;
import com.losthiro.ottohubclient.ui.*;
import com.losthiro.ottohubclient.adapter.page.*;
import android.graphics.*;
import androidx.fragment.app.*;
import pl.droidsonroids.gif.GifImageView;
import com.losthiro.ottohubclient.impl.*;

/**
 * @Author Hiro
 * @Date 2025/05/21 15:31
 */
public class MainActivity extends BasicActivity {
	public static final String TAG = "VideosActivity";
	public static WeakReference<BasicActivity> activity;
	private static final Handler main = new Handler(Looper.getMainLooper());
	private long firstBackTime;
	private ViewPager mainPage;
	private TextView[] pages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		activity = new WeakReference<>(this);
		mainPage = findViewById(R.id.main_pager);
		mainPage.setAdapter(initPager());
		mainPage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
		if (!FileUtils.isStorageAvailable()) {
			Toast.makeText(getApplication(), "你的内存不够保存东西了", Toast.LENGTH_SHORT).show();
		}
		SlideDrawerManager.getInstance().saveLastParent(findViewById(android.R.id.content));
		DefDanmakuManager.getInstance(this);
		requestPreDialog(this);
		initPageView();
        initSettings(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AccountManager.getInstance(getApplicationContext()).saveAccounts();
		main.removeCallbacksAndMessages(null);
	}

	@Override
	protected void onStop() {
		super.onStop();
		AccountManager.getInstance(getApplicationContext()).saveAccounts();
	}

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - firstBackTime > 2000) {
			Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
			firstBackTime = System.currentTimeMillis();
			return;
		}
		super.onBackPressed();
		finishAffinity();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO: Implement this method
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == BasicActivity.LOGIN_REQUEST_CODE && resultCode == BasicActivity.RESULT_OK && data != null) {
			String password = data.getStringExtra("password");
			String content = data.getStringExtra("content");
			String token = data.getStringExtra("token");
			try {
				Account a = new Account(this, new JSONObject(content), token);
				a.setCurrent(true);
				AccountManager.getInstance(this).login(a, password);
				PagesAdapter adapter = (PagesAdapter) mainPage.getAdapter();
				Fragment page = adapter.getItem(mainPage.getCurrentItem());
				if (page instanceof VideosFragment) {
					((VideosFragment) page).AccountCallback(a);
				}
				if (page instanceof BlogsFragment) {
					((BlogsFragment) page).AccountCallback(a);
				}
				adapter.addPage(AccountFragment.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		Client.saveActivity(getIntent());
		super.startActivityForResult(intent, requestCode);
	}

	@Override
	public void startActivity(Intent intent) {
		Client.saveActivity(getIntent());
		super.startActivity(intent);
	}

	private FragmentPagerAdapter initPager() {
		List<Fragment> pages = new ArrayList<>();
		pages.add(VideosFragment.newInstance());
		pages.add(BlogsFragment.newInstance());
		AccountManager manager = AccountManager.getInstance(this);
		if (manager.isLogin()) {
			pages.add(AccountFragment.newInstance());
		}
		return new PagesAdapter(getSupportFragmentManager(), pages);
	}

	private void initPageView() {
		int pageCount = 3;
		int currentPage = mainPage.getCurrentItem();
		int colorAccent = ResourceUtils.getColor(R.color.colorAccent);
		int color = ResourceUtils.getColor(R.color.colorSecondary);
		View parent = findViewById(android.R.id.content);
		pages = new TextView[pageCount];
		pages[0] = parent.findViewWithTag("main");
		pages[1] = parent.findViewWithTag("blog");
		pages[2] = parent.findViewWithTag("account");
		for (int i = 0; i < pageCount; i++) {
			pages[i].setTextColor(i == currentPage ? colorAccent : color);
			Drawable icon = pages[i].getCompoundDrawables()[1];
			if (icon != null) {
				icon.setColorFilter(i == currentPage ? colorAccent : color, PorterDuff.Mode.SRC_IN);
			}
			final int index = i;
			pages[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO: Implement this method
					int currentPage = mainPage.getCurrentItem();
					if (index == currentPage) {
						return;
					}
					AccountManager manager = AccountManager.getInstance(getApplication());
					if (index == 2 && !manager.isLogin()) {
						startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), LOGIN_REQUEST_CODE);
						return;
					}
					updatePage(index);
					mainPage.setCurrentItem(index, true);
				}
			});
		}
	}
    
    private static void initSettings(Context c){
        ClientSettings settings = ClientSettings.getInstance();
        try {
            settings.register(c);
        } catch (Exception e) {
            Log.e(TAG, "setting register failed", e);
        }
    }

	private void updatePage(int index) {
		int colorAccent = ResourceUtils.getColor(R.color.colorAccent);
		int color = ResourceUtils.getColor(R.color.colorSecondary);
		for (int i = 0; i < 3; i++) {
			pages[i].setTextColor(index == i ? colorAccent : color);
			Drawable icon = pages[i].getCompoundDrawables()[1];
			if (icon != null) {
				icon.setColorFilter(index == i ? colorAccent : color, PorterDuff.Mode.SRC_IN);
			}
		}
	}

	public void messageCallback(String token) {
		Fragment view = ((PagesAdapter) mainPage.getAdapter()).getItem(VideosFragment.TAG);
		if (view instanceof VideosFragment) {
			((VideosFragment) view).initMessageView(token);
		}
	}

	public void switchAccountDia(View v) {
		SlideDrawerManager.getInstance().showAccountSwitch(v);
	}

	public void addAccount(View v) {
		startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST_CODE);
	}

	public void lastPopular(View v) {
		Fragment view = ((PagesAdapter) mainPage.getAdapter()).getItem(VideosFragment.TAG);
		if (view instanceof VideosFragment) {
			((VideosFragment) view).lastPopular();
		}
	}

	public void nextPopular(View v) {
		Fragment view = ((PagesAdapter) mainPage.getAdapter()).getItem(VideosFragment.TAG);
		if (view instanceof VideosFragment) {
			((VideosFragment) view).nextPopular();
		}
	}

	public void searchVideo(View v) {
		startActivity(new Intent(this, SearchActivity.class));
	}

	public void upload(View v) {
		final HashMap<String, Class<?>> map = new HashMap<>();
		map.put("上传视频", UploadVideoActivity.class);
		map.put("发布动态", UploadBlogActivity.class);
		List<String> names = new ArrayList<>();
		for (String name : map.keySet()) {
			names.add(name);
		}
		ArrayAdapter<String> name = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
		ListView view = new ListView(this);
		view.setLayoutParams(
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		view.setAdapter(name);
		view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO: Implement this method
				String current = (String) parent.getItemAtPosition(position);
				Intent i = new Intent(MainActivity.this, map.get(current));
				startActivity(i);

			}
		});
		PopupWindow window = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, true);
		window.setTouchable(true);
		window.setBackgroundDrawable(new ColorDrawable(ResourceUtils.getColor(R.color.colorMain)));
		window.showAsDropDown(v, 0, view.getHeight());
	}

	public void messageDetail(View v) {
		if (!AccountManager.getInstance(this).isLogin()) {
			startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST_CODE);
			return;
		}
		startActivity(new Intent(this, MessageActivity.class));
	}

	public static void requestPreDialog(final Activity a) {
		SharedPreferences sharedPreferences = a.getSharedPreferences("Settings", 0);
		if (new Boolean(sharedPreferences.getBoolean("First", true)).booleanValue()) {
			sharedPreferences.edit().putBoolean("First", false).commit();
			AlertDialog.Builder dialog = new AlertDialog.Builder(a);
			dialog.setTitle("权限请求");
			dialog.setMessage("或许需要一些权限，以便我们能够保存您的设置");
			dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dia, int which) {
					PermissionHelper.requestPermissions(a,
							new String[]{"android.permission.WRITE_EXTERNAL_STORAGE",
									"android.permission.READ_EXTERNAL_STORAGE"},
							new PermissionHelper.PermissionCallback() {
								@Override
								public void onAllGranted() {
									Toast.makeText(a, "权限授予成功", Toast.LENGTH_SHORT).show();
                                    initSettings(a);
								}

								@Override
								public void onDeniedWithNeverAsk() {
									Toast.makeText(a, "权限已拒绝(后续可在设置重新授予)", Toast.LENGTH_SHORT).show();
								}
							});
				}
			});
			dialog.setNegativeButton(android.R.string.cancel, null);
			dialog.create().show();
		}
	}
}

