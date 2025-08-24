/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.view.dialog;
import com.losthiro.ottohubclient.R;
import android.app.*;
import android.content.*;
import pl.droidsonroids.gif.*;
import android.view.*;

public class UploadDialog extends AlertDialog.Builder {
	public UploadDialog(Context c) {
		super(c);
		final GifImageView view = new GifImageView(c);
        view.setLayoutParams(new ViewGroup.LayoutParams(35, 35));
		view.setImageResource(R.drawable.loading);
		setIcon(R.drawable.ic_launcher);
		setTitle("正在走位中~别急...");
		setCancelable(false);
		setView(view);
		setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	}
}

