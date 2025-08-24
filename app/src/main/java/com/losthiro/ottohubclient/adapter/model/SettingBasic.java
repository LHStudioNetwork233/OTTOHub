/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter.model;
import android.graphics.drawable.*;
import com.losthiro.ottohubclient.utils.*;

public class SettingBasic {
	public static final int TYPE_ACTION = 0;
	public static final int TYPE_TOGGLE = 1;
	public static final int TYPE_SLIDER = 2;
	public static final int TYPE_COLOR = 3;
	public static final int TYPE_EDITTEXT = 4;
	public static final int TYPE_TITLE = 5;

	private int mID;
	private String mTitle;
	private String mText;
	private String mTag;
	private Drawable mIcon;

	public SettingBasic(String title) {
		mTitle = title;
		mText = new String();
	}

	public SettingBasic(String title, String text) {
		mTitle = title;
		mText = text;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getText() {
		return mText;
	}

	public String getTag() {
		return mTag;
	}

	public Drawable getIcon() {
		return mIcon;
	}

	public int getIconID() {
		return mID;
	}

	public void setTag(String tag) {
		mTag = tag;
	}

	public void setIcon(Drawable icon) {
		mIcon = icon;
	}

	public void setIcon(int id) {
		mID = id;
	}
}

