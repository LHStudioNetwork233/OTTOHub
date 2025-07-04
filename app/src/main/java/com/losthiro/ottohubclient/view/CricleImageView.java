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
import android.graphics.Paint;
import android.graphics.Color;

/**
 * @Author Hiro
 * @Date 2025/06/01 17:25
 */
public class CricleImageView extends ImageView {
    public static final String TAG = "CricleImageView";
    private static final float radius=200f;
    private RectF rec;
    private Path path;
    private Paint roundBg;

    public CricleImageView(Context context) {
        super(context);
        init();
    }

    public CricleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CricleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        path = new Path();
        rec = new RectF();
        roundBg = new Paint();
        roundBg.setAntiAlias(true);
        roundBg.setColor(Color.WHITE);
        roundBg.setStyle(Paint.Style.STROKE);
        roundBg.setStrokeWidth(10);
        setOutlineProvider(new ViewOutlineProvider(){
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
        setClipToOutline(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        path.reset();
        super.onDraw(canvas);
        rec.set(0, 0, getWidth(), getHeight());
        path.addRoundRect(rec, radius, radius, Path.Direction.CW);
        canvas.drawPath(path, roundBg);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm == null) {
            return;
        }
        super.setImageBitmap(bm);
        setScaleType(ImageView.ScaleType.CENTER_CROP);
        invalidate();
    }
}
