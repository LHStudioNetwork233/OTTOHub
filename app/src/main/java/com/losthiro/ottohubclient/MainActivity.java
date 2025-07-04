package com.losthiro.ottohubclient;

import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import com.losthiro.ottohubclient.impl.TypeManager;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.lang.ref.WeakReference;
import com.losthiro.ottohubclient.impl.UploadManager;

public class MainActivity extends AppCompatActivity {
    public static final int LOGIN_REQUEST_CODE=114;
    public static final int IMAGE_REQUEST_CODE=514;
    public static final int VIDEO_REQUEST_CODE=1919;
    public static final int AVATAR_REQUEST_CODE=810;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Main", getClass().getName() + "has create");
    }
}
