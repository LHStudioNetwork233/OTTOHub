/**
 * @Author Hiro
 * @Date 2025/09/03 18:23
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.ui;
import androidx.fragment.app.*;
import android.os.Bundle;
import android.view.*;
import android.app.Dialog;
import com.losthiro.ottohubclient.R;
import android.widget.*;
import com.losthiro.ottohubclient.impl.*;
import android.view.View.*;
import android.util.*;
import com.losthiro.ottohubclient.view.dialog.*;
import com.losthiro.ottohubclient.utils.*;
import android.os.Environment;
import android.app.AlertDialog;
import java.io.*;
import android.os.*;
import android.graphics.drawable.*;
import android.view.animation.*;
import android.graphics.Matrix;
import android.graphics.RectF;
import com.github.chrisbanes.photoview.*;
import android.content.*;

public class ImageViewerFragment extends DialogFragment implements OnLongClickListener, OnClickListener {
	public static final String TAG = "ImageViewer";
	private PhotoView viewer;
	private OnClickListener mListener;

	public static ImageViewerFragment newInstance(String uri) {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		arg.putString("uri", uri);
		ImageViewerFragment imageViewer = new ImageViewerFragment();
		imageViewer.setArguments(arg);
		return imageViewer;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO: Implement this method
		return new Dialog(getActivity(), R.style.FullscreenDialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: Implement this method
		View root = inflater.inflate(R.layout.fragment_image_viewer, container, false);
		ImageButton quit = root.findViewById(R.id.image_viewer_quit);
		quit.setOnClickListener(this);
		ImageButton more = root.findViewById(R.id.image_viewer_more);
		more.setOnClickListener(this);
		viewer = root.findViewById(R.id.image_viewer);
		viewer.setOnLongClickListener(this);
		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onViewCreated(view, savedInstanceState);
		Bundle arg = getArguments();
		if (arg == null) {
			return;
		}
		String uri = arg.getString("uri");
		ImageDownloader.loader(viewer, uri);
	}

	@Override
	public void onClick(View v) {
		// TODO: Implement this method
		switch (v.getId()) {
			case R.id.image_viewer_quit :
				dismiss();
				break;
			case R.id.image_viewer_more :
				imageDia();
				break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO: Implement this method
		imageDia();
		return true;
	}

	private void imageDia() {
		final BottomDialog dialog = new BottomDialog(getContext(), R.layout.dialog_image);
		final AlertDialog progress = new UploadDialog(getContext()).create();
		final Runnable callback = new Runnable() {
			@Override
			public void run() {
				// TODO: Implement this method
				progress.dismiss();
				Toast.makeText(getContext(), "下载成功", Toast.LENGTH_SHORT).show();
			}
		};
        View imageDia = dialog.getContent();
        if (imageDia == null) {
            return;
        }
		if (mListener != null) {
			TextView upload = imageDia.findViewWithTag("upload_image");
			upload.setVisibility(View.VISIBLE);
			upload.setOnClickListener(mListener);
		}
		imageDia.findViewWithTag("download_image").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				Bundle arg = getArguments();
				if (arg == null) {
					return;
				}
				String name = StringUtils.strCat(new Object[]{"Image_", SystemUtils.getTime(), ".jpg"});
				progress.show();
				dialog.dismiss();
				Toast.makeText(getContext(), "即将开始下载", Toast.LENGTH_SHORT).show();
				NetworkUtils.getNetwork.download(arg.getString("uri"),
						new File(Environment.DIRECTORY_PICTURES, name).toString(), callback);
			}
		});
		imageDia.findViewWithTag("copy_uri").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				Bundle arg = getArguments();
				if (arg == null) {
					return;
				}
				progress.show();
				dialog.dismiss();
				Toast.makeText(getContext(), "链接复制成功~", Toast.LENGTH_SHORT).show();
				((ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE))
						.setText(arg.getString("uri"));
			}
		});
		dialog.show();
	}

	public void show(FragmentManager manager) {
		show(manager, TAG);
	}

	public void setUploadClickListener(OnClickListener listener) {
		mListener = listener;
	}
}

