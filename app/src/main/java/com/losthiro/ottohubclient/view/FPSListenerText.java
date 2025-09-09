/**
 * @Author Hiro
 * @Date 2025/09/08 19:42
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.view;
import android.content.*;
import android.widget.*;
import android.util.*;
import android.graphics.*;

public class FPSListenerText extends TextView {
    private int frameCount;
    private long lastTime;
    
	public FPSListenerText(Context ctx) {
		super(ctx);
        init();
	}

	public FPSListenerText(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		init();
	}
    
    private void init() {
        lastTime = System.currentTimeMillis();
        frameCount = 0;
    }

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO: Implement this method
		super.onDraw(canvas);
        frameCount++;
        long currentTimeMillis = System.currentTimeMillis();
        long j = currentTimeMillis - lastTime;
        if (j > 1000) {
            setText(String.format("FPS: %.2f", new Float((frameCount * 1000) / ((float) j))));
            frameCount = 0;
            lastTime = currentTimeMillis;
        }
        invalidate();
	}
}

