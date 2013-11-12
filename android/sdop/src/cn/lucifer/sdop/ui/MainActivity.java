package cn.lucifer.sdop.ui;

import cn.lucifer.sdop.R;
import cn.lucifer.sdop.adt.LogAdapter;
import cn.lucifer.sdop.service.HttpService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		IntentFilter filter = new IntentFilter();
		filter.addAction(lcf().sdop.LOG_RECEIVER_ACTION);
		registerReceiver(logReceiver, filter);

		lcf().sdop.context = getApplicationContext();

		Intent httpService = new Intent(this, HttpService.class);
		startService(httpService);
		
		viewInit();
	}

	ListView listView_log;
	LogAdapter logAdapter;

	void viewInit() {
		listView_log = (ListView) findViewById(R.id.listView_log);
		logAdapter = new LogAdapter(this, android.R.layout.simple_list_item_1);
		listView_log.setAdapter(logAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		// menu.add(int groupId, int itemId, int order, CharSequence title);
		// menu.add(1, 1, 1, "he");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.action_login:
			intent = new Intent(this, WebActivity.class);
			intent.putExtra("url", lcf().sdop.custom_login_url);
			break;
		case R.id.action_exit:
			exit();
			break;

		default:
			break;
		}

		if (intent != null) {
			startActivityForResult(intent, R.id.action_login);
		}

		// return super.onMenuItemSelected(featureId, item);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		Log.i("Lucifer", requestCode + " : " + R.id.action_login + " : "
				+ resultCode);
		if (requestCode == R.id.action_login && resultCode == RESULT_OK) {
			String ssid = data.getStringExtra("ssid");
			Toast.makeText(this, "ssid : " + ssid, Toast.LENGTH_LONG).show();

			lcf().sdop.login();
			// addLog("获得ssid : " + Lcf.getInstance().getCookie(cookies,
			// "ssid"));
		}
	}

	protected void addLog(String text) {
		if (logAdapter.getCount() > 100) {
			logAdapter.clear();
		}
		logAdapter.add(text);
		// logAdapter.notifyDataSetChanged();//数据发生变化, 刷新
	}

	private BroadcastReceiver logReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(lcf().sdop.LOG_RECEIVER_ACTION)) {
				addLog(intent.getExtras().getString(lcf().sdop.EXTRA_LOG_NAME));
			}
		}

	};

}
