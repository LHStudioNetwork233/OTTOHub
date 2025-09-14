package com.losthiro.ottohubclient.adapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.losthiro.ottohubclient.Client;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.SearchActivity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * @Author Hiro
 * @Date 2025/05/23 07:10
 */
public class HonourAdapter extends RecyclerView.Adapter<HonourAdapter.ViewHolder> {
    public static final String TAG = "HonourAdapter";
    private Context main;
    private boolean isUsing;
    private boolean isHidden=true;
    private List<String> data;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View root;
        public TextView content;

        public ViewHolder(View v) {
            super(v);
            root = v;
            content = v.findViewById(R.id.honour_text);
        }
    }

    public HonourAdapter(Context c, List<String> list) {
        main = c;
        data = new ArrayList<String>(new HashSet<String>(list));
        data.sort(new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(final HonourAdapter.ViewHolder vH, int p) {
        String current = data.get(p);
        if (current.isEmpty()) {
            vH.root.setVisibility(View.GONE);
            return;
        }
        if (current.equals("吉吉国民") && isHidden) {
            vH.root.setVisibility(View.GONE);
            return;
        }
        vH.content.setText(current);
        if (isUsing) {
            vH.root.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Intent i=new Intent(main, SearchActivity.class);
                        i.putExtra("query", vH.content.getText().toString());
                        main.startActivity(i);
                    }
                });
        }
    }

    @Override
    public HonourAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int p) {
        return new ViewHolder(LayoutInflater.from(main).inflate(R.layout.list_honour, viewGroup, false));
    }

    public void setUsingSearch(boolean action) {
        isUsing = action;
    }

    public void setHiddenDef(boolean hidden) {
        isHidden = hidden;
    }
}
