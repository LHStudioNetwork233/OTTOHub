/**
 * @Author Hiro
 * @Date 2025/09/09 15:42
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.function;
import android.widget.*;
import com.losthiro.ottohubclient.impl.*;
import android.util.*;
import android.content.*;

public class Reset implements Runnable{
    public static final String TAG = "ottohub/client/reset";
    private Context mContext;
    
    public Reset(Context c) {
        mContext = c;
    }
    
    @Override
    public void run() {
        // TODO: Implement this method
        try {
            Toast.makeText(mContext, "操作成功", Toast.LENGTH_SHORT).show();
            ClientSettings.getInstance().reset();
        } catch (Exception e) {
            Log.e("Function", "reset failed ", e);
        }
    }
}
