/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter.model;

public class SettingColor extends SettingBasic {
	private OnColorChangeListener command;
	private int mColor;

	public SettingColor(String title) {
		super(title);
	}

	public SettingColor(String title, String text) {
		super(title, text);
	}

	public SettingColor(String title, String text, int defColor) {
		super(title, text);
		mColor = defColor;
	}

	public int getColor() {
		return mColor;
	}

	public void setColor(int color) {
		mColor = color;
		if (command != null) {
			command.onChange(color);
		}
	}

	public void setOnColorChangeListener(OnColorChangeListener listener) {
		command = listener;
	}

	public static interface OnColorChangeListener {
		void onChange(int color);
	}
}

