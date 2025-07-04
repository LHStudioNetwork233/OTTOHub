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

/**
 * @Author Hiro
 * @Date 2025/05/28 16:06
 */
public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.ViewHolder> {
	public static final String TAG = "BlogAdapter";
	private Context main;
	private List<Blog> data;

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
        data = new ArrayList<Blog>(new HashSet<Blog>(list));
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	@Override
	public void onBindViewHolder(final BlogAdapter.ViewHolder vH, int p) {
		final Blog current = data.get(p);
		ImageDownloader.loader(vH.avatar, current.getAvatarURI());
		vH.avatar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(main, AccountDetailActivity.class);
				i.putExtra("uid", current.getUID());
				Intent save = ((Activity) main).getIntent();
				Client.saveActivity(save);
				main.startActivity(i);
			}
		});
		vH.title.setText(current.getTitle());
		vH.info.setText(StringUtils.strCat(new Object[]{current.getLikeCount(), "获赞 - ", current.getFavoriteCount(),
				"收藏 - ", current.getViewCount(), "浏览 - ", current.getTime()}));
		final ClientString string = new ClientString(current.getContent());
		string.load(vH.content, vH.isShowContent);
		vH.content.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				vH.isShowContent = !vH.isShowContent;
				string.load(vH.content, vH.isShowContent);
			}
		});
		vH.root.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(main, BlogDetailActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("bid", current.getBID());
				Intent save = ((Activity) main).getIntent();
				Client.saveActivity(save);
				main.startActivity(i);
			}
		});
	}

	@Override
	public BlogAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int p) {
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
		notifyItemRangeInserted(data.size() - newData.size(), newData.size());
	}
}

