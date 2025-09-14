package com.losthiro.ottohubclient.adapter;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.losthiro.ottohubclient.AccountDetailActivity;
import com.losthiro.ottohubclient.BlogDetailActivity;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.impl.ClientString;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import android.app.Activity;
import com.losthiro.ottohubclient.Client;
import android.widget.*;
import android.app.*;
import android.content.*;
import com.losthiro.ottohubclient.utils.*;
import com.losthiro.ottohubclient.impl.*;
import android.util.*;
import org.json.*;
import android.os.*;
import com.losthiro.ottohubclient.adapter.model.*;
import java.util.*;

/**
 * @Author Hiro
 * @Date 2025/05/28 16:06
 */
public class BlogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	public static final String TAG = "BlogAdapter";
	private static final int STATUS_DEF = 0;
	private static final int STATUS_LOADING = 1;
	private Context main;
	private List<Blog> data;
	private boolean isLoading = false;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View root;
		public ImageView avatar;
		public TextView title;
		public TextView info;
		public TextView content;
		public boolean isShowContent = true;

		public ViewHolder(View v) {
			super(v);
			root = v;
			avatar = v.findViewById(R.id.user_avatar);
			title = v.findViewById(R.id.blog_title);
			info = v.findViewById(R.id.blog_info);
			content = v.findViewById(R.id.blog_content);
		}
	}

	public BlogAdapter(Context c, List<Blog> list) {
		main = c;
		data = new ArrayList<Blog>(new LinkedHashSet<Blog>(list));
	}

	@Override
	public int getItemCount() {
		return data.size() + (isLoading ? 1 : 0);
	}

	@Override
	public int getItemViewType(int position) {
		// TODO: Implement this method
		return (position == data.size() && isLoading) ? STATUS_LOADING : STATUS_DEF;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder vH, int p) {
		if (vH instanceof ViewHolder) {
			final ViewHolder holder = (ViewHolder) vH;
			final Blog current = data.get(p);
			ImageDownloader.loader(holder.avatar, current.getAvatarURI());
			holder.avatar.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    ClickSounds.playSound(v.getContext());
					Intent i = new Intent(main, AccountDetailActivity.class);
					i.putExtra("uid", current.getUID());
					main.startActivity(i);
				}
			});
			holder.title.setText(current.getTitle());
			holder.info.setText(StringUtils.strCat(new Object[]{current.getLikeCount(), "获赞 - ",
					current.getFavoriteCount(), "收藏 - ", current.getViewCount(), "浏览 - ", current.getTime()}));
			final ClientString string = new ClientString(current.getContent());
			string.load(holder.content, holder.isShowContent);
			holder.content.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					holder.isShowContent = !holder.isShowContent;
					string.load(holder.content, holder.isShowContent);
				}
			});
			holder.root.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    ClickSounds.playSound(v.getContext());
					Intent i = new Intent(main, BlogDetailActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.putExtra("bid", current.getBID());
					main.startActivity(i);
				}
			});
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int p) {
		if (p == STATUS_LOADING) {
			return new LoadingViewHolder(viewGroup);
		}
		return new ViewHolder(LayoutInflater.from(main).inflate(R.layout.list_blog, viewGroup, false));
	}

	public boolean isExists(Blog newBlog) {
		for (Blog existingBlog : data) {
			if (existingBlog.equals(newBlog)) {
				return true;
			}
		}
		return false;
	}

	public void addNewData(List<Blog> newData) {
		if (data.containsAll(newData)) {
			return;
		}
		data.addAll(newData);
	}

	public void setData(List<Blog> newData) {
		if (data.containsAll(newData)) {
			return;
		}
		data.clear();
		data.addAll(newData);
		notifyDataSetChanged();
	}

	public void startLoading() {
		if (isLoading) {
			return;
		}
		isLoading = true;
		notifyItemInserted(data.size());
	}

	public void stopLoading() {
		isLoading = false;
		notifyItemRemoved(data.size());
	}
}

