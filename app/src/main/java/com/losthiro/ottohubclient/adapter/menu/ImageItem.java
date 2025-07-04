package com.losthiro.ottohubclient.adapter.menu;
import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;
import android.graphics.Bitmap;

/**
 * @Author Hiro
 * @Date 2025/06/05 23:00
 */
public class ImageItem {
    public static final String TAG = "ImageItem";
    private Class<?> clz;
    private int image;
    private String title;
    
    public ImageItem(String title, int id, Class<?> link){
        this.title=title;
        this.image=id;
        this.clz=link;
    }
    
    public int getIconID(){
        return image;
    }
    
    public String getTitle(){
        return title;
    }
    
    public Class<?> getLink(){
        return clz;
    }
}
