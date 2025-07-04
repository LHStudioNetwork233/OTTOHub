package com.losthiro.ottohubclient.utils;

import android.os.Build;
import android.content.Context;
import android.app.ActivityManager;
import android.util.Log;
import android.os.storage.StorageManager;
import java.util.UUID;
import java.io.File;

public class DeviceUtils {
    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public static int getAndroidSDK() {
        return Build.VERSION.SDK_INT;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static int getWindowWidth(Context c) {
        return c.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getWindowHeight(Context c) {
        return c.getResources().getDisplayMetrics().heightPixels;
    }

    public static long getAvailableMemory(Context c) {
        ActivityManager activityManager = c.getSystemService(ActivityManager.class);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    public static long getAllocatableBytes(Context c, String path) {
        try {
            StorageManager storage=c.getSystemService(StorageManager.class);
            UUID id= storage.getUuidForPath(new File(path));
            return storage.getAllocatableBytes(id);
        } catch (Exception e) {
            Log.e("DeviceUtils", " get Allocatable Bytes ERROR: ", e);
            return 0;
        }
    }
}
