package com.losthiro.ottohubclient.adapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.losthiro.ottohubclient.AccountDetailActivity;
import com.losthiro.ottohubclient.BlogDetailActivity;
import com.losthiro.ottohubclient.Client;
import com.losthiro.ottohubclient.PlayerActivity;
import com.losthiro.ottohubclient.R;
import com.losthiro.ottohubclient.adapter.model.Comment;
import com.losthiro.ottohubclient.adapter.CommentAdapter;
import com.losthiro.ottohubclient.impl.ClientString;
import com.losthiro.ottohubclient.utils.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.losthiro.ottohubclient.impl.WebBean;
import com.losthiro.ottohubclient.view.ClientWebView;
import com.losthiro.ottohubclient.impl.*;
import android.util.*;
import com.losthiro.ottohubclient.utils.*;
import org.json.*;
import android.os.*;
import android.widget.*;
import com.losthiro.ottohubclient.view.dialog.*;
import com.losthiro.ottohubclient.adapter.model.*;

/**
 * @Author Hiro
 * @Date 2025/05/23 06:43
 */
public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	public static final String TAG = "CommentAdapter";
	private static final int STATUS_DEF = 0;
	private static final int STATUS_LOADING = 1;
	private String currentHint;
	private boolean isShowChild;
	private boolean isLoading = false;
	private Context main;
	private List<Comment> data;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View root;
		public ImageView userIcon;
		public Button report;
		public Button share;
		public Button delete;
		public TextView userName;
		public TextView commentInfo;
		public TextView childCommentList;
		public ClientWebView commentContent;
		public RecyclerView honourList;

		public ViewHolder(View v) {
			super(v);
			root = v;
			userIcon = v.findViewById(R.id.comment_user_avatar);
			userName = v.findViewById(R.id.comment_user_name);
			commentInfo = v.findViewById(R.id.comment_info_text);
			honourList = v.findViewById(R.id.comment_user_honour_list);
			commentContent = v.findViewById(R.id.comment_content);
			childCommentList = v.findViewById(R.id.child_comment_list);
			report = v.findViewById(R.id.comment_report);
			share = v.findViewById(R.id.comment_share);
			delete = v.findViewById(R.id.comment_delete);
		}
	}

	public CommentAdapter(Context ctx, List<Comment> list, boolean showChild) {
		isShowChild = showChild;
		main = ctx;
		data = new ArrayList<Comment>(new HashSet<Comment>(list));
		data.sort(new Comparator<Comment>() {
			@Override
			public int compare(Comment o1, Comment o2) {
				return Math.toIntExact(o1.getCID() - o2.getCID());
			}
		});
	}

	@Override
	public int getItemCount() {
		return data.size() + (isLoading ? 1 : 0);
	}

	@Override
	public int getItemViewType(int position) {
		// TODO: Implement this method
		return (position == data.size() && isLoading) ? STATUS_LOADING : STATUS_DEF;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int p) {
		if (holder instanceof ViewHolder) {
			final ViewHolder vH = (ViewHolder) holder;
			final Comment currect = data.get(p);
			currect.setAvatar(vH.userIcon);
			vH.userIcon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(main, AccountDetailActivity.class);
					i.putExtra("uid", currect.getUID());
					Intent save = ((Activity) main).getIntent();
					Client.saveActivity(save);
					main.startActivity(i);
				}
			});
			vH.userName.setText(currect.getUser());
			vH.commentInfo.setText(StringUtils.strCat(new Object[]{currect.getTime(),
					(currect.getType() == Comment.TYPE_VIDEO ? " OVC" : " OBC"), currect.getCID()}));
			vH.commentContent.loadTextData(currect.getContent());
			vH.root.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					for (Comment another : data) {
						another.setCurrent(false);
					}
					currect.setCurrent(true);
				}
			});
			vH.report.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog dialog = new AlertDialog.Builder(main).setTitle("确认举报？").setMessage(currect.getUser())
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dia, int which) {
									currect.reportComment();
								}
							}).setNegativeButton(android.R.string.cancel, null).create();
					dialog.show();
				}
			});
			vH.share.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String content = currect.getContent();
					if (content.length() > 10) {
						content = content.substring(0, 10) + "...";
					}
					String type = currect.getType() == Comment.TYPE_BLOG ? "b/" : "v/";
					Intent i = new Intent(Intent.ACTION_SEND);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_TEXT, "OTTOHub邀请你来看评论 " + currect.getUser() + " 说:" + content
							+ "\n原文地址: https://m.ottohub.cn/" + type + currect.getID());
					main.startActivity(Intent.createChooser(i, "share"));
				}
			});
			vH.delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteDia(currect);
				}
			});
			AccountManager manager = AccountManager.getInstance(main);
			if (manager.isLogin()) {
				Account current = manager.getAccount();
				if (current.getUID() == currect.getUID()) {
					vH.delete.setVisibility(View.GONE);
				}
			}
			String[] honours = currect.getHonours();
			if (honours.length > 1) {
				vH.honourList.setAdapter(new HonourAdapter(main, Arrays.<String>asList(honours)));
				vH.honourList.setLayoutManager(new LinearLayoutManager(main, LinearLayoutManager.HORIZONTAL, false));
			}
			if (currect.getChildCount() > 0 && isShowChild) {
				currect.loadChildComment(vH.childCommentList, new CallComment(main), currect.getType());
			}
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int p) {
		if (p == STATUS_LOADING) {
			return new LoadingViewHolder(viewGroup);
		}
		return new ViewHolder(LayoutInflater.from(main).inflate(R.layout.list_comment, viewGroup, false));
	}

	public boolean isCommentExists(Comment newComment) {
		for (Comment existingComment : data) {
			if (existingComment.equals(newComment)) {
				return true;
			}
		}
		return false;
	}

	private void deleteDia(final Comment current) {
		AlertDialog dialog = new AlertDialog.Builder(main).setTitle("确认删除？你将会失去它很长时间")
				.setMessage("CID" + current.getID())
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dia, int which) {
						deleteComment(current);
					}
				}).setNegativeButton(android.R.string.cancel, null).create();
		dialog.show();
	}

	private void deleteComment(final Comment current) {
		String token = AccountManager.getInstance(main).getAccount().getToken();
		String uri = current.getType() == Comment.TYPE_VIDEO
				? APIManager.CommentURI.getDeleteVideoURI(current.getCID(), token)
				: APIManager.CommentURI.getDeleteBlogURI(current.getCID(), token);
		final Handler h = new Handler(Looper.getMainLooper());
		NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(String content) {
				// TODO: Implement this method
				if (content == null) {
					onFailed("content = null");
					return;
				}
				try {
					JSONObject json = new JSONObject(content);
					if (json.optString("status", "error").equals("success")) {
						h.post(new Runnable() {
							@Override
							public void run() {
								// TODO: Implement this method
								if (data.remove(current)) {
									Toast.makeText(main, "删除成功", Toast.LENGTH_SHORT).show();
									notifyDataSetChanged();
								}
							}
						});
						return;
					}
					onFailed(json.optString("message"));
				} catch (Exception e) {
					onFailed(e.toString());
				}
			}

			@Override
			public void onFailed(String cause) {
				// TODO: Implement this method
				Log.e("Network", cause);
			}
		});
	}

	public void addNewData(List<Comment> newData) {
		data.addAll(newData);
		notifyItemRangeInserted(data.size() - newData.size(), newData.size());
	}
    
    public void setData(List<Comment> newData){
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }
    
    public void onBack(EditText commentEdit){
        commentEdit.setHint(currentHint);
        for (Comment current : data) {
            current.setCurrent(false);
		}
    }
    
    public void cacheHint(String hint){
        currentHint = hint;
    }

	public Comment getCurrent() {
		for (Comment current : data) {
			if (current.isCurrent()) {
				return current;
			}
		}
		return null;
	}
    
    public void startLoading() {
        if (isLoading) {
            return;
        }
        isLoading = true;
        notifyItemInserted(data.size());
    }

    public void stopLoading() {
        isLoading = false;
        notifyItemRemoved(data.size());
	}

	public static class CallComment {
		private Context ctx;

		public CallComment(Context c) {
			ctx = c;
		}

		public void showChildComment(List<Comment> childList, int count) {
			View inflate = LayoutInflater.from(ctx).inflate(R.layout.dialog_child_comment, null);
			BottomDialog commentDialog = new BottomDialog(ctx, inflate);
			((TextView) inflate.findViewWithTag("comment_count")).setText("总共" + count + "条评论");
			RecyclerView view = inflate.findViewWithTag("child_comment_list");
			view.setAdapter(new CommentAdapter(ctx, childList, false));
			view.setLayoutManager(new GridLayoutManager(ctx, 1));
			commentDialog.show();
		}
	}
}

