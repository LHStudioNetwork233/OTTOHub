package com.losthiro.ottohubclient.view;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.graphics.Color;

/**
 * @Author Hiro
 * @Date 2025/05/23 23:22
 */
public class RoundImageView extends ImageView {
    public static final String TAG = "RoundImageView";
    private static final float radius=200f;
    private RectF rec;
    private Path path;

    public RoundImageView(Context context) {
        super(context);
        init();
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        path = new Path();
        rec = new RectF();
        setOutlineProvider(new ViewOutlineProvider(){
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
        setClipToOutline(true);
        setElevation(8f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        path.reset();
        rec.set(0, 0, getWidth(), getHeight());
        path.addRoundRect(rec, radius, radius, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm == null) {
            return;
        }
        super.setImageBitmap(bm);
        setScaleType(ImageView.ScaleType.CENTER_CROP);
        setBackgroundColor(Color.GRAY);
        invalidate();
    }
}
