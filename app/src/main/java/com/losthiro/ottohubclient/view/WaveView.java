package com.losthiro.ottohubclient.view;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

/**
 * @Author Hiro
 * @Date 2025/05/24 13:28
 */
public class WaveView extends LinearLayout {
    public static final String TAG = "WaveView";
    public String MAINCOLOR_DEF="#ff88d9fa";
    public String NEXTCOLOR_DEF="#8088d9fa";
    private Double anglenum= 0.017453292519943295d;
    private ValueAnimator animator;
    private ValueAnimator animatorh;
    private Boolean antiAlias=true;
    private float arcRa=0f;
    private int count;
    private PointF drawPoint;
    private PointF drawPoint2;
    private Boolean iscircle=false;
    private float mHeight;
    private Paint mPaint;
    private Paint mPaintMore;
    private Path mPath;
    private float mWidth;
    private int mainColor;
    private int nextColor;
    private float waveDeep=10f;
    private float waveDeepMax=20f;
    private float waveDeepmin=8f;
    private float waveHeight;

    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public WaveView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    private void init() {
        this.mainColor = Color.parseColor(this.MAINCOLOR_DEF);
        this.nextColor = Color.parseColor(this.NEXTCOLOR_DEF);
        this.mPath = new Path();
        this.mPaint = new Paint();
        this.mPaint.setColor(this.mainColor);
        this.mPaint.setAntiAlias(this.antiAlias.booleanValue());
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setAlpha(50);
        this.mPaintMore = new Paint();
        this.mPaintMore.setAntiAlias(this.antiAlias.booleanValue());
        this.mPaintMore.setStyle(Paint.Style.FILL);
        this.mPaintMore.setColor(this.nextColor);
        this.mPaintMore.setAlpha(30);
        this.drawPoint = new PointF(0, 0);
        this.drawPoint2 = new PointF(0, 0);
    }

    public void ChangeWaveLevel(int i) {
        this.animator = ValueAnimator.ofFloat(this.waveDeepmin, this.waveDeepMax);
        this.animator.setDuration(20000);
        this.animator.setInterpolator(new LinearInterpolator());
        this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    waveDeep = animation.getAnimatedValue();
                }
            });
        this.animator.setRepeatMode(2);
        this.animator.setRepeatCount(3);
        this.animatorh = ValueAnimator.ofFloat(this.waveHeight, (this.mHeight * (10 - i)) / 10);
        this.animatorh.setDuration(25000);
        this.animatorh.setInterpolator(new DecelerateInterpolator());
        this.animatorh.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    waveHeight = animation.getAnimatedValue();
                }
            });
        this.animator.start();
        this.animatorh.start();
    }

    public void isCircle(Boolean bool) {
        this.iscircle = bool;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.iscircle.booleanValue()) {
            this.mPath.reset();
            this.mPath.addCircle(this.arcRa, this.arcRa, this.arcRa, Path.Direction.CW);
            canvas.clipPath(this.mPath);
        }
        this.drawPoint.x = 0;
        Double d = new Double(0.39269908169872414d * this.count);
        if (this.count == 16) {
            this.count = 0;
        } else {
            this.count++;
        }
        while (this.drawPoint.x < this.mWidth) {
            this.drawPoint.y = (float) (this.waveHeight - (this.waveDeep * Math.sin((this.drawPoint.x * this.anglenum.doubleValue()) - d.doubleValue())));
            this.drawPoint2.y = (float) (this.waveHeight - (this.waveDeep * Math.sin(((this.drawPoint.x * this.anglenum.doubleValue()) - d.doubleValue()) - 1.5707963267948966d)));
            canvas.drawLine(this.drawPoint.x, this.drawPoint2.y, this.drawPoint.x, this.mHeight, this.mPaintMore);
            canvas.drawLine(this.drawPoint.x, this.drawPoint.y, this.drawPoint.x, this.mHeight, this.mPaint);
            this.drawPoint.x += 1.0f;
        }
        postInvalidateDelayed(30);
    }

    @Override
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        this.mWidth = i;
        this.mHeight = i2;
        if (this.mWidth > this.mHeight) {
            this.arcRa = this.mHeight / 2;
            if (this.iscircle.booleanValue()) {
                this.mWidth = this.mHeight;
            }
        } else {
            this.arcRa = this.mWidth / 2;
            if (this.iscircle.booleanValue()) {
                this.mHeight = this.mWidth;
            }
        }
        this.waveHeight = this.mHeight;
        ChangeWaveLevel(5);
        super.onSizeChanged(i, i2, i3, i4);
    }

    public void setAntiAlias(Boolean bool) {
        this.antiAlias = bool;
        this.mPaint.setAntiAlias(bool.booleanValue());
        this.mPaintMore.setAntiAlias(bool.booleanValue());
    }

    public void setMainWaveColor(int i) {
        this.mainColor = i;
        this.mPaint.setColor(i);
    }

    public void setSecondaryWaveColor(int i) {
        this.nextColor = i;
        this.mPaintMore.setColor(i);
    }
}
