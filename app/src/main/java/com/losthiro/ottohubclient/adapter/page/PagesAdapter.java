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

public class PagesAdapter extends FragmentPagerAdapter {
	private List<Fragment> data;
	private Context ctx;
    private boolean mCheckAccount;

	public PagesAdapter(FragmentActivity act, List<Fragment> pages) {
		super(act.getSupportFragmentManager());
		data = pages;
		ctx = act.getApplication();
	}
    
    public PagesAdapter(Fragment parent, List<Fragment> pages) {
        super(parent.getChildFragmentManager());
        data = pages;
        ctx = parent.getContext();
	}

	@Override
	public int getCount() {
		// TODO: Implement this method
        int offset = AccountManager.getInstance(ctx).isLogin() ? 0 : 1;
		return mCheckAccount ? data.size() - offset : data.size();
	}

	@Override
	public Fragment getItem(int p) {
		// TODO: Implement this method
		return data.get(p);
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
    
    public void setCheckAccount(boolean isCheck) {
        mCheckAccount = isCheck;
    }
}

