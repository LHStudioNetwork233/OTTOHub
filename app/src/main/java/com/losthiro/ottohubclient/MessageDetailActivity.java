package com.losthiro.ottohubclient;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import com.losthiro.ottohubclient.adapter.Account;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import com.losthiro.ottohubclient.utils.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.ImageView;
import android.widget.TextView;
import com.losthiro.ottohubclient.view.ClientWebView;

/**
 * @Author Hiro
 * @Date 2025/06/16 13:21
 */
public class MessageDetailActivity extends MainActivity {
    public static final String TAG = "MessageDetailActivity";
    private long uid;
    private Dialog msgDia;
    private EditText UIDEdit;
    private EditText messageEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        Intent i=getIntent();
        uid=i.getLongExtra("sender", 0);
        String time=i.getStringExtra("time");
        String content=i.getStringExtra("content");
        NetworkUtils.getNetwork.getNetworkJson(APIManager.UserURI.getUserDetail(uid), new NetworkUtils.HTTPCallback(){
                @Override
                public void onSuccess(String content) {
                    if (content == null || content.isEmpty()) {
                        onFailed("empty content");
                        return;
                    }
                    try {
                        final JSONObject json=new JSONObject(content);
                        if (json == null) {
                            onFailed("null json");
                            return;
                        }
                        String status=json.optString("status", "error");
                        if (status.equals("success")) {
                            runOnUiThread(new Runnable(){
                                    @Override
                                    public void run() {
                                        ImageDownloader.loader((ImageView)findViewById(R.id.main_user_avatar), json.optString("avatar_url"));
                                        ((TextView)findViewById(R.id.main_user_name)).setText(json.optString("username", "棍母"));
                                    }
                                });
                            return;
                        }
                        onFailed(content);
                    } catch (JSONException e) {
                        onFailed(e.toString());
                    }
                }

                @Override
                public void onFailed(String cause) {
                    Log.e("Network", cause);
                }
            });
        ((TextView)findViewById(R.id.message_time)).setText(time);
        ((ClientWebView)findViewById(R.id.message_content)).loadTextData(content.substring(content.indexOf(":") + 1));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent last=Client.getLastActivity();
        if (last != null && Client.isFinishingLast(last)) {
            Client.removeActivity();
            startActivity(last);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void sendDia(final long uid, String user, final String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定发送回信吗？");
        builder.setMessage(StringUtils.strCat(new Object[]{"将发送给", user, " UID:", uid}));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    requestSend(uid, msg);
                }
            });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    private void requestSend(long uid, String msg) {
        Account current=AccountManager.getInstance(this).getAccount();
        if (current == null) {
            return;
        }
        NetworkUtils.getNetwork.getNetworkJson(APIManager.MessageURI.getSendMessageURI(current.getToken(), uid, msg), new NetworkUtils.HTTPCallback(){
                @Override
                public void onSuccess(String content) {
                    if (content == null || content.isEmpty()) {
                        onFailed("content is empty");
                        return;
                    }
                    try {
                        final JSONObject detail=new JSONObject(content);
                        String status=detail.optString("status", "error");
                        if (status.equals("success")) {
                            runOnUiThread(new Runnable(){
                                    @Override
                                    public void run() {
                                        if (msgDia != null) {
                                            msgDia.dismiss();
                                        }
                                        Toast.makeText(getApplication(), "发送成功(已发送的消息可以在管理页面操作)", Toast.LENGTH_SHORT).show();
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

    public void sendMsg(View v) {
        AccountManager manager=AccountManager.getInstance(this);
        if (!manager.isLogin()) {
            Client.saveActivity(getIntent());
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_message_edit, null);
        if (msgDia == null) {
            msgDia = new Dialog(this);
            msgDia.requestWindowFeature(1);
            msgDia.setContentView(inflate);
            UIDEdit = inflate.findViewWithTag("uid_input_edittext");
            UIDEdit.setText(StringUtils.toStr(uid));
            messageEdit = inflate.findViewWithTag("msg_input_edittext");
            inflate.findViewWithTag("msg_save").setVisibility(View.GONE);
        }
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(inflate, "translationY", 100.0f, 0.0f);
        ofFloat.setDuration(1000L);
        ofFloat.start();
        Window window = msgDia.getWindow();
        window.setFlags(4, 4);
        if (window != null) {
            window.setBackgroundDrawableResource(0x0106000d);
        }
        window.setGravity(80);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.y = 20;
        attributes.dimAmount = 0.0f;
        if (Build.VERSION.SDK_INT == 31) {
            attributes.setBlurBehindRadius(20);
        }
        window.setAttributes(attributes);
        msgDia.show();
    }

    public void sendDia(View v) {
        try {
            final long uid=Long.parseLong(UIDEdit.getText().toString());
            final String msg=messageEdit.getText().toString().replace("\n", "\\n");
            NetworkUtils.getNetwork.getNetworkJson(APIManager.UserURI.getUserDetail(uid), new NetworkUtils.HTTPCallback(){
                    @Override
                    public void onSuccess(String content) {
                        if (content == null || content.isEmpty()) {
                            onFailed("content is empty");
                            return;
                        }
                        try {
                            final JSONObject detail=new JSONObject(content);
                            String status=detail.optString("status", "error");
                            if (status.equals("success")) {
                                runOnUiThread(new Runnable(){
                                        @Override
                                        public void run() {
                                            sendDia(uid, detail.optString("username", "棍母"), msg);
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
        } catch (NumberFormatException e) {
            Toast.makeText(getApplication(), "请输入正确的UID(一串数字)", Toast.LENGTH_SHORT).show();
        }
    }

    public void quit(View v) {
        finish();
    }
}
