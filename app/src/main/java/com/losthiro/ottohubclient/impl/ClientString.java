package com.losthiro.ottohubclient.impl;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.text.Html;
import android.text.style.ClickableSpan;
import android.view.View;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * @Author Hiro
 * @Date 2025/05/26 02:24
 */
public class ClientString {
    public static final String TAG = "ClientString";
    private String oldData="";

    public ClientString(String content) {
        oldData = content;
    }

    private static String getIDColor(String content) {
        String regex = "(?i)(ov[0-9])|(uid[0-9])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        boolean find = false;
        while (matcher.find()) {
            result.append(content, lastEnd, matcher.start());
            result.append("§d").append(matcher.group()).append("§0");
            lastEnd = matcher.end();
            find = true;
        }
        result.append(content.substring(lastEnd));
        return find ? result.toString() : content;
    }

    private static String[] extractImageUrls(String htmlString) {
        Pattern pattern = Pattern.compile("<img src=\"(.*?)\"");
        Matcher matcher = pattern.matcher(htmlString);
        int start = 0;
        String[] imageUrls = new String[matcher.groupCount()];
        while (matcher.find()) {
            imageUrls[matcher.start() - start] = matcher.group(1);
            start = matcher.end();
        }
        return imageUrls;
    }

    public static SpannableString getColorText(String content) {
        List<ColorRange> colorRanges=new ArrayList<>();
        Pattern pattern=Pattern.compile("§([a-z0-9])");
        Matcher mach=pattern.matcher(content);
        StringBuilder processedString = new StringBuilder();
        int lastEnd = 0;
        while (mach.find()) {
            int start = mach.start();
            int end = mach.end();
            String color = mach.group(1);
            processedString.append(content, lastEnd, start);
            lastEnd = end;
            colorRanges.add(new ColorRange(processedString.length(), Color.parseColor(WebBean.getColor(color))));
        }
        processedString.append(content.substring(lastEnd));
        SpannableString newString = new SpannableString(processedString.toString());
        for (int i = 0; i < colorRanges.size(); i++) {
            ColorRange colorRange = colorRanges.get(i);
            Object what=colorRange.onClick == null ?new ForegroundColorSpan(colorRange.color): colorRange.onClick;
            newString.setSpan(what, colorRange.start, newString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return newString;
    }

    private String replaceAt(String content) {
        Pattern pattern = Pattern.compile("@\\S+[ \n]");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group();
            matcher.appendReplacement(sb, "§e" + match);
        }
        matcher.appendTail(sb);
        return sb.toString(); 
    }

    public void load(TextView view, boolean loadLength) {
        String newData=oldData;
        if (newData == null) {
            return;
        }
        if (newData.isEmpty()) {
            return;
        }
        if (newData.contains("@")) {
            newData = replaceAt(newData);
        }
        if (newData.length() > 64 && newData.length() - 64 > 8 && loadLength) {
            newData = StringUtils.strCat(newData.substring(0, 63), "\n§b...展开");
        }
        //newData = getIDColor(newData);
        view.setText(getColorText(newData));
    }

    public void colorTo(TextView view, int color) {
        Pattern pattern=Pattern.compile("\\d+");
        Matcher mach=pattern.matcher(oldData);
        SpannableString data = new SpannableString(oldData);
        while (mach.find()) {
            int start = mach.start();
            int end = mach.end();
            data.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            view.setText(data);
        }
    }

    public long findID(String type) {
        Pattern pattern=Pattern.compile(StringUtils.strCat(type, "(\\d+)"));
        Matcher mach=pattern.matcher(oldData);
        while (mach.find()) {
            return Long.parseLong(mach.group(mach.groupCount()));
        }
        return 0;
    }

    private static class ColorRange {
        int start;
        int color;
        ClickableSpan onClick;

        ColorRange(int start, int color) {
            this.start = start;
            this.color = color;
        }

        ColorRange(int start, int color, ClickableSpan onClick) {
            this.start = start;
            this.color = color;
            this.onClick = onClick;
        }
    }
}
