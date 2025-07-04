/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter;
import androidx.recyclerview.widget.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import java.util.*;
import com.losthiro.ottohubclient.*;
import android.view.View.*;
import android.app.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.utils.*;

public class AuditAdapter extends RecyclerView.Adapter<AuditAdapter.ViewHolder> {
	private Context main;
	private List<AuditModel> data;

	public AuditAdapter(Context ctx, List<AuditModel> list) {
		main = ctx;
		data = new ArrayList<AuditModel>(new HashSet<AuditModel>(list));
	}

	@Override
	public int getItemViewType(int position) {
		// TODO: Implement this method
		return data.get(position).getType();
	}

	@Override
	public int getItemCount() {
		// TODO: Implement this method
		return data.size();
	}

	@Override
	public void onBindViewHolder(final AuditAdapter.ViewHolder vH, int p) {
		// TODO: Implement this method
		if (vH == null) {
			return;
		}
		final AuditModel current = data.get(p);
		switch (current.getType()) {
			case AuditModel.TYPE_VIDEO :
				ImageDownloader.loader(vH.cover, current.getCover());
				vH.title.setText(current.getTitle());
				vH.user.setText(StringUtils.strCat(new Object[]{"UID:", current.getUID(), " VID:", current.getVID()}));
				vH.root.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO: Implement this method
						Intent i = new Intent(main, AuditViewActivity.class);
						i.putExtra("type", AuditModel.TYPE_VIDEO);
						i.putExtra("uid", current.getUID());
						i.putExtra("vid", current.getVID());
						i.putExtra("title", current.getTitle());
						i.putExtra("intro", current.getIntro());
						i.putExtra("tag", current.getTags());
						i.putExtra("cover_uri", current.getCover());
						i.putExtra("video_uri", current.getVideo());
						Client.saveActivity(((Activity) main).getIntent());
						main.startActivity(i);
					}
				});
				break;
			case AuditModel.TYPE_BLOG :
				vH.title.setText(current.getTitle());
				vH.user.setText("BID:" + current.getBID());
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
						// TODO: Implement this method
						Intent i = new Intent(main, AuditViewActivity.class);
						i.putExtra("type", AuditModel.TYPE_BLOG);
						i.putExtra("bid", current.getVID());
						i.putExtra("title", current.getTitle());
						i.putExtra("content", current.getIntro());
						Client.saveActivity(((Activity) main).getIntent());
						main.startActivity(i);
					}
				});
				break;
			case AuditModel.TYPE_AVATAR :
				ImageDownloader.loader(vH.cover, current.getAvatar());
				vH.title.setText(
						StringUtils.strCat(new Object[]{current.getName(), " UID: ", current.getUID(), "上传的头像"}));
				vH.root.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO: Implement this method
						Intent i = new Intent(main, AuditViewActivity.class);
						i.putExtra("type", AuditModel.TYPE_AVATAR);
						i.putExtra("uid", current.getUID());
						i.putExtra("name", current.getName());
						i.putExtra("avatar_uri", current.getAvatar());
						Client.saveActivity(((Activity) main).getIntent());
						main.startActivity(i);
					}
				});
                break;
            case AuditModel.TYPE_COVER :
                ImageDownloader.loader(vH.cover, current.getCover());
                vH.title.setText(
                    StringUtils.strCat(new Object[]{current.getName(), " UID: ", current.getUID(), "上传的封面"}));
                vH.root.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // TODO: Implement this method
                            Intent i = new Intent(main, AuditViewActivity.class);
                            i.putExtra("type", AuditModel.TYPE_COVER);
                            i.putExtra("uid", current.getUID());
                            i.putExtra("name", current.getName());
                            i.putExtra("cover_uri", current.getAvatar());
                            Client.saveActivity(((Activity) main).getIntent());
                            main.startActivity(i);
                        }
                    });
                break;
		}

	}

	@Override
	public AuditAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int p) {
		// TODO: Implement this method
		LayoutInflater inflater = LayoutInflater.from(main);
		AuditModel current = data.get(p);
		switch (current.getType()) {
			case AuditModel.TYPE_VIDEO :
				return new ViewHolder(inflater.inflate(R.layout.list_video_detail, viewGroup, false),
						AuditModel.TYPE_VIDEO);
			case AuditModel.TYPE_BLOG :
				return new ViewHolder(inflater.inflate(R.layout.list_blog, viewGroup, false), AuditModel.TYPE_BLOG);
			case AuditModel.TYPE_AVATAR :
				return new ViewHolder(inflater.inflate(R.layout.list_video_detail, viewGroup, false),
						AuditModel.TYPE_AVATAR);
			case AuditModel.TYPE_COVER :
				return new ViewHolder(inflater.inflate(R.layout.list_video_detail, viewGroup, false),
						AuditModel.TYPE_COVER);
		}
		return null;
	}
    
    public void addNewData(List<AuditModel> newData){
        if (data.containsAll(newData)) {
            return;
        }
        data.addAll(newData);
        notifyItemRangeInserted(data.size() - newData.size(), newData.size());
    }

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View root;
		public ImageView cover;
		public TextView title;
		public TextView user;
		public TextView content;
		public boolean isShowContent;

		public ViewHolder(View v, int type) {
			super(v);
			root = v;
			switch (type) {
				case AuditModel.TYPE_VIDEO :
					cover = v.findViewById(R.id.video_cover);
					title = v.findViewById(R.id.video_title);
					user = v.findViewById(R.id.video_user);
					v.findViewById(R.id.video_detail_time).setVisibility(View.GONE);
					v.findViewById(R.id.video_detail_info).setVisibility(View.GONE);
					break;
				case AuditModel.TYPE_BLOG :
					title = v.findViewById(R.id.blog_title);
					user = v.findViewById(R.id.blog_info);
					content = v.findViewById(R.id.blog_content);
					v.findViewById(R.id.user_avatar).setVisibility(View.GONE);
					break;
				case AuditModel.TYPE_AVATAR :
					cover = v.findViewById(R.id.video_cover);
					title = v.findViewById(R.id.video_title);
					v.findViewById(R.id.video_user).setVisibility(View.GONE);
					v.findViewById(R.id.video_detail_time).setVisibility(View.GONE);
					v.findViewById(R.id.video_detail_info).setVisibility(View.GONE);
					break;
				case AuditModel.TYPE_COVER :
					cover = v.findViewById(R.id.video_cover);
					title = v.findViewById(R.id.video_title);
					v.findViewById(R.id.video_user).setVisibility(View.GONE);
					v.findViewById(R.id.video_detail_time).setVisibility(View.GONE);
					v.findViewById(R.id.video_detail_info).setVisibility(View.GONE);
					break;
			}
		}
	}
}

