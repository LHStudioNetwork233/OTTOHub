/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.losthiro.ottohubclient.utils.*;
import android.content.*;
import android.text.method.*;
import android.webkit.*;

public class InfoActivity extends BasicActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		View content = findViewById(android.R.id.content);
		TextView tv = content.findViewWithTag("device_info");
		String pack = ApplicationUtils.getPackage(this);
		Object[] infos = {"android版本: android", DeviceUtils.getAndroidVersion(), "\nSDK版本: ",
				DeviceUtils.getAndroidSDK(), "\n设备型号: ", DeviceUtils.getDeviceModel(), "\n屏幕尺寸: ",
				DeviceUtils.getWindowHeight(this), "x", DeviceUtils.getWindowWidth(this), "\n内存大小: ",
				DeviceUtils.getAvailableMemory(this), "\n应用包名: ", pack, "\n应用名: ", ApplicationUtils.getName(this, pack),
				"\n版本号: ", ApplicationUtils.getVersionCode(this, pack), "\n当前版本: ",
				ApplicationUtils.getVersionName(this, pack), "\n目标SDK: ", ApplicationUtils.getTargetSDK(this, pack),
				"\n最小支持SDK: ", ApplicationUtils.getMinSDK(this, pack), "\n最大JVM内存: ", SystemUtils.getJVMmaxMemory()};
		tv.setText(StringUtils.strCat(infos));
		WebView log = content.findViewWithTag("update_log");
		log.loadUrl("file:///android_asset/Devinfo.html");
		TextView link = content.findViewWithTag("links");
		link.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// TODO: Implement this method
		super.onBackPressed();
		finish();
	}

	public void quit(View v) {
		finish();
	}
}

