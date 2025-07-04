package com.losthiro.ottohubclient;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.losthiro.ottohubclient.LoginActivity;
import com.losthiro.ottohubclient.adapter.Blog;
import com.losthiro.ottohubclient.adapter.Video;
import com.losthiro.ottohubclient.adapter.VideoAdapter;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.losthiro.ottohubclient.adapter.BlogAdapter;
import com.losthiro.ottohubclient.impl.TypeManager;
import android.widget.LinearLayout;

/**
 * @Author Hiro
 * @Date 2025/05/28 14:52
 */
public class BlogActivity extends MainActivity {
    public static final String TAG = "BlogActivity";
    private static final Handler main=new Handler(Looper.getMainLooper());
    private static final List<Blog> blogs=new ArrayList<>();
    private static final Semaphore request=new Semaphore(1);
    private int categoryIndex=0;
    private SwipeRefreshLayout blogRefresh;
    private LinearLayout onloadView;
    private RecyclerView blogList;
    private BlogAdapter adapter;
    private TextView[] categorys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        final AccountManager manager=AccountManager.getInstance(getApplication());
        ImageView userMain = findViewById(R.id.main_user_avatar);
        userMain.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (manager.isLogin()) {
                        TypeManager.runTypeInstance(2);
                    } else {
                        startActivity(new Intent(BlogActivity.this, LoginActivity.class));
                    }
                }
            });
        if (manager.isLogin()) {
            ImageDownloader.loader(userMain, manager.getAccount().getAvatarURI());
        }
        blogRefresh = findViewById(R.id.blog_refresh);
        blogRefresh.setRefreshing(true);
        blogRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
                @Override
                public void onRefresh() {
                    Toast.makeText(getApplication(), "走位中...", Toast.LENGTH_SHORT).show();
                    requestCategory(categoryIndex, true);
                }
            });
            onloadView=findViewById(R.id.more_onload);
        GridLayoutManager layout=new GridLayoutManager(this, 1);
        layout.setInitialPrefetchItemCount(6);
        layout.setItemPrefetchEnabled(true);
        blogList = findViewById(R.id.blogs_list);
        blogList.setItemViewCacheSize(20);
        blogList.setDrawingCacheEnabled(true);
        blogList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        blogList.setLayoutManager(layout);
        blogList.addOnScrollListener(new RecyclerView.OnScrollListener(){
                @Override
                public void onScrollStateChanged(RecyclerView view, int state) {
                    super.onScrollStateChanged(view, state);
                    if (state == RecyclerView.SCROLL_STATE_IDLE) {
                        int itemCount = view.getLayoutManager().getItemCount();
                        int lastPos = ((LinearLayoutManager)view.getLayoutManager()).findLastVisibleItemPosition();
                        if (lastPos >= itemCount - 1) {
                            onloadView.setVisibility(View.VISIBLE);
                            requestCategory(categoryIndex, false);
                        }
                    }
                }
            });
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "那我缺的网络这一块", Toast.LENGTH_SHORT).show();
        }
        main.postDelayed(new Runnable(){
                @Override
                public void run() {
                    requestCategory(categoryIndex, true);
                }
            }, 1000L);
        TypeManager.initTypes(findViewById(android.R.id.content));
        initCategoryView();
    }

    @Override
    public void onBackPressed() {
        TypeManager.runTypeInstance(0);
        super.onBackPressed();
    }

    private void requestCategory(int index, boolean isRefresh) {
        String uri=APIManager.BlogURI.getRandomBlogURI(12);
        if (index == 1) {
            uri = APIManager.BlogURI.getNewBlogURI(0, 12);
        }
        if (index >= 2 && index < 5) {
            int mode=-1;
            if (index == 2) {
                mode = APIManager.BlogURI.WEEK;
            }
            if (index == 3) {
                mode = APIManager.BlogURI.MONTH;
            }
            if (index == 4) {
                mode = APIManager.BlogURI.QUARTERLY;
            }
            uri = APIManager.BlogURI.getPopularBlogURI(mode, 0, 12);
        }
        blogRefresh.setRefreshing(true);
        request(uri, isRefresh);
    }

    private void request(String uri, final boolean isRefresh) {
        try {
            if (!request.tryAcquire()) {
                return;
            }
            if (isRefresh) {
                blogs.clear();
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
                                JSONArray video=json.optJSONArray("blog_list");
                                final List<Blog> data=new ArrayList<>();
                                for (int i=0;i < video.length();i++) {
                                    data.add(new Blog(new JSONObject(video.optString(i, "棍母"))));
                                }
                                main.post(new Runnable(){
                                        @Override
                                        public void run() {
                                            if (blogs.isEmpty()) {
                                                blogs.addAll(data);
                                                adapter = new BlogAdapter(BlogActivity.this, blogs);
                                                blogList.setAdapter(adapter);
                                                return;
                                            }
                                            if (isRefresh) {
                                                blogs.addAll(data);
                                                adapter.notifyDataSetChanged();
                                            } else {
                                                onloadView.setVisibility(View.GONE);
                                                adapter.addNewData(data);
                                            }
                                        }
                                    });
                                blogRefresh.setRefreshing(false);
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
                        blogRefresh.setRefreshing(false);
                    }
                });
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } finally {
            request.release();
        }
    }

    private void initCategoryView() {
        View contentView = findViewById(android.R.id.content);
        final int categoryCount=5;
        categorys = new TextView[categoryCount];
        categorys[0] = contentView.findViewWithTag("recommend");
        categorys[1] = contentView.findViewWithTag("new");
        categorys[2] = contentView.findViewWithTag("week");
        categorys[3] = contentView.findViewWithTag("month");
        categorys[4] = contentView.findViewWithTag("quarterly");
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
                        requestCategory(index, true);
                        categoryIndex = index;
                    }
                });
        }
    }
}
