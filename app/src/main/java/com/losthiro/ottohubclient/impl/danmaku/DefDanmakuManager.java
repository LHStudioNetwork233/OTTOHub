package com.losthiro.ottohubclient.impl.danmaku;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import com.losthiro.ottohubclient.utils.DeviceUtils;
import com.losthiro.ottohubclient.utils.FileUtils;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import master.flame.danmaku.danmaku.model.AlphaValue;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.Duration;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.util.DanmakuUtils;
import org.json.JSONArray;
import org.json.JSONException;
import com.losthiro.ottohubclient.*;

/**
 * @Author Hiro
 * @Date 2025/06/06 10:30
 */
public class DefDanmakuManager {
    public static final String TAG = "DefDanmakuManager";
    private static final List<String> data = new ArrayList<>();
    private static DefDanmakuManager INSTANCE;
    private Context main;

    public static final synchronized DefDanmakuManager getInstance(Context c) {
        if (INSTANCE == null) {
            INSTANCE = new DefDanmakuManager(c);
        }
        return INSTANCE;
    }

    private DefDanmakuManager(Context c) {
        main = c;
        initData();
    }

    private void initData() {
        File f=new File(FileUtils.getStorage(main, BasicActivity.FILE_DEF_DICTIONARY));
        if (!f.exists()) {
            FileUtils.AssetUtils.copyFileAssets(main, BasicActivity.FILE_DEF_DICTIONARY, f.toString());
        }
        String content=FileUtils.readFile(main, f.toString());
        try {
            JSONArray jsonData=new JSONArray(content);
            for (int i = 0; i < jsonData.length(); i++) {
                data.add(jsonData.optString(i, "棍母"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public BaseDanmakuParser createParser() {
        return new BaseDanmakuParser(){
            @Override
            protected IDanmakus parse() {
                long danmakuDuration=3000L;
                Danmakus result = new Danmakus(IDanmakus.ST_BY_TIME, false, mContext.getBaseComparator());
                for (String text: data) {
                    BaseDanmaku data=mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_LR, mContext);
                    DanmakuUtils.fillText(data, text);
                    mContext.mDanmakuFactory.fillAlphaData(data, AlphaValue.MAX, AlphaValue.MAX, danmakuDuration);
                    data.padding = 5;
                    data.priority = 0;
                    data.isLive = false;
                    data.textColor = -StringUtils.rng(0, 0xffffff);
                    data.textShadowColor = Color.BLACK;
                    data.textSize = StringUtils.rng(20, 45) * (getDisplayer().getDensity() - 0.6f);
                    data.duration = new Duration(danmakuDuration);
                    data.flags = mContext.mGlobalFlagValues;
                    data.setTime(StringUtils.rng(1919, 114514));
                    if (data.text != null && data.duration != null) {
                        data.setTimer(mTimer);
                        synchronized (result.obtainSynchronizer()) {
                            result.addItem(data);
                        }
                    }
                }
                return result;
            }
        };
    }

    public String getRandomString() {
        if (data.isEmpty()) {
            return "棍母";
        }
        return data.get(StringUtils.rng(0, data.size() - 1));
    }
}
