package com.losthiro.ottohubclient.adapter;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.losthiro.ottohubclient.R;
import android.view.ViewGroup;
import android.content.Context;
import java.util.List;
import com.losthiro.ottohubclient.impl.AccountManager;
import android.view.LayoutInflater;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.StringUtils;
import android.view.View.OnClickListener;
import com.losthiro.ottohubclient.adapter.model.*;
import com.losthiro.ottohubclient.utils.*;
import com.losthiro.ottohubclient.view.drawer.*;
import com.losthiro.ottohubclient.impl.*;
import android.util.*;
import org.json.*;
import android.os.Handler;
import android.os.Looper;
import android.widget.*;
import android.view.View.*;
import android.app.*;
import android.graphics.drawable.*;
import android.content.*;

/**
 * @Author Hiro
 * @Date 2025/06/13 18:00
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {
	public static final String TAG = "AccountAdapter";
	private static final Handler uiThread = new Handler(Looper.getMainLooper());
	private Context ctx;
	private AccountManager manager;
	private AccountManager.AccountListener onChange;
	private SlideDrawerManager.UpdateDrawer update;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View root;
		public ImageView current;
		public ImageView avatar;
		public TextView username;
		public TextView info;

		public ViewHolder(View v) {
			super(v);
			root = v;
			current = v.findViewById(R.id.list_account_current);
			avatar = v.findViewById(R.id.main_user_avatar);
			username = v.findViewById(R.id.list_user_name);
			info = v.findViewById(R.id.list_user_info);
		}
	}

	public AccountAdapter(Context c, SlideDrawerManager.UpdateDrawer action) {
		ctx = c;
		update = action;
		manager = AccountManager.getInstance(c);
	}

	@Override
	public void onBindViewHolder(final AccountAdapter.ViewHolder vH, final int p) {
		long currentUID = manager.getAccountID(p);
		NetworkUtils.getNetwork.getNetworkJson(APIManager.UserURI.getIDuserURI(currentUID),
				new NetworkUtils.HTTPCallback() {
					@Override
					public void onSuccess(String content) {
						// TODO: Implement this method
						try {
							JSONObject json = new JSONObject(content);
							if (json.optString("status", "error").equals("success")) {
								JSONArray list = json.optJSONArray("user_list");
								for (int i = 0; i < list.length(); i++) {
									final JSONObject current = list.optJSONObject(i);
									uiThread.post(new Runnable() {
										@Override
										public void run() {
											// TODO: Implement this method
											update(vH, current, p);
										}
									});
								}
								return;
							}
							onFailed(json.optString("message"));
						} catch (Exception e) {
							onFailed(e.toString());
						}
					}

					@Override
					public void onFailed(String cause) {
						// TODO: Implement this method
						Log.e("Network", cause);
					}
				});
	}

	@Override
	public AccountAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int p) {
		return new ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.list_account, viewGroup, false));
	}

	@Override
	public int getItemCount() {
		return manager.accountCount();
	}

	private void update(final ViewHolder vH, JSONObject current, final int p) {
		long id = 0;
		Object strID = current.opt("uid");
		try {
			id = Long.parseLong(strID.toString());
		} catch (NumberFormatException e) {
		}
		final long uid = id;
		ImageDownloader.loader(vH.avatar, current.optString("avatar_url"));
		vH.username.setText(current.optString("username", "棍母"));
		vH.info.setText(StringUtils.strCat("UID: ", StringUtils.toStr(p)));
		vH.current.setColorFilter(ResourceUtils.getColor(R.color.colorSecondary));
		if (manager.isLogin()) {
			vH.current.setVisibility(uid == manager.getAccount().getUID() ? View.VISIBLE : View.GONE);
		}
		vH.root.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = 0; i < getItemCount(); i++) {
					vH.current.setVisibility(i == p ? View.VISIBLE : View.GONE);
				}
				manager.setLoginCallback(new Runnable() {
					@Override
					public void run() {
						// TODO: Implement this method
						if (onChange != null) {
							onChange.onCurrentChange(manager.getAccount());
						}
						if (update != null) {
							update.update(manager.getAccount());
						}
					}
				});
				manager.login(uid);
				notifyDataSetChanged();
			}
		});
		vH.root.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// TODO: Implement this method
				dialog(vH.avatar.getDrawable(), vH.info.getText(), p);
				return false;
			}
		});
	}

	private void dialog(Drawable icon, CharSequence info, final int pos) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setIcon(icon);
		builder.setTitle("确定移除该账号？");
		builder.setMessage(info);
		builder.setCancelable(false);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				Toast.makeText(ctx, "操作成功", Toast.LENGTH_SHORT).show();
				Account current = manager.getAccount();
				if (manager.isLogin() && current.getUID() == manager.getAccountID(pos)) {
					manager.logout();
				}
				manager.removeAccount(pos);
				dia.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	public void setOnAccountChangeListener(AccountManager.AccountListener listener) {
		onChange = listener;
	}
}

