package com.losthiro.ottohubclient.adapter.menu;
import android.widget.ArrayAdapter;
import android.content.Context;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.LayoutInflater;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.utils.*;
import android.graphics.*;

/**
 * @Author Hiro
 * @Date 2025/06/05 23:04
 */
public class ImageAdapter extends ArrayAdapter {
    public static final String TAG = "ImageAdapter";
    private List<ImageItem> menuData;

    public ImageAdapter(Context context, List<ImageItem> list) {
        super(context, 0);
        menuData = list;
    }

    @Override
    public Object getItem(int position) {
        return menuData.get(position);
    }

    @Override
    public int getCount() {
        return menuData.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_list_image, parent, false);
            view = new ViewHolder();
            view.icon = convertView.findViewById(R.id.menu_list_icon);
            view.title = convertView.findViewById(R.id.menu_list_title);
            convertView.setTag(view);
        } else {
            view = (ViewHolder)convertView.getTag();
        }
        ImageItem current=menuData.get(position);
        view.icon.setImageResource(current.getIconID());
        view.icon.setColorFilter(ResourceUtils.getColor(R.color.colorSecondary), PorterDuff.Mode.SRC_IN);
        view.title.setText(current.getTitle());
        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        ImageView icon;
    }
}
