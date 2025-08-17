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

/**
 * @Author Hiro
 * @Date 2025/06/13 18:00
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {
	public static final String TAG = "AccountAdapter";
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
		final Account current = manager.getAccount(p);
		ImageDownloader.loader(vH.avatar, current.getAvatarURI());
		vH.username.setText(current.getName());
		vH.info.setText(StringUtils.strCat("UID: ", StringUtils.toStr(current.getUID())));
		vH.current.setColorFilter(ResourceUtils.getColor(R.color.colorSecondary));
		vH.root.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = 0; i < getItemCount(); i++) {
					manager.getAccount(i).setCurrent(i == p);
					vH.current.setVisibility(i == p ? View.VISIBLE : View.GONE);
				}
				if (onChange != null) {
					onChange.onCurrentChange(current);
				}
				if (update != null) {
					update.update(current);
				}
				notifyDataSetChanged();
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

	public void setOnAccountChangeListener(AccountManager.AccountListener listener) {
		onChange = listener;
	}
}

