package com.losthiro.ottohubclient.crashlogger;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;
import com.losthiro.ottohubclient.ErrorActivity;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.utils.DeviceUtils;
import com.losthiro.ottohubclient.utils.FileUtils;
import com.losthiro.ottohubclient.utils.StringUtils;
import com.losthiro.ottohubclient.utils.SystemUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Hiro
 * @Date 2025/05/21 14:18
 */
public class CrashManager implements Thread.UncaughtExceptionHandler {
	public static final String TAG = "CrashManager";
	private static CrashManager INSTANCE = new CrashManager();
	private Context mContext;
	private Thread.UncaughtExceptionHandler def;
	private HashMap<String, String> infoMap = new HashMap<>();

	public static final CrashManager getInstance() {
		return INSTANCE;
	}

	public final void register(Context ctx) {
		mContext = ctx;
		def = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
		if (!handleException(e) && (uncaughtExceptionHandler = def) != null) {
			uncaughtExceptionHandler.uncaughtException(t, e);
			return;
		}
		try {
			Thread.sleep(3000L);
		} catch (InterruptedException e2) {
			Log.e("CrashHandler", "error : ", e2);
		}
		Process.killProcess(Process.myPid());
		System.exit(1);
	}

	private boolean handleException(Throwable e) {
		if (e == null) {
			return false;
		}
		final String errorToast = StringUtils.strCat(mContext.getString(R.string.error_toast), saveLog(e));
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(mContext, errorToast, Toast.LENGTH_SHORT).show();
				Looper.loop();
			}
		}).run();
		collectInfo(mContext);
		return true;
	}

	private void collectInfo(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 1);
			if (packageInfo != null) {
				infoMap.put("versionName", packageInfo.versionName == null ? "null" : packageInfo.versionName);
				infoMap.put("versionCode", StringUtils.toStr((Integer) packageInfo.versionCode));
			}
			for (Field field : Class.forName("android.os.Build").getDeclaredFields()) {
				field.setAccessible(true);
				String name = field.getName();
				String stack = StringUtils.toStr(field.get(null));
				infoMap.put(name, stack);
				Log.d(TAG, StringUtils.strCat(new String[]{name, " : ", stack}));
			}
		} catch (ClassNotFoundException e) {
			throw new NoClassDefFoundError(e.getMessage());
		} catch (IllegalAccessException e) {
			Log.e(TAG, "an error occured when collect crash info", e);
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "an error occured when collect package info", e);
		}
	}

	private String saveLog(Throwable e) {
		StringBuffer buffer = new StringBuffer();
		for (Map.Entry<String, String> entry : infoMap.entrySet()) {
			buffer.append(entry.getKey()).append("=").append(entry.getValue()).append(System.lineSeparator());
		}
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
			cause.printStackTrace(printWriter);
		}
		printWriter.close();
		buffer.append(writer.toString());
		try {
			Intent intent = new Intent(this.mContext, ErrorActivity.class);
			intent.addFlags(0x10000000);
			intent.putExtra("error_name", e.getLocalizedMessage());
			intent.putExtra("error_text", buffer.toString());
			this.mContext.startActivity(intent);
			String fileName = StringUtils.strCat(new String[]{"OTTOHub_log_",
					SystemUtils.getDate("yyyy_MM_dd_HH_mm_ss_"), StringUtils.toStr(SystemUtils.getTime()), ".log"});
			String filePath = FileUtils.getStorage(mContext, null);
			FileUtils.createDir(filePath);
			File log = new File(filePath, fileName);
			FileOutputStream fileOutputStream = new FileOutputStream(log);
			fileOutputStream.write(buffer.toString().getBytes());
			fileOutputStream.close();
			return log.toString();
		} catch (Exception e2) {
			Log.e(TAG, "save log failed", e2);
			return null;
		}
	}
}

