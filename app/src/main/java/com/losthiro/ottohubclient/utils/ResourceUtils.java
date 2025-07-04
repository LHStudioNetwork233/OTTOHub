package com.losthiro.ottohubclient.utils;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.AssetManager;
import java.io.InputStream;
import java.io.IOException;

public class ResourceUtils {
    public static final String TAG = "ResourceUtils";
    private static ResourceUtils resUtil;
    private static String pkg;
    private static AssetManager assets;
    private static Resources res;

    private ResourceUtils(Context ctx) {
        pkg = ctx.getPackageName();
        res = ctx.getResources();
        assets = ctx.getAssets();
    }

    public static final synchronized ResourceUtils getInstance(Context ctx) {
        if (resUtil == null) {
            resUtil = new ResourceUtils(ctx);
        }
        return resUtil;
    }

    public int getResID(String name, String type) {
        return getStroageID(name, type, pkg);
    }

    public int getStroageID(String name, String type, String pack) {
        return res.getIdentifier(name, type, pack);
    }

    public InputStream getAssetsFile(String path) throws IOException {
        return assets.open(path);
    }
}
