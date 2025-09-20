/**
 * @Author Hiro
 * @Date 2025/09/20 05:10
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.function;
import com.losthiro.ottohubclient.utils.*;
import android.util.*;
import org.json.*;
import com.losthiro.ottohubclient.view.*;
import android.content.*;
import android.app.*;
import android.widget.*;
import android.os.*;

public class UpdateCheck implements Runnable, DialogInterface.OnClickListener {
	private static final String URI = "https://api.github.com/repos/LHStudioNetwork233/OTTOHub/releases/latest";
	private static final Handler uiThread = new Handler(Looper.getMainLooper());
	private Context mContext;
	private String downloadUri;

	public UpdateCheck(Context ctx) {
		mContext = ctx;
	}

	@Override
	public void run() {
		// TODO: Implement this method
		NetworkUtils.getNetwork.getNetworkJson(URI, new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(String content) {
				// TODO: Implement this method
				try {
					JSONObject json = new JSONObject(content);
					downloadUri = json.optJSONArray("assets").optJSONObject(0).optString("browser_download_url");
					final String versionInfo = json.optString("body");
					requestConfig(versionInfo);
				} catch (Exception e) {
					onFailed(e.toString());
				}
			}

			@Override
			public void onFailed(String cause) {
				// TODO: Implement this method
				Log.e("Network", cause);
			}
		});
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO: Implement this method
		Toast.makeText(mContext, downloadUri, Toast.LENGTH_SHORT).show();
	}

	private void requestConfig(final String versionInfo) {
		NetworkUtils.getNetwork.getNetworkJson(URI, new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(String content) {
				// TODO: Implement this method
                boolean isLock = false;
                final boolean isCheck = false;
				uiThread.post(new Runnable() {
					@Override
					public void run() {
						// TODO: Implement this method
                        if (!isCheck) {
                            return;
                        }
						ClientWebView info = new ClientWebView(mContext);
						info.setTextData(versionInfo);
						info.load();
						AlertDialog.Builder build = new AlertDialog.Builder(mContext);
						build.setCancelable(false);
						build.setTitle("有新的版本");
						build.setView(info);
						build.setPositiveButton(android.R.string.ok, UpdateCheck.this);
						build.setNegativeButton(android.R.string.cancel, null);
						build.create().show();
					}
				});
			}

			@Override
			public void onFailed(String cause) {
				// TODO: Implement this method
				Log.e("Network", cause);
			}
		});
	}
}

