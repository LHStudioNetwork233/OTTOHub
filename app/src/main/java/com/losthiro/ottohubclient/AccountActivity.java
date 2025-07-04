package com.losthiro.ottohubclient;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.adapter.Account;
import com.losthiro.ottohubclient.adapter.HonourAdapter;
import com.losthiro.ottohubclient.adapter.Video;
import com.losthiro.ottohubclient.adapter.VideoAdapter;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.impl.ClientString;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.impl.TypeManager;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.losthiro.ottohubclient.impl.APIManager;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.LinearLayout;
import com.losthiro.ottohubclient.view.drawer.SlideDrawerManager;

/**
 * @Author Hiro
 * @Date 2025/05/28 11:06
 */
public class AccountActivity extends MainActivity {
    public static final String TAG = "AccountActivity";
    private static final List<Video> videos=new ArrayList<>();
    private static final Handler main=new Handler(Looper.getMainLooper());
    private static final Semaphore request=new Semaphore(1);
    private LinearLayout onloadView;
    private Account current;
    private RecyclerView historyView;
    private SwipeRefreshLayout historyRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        View parent=findViewById(android.R.id.content);
        TypeManager.initTypes(parent);
        SlideDrawerManager.getInstance().registerDrawer(parent);
        AccountManager manager=AccountManager.getInstance(this);
        current = manager.getAccount();
        initView();
    }

    @Override
    public void onBackPressed() {
        SlideDrawerManager manager = SlideDrawerManager.getInstance();
        manager.registerDrawer(manager.getLastParent());
        TypeManager.runTypeInstance(0);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        SlideDrawerManager manager = SlideDrawerManager.getInstance();
        manager.registerDrawer(manager.getLastParent());
        super.onDestroy();
    }
    
    @Override
    protected void onActivityResult(int who, int targetFragment, Intent requestCode) {
        super.onActivityResult(who, targetFragment, requestCode);
        if (who == LOGIN_REQUEST_CODE) {
            String password=requestCode.getStringExtra("password");
            String content=requestCode.getStringExtra("content");
            String token=requestCode.getStringExtra("token");
            try {
                Account a=new Account(this, new JSONObject(content), token);
                AccountManager.getInstance(this).login(a, password);
                SlideDrawerManager.getInstance().registerDrawer(findViewById(android.R.id.content));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initView() {
        ImageDownloader.loader((ImageView)findViewById(R.id.main_user_cover), current.getCoverURI());
        ImageDownloader.loader((ImageView)findViewById(R.id.main_user_avatar), current.getAvatarURI());
        TextView userName= findViewById(R.id.main_user_name);
        userName.setText(current.getName());
        ((TextView)findViewById(R.id.main_user_info)).setText(StringUtils.strCat(new Object[]{"UID: ", current.getUID(), " 性别: ", current.getSex(), " 注册日: ", current.getTime()}));
        View parent=(View)userName.getParent();
        parent.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(AccountActivity.this, AccountDetailActivity.class);
                    i.putExtra("uid", current.getUID());
                    i.putExtra("avatar", current.getAvatarURI());
                    startActivity(i);
                }
            });
        HashMap<String, Integer> levelMap=current.getLevel();
        GradientDrawable bg=new GradientDrawable();
        bg.setCornerRadius(8f);
        TextView levelText=findViewById(R.id.main_user_level);
        for (HashMap.Entry<String, Integer> entry: levelMap.entrySet()) {
            levelText.setText(entry.getKey());
            bg.setColor(entry.getValue());
        }
        levelText.setBackgroundDrawable(bg);
        HonourAdapter adapter = new HonourAdapter(this, Arrays.asList(current.getHonours()));
        adapter.setHiddenDef(false);
        RecyclerView honourList=findViewById(R.id.main_user_honours);
        honourList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        honourList.setAdapter(adapter);
        String videoCount=StringUtils.strCat(new Object[]{current.getVideoCount(), System.lineSeparator(), "视频"});
        String blogCount=StringUtils.strCat(new Object[]{current.getBlogCount(), System.lineSeparator(), "动态"});
        String followingCount=StringUtils.strCat(new Object[]{current.getFollowingCount(), System.lineSeparator(), "关注"});
        String fansCount=StringUtils.strCat(new Object[]{current.getFansCount(), System.lineSeparator(), "粉丝"});
        new ClientString(videoCount).colorTo((TextView)findViewById(R.id.main_user_video_count), 0xff88d9fa);
        new ClientString(blogCount).colorTo((TextView)findViewById(R.id.main_user_blog_count), 0xff88d9fa);
        new ClientString(followingCount).colorTo((TextView)findViewById(R.id.main_user_following_count), 0xff88d9fa);
        new ClientString(fansCount).colorTo((TextView)findViewById(R.id.main_user_fans_count), 0xff88d9fa);
        GridLayoutManager layout=new GridLayoutManager(this, 2);
        layout.setInitialPrefetchItemCount(6);
        layout.setItemPrefetchEnabled(true);
        onloadView = findViewById(R.id.more_onload);
        historyView = findViewById(R.id.user_history_list);
        historyView.setLayoutManager(layout);
        historyView.setItemViewCacheSize(20);
        historyView.setDrawingCacheEnabled(true);
        historyView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        historyView.addOnScrollListener(new RecyclerView.OnScrollListener(){
                @Override
                public void onScrollStateChanged(RecyclerView view, int state) {
                    super.onScrollStateChanged(view, state);
                    if (state == RecyclerView.SCROLL_STATE_IDLE) {
                        int itemCount = view.getLayoutManager().getItemCount();
                        int lastPos = ((LinearLayoutManager)view.getLayoutManager()).findLastVisibleItemPosition();
                        if (lastPos >= itemCount - 1) {
                            onloadView.setVisibility(View.VISIBLE);
                            request(false);
                        }
                    }
                }
            });
        historyRefresh = findViewById(R.id.user_history_refresh);
        historyRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
                @Override
                public void onRefresh() {
                    Toast.makeText(getApplication(), "走位中...", Toast.LENGTH_SHORT).show();
                    request(true);
                }
            });
        request(true);
    }

    private void request(final boolean isRefresh) {
        try {
            if (!request.tryAcquire()) {
                return;
            }
            AccountManager manager=AccountManager.getInstance(this);
            if (!manager.isLogin()) {
                return;
            }
            if (isRefresh) {
                videos.clear();
            }
            NetworkUtils.getNetwork.getNetworkJson(APIManager.ProfileURI.getNetworkHistoryURI(manager.getAccount().getToken()), new NetworkUtils.HTTPCallback(){
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
                                    Video currentVideo=new Video(getApplication(), video.optJSONObject(i), Video.VIDEO_DEF);
                                    VideoAdapter adapter=(VideoAdapter)historyView.getAdapter();
                                    if (adapter == null || !adapter.isExists(currentVideo)) {
                                        data.add(currentVideo);
                                    }
                                }
                                main.post(new Runnable(){
                                        @Override
                                        public void run() {
                                            if (videos.isEmpty()) {
                                                videos.addAll(data);
                                                VideoAdapter adapter = new VideoAdapter(AccountActivity.this, videos);
                                                historyView.setAdapter(adapter);
                                                return;
                                            }
                                            if (isRefresh) {
                                                videos.addAll(data);
                                                historyView.getAdapter().notifyDataSetChanged();
                                            } else {
                                                onloadView.setVisibility(View.GONE);
                                                ((VideoAdapter)historyView.getAdapter()).addNewData(data);
                                            }
                                        }
                                    });
                                historyRefresh.setRefreshing(false);
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
                        historyRefresh.setRefreshing(false);
                    }
                });
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } finally {
            request.release();
        }
    }
    
    public void switchAccountDia(View v) {
        SlideDrawerManager.getInstance().showAccountSwitch(v);
    }

    public void addAccount(View v) {
        startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST_CODE);
    }
}
