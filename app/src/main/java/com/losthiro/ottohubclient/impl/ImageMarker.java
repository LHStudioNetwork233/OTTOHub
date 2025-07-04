/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.impl;

import android.content.*;
import android.database.*;
import android.net.*;
import android.provider.*;
import android.os.*;
import java.net.*;
import java.io.*;
import java.util.*;
import com.losthiro.ottohubclient.utils.*;
import android.widget.*;

public class ImageMarker extends AsyncTask<Uri, Void, String> {
	private String defURI = "https://img.api.aa1.cn/api/upload";
	private Context ctx;
	private NetworkUtils.HTTPCallback cmd;
	private int requestCode;

	public ImageMarker(Context c, NetworkUtils.HTTPCallback callback) {
		ctx = c;
        cmd = callback;
	}
    
	@Override
	protected String doInBackground(Uri[] params) {
		// TODO: Implement this method
		File file = new File(getPath(params[0]));
		String boundary = UUID.randomUUID().toString();
		String CRLF = "\r\n";
		try {
			URL url = new URL(defURI);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			// Create the output stream
			OutputStream outputStream = connection.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
			// Send file data
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + file.getName() + "\"")
					.append(CRLF);
			writer.append("Content-Type: image/" + getType(file)).append(CRLF);
			writer.append(CRLF).flush();
			InputStream inputStream = new FileInputStream(file);
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
			writer.append(CRLF).flush(); // End of file data
			writer.append("--" + boundary + "--").append(CRLF).flush();
			requestCode = connection.getResponseCode();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			writer.close();
			outputStream.close();
			connection.disconnect();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error: " + e.getMessage();
		}
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO: Implement this method
		super.onPostExecute(result);
		if (requestCode <= 0 || requestCode > 200) {
			cmd.onFailed(result);
			return;
		}
		cmd.onSuccess(result);
	}

	private String getType(File file) {
		String[] split = file.getName().split(".", 2);
		if (split.length == 2) {
			return split[1];
		}
		return null;
	}

	private String getPath(Uri uri) {
		String path = null;
		Cursor cursor = null;
		try {
			String[] projection = {MediaStore.Images.Media.DATA};
			cursor = ctx.getContentResolver().query(uri, projection, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				path = cursor.getString(columnIndex);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return path;
	}
}

