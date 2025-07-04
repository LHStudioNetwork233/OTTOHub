package com.losthiro.ottohubclient.view;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import com.losthiro.ottohubclient.utils.StringUtils;
import com.losthiro.ottohubclient.impl.danmaku.DefDanmakuManager;

/**
 * @Author Hiro
 * @Date 2025/05/23 08:39
 */
public class RandomTextEdit extends EditText{
    public static final String TAG = "RandomTextEdit";
    
    public RandomTextEdit(Context context) {
        super(context);
        init();
    }

    public RandomTextEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RandomTextEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setHint(DefDanmakuManager.getInstance(getContext()).getRandomString());
    }
}
