package com.losthiro.ottohubclient.utils;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.AssetManager;
import java.io.InputStream;
import java.io.IOException;
import android.os.*;
import androidx.core.content.*;
import androidx.appcompat.content.res.*;
import android.content.res.*;
import com.losthiro.ottohubclient.*;
import android.app.*;

public class ResourceUtils {
	public static final String TAG = "ResourceUtils";
	private static String pkg;
	private static AssetManager assets;
	private static Resources res;
	private static Context c;

	public static void init(Context ctx) {
		c = ctx;
		pkg = ctx.getPackageName();
		res = ctx.getResources();
		assets = ctx.getAssets();
	}

	public static int getResID(String name, String type) {
		return getStroageID(name, type, pkg);
	}

	public static int getStroageID(String name, String type, String pack) {
		return res.getIdentifier(name, type, pack);
	}

	public static int getColor(int resID) {
		return getColor(Client.getCurrentActivity(c), resID);
	}
    
    public static int getColor(Activity act, int resID) {
        //int def = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? c.getColor(resID) : res.getColor(resID);
        return act == null ? ContextCompat.getColor(c, resID):ContextCompat.getColor(act, resID);
	}

	public static InputStream getAssetsFile(String path) throws IOException {
		return assets.open(path);
	}
}

