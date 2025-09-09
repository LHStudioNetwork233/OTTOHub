/**
 * @Author Hiro
 * @Date 2025/09/08 20:46
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.view.window;
import android.content.*;
import android.view.*;
import android.view.WindowManager.*;
import com.losthiro.ottohubclient.*;
import android.app.*;
import android.view.View.*;
import android.os.*;

public class InfoWindow implements OnTouchListener {
	private static InfoWindow INSTANCE;
	private float[] lastClickPoint = new float[2];
	private Context mContext;
	private WindowManager manager;
	private WindowManager.LayoutParams params;
	private View root;

	private InfoWindow(Activity act) {
		mContext = act;
		init();
	}

	private void init() {
		manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		params = createParams();
		root = createView();
		manager.addView(root, params);
	}

	private View createView() {
		View parent = LayoutInflater.from(mContext).inflate(R.layout.window_info, null, false);
		parent.setVisibility(View.GONE);
		parent.setOnTouchListener(this);
		return parent;
	}

	private WindowManager.LayoutParams createParams() {
		WindowManager.LayoutParams value = new WindowManager.LayoutParams();
		value.type = Build.VERSION.SDK_INT > Build.VERSION_CODES.O
				? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
				: WindowManager.LayoutParams.TYPE_PHONE;
		value.format = -3;
		value.gravity = 85;
		value.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		value.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		value.flags = 1320;
		value.x = 128;
		value.y = 16;
		return value;
	}

	public static final synchronized InfoWindow getInstance(Activity act) {
		if (INSTANCE == null) {
			INSTANCE = new InfoWindow(act);
		}
		return INSTANCE;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO: Implement this method
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				lastClickPoint[0] = event.getX();
				lastClickPoint[1] = event.getY();
				break;
			case MotionEvent.ACTION_MOVE :
                float x = lastClickPoint[0];
				float y = lastClickPoint[1];
                lastClickPoint[0] = event.getX();
				lastClickPoint[1] = event.getY();
				params.x += (x - lastClickPoint[0]);
				params.y += (y - lastClickPoint[1]);
				manager.updateViewLayout(root, params);
				break;
		}
		return true;
	}
    
    public float[] getPos() {
        lastClickPoint[0] = params.x;
        lastClickPoint[1] = params.y;
        return lastClickPoint;
    }

	public void show() {
		if (root == null) {
			return;
		}
		if (root.getVisibility() == View.GONE) {
			root.setVisibility(View.VISIBLE);
		}
	}

	public void dissmiss() {
		if (root != null) {
			root.setVisibility(View.GONE);
		}
	}

	public void switchShow() {
		if (root != null) {
			root.setVisibility(root.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
		}
	}

	public void setShow(boolean isShow) {
		if (root != null) {
			root.setVisibility(isShow ? View.VISIBLE : View.GONE);
		}
	}

	public void release() {
		if (manager != null && root != null) {
			manager.removeView(root);
		}
	}
}

