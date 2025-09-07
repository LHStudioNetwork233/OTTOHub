/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter;
import android.app.*;
import android.content.*;
import android.os.Handler;
import android.os.Looper;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import androidx.recyclerview.widget.*;
import com.losthiro.ottohubclient.*;
import com.losthiro.ottohubclient.impl.*;
import com.losthiro.ottohubclient.utils.*;
import java.util.*;
import org.json.*;
import com.losthiro.ottohubclient.adapter.model.*;

public class ManagerAdapter extends RecyclerView.Adapter<ManagerAdapter.ViewHolder> {
	private Context main;
	private List<ManagerModel> data;

	public ManagerAdapter(Context ctx, List<ManagerModel> list) {
		main = ctx;
		data = new ArrayList<ManagerModel>(new HashSet<ManagerModel>(list));
	}

	@Override
	public int getItemViewType(int position) {
		// TODO: Implement this method
		return data.get(position).getType();
	}

	@Override
	public int getItemCount() {
		// TODO: Implement this method
		return data.size();
	}

	@Override
	public void onBindViewHolder(ManagerAdapter.ViewHolder vH, int p) {
		// TODO: Implement this method
		final ManagerModel current = data.get(p);
		if (current.getType() == ManagerModel.TYPE_BLOG) {
			ImageDownloader.loader(vH.image, current.getAvatar());
			final ClientString str = new ClientString(current.getContent());
			str.load(vH.content, true);
			vH.content.setOnClickListener(new OnClickListener() {
				private boolean isShow;

				@Override
				public void onClick(View v) {
					// TODO: Implement this method
					isShow = !isShow;
					str.load((TextView) v, isShow);
				}
			});
			vH.title.setText(current.getTitle());
			if (current.isDeleted()) {
				vH.info.setText(StringUtils.strCat(new Object[]{"[已删除] ", current.getLikeCount(), " - ",
						current.getFavoriteCount(), " - ", current.getViewCount(), " - ", current.getTime()}));
				return;
			}
			switch (current.getAuditStatus()) {
				case ManagerModel.STATUS_DEF :
					vH.info.setText(StringUtils.strCat("[审核中] ", current.getTime()));
					break;
				case ManagerModel.STATUS_APPROVE :
					vH.info.setText(StringUtils.strCat(new Object[]{"[已过审] ", current.getLikeCount(), " - ",
							current.getFavoriteCount(), " - ", current.getViewCount(), " - ", current.getTime()}));
					vH.share.setVisibility(View.VISIBLE);
					vH.share.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO: Implement this method
							Account a = AccountManager.getInstance(main).getAccount();
							if (a == null) {
								return;
							}
							Intent i = new Intent(Intent.ACTION_SEND);
							i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							i.setType("text/plain");
							i.putExtra(Intent.EXTRA_TEXT, "OTTOHub邀请你来看 " + a.getName() + " 发布的动态\n"
									+ "https://m.ottohub.cn/b/" + current.getBID());
							main.startActivity(Intent.createChooser(i, "share"));
						}
					});
					vH.root.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO: Implement this method
							Intent i = new Intent(main, BlogDetailActivity.class);
							i.putExtra("bid", current.getBID());
							Client.saveActivity(((Activity) main).getIntent());
							main.startActivity(i);
						}
					});
					break;
				case ManagerModel.STATUS_REJECT :
					vH.info.setText(StringUtils.strCat(new Object[]{"[未过审] ", current.getLikeCount(), " - ",
							current.getFavoriteCount(), " - ", current.getViewCount(), " - ", current.getTime()}));
					vH.appeal.setVisibility(View.VISIBLE);
					vH.appeal.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO: Implement this method
							appealDia(current);
						}
					});
					break;
			}
		} else {
            if (current.isLocal()) {
                vH.image.setImageBitmap(current.getCover(main));
                try {
                    JSONObject mainfest = current.getInfos(main);
                    final long vid = mainfest.optLong("vid", 0);
                    final String title = mainfest.optString("title", "大家好啊，今天来点大家想看的东西");
                    final String user = mainfest.optString("user_name", "棍母");
                    final String time = mainfest.optString("time", "2009-04-09 00:00:00");
                    final String view = mainfest.optString("view_count", "0播放");
                    final String like = mainfest.optString("like_count", "0获赞");
                    vH.title.setText(title);
                    vH.info.setText(StringUtils.strCat(new String[]{view, " ", like}));
                    vH.content.setText(time);
                    vH.username.setText(user);
                    vH.root.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO: Implement this method
                                Intent i = new Intent(main, PlayerActivity.class);
                                i.putExtra("root_path", current.getRootPath());
                                i.putExtra("vid", vid);
                                Intent save = ((Activity) main).getIntent();
                                Client.saveActivity(save);
                                main.startActivity(i);
                            }
                        });
                    vH.root.setOnLongClickListener(new OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                // TODO: Implement this method
                                localManagerDia(current);
                                return false;
                            }
                        });
                    vH.appeal.setVisibility(View.VISIBLE);
                    vH.appeal.setText("导出");
                    vH.appeal.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO: Implement this method
                                exportDia(current);
                            }
                        });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return;
            }
			ImageDownloader.loader(vH.image, current.getCover());
			vH.title.setText(current.getTitle());
			vH.info.setText(StringUtils.strCat(new String[]{current.getViewCount(), " ", current.getLikeCount()}));
			vH.content.setText(current.getTime());
			vH.root.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					// TODO: Implement this method
					deleteDia(current);
					return false;
				}
			});
			if (current.isDeleted()) {
				vH.username.setText("[该稿件已删除]");
				return;
			}
			switch (current.getAuditStatus()) {
				case ManagerModel.STATUS_DEF :
					vH.username.setText("[审核中]");
					break;
				case ManagerModel.STATUS_APPROVE :
					vH.username.setText("[已过审]");
					vH.share.setVisibility(View.VISIBLE);
					vH.share.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Account a = AccountManager.getInstance(main).getAccount();
							if (a == null) {
								return;
							}
							Intent i = new Intent(Intent.ACTION_SEND);
							i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							i.setType("text/plain");
							i.putExtra(Intent.EXTRA_TEXT, "OTTOHub邀请你来看 " + a.getName() + " 发布的视频\n"
									+ "https://m.ottohub.cn/v/" + current.getVID());
							main.startActivity(Intent.createChooser(i, "share"));
						}
					});
					vH.root.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO: Implement this method
							Intent i = new Intent(main, PlayerActivity.class);
							i.putExtra("vid", current.getVID());
							Intent save = ((Activity) main).getIntent();
							Client.saveActivity(save);
							main.startActivity(i);
						}
					});
					break;
				case ManagerModel.STATUS_REJECT :
					vH.username.setText("[稿件被退回]");
					vH.appeal.setVisibility(View.VISIBLE);
					vH.appeal.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO: Implement this method
							appealDia(current);
						}
					});
					break;
			}
		}
	}

	@Override
	public ManagerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int p) {
		// TODO: Implement this method
		int res = 0;
		if (p == ManagerModel.TYPE_BLOG) {
			res = R.layout.list_blog_manager;
		} else {
			res = R.layout.list_video_manager;
		}
		return new ViewHolder(LayoutInflater.from(main).inflate(res, viewGroup, false), p);
	}

	private void appealDia(final ManagerModel current) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(main);
		dialog.setTitle("确定申诉？");
		dialog.setMessage(StringUtils.strCat(new Object[]{"确定申诉",
				current.getType() == ManagerModel.TYPE_BLOG ? "动态(BID: " : "视频(VID: ",
				current.getType() == ManagerModel.TYPE_BLOG ? current.getBID() : current.getVID(), ")吗？请仔细检查稿件是否违规"}));
		dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				appeal(current);
				dia.dismiss();
			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.create().show();
	}

	private void appeal(final ManagerModel current) {
		Account account = AccountManager.getInstance(main).getAccount();
		if (account == null) {
			return;
		}
		String token = account.getToken();
		String uri = current.getType() == ManagerModel.TYPE_BLOG
				? APIManager.ManageURI.getAppealBlogURI(current.getBID(), token)
				: APIManager.ManageURI.getAppealVideoURI(current.getVID(), token);
		final Handler h = new Handler(Looper.getMainLooper());
		NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(String content) {
				// TODO: Implement this method
				if (content == null) {
					onFailed("content==null");
					return;
				}
				try {
					JSONObject json = new JSONObject(content);
					if (json.optString("status", "error").equals("success")) {
						h.post(new Runnable() {
							@Override
							public void run() {
								// TODO: Implement this method
								Toast.makeText(main,
										StringUtils.strCat(current.getType() == ManagerModel.TYPE_BLOG
												? new Object[]{"已申诉动态 BID:", current.getBID(), " 请等待审核员私信回复"}
												: new Object[]{"已申诉视频 VID:", current.getVID(), " 请等待审核员私信回复"}),
										Toast.LENGTH_SHORT).show();
							}
						});
						return;
					}
					onFailed(json.optString("messgae"));
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

	private void deleteDia(final ManagerModel current) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(main);
		dialog.setTitle("确定删除？");
		dialog.setMessage(StringUtils.strCat(new Object[]{"确定删除",
				current.getType() == ManagerModel.TYPE_BLOG ? "动态(BID: " : "视频(VID: ",
				current.getType() == ManagerModel.TYPE_BLOG ? current.getBID() : current.getVID(), ")吗？你将会失去他很长时间"}));
		dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				delete(current);
				dia.dismiss();
			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.create().show();
	}

	private void delete(final ManagerModel current) {
		Account account = AccountManager.getInstance(main).getAccount();
		if (account == null) {
			return;
		}
		String token = account.getToken();
		String uri = current.getType() == ManagerModel.TYPE_BLOG
				? APIManager.ManageURI.getDeleteBlogURI(current.getBID(), token)
				: APIManager.ManageURI.getDeleteVideoURI(current.getVID(), token);
		final Handler h = new Handler(Looper.getMainLooper());
		NetworkUtils.getNetwork.getNetworkJson(uri, new NetworkUtils.HTTPCallback() {
			@Override
			public void onSuccess(String content) {
				// TODO: Implement this method
				if (content == null) {
					onFailed("content==null");
					return;
				}
				try {
					JSONObject json = new JSONObject(content);
					if (json.optString("status", "error").equals("success")) {
						h.post(new Runnable() {
							@Override
							public void run() {
								// TODO: Implement this method
								Toast.makeText(main,
										StringUtils.strCat(current.getType() == ManagerModel.TYPE_BLOG
												? new Object[]{"成功删除动态 BID:", current.getBID()}
												: new Object[]{"成功删除视频 VID:", current.getVID()}),
										Toast.LENGTH_SHORT).show();
								data.remove(current);
								notifyDataSetChanged();
							}
						});
						return;
					}
					onFailed(json.optString("messgae"));
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

	private void exportDia(final ManagerModel current) {
		final EditText text = new EditText(main);
		text.setHint("请输入外部存储路径，如 /sdcard/OTTOHub/test/share.zip");
		AlertDialog.Builder dialog = new AlertDialog.Builder(main);
		dialog.setTitle("确定导出？");
		dialog.setMessage(StringUtils.strCat("是否导出视频文件？本地存储目录: ", current.getRootPath()));
		dialog.setView(text);
		dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				String destPath = text.getText().toString();
				if (destPath.isEmpty()) {
					Toast.makeText(main, "你没有输入任何内容，请输入正确的输出路径", Toast.LENGTH_SHORT).show();
					return;
				}
				Toast.makeText(main, "导出成功", Toast.LENGTH_SHORT).show();
				FileUtils.ZIPUtils.zipFile(main, current.getRootPath(), destPath);
				dia.dismiss();
			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.create().show();
	}

	private void localManagerDia(final ManagerModel current) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(main);
		dialog.setTitle("确定删除？");
		dialog.setMessage(StringUtils.strCat("是否删除本地视频？本地存储目录: ", current.getRootPath()));
		dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dia, int which) {
				if (FileUtils.deleteDir(current.getRootPath()) && data.remove(current)) {
					Toast.makeText(main, "删除成功", Toast.LENGTH_SHORT).show();
					notifyDataSetChanged();
				}
				dia.dismiss();
			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.create().show();
	}

	public void setData(List<ManagerModel> newData) {
		data.clear();
		data.addAll(newData);
		notifyDataSetChanged();
	}

	public void addNewData(List<ManagerModel> newData) {
		if (data.containsAll(newData)) {
			return;
		}
		data.addAll(newData);
		notifyItemRangeInserted(data.size() - newData.size(), newData.size());
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View root;
		public ImageView image;
		public TextView title;
		public TextView info;
		public TextView content;
		public TextView username;
		public Button share;
		public Button appeal;

		public ViewHolder(View v, int type) {
			super(v);
			root = v;
			if (type == ManagerModel.TYPE_BLOG) {
				image = v.findViewById(R.id.user_avatar);
				title = v.findViewById(R.id.blog_title);
				info = v.findViewById(R.id.blog_info);
				content = v.findViewById(R.id.blog_content);
				share = v.findViewById(R.id.blog_share_current);
				appeal = v.findViewById(R.id.blog_appeal_current);
			} else {
				image = v.findViewById(R.id.video_cover);
				title = v.findViewById(R.id.video_title);
				info = v.findViewById(R.id.video_detail_info);
				content = v.findViewById(R.id.video_detail_time);
				username = v.findViewById(R.id.video_user);
				share = v.findViewById(R.id.video_share_current);
				appeal = v.findViewById(R.id.video_appeal_current);
			}
		}
	}
}

