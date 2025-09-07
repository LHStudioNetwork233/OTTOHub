/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter.page;
import androidx.fragment.app.*;
import java.util.*;
import android.widget.*;
import android.os.*;
import android.content.*;
import com.losthiro.ottohubclient.impl.*;
import androidx.viewpager.widget.*;

public class PagesAdapter extends FragmentStatePagerAdapter {
	private final List<Fragment> data = new ArrayList<>();
	private Context ctx;

	public PagesAdapter(FragmentActivity act) {
		super(act.getSupportFragmentManager());
		ctx = act.getApplication();
	}
    
    public PagesAdapter(Fragment parent) {
        super(parent.getChildFragmentManager());
        ctx = parent.getContext();
	}

	@Override
	public int getCount() {
		// TODO: Implement this method
		return data.size();
	}

	@Override
	public Fragment getItem(int p) {
		// TODO: Implement this method
		return data.get(p);
	}

    @Override
    public int getItemPosition(Object object) {
        // TODO: Implement this method
        return PagerAdapter.POSITION_NONE;
    }

	public Fragment getItem(String tag) {
		for (Fragment current : data) {
			Bundle arg = current.getArguments();
			if (arg != null && arg.getString("tag").equals(tag)) {
				return current;
			}
		}
		return null;
	}
    
    public void addItem(Fragment current) {
        data.add(current);
        notifyDataSetChanged();
    }
}

