/**
 * @Author Hiro
 * @Date 2025/09/08 19:36
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.view;
import android.widget.*;
import android.content.*;
import android.util.*;
import android.graphics.*;
import com.losthiro.ottohubclient.utils.*;

public class JVMListenerText extends TextView {
	private long lastTime;

	public JVMListenerText(Context ctx) {
		super(ctx);
        lastTime = System.currentTimeMillis();
	}

	public JVMListenerText(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
        lastTime = System.currentTimeMillis();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO: Implement this method
		super.onDraw(canvas);
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory(); // 最大内存
		long totalMemory = runtime.totalMemory(); // 已分配的内存
		long freeMemory = runtime.freeMemory(); // 空闲内存
		long currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis - lastTime > 1000) {
			// 将内存使用量转换为MB，并保留两位小数
			String[] memoryUsage = {"Use:", memoryString(totalMemory - freeMemory), "\nMax:", memoryString(maxMemory)};
			setText(StringUtils.strCat(memoryUsage));
			lastTime = currentTimeMillis;
		}
        invalidate();
	}

	private static String memoryString(long l) {
		return String.format("%.2f MB", l / (1024.0 * 1024.0));
	}
}

