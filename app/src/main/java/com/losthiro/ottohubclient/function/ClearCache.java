/**
 * @Author Hiro
 * @Date 2025/09/09 11:54
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.function;
import com.losthiro.ottohubclient.*;
import android.content.*;

public class ClearCache implements Runnable {
    public static final String TAG = "ottohub/storage/clear_cache";
	private Context mContext;

	public ClearCache(BasicActivity act) {
		mContext = act;
	}

	@Override
	public void run() {
		// TODO: Implement this method
		((BasicActivity) mContext).cleanCache(true);
	}
}

