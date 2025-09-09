/**
 * @Author Hiro
 * @Date 2025/09/09 16:01
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.function;
import android.content.*;

public class Share implements Runnable {
    public static final String TAG = "ottohub/client/share";
    private Context mContext;
    private String mShareContent;
    
    public Share(Context ctx, String msg) {
        mContext = ctx;
        mShareContent = msg;
    }

    @Override
    public void run() {
        // TODO: Implement this method
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Intent.EXTRA_TEXT, mShareContent);
        mContext.startActivity(Intent.createChooser(i, "share"));
    }
}
