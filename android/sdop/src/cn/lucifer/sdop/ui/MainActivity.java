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
import android.view.KeyEvent;
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

		filter = new IntentFilter();
		filter.addAction(lcf().sdop.AUTO_LOGIN_RECEIVER_ACTION);
		registerReceiver(autologinReceiver, filter);

		lcf().sdop.setContext(getApplicationContext());

		Intent httpService = new Intent(this, HttpService.class);
		startService(httpService);

		viewInit();
	}

	ListView listView_log;
	LogAdapter logAdapter;

	void viewInit() {
		listView_log = (ListView) findViewById(R.id.listView_log);
		// logAdapter = new LogAdapter(this,
		// android.R.layout.simple_list_item_1);
		logAdapter = new LogAdapter(this);
		listView_log.setAdapter(logAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		// menu.add(int groupId, int itemId, int order, CharSequence title);
		return true;
	}

	private boolean isDisabledLoginMenu = true;

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (isDisabledLoginMenu && lcf().sdop.getTokenId() != null) {
			// menu.findItem(R.id.action_test_hello).setEnabled(true);
			menu.findItem(R.id.action_auto_GB).setEnabled(true);
			menu.findItem(R.id.action_auto_boss).setEnabled(true);
			menu.findItem(R.id.action_sneaking).setEnabled(true);
			isDisabledLoginMenu = false;
		}
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_login: {
			Intent intent = new Intent(this, WebActivity.class);
			intent.putExtra("url", lcf().sdop.custom_login_url);
			startActivityForResult(intent, R.id.action_login);
			break;
		}
		case R.id.action_auto_login: {
			autoLogin();
			break;
		}
		case R.id.action_auto_GB_FIGHT:
			lcf().sdop.duel.targetUnitAttribute = lcf().sdop.ms.unitAttribute[0];
			lcf().sdop.duel.startAutoDuel();
			break;
		case R.id.action_auto_GB_SPECIAL:
			lcf().sdop.duel.targetUnitAttribute = lcf().sdop.ms.unitAttribute[1];
			lcf().sdop.duel.startAutoDuel();
			break;
		case R.id.action_auto_GB_SHOOT:
			lcf().sdop.duel.targetUnitAttribute = lcf().sdop.ms.unitAttribute[2];
			lcf().sdop.duel.startAutoDuel();
			break;
		case R.id.action_auto_GB_record:
			lcf().sdop.duel.startRecordMode();
			item.setEnabled(false);// 不支持关闭
			break;
		case R.id.action_auto_GB_off:
			lcf().sdop.duel.cancelAutoDuel();
			break;
		case R.id.action_auto_super_boss_start:
			lcf().sdop.boss.AI.startAutoSuperRaidBoss();
			break;
		case R.id.action_auto_normal_boss_start:
			lcf().sdop.boss.AI.startAutoNormalRaidBoss();
			break;
		case R.id.action_equip_item_4_sp:
			lcf().sdop.equipItem4Sp(null);
			break;
		case R.id.action_auto_boss_off:
			lcf().sdop.boss.AI.cancelAutoSuperRaidBoss();
			break;
		case R.id.action_sneaking:
			Intent intent = new Intent(this, SneakingActivity.class);
			startActivity(intent);
			break;
		case R.id.action_exit:
			exit();
			break;

		default:
			break;
		}

		// return super.onMenuItemSelected(featureId, item);
		return true;
	}

	/**
	 * true登录中, 不要再次发送登录请求
	 */
	private boolean isLogin;

	private void autoLogin() {
		if (isLogin) {
			return;
		}
		isLogin = true;
		Intent intent = new Intent(MainActivity.this, WebActivity.class);
		intent.putExtra("url", lcf().sdop.login_url);
		startActivityForResult(intent, R.id.action_login);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		Log.i("Lucifer", requestCode + " : " + R.id.action_login + " : "
				+ resultCode);
		if (requestCode == R.id.action_login && resultCode == RESULT_OK) {
			String ssid = data.getStringExtra("ssid");
			Toast.makeText(this, "ssid : " + ssid, Toast.LENGTH_SHORT).show();

			lcf().sdop.login();
			// addLog("获得ssid : " + Lcf.getInstance().getCookie(cookies,
			// "ssid"));
			isLogin = false;
		}
	}

	protected void addLog(String text) {
		logAdapter.addFirst(text);
		// logAdapter.notifyDataSetChanged();//数据发生变化, 刷新
	}

	private BroadcastReceiver logReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// if (intent.getAction().equals(lcf().sdop.LOG_RECEIVER_ACTION)) {
			addLog(intent.getExtras().getString(lcf().sdop.EXTRA_LOG_NAME));
			// }
		}

	};

	private BroadcastReceiver autologinReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			autoLogin();
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(logReceiver);
		unregisterReceiver(autologinReceiver);
	}

	private long exitTime = 0;

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出sdop！",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				exit();
			}
			return true;
		}
		return false;
	}

}
