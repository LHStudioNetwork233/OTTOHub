/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter;
import com.losthiro.ottohubclient.R;
import androidx.recyclerview.widget.*;
import android.view.*;

public class LoadingViewHolder extends RecyclerView.ViewHolder{
    public LoadingViewHolder(ViewGroup group){
        super(LayoutInflater.from(group.getContext()).inflate(R.layout.list_loading_char, group, false));
    }
}
