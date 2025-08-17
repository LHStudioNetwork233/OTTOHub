/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter.model;
import org.json.*;

public class AuditModel {
	public static final int TYPE_VIDEO = 0;
	public static final int TYPE_BLOG = 1;
    public static final int TYPE_AVATAR = 2;
	public static final int TYPE_COVER = 3;

	private int type;
	private JSONObject main;

	public AuditModel(JSONObject json, int t) {
		type = t;
		main = json;
	}

	public int getType() {
		return type;
	}

	public long getVID() {
		String stringID = main.optString("vid", null);
		return stringID == null ? main.optLong("vid", -1) : Long.parseLong(stringID);
	}

	public String getTitle() {
		return main.optString("title", "大家好啊，今天来点大家想看的东西");
	}
    
    public String getIntro(){
        return main.optString("intro", "大家好啊，我是电棍");
    }

	public String[] getTags() {
		return main.optString("tag", "#棍母").split("#");
	}
    
    public String getCover(){
        return main.optString("cover_url");
    }
    
    public String getVideo(){
        return main.optString("video_url");
    }
    
    public long getBID() {
        String stringID = main.optString("bid", null);
        return stringID == null ? main.optLong("bid", -1) : Long.parseLong(stringID);
    }
    
    public String getContent(){
        return main.optString("content", "[填词时间]");
    }

    public long getUID() {
        String stringID = main.optString("uid", null);
        return stringID == null ? main.optLong("uid", -1) : Long.parseLong(stringID);
	}
    
    public String getName(){
        return main.optString("username", "棍母");
    }
    
    public String getAvatar(){
        return main.optString("avatar_url");
    }
}

