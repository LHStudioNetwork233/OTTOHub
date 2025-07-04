package com.losthiro.ottohubclient.impl;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.losthiro.ottohubclient.AccountActivity;
import com.losthiro.ottohubclient.BlogActivity;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.VideosActivity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import com.losthiro.ottohubclient.LoginActivity;
import com.losthiro.ottohubclient.view.drawer.SlideDrawerManager;
import android.graphics.PorterDuff;

/**
 * @Author Hiro
 * @Date 2025/05/29 16:08
 */
public class TypeManager {
    public static final String TAG = "TypeManager";
    private static final TextView[] types=new TextView[3];
    private static int typeIndex;

    public static void initTypes(View parent) {
        types[0] = parent.findViewWithTag("main");
        types[1] = parent.findViewWithTag("blog");
        types[2] = parent.findViewWithTag("account");
        for (int i = 0; i < 3; i++) {
            final int index=i;
            TextView current = types[i];
            if (current == null) {
                return;
            }
            if (typeIndex == i) {
                current.getCompoundDrawables()[1].setColorFilter(Color.parseColor("#88d9fa"), PorterDuff.Mode.SRC_IN);
            }
            current.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        runTypeInstance(index);
                    }
                });
        }
    }

    public static void runTypeInstance(int index) {
        if (typeIndex == index) {
            return;
        }
        Activity main=VideosActivity.activity.get();
        View p=main.findViewById(android.R.id.content);
        TextView[] typesCache={p.findViewWithTag("main"), p.findViewWithTag("blog"), p.findViewWithTag("account")};
        for (int i = 0; i < types.length; i++) {
            TextView current = types[i];
            int color=i == index ?Color.parseColor("#88d9fa"): Color.BLACK;
            current.getCompoundDrawables()[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            current.setTextColor(color);
            TextView m = typesCache[i];
            m.getCompoundDrawables()[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            m.setTextColor(color);
        }
        Activity a=getCurrentActivity(main.getApplicationContext());
        switch (index) {
            case 0:
                if (!a.getClass().getName().equals(main.getClass().getName())) {
                    a.finish();
                }
                break;
            case 1:
                if (!a.getClass().getName().equals(main.getClass().getName())) {
                    a.finish();
                }
                main.startActivity(new Intent(main, BlogActivity.class));
                break;
            case 2:
                if (!a.getClass().getName().equals(main.getClass().getName())) {
                    a.finish();
                }
                AccountManager manager=AccountManager.getInstance(main.getApplicationContext());
                if (manager.isLogin()) {
                    main.startActivity(new Intent(main, AccountActivity.class));
                    break;
                }
                main.startActivity(new Intent(main, LoginActivity.class));
                break;
            default:
                break;
        }
        a.overridePendingTransition(0x010a0000, 0x010a0001);
        typeIndex = index;
    }

    public static Activity getCurrentActivity(Context context) {
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
        if (activity == null) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
        if (runningTasks != null && runningTasks.size() > 0) {
            ActivityManager.RunningTaskInfo runningTask = runningTasks.get(0);
            String currentActivityClassName = runningTask.topActivity.getClassName();
            return activity.getClass().getName().equals(currentActivityClassName);
        }
        return false;
    }
}
