/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter.model;
import org.json.*;
import com.losthiro.ottohubclient.utils.*;
import java.io.*;
import android.graphics.*;
import android.content.*;
import com.losthiro.ottohubclient.R;

public class ManagerModel {
	public static final int TYPE_VIDEO = 0;
	public static final int TYPE_BLOG = 1;

	public static final int STATUS_DEF = 0;//审核中
	public static final int STATUS_APPROVE = 1;//已过审
	public static final int STATUS_REJECT = 2;//未过审

	private int type;
    private boolean isLocal;
	private JSONObject root;
    private File local;

	public ManagerModel(JSONObject json, int t) {
		type = t;
		root = json;
        isLocal = false;
	}
    
    public ManagerModel(File v, int t) {
        local = v;
        type = t;
        isLocal = true;
	}

	public int getType() {
		return type;
	}
    
    public boolean isLocal(){
        return isLocal;
    }

	public long getBID() {
		String stringID = root.optString("bid", null);
		return stringID == null ? root.optLong("bid", -1) : Long.parseLong(stringID);
	}

	public long getVID() {
		String stringID = root.optString("vid", null);
		return stringID == null ? root.optLong("vid", -1) : Long.parseLong(stringID);
	}

	public long getUID() {
		String stringID = root.optString("uid", null);
		return stringID == null ? root.optLong("uid", -1) : Long.parseLong(stringID);
	}

	public String getTitle() {
		return root.optString("title", "大家好啊，今天来点大家想看的东西");
	}

	public String getContent() {
		return root.optString("content", "[填词时间]");
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
		String def = StringUtils.toStr(0);
		if (root == null) {
			return def;
		}
		String strCount = root.optString("like_count", def);
		long count = Long.parseLong(strCount);
		if (count >= 1000) {
			strCount = count / 1000 + "k";
		} else if (count >= 10000) {
			strCount = count / 10000 + "w";
		}
		return strCount + "获赞";
	}

	public String getFavoriteCount() {
		String def = StringUtils.toStr(0);
		if (root == null) {
			return def;
		}
		String strCount = root.optString("favorite_count", def);
		long count = Long.parseLong(strCount);
		if (count >= 1000) {
			strCount = count / 1000 + "k";
		} else if (count >= 10000) {
			strCount = count / 10000 + "w";
		}
		return strCount + "冷藏";
	}

	public String getViewCount() {
		String def = StringUtils.toStr(0);
		if (root == null) {
			return def;
		}
		String strCount = root.optString("view_count", def);
		long count = Long.parseLong(strCount);
		if (count >= 1000) {
			strCount = count / 1000 + "k";
		} else if (count >= 10000) {
			strCount = count / 10000 + "w";
		}
		return strCount + (type == TYPE_VIDEO ? "播放" : "浏览");
	}

	public boolean isDeleted() {
		String stringID = root.optString("is_delete", null);
		int status = stringID == null ? root.optInt("is_delete", -1) : Integer.parseInt(stringID);
		return status > 0;
	}

	public int getAuditStatus() {
		String stringID = root.optString("audit_status", null);
		return stringID == null ? root.optInt("audit_status", -1) : Integer.parseInt(stringID);
	}

	public String getAvatar() {
		return root.optString("avatar_url");
	}

	public String getCover() {
		return root.optString("cover_url");
	}
    
    public Bitmap getCover(Context ctx) {
        File cover = new File(local, "cover");
        return cover.exists()
            ? BitmapFactory.decodeFile(cover.getPath())
            : BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_def_video_cover);
	}
    
    public String getRootPath() {
        return local.getPath();
    }

    public File getVideo() {
        return new File(local, "video");
	}
    
    public Bitmap getAvatar(Context ctx) {
        File avatar = new File(local, "user_avatar");
        return avatar.exists()
            ? BitmapFactory.decodeFile(avatar.getPath())
            : BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_unloading_user);
	}
    
    public JSONObject getInfos(Context c) throws JSONException {
        File danmaku = new File(local, "mainfest.json");
        return new JSONObject(FileUtils.readFile(c, danmaku.getPath()));
	}
}

