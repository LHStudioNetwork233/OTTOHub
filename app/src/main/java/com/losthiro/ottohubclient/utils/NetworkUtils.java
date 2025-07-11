package com.losthiro.ottohubclient.utils;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import android.os.Handler;
import android.os.Looper;
import java.net.*;
import java.io.*;
import java.nio.charset.*;

/**
 * @Author Hiro
 * @Date 2025/05/21 19:01
 */
public class NetworkUtils {
	public static final String TAG = "NetworkUtils";

	public static boolean isNetworkAvailable(Context context) {//检查网络是否可用
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return false;
		}
		NetworkCapabilities capabilities = connectivityManager
				.getNetworkCapabilities(connectivityManager.getActiveNetwork());
		return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
	}

	public static class getNetwork {//获取网络内容内部类
		private static int defTimeout = 114514;//设置一个默认超时时间

		public static void getNetworkJson(String uri, HTTPCallback callback) {
			getNetworkJson(uri, callback, defTimeout);
		}

		public static void getNetworkJson(final String uri, final HTTPCallback callback, final int timeout) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					HttpsURLConnection http = null;
					try {
						http = (HttpsURLConnection) new URL(uri).openConnection();
						http.setRequestMethod("GET");
						http.setConnectTimeout(timeout);
						http.setReadTimeout(timeout);
						http.connect();
						int code = http.getResponseCode();
						if (code >= 200 && code < 300) {
							callback.onSuccess(StringUtils.streamReader(http.getInputStream()));
						} else {
							callback.onFailed("error " + code);
						}
					} catch (Exception e) {
						Log.e(TAG, "get network json content error", e);
						callback.onFailed(e.toString());
					} finally {
						if (http != null) {
							http.disconnect();
						}
					}
				}
			}).start();
		}

		public static void getNetworkByte(final String uri, final HTTPByteCallback callback, final int timeout) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					HttpsURLConnection http = null;
					try {
						http = (HttpsURLConnection) new URL(uri).openConnection();
						http.setRequestMethod("GET");
						http.setConnectTimeout(timeout);
						http.setReadTimeout(timeout);
						http.connect();
						int code = http.getResponseCode();
						if (code >= 200 && code < 300) {
							callback.onSuccess(StringUtils.byteStreamReader(http.getInputStream()));
						} else {
							callback.onFailed("error " + code);
						}
					} catch (Exception e) {
						Log.e(TAG, "get network json content error", e);
						callback.onFailed(e.toString());
					} finally {
						if (http != null) {
							http.disconnect();
						}
					}
				}
			}).start();
		}

		public static void download(String uri, final String targetDir) {
			getNetworkByte(uri, new HTTPByteCallback() {
				@Override
				public void onSuccess(byte[] data) {
					FileUtils.writeFile(targetDir, data);
				}

				@Override
				public void onFailed(String cause) {
					Log.e(TAG, cause);
				}
			}, 999999999);
		}

		public static void download(String uri, final String targetDir, final Runnable callback) {
			final Handler main = new Handler(Looper.getMainLooper());
			getNetworkByte(uri, new HTTPByteCallback() {
				@Override
				public void onSuccess(byte[] data) {
					FileUtils.writeFile(targetDir, data);
					main.post(callback);
				}

				@Override
				public void onFailed(String cause) {
					Log.e(TAG, cause);
				}
			}, 999999999);
		}
	}

	public static class postNetwork {
		private static int defTimeout = 114514;

		public static void postJSON(String uri, String postData, HTTPCallback callback) {
			postJSON(uri, postData, callback, defTimeout);
		}

		public static void postJSON(final String uri, final String postData, final HTTPCallback callback, final int timeOut) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO: Implement this method
					try {
						// 请求的 URL
						//String urlString = "https://api.ottohub.cn/module/creator/save_blog.php";
						URL url = new URL(uri);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("POST");
						connection.setDoOutput(true);
						connection.setConnectTimeout(timeOut);
						connection.setReadTimeout(timeOut);
                        connection.setRequestProperty("Host", "api.ottohub.cn");
                        connection.setRequestProperty("Origin", "https://m.ottohub.cn");
                        connection.setRequestProperty("Referer", "https://m.ottohub.cn/");
						connection.setRequestProperty("Content-Type",
								"application/x-www-form-urlencoded; charset=UTF-8");
						connection.setRequestProperty("User-Agent",
								"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
						try (OutputStream os = connection.getOutputStream()) {
							byte[] input = postData.getBytes(StandardCharsets.UTF_8);
							os.write(input, 0, input.length);
						}
						int responseCode = connection.getResponseCode();
						if (responseCode >= 200 && responseCode < 299) {
							callback.onSuccess(StringUtils.streamReader(connection.getInputStream()));
						} else {
							callback.onFailed("code=" + responseCode + StringUtils.streamReader(connection.getErrorStream()));
						}
						connection.disconnect();
					} catch (Exception e) {
						callback.onFailed(e.toString());
					}
				}
			}).start();
		}
	}

	public static interface HTTPCallback {
		void onSuccess(String content);
		void onFailed(String cause);
	}

	public static interface HTTPByteCallback {
		void onSuccess(byte[] data);
		void onFailed(String cause);
	}
}

