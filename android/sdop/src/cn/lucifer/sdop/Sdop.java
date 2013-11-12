package cn.lucifer.sdop;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import cn.lucifer.sdop.dispatch.Enter;
import cn.lucifer.sdop.service.HttpService;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Sdop extends LcfExtend {

	public final String LOG_RECEIVER_ACTION = "lcf.sdop.ui.Log";

	private String cookies;
	private String userAgent;

	private String ssid;

	private String tokenId;
	private String myUserId;
	private int bp;
	private int ep;
	private int maxSp;
	private int currentSp;

	public final String host = "sdop-g.bandainamco-ol.jp";
	private final String httpUrlPrefix = "http://sdop-g.bandainamco-ol.jp";

	public final String custom_login_url = "http://sdop.bandainamco-ol.jp/";
	public final String game_url = "http://sdop-g.bandainamco-ol.jp/game/top";
	public final String login_url = "http://sdop.bandainamco-ol.jp/api/sdop-g/login.php";

	private final String space = " ";

	private final DateFormat timeFormat = new SimpleDateFormat(
			"M月d日 HH:mm:ss.SSS");

	public Context context;

	public final String EXTRA_LOG_NAME = "log";

	public void log(String msg) {
		if (context == null) {
			return;
		}
		Intent intent = new Intent(LOG_RECEIVER_ACTION);
		intent.putExtra(EXTRA_LOG_NAME, timeFormat.format(new Date()) + space
				+ msg);
		context.sendBroadcast(intent);
	}

	public void post(String url, String payload, String callback) {
		if (context == null) {
			Log.i("Lucifer", "post : no context");
			return;
		}
		Log.i("Lucifer", "post : " + url);
		Intent intent = new Intent(HttpService.POST_ACTION);
		intent.putExtra("url", url);
		intent.putExtra("payload", payload);
		intent.putExtra("callback", callback);
		context.sendBroadcast(intent);
	}

	public void login() {
		String url = httpUrlPrefix + "/PostForAuthentication/enter";
		if (context == null) {
			return;
		}
		try {
			InputStream input = context.getAssets().open("login.json");
			List<String> lines = IOUtils.readLines(input);
			IOUtils.closeQuietly(input);
			String payload = lines.get(0);// 这里处理过gson, 所以只有一行
			post(url, payload, Enter.procedure);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setCookies(String cookies) {
		this.cookies = cookies;
		String ssid = lcf().getCookie(cookies, "ssid");
		if (ssid.equals(this.ssid)) {
			Log.i("Lucifer", "ssid no change !");
			return;
		}
		this.ssid = ssid;
		log("获取 ssid 成功！");
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getSsid() {
		return ssid;
	}

	public String getCookies() {
		return cookies;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
		log("获取 ssid 成功！" + tokenId);
	}

}
