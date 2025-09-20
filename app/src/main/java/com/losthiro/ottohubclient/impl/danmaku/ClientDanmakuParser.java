package com.losthiro.ottohubclient.impl.danmaku;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import org.json.JSONArray;
import org.json.JSONObject;
import master.flame.danmaku.danmaku.model.Danmaku;
import android.graphics.Color;
import master.flame.danmaku.danmaku.model.L2RDanmaku;
import master.flame.danmaku.danmaku.model.Duration;
import master.flame.danmaku.danmaku.model.FBDanmaku;
import master.flame.danmaku.danmaku.model.FTDanmaku;
import java.util.LinkedList;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.util.DanmakuUtils;
import master.flame.danmaku.danmaku.model.android.DanmakuFactory;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.AlphaValue;
import master.flame.danmaku.danmaku.model.IDisplayer;
import com.losthiro.ottohubclient.utils.StringUtils;

/**
 * @Author Hiro
 * @Date 2025/06/03 01:02
 */
public class ClientDanmakuParser extends BaseDanmakuParser {
	public static final String TAG = "ClientDanmakuParser";
	private JSONArray danmakuData;
	private long danmakuDuration = 3000L;
	protected float mDispScaleX;
	protected float mDispScaleY;

	public ClientDanmakuParser(JSONArray json) {
		danmakuData = json;
	}

	@Override
	protected IDanmakus parse() {
		Danmakus result = new Danmakus(IDanmakus.ST_BY_TIME, false, mContext.getBaseComparator());
		for (int i = 0; i < danmakuData.length(); i++) {
			JSONDanmaku currentData = new JSONDanmaku(danmakuData.optJSONObject(i));
			BaseDanmaku data = createDanmaku(currentData.getMode());
			DanmakuUtils.fillText(data, currentData.getContent());
			mContext.mDanmakuFactory.fillAlphaData(data, AlphaValue.MAX, AlphaValue.MAX, danmakuDuration);
			data.padding = 5;
			data.priority = 0;
			data.isLive = false;
			data.textColor = currentData.getColor();
			data.textShadowColor = currentData.getColor() <= Color.BLACK ? Color.WHITE : Color.BLACK;
			data.textSize = currentData.getSize() * (getDisplayer().getDensity() - 0.6f);
			data.duration = new Duration(danmakuDuration);
			data.flags = mContext.mGlobalFlagValues;
			data.userId = (int) currentData.getID();
			data.setTime(currentData.getCurrentTime());
			if (data.text != null && data.duration != null) {
				data.setTimer(mTimer);
				synchronized (result.obtainSynchronizer()) {
					result.addItem(data);
				}
			}
		}
		return result;
	}

	@Override
	public BaseDanmakuParser setDisplayer(IDisplayer disp) {
		super.setDisplayer(disp);
		mDispScaleX = mDispWidth / DanmakuFactory.BILI_PLAYER_WIDTH;
		mDispScaleY = mDispHeight / DanmakuFactory.BILI_PLAYER_HEIGHT;
		return this;
	}

	private BaseDanmaku createDanmaku(String mode) {
		int type = BaseDanmaku.TYPE_SCROLL_LR;
		if (mode.equals("bottom")) {
			type = BaseDanmaku.TYPE_FIX_BOTTOM;
		}
		if (mode.equals("top")) {
			type = BaseDanmaku.TYPE_FIX_TOP;
		}
		return mContext.mDanmakuFactory.createDanmaku(type, mContext);
	}

	private static class JSONDanmaku {
		private JSONObject root;

		public JSONDanmaku(JSONObject obj) {
			root = obj;
		}

		public String getContent() {
			return root.optString("text", "OTTOHub─=≡Σ((( つ•̀ω•́)つ衝刺～♿️♿️♿️");
		}

		public long getID() {
			long id = root.optLong("danmaku_id");
			try {
				if (id == 0) {
					id = Long.parseLong(root.optString("danmaku_id"));
				}
			} catch (NumberFormatException unuse) {
			}
			return id;
		}

		public long getCurrentTime() {
			return (long) root.optDouble("time", 0) * 1000L;
		}

		public String getMode() {
			return root.optString("mode", "scroll");
		}

		public int getColor() {
			return Color.parseColor(root.optString("color", "#ffffff"));
		}

		public int getSize() {
			return Integer.parseInt(root.optString("font_size", "20px").replace("px", ""));
		}

		public String getRenderCommand() {
			return root.optString("render", "");
		}
	}
}

