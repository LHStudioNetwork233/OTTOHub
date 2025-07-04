package com.losthiro.ottohubclient;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.losthiro.ottohubclient.crashlogger.CrashManager;
import com.losthiro.ottohubclient.utils.ResourceUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.impl.UploadManager;

/**
 * @Author Hiro
 * @Date 2025/05/21 14:17
 */
public class Client extends Application {
    public static final String TAG = "Client";
    private static final LinkedList<Intent> activitys=new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "————Application OnCreate————");
        Context main=getApplicationContext();
        CrashManager.getInstance().register(main);
        UploadManager.getInstance(main).load();
        ResourceUtils.getInstance(main);
        Log.i(TAG, "client init complete");
        Log.i(TAG, "Hello OTTOHub!");
    }

    @Override
    public void onTerminate() {
        Log.i(TAG, "————Application OnDestory————");
        super.onTerminate();
        activitys.clear();
        ImageDownloader.release();
    }

    public static void saveActivity(Intent i) {
        activitys.add(i);
    }

    public static Intent getLastActivity() {
        return activitys.size() == 0 ?null: activitys.getLast();
    }

    public static void removeActivity() {
        activitys.removeLast();
    }
    
    public static boolean isFinishingLast(Intent i){
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object activityThread = currentActivityThreadMethod.invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Object activities = activitiesField.get(activityThread);
            if (activities instanceof java.util.Map) {
                java.util.Map<?, ?> activityMap = (java.util.Map<?, ?>) activities;
                for (Object activityRecord : activityMap.values()) {
                    if (activityRecord != null) {
                        Class<?> activityRecordClass = activityRecord.getClass();
                        Field activityField = activityRecordClass.getDeclaredField("activity");
                        activityField.setAccessible(true);
                        Activity activity = (Activity) activityField.get(activityRecord);
                        return activity.getIntent().filterEquals(i);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
