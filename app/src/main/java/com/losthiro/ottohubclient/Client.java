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
import java.util.*;
import android.app.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.view.window.*;

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
        ResourceUtils.init(main);
        initSettings(main);
        Log.i(TAG, "client init complete");
        Log.i(TAG, "Hello OTTOHub!");
    }

    @Override
    public void onTerminate() {
        Log.i(TAG, "————Application OnDestory————");
        //InfoWindow.getInstance(null).release();
        super.onTerminate();
        activitys.clear();
        ImageDownloader.release();
    }
    
    public static void initSettings(Context c){
        ClientSettings settings = ClientSettings.getInstance();
        try {
            settings.register(c);
        } catch (Exception e) {
            Log.e(TAG, "setting register failed", e);
        }
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
    
    public static Activity getCurrentActivity(Context context) {
        try {
            Class<?> cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread", new Class[0]);
            declaredMethod.setAccessible(true);
            Object invoke = declaredMethod.invoke(null, new Object[0]);
            Field declaredField = cls.getDeclaredField("mActivities");
            declaredField.setAccessible(true);
            Object obj = declaredField.get(invoke);
            if (obj instanceof Map) {
                Collection values = ((Map) obj).values();
                for (Object obj2 : values) {
                    if (obj2 != null) {
                        Field declaredField2 = obj2.getClass().getDeclaredField("activity");
                        declaredField2.setAccessible(true);
                        Activity activity = (Activity) declaredField2.get(obj2);
                        if (isCurrentActivity(activity, context)) {
                            return activity;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isCurrentActivity(Activity activity, Context context) {
        List<ActivityManager.RunningTaskInfo> runningTasks;
        if (activity == null || (runningTasks = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1)) == null || runningTasks.size() <= 0) {
            return false;
        }
        return activity.getClass().getName().equals(((TaskInfo) runningTasks.get(0)).topActivity.getClassName());
    }
}
