package com.losthiro.ottohubclient.view;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;
import com.losthiro.ottohubclient.impl.ClientString;
import com.losthiro.ottohubclient.utils.FileUtils;
import com.losthiro.ottohubclient.utils.SystemUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.os.Looper;
import android.text.Spanned;

/**
 * @Author Hiro
 * @Date 2025/06/06 15:37
 */
public class LogView extends TextView {
    public static final String TAG = "LogView";
    private final StringBuilder logContent=new StringBuilder();
    private final Handler main=new Handler(Looper.getMainLooper());
    private final Thread background;
    private boolean isRunning;

    public LogView(Context ctx) {
        super(ctx);
        background = new Thread(new Runnable(){
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            Process process = Runtime.getRuntime().exec("logcat -b all -v color");
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                synchronized (logContent) {
                                    logContent.append(line).append(System.lineSeparator());
                                }
                            }
                            bufferedReader.close();
                            process.destroy();
                            main.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        setText(parseLog(logContent.toString()));
                                        invalidate();
                                    }
                                });
                            Thread.sleep(1500L);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        setBackgroundColor(Color.GRAY);
        setAlpha(0.5f);
    }

    private int getColor(int tag) {
        // 根据颜色代码返回颜色值
        switch (tag) {
            case 40:
                return -8271996; // 深灰色
            case 75:
                return -10177034; // 浅蓝色
            case 166:
                return -30107; // 橙色
            case 196:
                return -1739917; // 红色
            default:
                return -1; // 未知颜色代码
        }
    }

    private Spannable parseLog(String log) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        Pattern pattern = Pattern.compile("\u001b\\[([0-9;]+)m");
        Matcher matcher = pattern.matcher(log);
        ForegroundColorSpan span = null;
        int lastEnd = 0;
        while (matcher.find()) {
            builder.append(log.subSequence(lastEnd, matcher.start()));
            String group = matcher.group(1);
            if (group.equals("0")) {
                for (Object obj : builder.getSpans(0, builder.length(), ForegroundColorSpan.class)) {
                    builder.removeSpan(obj);
                }
            } else if (group.startsWith("38;5;")) {
                try {
                    int tag = Integer.parseInt(group.substring(5));
                    int color = getColor(tag);
                    if (color != -1) {
                        span = new ForegroundColorSpan(color);
                        builder.setSpan(span, builder.length(), builder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid color tag: " + group);
                }
            } else {
                Log.e(TAG, "Unknown cmd: " + group);
            }
            lastEnd = matcher.end();
        }
        builder.append(log.subSequence(lastEnd, log.length()));
        return builder;
    }

    public void saveLog(String destPath) {
        File f=new File(destPath);
        if (!f.exists()) {
            FileUtils.createFile(getContext(), destPath, logContent.toString());
            return;
        }
        FileUtils.writeFile(getContext(), destPath, logContent.toString());
    }

    public void startLogging() {
        isRunning = true;
        background.start();
    }

    public void stopLogging() {
        if (isRunning) {
            isRunning = false;
            background.interrupt();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopLogging();
    }
}
