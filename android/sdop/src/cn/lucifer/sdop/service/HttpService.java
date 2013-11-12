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
	}

	private BroadcastReceiver httpReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i("Lucifer", "--------- action : " + action);
			if (action.equals(GET_ACTION)) {
				try {
					get(intent.getExtras());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}

			if (action.equals(POST_ACTION)) {
				try {
					post(intent.getExtras());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
		}

	};

	private final String GET = "GET", POST = "POST";

	protected void post(Bundle bundle) throws IOException {
		URL url = new URL(bundle.getString("url"));
		String payload = bundle.getString("payload");

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);// //POST专用

		if (payload == null) {
			return;
		}

		conn.setRequestMethod(POST);
		conn.setRequestProperty("Content-Length",
				String.valueOf(payload.length()));

		conn(conn);

		OutputStream os = conn.getOutputStream();
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
		pw.write(payload);
		pw.close();

		getResponse(conn, bundle);
	}

	protected void get(Bundle bundle) throws IOException {
		URL url = new URL(bundle.getString("url"));

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		// conn.setDoOutput(true);////POST专用

		conn.setRequestMethod(GET);

		conn(conn);

		getResponse(conn, bundle);
	}

	private void getResponse(HttpURLConnection conn, Bundle bundle)
			throws IOException {
		String callback = bundle.getString("callback");
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
