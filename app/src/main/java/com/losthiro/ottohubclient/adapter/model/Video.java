package com.losthiro.ottohubclient.adapter.model;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.StringUtils;
import com.losthiro.ottohubclient.utils.SystemUtils;
import java.io.File;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import com.losthiro.ottohubclient.utils.FileUtils;

/**
 * @Author Hiro
 * @Date 2025/05/21 18:14
 */
public class Video {
	public static final String TAG = "Video";
	public static final int VIDEO_DEF = 0;
	public static final int VIDEO_DETAIL = 1;
	private Context main;
	private int type;
	private boolean isLocal;
	private JSONObject root;
	private File local;

	public Video(Context ctx, JSONObject json, int t) {
		main = ctx;
		type = t;
		root = json;
		isLocal = false;
	}

	public Video(Context ctx, File v, int t) {
		main = ctx;
		local = v;
		type = t;
		isLocal = true;
	}

	private long getID(String type) {
		if (root == null) {
			return -1;
		}
		String stringID = root.optString(type, null);
		return stringID == null ? root.optLong(type, -1) : Long.parseLong(stringID);
	}

	private String getCount(String type) {
		String def = StringUtils.toStr(0);
		if (root == null) {
			return def;
		}
		String strCount = root.optString(type, def);
		long count = Long.parseLong(strCount);
		if (count >= 1000) {
			return count / 1000 + "k";
		}
		if (count >= 10000) {
			return count / 10000 + "w";
		}
		return strCount;
	}

	private String getName(String type) {
		String def = "棍母";
		if (root == null) {
			return def;
		}
		return root.optString(type, def);
	}

	public boolean isLocal() {
		return isLocal;
	}

	public int getType() {
		return type;
	}

	public long getVID() {
		return getID("vid");
	}

	public String getTitle() {
		return getName("title");
	}

	public String getTime() {
		String format = "yyyy-MM-dd HH:mm:ss";
		String def = SystemUtils.getDate(format);
		if (root == null) {
			return def;
		}
		String video = root.optString("time", def);
		long time = SystemUtils.getTime() - SystemUtils.getTime(video, format);
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
		return getCount("view_count") + "播放";
	}

	public void setCover(ImageView target) {
		Bitmap ic = BitmapFactory.decodeResource(main.getResources(), R.drawable.ic_def_video_cover);
		try {
			if (root == null) {
				target.setImageBitmap(ic);
				return;
			}
			String uri = root.optString("cover_url", "null");
			if (uri.equals("null")) {
				target.setImageBitmap(ic);
				return;
			}
			ImageDownloader.loader(target, uri);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Bitmap getCover() {
		File cover = new File(local, "cover");
		return cover.exists()
				? BitmapFactory.decodeFile(cover.getPath())
				: BitmapFactory.decodeResource(main.getResources(), R.drawable.ic_def_video_cover);
	}

	public long getUID() {
		return getID("uid");
	}

	public String getUser() {
		return getName("username");
	}

	public void setAvatar(ImageView target) {
		Bitmap ic = BitmapFactory.decodeResource(main.getResources(), R.drawable.ic_unloading_user);
		try {
			if (root == null) {
				target.setImageBitmap(ic);
			}
			String uri = root.optString("avatar_url", "null");
			ImageDownloader.loader(target, uri);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Bitmap getAvatar() {
		File avatar = new File(local, "user_avatar");
		return avatar.exists()
				? BitmapFactory.decodeFile(avatar.getPath())
				: BitmapFactory.decodeResource(main.getResources(), R.drawable.ic_unloading_user);
	}

	public String getRootPath() {
		return local.getPath();
	}

	public File getVideo() {
		return new File(local, "video");
	}

	public JSONObject getInfos(Context c) throws JSONException {
		File danmaku = new File(local, "mainfest.json");
		return new JSONObject(FileUtils.readFile(c, danmaku.getPath()));
	}

	public JSONArray getDanmakuList(Context c) throws JSONException {
		File danmaku = new File(local, "danmaku_config.json");
		return new JSONArray(FileUtils.readFile(c, danmaku.getPath()));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Video) {
			Video another = (Video) obj;
			return isLocal ? getVideo().equals(another.getVideo()) : another.getVID() == getVID();
		}
		return false;
	}

	@Override
	public int hashCode() {
		// TODO: Implement this method
		return isLocal ? getVideo().hashCode() : (int) getVID();
	}
}

