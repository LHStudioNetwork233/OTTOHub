package com.losthiro.ottohubclient.adapter;
import org.json.JSONObject;
import com.losthiro.ottohubclient.utils.SystemUtils;

/**
 * @Author Hiro
 * @Date 2025/05/28 08:40
 */
public class Blog {
    public static final String TAG = "Blog";
    private JSONObject root;

    public Blog(JSONObject json) {
        root = json;
    }

    public long getBID() {
        String id=root.optString("bid", null);
        return id == null ?root.optLong("bid", -1): Long.parseLong(id);
    }

    public long getUID() {
        String id=root.optString("uid", null);
        return id == null ?root.optLong("uid", -1): Long.parseLong(id);
    }

    public int getLikeCount() {
        String count=root.optString("like_count", null);
        return count == null ?root.optInt("like_count", -1): Integer.parseInt(count);
    }

    public int getViewCount() {
        String count=root.optString("view_count", null);
        return count == null ?root.optInt("view_count", -1): Integer.parseInt(count);
    }

    public int getFavoriteCount() {
        String count=root.optString("favorite_count", null);
        return count == null ?root.optInt("favorite_count", -1): Integer.parseInt(count);
    }

    public String getTitle() {
        return root.optString("title", "棍母");
    }

    public String getContent() {
        return root.optString("content", "今天来点大家想看的东西啊");
    }

    public String getAvatarURI() {
        return root.optString("avatar_url", "null");
    }

    public String getTime() {
        String format="yyyy-MM-dd HH:mm:ss";
        String def=SystemUtils.getDate(format);
        if (root == null) {
            return def;
        }
        String blog=root.optString("time", def);
        long time=SystemUtils.getTime() - SystemUtils.getTime(blog, format);
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
        return blog;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Blog) {
            return ((Blog)obj).getBID() == getBID();
        }
        return false;
    }
}
