/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient;
import android.view.*;
import android.content.*;
import android.os.Bundle;
import android.os.Build;
import android.provider.*;
import android.widget.*;
import android.net.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.utils.*;
import java.util.*;
import com.losthiro.ottohubclient.adapter.*;
import androidx.recyclerview.widget.*;
import android.view.View.*;
import android.app.*;
import android.animation.*;
import android.widget.AdapterView.*;
import android.text.method.*;
import android.text.*;
import android.graphics.*;
import java.io.*;
import org.json.*;
import android.util.*;
import android.os.AsyncTask;
import java.net.*;
import android.database.*;

public class UploadVideoActivity extends MainActivity {
	private Uri video;
	private Uri cover;
	private int videoCategory = -1;
	private int videoCopyright = -1;
	private TagAdapter adapter;
	private ImageView coverView;
	private TextView text;
    private TextView info;
	private Button categoryBtn;
	private Button typeBtn;
	private EditText title;
	private EditText intro;
	private EditText tag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_video);
		if (!NetworkUtils.isNetworkAvailable(this)) {
			Toast.makeText(getApplication(), "唉服务器怎么死了", Toast.LENGTH_SHORT).show();
			return;
		}
		AccountManager manager = AccountManager.getInstance(this);
		if (!manager.isLogin()) {
			Toast.makeText(getApplication(), "那我缺的登录这一块", Toast.LENGTH_SHORT).show();
			return;
		}
		View parent = findViewById(android.R.id.content);
		adapter = new TagAdapter(this);
		text = parent.findViewWithTag("cover");
		categoryBtn = parent.findViewWithTag("switch_category");
		categoryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				switchCategoryDia();
			}
		});
		typeBtn = parent.findViewWithTag("switch_copyright");
		typeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				switchCopyrightDia();
			}
		});
        info = parent.findViewWithTag("uploader_info");
        
		title = findViewById(R.id.video_upload_title);
		intro = findViewById(R.id.video_upload_intro);
		tag = findViewById(R.id.upload_tags_edit);
		coverView = findViewById(R.id.video_cover);
		coverView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				coverPicker();
			}
		});
		RecyclerView tagsView = findViewById(R.id.tags_list);
		tagsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		tagsView.setAdapter(adapter);
		TextView error = parent.findViewWithTag("error");
		error.setMovementMethod(LinkMovementMethod.getInstance());
		videoPicker();
	}

	@Override
	protected void onDestroy() {
		// TODO: Implement this method
		Intent i = Client.getLastActivity();
		if (i != null) {
			Client.removeActivity();
			startActivity(i);
		}
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// TODO: Implement this method
		super.onBackPressed();
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO: Implement this method
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null && resultCode == RESULT_OK) {
			if (requestCode == VIDEO_REQUEST_CODE) {
				video = data.getData();
                info.setText(getInfo());
			} else if (requestCode == IMAGE_REQUEST_CODE) {
				cover = data.getData();
				coverView.setImageURI(cover);
				text.setTextColor(Color.WHITE);
				text.setBackgroundResource(R.drawable.text_bg);
                info.setText(getInfo());
			}
			Toast.makeText(getApplication(), data.getDataString(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplication(), "请重新选择文件", Toast.LENGTH_SHORT).show();
		}
	}
    
    private static String getPath(Context ctx, Uri uri) {
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

	private void videoPicker() {
		Toast.makeText(getApplication(), "请选择要上传的视频文件(限定mp4)，大小不超过200MB", Toast.LENGTH_SHORT).show();
		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, VIDEO_REQUEST_CODE);
	}

	private void coverPicker() {
		Toast.makeText(getApplication(), "请选择要上传的视频封面(限定jpg)，大小不超过1MB", Toast.LENGTH_SHORT).show();
		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, IMAGE_REQUEST_CODE);
	}
    
    private String getInfo(){
        StringBuilder string = new StringBuilder();
        if(cover==null){
            string.append("未选择封面").append(System.lineSeparator());
        }else{
            File f=new File(getPath(this, cover));
            Bitmap bitmap=BitmapFactory.decodeFile(f.getPath());
            string.append("封面名称: ").append(f.getName()).append(System.lineSeparator());
            string.append("封面大小: ").append(bitmap.getWidth()).append("x").append(bitmap.getHeight()).append(System.lineSeparator());
            string.append("文件大小: ").append(getSize(f)).append(System.lineSeparator());
        }
        if(video==null){
            string.append("未选择视频");
        }else{
            File f=new File(getPath(this, video));
            string.append("视频名称: ").append(f.getName()).append(System.lineSeparator());
            string.append("视频大小: ").append(getSize(f));
        }
        return string.toString();
    }

    private String getSize(File f) {
        // TODO: Implement this method
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = 0;
        double size = f.length();
        if(size<=0){
            return "0B";
        }
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }

	private HashMap<String, Integer> initCategorys() {
		HashMap<String, Integer> data = new HashMap<>();
		data.put("鬼畜调教", APIManager.VideoURI.CATEGORY_FUN);
		data.put("音mad", APIManager.VideoURI.CATEGORY_MAD);
		data.put("人力vocaloid", APIManager.VideoURI.CATEGORY_VOCALOID);
		data.put("剧场", APIManager.VideoURI.CATEGORY_THEATER);
		data.put("游戏", APIManager.VideoURI.CATEGORY_GAME);
		data.put("怀旧", APIManager.VideoURI.CATEGORY_OLD);
		data.put("音乐", APIManager.VideoURI.CATEGORY_MUSIC);
		data.put("其他", APIManager.VideoURI.CATEGORY_OTHER);
		return data;
	}

	private HashMap<String, Integer> initCopyright() {
		HashMap<String, Integer> data = new HashMap<>();
		data.put("转载", 1);
		data.put("自制", 2);
		data.put("其他", 0);
		return data;
	}

	private String createDraftJSON() throws JSONException {
		JSONObject root = new JSONObject();
		root.put("title", title.getText().toString());
		root.put("intro", intro.getText().toString());
		root.put("cover_uri", cover.toString());
		root.put("video_uri", video.toString());
		root.put("category", videoCategory);
		root.put("type", videoCopyright);
		root.put("tags", new JSONArray(adapter.getTags()));
		return root.toString(4);
	}

	private void switchCategoryDia() {
		final Dialog cateDia = new Dialog(this);
		final HashMap<String, Integer> map = initCategorys();
		List<String> data = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			data.add(entry.getKey());
		}
		ArrayAdapter<String> category = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
		ListView content = new ListView(this);
		content.setBackgroundResource(R.drawable.video_card_bg);
		content.setAdapter(category);
		content.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO: Implement this method
				String current = (String) parent.getItemAtPosition(position);
				videoCategory = map.get(current);
				categoryBtn.setText(current);
				cateDia.dismiss();
			}
		});
		cateDia.requestWindowFeature(1);
		cateDia.setContentView(content);
		ObjectAnimator ofFloat = ObjectAnimator.ofFloat(content, "translationY", 100.0f, 0.0f);
		ofFloat.setDuration(1000L);
		ofFloat.start();
		Window window = cateDia.getWindow();
		window.setFlags(4, 4);
		if (window != null) {
			window.setBackgroundDrawableResource(0x0106000d);
		}
		window.setGravity(80);
		WindowManager.LayoutParams attributes = window.getAttributes();
		attributes.y = 20;
		attributes.dimAmount = 0.0f;
		if (Build.VERSION.SDK_INT == 31) {
			attributes.setBlurBehindRadius(20);
		}
		window.setAttributes(attributes);
		cateDia.show();
	}

	private void switchCopyrightDia() {
		final Dialog cateDia = new Dialog(this);
		final HashMap<String, Integer> map = initCopyright();
		List<String> data = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			data.add(entry.getKey());
		}
		ArrayAdapter<String> category = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
		ListView content = new ListView(this);
		content.setAdapter(category);
		content.setBackgroundResource(R.drawable.video_card_bg);
		content.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO: Implement this method
				String current = (String) parent.getItemAtPosition(position);
				typeBtn.setText(current);
				videoCopyright = map.get(current);
				cateDia.dismiss();
			}
		});
		cateDia.requestWindowFeature(1);
		cateDia.setContentView(content);
		ObjectAnimator ofFloat = ObjectAnimator.ofFloat(content, "translationY", 100.0f, 0.0f);
		ofFloat.setDuration(1000L);
		ofFloat.start();
		Window window = cateDia.getWindow();
		window.setFlags(4, 4);
		if (window != null) {
			window.setBackgroundDrawableResource(0x0106000d);
		}
		window.setGravity(80);
		WindowManager.LayoutParams attributes = window.getAttributes();
		attributes.y = 20;
		attributes.dimAmount = 0.0f;
		if (Build.VERSION.SDK_INT == 31) {
			attributes.setBlurBehindRadius(20);
		}
		window.setAttributes(attributes);
		cateDia.show();
	}

	private void draftManagerDia(String path, String name) {
		final File f = new File(path, name);
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		Button delete = new Button(this);
		delete.setText("删除草稿");
		delete.setTextColor(Color.WHITE);
		delete.setBackgroundResource(R.drawable.btn_bg);
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				delete(f);
			}
		});
		layout.addView(delete);
		Button rename = new Button(this);
		rename.setText("草稿改名");
		rename.setTextColor(Color.WHITE);
		rename.setBackgroundResource(R.drawable.btn_bg);
		rename.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				rename(f);
			}
		});
		layout.addView(rename);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("草稿文件操作");
		builder.setMessage(StringUtils.strCat("当前选择: ", name));
		builder.setView(layout);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void delete(final File f) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("确定删除草稿？");
		builder.setMessage("该操作不可逆，请谨慎选择");
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				if (FileUtils.deleteFile(f.getPath())) {
					Toast.makeText(getApplication(), "操作成功", Toast.LENGTH_SHORT).show();
					dia.dismiss();
				}
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void rename(final File f) {
		final EditText name = new EditText(this);
		name.setHint("请在此处输入文件新名字，后缀必须为.json否则无法读取");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("确定删除草稿？");
		builder.setMessage("该操作不可逆，请谨慎选择");
		builder.setView(name);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				if (FileUtils.renameFile(f.getPath(), name.getText().toString())) {
					Toast.makeText(getApplication(), "操作成功", Toast.LENGTH_SHORT).show();
					dia.dismiss();
				}
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void uploader(String uri) {
		new VideoUploader(this, uri, new onUploaderCallback() {
			@Override
			public void onSuccess(String reslutURI) {
				// TODO: Implement this method
				try {
					JSONObject json = new JSONObject(reslutURI);
					if (json.optString("status", "error").equals("success")) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO: Implement this method
								Toast.makeText(getApplication(), "视频发布成功~冲刺冲刺", Toast.LENGTH_SHORT).show();
							}
						});
						return;
					}
					onFailed(json.optString("message"));
				} catch (Exception e) {
					onFailed(e.toString());
				}
			}

			@Override
			public void onFailed(final String message) {
				// TODO: Implement this method
				Log.e("Network", message);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO: Implement this method
						String msg = "视频发布失败Σ(ﾟдﾟlll)";
						switch (message) {
							case "title_too_long" :
								msg = "标题笑传之长长版";
								break;
							case "title_too_short" :
								msg = "标题还是太短了";
								break;
							case "intro_too_long" :
								msg = "不能在简介写小作文";
								break;
							case "intro_too_short" :
								msg = "我缺的简介这一块";
								break;
							case "error_type" :
								msg = "请选择一个版权声明";
								break;
							case "error_category" :
								msg = "至少选一个分区罢";
								break;
							case "error_tag" :
								msg = "标签笑传之错错报";
								break;
							case "error_file" :
								msg = "抱歉目前还不支持这种格式捏∑(￣□￣)";
								break;
							case "warn" :
								msg = "这是碰也不能碰的话题(ﾉ ○ Д ○)ﾉ　";
								break;
							case "file_not_found" :
								msg = "你好像还没选择文件呢";
								break;
							case "too_big_file" :
								msg = "不能上传太大的文件啊";
								break;
						}
						Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	public void videoPicker(View v) {
		videoPicker();
	}

	public void addTags(View v) {
		String current = tag.getText().toString();
		if (current.isEmpty()) {
			Toast.makeText(getApplication(), "不能输入棍母标签", Toast.LENGTH_SHORT).show();
			return;
		}
		adapter.addTag(current);
		tag.setText("");
	}

	public void clearTags(View v) {
		adapter.clearTags();
	}

	public void uploadVideo(View v) {
		StringBuilder tags = new StringBuilder();
		for (String current : adapter.getTags()) {
			tags.append("#").append(current);
		}
		Account a = AccountManager.getInstance(this).getAccount();
		if (a == null) {
			return;
		}
		String token = a.getToken();
		String titleContent = title.getText().toString();
		String introContent = intro.getText().toString();
		final String uri = APIManager.CreatorURI.getSubmitVideoURI(token, titleContent, introContent, videoCopyright,
				videoCategory, tags.toString());
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("确定发布视频？");
		builder.setMessage("请检查填写的信息是否有疏漏或是否违反了相关规定");
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				uploader(uri);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	public void saveVideo(View v) {
		final String destDir = StringUtils.strCat(DeviceUtils.getAndroidSDK() >= Build.VERSION_CODES.R
				? getExternalFilesDir(null).toString()
				: "/sdcard/OTTOHub", "/draft/");
		if (!new File(destDir).exists()) {
			FileUtils.createDir(destDir);
		}
		final EditText name = new EditText(this);
		name.setHint("自定义草稿名称，如config.json，如不填写将使用默认名称");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("确定保存草稿？");
		builder.setMessage("可通过发布界面按钮加载任意草稿文件");
		builder.setView(name);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				try {
					String customName = name.getText().toString();
					String randomName = customName.isEmpty()
							? StringUtils.strCat(new Object[]{"OTTOHub_draft-",
									SystemUtils.getDate("yyyy-MM-dd-HH-mm-ss-"), SystemUtils.getTime(), ".json"})
							: customName;
					String realDir = StringUtils.strCat(destDir, randomName);
					String content = createDraftJSON();
					final File dir = new File(realDir);
					if (!dir.exists()) {
						FileUtils.createFile(getApplication(), realDir, content);
						return;
					}
					FileUtils.writeFile(getApplication(), realDir, content);
					Toast.makeText(getApplication(), "保存成功", Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Log.e("UploadVideoActivity", e.toString());
					Toast.makeText(getApplication(), "保存失败，请联系作者或查看本地文件解决", Toast.LENGTH_SHORT).show();
				}
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	public void loadVideo(View v) {
		final Dialog cateDia = new Dialog(this);
		final String dir = StringUtils.strCat(DeviceUtils.getAndroidSDK() >= Build.VERSION_CODES.R
				? getExternalFilesDir(null).toString()
				: "/sdcard/OTTOHub", "/draft/");
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// TODO: Implement this method
				return name.endsWith(".json");
			}
		};
		List<String> data = new ArrayList<>();
		for (File f : FileUtils.listFile(dir, filter)) {
			data.add(f.getName());
		}
		ArrayAdapter<String> category = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
		ListView content = new ListView(this);
		content.setAdapter(category);
		content.setBackgroundResource(R.drawable.video_card_bg);
		content.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO: Implement this method
				try {
					String current = (String) parent.getItemAtPosition(position);
					String content = FileUtils.readFile(getApplication(), new File(dir, current).getPath());
					HashMap<String, Integer> typeMap = initCopyright();
					HashMap<String, Integer> categoryMap = initCategorys();
					JSONObject root = new JSONObject(content);
					title.setText(root.optString("title", "大家好啊，今天来点大家想看的东西啊"));
					intro.setText(root.optString("intro", "[填词时间]"));
					video = Uri.parse(root.optString("video_uri"));
					cover = Uri.parse(root.optString("cover_uri"));
					coverView.setImageURI(cover);
					text.setTextColor(Color.WHITE);
					text.setBackgroundResource(R.drawable.text_bg);
					videoCopyright = root.optInt("type", 0);
					for (Map.Entry<String, Integer> entry : typeMap.entrySet()) {
						if (entry.getValue() == videoCopyright) {
							typeBtn.setText(entry.getKey());
						}
					}
					videoCategory = root.optInt("category", APIManager.VideoURI.CATEGORY_OTHER);
					for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
						if (entry.getValue() == videoCategory) {
							categoryBtn.setText(entry.getKey());
						}
					}
					List<String> tagData = new ArrayList<>();
					JSONArray tags = root.optJSONArray("tags");
					for (int i = 0; i < tags.length(); i++) {
						tagData.add(tags.optString(i));
					}
					adapter.setTags(tagData, true);
					cateDia.dismiss();
				} catch (Exception e) {
					Log.e("UploadVideoActivity", e.toString());
					Toast.makeText(getApplication(), "加载失败，原因: " + e.getCause().toString(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		content.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO: Implement this method
				String current = (String) parent.getItemAtPosition(position);
				draftManagerDia(dir, current);
				return false;
			}
		});
		Toast.makeText(getApplication(), "点击加载，长按改名或删除", Toast.LENGTH_SHORT).show();
		cateDia.requestWindowFeature(1);
		cateDia.setContentView(content);
		ObjectAnimator ofFloat = ObjectAnimator.ofFloat(content, "translationY", 100.0f, 0.0f);
		ofFloat.setDuration(1000L);
		ofFloat.start();
		Window window = cateDia.getWindow();
		window.setFlags(4, 4);
		if (window != null) {
			window.setBackgroundDrawableResource(0x0106000d);
		}
		window.setGravity(80);
		WindowManager.LayoutParams attributes = window.getAttributes();
		attributes.y = 20;
		attributes.dimAmount = 0.0f;
		if (Build.VERSION.SDK_INT == 31) {
			attributes.setBlurBehindRadius(20);
		}
		window.setAttributes(attributes);
		cateDia.show();
	}

	public void quit(View v) {
		finish();
	}

	private static interface onUploaderCallback {
		void onSuccess(String reslutURI);
		void onFailed(String message);
	}

	private static class VideoUploader extends AsyncTask<Uri, Void, String> {
		private String URI;
		private Context ctx;
		private onUploaderCallback cmd;
		private int requestCode;

		public VideoUploader(Context c, String uri, onUploaderCallback callback) {
			ctx = c;
			URI = uri;
			cmd = callback;
		}

		@Override
		protected String doInBackground(Uri[] params) {
			// TODO: Implement this method
			File file = new File(getPath(ctx, params[0]));
			String boundary = UUID.randomUUID().toString();
			String CRLF = "\r\n";
			try {
				URL url = new URL(URI);
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
				InputStream cover = new FileInputStream(file);
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = cover.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				outputStream.flush();
				writer.append(CRLF).flush(); // End of file data
				// Send file data
				writer.append("--" + boundary).append(CRLF);
				writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + file.getName() + "\"")
						.append(CRLF);
				writer.append("Content-Type: video/" + getType(file)).append(CRLF);
				writer.append(CRLF).flush();
				InputStream inputStream = new FileInputStream(file);
				byte[] buffer2 = new byte[4096];
				int bytesRead2;
				while ((bytesRead2 = inputStream.read(buffer2)) != -1) {
					outputStream.write(buffer2, 0, bytesRead2);
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
			if (requestCode <= 0 || requestCode > 299) {
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
	}
}

