package com.losthiro.ottohubclient;

import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.lang.ref.WeakReference;
import com.losthiro.ottohubclient.impl.UploadManager;
import java.io.*;
import android.os.*;
import com.losthiro.ottohubclient.utils.*;
import android.widget.*;
import com.losthiro.ottohubclient.impl.*;
import org.json.*;
import java.util.concurrent.*;

public class BasicActivity extends AppCompatActivity {
	public static final int LOGIN_REQUEST_CODE = 114;
	public static final int IMAGE_REQUEST_CODE = 514;
	public static final int VIDEO_REQUEST_CODE = 1919;
	public static final int AVATAR_REQUEST_CODE = 810;

	public static final long VIDEO_SIZE = 200 * 1024 * 1024;
	public static final long COVER_SIZE = 1024 * 1024;

	public static final String OLD_STORAGE = "/sdcard/OTTOHub/";
	public static final String PATH_DRAFT = "/draft/";
	public static final String PATH_SAVE = "/save/";
	public static final String FILE_MSG_DRAFT = "config/content_cache.json";
	public static final String FILE_SEARCH_HISTORY = "config/history_search.json";
	public static final String FILE_DEF_DICTIONARY = "config/rng_danmaku_config.json";

    private static final Semaphore request = new Semaphore(1);
	private static String currentStorage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Main", getClass().getName() + "has create");
	}

	public static String getCurrentStorage() {
		if (currentStorage == null) {
			currentStorage = OLD_STORAGE;
		}
		return currentStorage;
	}

	public static boolean setCurrentStorage(String newStorage) {
		File path = new File(newStorage);
		boolean isSuccess = DeviceUtils.getAndroidSDK() < Build.VERSION_CODES.R && path.exists() && path.isDirectory();
		if (isSuccess) {
			currentStorage = newStorage;
		}
		return isSuccess;
	}

	public static void resetStorage() {
		currentStorage = OLD_STORAGE;
	}
    
    public void checkServer(){
        try {
            if (!request.tryAcquire()) {
                return;
            }
            if (!NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
                return;
            }
            NetworkUtils.getNetwork.getNetworkJson(APIManager.SystemURI.getVersionURI(), new NetworkUtils.HTTPCallback() {
                    @Override
                    public void onSuccess(String content) {
                        if (content == null || content.isEmpty()) {
                            onFailed("empty content");
                            return;
                        }
                        try {
                            JSONObject json = new JSONObject(content);
                            if (json == null) {
                                onFailed("null json");
                                return;
                            }
                            String status = json.optString("status", "error");
                            if (status.equals("success")) {
                                
                                return;
                            }
                            onFailed(content);
                        } catch (JSONException e) {
                            onFailed(e.toString());
                        }
                    }

                    @Override
                    public void onFailed(final String cause) {
                        Log.e("Network", cause);
                        runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    // TODO: Implement this method
                                    Toast.makeText(getApplication(), cause, Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
                });
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } finally {
            request.release();
		}
    }
}

