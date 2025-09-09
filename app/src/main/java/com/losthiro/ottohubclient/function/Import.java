/**
 * @Author Hiro
 * @Date 2025/09/09 11:58
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.function;
import android.content.*;
import android.app.*;
import com.losthiro.ottohubclient.*;
import android.provider.*;

public class Import implements Runnable {
	public static final String TAG_VIDEO = "ottohub/storage/import_video";
	public static final String TAG_IMAGE = "ottohub/storage/import_cover";
	private Context mContext;
	private int mCode;

	public Import(Context ctx, int code) {
		mContext = ctx;
		mCode = code;
	}

	@Override
	public void run() {
		// TODO: Implement this method
		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);;
		if (mCode == BasicActivity.FILE_REQUEST_CODE) {
			i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			i.setType("application/zip");
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}
		((Activity) mContext).startActivityForResult(i, mCode);
	}
}

