package com.losthiro.ottohubclient;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.losthiro.ottohubclient.adapter.Blog;
import com.losthiro.ottohubclient.adapter.SearchAdapter;
import com.losthiro.ottohubclient.adapter.SearchContent;
import com.losthiro.ottohubclient.adapter.User;
import com.losthiro.ottohubclient.adapter.Video;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.impl.ClientString;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.FileUtils;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.losthiro.ottohubclient.impl.TypeManager;
import com.losthiro.ottohubclient.utils.StringUtils;
import com.losthiro.ottohubclient.utils.DeviceUtils;
import android.os.Build;

/**
 * @Author Hiro
 * @Date 2025/05/24 09:35
 */
public class SearchActivity extends MainActivity {
    public static final String TAG = "SearchActivity";
    private static final LinkedList<String> history=new LinkedList<>();
    private static final Semaphore request=new Semaphore(1);
    private LinearLayout searchCategorys;
    private LinearLayout onloadView;
    private RecyclerView searchCallback;
    private ListView searchHistory;
    private SearchView search;
    private SearchAdapter adapter;
    private int categoryIndex=0;
    private TextView[] categorys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ImageView userAvatar=findViewById(R.id.main_user_avatar);
        final AccountManager manager=AccountManager.getInstance(this);
        if (manager.isLogin()) {
            String uri=manager.getAccount().getAvatarURI();
            ImageDownloader.loader(userAvatar, uri);
        }
        userAvatar.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (manager.isLogin()) {
                        Toast.makeText(getApplication(), "登录完了还点干嘛", Toast.LENGTH_SHORT).show();
                    } else {
                        Client.saveActivity(getIntent());
                        startActivity(new Intent(SearchActivity.this, LoginActivity.class));
                    }
                }
            });
        GridLayoutManager layout = new GridLayoutManager(this, 1);
        layout.setInitialPrefetchItemCount(6);
        layout.setItemPrefetchEnabled(true);
        searchCallback = findViewById(R.id.search_list);
        searchCallback.setVisibility(View.GONE);
        searchCallback.setLayoutManager(layout);
        searchCallback.setItemViewCacheSize(20);
        searchCallback.setDrawingCacheEnabled(true);
        searchCallback.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        search = findViewById(R.id.main_search_view);
        search.setFocusable(true);
        search.setIconifiedByDefault(false);
        search.setQueryHint("那我问你那我问你");
        search.setOnFocusChangeListener(new OnFocusChangeListener(){
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        return;
                    }
                    v.requestFocus();
                }
            });
        search.setOnQueryTextListener(new OnQueryTextListener(){
                @Override
                public boolean onQueryTextChange(String newText) {
                    searchHistory.setVisibility(View.VISIBLE);
                    searchCategorys.setVisibility(View.GONE);
                    searchCallback.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchRequest(query);
                    return false;
                }
            });
        ListAdapter historyAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, history);
        searchHistory = findViewById(R.id.search_history);
        searchHistory.setAdapter(historyAdapter);
        searchHistory.setOnItemClickListener(new OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String current=(String)parent.getItemAtPosition(position);
                    search.setQuery(current, false);
                    searchRequest(current);
                }
            });
        onloadView = findViewById(R.id.more_onload);
        searchCategorys = findViewById(R.id.search_categorys);
        searchCategorys.setVisibility(View.GONE);
        initCategory();
        loadHistory(this);
        String query=getIntent().getStringExtra("query");
        if (query != null) {//检查是否有包含特定搜索词的启动请求
            search.setQuery(query, false);
            searchRequest(query);
            return;
        }
        Toast.makeText(getApplication(), "按回车键检索", Toast.LENGTH_SHORT).show();
    }

    private void initCategory() {
        final int maxCount=4;
        View root=findViewById(android.R.id.content);//获取activity布局
        categorys = new TextView[maxCount];
        categorys[0] = root.findViewWithTag("all");
        categorys[1] = root.findViewWithTag("video");
        categorys[2] = root.findViewWithTag("blog");
        categorys[3] = root.findViewWithTag("user");
        for (int i = 0; i < maxCount; i++) {
            final int index=i;//防越权设计
            categorys[i].setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        updateCategory(index);
                    }
                });
        }
    }

    @Override
    protected void onStop() {
        saveHistory(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        saveHistory(this);
        Intent last=Client.getLastActivity();
        if (last != null && Client.isFinishingLast(last)) {
            Client.removeActivity();
            startActivity(last);
        }
        super.onDestroy();
    }

    private void updateCategory(int index) {
        if (categoryIndex == index) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            TextView current = categorys[i];
            current.setBackgroundResource(i == index ?R.drawable.btn_bg: R.drawable.btn_empty_bg);
            current.setTextColor(i == index ?Color.WHITE: Color.BLACK);
        }
        categoryIndex = index;
        request(search.getQuery().toString(), true);
    }

    private void searchRequest(final String query) {
        searchHistory.setVisibility(View.GONE);
        searchCategorys.setVisibility(View.VISIBLE);
        searchCallback.setVisibility(View.VISIBLE);
        searchCallback.addOnScrollListener(new RecyclerView.OnScrollListener(){
                private int itemCount;
                private int lastPos;

                @Override
                public void onScrollStateChanged(RecyclerView view, int state) {
                    super.onScrollStateChanged(view, state);
                    if (state == RecyclerView.SCROLL_STATE_IDLE) {
                        itemCount = view.getLayoutManager().getItemCount();
                        lastPos = ((LinearLayoutManager)view.getLayoutManager()).findLastVisibleItemPosition();
                        if (lastPos >= itemCount - 1) {
                            onloadView.setVisibility(View.VISIBLE);
                            requestCategory(query, false);
                        }
                    }
                }
            });
        request(query, true);
        addHistory(query);
    }

    private void request(String query, final boolean isRefresh) {
        ClientString str=new ClientString(query);
        if (query.contains("OB") || query.contains("ob") || query.contains("bid") || query.contains("BID")) {
            Intent i=new Intent(this, BlogDetailActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("bid", str.findID("(?i)(bid|ob)\\s*"));
            Client.saveActivity(getIntent());
            startActivity(i);
            return;
        }
        if (query.contains("OV") || query.contains("ov") || query.contains("vid") || query.contains("VID")) {
            callPlayer(this, str.findID("(?i)(vid|ov)\\s*"));
            return;
        }
        if (query.contains("OU") || query.contains("ou") || query.contains("uid") || query.contains("UID")) {
            Intent i=new Intent(this, AccountDetailActivity.class);
            i.putExtra("uid", str.findID("(?i)(uid|ou)\\s*"));
            Client.saveActivity(getIntent());
            startActivity(i);
            return;
        }
        try {
            if (!request.tryAcquire()) {
                return;
            }
            requestCategory(query, isRefresh);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } finally {
            request.release();
        }
    }

    private void requestCategory(String query, final boolean isRefresh) {
        String[] uris={
            APIManager.UserURI.getSearchUserURI(query, categoryIndex == 0 ?3: 12),
            APIManager.BlogURI.getSearchBlogURI(query, categoryIndex == 0 ?3: 12),
            APIManager.VideoURI.getSearchVideoURI(query, 12)
        };
        switch (categoryIndex) {
            case 1:
                NetworkUtils.getNetwork.getNetworkJson(uris[2], new NetworkUtils.HTTPCallback(){
                        @Override
                        public void onSuccess(String content) {
                            if (content == null || content.isEmpty()) {
                                return;
                            }
                            try {
                                JSONObject json=new JSONObject(content);
                                if (json == null) {
                                    return;
                                }
                                String status=json.optString("status", "error");
                                if (status.equals("success")) {
                                    JSONArray video=json.optJSONArray("video_list");
                                    final List<SearchContent> data=new ArrayList<>();
                                    for (int i=0;i < video.length();i++) {
                                        SearchContent current=new SearchContent(new Video(getApplication(), video.optJSONObject(i), Video.VIDEO_DETAIL));
                                        if (!adapter.isContains(current) || isRefresh) {
                                            data.add(current);
                                        }
                                    }
                                    runOnUiThread(new Runnable(){
                                            @Override
                                            public void run() {
                                                if (isRefresh) {
                                                    adapter.setData(data);
                                                    return;
                                                }
                                                onloadView.setVisibility(View.GONE);
                                                adapter.addNewData(data);
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
                break;
            case 2:
                NetworkUtils.getNetwork.getNetworkJson(uris[1], new NetworkUtils.HTTPCallback(){
                        @Override
                        public void onSuccess(String content) {
                            if (content == null || content.isEmpty()) {
                                return;
                            }
                            try {
                                JSONObject json=new JSONObject(content);
                                if (json == null) {
                                    return;
                                }
                                String status=json.optString("status", "error");
                                if (status.equals("success")) {
                                    JSONArray blog=json.optJSONArray("blog_list");
                                    final List<SearchContent> data=new ArrayList<>();
                                    for (int i=0;i < blog.length();i++) {
                                        SearchContent current=new SearchContent(new Blog(blog.optJSONObject(i)));
                                        if (!adapter.isContains(current) || isRefresh) {
                                            data.add(current);
                                        }
                                    }
                                    runOnUiThread(new Runnable(){
                                            @Override
                                            public void run() {
                                                if (isRefresh) {
                                                    adapter.setData(data);
                                                    return;
                                                }
                                                onloadView.setVisibility(View.GONE);
                                                adapter.addNewData(data);
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
                break;
            case 3:
                NetworkUtils.getNetwork.getNetworkJson(uris[0], new NetworkUtils.HTTPCallback(){
                        @Override
                        public void onSuccess(String content) {
                            if (content == null || content.isEmpty()) {
                                return;
                            }
                            try {
                                JSONObject json=new JSONObject(content);
                                if (json == null) {
                                    return;
                                }
                                String status=json.optString("status", "error");
                                if (status.equals("success")) {
                                    JSONArray user=json.optJSONArray("user_list");
                                    final List<SearchContent> data=new ArrayList<>();
                                    for (int i=0;i < user.length();i++) {
                                        SearchContent current = new SearchContent(new User(user.optJSONObject(i)));
                                        if (!adapter.isContains(current) || isRefresh) {
                                            data.add(current);
                                        }
                                    }
                                    runOnUiThread(new Runnable(){
                                            @Override
                                            public void run() {
                                                if (isRefresh) {
                                                    adapter.setData(data);
                                                    return;
                                                }
                                                onloadView.setVisibility(View.GONE);
                                                adapter.addNewData(data);
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
                break;
            default:
                if (isRefresh) {
                    final List<SearchContent> data=new ArrayList<>();
                    for (int i=0;i < uris.length;i++) {
                        final int index=i;
                        NetworkUtils.getNetwork.getNetworkJson(uris[i], new NetworkUtils.HTTPCallback(){
                                @Override
                                public void onSuccess(String content) {
                                    if (content == null || content.isEmpty()) {
                                        return;
                                    }
                                    try {
                                        JSONObject json=new JSONObject(content);
                                        if (json == null) {
                                            return;
                                        }
                                        String status=json.optString("status", "error");
                                        if (status.equals("success")) {
                                            String def="video_list";
                                            if (index == 0) {
                                                def = "user_list";
                                            } else if (index == 1) {
                                                def = "blog_list";
                                            }
                                            JSONArray array=json.optJSONArray(def);
                                            for (int i=0;i < array.length();i++) {
                                                JSONObject obj=array.optJSONObject(i);
                                                SearchContent current=null;
                                                switch (index) {
                                                    case 0:
                                                        current = new SearchContent(new User(obj));
                                                        break;
                                                    case 1:
                                                        current = new SearchContent(new Blog(obj));
                                                        break;
                                                    case 2:
                                                        current = new SearchContent(new Video(getApplication(), obj, Video.VIDEO_DETAIL));
                                                        break;
                                                }
                                                if (current != null) {
                                                    data.add(current);
                                                }
                                            }
                                            if (index >= 2) {
                                                runOnUiThread(new Runnable(){
                                                        @Override
                                                        public void run() {
                                                            if (adapter == null) {
                                                                adapter = new SearchAdapter(SearchActivity.this, data);
                                                                searchCallback.setAdapter(adapter);
                                                                return;
                                                            }
                                                            adapter.setData(data);
                                                        }
                                                    });
                                            }
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
                } else {
                    NetworkUtils.getNetwork.getNetworkJson(uris[2], new NetworkUtils.HTTPCallback(){
                            @Override
                            public void onSuccess(String content) {
                                if (content == null || content.isEmpty()) {
                                    return;
                                }
                                try {
                                    JSONObject json=new JSONObject(content);
                                    if (json == null) {
                                        return;
                                    }
                                    String status=json.optString("status", "error");
                                    if (status.equals("success")) {
                                        JSONArray video=json.optJSONArray("video_list");
                                        final List<SearchContent> data=new ArrayList<>();
                                        for (int i=0;i < video.length();i++) {
                                            SearchContent current = new SearchContent(new Video(getApplication(), video.optJSONObject(i), Video.VIDEO_DETAIL));
                                            if (!adapter.isContains(current)) {
                                                data.add(current);
                                            }
                                        }
                                        runOnUiThread(new Runnable(){
                                                @Override
                                                public void run() {
                                                    onloadView.setVisibility(View.GONE);
                                                    adapter.addNewData(data);
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
                break;
        }
    }

    public static void callPlayer(final Context c, final long vid) {
        NetworkUtils.getNetwork.getNetworkJson(APIManager.VideoURI.getIDvideoURI(vid), new NetworkUtils.HTTPCallback(){
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
                            Video v = new Video(c, video.optJSONObject(0), Video.VIDEO_DEF);
                            Intent i=new Intent(c, PlayerActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.putExtra("vid", vid);
                            i.putExtra("uid", v.getUID());
                            i.putExtra("title", v.getTitle());
                            i.putExtra("time", v.getTime());
                            i.putExtra("name", v.getUser());
                            i.putExtra("view", v.getViewCount());
                            i.putExtra("like", v.getLikeCount());
                            i.putExtra("favorite", v.getFavoriteCount());
                            Client.saveActivity(TypeManager.getCurrentActivity(c).getIntent());
                            c.startActivity(i);
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

    private void addHistory(String query) {
        if (history.contains(query)) {
            return;
        }
        if (history.size() == 8) {
            history.remove(0);
        }
        history.add(query);
    }

    private void saveHistory(Context c) {
        String path = StringUtils.strCat(DeviceUtils.getAndroidSDK() >= Build.VERSION_CODES.R ?getExternalFilesDir(null).toString(): "/sdcard/OTTOHub", "/config/history_search.json");
        try {
            JSONArray config=new JSONArray();
            for (String query:history) {
                config.put(query);
            }
            if (FileUtils.createFile(c, path, config.toString(4))) {
                return;
            }
            FileUtils.writeFile(c, path, config.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadHistory(Context c) {
        String path = StringUtils.strCat(DeviceUtils.getAndroidSDK() >= Build.VERSION_CODES.R ?getExternalFilesDir(null).toString(): "/sdcard/OTTOHub", "/config/history_search.json");
        try {
            String content = FileUtils.readFile(c, path);
            if (content == null) {
                return;
            }
            JSONArray config=new JSONArray(content);
            for (int i = 0; i < config.length(); i++) {
                addHistory(config.optString(i, "棍母"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
