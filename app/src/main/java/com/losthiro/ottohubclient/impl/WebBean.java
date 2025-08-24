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

/**
 * @Author Hiro
 * @Date 2025/06/18 12:17
 */
public class WebBean {
	public static final String TAG = "WebBean";
	private Context ctx;
	private String content;
	private String css = "file:///android_asset/css/main_layout.css";
	private boolean usingCSS = true;
	private List<String> scripts = new ArrayList<>();

	public WebBean(Context c, String str) {
		ctx = c;
		content = str;
	}

	public void loadHTML(WebView view) {
		if ((content.startsWith("<!DOCTYPE html>") || content.startsWith("<html>")) && content.endsWith("</html>")) {
			view.loadData(content, "text/html;charset=UTF-8", "UTF-8");
			return;
		}
		StringBuilder htmlBulider = new StringBuilder(
				"<!DOCTYPE html><html><head><title>Hiro Loading...</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		MutableDataSet options = new MutableDataSet();
		options.set(Parser.EXTENSIONS,
				Arrays.asList(TablesExtension.create(), StrikethroughExtension.create(), TaskListExtension.create(),
						FootnoteExtension.create(), AutolinkExtension.create(), TypographicExtension.create(),
						AnchorLinkExtension.create(), TocExtension.create(), AbbreviationExtension.create(),
						WikiLinkExtension.create(), AttributesExtension.create(), GitLabExtension.create()));
		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).build();
		String text = content;
		try {
            if(!ClientSettings.getInstance().getBoolean(ClientSettings.SettingPool.MSG_MARKDOWN_SURPPORT)){
                throw new Exception();
            }
			text = renderer.render(parser.parse(content));
			text = text.replace("\n", "<br/>");
			//Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Log.i(TAG, e.toString());
			//            text = text.replace("&amp;", "&");
			//            text = text.replace("&lt;", "<");
			//            text = text.replace("&gt;", ">");
			//            text = text.replace("&quot;", "\"");
			//            text = text.replace("&#39;", "'");
			//Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
			text = replaceText(text);
		}
		//text = replaceAt(text);
		text = replaceLinks(text);
		if (!scripts.isEmpty()) {
			for (String js : scripts) {
                htmlBulider.append("<script src=\"").append(js).append("\"/>");
            }
		}
		if (usingCSS) {
			htmlBulider.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
			htmlBulider.append(css);
			htmlBulider.append("?v1.0.1\"/>");
		}
		htmlBulider.append("</head><body>");
		htmlBulider.append(text);
		htmlBulider.append("</body></html>");
		//Toast.makeText(ctx, htmlBulider, Toast.LENGTH_SHORT).show();
		view.loadData(htmlBulider.toString(), "text/html;charset=UTF-8", "UTF-8");
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
		Pattern pattern = Pattern.compile("@\\S+[ \n]");
		Matcher matcher = pattern.matcher(htmlContent);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String match = matcher.group();
			matcher.appendReplacement(sb,
					"<span class=\"colored-text\" style=\"color:" + getColor("e") + "\">" + match + "</span>");
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
}

