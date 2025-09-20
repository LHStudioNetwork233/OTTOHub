/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.impl;
import android.content.*;
import org.json.*;
import com.losthiro.ottohubclient.utils.*;
import java.util.*;
import androidx.appcompat.app.*;
import com.losthiro.ottohubclient.*;
import cn.jzvd.*;

public class ClientSettings {
	private static final ClientSettings INSTANCE = new ClientSettings();
	private static final String MAINFEST = "config/mainfest.json";
	private static final HashMap<String, Object> mainfest = new HashMap<>();
    private static final HashMap<String, Object> defMap = new HashMap<>();
	private Context ctx;
    private long lastTime;//不知道该拿来干嘛

	public static final ClientSettings getInstance() {
		return INSTANCE;
	}

	public final synchronized void register(Context c) throws Exception {
		ctx = c;
		init();
	}

	public final synchronized void release() throws Exception {
		FileUtils.writeFile(ctx, FileUtils.getStorage(ctx, MAINFEST), mapToJson().toString(4));
	}

	public final synchronized void reset() throws Exception {
		mainfest.putAll(createDef());
	}

	private void init() throws Exception {
		String path = FileUtils.getStorage(ctx, MAINFEST);
		String setting = FileUtils.readFile(ctx, path);
	    defMap.putAll(createDef());
		if (setting.isEmpty()) {
			mainfest.putAll(defMap);
            FileUtils.AssetUtils.copyFileAssets(ctx, MAINFEST, FileUtils.getStorage(ctx, MAINFEST));
		} else {
            JSONObject root = new JSONObject(setting);
			JSONArray content = root.optJSONArray("setting");
			for (int i = 0; i < content.length(); i++) {
				JSONObject obj = content.optJSONObject(i);
				String name = obj.optString("name");
				if (!name.isEmpty()) {
					mainfest.putIfAbsent(name, obj.opt("value"));
				}
			}
			for (HashMap.Entry<String, Object> entry : defMap.entrySet()) {
				String name = entry.getKey();
				Object value = entry.getValue();
				if (!mainfest.containsKey(name)) {
					mainfest.put(name, value);
				}
			}
            lastTime = root.optLong("time");
		}
	}

	private JSONObject mapToJson() throws Exception {
		JSONObject settings = new JSONObject();
		JSONArray content = new JSONArray();
		for (HashMap.Entry<String, Object> setting : mainfest.entrySet()) {
			JSONObject json = new JSONObject();
			json.put("name", setting.getKey());
			json.put("value", setting.getValue());
			content.put(json);
		}
		settings.put("time", SystemUtils.getTime());
		settings.put("id", UUID.randomUUID().toString());
		settings.put("setting", content);
		return settings;
	}

	private HashMap<String, Object> createDef() throws Exception {
		HashMap<String, Object> def = new HashMap<>();
		JSONObject defJson = new JSONObject(FileUtils.AssetUtils.readAssetsFile(ctx, MAINFEST));
		JSONArray setting = defJson.optJSONArray("setting");
		for (int i = 0; i < setting.length(); i++) {
			JSONObject current = setting.optJSONObject(i);
            String name = current.optString("name");
            if (!name.isEmpty()) {
                defMap.put(name, current.opt("value"));
            }
		}
		return def;
	}

	public void putValue(String name, Object value) {
		mainfest.put(name, value);
	}

	public boolean getBoolean(String name) {
		Object value = mainfest.getOrDefault(name, false);
		if (value != null && value instanceof Boolean) {
			return (boolean) value;
		}
		return false;
	}

	public boolean getBoolean(String name, boolean def) {
		Object value = mainfest.getOrDefault(name, def);
		if (value != null && value instanceof Boolean) {
			return (boolean) value;
		}
		return def;
	}

	public int getInt(String name) {
		Object value = mainfest.getOrDefault(name, 0);
		if (value instanceof Integer) {
			return (int) value;
		}
		return 0;
	}

	public int getInt(String name, int def) {
		Object value = mainfest.getOrDefault(name, def);
		if (value instanceof Integer) {
			return (int) value;
		}
		return def;
	}

	public float getFloat(String name) {
		Object value = mainfest.getOrDefault(name, 0);
		if (value instanceof Float) {
			return (float) value;
		}
		return 0;
	}

	public float getFloat(String name, float def) {
		Object value = mainfest.getOrDefault(name, def);
		if (value instanceof Float) {
			return (float) value;
		}
		return def;
	}

	public String getString(String name) {
		return getString(name, new String());
	}

	public String getString(String name, String def) {
        Object obj = mainfest.getOrDefault(name, def);
        if (obj == null) {
            return def;
        }
		return obj.toString();
	}
    
    public long getLastUpdate() {
        return lastTime;
    }

	public static class SettingPool {//设置字典
		public static final String ACCOUNT_AUTO_LOGIN = "ottohub/account/auto_login";
		public static final String ACCOUNT_AUTO_REMOVE = "ottohub/account/auto_remove";

		public static final String PLAYER_BACKGROUND_PLAY = "ottohub/player/background_play";
		public static final String PLAYER_AUTO_QUIT = "ottohub/player/auto_quit";
        public static final String PLAYER_AUTO_FULLSCREEN = "ottohub/player/auto_fullscreen";
		public static final String PLAYER_IMAGE_DISPLAY = "ottohub/player/image_display";

		public static final String MSG_MARKDOWN_SURPPORT = "ottohub/msg/markdown_surpport";
        public static final String MSG_AUTO_SAVE = "ottohub/msg/auto_save";

		public static final String SYSTEM_CHECK_PERMISSION = "ottohub/system/permission_check";
        public static final String SYSTEM_CHECK_CLIPBOARD = "ottohub/system/check_clipboard";
		public static final String SYSTEM_CLICK_SOUND = "ottohub/system/click_sound";
		public static final String SYSTEM_SWITCH_THEME = "ottohub/system/switch_theme";
		public static final String SYSTEM_STORAGE_EDIT = "ottohub/system/storage_edit";
		public static final String SYSTEM_SPLASH_BG = "ottohub/system/splash_bg";
        public static final String SYSTEM_USE_DECOR = "ottohub/system/use_decor";
	}
}

