package cn.lucifer.sdop;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.CallbackThread;
import cn.lucifer.sdop.dispatch.ex.Enter;
import cn.lucifer.sdop.dispatch.ex.EquipItem4Sp;
import cn.lucifer.sdop.dispatch.ex.PostGreeting;
import cn.lucifer.sdop.service.HttpService;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Sdop extends LcfExtend {

	public final String LOG_RECEIVER_ACTION = "lcf.sdop.ui.Log";
	public final String AUTO_LOGIN_RECEIVER_ACTION = "lcf.sdop.autoLogin";

	private String cookies;
	private String userAgent;

	public String ssid;

	private String tokenId;
	public int myUserId;
	public int bp;
	public int ep;
	public int maxSp;
	public int currentSp;

	public final String host = "sdop-g.bandainamco-ol.jp";
	public final String httpUrlPrefix = "http://sdop-g.bandainamco-ol.jp";

	public final String custom_login_url = "http://sdop.bandainamco-ol.jp/";
	public final String game_url = "http://sdop-g.bandainamco-ol.jp/game/top";
	public final String login_url = "http://sdop.bandainamco-ol.jp/api/sdop-g/login.php";
	public final String mobile_index_url ="http://sdop-g.bandainamco-ol.jp/mobile/top";

	private final String space = " ";

	private final DateFormat timeFormat = new SimpleDateFormat(
			"M月d日 HH:mm:ss.SSS");

	private Context context;

	public void setContext(Context context) {
		this.context = context;
		initAfterContext();
	}

	public Auto auto;
	public Ms ms;
	public Pilot pilot;
	public Duel duel;
	public Boss boss;

	public void initAfterContext() {
		if (auto == null) {
			auto = new Auto();
		}
		if (ms == null) {
			ms = new Ms();
		}
		if (pilot == null) {
			pilot = new Pilot();
		}
		if (duel == null) {
			duel = new Duel();
		}
		if (boss == null) {
			boss = new Boss();
		}
	}

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

	public String createGetParams() {
		return String.format("ssid=%s&tokenId=%s&timeStamp=%d", ssid, tokenId,
				lcf().getTimeStamp());
	}

	public JSONObject createBasePayload(String procedure, JSONObject args)
			throws JSONException {
		JSONObject json = new JSONObject();
		json.put("procedure", procedure);
		json.put("tokenId", tokenId);
		json.put("ssid", ssid);
		json.put("args", args);
		return json;
	}

	public void get(String url, String procedure, String callback) {
		if (context == null) {
			Log.i("Lucifer", "post : no context");
			return;
		}
		Log.i("Lucifer", "get : " + url);
		Intent intent = new Intent(HttpService.GET_ACTION);
		intent.putExtra("url", url);
		intent.putExtra("procedure", procedure);
		intent.putExtra("callback", callback);
		context.sendBroadcast(intent);
	}

	public void post(String url, String payload, String procedure,
			String callback) {
		if (context == null) {
			Log.i("Lucifer", "post : no context");
			return;
		}
		Log.i("Lucifer", "post : " + url);
		Intent intent = new Intent(HttpService.POST_ACTION);
		intent.putExtra("url", url);
		intent.putExtra("payload", payload);
		intent.putExtra("procedure", procedure);
		intent.putExtra("callback", callback);
		context.sendBroadcast(intent);
	}

	public boolean checkError(JSONObject dataArgs, String msg)
			throws JSONException {
		if (dataArgs.isNull("message")) {
			return false;
		}
		String message = dataArgs.getString("message");
		Log.w("Lucifer", dataArgs.toString());
		printStackTrace();
		log(msg + "：" + message);

		checkReload(message);
		// lcf.sdop.checkBattleFinished(data);

		return true;
	}

	protected void checkReload(String message) {
		if (message.indexOf("再度ログインお願いします。") > 0) {
			Intent intent = new Intent(AUTO_LOGIN_RECEIVER_ACTION);
			context.sendBroadcast(intent);
		}
	}

	final long delayMillis = 100;

	public void checkCallback(String callback) {
		checkCallback(callback, delayMillis, null);
	}

	public void checkCallback(String callback, Object[] args) {
		checkCallback(callback, delayMillis, args);
	}

	private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
			10);

	public void checkCallback(String callback, long delayMillis, Object[] args) {
		if (callback == null) {
			return;
		}
		executor.schedule(new CallbackThread(callback, args), delayMillis,
				TimeUnit.MILLISECONDS);
	}

	public void clearAllJob() {
		if (executor != null) {
			executor.shutdownNow();
			executor.getQueue().clear();
		}
		executor = new ScheduledThreadPoolExecutor(10);
	}

	public void startJob(Runnable command, long initialDelayMillis,
			long periodMillis) {
		executor.scheduleAtFixedRate(command, initialDelayMillis, periodMillis,
				TimeUnit.MILLISECONDS);
	}

	public String loadJson(String assetsFileName) throws IOException {
		InputStream input = lcf().sdop.context.getAssets().open(assetsFileName);
		List<String> lines = IOUtils.readLines(input);
		IOUtils.closeQuietly(input);
		String json = lines.get(0);// 这里处理过gson, 所以只有一行
		lines = null;

		return json;
	}

	public JSONObject loadJsonObject(String assetsFileName) throws IOException,
			JSONException {
		return new JSONObject(loadJson(assetsFileName));
	}

	public void login() {
		String url = httpUrlPrefix + "/PostForAuthentication/enter";
		try {
			String payload = loadJson("login.json");
			post(url, payload, Enter.procedure, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testToAdsPostGreeting() {
		String url = httpUrlPrefix + "/PostForProfile/postGreeting?ssid="
				+ ssid;
		try {
			JSONObject payload = createBasePayload(
					PostGreeting.procedure,
					new JSONObject().put("comment", "hello").put(
							"greetingUserId", 339947));
			post(url, payload.toString(), PostGreeting.procedure, null);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// private final int[] itemIdList = new int[] { 20006, 20011, 20013 };
	private final String itemIdList_json = "[ 20006, 20011, 20013 ]";

	public void equipItem4Sp(String callback) {
		String url = httpUrlPrefix + "/PostForCardPlatoon/equipItem?ssid="
				+ ssid;
		try {
			JSONObject payload = createBasePayload(EquipItem4Sp.procedure,
					new JSONObject().put("itemIdList", new JSONArray(
							itemIdList_json)));
			post(url, payload.toString(), EquipItem4Sp.procedure, callback);
		} catch (JSONException e) {
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

	public String getRedMsg(String msg) {
		return "<font color=\"#FF0000\">" + msg + "</font>";
	}

	public String getRedBoldMsg(String msg) {
		return "<font color=\"#FF0000\"><b>" + msg + "</b></font>";
	}

	public String getBoldMsg(String msg) {
		return "<b>" + msg + "</b>";
	}

	public String getBlueMsg(String msg) {
		return "<font color=\"#0000FF\">" + msg + "</font>";
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
		log("获取 tokenId 成功！" + tokenId);
	}

}
