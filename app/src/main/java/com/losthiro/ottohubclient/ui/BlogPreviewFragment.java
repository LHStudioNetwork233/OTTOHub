/**
 * @Author Hiro
 * @Date 2025/09/11 18:06
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.ui;
import androidx.fragment.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.losthiro.ottohubclient.view.*;
import com.losthiro.ottohubclient.R;

public class BlogPreviewFragment extends Fragment {
	public static final String TAG = "BlogPreview";
    private TextView mTitle;
    private ClientWebView mContent;

	public static BlogPreviewFragment newInstance(String title, String content) {
		Bundle arg = new Bundle();
		arg.putString("tag", TAG);
		arg.putString("title", title);
		arg.putString("content", content);
		BlogPreviewFragment previewPage = new BlogPreviewFragment();
		previewPage.setArguments(arg);
		return previewPage;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: Implement this method
        View root = inflater.inflate(R.layout.fragment_blog_preview, container, false);
        mTitle = root.findViewById(R.id.blog_title);
        mContent = root.findViewById(R.id.blog_content_view);
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
        mTitle.setText(arg.getString("title"));
        mContent.setTextData(arg.getString("content"));
        mContent.setFragmentManager(getChildFragmentManager());
        mContent.load();
	}
}

