/**
 * @Author Hiro
 * @Date 2025/09/10 22:04
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.ui;
import androidx.fragment.app.*;
import android.os.*;
import android.app.Dialog;
import android.view.*;
import com.losthiro.ottohubclient.view.dialog.*;
import com.losthiro.ottohubclient.*;
import androidx.recyclerview.widget.*;
import com.losthiro.ottohubclient.adapter.*;
import android.widget.*;
import com.losthiro.ottohubclient.adapter.model.*;
import java.util.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.utils.*;
import org.json.*;
import android.util.*;

public class CommentDialogFragment extends DialogFragment {
	public final static String TAG = "SubComment";
	private RecyclerView list;
	private TextView count;

	public static CommentDialogFragment newInstance(Comment parent, JSONArray data) {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		arg.putString("data", data.toString());
        arg.putString("user", parent.getUser());
		arg.putLong("id", parent.getID());
		arg.putLong("parent_id", parent.getCID());
		arg.putInt("type", parent.getType());
		CommentDialogFragment commentPage = new CommentDialogFragment();
		commentPage.setArguments(arg);
		return commentPage;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO: Implement this method
		return new BottomDialog(getContext(), null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: Implement this method
		View root = inflater.inflate(R.layout.dialog_child_comment, container, false);
		list = root.findViewById(R.id.comment_list);
		list.setLayoutManager(new GridLayoutManager(getContext(), 1));
		count = root.findViewWithTag("comment_count");
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
		long id = arg.getLong("id");
		long parent = arg.getLong("parent_id");
		int type = arg.getInt("type");
        String user = arg.getString("user");
		List<Comment> childList = new ArrayList<>();
		try {
			JSONArray data = new JSONArray(arg.getString("data"));
			for (int i = 0; i < data.length(); i++) {
				Comment current = new Comment(getContext(), data.optJSONObject(i), id, type);
				childList.add(current);
			}
		} catch (Exception e) {
		}
        CommentEditFragment edit = CommentEditFragment.newInstance(id, parent, type);
		FragmentTransaction tran = getChildFragmentManager().beginTransaction();
		tran.add(R.id.comment_edit_view, edit);
		tran.commit();
        edit.setHint("回复 @"+user+":");
		count.setText("总共" + childList.size() + "条评论");
		list.setAdapter(new CommentAdapter(getContext(), getChildFragmentManager(), childList, this));
	}
    
    public void show(FragmentManager manager) {
        show(manager, TAG);
    }
    
    public void changeCurrent(Comment current) {
        Fragment now = getChildFragmentManager().findFragmentById(R.id.comment_edit_view);
        if (now != null && now instanceof CommentEditFragment) {
            CommentEditFragment edit = (CommentEditFragment) now;
            edit.setHint("回复 @"+current.getUser()+":");
            Bundle arg = edit.getArguments();
            arg.putString("at", current.getUser());
        }
    }
}

