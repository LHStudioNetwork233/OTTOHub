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
import com.losthiro.ottohubclient.adapter.Account;
import com.losthiro.ottohubclient.adapter.Video;
import com.losthiro.ottohubclient.adapter.VideoAdapter;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.impl.TypeManager;
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
import com.losthiro.ottohubclient.adapter.PopularAdapter;
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
import android.app.*;

/**
 * @Author Hiro
 * @Date 2025/05/21 15:31
 */
public class VideosActivity extends MainActivity {
    public static final String TAG = "VideosActivity";
    public static WeakReference<MainActivity> activity;
    private static final List<Video> videos=new ArrayList<>();
    private static final Handler main=new Handler(Looper.getMainLooper());
    private static final Semaphore request=new Semaphore(1);
    private static ImageView userMain;
    private static TextView countView;
    private long firstBackTime;
    private boolean isAuto=true;
    private int categoryIndex=0;
    private TextView[] categorys;
    private RecyclerView videoList;
    private LinearLayout onloadView;
    private SwipeRefreshLayout videoRefresh;
    private VideoAdapter adapter;
    private ViewPager popularList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = new WeakReference<>(this);
        findViewById(android.R.id.content);
        popularList = findViewById(R.id.popular_list);
        onloadView = findViewById(R.id.more_onload);
        countView = findViewById(R.id.main_message_count);
        
        GridLayoutManager layout=new GridLayoutManager(this, 2);
        layout.setInitialPrefetchItemCount(6);
        layout.setItemPrefetchEnabled(true);
        videoList = findViewById(R.id.videos_list);
        videoList.setLayoutManager(layout);
        videoList.setItemViewCacheSize(20);
        videoList.setDrawingCacheEnabled(true);
        videoList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        //videoList.addView(getLayoutInflater().inflate(R.layout.header_popular, null));
        videoList.addOnScrollListener(new RecyclerView.OnScrollListener(){
                @Override
                public void onScrollStateChanged(RecyclerView view, int state) {
                    super.onScrollStateChanged(view, state);
                    if (state == RecyclerView.SCROLL_STATE_IDLE) {
                        int itemCount = view.getLayoutManager().getItemCount();
                        int lastPos = ((LinearLayoutManager)view.getLayoutManager()).findLastVisibleItemPosition();
                        if (lastPos >= itemCount - 1) {
                            onloadView.setVisibility(View.VISIBLE);
                            requestCategory(false);
                        }
                    }
                }
            });
        videoRefresh = findViewById(R.id.video_refresh);
        videoRefresh.setRefreshing(true);
        videoRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
                @Override
                public void onRefresh() {
                    int topPos = popularList.getTop();
                    if (topPos <= 0) {
                        Toast.makeText(getApplication(), "走位中...", Toast.LENGTH_SHORT).show();
                        requestCategory(true);
                    }
                }
            });
        userMain = findViewById(R.id.main_user_avatar);
        userMain.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    AccountManager manager=AccountManager.getInstance(getApplication());
                    if (manager.isLogin()) {
                        Intent i=new Intent(VideosActivity.this, AccountDetailActivity.class);
                        i.putExtra("uid", manager.getAccount().getUID());
                        startActivity(i);
                    } else {
                        startActivityForResult(new Intent(VideosActivity.this, LoginActivity.class), LOGIN_REQUEST_CODE);
                    }
                }
            });
        AccountManager manager=AccountManager.getInstance(this);
        if (manager.isLogin()) {
            String uri=APIManager.MessageURI.getNewMessageURI(manager.getAccount().getToken());
            ImageDownloader.loader(userMain, manager.getAccount().getAvatarURI());
            initMessageView(uri);
        } else if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(getApplication(), "那我缺的网络这一块", Toast.LENGTH_SHORT).show();
        } else {
            manager.resetLogin();
        }
        TypeManager.initTypes(findViewById(android.R.id.content));
        final Context ctx=getApplication();
        if (!FileUtils.isStorageAvailable()) {
            Toast.makeText(ctx, "你的内存不够保存东西了", Toast.LENGTH_SHORT).show();
        }
        main.postDelayed(new Runnable(){
                @Override
                public void run() {
                    requestCategory(true);
                }
            }, 1000L);
        SlideDrawerManager.getInstance().registerDrawer(findViewById(android.R.id.content));
        DefDanmakuManager.getInstance(this);
        initPopularList();
        initCategoryView();
        requestPreDialog(this);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: Implement this method
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String password=data.getStringExtra("password");
            String content=data.getStringExtra("content");
            String token=data.getStringExtra("token");
            try {
                Account a=new Account(this, new JSONObject(content), token);
                AccountManager.getInstance(this).login(a, password);
                SlideDrawerManager.getInstance().registerDrawer(findViewById(android.R.id.content));
                ImageDownloader.loader(userMain, a.getAvatarURI());
                initMessageView(a.getToken());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        SlideDrawerManager.getInstance().saveLastParent(findViewById(android.R.id.content));
        Client.saveActivity(getIntent());
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startActivity(Intent intent) {
        SlideDrawerManager.getInstance().saveLastParent(findViewById(android.R.id.content));
        Client.saveActivity(getIntent());
        super.startActivity(intent);
    }

    public static void initMessageView(String token) {
        NetworkUtils.getNetwork.getNetworkJson(APIManager.MessageURI.getNewMessageURI(token), new NetworkUtils.HTTPCallback(){
                @Override
                public void onSuccess(String content) {
                    if (content == null || content.isEmpty()) {
                        onFailed("content is empty!");
                        return;
                    }
                    try {
                        JSONObject data=new JSONObject(content);
                        if (data.optString("status", "error").equals("success")) {
                            String num=data.optString("new_message_num", null);
                            final int msgCount=num == null ?data.optInt("new_message_num", 0): Integer.parseInt(num);
                            main.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        GradientDrawable bg=new GradientDrawable();
                                        bg.setColor(Color.RED);
                                        bg.setShape(GradientDrawable.OVAL);
                                        bg.setCornerRadius(20);
                                        countView.setText(msgCount > 99 ?"99+": StringUtils.toStr(msgCount));
                                        countView.setBackground(bg);
                                        countView.setVisibility(msgCount > 0 ?View.VISIBLE: View.GONE);
                                    }
                                });
                        }
                    } catch (JSONException e) {
                        onFailed(e.toString());
                    }
                }

                @Override
                public void onFailed(String cause) {
                    Log.e(TAG, cause);
                }
            });
    }

    private void initPopularList() {
        NetworkUtils.getNetwork.getNetworkJson(APIManager.VideoURI.getPopularVideoURI(7, 0, 5), new NetworkUtils.HTTPCallback(){
                @Override
                public void onSuccess(String content) {
                    if (content == null || content.isEmpty()) {
                        onFailed("empty content");
                        return;
                    }
                    try {
                        JSONObject json=new JSONObject(content);
                        if (json == null) {
                            onFailed("null json");
                            return;
                        }
                        String status=json.optString("status", "error");
                        if (status.equals("success")) {
                            JSONArray video=json.optJSONArray("video_list");
                            final List<Video> data=new ArrayList<>();
                            for (int i=0;i < video.length();i++) {
                                data.add(new Video(getApplication(), video.optJSONObject(i), Video.VIDEO_DEF));
                            }
                            main.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        PopularAdapter popular = new PopularAdapter(VideosActivity.this, data);
                                        popularList.setAdapter(popular);
                                        updatePopular();
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

    private void updatePopular() {
        final TextView currentTitle=findViewById(R.id.list_popular_title);
        popularList.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
                @Override
                public void onPageScrolled(int p, float p1, int p2) {
                    PopularAdapter popular=(PopularAdapter)popularList.getAdapter();
                    currentTitle.setText(popular.get(p).getTitle());
                }

                @Override
                public void onPageScrollStateChanged(int p) {
                }

                @Override
                public void onPageSelected(int p) {
                }
            });
        main.post(new Runnable(){
                @Override
                public void run() {
                    if (isAuto) {
                        nextPopular();
                        main.postDelayed(this, 5000L);
                    } else {
                        main.removeCallbacks(this);
                    }
                }
            });
    }

    private void lastPopular() {
        PopularAdapter popular=(PopularAdapter)popularList.getAdapter();
        int max=popular.getCount();
        int current=popularList.getCurrentItem();
        current--;
        popularList.setCurrentItem((current + max) % max, true);
    }

    private void nextPopular() {
        PopularAdapter popular=(PopularAdapter)popularList.getAdapter();
        int max=popular.getCount();
        int current=popularList.getCurrentItem();
        current++;
        popularList.setCurrentItem(current % max, true);
    }

    public void switchAccountDia(View v) {
        SlideDrawerManager.getInstance().showAccountSwitch(v);
    }

    public void addAccount(View v) {
        startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST_CODE);
    }

    public void lastPopular(View v) {
        lastPopular();
    }

    public void nextPopular(View v) {
        nextPopular();
    }

    public void searchVideo(View v) {
        startActivity(new Intent(this, SearchActivity.class));
    }

    public void upload(View v) {
        final HashMap<String, Class<?>> map=new HashMap<>();
        map.put("上传视频", UploadVideoActivity.class);
        map.put("发布动态", UploadBlogActivity.class);
        List<String> names=new ArrayList<>();
        for(String name: map.keySet()){
            names.add(name);
        }
        ArrayAdapter<String> name=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        ListView view=new ListView(this);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setAdapter(name);
        view.setOnItemClickListener(new OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // TODO: Implement this method
                    String current = (String)parent.getItemAtPosition(position);
                    Intent i = new Intent(VideosActivity.this, map.get(current));
                    startActivity(i);
                    
                }
            });
        PopupWindow window=new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        window.setTouchable(true);
        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        window.showAsDropDown(v, 0, view.getHeight());
    }

    public void messageDetail(View v) {
        if (!AccountManager.getInstance(this).isLogin()) {
            startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST_CODE);
            return;
        }
        startActivity(new Intent(this, MessageActivity.class));
    }

    private void initCategoryView() {
        View contentView = findViewById(android.R.id.content);
        final int categoryCount=13;
        categorys = new TextView[categoryCount];
        categorys[0] = contentView.findViewWithTag("recommend");
        categorys[1] = contentView.findViewWithTag("new");
        categorys[2] = contentView.findViewWithTag("week");
        categorys[3] = contentView.findViewWithTag("month");
        categorys[4] = contentView.findViewWithTag("quarterly");
        categorys[5] = contentView.findViewWithTag("1");
        categorys[6] = contentView.findViewWithTag("2");
        categorys[7] = contentView.findViewWithTag("3");
        categorys[8] = contentView.findViewWithTag("4");
        categorys[9] = contentView.findViewWithTag("5");
        categorys[10] = contentView.findViewWithTag("6");
        categorys[11] = contentView.findViewWithTag("7");
        categorys[12] = contentView.findViewWithTag("0");
        for (int i = 0; i < categoryCount; i++) {
            final int index=i;//防止越权
            categorys[i].setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        if (categoryIndex == index) {
                            return;
                        }
                        for (int i = 0; i < categoryCount; i++) {
                            TextView current = categorys[i];
                            current.setBackgroundResource(i == index ?R.drawable.btn_bg: R.drawable.btn_empty_bg);
                            current.setTextColor(i == index ?Color.WHITE: Color.BLACK);
                        }
                        //((View)popularList.getParent()).setVisibility(categoryIndex > 0 ?View.GONE: View.VISIBLE);
                        requestCategory(true);
                        categoryIndex = index;
                    }
                });
        }
    }

    private void requestCategory(boolean isRefresh) {
        String uri=APIManager.VideoURI.getRandomVideoURI(12);
        int index=categoryIndex;
        if (index == 1) {
            uri = APIManager.VideoURI.getNewVideoURI(0, 12);
        }
        if (index >= 2 && index < 5) {
            int mode=-1;
            if (index == 2) {
                mode = APIManager.VideoURI.WEEK;
            }
            if (index == 3) {
                mode = APIManager.VideoURI.MONTH;
            }
            if (index == 4) {
                mode = APIManager.VideoURI.QUARTERLY;
            }
            uri = APIManager.VideoURI.getPopularVideoURI(mode, 0, 12);
        }
        if (index >= 5 && index < categorys.length - 1) {
            uri = APIManager.VideoURI.getCategoryVideoURI(index - 4, 12);
        }
        if (index == categorys.length - 1) {
            uri = APIManager.VideoURI.getCategoryVideoURI(APIManager.VideoURI.CATEGORY_OTHER, 12);
        }
        videoRefresh.setRefreshing(true);
        request(uri, isRefresh);
    }

    private void request(String uri, final boolean isRefresh) {
        try {
            if (!request.tryAcquire()) {
                return;
            }
            if (isRefresh) {
                videos.clear();
            }
            NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback(){
                    @Override
                    public void onSuccess(String content) {
                        if (content == null || content.isEmpty()) {
                            onFailed("empty content");
                            return;
                        }
                        try {
                            JSONObject json=new JSONObject(content);
                            if (json == null) {
                                onFailed("null json");
                                return;
                            }
                            String status=json.optString("status", "error");
                            if (status.equals("success")) {
                                JSONArray video=json.optJSONArray("video_list");
                                final List<Video> data=new ArrayList<>();
                                for (int i=0;i < video.length();i++) {
                                    data.add(new Video(getApplication(), video.optJSONObject(i), Video.VIDEO_DEF));
                                }
                                main.post(new Runnable(){
                                        @Override
                                        public void run() {
                                            if (videos.isEmpty() || adapter == null) {
                                                videos.addAll(data);
                                                adapter = new VideoAdapter(VideosActivity.this, videos);
                                                videoList.setAdapter(adapter);
                                                return;
                                            }
                                            if (isRefresh) {
                                                videos.addAll(data);
                                                adapter.notifyDataSetChanged();
                                            } else {
                                                onloadView.setVisibility(View.GONE);
                                                adapter.addNewData(data);
                                            }
                                        }
                                    });
                                videoRefresh.setRefreshing(false);
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
                        videoRefresh.setRefreshing(false);
                    }
                });
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } finally {
            request.release();
        }
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
                        PermissionHelper.requestPermissions(a, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, new PermissionHelper.PermissionCallback(){
                                @Override
                                public void onAllGranted() {
                                    Toast.makeText(a, "权限授予成功", Toast.LENGTH_SHORT).show();
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
