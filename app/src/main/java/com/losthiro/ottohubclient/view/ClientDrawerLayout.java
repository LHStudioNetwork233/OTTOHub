package com.losthiro.ottohubclient.view;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * @Author Hiro
 * @Date 2025/05/26 01:53
 */
public class ClientDrawerLayout extends ViewGroup {
    public static final String TAG = "SlideBar";
    private View mainContent;
    private View drawer;
    private int drawerWidth;
    private float startX;
    private float startY;
    private boolean isDragging = false;
    private boolean isDrawerOpen = false;

    public ClientDrawerLayout(Context c) {
        super(c);
    }

    public ClientDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClientDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClientDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChild(mainContent, widthMeasureSpec, heightMeasureSpec);
        measureChild(drawer, widthMeasureSpec, heightMeasureSpec);
        drawerWidth = drawer.getMeasuredWidth();
        setMeasuredDimension(resolveSize(mainContent.getMeasuredWidth(), widthMeasureSpec),
                             resolveSize(mainContent.getMeasuredHeight(), heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mainContent.layout(0, 0, mainContent.getMeasuredWidth(), mainContent.getMeasuredHeight());
        drawer.layout(-drawerWidth, 0, 0, drawer.getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                isDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = ev.getX() - startX;
                if (Math.abs(dx) > Math.abs(ev.getY() - startY)) {
                    isDragging = true;
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDragging) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - startX;
                if (isDrawerOpen) {
                    dx = Math.max(-drawerWidth, dx);
                } else {
                    dx = Math.min(0, dx);
                }
                drawer.layout((int) dx - drawerWidth, 0, (int) dx, drawer.getMeasuredHeight());
                break;
            case MotionEvent.ACTION_UP:
                if (event.getX() - startX > drawerWidth / 2) {
                    openDrawer();
                } else {
                    closeDrawer();
                }
                break;
        }
        return true;
    }

    public void openDrawer() {
        drawer.animate().translationX(0).setDuration(300).start();
        isDrawerOpen = true;
    }

    public void closeDrawer() {
        drawer.animate().translationX(-drawerWidth).setDuration(300).start();
        isDrawerOpen = false;
    }

    public void addView(View child) {
        if (mainContent == null) {
            mainContent = child;
        } else if (drawer == null) {
            drawer = child;
        }
        super.addView(child);
    }
}
