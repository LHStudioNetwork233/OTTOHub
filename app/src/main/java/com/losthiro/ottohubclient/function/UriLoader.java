/**
 * @Author Hiro
 * @Date 2025/09/09 15:47
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.function;
import com.losthiro.ottohubclient.utils.*;
import android.content.*;

public class UriLoader implements Runnable {
    public static final String TAG_GROUP = "ottohub/client/group";
    private Context mContext;
    private String mSource;
    
    public UriLoader(Context ctx, String uri) {
        mContext = ctx;
        mSource = uri;
    }

    @Override
    public void run() {
        // TODO: Implement this method
        SystemUtils.loadUri(mContext, mSource);
    }
}
