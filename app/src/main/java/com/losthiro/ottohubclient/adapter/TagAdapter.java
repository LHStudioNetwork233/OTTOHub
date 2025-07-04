/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter;
import androidx.recyclerview.widget.*;
import android.widget.*;
import android.view.*;
import java.util.*;
import com.losthiro.ottohubclient.R;
import android.content.*;
import android.view.View.*;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {
	private final List<String> data = new ArrayList<>();
	private Context main;
	private Comparator<String> action;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View root;
		public TextView content;

		public ViewHolder(View v) {
			super(v);
			root = v;
			content = v.findViewById(R.id.tag_content);
		}
	}

	public TagAdapter(Context c) {
		main = c;
		action = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		};
	}
    
	@Override
	public int getItemCount() {
		return data.size();
	}

	@Override
	public void onBindViewHolder(final TagAdapter.ViewHolder vH, int p) {
		final String current = data.get(p);
		vH.content.setText(current);
		vH.root.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Implement this method
				if (data.remove(current)) {
					data.sort(action);
                    notifyDataSetChanged();
				}
			}
		});
	}

	@Override
	public TagAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int p) {
		return new ViewHolder(LayoutInflater.from(main).inflate(R.layout.list_tag, viewGroup, false));
	}
    
    public void setTags(List<String> list, boolean isRefresh){
        if(isRefresh){
            data.clear();
        }
        data.addAll(list);
        notifyDataSetChanged();
    }

	public void addTag(String tag) {
		data.add(tag);
		data.sort(action);
		notifyDataSetChanged();
	}
    
    public void clearTags(){
        data.clear();
        notifyDataSetChanged();
    }
    
    public List<String> getTags(){
        return data;
    }
}

