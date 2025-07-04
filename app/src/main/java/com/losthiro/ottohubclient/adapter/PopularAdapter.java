package com.losthiro.ottohubclient.adapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.losthiro.ottohubclient.Client;
import com.losthiro.ottohubclient.PlayerActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import com.losthiro.ottohubclient.R;
/**
 * @Author Hiro
 * @Date 2025/05/27 15:44
 */
public class PopularAdapter extends PagerAdapter {
    public static final String TAG = "PopularAdapter";
    private List<Video> data;
    private List<ImageView> holder;
    private Context main;

    public PopularAdapter(Context c, List<Video> list) {
        main = c;
        data = new ArrayList<Video>(new HashSet<Video>(list));
        holder = initView();
    }

    @Override
    public int getCount() {
        return holder.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView root=holder.get(position);
        ((ViewPager)container).addView(root);
        return root;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager)container).removeView(holder.get(position));
    }
    
    private List<ImageView> initView(){
        List<ImageView> views=new ArrayList<>(data.size());
        for (final Video current: data) {
            ImageView view=new ImageView(main);
            view.setImageResource(R.drawable.ic_def_video_cover);
            view.setBackgroundResource(R.drawable.video_card_bg);
            view.setElevation(4f);
            final String name=current.getTitle();
            view.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Intent i=new Intent(main, PlayerActivity.class);
                        i.putExtra("vid", current.getVID());
                        i.putExtra("uid", current.getUID());
                        i.putExtra("title", name);
                        i.putExtra("time", current.getTime());
                        i.putExtra("name", current.getUser());
                        i.putExtra("view", current.getViewCount());
                        i.putExtra("like", current.getLikeCount());
                        i.putExtra("favorite", current.getFavoriteCount());
                        Intent save=((Activity)main).getIntent();
                        Client.saveActivity(save);
                        main.startActivity(i);
                    }
                });
            current.setCover(view);
            views.add(view);
        }
        return views;
    }
    
    public Video get(int p){
        return data.get(p);
    }
}
