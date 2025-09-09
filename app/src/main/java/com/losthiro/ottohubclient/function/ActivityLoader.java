/**
 * @Author Hiro
 * @Date 2025/09/09 16:06
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.function;
import android.content.*;
import android.app.*;

public class ActivityLoader implements Runnable {
    public static final String TAG_INFO = "ottohub/client/about";
    private Context mContext;
    private Class<?> clz;
    
    public ActivityLoader(Context ctx, Class<?> activityClz) {
        mContext = ctx;
        clz = activityClz;
    }
    
	@Override
	public void run() {
		// TODO: Implement this method
        Intent i = new Intent(mContext, clz);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
	}
}

