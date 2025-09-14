package com.losthiro.ottohubclient.impl;
import android.content.Context;
import android.webkit.WebView;
import android.widget.Toast;
import com.losthiro.ottohubclient.utils.ApplicationUtils;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.gitlab.GitLabExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.util.Log;
import android.content.res.*;
import com.losthiro.ottohubclient.utils.*;
import android.webkit.*;
import org.json.*;
import com.losthiro.ottohubclient.*;
import java.util.*;
import com.losthiro.ottohubclient.ui.*;
import androidx.fragment.app.*;
import com.losthiro.ottohubclient.view.*;

/**
 * @Author Hiro
 * @Date 2025/06/18 12:17
 */
 
public class WebBean {
	public static final String TAG = "WebBean";
	private Context ctx;
	private String content;
	private String css;
	private List<String> scripts = new ArrayList<>();
    private Parser parser;
    private HtmlRenderer renderer;

	public WebBean(Context c) {
		ctx = c;
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS,
                    Arrays.asList(TablesExtension.create(), StrikethroughExtension.create(), TaskListExtension.create(),
                                  FootnoteExtension.create(), AutolinkExtension.create(), TypographicExtension.create(),
                                  AnchorLinkExtension.create(), TocExtension.create(), AbbreviationExtension.create(),
                                  WikiLinkExtension.create(), AttributesExtension.create(), GitLabExtension.create()));
        parser = Parser.builder(options).build();
		renderer = HtmlRenderer.builder(options).build();
        
	}

	public void setData(String data) {
		content = data;
	}

	public void setCssURI(String uri) {
		css = uri;
	}

	public void addScript(String uri) {
		scripts.add(uri);
	}

	public void removeScript(String uri) {
		scripts.remove(uri);
	}

	private String replaceText(String text) {
		if (text.contains("\n")) {
			StringBuilder html = new StringBuilder();
			for (String line : text.split("\n")) {
				html.append("<p>").append(line).append("</p>").append("<br/>");
			}
			return html.toString();
		}
		return "<p>" + text + "</p>";
	}

	public static String getColor(String color) {
		switch (color) {
			case "a" :
				return "#73d858";
			case "b" :
				return "#52abd5";
			case "c" :
				return "#dc6c6b";
			case "d" :
				return "#b070c7";
			case "e" :
				return "#dad55d";
			case "f" :
				return "#ffffff";
			default :
				if (color.matches("-?\\d+(\\.\\d+)?")) {
					int i = Integer.parseInt(color);
					switch (i) {
						case 0 :
							return "#000000";
						case 1 :
							return "#2040a5";
						case 2 :
							return "#6ea06f";
						case 3 :
							return "#609ca6";
						case 4 :
							return "#931918";
						case 5 :
							return "#641483";
						case 6 :
							return "#e2ad67";
						case 7 :
							return "#c1c1c1";
						case 8 :
							return "#6d6d6d";
						case 9 :
							return "#6779a9";
					}
				}
				return "#ffffff";
		}
	}

	private String replaceAt(String htmlContent) {
		Pattern pattern = Pattern.compile("@.*\\s$");
		Matcher matcher = pattern.matcher(htmlContent);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String match = matcher.group();
			matcher.appendReplacement(sb,
					"<span class=\"at-text\">"/* style=\"color:" + getColor("e") + "\">"*/ + match + "</span>");
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private String replaceLinks(String htmlContent) {
		Pattern pattern = Pattern.compile("(?i)(ob|ov|ou|bid|vid|uid)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(htmlContent);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String match = matcher.group();
			String type = matcher.group(1).toLowerCase();
			String num = matcher.group(2);
			matcher.appendReplacement(sb,
					"<a href=\"https://m.ottohub.cn/" + getType(type) + num + "\">" + match + "</a>");
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private String getType(String type) {
		if (type.equals("ob") || type.equals("bid")) {
			return "b/";
		}
		if (type.equals("ov") || type.equals("vid")) {
			return "v/";
		}
		if (type.equals("ou") || type.equals("uid")) {
			return "u/";
		}
		return null;
	}
    
    @JavascriptInterface
    public void toast(String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

	@JavascriptInterface
	public String getColor(int type) {
        int color = ResourceUtils.getColor(R.color.colorMain);
		switch (type) {
			case 1 :
				color = ResourceUtils.getColor(R.color.colorSecondary);
                break;
			case 2 :
				color = ResourceUtils.getColor(R.color.colorAccent);
                break;
		}
        String rgba = StringUtils.convertToRGB(color).toLowerCase(Locale.getDefault());
        return StringUtils.strCat("#", rgba);
	}

	@JavascriptInterface
	public String getStyleFile() {
		return css;
	}

	@JavascriptInterface
	public String getScriptFiles() {
		JSONArray array = new JSONArray();
		for (String current : scripts) {
			array.put(current);
		}
		return array.toString();
	}

	@JavascriptInterface
	public String getData() {//将后端处理的数据传输到前端
		if (content == null) {
			return "loading";
		}
		String text = content;
		try {
			if (!ClientSettings.getInstance().getBoolean(ClientSettings.SettingPool.MSG_MARKDOWN_SURPPORT)) {
				throw new Exception();
			}
			text = renderer.render(parser.parse(content));
			text = text.replace("\n", "<br/>");
		} catch (Exception unuse) {
			text = replaceText(text);
		}
        text = replaceAt(replaceLinks(text));
		return text;
	}
    
    public static class ImageBridge{
        public static final String TAG = "ImageBridge";
        private FragmentManager mFragment;
        
        public ImageBridge(FragmentManager manager) {
            mFragment = manager;
        }
        
        @JavascriptInterface
        public boolean isHandle() {//是否启用拦截(待完善)
            return true;
        }
        
        @JavascriptInterface
        public void callImageViewer(String uri) {//点击图片唤起大图查看器
            if (mFragment == null) {
                return;
            }
            ImageViewerFragment.newInstance(uri).show(mFragment, uri);
        }
    }
    
    public static class VideoBridge {
        public static final String TAG = "VideoBridge";
        private ClientVideoView mVideoView;
        private WebView mContainer;
        private boolean isPrepared;
        
        public VideoBridge(WebView view) {
            mContainer = view;
        }
        
        //调用顺序
        //1. window.VideoBridge.initView(video, cover, title);
        //允许 cover == null 不会影响实际播放
        //2. window.VideoBridge.enabledWebSurrport(tag);
        //tag 是你实际要调用的JavaScriptInterface名
        //是的，我们支持自定义接口名了(最好不要和已有接口同名)
        
        //2. window.VideoBridge.enabledWebSurrportDef();
        //使用默认接口名(ClientVideoBridge)
        
        //添加完之后:
        //window./*你自己的接口名*/.addVideoViewToHTML(width, height, top, left);
        //width, height : 视图宽高
        //top : 顶部起始点
        //left : 左方起始点
        //可以自定义videoview的插入位置
        
        //该类会始终调用，但是如果不设置内容则不启用
        
        @JavascriptInterface
        public void enabledWebSurrport(String tag) {
            if (!isPrepared) {
                return;
            }
            mVideoView.setWebViewBridge(mContainer, tag);
        }
        
        @JavascriptInterface
        public void enabledWebSurrportDef() {
            if (!isPrepared) {
                return;
            }
            mVideoView.setWebViewBridge(mContainer);
        }
        
        @JavascriptInterface
        public void initView(String videoUri, String posterUri, String title) {
            if (videoUri == null) {
                return;
            }
            mVideoView = new ClientVideoView(mContainer.getContext());
            mVideoView.init(videoUri, posterUri, title);
            isPrepared = true;
        }
        
        @JavascriptInterface
        public boolean equalTag(String other) {
            if (other == null) {
                return false;
            }
            return TAG.equals(other);
        }
        
        @JavascriptInterface
        public boolean isPrepared() {
            return isPrepared;
        }
    }
}

