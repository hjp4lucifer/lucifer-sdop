package cn.lucifer.sdop;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.lucifer.sdop.adt.LogAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);

		Log.i("Lucifer", requestCode + " : " + R.id.action_login + " : "
				+ resultCode);
		if (requestCode == R.id.action_login && resultCode == RESULT_OK) {
			String text = data.getStringExtra("cookies");
			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
			addLog(text);
		}
	}

	protected void addLog(String text) {
		logAdapter.add(text);
	}

}
