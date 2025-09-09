/**
 * @Author Hiro
 * @Date 2025/09/09 12:08
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.function;
import com.losthiro.ottohubclient.impl.*;
import android.widget.*;
import com.losthiro.ottohubclient.*;
import android.app.*;

public class RequestPermission implements Runnable{
    public static final String TAG = "ottohub/client/request_permission";
    private Activity a;
    
    public RequestPermission(Activity act) {
        a = act;
    }
    
    @Override
    public void run() {
        // TODO: Implement this method
        PermissionHelper.requestPermissions(a,
            new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"},
            new PermissionHelper.PermissionCallback() {
                @Override
                public void onAllGranted() {
                    Toast.makeText(a, "权限授予成功", Toast.LENGTH_SHORT).show();
                    Client.initSettings(a);
                }

                @Override
                public void onDeniedWithNeverAsk() {
                    Toast.makeText(a, "权限已拒绝(后续可在设置重新授予)", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
