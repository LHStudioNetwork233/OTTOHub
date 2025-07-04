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
import com.losthiro.ottohubclient.adapter.Comment;
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

/**
 * @Author Hiro
 * @Date 2025/05/29 10:38
 */
public class BlogDetailActivity extends MainActivity {
    public static final String TAG = "BlogDetailActivity";
    private static final List<Comment> commentList=new ArrayList<>();
    private static final Semaphore request=new Semaphore(1);
    public static EditText commentEdit;
    private BlogInfo current;
    private SwipeRefreshLayout refresh;
    private RecyclerView commentView;
    private long firstBackTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_detail);
        final long bid=getIntent().getLongExtra("bid", -1);
        if (bid == -1) {
            return;
        }
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(getApplication(), "那我缺的网络这块谁来给我补上啊", Toast.LENGTH_SHORT).show();
            return;
        }
        AccountManager manager=AccountManager.getInstance(this);
        String uri=manager.isLogin() ?APIManager.BlogURI.getBlogDetailURI(bid, manager.getAccount().getToken()): APIManager.BlogURI.getBlogDetailURI(bid);
        final Handler h=new Handler(getMainLooper());
        NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback(){
                @Override
                public void onSuccess(String content) {
                    if (content == null || content.isEmpty()) {
                        return;
                    }
                    try {
                        JSONObject root=new JSONObject(content);
                        if (root == null) {
                            return;
                        }
                        if (root.optString("status", "error").equals("success")) {
                            current = new BlogInfo(root, bid);
                            h.post(new Runnable(){
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
                }
            });

    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstBackTime > 2000) {
            Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
            CommentAdapter adapter=(CommentAdapter)commentView.getAdapter();
            if (adapter != null) {
                adapter.onBack(Comment.TYPE_BLOG);
            }
            firstBackTime = System.currentTimeMillis();
            return;
        }
        SlideDrawerManager manager = SlideDrawerManager.getInstance();
        manager.registerDrawer(manager.getLastParent());
        super.onBackPressed();
        Intent last=Client.getLastActivity();
        if (last != null && Client.isFinishingLast(last)) {
            Client.removeActivity();
            startActivity(last);
        }
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

    private void loadUI() {
        SlideDrawerManager.getInstance().registerDrawer(findViewById(android.R.id.content));
        ImageView avatar=findViewById(R.id.user_avatar);
        avatar.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(BlogDetailActivity.this, AccountDetailActivity.class);
                    i.putExtra("uid", current.getUID());
                    Client.saveActivity(getIntent());
                    startActivity(i);
                }
            });
        ImageDownloader.loader(avatar, current.getAvatarURI());
        ((TextView)findViewById(R.id.user_name)).setText(current.getUser());
        ((TextView)findViewById(R.id.blog_title)).setText(current.getTitle());
        String info=StringUtils.strCat(new Object[]{current.getTime(), " - ", current.getViewCount(), " - OB", current.getID()});
        ((TextView)findViewById(R.id.blog_info)).setText(info);
        ((ClientWebView)findViewById(R.id.blog_content_view)).loadTextData(current.getContent());
        final TextView likeCount = findViewById(R.id.blog_like_count);
        likeCount.setText(current.getLikeCount());
        final TextView favoriteCount = findViewById(R.id.blog_favorite_count);
        favoriteCount.setText(current.getFavoriteCount());
        ImageButton like=findViewById(R.id.blog_like_btn);
        ImageButton favorite=findViewById(R.id.blog_favorite_btn);
        like.setColorFilter(current.isLike() ?Color.parseColor("#88d9fa"): Color.BLACK);
        favorite.setColorFilter(current.isLike() ?Color.parseColor("#88d9fa"): Color.BLACK);
        like.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    likeCurrent(v, likeCount);
                }
            });
        favorite.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    favouriteCurrent(v, favoriteCount);
                }
            });
        refresh = findViewById(R.id.comment_refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
                @Override
                public void onRefresh() {
                    request(true);
                }
            });
        commentView = findViewById(R.id.comment_list);
        commentView.setLayoutManager(new GridLayoutManager(this, 1));
        commentView.setItemViewCacheSize(20);
        commentView.setDrawingCacheEnabled(true);
        commentView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        commentView.addOnScrollListener(new RecyclerView.OnScrollListener(){
                @Override
                public void onScrollStateChanged(RecyclerView view, int state) {
                    super.onScrollStateChanged(view, state);
                    if (state == RecyclerView.SCROLL_STATE_IDLE) {
                        int itemCount = view.getLayoutManager().getItemCount();
                        int lastPos = ((LinearLayoutManager)view.getLayoutManager()).findLastVisibleItemPosition();
                        if (lastPos >= itemCount - 1 && itemCount >= 12) {
                            request(false);
                        }
                    }
                }
            });
        commentEdit = findViewById(R.id.blog_comment_edit);
        request(true);
    }

    private void request(final boolean isRefresh) {
        try {
            if (!request.tryAcquire()) {
                return;
            }
            if (isRefresh) {
                commentList.clear();
            }
            AccountManager manager=AccountManager.getInstance(this);
            String uri=manager.isLogin() ?APIManager.CommentURI.getBlogCommentURI(current.getID(), 0, manager.getAccount().getToken(),  0, 12): APIManager.CommentURI.getBlogCommentURI(current.getID(), 0, 0, 12);
            final Handler main=new Handler(getMainLooper());
            NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback(){
                    @Override
                    public void onSuccess(final String content) {
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
                                JSONArray comment=json.optJSONArray("comment_list");
                                final List<Comment> data=new ArrayList<>();
                                for (int i=0;i < comment.length();i++) {
                                    Comment currentComment=new Comment(getApplication(), comment.optJSONObject(i), current.getID(), Comment.TYPE_BLOG);
                                    CommentAdapter adapter=(CommentAdapter)commentView.getAdapter();
                                    if (adapter == null || !adapter.isCommentExists(currentComment)) {
                                        data.add(currentComment);
                                    }
                                }
                                main.post(new Runnable(){
                                        @Override
                                        public void run() {
                                            if (commentList.isEmpty()) {
                                                commentList.addAll(data);
                                                commentView.setAdapter(new CommentAdapter(BlogDetailActivity.this, commentList, true));
                                                return;
                                            }
                                            if (isRefresh) {
                                                commentList.addAll(data);
                                                commentView.getAdapter().notifyDataSetChanged();
                                            } else {
                                                ((CommentAdapter)commentView.getAdapter()).addNewData(data);
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
                        refresh.setRefreshing(false);
                    }
                });
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } finally {
            request.release();
        }
    }

    private void likeCurrent(final View v, final TextView likeCountView) {
        AccountManager manager=AccountManager.getInstance(this);
        if (!manager.isLogin()) {
            Toast.makeText(getApplication(), "没登录喜欢牛魔", Toast.LENGTH_SHORT).show();
            return;
        }
        long bid=current.getID();
        String token=manager.getAccount().getToken();
        final Handler h=new Handler(getMainLooper());
        NetworkUtils.getNetwork.getNetworkJson(APIManager.EngagementURI.getLikeBlogURI(bid, token), new NetworkUtils.HTTPCallback(){
                @Override
                public void onSuccess(String content) {
                    if (content == null || content.isEmpty()) {
                        onFailed("empty content");
                        return;
                    }
                    try {
                        final JSONObject root=new JSONObject(content);
                        if (root == null) {
                            onFailed("null json");
                            return;
                        }
                        if (root.optString("status", "error").equals("success")) {
                            final boolean isLike=root.optInt("if_like", 0) == 1;
                            String count=root.optString("like_count", null);
                            final int likeCount=count == null ?root.optInt("like_count", 0): Integer.parseInt(count);
                            h.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        ((ImageButton)v).setColorFilter(isLike ?Color.parseColor("#88d9fa"): Color.BLACK);
                                        likeCountView.setText(likeCount + "获赞");
                                        String msg=isLike ?"点赞成功": "取消点赞成功";
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
                }
            });
    }

    private void favouriteCurrent(final View v, final TextView favouriteView) {
        AccountManager manager=AccountManager.getInstance(this);
        if (!manager.isLogin()) {
            Toast.makeText(getApplication(), "没登录点牛魔", Toast.LENGTH_SHORT).show();
            return;
        }
        long bid=current.getID();
        String token=manager.getAccount().getToken();
        final Handler h=new Handler(getMainLooper());
        NetworkUtils.getNetwork.getNetworkJson(APIManager.EngagementURI.getFavoriteBlogURI(bid, token), new NetworkUtils.HTTPCallback(){
                @Override
                public void onSuccess(String content) {
                    if (content == null || content.isEmpty()) {
                        onFailed("empty content");
                        return;
                    }
                    try {
                        final JSONObject root=new JSONObject(content);
                        if (root == null) {
                            onFailed("null json");
                            return;
                        }
                        if (root.optString("status", "error").equals("success")) {
                            final boolean isFav=root.optInt("if_favorite", 0) == 1;
                            String count=root.optString("like_favorite", null);
                            final int fCount=count == null ?root.optInt("like_favorite", 0): Integer.parseInt(count);
                            h.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        ((ImageButton)v).setColorFilter(isFav ?Color.parseColor("#88d9fa"): Color.BLACK);
                                        favouriteView.setText(fCount + "冷藏");
                                        String msg=isFav ?"冷藏成功": "取消冷藏成功";
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
                }
            });
    }

    private void sendComment(long parent) {
        AccountManager manager = AccountManager.getInstance(this);
        if (!manager.isLogin()) {
            Toast.makeText(this, "没登录发牛魔", Toast.LENGTH_SHORT).show();
            return;
        }
        String content=commentEdit.getText().toString();
        if (content.isEmpty()) {
            Toast.makeText(this, "你发的是棍母", Toast.LENGTH_SHORT).show();
            return;
        }
        NetworkUtils.getNetwork.getNetworkJson(APIManager.CommentURI.getCommentBlogURI(current.getID(), parent, manager.getAccount().getToken(), content), new NetworkUtils.HTTPCallback(){
                @Override
                public void onSuccess(String content) {
                    if (content.isEmpty() || content == null) {
                        onFailed("empty content");
                        return;
                    }
                    try {
                        final JSONObject root=new JSONObject(content);
                        new Handler(Looper.getMainLooper()).post(new Runnable(){
                                @Override
                                public void run() {
                                    refresh.setRefreshing(true);
                                    String status=root.optString("status", "error");
                                    if (status.equals("success")) {
                                        int callback=root.optInt("if_get_experience", 0);
                                        String msg="评论发送成功~";
                                        if (callback == 1) {
                                            msg = msg + "经验+3";
                                        }
                                        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
                                        commentEdit.setText("");
                                        request(false);
                                        return;
                                    }
                                    String message=root.optString("message", "error");
                                    if (message.equals("content_too_long")) {
                                        Toast.makeText(getApplication(), "不许你发小作文", Toast.LENGTH_SHORT).show();
                                    }
                                    if (message.equals("content_too_short")) {
                                        Toast.makeText(getApplication(), "才发两三个字是什么意思啊", Toast.LENGTH_SHORT).show();
                                    }
                                    if (message.equals("error_parent")) {
                                        Toast.makeText(getApplication(), "这个嘛...目前还没有楼中楼中楼功能哦", Toast.LENGTH_SHORT).show();
                                    }
                                    if (message.equals("warn")) {
                                        Toast.makeText(getApplication(), "冰不许爆(把你违禁词删了)", Toast.LENGTH_SHORT).show();
                                    }
                                    onFailed(message);
                                }
                            });
                    } catch (JSONException e) {
                        onFailed(e.toString());
                    }
                }

                @Override
                public void onFailed(final String cause) {
                    Log.e("Network", cause);
                    refresh.setRefreshing(false);
                }
            });
    }

    private void reportCurrent() {
        try {
            if (!request.tryAcquire()) {
                return;
            }
            String token=AccountManager.getInstance(this).getAccount().getToken();
            NetworkUtils.getNetwork.getNetworkJson(APIManager.ModerationURI.getReportBlogURI(current.getID(), token), new NetworkUtils.HTTPCallback(){
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

    public void quit(View v) {
        Intent last=Client.getLastActivity();
        if (last != null && Client.isFinishingLast(last)) {
            Client.removeActivity();
            startActivity(last);
        }
        finish();
    }

    public void sendComment(View v) {
        CommentAdapter adapter=(CommentAdapter)commentView.getAdapter();
        if (adapter == null) {
            return;
        }
        Comment c=adapter.getCurrent();
        long parent=c == null ?0: c.getCID();
        sendComment(parent);
    }

    public void shareBlog(View v) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "OTTOHub邀请你来看动态 OB" + current.getID() + " " + current.getUser() + " 发布的动态");
        startActivity(Intent.createChooser(i, "share"));
    }

    public void reportBlog(View v) {
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("确认举报？")
            .setMessage("OB" + current.getID())
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    reportCurrent();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create();
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
            String def=StringUtils.toStr(0);
            if (main == null) {
                return def;
            }
            String strCount=main.optString(type, null);
            long count=Long.parseLong(strCount);
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
            String uid=main.optString("uid", null);
            return uid == null ?main.optLong("uid", -1): Long.parseLong(uid);
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
            String format="yyyy-MM-dd HH:mm:ss";
            String def=SystemUtils.getDate(format);
            if (main == null) {
                return def;
            }
            String video=main.optString("time", def);
            long time=SystemUtils.getTime() - SystemUtils.getTime(video, format);
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
