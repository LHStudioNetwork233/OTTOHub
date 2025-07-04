package com.losthiro.ottohubclient.view.window;
import android.view.View;
import android.content.Context;
import android.view.WindowManager;
import android.view.LayoutInflater;
import com.losthiro.ottohubclient.R;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.widget.ImageButton;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Animator;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import androidx.recyclerview.widget.GridLayoutManager;
import com.losthiro.ottohubclient.impl.AccountManager;
import com.losthiro.ottohubclient.adapter.AccountAdapter;
import android.widget.*;
import android.graphics.*;
import android.graphics.drawable.*;

/**
 * @Author Hiro
 * @Date 2025/06/16 21:44
 */
public class AccountSwitchWindow {
	public static final String TAG = "AccountSwitchWindow";
	private static AccountSwitchWindow INSTANCE;
	private PopupWindow window;
	private RecyclerView accountList;
	private View contentView;
	private Context ctx;

	private AccountSwitchWindow(Context c) {
		ctx = c;
		contentView = LayoutInflater.from(c).inflate(R.layout.window_account_switch, null);
		window = new PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, true);
        window.setTouchable(true);
        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		accountList = contentView.findViewById(R.id.account_list);
		accountList.setLayoutManager(new GridLayoutManager(c, 1));
		accountList.setAdapter(new AccountAdapter(c));
	}

	public static final synchronized AccountSwitchWindow getInstance(Context c) {
		if (INSTANCE == null) {
			INSTANCE = new AccountSwitchWindow(c);
		}
		return INSTANCE;
	}

	public void switchShow(final View v) {
        window.setOnDismissListener(new PopupWindow.OnDismissListener(){
                @Override
                public void onDismiss() {
                    // TODO: Implement this method
                    ((ImageButton) v).setImageResource(R.drawable.ic_down);
                }
            });
		ObjectAnimator anim = ObjectAnimator.ofFloat(v, "rotation", window == null ? 0f : 180f);
		anim.setDuration(300L);
		anim.setInterpolator(new AccelerateDecelerateInterpolator());
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator anim) {
				super.onAnimationEnd(anim);
				if (window.isShowing()) {
					window.dismiss();
				} else {
					((ImageButton) v).setImageResource(R.drawable.ic_up);
					window.showAsDropDown(v, 0, window.getHeight());
				}
			}
		});
		anim.start();
	}

	public void update() {
		accountList.getAdapter().notifyDataSetChanged();
	}
}

