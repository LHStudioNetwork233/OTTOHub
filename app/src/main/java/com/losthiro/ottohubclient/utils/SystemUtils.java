package com.losthiro.ottohubclient.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import java.util.ArrayList;
import java.util.List;
import com.losthiro.ottohubclient.BasicActivity;
import com.losthiro.ottohubclient.SplashActivity;
import java.text.DateFormat;
import java.util.logging.SimpleFormatter;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import androidx.core.app.ActivityCompat;

public class SystemUtils {
    private static final String TAG="IntentUtils";

    public static void loadActivity(Context c, Class<?> activity) {
        try {
            Intent i=new Intent();
            i.setClass(c, activity);
            c.startActivity(i);
        } catch (Exception e) {
            Log.e(TAG, "activity error: ", e);
        }
    }

    public static void loadActivity(Context c, String activity) {
        try {
            Intent i=new Intent();
            i.setClass(c, Class.forName(activity));
            c.startActivity(i);
        } catch (Exception e) {
            Log.e(TAG, "activity error: ", e);
        }
    }

    public static void loadUri(Context c, String uri) {
        if (uri == null || uri.isEmpty()) {
            return;
        }
        try {
            Intent i=new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setData(Uri.parse(uri));
            c.startActivity(i);
        } catch (Exception e) {
            Log.e(TAG, "uri load failed", e);
        }
    }

    public static void loadApp(Context c, String packName) {
        Intent launch = c.getPackageManager().getLaunchIntentForPackage(packName);
        c.startActivity(launch);
    }

    public static boolean hasApp(Context c, String packName) {
        if (packName == null || packName.isEmpty()) {
            return false;
        }
        try {
            c.getPackageManager().getPackageInfo(packName, 0);
        } catch (Exception x) {
            return false;
        }
        return true;
    }

    public static void restart(Activity act) {
        Intent intent = new Intent(act, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        act.startActivity(intent);
        act.finish();
    }

    public static void exit() {
        System.exit(0);
    }

    public static String getDate(String timeFormat) {
        return new SimpleDateFormat(timeFormat).format(new Date());
    }
    
    public static String getDate(String timeFormat, long date) {
        return new SimpleDateFormat(timeFormat).format(new Date(date));
    }
    
    public static long getTime(String date, String format){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date d = sdf.parse(date);
            // 获取时间戳（自1970年1月1日以来的毫秒数）
            return d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return getTime();
        }
    }

    public static long getTime() {
        return System.currentTimeMillis();
    }
    
    public static long getJVMmaxMemory(){
        return Runtime.getRuntime().maxMemory();
    }
    
    public static boolean clearCache(Context context) {
        return FileUtils.deleteDir(context.getCacheDir().getPath()) && (context.getExternalCacheDir() != null ? FileUtils.deleteDir(context.getExternalCacheDir().getPath()) : true);
    }

    public static int getStatusBarHeight(Context context) {
        Rect rectangle = new Rect();
        Window window = ((Activity) context).getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return rectangle.top;
    }
}
