package cn.lucifer.sdop.ui;

import cn.lucifer.sdop.R;
import cn.lucifer.sdop.R.id;
import cn.lucifer.sdop.R.layout;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebActivity extends BaseActivity {

	private String url;

	private WebView wv;
	private ProgressBar progressBar;

	private OnReceivedErrorListener errorListener;

	protected String game_url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web);

		wv = (WebView) findViewById(R.id.wv);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		Intent beforeIntent = getIntent();
		url = beforeIntent.getExtras().getString("url");
		game_url = lcf().sdop.game_url;

		viewInit();
	}

	private void viewInit() {

		wv.getSettings().setJavaScriptEnabled(true);// 可用JS
		// 不保存表单数据
		// wv.getSettings().setSaveFormData(false);
		// 不保存密码
		// wv.getSettings().setSavePassword(false);
		// 不支持页面放大功能
		// wv.getSettings().setSupportZoom(false);

		wv.setScrollBarStyle(0);// 滚动条风格，为0就是不给滚动条留空间，滚动条覆盖在网页上
		wv.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(final WebView view,
					final String url) {
				loadurl(view, url);// 载入网页
				return true;
			}// 重写点击动作,用webview载入

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (url.equals(game_url)) {

					CookieManager cookieManager = CookieManager.getInstance();
					Intent data = new Intent();
					String cookies = cookieManager.getCookie(game_url);
					Log.i("Lucifer", "cookies : " + cookies);
					// data.putExtra("cookies", cookies);
					String userAgent = view.getSettings().getUserAgentString();
					Log.i("Lucifer", "userAgent : " + userAgent);
					// data.putExtra("userAgent", userAgent);

					lcf().sdop.setCookies(cookies);
					lcf().sdop.setUserAgent(userAgent);

					data.putExtra("ssid", lcf().sdop.getSsid());
					setResult(RESULT_OK, data);// 通过setResult来返回主页
					finish();
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// TODO Auto-generated method stub
				Log.i("Lucifer", "errorCode : " + errorCode);
				super.onReceivedError(view, errorCode, description, failingUrl);
				if (errorListener != null) {
					errorListener.onReceivedError(errorCode);
				}
			}
		});

		wv.setWebChromeClient(new WebChromeClient() {

			public void onProgressChanged(WebView view, int progress) {// 载入进度改变而触发

				if (handler != null)
					if (progress == 100) {
						handler.sendEmptyMessage(1);// 如果全部载入,隐藏进度对话框
					} else {
						Message message = handler.obtainMessage();
						message.what = 2;
						message.arg1 = progress;
						handler.sendMessage(message);
					}
			}
		});

		if (url != null)
			loadurl(wv, url);
	}

	void loadurl(final WebView view, final String url) {
		new Thread() {
			public void run() {
				Message msg = new Message();
				msg.obj = url;
				if (handler != null)
					handler.sendMessage(msg);
			}
		}.start();
	}

	protected void onCallBack(Intent intent) {
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// if (wv != null)
		// wv.destroy();
		// handler = null;
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
		if (wv != null) {
			wv.freeMemory();
		}
	}

	public void setErrorListener(OnReceivedErrorListener errorListener) {
		this.errorListener = errorListener;
	}

	interface OnReceivedErrorListener {
		void onReceivedError(int errorCode);
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {// 定义一个Handler，用于处理下载线程与UI间通讯
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case 0:
					progressBar.setVisibility(View.VISIBLE);
					progressBar.setProgress(0);
					try {
						if (msg.obj != null) {
							wv.loadUrl(msg.obj.toString());// 载入网页
						} else {
							wv.loadUrl(url);// 载入原来的网页
						}
					} catch (Exception e1) {
					}
					break;
				case 1:
					progressBar.setVisibility(View.GONE);
					break;
				case 2:
					progressBar.setProgress(msg.arg1);
					break;
				case 999:
					if (errorListener != null)
						errorListener.onReceivedError(msg.arg2);
					break;
				}
			}
		}
	};
}
