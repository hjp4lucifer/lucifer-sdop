package cn.lucifer.sdop.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import cn.lucifer.sdop.IGetLcf;
import cn.lucifer.sdop.Lcf;
import cn.lucifer.sdop.dispatch.DF;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class HttpService extends Service implements IGetLcf {

	public final static String GET_ACTION = "cn.lucifer.sdop.service.GET";
	public final static String POST_ACTION = "cn.lucifer.sdop.service.POST";

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter filter = new IntentFilter(GET_ACTION);
		filter.addAction(POST_ACTION);
		registerReceiver(httpReceiver, filter);

		Log.i("Lucifer", "--------- HttpService onCreate ! ");
		DF.init();
	}

	private BroadcastReceiver httpReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i("Lucifer", "--------- action : " + action);
			Bundle bundle = intent.getExtras();
			String url = bundle.getString("url");
			String callback = bundle.getString("callback");
			String payload = bundle.getString("payload");
			new HttpThread(action, url, payload, callback).start();
		}

	};

	private class HttpThread extends Thread {
		private String action, url, payload, callback;

		public HttpThread(String action, String url, String payload,
				String callback) {
			super();
			this.action = action;
			this.url = url;
			this.payload = payload;
			this.callback = callback;
		}

		@Override
		public void run() {
			if (action.equals(GET_ACTION)) {
				try {
					get(url, callback);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("Lucifer", "GET IOException", e);
				}
				return;
			}

			if (action.equals(POST_ACTION)) {
				try {
					post(url, payload, callback);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("Lucifer", "POST IOException", e);
				}
				return;
			}
		}
	}

	private final String GET = "GET", POST = "POST";

	protected void post(String urlStr, String payload, String callback)
			throws IOException {
		URL url = new URL(urlStr);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);// //POST专用

		if (payload == null) {
			return;
		}
		Log.i("Lucifer", "payload : " + payload);

		conn.setRequestMethod(POST);
		conn.setRequestProperty("Content-Length",
				String.valueOf(payload.length()));

		conn(conn);

		OutputStream os = conn.getOutputStream();
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
		pw.write(payload);
		pw.close();

		getResponse(conn, callback);
	}

	protected void get(String urlStr, String callback) throws IOException {
		URL url = new URL(urlStr);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		// conn.setDoOutput(true);////POST专用

		conn.setRequestMethod(GET);

		conn(conn);

		getResponse(conn, callback);
	}

	private void getResponse(HttpURLConnection conn, String callback)
			throws IOException {
		InputStream is = conn.getInputStream();
		GZIPInputStream gzin = new GZIPInputStream(is);

		byte[] response = IOUtils.toByteArray(gzin);

		// 获取cookies的set信息
		// Map<String, List<String>> map = conn.getHeaderFields();
		// List<String> newCookies = map.get("Set-Cookie");

		is.close();

		if (callback != null) {
			DF.dispatch(callback, response);
		}
	}

	private void conn(HttpURLConnection conn) throws IOException {
		conn.setRequestProperty("Cookie", lcf().sdop.getCookies());
		conn.setRequestProperty("Content-Type", "application/octet-stream");
		conn.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
		conn.setRequestProperty("Accept", "*/*");
		conn.setRequestProperty("Host", lcf().sdop.host);

		conn.setRequestProperty("Referer", lcf().sdop.game_url);
		conn.setRequestProperty("User-Agent", lcf().sdop.getUserAgent());

		conn.connect();
	}

	@Override
	public Lcf lcf() {
		return Lcf.getInstance();
	}
}
