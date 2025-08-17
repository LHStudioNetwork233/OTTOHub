/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter.setting;
import androidx.recyclerview.widget.*;
import android.view.*;
import com.losthiro.ottohubclient.adapter.model.*;
import android.content.*;
import java.util.*;
import com.losthiro.ottohubclient.*;
import android.widget.*;
import com.losthiro.ottohubclient.utils.*;
import android.graphics.drawable.*;
import android.view.View.*;
import android.text.*;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
	private Context mContext;
	private List<SettingBasic> data;

	public SettingsAdapter(Context c, List<SettingBasic> settings) {
		mContext = c;
		data = new ArrayList<SettingBasic>(new LinkedHashSet<SettingBasic>(settings));
	}

	@Override
	public int getItemCount() {
		// TODO: Implement this method
		return data.size();
	}

	@Override
	public int getItemViewType(int position) {
		// TODO: Implement this method
		SettingBasic current = data.get(position);
		if (current instanceof SettingToggle) {
			return SettingBasic.TYPE_TOGGLE;
		}
		if (current instanceof SettingSlider) {
			return SettingBasic.TYPE_SLIDER;
		}
		if (current instanceof SettingColor) {
			return SettingBasic.TYPE_COLOR;
		}
		if (current instanceof SettingEdittext) {
			return SettingBasic.TYPE_EDITTEXT;
		}
		return SettingBasic.TYPE_ACTION;
	}

	@Override
	public void onBindViewHolder(SettingsAdapter.ViewHolder vH, int p) {
		// TODO: Implement this method
		int type = getItemViewType(p);
		final SettingBasic current = data.get(p);
		vH.title.setText(current.getTitle());
		vH.text.setText(current.getText());
		vH.icon.setColorFilter(ResourceUtils.getColor(R.color.colorSecondary));
		Drawable icon = current.getIcon();
		if (icon != null) {
			vH.icon.setImageDrawable(icon);
		}
		switch (type) {
			case SettingBasic.TYPE_ACTION :
				vH.content.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO: Implement this method
						((SettingAction) current).run();
					}
				});
				break;
			case SettingBasic.TYPE_TOGGLE :
				vH.toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						// TODO: Implement this method
						((SettingToggle) current).setToggle(isChecked);
					}
				});
				break;
			case SettingBasic.TYPE_SLIDER :
				break;
			case SettingBasic.TYPE_COLOR :
				break;
			case SettingBasic.TYPE_EDITTEXT :
				SettingEdittext edit = (SettingEdittext) current;
				vH.edit.setHint(edit.getHint());
				vH.edit.setText(edit.getContent());
				vH.edit.addTextChangedListener(new TextWatcher() {
					@Override
					public void afterTextChanged(Editable s) {
						// TODO: Implement this method
						((SettingEdittext) current).setContent(s.toString());
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
						// TODO: Implement this method
					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						// TODO: Implement this method
					}
				});
				break;
		}
	}

	@Override
	public SettingsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int p) {
		// TODO: Implement this method
		int id = R.layout.list_setting_action;
		switch (p) {
			case SettingBasic.TYPE_TOGGLE :
				id = R.layout.list_setting_toggle;
				break;
			//			case SettingBasic.TYPE_SLIDER :
			//				id = 0;
			//				break;
			//			case SettingBasic.TYPE_COLOR :
			//				id = 0;
			//				break;
			case SettingBasic.TYPE_EDITTEXT :
				id = R.layout.list_setting_edit;
				break;
		}
		return new ViewHolder(LayoutInflater.from(mContext).inflate(id, viewGroup, false), p);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		View content;
		ImageView icon;
		TextView title;
		TextView text;
		Switch toggle;
		EditText edit;

		public ViewHolder(View root, int type) {
			super(root);
			content = root;
			icon = root.findViewById(R.id.list_setting_icon);
			title = root.findViewById(R.id.list_setting_title);
			text = root.findViewById(R.id.list_setting_text);
			switch (type) {
				case SettingBasic.TYPE_TOGGLE :
					toggle = root.findViewById(R.id.list_setting_toggle);
					break;
				case SettingBasic.TYPE_EDITTEXT :
					edit = root.findViewById(R.id.list_setting_edit);
					break;
			}
		}
	}
}

