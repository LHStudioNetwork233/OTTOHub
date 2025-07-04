package com.losthiro.ottohubclient.utils;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import android.security.keystore.KeyProperties;
import java.security.KeyPair;
import java.security.KeyStore;
import java.io.IOException;
import android.graphics.*;

public class StringUtils {
    public static final String TAG="StringUtils";

    public static int rng(int min, int max) {
        Random rng=new Random();
        return rng.nextInt(max) % (max - min + 1) + min;
    }

    public static int rng() {
        Random rng=new Random();
        return rng.nextInt();
    }

    public static String strToUnicode(String str) {
        StringBuffer sb = new StringBuffer();
        char[] c = str.toCharArray();
        for (char ch: c) {
            sb.append("\\u" + Integer.toHexString(ch));
        }
        return sb.toString();
    }
    
    public static String convertToRGB(int color) {
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));
        if (red.length() == 1) red = "0" + red;
        if (green.length() == 1) green = "0" + green;
        if (blue.length() == 1) blue = "0" + blue;
        return red + green + blue;
    }
    
    public static int convertToColorInt(String argb) throws IllegalArgumentException {
        if (argb.matches("[0-9a-fA-F]{1,6}")) {
            switch (argb.length()) {
                case 1:
                    return Color.parseColor("#00000" + argb);
                case 2:
                    return Color.parseColor("#0000" + argb);
                case 3:
                    char r = argb.charAt(0), g = argb.charAt(1), b = argb.charAt(2);
                    //noinspection StringBufferReplaceableByString
                    return Color.parseColor(new StringBuilder("#")
                                            .append(r).append(r)
                                            .append(g).append(g)
                                            .append(b).append(b)
                                            .toString());
                case 4:
                    return Color.parseColor("#00" + argb);
                case 5:
                    return Color.parseColor("#0" + argb);
                case 6:
                    return Color.parseColor("#" + argb);
            }
        }
        throw new IllegalArgumentException(argb + " is not a valid color.");
    }

    public static String strCat(String str1, String str2) {
        return str1 + str2;
    }

    public static String strCat(Object[] stringSrc) {
        StringBuilder sb=new StringBuilder();
        for (Object str: stringSrc) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static String toStr(Object obj) {
        return obj.toString();
    }

    public static String streamReader(InputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] bArr = new byte[10240];
            while (true) {
                int read = inputStream.read(bArr);
                if (read == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            }
        } catch (Exception e) {
            try {
                inputStream.close();
                byteArrayOutputStream.close();
            } catch (Exception e2) {
                Log.e(TAG, "read stream failed", e2);
            }
        }
        return byteArrayOutputStream.toString();
    }

    public static byte[] byteStreamReader(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            Log.e(TAG, "byte reader error", e);
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "release error", e);
            }
        }
        return outputStream.toByteArray();
    }

    public static byte[] encryptString(String password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyStore.Entry entry = keyStore.getEntry("user_key", null);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            if (entry instanceof KeyStore.PrivateKeyEntry) {
                cipher.init(Cipher.ENCRYPT_MODE, ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey());
                return cipher.doFinal(password.getBytes("UTF-8")); // 返回加密后的字节数组
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptString(byte[] encryptedPassword) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyStore.Entry entry = keyStore.getEntry("user_key", null);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            if (entry instanceof KeyStore.PrivateKeyEntry) {
                cipher.init(Cipher.DECRYPT_MODE, ((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
                return new String(cipher.doFinal(encryptedPassword), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
