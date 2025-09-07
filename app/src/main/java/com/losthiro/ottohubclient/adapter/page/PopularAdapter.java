package com.losthiro.ottohubclient.adapter.page;
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
import com.losthiro.ottohubclient.adapter.model.*;
import org.json.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.*;
import com.losthiro.ottohubclient.view.*;
import com.losthiro.ottohubclient.utils.*;
import android.util.*;
/**
 * @Author Hiro
 * @Date 2025/05/27 15:44
 */
public class PopularAdapter extends PagerAdapter {
	public static final String TAG = "PopularAdapter";
	private List<JSONObject> data;
	private List<ImageView> holder;
	private Context main;

	public PopularAdapter(Context c, List<JSONObject> list) {
		main = c;
		data = new ArrayList<JSONObject>(new HashSet<JSONObject>(list));
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
		ImageView root = holder.get(position);
		((ViewPager) container).addView(root);
		return root;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView(holder.get(position));
	}

	private List<ImageView> initView() {
		List<ImageView> views = new ArrayList<>(data.size());
		for (final JSONObject obj : data) {
			ImageView view = new ImageView(main);
			view.setImageResource(R.drawable.ic_def_video_cover);
			view.setBackgroundResource(R.drawable.video_card_bg);
			view.setScaleType(ImageView.ScaleType.CENTER_CROP);
			view.setElevation(4f);
			final String link = obj.optString("href");
			String cover = StringUtils.strCat("https://", obj.optString("img_url"));
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ClientWebView.praseLinkAndLoad(main, link);
				}
			});
			ImageDownloader.loader(view, cover);
			views.add(view);
		}
		return views;
	}

	public String getTitle(int p) {
		return data.get(p).optString("title", "大家好啊，我是说的道理");
	}
}

