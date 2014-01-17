package cn.lucifer.sdop.ui;

import cn.lucifer.sdop.IGetLcf;
import cn.lucifer.sdop.Lcf;
import cn.lucifer.sdop.service.HttpService;
import android.app.Activity;
import android.content.Intent;

public class BaseActivity extends Activity implements IGetLcf {

	@Override
	public Lcf lcf() {
		return Lcf.getInstance();
	}

	protected void exit() {
		lcf().sdop.duel.releasesRecordMode();
		lcf().sdop.clearAllJob();
		
		Intent httpService = new Intent(this, HttpService.class);
		stopService(httpService);

		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(startMain);

		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
