/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter.model;

public class SettingEdittext extends SettingBasic {
	private OnTextChangeListener mListener;
	private String mHint;
	private String mContent;
	private String mText;

	public SettingEdittext(String title) {
		super(title);
		mContent = new String();
	}

	public SettingEdittext(String title, String text) {
		super(title, text);
		mContent = new String();
	}

	public SettingEdittext(String title, String text, String hint) {
		super(title, text);
		mHint = hint;
		mContent = new String();
	}

	public SettingEdittext(String title, String text, String hint, String defText) {
		super(title, text);
		mHint = hint;
		mContent = defText;
	}

	public boolean isStatic() {
		return mListener == null;
	}

	public String getHint() {
		return mHint;
	}

	public String getContent() {
		return mContent;
	}

	public String getBtnText() {
		return mText;
	}

	public void setContent(String content) {
		mContent = content;
		if (!isStatic()) {
			mListener.onChange(content);
		}
	}

	public void setBtnText(String text) {
		mText = text;
	}

	public void setOnTextChangeListener(OnTextChangeListener listener) {
		mListener = listener;
	}

	public static interface OnTextChangeListener {
		void onChange(String newText);
	}
}

