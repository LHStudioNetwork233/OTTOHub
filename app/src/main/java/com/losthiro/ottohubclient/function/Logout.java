/**
 * @Author Hiro
 * @Date 2025/09/09 11:50
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.function;
import android.widget.*;
import com.losthiro.ottohubclient.impl.*;
import android.content.*;

public class Logout implements Runnable{
    public static final String TAG = "ottohub/account/logout";
    private Context mContext;
    
    public Logout(Context ctx) {
        mContext = ctx;
    }
    
    @Override
    public void run() {
        // TODO: Implement this method
        Toast.makeText(mContext, "退出登录成功", Toast.LENGTH_SHORT).show();
        AccountManager.getInstance(mContext).logout();
    }
}
