package com.losthiro.ottohubclient.impl;

/**
 * @Author Hiro
 * @Date 2025/06/21 14:52
 */
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;

public class PermissionHelper {
    public static final String TAG = "PermissionHelper";
    private static final int REQ_CODE = 0x50;
    private static PermissionCallback callback;
    private static String[] requiredPermissions;

    public interface PermissionCallback {
        void onAllGranted();
        void onDeniedWithNeverAsk();
    }

    public static void requestPermissions(Activity host, String[] permissions, PermissionCallback cb) {
        requiredPermissions = permissions;
        callback = cb;

        if (checkAllGranted(host)) {
            cb.onAllGranted();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && needManageExternalStorage(permissions)) {
            openManageAllFiles(host);
        } else {
            ActivityCompat.requestPermissions(host, permissions, REQ_CODE);
        }
    }

    private static boolean checkAllGranted(Activity context) {
        for (String perm : requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void handleResult(Activity host, int requestCode, int[] grantResults) {
        if (requestCode != REQ_CODE || callback == null) return;

        boolean allGranted = true;
        boolean hasNeverAsk = false;

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                if (!ActivityCompat.shouldShowRequestPermissionRationale(host, requiredPermissions[0])) {
                    hasNeverAsk = true;
                }
            }
        }

        if (allGranted) {
            callback.onAllGranted();
        } else if (hasNeverAsk) {
            callback.onDeniedWithNeverAsk();
            openAppSettings(host);
        }
    }

    private static void openAppSettings(Activity context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    private static void openManageAllFiles(Activity context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        context.startActivity(intent);
    }

    private static boolean needManageExternalStorage(String[] perms) {
        for (String perm : perms) {
            if (perm.equals(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
                return true;
            }
        }
        return false;
    }
}
