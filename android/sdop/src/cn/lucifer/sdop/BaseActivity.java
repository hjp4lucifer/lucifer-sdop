package cn.lucifer.sdop;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends Activity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.action_login:
			intent = new Intent(this, WebActivity.class);
			break;
		case R.id.action_exit:
			exit();
			break;

		default:
			break;
		}
		
		if (intent != null) {
			//startActivity(intent);
			startActivityForResult(intent, R.id.action_login);
		}
		
//		return super.onMenuItemSelected(featureId, item);
		return true;
	}

	private void exit() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(startMain);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
