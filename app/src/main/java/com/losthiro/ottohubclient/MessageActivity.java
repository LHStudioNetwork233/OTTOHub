package com.losthiro.ottohubclient;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.losthiro.ottohubclient.adapter.model.Account;
import com.losthiro.ottohubclient.adapter.model.Message;
import com.losthiro.ottohubclient.adapter.MessageAdapter;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;
import com.losthiro.ottohubclient.utils.StringUtils;
import com.losthiro.ottohubclient.impl.UploadManager;
import android.text.TextWatcher;
import android.text.Editable;
import com.losthiro.ottohubclient.view.dialog.*;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.utils.*;

/**
 * @Author Hiro
 * @Date 2025/06/12 17:07
 */
public class MessageActivity extends BasicActivity {
    public static final String TAG = "MessageActivity";
    private static final Semaphore request=new Semaphore(1);
    private RecyclerView msgList;
    private SwipeRefreshLayout msgRefresh;
    private MessageAdapter adapter;
    private EditText UIDEdit;
    private EditText messageEdit;
    private Button msgRead;
    private BottomDialog msgDia;
    private int currentCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        GridLayoutManager manager=new GridLayoutManager(this, 1);
        manager.setInitialPrefetchItemCount(6);
        manager.setItemPrefetchEnabled(true);
        msgList = findViewById(R.id.message_list);
        msgList.setLayoutManager(manager);
        msgList.setItemViewCacheSize(20);
        msgList.setDrawingCacheEnabled(true);
        msgList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        msgList.addOnScrollListener(new RecyclerView.OnScrollListener(){
                @Override
                public void onScrollStateChanged(RecyclerView view, int state) {
                    super.onScrollStateChanged(view, state);
                    if (state == RecyclerView.SCROLL_STATE_IDLE) {
                        int itemCount = view.getLayoutManager().getItemCount();
                        int lastPos = ((LinearLayoutManager)view.getLayoutManager()).findLastVisibleItemPosition();
                        if (lastPos >= itemCount - 1) {
                            request(false);
                        }
                    }
                }
            });
        msgRefresh = findViewById(R.id.refresh);
        msgRefresh.setRefreshing(true);
        msgRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
                @Override
                public void onRefresh() {
                    request(true);
                }
            });
        msgRead = findViewById(R.id.message_read_btn);
        msgRead.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    readAll();
                }
            });
        initCategoryView();
        request(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UploadManager.getInstance(this).save();
        Intent last=Client.getLastActivity();
        if (last != null && Client.isFinishingLast(last)) {
            Client.removeActivity();
            startActivity(last);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        UploadManager.getInstance(this).save();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void readAll() {
        try {
            if (!request.tryAcquire()) {
                return;
            }
            Account current = AccountManager.getInstance(this).getAccount();
            if (current == null) {
                return;
            }
            String uri=APIManager.MessageURI.getSystemReaderMessageURI(current.getToken());
            NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback(){
                    @Override
                    public void onSuccess(String content) {
                        Log.i("Network", content);
                    }

                    @Override
                    public void onFailed(String cause) {
                        Log.e("Network", cause);
                    }
                });
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } finally {
            request.release();
        }
    }

    private void request(final boolean isRefresh) {
        try {
            if (!request.tryAcquire()) {
                return;
            }
            Account current = AccountManager.getInstance(this).getAccount();
            if (current == null) {
                return;
            }
            String token = current.getToken();
            String uri=APIManager.MessageURI.getUnreadMessageURI(token, 0, 12);
            if (currentCategory == 2) {
                uri = APIManager.MessageURI.getReadMessageURI(token, 0, 12);
            }
            NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback(){
                    @Override
                    public void onSuccess(String content) {
                        if (content == null || content.isEmpty()) {
                            onFailed("empty content");
                            return;
                        }
                        try {
                            JSONObject json=new JSONObject(content);
                            if (json == null) {
                                onFailed("null json");
                                return;
                            }
                            String status=json.optString("status", "error");
                            if (status.equals("success")) {
                                int type=Message.TYPE_SYSTEM;
                                if (currentCategory == 0) {
                                    type = Message.TYPE_ANSWER;
                                }
                                if (currentCategory == 1) {
                                    type = Message.TYPE_AT;
                                }
                                JSONArray video=json.optJSONArray(currentCategory == 2 ?"read_message_list": "unread_message_list");
                                final List<Message> data=new ArrayList<>();
                                for (int i=0;i < video.length();i++) {
                                    Message current = new Message(video.optJSONObject(i));
                                    if (current.getType() == type || currentCategory == 2) {
                                        data.add(current);
                                    }
                                }
                                runOnUiThread(new Runnable(){
                                        @Override
                                        public void run() {
                                            msgRefresh.setRefreshing(false);
                                            if (adapter == null) {
                                                adapter = new MessageAdapter(MessageActivity.this, data);
                                                msgList.setAdapter(adapter);
                                                return;
                                            }
                                            if (isRefresh) {
                                                adapter.update(data);
                                            } else {
                                                adapter.addNewData(data);
                                            }
                                        }
                                    });
                            }
                            onFailed(content);
                        } catch (JSONException e) {
                            onFailed(e.toString());
                        }
                    }

                    @Override
                    public void onFailed(String cause) {
                        Log.e("Network", cause);
                        msgRefresh.setRefreshing(false);
                    }
                });
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } finally {
            request.release();
        }
    }

    private void initCategoryView() {
        View parent=findViewById(android.R.id.content);
        final TextView[] categorys=new TextView[4];
        categorys[0] = parent.findViewWithTag("answer");
        categorys[1] = parent.findViewWithTag("at");
        categorys[2] = parent.findViewWithTag("history");
        categorys[3] = parent.findViewWithTag("system");
        for (int i = 0; i < 4; i++) {
            final int index=i;
            categorys[i].setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        if (index == currentCategory) {
                            return;
                        }
                        for (int i = 0; i < 4; i++) {
                            categorys[i].setTextColor(i == index ?ResourceUtils.getColor(R.color.colorAccent) : ResourceUtils.getColor(R.color.colorSecondary));
                        }
                        msgRead.setVisibility(index == 2 ?View.GONE: View.VISIBLE);
                        currentCategory = index;
                        request(true);
                    }
                });
        }
    }

    private void sendDia(final long uid, String user, final String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定发送私信吗？");
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

    private void sendDia(final long uid, final String msg) {
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
            msgDia = new BottomDialog(this, inflate);
            UIDEdit = inflate.findViewWithTag("uid_input_edittext");
            messageEdit = inflate.findViewWithTag("msg_input_edittext");
            UIDEdit.addTextChangedListener(new TextWatcher(){
                    @Override
                    public void afterTextChanged(Editable s) {
                        try{
                            long uid=Long.parseLong(s.toString());
                            messageEdit.setText(UploadManager.getInstance(getApplication()).getMsg(uid));
                        }catch(NumberFormatException e){}
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                });
        }
        msgDia.show();
    }

    public void saveMsg(View v) {
        try {
            long uid=Long.parseLong(UIDEdit.getText().toString());
            String msg=messageEdit.getText().toString().replace("\n", "\\n");
            UploadManager.getInstance(this).putMsg(uid, msg);
            Toast.makeText(getApplication(), "走位成功", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(getApplication(), "请输入正确的UID(一串数字)", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendDia(View v) {
        try {
            long uid=Long.parseLong(UIDEdit.getText().toString());
            String msg=messageEdit.getText().toString().replace("\n", "\\n");
            sendDia(uid, msg);
        } catch (NumberFormatException e) {
            Toast.makeText(getApplication(), "请输入正确的UID(一串数字)", Toast.LENGTH_SHORT).show();
        }
    }

    public void quit(View v) {
        finish();
    }
}
