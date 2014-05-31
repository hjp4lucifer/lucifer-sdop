package cn.lucifer.sdop.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import cn.lucifer.sdop.IGetLcf;
import cn.lucifer.sdop.Lcf;
import cn.lucifer.sdop.dispatch.DF;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class HttpService extends Service implements IGetLcf {

	public static final String GET = "GET", POST = "POST";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3);

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(lcf().LOG_TAG, "--------- HttpService onCreate ! ");
		DF.init();

		acquireWakeLock();
		clearAllJob();
	}

	protected void clearAllJob() {
		releaseThreadPool();
		executor = new ScheduledThreadPoolExecutor(3);
	}

	protected void releaseThreadPool() {
		if (executor != null) {
			executor.shutdownNow();
			executor.getQueue().clear();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return super.onStartCommand(intent, flags, startId);
		}
		Bundle bundle = intent.getExtras();
		if (bundle == null) {
			return super.onStartCommand(intent, flags, startId);
		}
		String method = bundle.getString("method");
		String url = bundle.getString("url");
		if (url == null) {
			return super.onStartCommand(intent, flags, startId);
		}

		String payload = bundle.getString("payload");
		String procedure = bundle.getString("procedure");
		String callback = bundle.getString("callback");

		executor.execute(new HttpThread(method, url, payload, procedure,
				callback));

		return super.onStartCommand(intent, flags, startId);
	}

	private class HttpThread extends Thread {
		private String method, url, payload, procedure, callback;

		public HttpThread(String method, String url, String payload,
				String procedure, String callback) {
			super();
			this.method = method;
			this.url = url;
			this.payload = payload;
			this.procedure = procedure;
			this.callback = callback;
		}

		@Override
		public void run() {
			if (method.equals(GET)) {
				try {
					get(url, procedure, callback);
				} catch (IOException e) {
					Log.e(lcf().LOG_TAG, "GET IOException", e);
					reTry();
				}
				return;
			}

			if (method.equals(POST)) {
				try {
					post(url, payload, procedure, callback);
				} catch (IOException e) {
					Log.e(lcf().LOG_TAG, "POST IOException", e);
					reTry();
				}
				return;
			}
		}

		private int tryCount = 0;

		private void reTry() {
			if (tryCount < 5) {
				tryCount++;
				try {
					Log.w(lcf().LOG_TAG, "retry connection ---> " + tryCount);
					lcf().sdop.log("请求失败！尝试重连  ---> " + tryCount);
					sleep(tryCount * 2000);
					run();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void post(String urlStr, String payload, String procedure,
			String callback) throws IOException {
		URL url = new URL(urlStr);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);// //POST专用

		if (payload == null) {
			return;
		}
		Log.i(lcf().LOG_TAG, "payload : " + payload);

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

	private final int connect_timeout = 61000;

	private void conn(HttpURLConnection conn) throws IOException {
		conn.setRequestProperty("Cookie", lcf().sdop.getCookies());
		conn.setRequestProperty("Content-Type", "application/octet-stream");
		conn.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
		conn.setRequestProperty("Accept", "*/*");
		conn.setRequestProperty("Host", lcf().sdop.host);

		conn.setRequestProperty("Referer", lcf().sdop.game_url);
		conn.setRequestProperty("User-Agent", lcf().sdop.getUserAgent());

		conn.setConnectTimeout(connect_timeout);
		conn.setReadTimeout(connect_timeout);

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
					Log.i(lcf().LOG_TAG, "mWakeLock acquire! =================");
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
			Log.i(lcf().LOG_TAG, "mWakeLock release! =================");
		}
		mWakeLock = null;
	}

	@Override
	public void onDestroy() {
		releaseWakeLock();
		releaseThreadPool();
	}
}
