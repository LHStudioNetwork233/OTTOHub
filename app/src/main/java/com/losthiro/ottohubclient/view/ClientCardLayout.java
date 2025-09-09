/**
 * @Author Hiro
 * @Date 2025/09/09 01:36
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.view;
import android.widget.*;
import android.content.*;
import android.util.*;
import android.view.*;
import com.losthiro.ottohubclient.*;
import android.annotation.*;
import com.losthiro.ottohubclient.impl.*;
import android.view.View.*;
import androidx.core.widget.*;
import androidx.recyclerview.widget.*;
import android.graphics.*;

public class ClientCardLayout extends RelativeLayout {
	private boolean isAnim = true;
	private float[] startPoint = new float[2];
	private int currentPivot;

	public ClientCardLayout(Context context) {
		super(context);
		init();
	}

	public ClientCardLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setClickable(true);
		setElevation(4);
		setPadding(8, 5, 8, 5);
		setBackgroundResource(R.drawable.video_card_bg);
	}

	@Override
	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent event) {
		// TODO: Implement this method
		View parent = (View) getParent();
		if (parent instanceof ScrollView || parent instanceof NestedScrollView || parent instanceof ListView
				|| parent instanceof RecyclerView) {
			isAnim = false;
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				startPoint[0] = event.getX();
				startPoint[1] = event.getY();
				currentPivot = ClientAnim.startAnimDown(this, isAnim, event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_MOVE :
				int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
				if (Math.abs(event.getX() - startPoint[0]) > touchSlop
						|| Math.abs(event.getY() - startPoint[1]) > touchSlop) {
					ClientAnim.startAnimUp(this, currentPivot);
				}
				break;
			case MotionEvent.ACTION_UP :
				ClientAnim.startAnimUp(this, currentPivot);
				ClickSounds.playSound(getContext());
				break;
		}
		return super.onTouchEvent(event);
	}

	public void openAnim() {
		isAnim = true;
	}

	public void closeAnim() {
		isAnim = false;
	}
}

