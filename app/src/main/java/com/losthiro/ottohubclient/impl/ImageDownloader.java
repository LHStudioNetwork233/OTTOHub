package com.losthiro.ottohubclient.impl;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import com.losthiro.ottohubclient.utils.SystemUtils;
import java.io.InputStream;
import java.net.URL;
import android.graphics.PorterDuff;
import android.graphics.Color;
import android.graphics.Xfermode;
import android.view.ViewTreeObserver;
import android.widget.*;
import android.graphics.drawable.*;

/**
 * @Author Hiro
 * @Date 2025/05/22 22:26
 */
public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {//图片异步加载类
	public static final String TAG = "ImageDownloader";
	private static final ImageCatch memoryCatch = ImageCatch.getInstance();//设置为全局通用缓存
	private ImageView view;//目标view

	private ImageDownloader(ImageView v) {
		view = v;
		view.setScaleType(ImageView.ScaleType.CENTER_CROP);
		view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (!view.isAttachedToWindow()) {
					cancel(true);
				}
			}
		});//在图片从视图中移除时暂停图片加载，防止占用网络资源
	}

	public static void loader(ImageView target, String uri) {//主加载类，外部代码调用该方法即可
		Bitmap icon = memoryCatch.get(uri);
		if (icon == null) {
			ImageDownloader downloader = new ImageDownloader(target);
			downloader.execute(uri);
		} else {
			target.setImageBitmap(icon);
		}
	}

	public static void release() {
		memoryCatch.evictAll();//缓存释放，可选是否调用
	}

	@Override
	protected Bitmap doInBackground(String[] params) {
		String urlDisplay = params[0];
		Bitmap image = null;
		try {//后台操作，通过uri打开图片输入流
			InputStream in = new URL(urlDisplay).openStream();
			image = BitmapFactory.decodeStream(in);
			if (memoryCatch.get(urlDisplay) == null) {
				memoryCatch.put(urlDisplay, image);
			} //记录图片缓存，防止多次请求
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
		return image;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		if (result == null) {
			Log.e(TAG, "error load: bitmap==null");
			return;
		}
		view.setImageBitmap(result);//在主线程中设置图片到view
	}

	private static class ImageCatch extends LruCache<String, Bitmap> {
		private static final ImageCatch INSTANCE = new ImageCatch();

		private ImageCatch() {
			super((int) SystemUtils.getJVMmaxMemory() / 2);//设置缓存大小为JVM虚拟机最大内存的一半
		}

		public static final synchronized ImageCatch getInstance() {
			return INSTANCE;
		}

		@Override
		protected int sizeOf(String key, Bitmap value) {
			return value.getByteCount() / 1024;
		}
	}
}

