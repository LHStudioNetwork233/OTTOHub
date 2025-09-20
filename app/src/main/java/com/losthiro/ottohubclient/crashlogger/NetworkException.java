/**
 * @Author Hiro
 * @Date 2025/09/15 07:20
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.crashlogger;
import android.content.*;
import java.util.*;
import com.losthiro.ottohubclient.utils.*;
import org.json.*;
import android.os.Handler;
import android.os.Looper;
import android.widget.*;

public class NetworkException {
	private static NetworkException INSTANCE;
	private static final String DIR = "texts/";
	private static final HashMap<String, String> langs = new HashMap<>();
	private static final Handler uiThread = new Handler(Looper.getMainLooper());
	private Context mContext;

	private NetworkException(Context ctx) {
		mContext = ctx;
		init();
	}

	public static final synchronized NetworkException getInstance(Context ctx) {
		if (INSTANCE == null) {
			INSTANCE = new NetworkException(ctx);
		}
		return INSTANCE;
	}

	private final void init() {
		try {
			Locale locale = Locale.getDefault();
			String[] name = {DIR, locale.getLanguage(), "_", locale.getCountry(), ".lang"};
			String content = FileUtils.AssetUtils.readAssetsFile(mContext, StringUtils.strCat(name));
			if (content.isEmpty()) {
				return;
			}
			String[] lines = content.split(System.lineSeparator());
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				int index = line.indexOf("=");
				if (index >= 0) {
					langs.putIfAbsent(line.substring(0, index), line.substring(index));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handlerError(final String cause) {
		Runnable callback = new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				int type = 0;
				String msg = null;
				for (HashMap.Entry<String, String> entry : langs.entrySet()) {
					String tag = entry.getKey();
					String info = entry.getValue();
					if (tag.equals(cause) || tag.substring(1).equals(cause)) {
						if (tag.startsWith("#")) {
							type = 1;
						}
						if (tag.startsWith("!")) {
							type = 2;
						}
						msg = info;
					}
				}
				if (msg == null) {
					return;
				}
				switch (type) {
					case 0 :
						Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
						break;
					case 1 :
						System.out.println(msg);
						break;
					case 2 :
						break;
				}
			}
		};
		Looper mainLooper = Looper.getMainLooper();
		Thread mainThread = mainLooper.getThread();
		if (mainLooper.equals(Looper.myLooper()) && mainThread == Thread.currentThread()) {//先检测是否在主线程中的调用
			callback.run();//如果在主线程直接运行
		} else {
			uiThread.post(callback);
		}
	}
}

