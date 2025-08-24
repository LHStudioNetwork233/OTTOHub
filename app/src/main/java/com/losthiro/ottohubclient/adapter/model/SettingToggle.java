/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter.model;
import java.util.*;

public class SettingToggle extends SettingBasic {
	private List<SettingBasic> childList;
	private OnToggleChangeListener command;
	private boolean hasChild;
	private boolean isToggle;

	public SettingToggle(String title) {
		super(title);
	}

	public SettingToggle(String title, String text) {
		super(title, text);
	}

	public SettingToggle(String title, String text, boolean defState) {
		super(title, text);
		isToggle = defState;
	}

	public SettingToggle(String title, String text, List<SettingBasic> child) {
		super(title, text);
		childList = child;
		hasChild = true;
	}

	public boolean isToggle() {
		return isToggle;
	}
    
    public boolean isGroup() {
        return hasChild;
    }

	public List<SettingBasic> getChildList() {
		return childList;
	}

	public void setToggle(boolean value) {
		isToggle = value;
		if (command != null) {
			command.onChange(value);
		}
	}

	public void switchToggle() {
		isToggle = !isToggle;
		if (command != null) {
			command.onChange(isToggle);
		}
	}

	public void setOnToggleChangeListener(OnToggleChangeListener listener) {
		command = listener;
	}

	public static interface OnToggleChangeListener {
		void onChange(boolean isToggle);
	}
}

