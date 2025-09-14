/**
 * @Author Hiro
 * @Date 2025/09/09 15:52
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.function;
import android.app.*;
import android.widget.*;
import android.content.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.*;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.utils.*;
import org.json.*;
import android.util.*;
import android.os.*;

public class BugReport implements Runnable {
    public static final String TAG = "ottohub/client/bug_report";
    private static final Handler uiThread = new Handler(Looper.getMainLooper());
    private Context mContext;
    
    public BugReport(Activity act) {
        mContext = act;
    }

    @Override
    public void run() {
        // TODO: Implement this method
        final EditText text = new EditText(mContext);
        text.setHint("在此处输入反馈内容...");
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("发送反馈");
        builder.setCancelable(false);
        builder.setView(text);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    send(text.getText().toString());
                    dia.dismiss();
                }
            });
        builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
    }
    
    private void send(String text) {
        AccountManager manager = AccountManager.getInstance(mContext);
        if (!manager.isLogin()) {
            mContext.startActivity(new Intent(mContext, LoginActivity.class));
            return;
        }
        Account current = manager.getAccount();
        if (current == null) {
            return;
        }
        NetworkUtils.getNetwork.getNetworkJson(APIManager.MessageURI.getSendMessageURI(current.getToken(), 5788, text),
            new NetworkUtils.HTTPCallback() {
                @Override
                public void onSuccess(String content) {
                    if (content == null || content.isEmpty()) {
                        onFailed("content is empty");
                        return;
                    }
                    try {
                        final JSONObject detail = new JSONObject(content);
                        String status = detail.optString("status", "error");
                        if (status.equals("success")) {
                            uiThread.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mContext, "反馈成功，请注意收件箱是否收到回信", Toast.LENGTH_SHORT)
                                            .show();
                                    }
                                });
                            return;
                        }
                        onFailed(detail.optString("message", "error"));
                    } catch (JSONException e) {
                        onFailed(e.toString());
                    }
                }

                @Override
                public void onFailed(String cause) {
                    Log.e("Network", cause);
                }
            });
	}
}
