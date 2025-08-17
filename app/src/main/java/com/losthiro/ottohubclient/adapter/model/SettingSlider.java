/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter.model;

public class SettingSlider extends SettingBasic {
	public static final int TYPE_INT = 0;
	public static final int TYPE_FLOAT = 1;
	public static final int TYPE_RANGE = 2;
	public static final int TYPE_SLOT = 3;

	private int mType;
	private int intCurrent;
	private int maxValue;
	private int minValue;
	private int[] mValues;
	private float floatCurrent;
	private float maxFloat;
	private float minFloat;
	private OnValueChangeListener mListener;

	public SettingSlider(String title, int type) {
		super(title);
		mType = type;
	}

	public SettingSlider(String title, String text, int type) {
		super(title, text);
		mType = type;
	}

	public SettingSlider(String title, String text, int type, int min, int max) {
		super(title, text);
		maxValue = max;
		minValue = min;
		mType = type;
	}

	public SettingSlider(String title, String text, float min, float max) {
		super(title, text);
		maxFloat = max;
		minFloat = min;
		mType = TYPE_FLOAT;
	}

	public SettingSlider(String title, String text, int[] values) {
		super(title, text);
		mValues = values;
		mType = TYPE_SLOT;
	}

	public int getType() {
		return mType;
	}

	public int getMax() {
		return maxValue;
	}

	public int getMin() {
		return minValue;
	}

	public int getInt() {
		return intCurrent;
	}

	public float getFMax() {
		return maxFloat;
	}

	public float getFMin() {
		return minFloat;
	}

	public float getFloat() {
		return floatCurrent;
	}

	public int[] getRange() {
		return mValues;
	}

	public void setCurrent(int current) {
		intCurrent = current;
		if (mListener != null) {
			mListener.onIntChange(current);
		}
	}

	public void setCurrent(float current) {
		floatCurrent = current;
		if (mListener != null) {
			mListener.onFloatChange(current);
		}
	}

	public void setOnValueChngeListener(OnValueChangeListener listener) {
		mListener = listener;
	}

	public static interface OnValueChangeListener {
		void onIntChange(int value);
		void onFloatChange(float value);
	}
}

