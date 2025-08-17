package com.losthiro.ottohubclient.adapter.model;
import org.json.JSONObject;
import com.losthiro.ottohubclient.utils.SystemUtils;
import com.losthiro.ottohubclient.utils.StringUtils;

/**
 * @Author Hiro
 * @Date 2025/06/14 15:02
 */
public class Message {
    public static final String TAG = "Message";
    public static final int TYPE_ANSWER=0;
    public static final int TYPE_AT=1;
    public static final int TYPE_SYSTEM=2;
    private JSONObject root;
    private String content;

    public Message(JSONObject main) {
        root = main;
        content = main.optString("content", null);
    }

    public boolean isEmail() {
        return getUID() != 0;
    }

    public long getMID() {
        String mid=root.optString("msg_id", null);
        return mid == null ?root.optLong("msg_id", 0): Long.parseLong(mid);
    }

    public long getSendUID() {
        long uid = getUID();
        return uid == 0 ?Long.parseLong(content.substring(content.indexOf(":") + 1, content.indexOf(")"))): uid;
    }

    private long getUID() {
        String sid=root.optString("sender", null);
        return sid == null ?root.optLong("sender", -1): Long.parseLong(sid);
    }

    public long getReceiverUID() {
        String rid=root.optString("receiver", null);
        return rid == null ?root.optLong("receiver", 0): Long.parseLong(rid);
    }

    public int getType() {
        if (content.contains("评价") || getUID() != 0) {
            return TYPE_ANSWER;
        }
        if (content.contains("@")) {
            return TYPE_AT;
        }
        return TYPE_SYSTEM;
    }

    public String getContent() {
        return getUID() == 0 ?content.substring(content.indexOf(")") + 1): StringUtils.strCat("私信了你: " , content.substring(content.indexOf(System.lineSeparator()) + 1));
    }

    public String getTime() {
        String format="yyyy-MM-dd HH:mm:ss";
        String def=SystemUtils.getDate(format);
        if (root == null) {
            return def;
        }
        String video=root.optString("time", def);
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

    public String getSender() {
        String name = root.optString("sender_name", "爱丽丝网络节点");
        if (name.equals("爱丽丝网络节点")) {
            return content.substring(0, content.indexOf(")") + 1);
        }
        return name;
    }

    public String getReceiver() {
        return root.optString("receiver_name", "棍母");
    }

    public String getAvatarURI() {
        return root.optString("sender_avatar_url");
    }

    @Override
    public String toString() {
        return content;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Message) {
            return ((Message)obj).getMID() == getMID();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int)getMID();
    }
}
