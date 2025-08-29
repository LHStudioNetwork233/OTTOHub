package com.losthiro.ottohubclient.view;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.Toast;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.util.Log;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.io.File;
import com.losthiro.ottohubclient.utils.ApplicationUtils;
import com.losthiro.ottohubclient.impl.ClientString;
import android.content.Intent;
import com.losthiro.ottohubclient.BlogDetailActivity;
import com.losthiro.ottohubclient.Client;
import com.losthiro.ottohubclient.SearchActivity;
import com.losthiro.ottohubclient.AccountDetailActivity;
import com.losthiro.ottohubclient.impl.WebBean;
import com.losthiro.ottohubclient.utils.SystemUtils;
import android.net.*;
import android.database.*;
import android.os.*;
import android.content.res.*;
import android.provider.*;
import android.graphics.*;
import com.losthiro.ottohubclient.*;

/**
 * @Author Hiro
 * @Date 2025/06/18 17:39
 */
public class ClientWebView extends WebView {
	public static final String TAG = "ClientWebView";

	public ClientWebView(Context context) {
		super(context);
		init();
	}

	public ClientWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ClientWebView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public ClientWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		int windowStatus = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
		WebSettings setting = getSettings();
		setting.setJavaScriptEnabled(true);
		setting.setDomStorageEnabled(true);
        setting.setLoadWithOverviewMode(true);
        setting.setUseWideViewPort(true);
		setting.setAllowContentAccess(true);
		setting.setAllowFileAccess(true);
		setting.setAllowFileAccessFromFileURLs(true);
		setting.setAllowUniversalAccessFromFileURLs(true);
		setting.setSupportZoom(true);
		setting.setSupportMultipleWindows(true);
		setting.setMixedContentMode(0);
		setting.setCacheMode(WebSettings.LOAD_DEFAULT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			setting.setAlgorithmicDarkeningAllowed(windowStatus == Configuration.UI_MODE_NIGHT_YES);
		} else {
			setting.setForceDark(windowStatus == Configuration.UI_MODE_NIGHT_YES
					? WebSettings.FORCE_DARK_ON
					: WebSettings.FORCE_DARK_OFF);
		}
		setWebContentsDebuggingEnabled(true);
		setWebChromeClient(new ChromeClient());
		setWebViewClient(new ViewClient(getContext()));
        setBackgroundColor(Color.TRANSPARENT);
	}

	public void loadTextData(String content) {
		new WebBean(getContext(), content).loadHTML(this);
	}

	public static class ViewClient extends WebViewClient {
		private Context ctx;
		private String packName;

		public ViewClient(Context c) {
			ctx = c;
			packName = ApplicationUtils.getPackage(c).toLowerCase();
		}

		public void setPackage(String name) {
			packName = name.toLowerCase();
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			String uri = request.getUrl().toString().toLowerCase();
			if (uri.startsWith("https://m.ottohub.cn/")) {
				uri = uri.replace("https://m.ottohub.cn/", "");
				try {
					if (uri.startsWith("b")) {
						long bid = Long.parseLong(uri.split("/", 2)[1]);
						if (bid > 0) {
							Intent i = new Intent(ctx, BlogDetailActivity.class);
							i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							i.putExtra("bid", bid);
							Client.saveActivity(Client.getCurrentActivity(ctx).getIntent());
							ctx.startActivity(i);
						}
					}
					if (uri.startsWith("v")) {
						long vid = Long.parseLong(uri.split("/", 2)[1]);
						if (vid > 0) {
							Intent i=new Intent(ctx, PlayerActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.putExtra("vid", vid);
                            Client.saveActivity(Client.getCurrentActivity(ctx).getIntent());
                            ctx.startActivity(i);
						}
					}
					if (uri.startsWith("u")) {
						long uid = Long.parseLong(uri.split("/", 2)[1]);
						if (uid > 0) {
							Intent i = new Intent(ctx, AccountDetailActivity.class);
							i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							i.putExtra("uid", uid);
							Client.saveActivity(Client.getCurrentActivity(ctx).getIntent());
							ctx.startActivity(i);
						}
					}
				} catch (NumberFormatException e) {
					Log.d(TAG, e.toString());
				}
                return true;
			}
			SystemUtils.loadUri(ctx, uri);
			return true;
		}

		@Override
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError err) {
			super.onReceivedError(view, request, err);
			Log.e(TAG, err.getDescription().toString());
		}
	}

	public static class ChromeClient extends WebChromeClient {
	}
}

