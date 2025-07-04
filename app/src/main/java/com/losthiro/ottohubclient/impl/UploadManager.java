package com.losthiro.ottohubclient.impl;
import android.content.Context;
import android.os.Build;
import com.losthiro.ottohubclient.utils.DeviceUtils;
import com.losthiro.ottohubclient.utils.FileUtils;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Author Hiro
 * @Date 2025/06/19 16:50
 */
public class UploadManager {
    public static final String TAG = "UploadManager";
    private static UploadManager INSTANCE;
    private HashMap<Long, String> msgCache=new HashMap<>();
    private Context ctx;

    public static final synchronized UploadManager getInstance(Context c) {
        if (INSTANCE == null) {
            INSTANCE = new UploadManager(c);
        }
        return INSTANCE;
    }

    private UploadManager(Context c) {
        ctx = c;
    }

    public void putMsg(long uid, String msg) {
        msgCache.put(uid, msg);
    }

    public String getMsg(long uid) {
        return msgCache.get(uid);
    }

    public void save() {
        String path = StringUtils.strCat(DeviceUtils.getAndroidSDK() >= Build.VERSION_CODES.R ?ctx.getExternalFilesDir(null).toString(): "/sdcard/OTTOHub", "/config/content_cache.json");
        try {
            JSONArray config=new JSONArray();
            for (HashMap.Entry<Long, String> entry:msgCache.entrySet()) {
                JSONObject msg=new JSONObject();
                msg.put("reciever_uid", entry.getKey());
                msg.put("content", entry.getValue());
                config.put(msg);
            }
            if (FileUtils.createFile(ctx, path, config.toString(4))) {
                return;
            }
            FileUtils.writeFile(ctx, path, config.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        String path = StringUtils.strCat(DeviceUtils.getAndroidSDK() >= Build.VERSION_CODES.R ?ctx.getExternalFilesDir(null).toString(): "/sdcard/OTTOHub", "/config/content_cache.json");
        try {
            String content = FileUtils.readFile(ctx, path);
            if (content == null) {
                return;
            }
            JSONArray config=new JSONArray(content);
            for (int i = 0; i < config.length(); i++) {
                JSONObject msg=config.optJSONObject(i);
                putMsg(msg.optLong("reciever_uid", 0), msg.optString("content", "[填词时间]"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
