/**
 * @Author Hiro
 * @Date 2025/09/11 17:14
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.ui;
import androidx.fragment.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.losthiro.ottohubclient.R;
import android.text.*;
import com.losthiro.ottohubclient.utils.*;
import java.io.*;

public class LimitEditFragment extends Fragment implements TextWatcher {
	public static final String TAG = "LimitEditText";
	private TextWatcher mListener;
	private EditText mEdit;
	private TextView mLengthView;

	public static LimitEditFragment newInstance(int max, String hint) {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		arg.putString("hint", hint);
		arg.putInt("max", max);
		LimitEditFragment editView = new LimitEditFragment();
		editView.setArguments(arg);
		return editView;
	}
    
    public static LimitEditFragment newInstance(int max) {
        Bundle arg = new Bundle();
        arg.putString("tag", TAG);
        arg.putInt("max", max);
        LimitEditFragment editView = new LimitEditFragment();
        editView.setArguments(arg);
        return editView;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: Implement this method
		View root = inflater.inflate(R.layout.view_limit_edit, container, false);
		mEdit = root.findViewById(R.id.limit_edit_text);
		mEdit.addTextChangedListener(this);
		mLengthView = root.findViewById(R.id.edit_length_text);
		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onViewCreated(view, savedInstanceState);
		Bundle arg = getArguments();
		if (arg == null) {
			return;
		}
		int max = arg.getInt("max");
		InputFilter[] filters = {new InputFilter.LengthFilter(max)};
		mEdit.setFilters(filters);
        mEdit.setHint(arg.getString("hint", new String()));
		mLengthView.setText(StringUtils.strCat(new Object[]{mEdit.getText().length(), File.separator, max}));
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO: Implement this method
		if (mListener != null) {
			mListener.onTextChanged(s, start, before, count);
		}
		Bundle arg = getArguments();
		if (arg == null) {
			return;
		}
		int max = arg.getInt("max");
		int length = mEdit.getText().length();
		mLengthView.setText(StringUtils.strCat(new Object[]{length, File.separator, max}));
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// TODO: Implement this method
		if (mListener != null) {
			mListener.beforeTextChanged(s, start, count, after);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO: Implement this method
		if (mListener != null) {
			mListener.afterTextChanged(s);
		}
	}

	public void setOnTextChangeListener(TextWatcher listener) {
		mListener = listener;
	}

	public void setHint(String text) {
		mEdit.setHint(text);
		Bundle arg = getArguments();
		if (arg != null) {
			arg.putString("hint", text);
		}
	}

	public void setHint(int id) {
		mEdit.setHint(id);
		Bundle arg = getArguments();
		if (arg != null) {
			arg.putString("hint", getContext().getString(id));
		}
	}

	public void setText(String content) {
		mEdit.setText(content);
	}

	public String getText() {
		return mEdit.getText().toString();
	}
}

