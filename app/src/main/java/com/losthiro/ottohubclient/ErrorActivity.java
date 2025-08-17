package com.losthiro.ottohubclient;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.losthiro.ottohubclient.utils.StringUtils;
import android.view.View;
import com.losthiro.ottohubclient.utils.SystemUtils;

public class ErrorActivity extends BasicActivity{
    public static final String TAG = "ErrorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        Intent activityIntent=getIntent();
        String error_name=StringUtils.strCat(getString(R.string.error_name), activityIntent.getStringExtra("error_name"));
        String error_text=StringUtils.strCat(getString(R.string.error_text), activityIntent.getStringExtra("error_text"));
        TextView text=findViewById(R.id.error_text);
        if (text == null) {
            Log.e(TAG, "error text not found");
            return;
        } else {
            text.setText(StringUtils.strCat(error_name, error_text));
        }
    }

    public void restart_client(View view){
        SystemUtils.restart(this);
    }
    
    public void exit_client(View view){
        SystemUtils.exit();
    }
}
