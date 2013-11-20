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
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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

		acquireWakeLock();
	}

	private BroadcastReceiver httpReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i("Lucifer", "--------- action : " + action);
			Bundle bundle = intent.getExtras();
			String url = bundle.getString("url");
			String payload = bundle.getString("payload");
			String procedure = bundle.getString("procedure");
			String callback = bundle.getString("callback");
			new HttpThread(action, url, payload, procedure, callback).start();
		}

	};

	private class HttpThread extends Thread {
		private String action, url, payload, procedure, callback;

		public HttpThread(String action, String url, String payload,
				String procedure, String callback) {
			super();
			this.action = action;
			this.url = url;
			this.payload = payload;
			this.procedure = procedure;
			this.callback = callback;
		}

		@Override
		public void run() {
			if (action.equals(GET_ACTION)) {
				try {
					get(url, procedure, callback);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e("Lucifer", "GET IOException", e);
					e.printStackTrace();
					reTry();
				}
				return;
			}

			if (action.equals(POST_ACTION)) {
				try {
					post(url, payload, procedure, callback);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e("Lucifer", "POST IOException", e);
					e.printStackTrace();
					reTry();
				}
				return;
			}
		}

		private int tryCount = 0;

		private void reTry() {
			if (tryCount < 3) {
				tryCount++;
				try {
					Log.w("Lucifer", "retry connection ---> " + tryCount);
					sleep(tryCount * 2000);
					run();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private final String GET = "GET", POST = "POST";

	protected void post(String urlStr, String payload, String procedure,
			String callback) throws IOException {
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

		getResponse(conn, procedure, callback);
	}

	protected void get(String urlStr, String procedure, String callback)
			throws IOException {
		URL url = new URL(urlStr);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		// conn.setDoOutput(true);////POST专用

		conn.setRequestMethod(GET);

		conn(conn);

		getResponse(conn, procedure, callback);
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

	private void getResponse(HttpURLConnection conn, String procedure,
			String callback) throws IOException {
		InputStream is = conn.getInputStream();
		GZIPInputStream gzin = new GZIPInputStream(is);

		byte[] response = IOUtils.toByteArray(gzin);

		// 获取cookies的set信息
		// Map<String, List<String>> map = conn.getHeaderFields();
		// List<String> newCookies = map.get("Set-Cookie");

		IOUtils.closeQuietly(is);

		if (procedure != null) {
			DF.dispatch(procedure, response, callback);
		}
	}

	@Override
	public Lcf lcf() {
		return Lcf.getInstance();
	}

	private WakeLock mWakeLock;// 电源锁

	/**
	 * 申请设备电源锁
	 */
	private void acquireWakeLock() {
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, "cn.lucifer.sdop.service");
			if (null != mWakeLock) {
				try {
					mWakeLock.acquire();
					Log.i("Lucifer", "mWakeLock acquire! =================");
				} catch (SecurityException e) {
					mWakeLock = null;
				}
			}
		}
	}

	/**
	 * onDestroy时，释放设备电源锁
	 */
	private void releaseWakeLock() {
		if (null != mWakeLock) {
			mWakeLock.release();
			Log.i("Lucifer", "mWakeLock release! =================");
		}
		mWakeLock = null;
	}

	@Override
	public void onDestroy() {
		releaseWakeLock();
	}
}
