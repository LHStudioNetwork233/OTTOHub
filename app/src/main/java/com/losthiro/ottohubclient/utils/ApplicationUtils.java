package com.losthiro.ottohubclient.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.graphics.drawable.Drawable;

public class ApplicationUtils {
    private static final String TAG = "ApplicationUtils";

    public static String getPackage(Context c) {
        return c.getPackageName();
    }

    public static String getName(Context c, String packName) {
        try {
            PackageManager manager=c.getPackageManager();
            ApplicationInfo info = manager.getApplicationInfo(packName, 8);
            return info.loadLabel(manager).toString();
        } catch (Exception e) {
            Log.e(TAG, " get Name ERROR: ", e);
            return null;
        }
    }

    public static Drawable getIcon(Context c, String packName) {
        try {
            return c.getPackageManager().getApplicationIcon(packName);
        } catch (Exception e) {
            Log.e(TAG, " get Icon ERROR: ", e);
            return null;
        }
    }

    public static int getVersionCode(Context c, String packName) {
        try {
            return c.getPackageManager().getPackageInfo(packName, 0).versionCode;
        } catch (Exception e) {
            Log.e(TAG, " get Version Code ERROR: ", e);
            return 0;
        }
    }

    public static String getVersionName(Context c, String packName) {
        try {
            return c.getPackageManager().getPackageInfo(packName, 0).versionName;
        } catch (Exception e) {
            Log.e(TAG, " get Version Name ERROR: ", e);
            return null;
        }
    }

    public static int getTargetSDK(Context c, String packName) {
        try {
            return c.getPackageManager().getApplicationInfo(packName, 0).targetSdkVersion;
        } catch (Exception e) {
            Log.e(TAG, " get Target SDK ERROR: ", e);
            return 0;
        }
    }

    public static int getMinSDK(Context c, String packName) {
        try {
            return c.getPackageManager().getApplicationInfo(packName, 0).minSdkVersion;
        } catch (Exception e) {
            Log.e(TAG, " get Min SDK ERROR: ", e);
            return 0;
        }
    }
}
