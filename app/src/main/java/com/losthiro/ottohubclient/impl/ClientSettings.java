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

public class ClientSettings {
	private static final ClientSettings INSTANCE = new ClientSettings();
	private static final String MAINFEST = "config/mainfest.json";
	private HashMap<String, Object> mainfest = new HashMap<>();
	private Context ctx;

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

	public final synchronized void reset() {
		mainfest.putAll(createDef());
	}

	private void init() throws Exception {
		String path = FileUtils.getStorage(ctx, MAINFEST);
		String setting = FileUtils.readFile(ctx, path);
		HashMap<String, Object> def = createDef();
		if (setting.isEmpty()) {
			mainfest.putAll(def);
			FileUtils.createFile(ctx, path, mapToJson().toString(4));
		} else {
			JSONArray content = new JSONObject(setting).optJSONArray("setting");
			for (int i = 0; i < content.length(); i++) {
				JSONObject obj = content.optJSONObject(i);
				String name = obj.optString("name");
				if (name != null) {
					mainfest.putIfAbsent(name, obj.opt("value"));
				}
			}
			for (HashMap.Entry<String, Object> entry : def.entrySet()) {
				String name = entry.getKey();
				Object value = entry.getValue();
				if (!mainfest.containsKey(name)) {
					mainfest.put(name, value);
				}
			}
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

	private HashMap<String, Object> createDef() {
		HashMap<String, Object> defMap = new HashMap<>();
		defMap.put(SettingPool.ACCOUNT_AUTO_LOGIN, true);
		defMap.put(SettingPool.ACCOUNT_AUTO_REMOVE, false);
		defMap.put(SettingPool.MSG_MARKDOWN_SURPPORT, true);
		defMap.put(SettingPool.SYSTEM_CHECK_PERMISSION, false);
		return defMap;
	}

	public void putValue(String name, Object value) {
		mainfest.put(name, value);
	}

	public boolean getBoolean(String name) {
		Object value = mainfest.get(name);
		if (value != null && value instanceof Boolean) {
			return (boolean) value;
		}
		return false;
	}

	public int getInt(String name) {
		Object value = mainfest.getOrDefault(name, 0);
		if (value instanceof Integer) {
			return (int) value;
		}
		return 0;
	}

	public float getFloat(String name) {
		Object value = mainfest.getOrDefault(name, 0);
		if (value instanceof Float) {
			return (float) value;
		}
		return 0;
	}

	public String getString(String name) {
		Object value = mainfest.getOrDefault(name, new String());
		if (value instanceof String) {
			return (String) value;
		}
		return new String();
	}

	public static class SettingPool {
		public static final String ACCOUNT_AUTO_LOGIN = "ottohub/account/auto_login";
		public static final String ACCOUNT_AUTO_REMOVE = "ottohub/account/auto_remove";

		public static final String MSG_MARKDOWN_SURPPORT = "ottohub/msg/markdown_surpport";

		public static final String SYSTEM_CHECK_PERMISSION = "ottohub/system/permission_check";
	}
}

