package com.losthiro.ottohubclient.view;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.text.TextPaint;

/**
 * @Author Hiro
 * @Date 2025/05/22 22:50
 */
public class RunTextView extends TextView{
    public static final String TAG = "RunTextView";
    
    public RunTextView(Context context) {
        super(context);
    }

    public RunTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public RunTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
