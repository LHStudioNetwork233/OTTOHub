package com.losthiro.ottohubclient.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import android.widget.Toast;
import android.os.Environment;
import android.os.StatFs;
import java.io.IOException;
import java.io.*;
import android.database.*;
import android.provider.*;
import android.os.*;
import com.losthiro.ottohubclient.*;

public class FileUtils {
	private static final String TAG = "FileUtils";

	public static boolean isExternalStorageMounted() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	public static boolean isStorageAvailable() {
		File file = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(file.getPath());
		long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
		return bytesAvailable > 0;
	}

	public static String getStorage(Context ctx, String path) {
		String oldStorage = BasicActivity.getCurrentStorage();
		String newStorage = ctx.getExternalFilesDir(null).getPath();
		String realStorage = DeviceUtils.getAndroidSDK() >= Build.VERSION_CODES.R ? newStorage : oldStorage;
		if (path == null) {
			return realStorage;
		}
		return new File(realStorage, path).getPath();
	}

	public static File getFile(Context ctx, Uri uri) {
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
		return new File(path);
	}

	public static String getSize(File f) {
		// TODO: Implement this method
		return formatSize(f.length());
	}

	public static String formatSize(double size) {
		// TODO: Implement this method
		final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
		int unitIndex = 0;
		if (size <= 0) {
			return "0B";
		}
		while (size >= 1024 && unitIndex < units.length - 1) {
			size /= 1024;
			unitIndex++;
		}
		return String.format("%.2f %s", size, units[unitIndex]);
	}

	public static boolean createDir(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
			return true;
		}
		return false;
	}

	public static boolean createFile(Context ctx, String path, String defaultContent) {
		File f = new File(path);
		if (f.exists() || !isExternalStorageMounted()) {
			return false;
		}
		if (!f.getParentFile().exists()) {
			createDir(StringUtils.strCat(f.getParent(), File.separator));
		}
		try {
			boolean isSuccess = f.createNewFile();
			if (isSuccess && !defaultContent.isEmpty()) {
				writeFile(ctx, path, defaultContent);
			}
			return isSuccess;
		} catch (Exception e) {
			Log.e(TAG, " create File " + path + " ERROR: ", e);
		}
		return false;
	}
    
    public static boolean createFile(String path, byte[] defaultContent) {
        File f = new File(path);
        if (f.exists() || !isExternalStorageMounted()) {
            return false;
        }
        if (!f.getParentFile().exists()) {
            createDir(StringUtils.strCat(f.getParent(), File.separator));
        }
        try {
            boolean isSuccess = f.createNewFile();
            if (isSuccess) {
                writeFile(path, defaultContent);
            }
            return isSuccess;
        } catch (Exception e) {
            Log.e(TAG, " create File " + path + " ERROR: ", e);
        }
        return false;
	}

	public static boolean deleteFile(String file) {
		File f = new File(file);
		if (f.exists() && isExternalStorageMounted()) {
			return f.isDirectory() ? deleteDir(file) : f.delete();
		}
		return false;
	}

	public static boolean deleteDir(String path) {
		File f = new File(path);
		if (f.exists() && f.isDirectory()) {
			File[] list = f.listFiles();
			if (list != null) {
				for (File sub : list) {
					sub.delete();
				}
				return f.delete();
			} else {
				return f.delete();
			}
		}
		return false;
	}

	public static boolean clearDir(String path) {
		File f = new File(path);
		List<Boolean> status = new ArrayList<>();
		if (f.exists() && f.isDirectory()) {
			for (File sub : f.listFiles()) {
				status.add(sub.delete());
			}
		}
		return !status.contains(false);
	}

	public static void clearDir(String path, FileFilter filter) {
		File f = new File(path);
		if (f.exists() && f.isDirectory()) {
			for (File sub : f.listFiles(filter)) {
				sub.delete();
			}
		}
	}

	public static void clearDir(String path, FilenameFilter filter) {
		File f = new File(path);
		if (f.exists() && f.isDirectory()) {
			for (File sub : f.listFiles(filter)) {
				sub.delete();
			}
		}
	}

	public static boolean renameFile(String path, String name) {
		File old = new File(path);
		if (old.exists()) {
			File newFile = new File(path.substring(0, path.lastIndexOf(File.separator) + 1) + name);
			return old.renameTo(newFile);
		}
		return false;
	}

	public static void writeFile(Context c, String path, String content) {
		File f = new File(path);
		if (f.exists() && f.isFile() && f.canWrite()) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(f));
				writer.write(content);
				writer.close();
			} catch (Exception e) {
				Log.e(TAG, " write File " + path + " failed, ERROR: ", e);
			}
		}
	}

	public static void writeFile(String filePath, byte[] data) {
		if (data == null || filePath == null || filePath.isEmpty()) {
			throw new IllegalArgumentException("Data or filePath cannot be null/empty.");
		}
		File file = new File(filePath);
		File parentDir = file.getParentFile();
		if (!parentDir.exists()) {
			createDir(parentDir.toString());
		}
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
		} catch (IOException e) {
			Log.e(TAG, " write File failed, ERROR: ", e);
		}
	}

	public static String readFile(Context c, String path) {
		StringBuilder content = new StringBuilder();
		File f = new File(path);
		try {
			if (f.exists() && f.isFile() && f.canRead()) {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line;
				while ((line = reader.readLine()) != null) {
					content.append(line).append(System.lineSeparator());
				}
				reader.close();
			}
		} catch (Exception e) {
			Log.e(TAG, " read File " + path + " failed, ERROR: ", e);
		}
		return content.toString().trim();
	}

	public static byte[] readFile(Context context, Uri uri) throws IOException {
		InputStream inputStream = null;
		try {
			inputStream = context.getContentResolver().openInputStream(uri);
			ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];

			int len;
			while ((len = inputStream.read(buffer, 0, bufferSize)) > -1) {
				byteBuffer.write(buffer, 0, len);
			}
			return byteBuffer.toByteArray();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	public static String readFile(Context c, int id) {
		StringBuilder content = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(c.getResources().openRawResource(id)));
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append(System.lineSeparator());
			}
			reader.close();
		} catch (Exception e) {
			Log.e(TAG, "read File failed, ERROR: ", e);
		}
		return content.toString().trim();
	}

	public static void copyFile(Context c, String oldPath, String newPath) {
		try {
			File old = new File(oldPath);
			if (old.exists()) {
				FileInputStream fis = new FileInputStream(old);
				FileOutputStream fos = new FileOutputStream(new File(newPath));
				copyFile(fis, fos);
				fis.close();
				fos.close();
			}
		} catch (Exception e) {
			Log.e(TAG, " Copy " + oldPath + " to " + newPath + " failed, ERROR: ", e);
		}
	}

	public static void copyFile(InputStream old, OutputStream current) throws Exception {
		byte[] buffer = new byte[1145];
		int byteReader;
		while ((byteReader = old.read(buffer)) > 0) {
			current.write(buffer, 0, byteReader);
		}
	}

	public static void moveFile(Context c, String oldPath, String newPath) {
		copyFile(c, oldPath, newPath);
		deleteFile(oldPath);
	}

	public static List<File> listFile(String dir, FilenameFilter collectType) {
		List<File> list = new ArrayList<File>();
		File d = new File(dir);
		if (d.exists() && d.isDirectory()) {
			File[] files = d.listFiles(collectType);
			for (File f : files) {
				list.add(f);
			}
		}
		return list;
	}

	public static List<File> listFile(String dir) {
		List<File> list = new ArrayList<File>();
		File d = new File(dir);
		if (d.exists() && d.isDirectory()) {
			File[] files = d.listFiles();
			for (File f : files) {
				list.add(f);
			}
		}
		return list;
	}

	public static class ZIPUtils {
		private static final String TAG = "FileUtils/ZIPUtils";

		public static void zipFile(Context c, String srcDirPath, String zipFilePath) {
			File srcDir = new File(srcDirPath);
			try {
				if (srcDir.exists()) {
					FileOutputStream fos = new FileOutputStream(new File(zipFilePath));
					ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
					ZIPAllFilesInDir(c, srcDir, srcDir.getName(), zos, 0);
				}
			} catch (Exception e) {
				Log.e(TAG, " ZIP File " + srcDirPath + " failed, ERROR: ", e);
			}
		}

		private static void ZIPAllFilesInDir(Context c, File srcDir, String name, ZipOutputStream zos, int count) {
			if (count < 5 && srcDir.isDirectory()) {
				for (File f : srcDir.listFiles()) {
					String entry = name + File.separator + f.getName();
					if (f.isFile()) {
						try {
							BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
							ZipEntry zipEntry = new ZipEntry(entry);
							zos.putNextEntry(zipEntry);
							byte[] buffer = new byte[1145];
							int length;
							while ((length = bis.read(buffer)) > 0) {
								zos.write(buffer, 0, length);
							}
							zos.closeEntry();
						} catch (Exception e) {
							Log.e(TAG, " ZIP File write failed: ", e);
						}
					} else {
						ZIPAllFilesInDir(c, f, entry, zos, count + 1);
					}
				}
			}
		}

		public static void unzipFile(Context c, String zipFilePath, String destDirPath) {
			createDir(destDirPath);
			try {
				ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
				write(destDirPath, zis);
			} catch (Exception e) {
				Toast.makeText(c, e.toString(), Toast.LENGTH_SHORT).show();
				Log.e(TAG, " unZIP " + zipFilePath + " to " + destDirPath + " failed, ERROR: ", e);
			}
		}

		public static void unzipFile(Context c, Uri ZIPUri, String destDirPath) {
			createDir(destDirPath);
			try {
				ZipInputStream zis = new ZipInputStream(c.getContentResolver().openInputStream(ZIPUri));
				write(destDirPath, zis);
			} catch (Exception e) {
				Log.e(TAG, " unZIP " + ZIPUri + " to " + destDirPath + " failed, ERROR: ", e);
			}
		}

		private static void write(String dest, ZipInputStream zis) throws Exception {
			byte[] buffer = new byte[1024];
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				File file = new File(dest, zipEntry.getName());
                File parent = file.getParentFile();
                if (parent.exists() || parent.mkdirs()) {
                    if (!zipEntry.isDirectory() && file.createNewFile()) {
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                        bos.close();
                    }
                    zis.closeEntry();
                }
				zis.closeEntry();
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
            zis.close();
		}
	}

	public static class AssetUtils {
		private static final String TAG = "FileUtils/AssetUtils";

		public static Drawable getAssetsImage(Context c, String name) {
			try {
				return Drawable.createFromStream(ResourceUtils.getAssetsFile(name), null);
			} catch (Exception e) {
				Log.e(TAG, " open image error: ", e);
				return null;
			}
		}

		public static String readAssetsFile(Context c, String name) {
			StringBuilder content = new StringBuilder();
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceUtils.getAssetsFile(name)));
				String line;
				while ((line = reader.readLine()) != null) {
					content.append(line).append(System.lineSeparator());
				}
				reader.close();
			} catch (Exception e) {
				Log.e(TAG, " read Assets File " + name + " failed, ERROR: ", e);
			}
			return content.toString().trim();
		}

		public static void copyFileAssets(Context c, String name, String destDir) {
			try {
				InputStream is = ResourceUtils.getAssetsFile(name);
				FileOutputStream fos = new FileOutputStream(new File(destDir));
				byte[] buffer = new byte[1145];
				int bytesReader;
				while ((bytesReader = is.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesReader);
				}
				is.close();
				fos.close();
			} catch (Exception e) {
				Log.e(TAG, " assets copy failed: ", e);
			}
		}

		public static void unzipFileAssets(Context c, String name, String destDirPath) {
			File dir = new File(destDirPath);
			createDir(destDirPath);
			try {
				ZipInputStream zis = new ZipInputStream(ResourceUtils.getAssetsFile(name));
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
					File f = new File(dir, entry.getName());
					File d = f.getParentFile();
					if (!d.exists()) {
						d.mkdirs();
					}
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
					byte[] buffer = new byte[1145];
					int length;
					while ((length = zis.read(buffer)) > 0) {
						bos.write(buffer, 0, length);
					}
					zis.closeEntry();
				}
				zis.close();
			} catch (Exception e) {
				Log.e(TAG, " assets unZIP " + name + " to " + destDirPath + " failed, ERROR: ", e);
			}
		}
	}
}

