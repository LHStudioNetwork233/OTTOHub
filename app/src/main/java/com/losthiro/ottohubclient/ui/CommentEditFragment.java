/**
 * @Author Hiro
 * @Date 2025/09/10 18:30
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.ui;
import androidx.fragment.app.*;
import android.os.*;
import android.view.*;
import com.losthiro.ottohubclient.*;
import android.widget.*;
import com.losthiro.ottohubclient.impl.*;
import android.content.*;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.utils.*;
import org.json.*;
import android.util.*;
import android.view.View.*;

public class CommentEditFragment extends Fragment implements OnClickListener{
	public final static String TAG = "CommentEdit";
    private static final Handler uiThread = new Handler(Looper.getMainLooper());
    private Runnable mCallback;
    private EditText commentEdit;
    private Button sendBtn;

	public static CommentEditFragment newInstance(long id, long parent, int type) {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		arg.putLong("id", id);
		arg.putLong("parent_id", parent);
		arg.putInt("type", type);
		CommentEditFragment editPage = new CommentEditFragment();
		editPage.setArguments(arg);
		return editPage;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: Implement this method
        View root = inflater.inflate(R.layout.fragment_comment_edit, container, false);
        commentEdit = root.findViewById(R.id.comment_edit);
        sendBtn = root.findViewById(R.id.comment_send_btn);
        sendBtn.setOnClickListener(this);
		return root;
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO: Implement this method
        super.onViewCreated(view, savedInstanceState);
        Bundle arg = getArguments();
        if (arg == null) {
            return;
        }
        commentEdit.setHint(arg.getString("hint", commentEdit.getHint().toString()));
    }

    @Override
    public void onClick(View v) {
        // TODO: Implement this method
        sendComment();
    }
    
    public void setSendCallback(Runnable callback) {
        mCallback = callback;
    }
    
    public void setHint(String text) {
        Bundle arg = getArguments();
        if (arg == null) {
            return;
        }
        arg.putString("hint", text);
        if(commentEdit == null) {
            return;
        }
        commentEdit.setHint(text);
    }
    
    private void sendComment() {
        final Context ctx = getContext();
        AccountManager manager = AccountManager.getInstance(ctx);
        if (!manager.isLogin()) {
            Toast.makeText(ctx, "没登录发牛魔", Toast.LENGTH_SHORT).show();
            return;
        }
        String content = commentEdit.getText().toString();
        if (content.isEmpty()) {
            Toast.makeText(ctx, "你发的是棍母", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle arg = getArguments();
        if (arg == null) {
            return;
        }
        long id = arg.getLong("id");
        long parent = arg.getLong("parent_id");
        int type = arg.getInt("type");
        String at = arg.getString("at");
        String msg = at == null ? content : StringUtils.strCat(new String[]{"@", at, " ", content});
        String uri = type == Comment.TYPE_BLOG
            ? APIManager.CommentURI.getCommentBlogURI(id, parent, manager.getAccount().getToken(), msg)
            : APIManager.CommentURI.getCommentVideoURI(id, parent, manager.getAccount().getToken(), msg);
        NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
                @Override
                public void onSuccess(String content) {
                    if (content.isEmpty() || content == null) {
                        onFailed("empty content");
                        return;
                    }
                    try {
                        final JSONObject root = new JSONObject(content);
                        uiThread.post(new Runnable() {
                                @Override
                                public void run() {
                                    String status = root.optString("status", "error");
                                    if (status.equals("success")) {
                                        boolean canGetEXP = root.optInt("if_get_experience", 0) == 1;
                                        String msg = "评论发送成功~";
                                        if (canGetEXP) {
                                            msg = msg + "经验+3";
                                        }
                                        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
                                        commentEdit.setText("");
                                        if (mCallback != null) {
                                            mCallback.run();
                                        }
                                        return;
                                    }
                                    String message = root.optString("message", "error");
                                    if (message.equals("content_too_long")) {
                                        Toast.makeText(ctx, "不许你发小作文", Toast.LENGTH_SHORT).show();
                                    }
                                    if (message.equals("content_too_short")) {
                                        Toast.makeText(ctx, "才发两三个字是什么意思啊", Toast.LENGTH_SHORT).show();
                                    }
                                    if (message.equals("error_parent")) {
                                        Toast.makeText(ctx, "这个嘛...目前还没有楼中楼中楼功能哦", Toast.LENGTH_SHORT).show();
                                    }
                                    if (message.equals("warn")) {
                                        Toast.makeText(ctx, "冰不许爆(把你违禁词删了)", Toast.LENGTH_SHORT).show();
                                    }
                                    onFailed(message);
                                }
                            });
                    } catch (JSONException e) {
                        onFailed(e.toString());
                    }
                }

                @Override
                public void onFailed(final String cause) {
                    Log.e("Network", cause);
                }
            });
	}
}

