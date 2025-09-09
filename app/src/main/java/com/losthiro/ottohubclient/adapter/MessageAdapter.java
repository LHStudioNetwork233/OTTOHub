package com.losthiro.ottohubclient.adapter;

/**
 * @Author Hiro
 * @Date 2025/06/14 15:31
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.losthiro.ottohubclient.AccountDetailActivity;
import com.losthiro.ottohubclient.Client;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.adapter.MessageAdapter;
import com.losthiro.ottohubclient.impl.APIManager;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.NetworkUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Handler;
import android.os.Looper;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.BlogDetailActivity;
import com.losthiro.ottohubclient.impl.ClientString;
import com.losthiro.ottohubclient.PlayerActivity;
import org.json.JSONArray;
import com.losthiro.ottohubclient.MainActivity;
import com.losthiro.ottohubclient.MessageDetailActivity;
import com.losthiro.ottohubclient.adapter.model.*;
import android.widget.*;
import com.losthiro.ottohubclient.impl.*;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
	public static final String TAG = "MessageAdapter";
	private Context main;
	private List<Message> data;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View root;
		public ImageView avatar;
		public TextView title;
		public TextView info;
		public TextView content;

		public ViewHolder(View v) {
			super(v);
			root = v;
			avatar = v.findViewById(R.id.user_avatar);
			title = v.findViewById(R.id.blog_title);
			info = v.findViewById(R.id.blog_info);
			content = v.findViewById(R.id.blog_content);
		}
	}

	public MessageAdapter(Context c, List<Message> list) {
		main = c;
		data = new ArrayList<Message>(new HashSet<Message>(list));
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	@Override
	public void onBindViewHolder(final MessageAdapter.ViewHolder vH, final int p) {
		final Message current = data.get(p);
		if (current.getType() == Message.TYPE_SYSTEM) {
			ViewGroup.LayoutParams params = vH.avatar.getLayoutParams();
			params.width = 0;
			vH.avatar.setLayoutParams(params);
		} else {
			final Handler h = new Handler(Looper.getMainLooper());
			NetworkUtils.getNetwork.getNetworkJson(APIManager.UserURI.getUserDetail(current.getSendUID()),
					new NetworkUtils.HTTPCallback() {
						@Override
						public void onSuccess(String content) {
							if (content == null || content.isEmpty()) {
								onFailed("empty content");
								return;
							}
							try {
								final JSONObject json = new JSONObject(content);
								if (json == null) {
									onFailed("null json");
									return;
								}
								String status = json.optString("status", "error");
								if (status.equals("success")) {
									h.post(new Runnable() {
										@Override
										public void run() {
											ImageDownloader.loader(vH.avatar, json.optString("avatar_url"));
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
			vH.avatar.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    ClickSounds.playSound(v.getContext());
					Intent i = new Intent(main, AccountDetailActivity.class);
					i.putExtra("uid", current.getSendUID());
					Intent save = ((Activity) main).getIntent();
					Client.saveActivity(save);
					main.startActivity(i);
				}
			});
		}
		vH.title.setText(current.getSender());
		vH.info.setText(current.getTime());
		vH.content.setText(current.getContent());
		vH.root.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                ClickSounds.playSound(v.getContext());
				readMsg(current);
				data.remove(current);
				notifyDataSetChanged();
			}
		});
	}

	@Override
	public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int p) {
		return new ViewHolder(LayoutInflater.from(main).inflate(R.layout.list_blog, viewGroup, false));
	}

	private void readMsg(final Message current) {
		final Account a = AccountManager.getInstance(main).getAccount();
		if (a == null) {
			return;
		}
		String uri = APIManager.MessageURI.getReadMessageURI(a.getToken(), current.getMID());
		NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(String content) {
				if (content == null || content.isEmpty()) {
					onFailed("empty content");
					return;
				}
				try {
					final JSONObject json = new JSONObject(content);
					if (json == null) {
						onFailed("null json");
						return;
					}
					String status = json.optString("status", "error");
					if (status.equals("success")) {
						Log.i(TAG, content);
						Activity mainAct = MainActivity.activity.get();
						if (mainAct != null && mainAct instanceof MainActivity) {
							((MainActivity) mainAct).messageCallback(a.getToken());
						}
						ClientString str = new ClientString(current.getContent());
						if (current.getContent().contains("动态")) {
							Intent i = new Intent(main, BlogDetailActivity.class);
							i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							i.putExtra("bid", str.findID("BID+:"));
							Intent save = ((Activity) main).getIntent();
							Client.saveActivity(save);
							main.startActivity(i);
						} else if (current.getContent().contains("视频")) {
							Intent i = new Intent(main, PlayerActivity.class);
							i.putExtra("vid", str.findID("VID+:"));
							Intent save = ((Activity) main).getIntent();
							Client.saveActivity(save);
							main.startActivity(i);
						} else if (current.isEmail()) {
							Intent i = new Intent(main, MessageDetailActivity.class);
							i.putExtra("sender", current.getSendUID());
							i.putExtra("content", current.getContent());
							i.putExtra("time", current.getTime());
							Intent save = ((Activity) main).getIntent();
							Client.saveActivity(save);
							main.startActivity(i);
						}
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
	}

	public boolean isExists(Message newMsg) {
		for (Message existingBlog : data) {
			if (existingBlog.equals(newMsg)) {
				return true;
			}
		}
		return false;
	}

	public void update(List<Message> newData) {
		data.clear();
		data.addAll(newData);
		notifyDataSetChanged();
	}

	public void addNewData(List<Message> newData) {
		if (data.containsAll(newData)) {
			return;
		}
		data.addAll(newData);
		notifyItemRangeInserted(data.size() - newData.size(), newData.size());
	}

	public void clear() {
		data.clear();
        notifyDataSetChanged();
	}
}

