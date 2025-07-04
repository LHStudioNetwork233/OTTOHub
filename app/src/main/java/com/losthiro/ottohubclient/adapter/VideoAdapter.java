package com.losthiro.ottohubclient.adapter;
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
import com.losthiro.ottohubclient.PlayerActivity;
import com.losthiro.ottohubclient.R;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * @Author Hiro
 * @Date 2025/05/22 14:18
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    public static final String TAG = "VideoAdapter";
    private Context main;
    private List<Video> dataList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View root;
        public ImageView cover;
        public ImageView avatar;
        public TextView view_text;
        public TextView like_text;
        public TextView favorite_text;
        public TextView title;
        public TextView user;
        public TextView video_detail_info;
        public TextView video_detail_time;

        public ViewHolder(View v, int type) {
            super(v);
            cover = v.findViewById(R.id.video_cover);
            title = v.findViewById(R.id.video_title);
            user = v.findViewById(R.id.video_user);
            if (type == Video.VIDEO_DETAIL) {
                video_detail_time = v.findViewById(R.id.video_detail_time);
                video_detail_info = v.findViewById(R.id.video_detail_info);
            } else {
                view_text = v.findViewById(R.id.video_view_text);
                like_text = v.findViewById(R.id.video_like_text);
                favorite_text = v.findViewById(R.id.video_favorite_text);
                avatar = v.findViewById(R.id.video_avatar);
            }
            root = v;
        }
    }

    public VideoAdapter(Context c, List<Video> data) {
        main = c;
        dataList = new ArrayList<Video>(new LinkedHashSet<Video>(data));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onBindViewHolder(final VideoAdapter.ViewHolder vH, int p) {
        final Video currect=dataList.get(p);
        if (currect.isLocal()) {
            try {
                JSONObject mainfest=currect.getInfos(main);
                final long vid=mainfest.optLong("vid", -1);
                final String title=mainfest.optString("title", "大家好啊，今天来点大家想看的东西");
                final String user=mainfest.optString("user_name", "棍母");
                final String time=mainfest.optString("time", "2009-04-09 00:00:00");
                final String view=mainfest.optString("view_count", "0播放");
                final String like=mainfest.optString("like_count", "0获赞");
                final String favorite=mainfest.optString("favorite_count", "0冷藏");
                vH.cover.setImageBitmap(currect.getCover());
                vH.title.setText(title);
                if (currect.getType() == Video.VIDEO_DETAIL) {
                    vH.user.setText(user);
                    vH.video_detail_info.setText(view + " " + like);
                    vH.video_detail_time.setText(time);
                } else {
                    vH.avatar.setImageBitmap(currect.getAvatar());
                    vH.favorite_text.setText(favorite);
                    vH.like_text.setText(like);
                    vH.view_text.setText(view);
                    vH.user.setText(user + " - " + time);
                }
                vH.cover.post(new Runnable(){
                        @Override
                        public void run() {
                            if (currect.getType() == Video.VIDEO_DETAIL) {
                                vH.video_detail_time.bringToFront();
                                vH.video_detail_time.requestLayout();
                            } else {
                                vH.favorite_text.bringToFront();
                                vH.favorite_text.requestLayout();
                                vH.like_text.bringToFront();
                                vH.like_text.requestLayout();
                                vH.view_text.bringToFront();
                                vH.view_text.requestLayout();
                            }
                        }
                    });
                vH.root.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Intent i=new Intent(main, PlayerActivity.class);
                            i.putExtra("is_local", true);
                            i.putExtra("root_path", currect.getRootPath());
                            i.putExtra("vid", vid);
                            Intent save=((Activity)main).getIntent();
                            Client.saveActivity(save);
                            main.startActivity(i);
                        }
                    });
            } catch (JSONException e) {
                Log.e(TAG, "mainfest read failed", e);
            }
        } else {
            final long vid=currect.getVID();
            final String title=currect.getTitle();
            final String time=currect.getTime();
            final String view=currect.getViewCount();
            final String like=currect.getLikeCount();
            final String favorite=currect.getFavoriteCount();
            final long uid=currect.getUID();
            final String name=currect.getUser();
            currect.setCover(vH.cover);
            vH.title.setText(title);
            if (currect.getType() == Video.VIDEO_DETAIL) {
                vH.user.setText(name);
                vH.video_detail_info.setText(view + " " + like);
                vH.video_detail_time.setText(time);
            } else {
                currect.setAvatar(vH.avatar);
                vH.avatar.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Intent i=new Intent(main, AccountDetailActivity.class);
                            i.putExtra("uid", uid);
                            Intent save=((Activity)main).getIntent();
                            Client.saveActivity(save);
                            main.startActivity(i);
                        }
                    });
                vH.favorite_text.setText(favorite);
                vH.like_text.setText(like);
                vH.view_text.setText(view);
                vH.user.setText(name + " - " + time);
            }
            vH.cover.post(new Runnable(){
                    @Override
                    public void run() {
                        if (currect.getType() == Video.VIDEO_DETAIL) {
                            vH.video_detail_time.bringToFront();
                            vH.video_detail_time.requestLayout();
                        } else {
                            vH.favorite_text.bringToFront();
                            vH.favorite_text.requestLayout();
                            vH.like_text.bringToFront();
                            vH.like_text.requestLayout();
                            vH.view_text.bringToFront();
                            vH.view_text.requestLayout();
                        }
                    }
                });
            vH.root.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Intent i=new Intent(main, PlayerActivity.class);
                        i.putExtra("vid", vid);
                        i.putExtra("uid", uid);
                        i.putExtra("title", title);
                        i.putExtra("time", time);
                        i.putExtra("name", name);
                        i.putExtra("view", view);
                        i.putExtra("like", like);
                        i.putExtra("favorite", favorite);
                        Intent save=((Activity)main).getIntent();
                        Client.saveActivity(save);
                        main.startActivity(i);
                    }
                });
        }
    }

    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int p) {
        int currectType=dataList.get(p).getType();
        int id=currectType == Video.VIDEO_DEF ?R.layout.list_video: R.layout.list_video_detail;
        return new ViewHolder(LayoutInflater.from(main).inflate(id, viewGroup, false), currectType);
    }

    public boolean isExists(Video newVideo) {
        for (Video existingVideo : dataList) {
            if (existingVideo.equals(newVideo)) {
                return true;
            }
        }
        return false;
    }

    public void addNewData(List<Video> newData) {
        if (dataList.containsAll(newData)) {
            return;
        }
        dataList.addAll(newData);
        notifyItemRangeInserted(dataList.size() - newData.size(), newData.size());
    }

    public void setData(List<Video> newData) {
        if (dataList.containsAll(newData)) {
            return;
        }
        dataList.clear();
        dataList.addAll(newData);
        notifyDataSetChanged();
    }
}
