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
	private View root;

	public BottomDialog(Context ctx, View v) {
		super(ctx);
		init(v);
	}

	public BottomDialog(Context ctx, View v, int themeResId) {
		super(ctx, themeResId);
		init(v);
	}

	private void init(View content) {
		root = content;
		requestWindowFeature(1);
		setContentView(content);
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
        if(root == null){
            return;
        }
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(root, "translationY", 100.0f, 0.0f);
        ofFloat.setDuration(1000L);
		ofFloat.start();
        super.show();
    }
}

