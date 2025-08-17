/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter.page;
import androidx.fragment.app.*;
import java.util.*;

public class PagesAdapter extends FragmentPagerAdapter {
    private List<Fragment> data;

    public PagesAdapter(FragmentManager manager, List<Fragment> pages){
        super(manager);
        data = pages;
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
    
    public Fragment getItem(String tag){
        for(Fragment current: data){
            if(current.getArguments().getString("tag").equals(tag)){
                return current;
            }
        }
        return null;
    }
    
    public void addPage(Fragment page){
        boolean isAdd = false;
        for(Fragment current: data) {
            if(!current.equals(page)&&!current.getArguments().getString("tag").equals(page.getArguments().getString("tag"))){
                isAdd = true;
            }
        }
        if(isAdd){
            data.add(page);
            notifyDataSetChanged();
        }
    }
}
