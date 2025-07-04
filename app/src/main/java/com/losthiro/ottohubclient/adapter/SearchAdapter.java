package com.losthiro.ottohubclient.adapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.losthiro.ottohubclient.AccountDetailActivity;
import com.losthiro.ottohubclient.BlogDetailActivity;
import com.losthiro.ottohubclient.Client;
import com.losthiro.ottohubclient.PlayerActivity;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.impl.ClientString;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * @Author Hiro
 * @Date 2025/05/28 08:44
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    public static final String TAG = "SearchAdapter";
    private LinkedList<SearchContent> data;
    private Context main;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View root;
        public ImageView cover;
        public ImageView avatar;
        public TextView intro;
        public Button follow;
        public TextView user;
        public TextView title;
        public TextView video_detail_time;
        public TextView info;
        public TextView content;
        public boolean isShowContent=true;

        public ViewHolder(View v) {
            super(v);
            root = v;
        }
    }

    public SearchAdapter(Context ctx, List<SearchContent> list) {
        main = ctx;
        data = new LinkedList<SearchContent>(new LinkedHashSet<SearchContent>(compare(list)));
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(final SearchAdapter.ViewHolder vH, int p) {
        SearchContent current = data.get(p);
        switch (current.getType()) {
            case SearchContent.TYPE_USER:
                final User currentUser=current.getUser();
                if (currentUser == null) {
                    break;
                }
                ImageDownloader.loader(vH.avatar, currentUser.getAvatarURI());
                vH.user.setText(currentUser.getName());
                vH.intro.setText(currentUser.getIntro());
                vH.follow.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            PlayerActivity.following(currentUser.getUID(), v);
                        }
                    });
                PlayerActivity.setFollowingStatus(vH.follow, currentUser.getUID());
                vH.root.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Intent i=new Intent(main, AccountDetailActivity.class);
                            i.putExtra("uid", currentUser.getUID());
                            i.putExtra("avatar", currentUser.getAvatarURI());
                            Intent save=((Activity)main).getIntent();
                            Client.saveActivity(save);
                            main.startActivity(i);
                        }
                    });
                break;
            case SearchContent.TYPE_BLOG:
                final Blog currentBlog=current.getBlog();
                if (currentBlog == null) {
                    break;
                }
                ImageDownloader.loader(vH.avatar, currentBlog.getAvatarURI());
                vH.avatar.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Intent i=new Intent(main, AccountDetailActivity.class);
                            i.putExtra("uid", currentBlog.getUID());
                            Intent save=((Activity)main).getIntent();
                            Client.saveActivity(save);
                            main.startActivity(i);
                        }
                    });
                vH.title.setText(currentBlog.getTitle());
                vH.info.setText(StringUtils.strCat(new Object[]{currentBlog.getLikeCount(),
                                                       "获赞 - ",
                                                       currentBlog.getFavoriteCount(),
                                                       "收藏 - ",
                                                       currentBlog.getViewCount(),
                                                       "浏览 - ",
                                                       currentBlog.getTime()}));
                final ClientString string=new ClientString(currentBlog.getContent());
                string.load(vH.content, vH.isShowContent);
                vH.content.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            vH.isShowContent = !vH.isShowContent;
                            string.load(vH.content, vH.isShowContent);
                        }
                    });
                vH.root.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Intent i=new Intent(main, BlogDetailActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.putExtra("bid", currentBlog.getBID());
                            Intent save=((Activity)main).getIntent();
                            Client.saveActivity(save);
                            main.startActivity(i);
                        }
                    });
                break;
            case SearchContent.TYPE_VIDEO:
                final Video currentVideo=current.getVideo();
                if (currentVideo == null) {
                    break;
                }
                currentVideo.setCover(vH.cover);
                vH.video_detail_time.setText(currentVideo.getTime());
                vH.title.setText(currentVideo.getTitle());
                vH.user.setText(currentVideo.getUser());
                vH.info.setText(StringUtils.strCat(new String[]{currentVideo.getViewCount(), " ", currentVideo.getLikeCount()}));
                vH.cover.post(new Runnable(){
                        @Override
                        public void run() {
                            vH.video_detail_time.bringToFront();
                            vH.video_detail_time.requestLayout();
                        }
                    });
                vH.root.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Intent i=new Intent(main, PlayerActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.putExtra("vid", currentVideo.getVID());
                            i.putExtra("uid", currentVideo.getUID());
                            i.putExtra("title", currentVideo.getTitle());
                            i.putExtra("time", currentVideo.getTime());
                            i.putExtra("name", currentVideo.getUser());
                            i.putExtra("view", currentVideo.getViewCount());
                            i.putExtra("like", currentVideo.getLikeCount());
                            i.putExtra("favorite", currentVideo.getFavoriteCount());
                            Intent save=((Activity)main).getIntent();
                            Client.saveActivity(save);
                            main.startActivity(i);
                        }
                    });
                break;
        }
    }

    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int p) {
        int type=p >= data.size() ?data.getLast().getType(): data.get(p).getType();
        int res=0;
        switch (type) {
            case SearchContent.TYPE_USER:
                res = R.layout.list_user;
                break;
            case SearchContent.TYPE_BLOG:
                res = R.layout.list_blog;
                break;
            case SearchContent.TYPE_VIDEO:
                res = R.layout.list_video_detail;
                break;
        }
        View view=LayoutInflater.from(main).inflate(res, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        switch (type) {
            case SearchContent.TYPE_USER:
                holder.avatar = view.findViewById(R.id.user_avatar);
                holder.user = view.findViewById(R.id.main_user_name);
                holder.intro = view.findViewById(R.id.main_user_intro);
                holder.follow = view.findViewById(R.id.following_user);
                holder.cover = new ImageView(view.getContext());
                holder.video_detail_time = new TextView(view.getContext());
                holder.title = new TextView(view.getContext());
                holder.info = new TextView(view.getContext());
                holder.content=new TextView(view.getContext());
                break;
            case SearchContent.TYPE_BLOG:
                holder.avatar = view.findViewById(R.id.user_avatar);
                holder.title = view.findViewById(R.id.blog_title);
                holder.info = view.findViewById(R.id.blog_info);
                holder.content = view.findViewById(R.id.blog_content);
                holder.user = new TextView(view.getContext());
                holder.intro = new TextView(view.getContext());
                holder.follow = new Button(view.getContext());
                holder.cover = new ImageView(view.getContext());
                holder.video_detail_time = new TextView(view.getContext());
                break;
            case SearchContent.TYPE_VIDEO:
                holder.cover = view.findViewById(R.id.video_cover);
                holder.video_detail_time = view.findViewById(R.id.video_detail_time);
                holder.title = view.findViewById(R.id.video_title);
                holder.user = view.findViewById(R.id.video_user);
                holder.info = view.findViewById(R.id.video_detail_info);
                holder.avatar = new ImageView(view.getContext());
                holder.follow = new Button(view.getContext());
                holder.info = new TextView(view.getContext());
                holder.content=new TextView(view.getContext());
                break;
        }
        return holder;
    }

    private List<SearchContent> compare(List<SearchContent> list) {
        List<SearchContent> newData=new ArrayList<SearchContent>(list);
        Collections.sort(newData, new Comparator<SearchContent>(){
                @Override
                public int compare(SearchContent o1, SearchContent o2) {
                    return Integer.compare(o1.getType(), o2.getType());
                }
            });
        return newData;
    }

    public void setType(int t) {
        switch (t) {
            case SearchContent.TYPE_DEF:
                break;
            case SearchContent.TYPE_USER:
                for (SearchContent current:data) {
                    if (current.getType() != SearchContent.TYPE_USER) {
                        data.remove(current);
                    }
                }
                break;
            case SearchContent.TYPE_BLOG:
                for (SearchContent current:data) {
                    if (current.getType() != SearchContent.TYPE_BLOG) {
                        data.remove(current);
                    }
                }
                break;
            case SearchContent.TYPE_VIDEO:
                for (SearchContent current:data) {
                    if (current.getType() != SearchContent.TYPE_VIDEO) {
                        data.remove(current);
                    }
                }
                break;
        }
        notifyDataSetChanged();
    }

    public void setData(List<SearchContent> list) {
        data.clear();
        data.addAll(compare(list));
        notifyDataSetChanged();
    }

    public void addNewData(List<SearchContent> list) {
        if (data.containsAll(list)) {
            return;
        }
        data.addAll(list);
        notifyItemRangeInserted(data.size() - list.size(), list.size());
    }

    public boolean isContains(SearchContent another) {
        return data.contains(another);
    }
}
