/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.view.dialog;
import android.app.*;
import android.content.*;
import android.view.*;
import android.view.ViewGroup.*;
import android.animation.*;
import android.os.*;

public class BottomDialog extends Dialog {
	public BottomDialog(Context ctx, View v) {
		super(ctx);
		init(v);
	}

	public BottomDialog(Context ctx, View v, int themeResId) {
		super(ctx, themeResId);
		init(v);
	}
    
    public BottomDialog(Context ctx, int id) {
        super(ctx);
        init(id);
    }

    public BottomDialog(Context ctx, int id, int themeResId) {
        super(ctx, themeResId);
        init(id);
	}

	private void init(View content) {
        requestWindowFeature(1);
        if (content != null) {
            setContentView(content);
        }
		init();
	}
    
    private void init(int id) {
        requestWindowFeature(1);
        if (id > 0) {
            setContentView(id);
        }
        init();
    }
    
    private void init() {
        Window window = getWindow();
        window.setFlags(4, 4);
        if (window != null) {
            window.setBackgroundDrawableResource(0x0106000d);
        }
        window.setGravity(80);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.y = 20;
        attributes.dimAmount = 0.0f;
        if (Build.VERSION.SDK_INT == 31) {
            attributes.setBlurBehindRadius(20);
        }
		window.setAttributes(attributes);
    }

    @Override
    public void show() {
        // TODO: Implement this method
        View root = getContent();
        if(root == null){
            return;
        }
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(root, "translationY", 100.0f, 0.0f);
        ofFloat.setDuration(1000L);
		ofFloat.start();
        super.show();
    }
    
    public View getContent() {
        return findViewById(android.R.id.content);
    }
}

