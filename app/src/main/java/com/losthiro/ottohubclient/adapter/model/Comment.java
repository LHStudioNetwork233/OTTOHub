package com.losthiro.ottohubclient.adapter.model;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.adapter.model.Comment;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import com.losthiro.ottohubclient.utils.SystemUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.losthiro.ottohubclient.impl.ClientString;
import com.losthiro.ottohubclient.adapter.*;

/**
 * @Author Hiro
 * @Date 2025/05/23 05:59
 */
public class Comment {
    public static final String TAG = "Comment";
    public static final int TYPE_VIDEO=0;
    public static final int TYPE_BLOG=1;

    private static final Semaphore request=new Semaphore(1);
    private long answerID;
    private int typeCurrent;
    private boolean isCurrent;
    private JSONObject root;
    private Context main;

    public Comment(Context ctx, JSONObject json, long id, int type) {
        answerID = id;
        main = ctx;
        root = json;
        typeCurrent = type;
    }

    public int getType() {
        return typeCurrent;
    }

    public long getID() {
        return answerID;
    }
    
    public boolean isCurrent(){
        return isCurrent;
    }

    public long getCID() {
        String type=typeCurrent == TYPE_VIDEO ?"vcid": "bcid";
        String strId=root.optString(type, "-1");
        return strId.equals("-1") ? root.optLong(type, -1): Long.parseLong(strId);
    }

    public long getParentCID() {
        String type=typeCurrent == TYPE_VIDEO ?"parent_vcid": "parent_bcid";
        String strId=root.optString(type, "-1");
        return strId.equals("-1") ? root.optLong(type, -1): Long.parseLong(strId);
    }

    public long getUID() {
        String strId=root.optString("uid", "-1");
        return strId.equals("-1") ? root.optLong("uid", -1): Long.parseLong(strId);
    }

    public String getContent() {
        return root.optString("content", "大家好啊，我是电棍");
    }

    public String getTime() {
        String format="yyyy-MM-dd HH:mm:ss";
        String def=SystemUtils.getDate(format);
        if (root == null) {
            return def;
        }
        String comment=root.optString("time", def);
        long time=SystemUtils.getTime() - SystemUtils.getTime(comment, format);
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
        return comment;
    }

    public int getChildCount() {
        String strCount=root.optString("child_comment_num", "0");
        return strCount.equals("0") ? root.optInt("child_comment_num", 0): Integer.parseInt(strCount);
    }

    public boolean isMyComment() {
        String strCount=root.optString("child_comment_num", "0");
        int count= strCount.equals("0") ? root.optInt("child_comment_num", 0): Integer.parseInt(strCount);
        return count != 0;
    }

    public String getUser() {
        return root.optString("username", "棍母");
    }

    public String[] getHonours() {
        return root.optString("honour", "吉吉国民,").split(",");
    }
    
    public void setCurrent(boolean v){
        isCurrent = v;
    }

    public void setAvatar(ImageView target) {
        Bitmap ic = BitmapFactory.decodeResource(main.getResources(), R.drawable.ic_launcher);
        try {
            if (root == null) {
                target.setImageBitmap(ic);
                return;
            }
            String uri=root.optString("avatar_url", "null");
            ImageDownloader.loader(target, uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadChildComment(final TextView text, final CommentAdapter.CallComment child, final int type) {
        text.setVisibility(View.VISIBLE);
        AccountManager manager=AccountManager.getInstance(text.getContext());
        String video=manager.isLogin() ?APIManager.CommentURI.getVideoCommentURI(getID(), getCID(), manager.getAccount().getToken(),  0, 12): APIManager.CommentURI.getVideoCommentURI(getID(), getCID(), 0, 12);
        String blog=manager.isLogin() ?APIManager.CommentURI.getBlogCommentURI(getID(), getCID(), manager.getAccount().getToken(),  0, 12): APIManager.CommentURI.getBlogCommentURI(getID(), getCID(), 0, 12);
        String uri=type == TYPE_VIDEO ?video: blog;
        final Handler h=new Handler(Looper.getMainLooper());
        NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback(){
                @Override
                public void onSuccess(final String content) {
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
                            JSONArray array= root.optJSONArray("comment_list");
                            final List<Comment> data=new ArrayList<>();
                            final StringBuilder viewText=new StringBuilder();
                            for (int i = 0; i < array.length(); i++) {
                                Comment current=new Comment(main, array.optJSONObject(i), getID(), type);
                                String message=current.getContent();
                                if (message.length() > 16) {
                                    message = message.substring(0, 16) + "...";
                                }
                                if (i < 3) {
                                    viewText.append(current.getUser()).append(":").append(message).append(System.lineSeparator());
                                }
                                data.add(current);
                            }
                            viewText.append("查看全部").append(getChildCount()).append("条评论");
                            h.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        text.setText(viewText);
                                        text.setOnClickListener(new OnClickListener(){
                                                @Override
                                                public void onClick(View v) {
                                                    child.showChildComment(data, getChildCount());
                                                }
                                            });
                                    }
                                });
                        }
                    } catch (JSONException e) {
                        onFailed(e.toString());
                    }
                }

                @Override
                public void onFailed(final String cause) {
                    Log.e("Network", cause);
                    h.post(new Runnable(){
                            @Override
                            public void run() {
                                Toast.makeText(main, cause, Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            });
    }

    public void reportComment() {
        try {
            if (!request.tryAcquire()) {
                return;
            }
            String token=AccountManager.getInstance(main).getAccount().getToken();
            String uri=getType() == TYPE_VIDEO ?APIManager.CommentURI.getReportVideoURI(getCID(), token): APIManager.CommentURI.getReportBlogURI(getCID(), token);
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
                                Toast.makeText(main, "举报成功", Toast.LENGTH_SHORT).show();
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
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Comment) {
            return ((Comment)obj).getCID() == getCID();
        }
        return false;
    }
}
