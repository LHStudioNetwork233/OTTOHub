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

	public String getHint() {
		return mHint;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(String content) {
		mContent = content;
		if (mListener != null) {
			mListener.onChange(content);
		}
	}

	public void setOnTextChangeListener(OnTextChangeListener listener) {
		mListener = listener;
	}

	public static interface OnTextChangeListener {
		void onChange(String newText);
	}
}

