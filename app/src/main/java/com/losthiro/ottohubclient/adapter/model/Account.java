package com.losthiro.ottohubclient.adapter.model;
import android.content.Context;
import com.losthiro.ottohubclient.utils.StringUtils;
import org.json.JSONObject;
import java.util.HashMap;
import android.graphics.Color;

/**
 * @Author Hiro
 * @Date 2025/05/23 21:45
 */
public class Account {
    public static final String TAG = "Account";
    private Context main;
    private JSONObject root;
    private String mainToken;
    private boolean isCurrent;

    public Account(Context ctx, JSONObject json, String token) {
        main = ctx;
        root = json;
        mainToken = token;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public long getUID() {
        if (root == null) {
            return -1;
        }
        String stringID=root.optString("uid", "-1");
        return stringID == null || stringID.isEmpty() ? root.optLong("uid", -1): Long.parseLong(stringID);
    }

    public String getName() {
        return root.optString("username", "棍母");
    }

    public String getIntro() {
        return root.optString("intro", "大家好啊，我是电棍");
    }

    public String getSex() {
        return root.optString("sex", "道理");
    }

    public String getTime() {
        return root.optString("time", "0月0日");
    }

    public String[] getHonours() {
        return root.optString("honour", "吉吉国民").split(",");
    }

    public int getExp() {
        return root.optInt("experience", 0);
    }

    public HashMap<String, Integer> getLevel() {
        int exp=getExp();
        HashMap<String, Integer> levelMap=new HashMap<>();
        if (exp >= 0 && exp < 49) {
            levelMap.put("ZERO", 0xff6D6D6D);
        } else if (exp >= 50 && exp < 499) {
            levelMap.put("UNO", 0xffC1C1C1);
        } else if (exp >= 500 && exp < 999) {
            levelMap.put("DUE", 0xff73D858);
        } else if (exp >= 1000 && exp < 2999) {
            levelMap.put("TRE", 0xff6779A9);
        } else if (exp >= 3000 && exp < 7999) {
            levelMap.put("QUATTRO", 0xff52ABD5);
        } else if (exp >= 8000 && exp < 14999) {
            levelMap.put("CINQUE", 0xffDC6C6B);
        } else if (exp >= 15000 && exp < 29999) {
            levelMap.put("SEI", 0xffB070C7);
        } else if (exp >= 30000 && exp < 79999) {
            levelMap.put("SETTE", 0xff000000);
        } else {
            levelMap.put("OTTO", 0xffDAD55D);
        }
        return levelMap;
    }

    public String getAvatarURI() {
        return root.optString("avatar_url", "null");
    }

    public String getCoverURI() {
        return root.optString("cover_url", "null");
    }

    public String getToken() {
        return mainToken;
    }

    public int getVideoCount() {
        if (root == null) {
            return -1;
        }
        String stringID=root.optString("video_num", "0");
        return stringID == null || stringID.isEmpty() ? root.optInt("video_num", 0): Integer.parseInt(stringID);
    }

    public int getBlogCount() {
        if (root == null) {
            return -1;
        }
        String stringID=root.optString("blog_num", "0");
        return stringID == null || stringID.isEmpty() ? root.optInt("blog_num", 0): Integer.parseInt(stringID);
    }

    public int getFollowingCount() {
        return root.optInt("followings_count", 0);
    }

    public int getFansCount() {
        return root.optInt("fans_count", 0);
    }

    public void setCurrent(boolean v) {
        isCurrent = v;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Account) {
            return ((Account)obj).getUID() == getUID();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int)getUID();
    }

    @Override
    public String toString() {
        return getName();
    }
}
