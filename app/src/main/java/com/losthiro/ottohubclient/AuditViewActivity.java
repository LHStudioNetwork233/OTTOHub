/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.*;
import com.losthiro.ottohubclient.adapter.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.utils.*;
import com.losthiro.ottohubclient.view.*;
import java.util.*;
import android.util.*;
import org.json.*;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.crashlogger.*;

public class AuditViewActivity extends BasicActivity {
	private long currentID;
	private int IDtype;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audit_view);
		Intent i = getIntent();
		IDtype = i.getIntExtra("type", AuditModel.TYPE_AVATAR);
		switch (IDtype) {
			case AuditModel.TYPE_BLOG :
				currentID = i.getLongExtra("bid", 0);
				findViewById(R.id.audit_blog_view).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.blog_title)).setText(i.getStringExtra("title"));
				((TextView) findViewById(R.id.blog_info)).setText("BID:" + currentID);
				String content = i.getStringExtra("content");
				final TextView textContent = findViewById(R.id.blog_content_text);
				textContent.setText(content);
				final ClientWebView webContent = findViewById(R.id.blog_content_view);
				webContent.setTextData(content);
                webContent.setFragmentManager(getSupportFragmentManager());
                webContent.load();
				Switch mode = findViewById(R.id.audit_blog_mode);
				mode.setVisibility(View.VISIBLE);
				mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (isChecked) {
							webContent.setVisibility(View.GONE);
							textContent.setVisibility(View.VISIBLE);
						} else {
							webContent.setVisibility(View.VISIBLE);
							textContent.setVisibility(View.GONE);
						}
					}
				});
				break;
			case AuditModel.TYPE_VIDEO :
				currentID = i.getLongExtra("vid", 0);
				findViewById(R.id.audit_video_view).setVisibility(View.VISIBLE);
				VideoView main = findViewById(R.id.main_video_view);
				MediaController control = new MediaController(this);
				control.setAnchorView(main);
				main.setMediaController(control);
				main.setVideoURI(Uri.parse(i.getStringExtra("video_uri")));
				main.start();
				String info = StringUtils
						.strCat(new Object[]{"上传者UID:", i.getLongExtra("uid", 0), " 视频vid:", currentID});
				((TextView) findViewById(R.id.video_detail_title)).setText(i.getStringExtra("title"));
				((TextView) findViewById(R.id.video_detail_info)).setText(info);
				((TextView) findViewById(R.id.video_detail_intro)).setText(i.getStringExtra("intro"));
				List<String> tags = Arrays.asList(i.getStringArrayExtra("tag"));
				if (tags.size() > 0) {
					RecyclerView tagsView = findViewById(R.id.video_detail_tags);
					tagsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
					tagsView.setAdapter(new HonourAdapter(this, tags));
				}
				ImageDownloader.loader((ImageView) findViewById(R.id.audit_video_cover), i.getStringExtra("cover_uri"));
				break;
			default :
				currentID = i.getLongExtra("uid", 0);
				findViewById(R.id.audit_picture_view).setVisibility(View.VISIBLE);
				String title = IDtype == AuditModel.TYPE_AVATAR ? "上传的头像" : "上传的封面";
				((TextView) findViewById(R.id.audit_picture_title))
						.setText(StringUtils.strCat(new Object[]{i.getStringExtra("name"), " UID:", currentID, title}));
				ImageDownloader.loader((ImageView) findViewById(R.id.audit_picture_image),
						i.getStringExtra(IDtype == AuditModel.TYPE_AVATAR ? "avatar_uri" : "cover_uri"));
				break;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO: Implement this method
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// TODO: Implement this method
		super.onBackPressed();
		finish();
	}

	public void rejectCurrent(View v) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("确定打回？");
		dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				rejectCurrent();
                dia.dismiss();
			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.create().show();
	}

	private void rejectCurrent() {
		String token = AccountManager.getInstance(this).getAccount().getToken();
		String uri = null;
		switch (IDtype) {
			case AuditModel.TYPE_VIDEO :
				uri = APIManager.ModerationURI.getRejectVideoURI(currentID, token);
				break;
			case AuditModel.TYPE_BLOG :
				uri = APIManager.ModerationURI.getRejectBlogURI(currentID, token);
				break;
			case AuditModel.TYPE_AVATAR :
				uri = APIManager.ProfileURI.getRejectAvatarURI(token, currentID);
				break;
			case AuditModel.TYPE_COVER :
				uri = APIManager.ProfileURI.getRejectCoverURI(token, currentID);
				break;
		}
		if (uri != null) {
			NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
				@Override
				public void onSuccess(String content) {
					// TODO: Implement this method
                    if(content==null){
                        onFailed("content = null");
                        return;
                    }
                    try{
                        JSONObject json=new JSONObject(content);
                        if(json.optString("status", "error").equals("success")){
                            runOnUiThread(new Runnable(){
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplication(), "操作成功", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            return;
                        }
                        onFailed(json.optString("message"));
                    }catch(Exception e){
                        onFailed(e.toString());
                    }
				}

				@Override
				public void onFailed(String cause) {
					// TODO: Implement this method
                    Log.e("Network", cause);
                    NetworkException.getInstance(getApplication()).handlerError(cause);
				}
			});
		}
	}

	public void approveCurrent(View v) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("确定通过？");
		dialog.setMessage("请仔细检查内容是否违反相关规定");
		dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
                approveCurrent();
                dia.dismiss();
			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.create().show();
	}
    
    private void approveCurrent() {
        String token = AccountManager.getInstance(this).getAccount().getToken();
        String uri = null;
        switch (IDtype) {
            case AuditModel.TYPE_VIDEO :
                uri = APIManager.ModerationURI.getApproveVideoURI(currentID, token);
                break;
            case AuditModel.TYPE_BLOG :
                uri = APIManager.ModerationURI.getApproveBlogURI(currentID, token);
                break;
            case AuditModel.TYPE_AVATAR :
                uri = APIManager.ProfileURI.getApproveAvatarURI(token, currentID);
                break;
            case AuditModel.TYPE_COVER :
                uri = APIManager.ProfileURI.getApproveCoverURI(token, currentID);
                break;
        }
        if (uri != null) {
            NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
                    @Override
                    public void onSuccess(String content) {
                        // TODO: Implement this method
                        if(content==null){
                            onFailed("content = null");
                            return;
                        }
                        try{
                            JSONObject json=new JSONObject(content);
                            if(json.optString("status", "error").equals("success")){
                                runOnUiThread(new Runnable(){
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplication(), "操作成功", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                return;
                            }
                            onFailed(json.optString("message"));
                        }catch(Exception e){
                            onFailed(e.toString());
                        }
                    }

                    @Override
                    public void onFailed(String cause) {
                        // TODO: Implement this method
                        Log.e("Network", cause);
                        NetworkException.getInstance(getApplication()).handlerError(cause);
                    }
                });
        }
	}

	public void quit(View v) {
		finish();
	}
}

