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

	public static void clearDir(String path) {
		File f = new File(path);
		if (f.exists() && f.isDirectory()) {
			for (File sub : f.listFiles()) {
				sub.delete();
			}
		}
	}

	public static void targetClearDir(String path, FilenameFilter filter) {
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
				byte[] buffer = new byte[1145];
				int byteReader;
				while ((byteReader = fis.read(buffer)) > 0) {
					fos.write(buffer, 0, byteReader);
				}
				fis.close();
				fos.close();
			}
		} catch (Exception e) {
			Log.e(TAG, " Copy " + oldPath + " to " + newPath + " failed, ERROR: ", e);
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
			File zip = new File(zipFilePath);
			File dir = new File(destDirPath);
			createDir(destDirPath);
			try {
				if (zip.isFile()) {
					ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
					ZipEntry entry;
					while ((entry = zis.getNextEntry()) != null) {
						File f = new File(dir, entry.getName());
						File d = f.getParentFile();
						if (!d.exists()) {
							d.mkdirs();
						}
						if (f.isFile()) {
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
							byte[] buffer = new byte[1145];
							int length;
							while ((length = zis.read(buffer)) > 0) {
								bos.write(buffer, 0, length);
							}
						}
						zis.closeEntry();
					}
					zis.close();
				}
			} catch (Exception e) {
				Log.e(TAG, " unZIP " + zipFilePath + " to " + destDirPath + " failed, ERROR: ", e);
			}
		}

		public static void unzipFile(Context c, Uri ZIPUri, String destDirPath) {
			File dir = new File(destDirPath);
			createDir(destDirPath);
			try {
				ZipInputStream zis = new ZipInputStream(c.getContentResolver().openInputStream(ZIPUri));
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
					File f = new File(dir, entry.getName());
					File d = f.getParentFile();
					if (!d.exists()) {
						d.mkdirs();
					}
					if (f.isFile()) {
						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
						byte[] buffer = new byte[1145];
						int length;
						while ((length = zis.read(buffer)) > 0) {
							bos.write(buffer, 0, length);
						}
					}
					zis.closeEntry();
				}
				zis.close();
			} catch (Exception e) {
				Log.e(TAG, " unZIP " + ZIPUri + " to " + destDirPath + " failed, ERROR: ", e);
			}
		}
	}

	public static class AssetUtils {
		private static final String TAG = "FileUtils/AssetUtils";

		public static Drawable getAssetsImage(Context c, String name) {
			try {
				return Drawable.createFromStream(ResourceUtils.getInstance(c).getAssetsFile(name), null);
			} catch (Exception e) {
				Log.e(TAG, " open image error: ", e);
				return null;
			}
		}

		public static String readAssetsFile(Context c, String name) {
			StringBuilder content = new StringBuilder();
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(ResourceUtils.getInstance(c).getAssetsFile(name)));
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
				InputStream is = ResourceUtils.getInstance(c).getAssetsFile(name);
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
				ZipInputStream zis = new ZipInputStream(ResourceUtils.getInstance(c).getAssetsFile(name));
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
					File f = new File(dir, entry.getName());
					File d = f.getParentFile();
					if (!d.exists()) {
						d.mkdirs();
					}
					if (f.isFile()) {
						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
						byte[] buffer = new byte[1145];
						int length;
						while ((length = zis.read(buffer)) > 0) {
							bos.write(buffer, 0, length);
						}
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

