package cn.lucifer.sdop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);

		Log.i("Lucifer", requestCode + " : " + R.id.action_login + " : " + resultCode);
		if (requestCode == R.id.action_login && resultCode == RESULT_OK) {
			String text = data.getStringExtra("cookies");
			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		}
	}

}
