package com.losthiro.ottohubclient.view.drawer;

/**
 * @Author Hiro
 * @Date 2025/06/04 11:09
 */
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.SettingsActivity;
import com.losthiro.ottohubclient.adapter.menu.ImageAdapter;
import com.losthiro.ottohubclient.adapter.menu.ImageItem;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.impl.ImageDownloader;
import java.util.ArrayList;
import java.util.List;
import com.losthiro.ottohubclient.view.window.AccountSwitchWindow;
import com.losthiro.ottohubclient.*;
import android.app.*;

public class SlideDrawerManager {
    public static final String TAG = "SlideDrawerManager";
    private static final List<ImageItem> data=new ArrayList<>();
    private static SlideDrawerManager INSTANCE;
    private View p;
    private DrawerLayout more;
    private ImageButton slideButton;

    public static final synchronized SlideDrawerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SlideDrawerManager();
        }
        return INSTANCE;
    }

    private SlideDrawerManager() {
        initMenuList();
    }

    private void initMenuList() {
        data.add(new ImageItem("视频上传", R.drawable.ic_video_black, UploadVideoActivity.class));
        data.add(new ImageItem("动态上传", R.drawable.ic_blog_black, UploadBlogActivity.class));
        data.add(new ImageItem("关注列表", R.drawable.ic_like_black, SuscribeActivity.class));
        data.add(new ImageItem("粉丝列表", R.drawable.ic_list_black, FansActivity.class));
        data.add(new ImageItem("稿件管理", R.drawable.ic_more_black, UploadManagerActivity.class));
        data.add(new ImageItem("大法庭", R.drawable.ic_home_black, AuditActivity.class));
        data.add(new ImageItem("设置", R.drawable.ic_settings_black, SettingsActivity.class));
    }

    public void saveLastParent(View parent) {
        p = parent;
    }

    public View getLastParent() {
        return p;
    }

    public void registerDrawer(View parent) {
        if (parent == null) {
            return;
        }
        more = parent.findViewById(R.id.main_drawer_layout);
        final View menu = parent.findViewById(R.id.drawer_content);
        slideButton = parent.findViewById(R.id.main_slide_bar);
        final ObjectAnimator animator = ObjectAnimator.ofFloat(slideButton, "rotation", 0f);
        animator.setDuration(300);
        animator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        more.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerClosed(View view) {
                    slideButton.setImageResource(R.drawable.ic_more);
                    animator.reverse(); // 恢复到初始状态
                }

                @Override
                public void onDrawerOpened(View view) {
                    slideButton.setImageResource(R.drawable.ic_left_arrow);
                    animator.end(); // 结束动画
                    animator.setCurrentPlayTime(300);
                }

                @Override
                public void onDrawerSlide(View view, float slideOffset) {
                    float rotation = slideOffset * 180;
                    slideButton.setRotation(rotation);
                }

                @Override
                public void onDrawerStateChanged(int state) {
                }
            });
        slideButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (more.isDrawerOpen(menu)) {
                        more.closeDrawer(menu, true);
                    } else {
                        more.openDrawer(menu, true);
                    }
                }
            });
        final Context main=parent.getContext();
        AccountManager manager=AccountManager.getInstance(main);
        if (manager.isLogin()) {
            ImageDownloader.loader((ImageView)parent.findViewById(R.id.main_user_cover), manager.getAccount().getCoverURI());
            AccountSwitchWindow.getInstance(parent.getContext()).update();
        }
        ListView menuList=parent.findViewById(R.id.main_menu_list);
        menuList.setAdapter(new ImageAdapter(main, data));
        menuList.setOnItemClickListener(new OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ImageItem current=(ImageItem)parent.getItemAtPosition(position);
                    Intent i=new Intent(main, current.getLink());
                    Client.saveActivity(((Activity)main).getIntent());
                    main.startActivity(i);
                }
            });
    }

    public void showAccountSwitch(View v) {
        AccountSwitchWindow.getInstance(v.getContext()).switchShow(v);
    }
}
