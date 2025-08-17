package com.losthiro.ottohubclient.adapter.model;
import org.json.JSONObject;

/**
 * @Author Hiro
 * @Date 2025/05/27 14:19
 */
public class User {
    public static final String TAG = "User";
    private JSONObject root;

    public User(JSONObject main) {
        root = main;
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
    
    public String getAvatarURI() {
        return root.optString("avatar_url", "null");
    }
}
