package com.losthiro.ottohubclient.view;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @Author Hiro
 * @Date 2025/05/30 10:53
 */
public class DanmakuView extends View {
    public static final String TAG = "DanmukuView";
    private static final List<DanmakuText> data=new ArrayList<>();
    private static final Queue<DanmakuText> pendingData=new LinkedList<>();
    private static final Handler timer=new Handler(Looper.getMainLooper());
    private static Paint paint;
    private static int width;
    private static int height;
    private Runnable updateCommand;
    private int scrollSpeed=5;
    private int maxCount=50;

    public DanmakuView(Context context) {
        super(context);
        init();
    }

    public DanmakuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DanmakuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DanmakuView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private void updateTexts() {
        synchronized (data) {
            for (int i = 0; i < data.size(); i++) {
                DanmakuText text = data.get(i);
                text.x -= scrollSpeed;
                if (text.x + text.textWidth < 0) {
                    data.remove(i);
                    i--; // 调整索引
                }
            }
            // 处理暂存的文本
            while (!pendingData.isEmpty() && data.size() < maxCount) {
                data.add(pendingData.poll());
            }
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (data) {
            for (DanmakuText text:data) {
                paint.setTextSize(text.size);
                paint.setColor(text.color);
                canvas.drawText(text.content, text.x, text.y, paint);
            }
        }
    }

    public void setDanmaku(List<DanmakuText> list) {
        synchronized (data) {
            data.clear();
            pendingData.clear();
            for (DanmakuText content:list) {
                if (data.size() < maxCount) {
                    data.add(content);
                } else {
                    pendingData.add(content);
                }
            }
        }
    }

    public void addDanmaku(String text, int color) {
        synchronized (data) {
            DanmakuText content=new DanmakuText(text, color);
            if (data.size() < maxCount) {
                data.add(content);
            } else {
                pendingData.add(content);
            }
        }
    }

    public void addDanmaku(String text) {
        addDanmaku(text, Color.WHITE);
    }

    public void start() {
        if (updateCommand == null) {
            updateCommand = new Runnable(){
                @Override
                public void run() {
                    updateTexts();
                    timer.postDelayed(this, 50);
                }
            };
        }
        timer.postDelayed(updateCommand, 50);
    }

    public void stop() {
        timer.removeCallbacks(updateCommand);
    }

    public static class DanmakuText {
        String content;
        float x;
        float y;
        float size;
        int color;
        int textWidth;

        public DanmakuText(String content, int color) {
            this.content = content;
            this.color = color;
            this.x = width;
            this.y = StringUtils.rng(0, height);
            this.size = StringUtils.rng(20, 50);
            paint.setTextSize(size);
            this.textWidth = (int)paint.measureText(content);
        }
        
        public DanmakuText(String content) {
            this.content = content;
            this.color = Color.WHITE;
            this.x = width;
            this.y = StringUtils.rng(0, height);
            this.size = StringUtils.rng(20, 50);
            paint.setTextSize(size);
            this.textWidth = (int)paint.measureText(content);
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
