package cn.lucifer.sdop.ui;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import cn.lucifer.sdop.R;
import cn.lucifer.sdop.adt.SneakingAdapter;
import cn.lucifer.sdop.domain.SneakingPlatoon;
import cn.lucifer.sdop.ui.extend.OnRefreshListenerTemplate;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class SneakingActivity extends BaseActivity {

	private PullToRefreshListView mPullRefreshListView;
	SneakingAdapter sneakingAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sneaking);

		IntentFilter filter = new IntentFilter();
		filter.addAction(lcf().sdop.sneaking.REFRESH_RECEIVER_ACTION);
		registerReceiver(refreshReceiver, filter);

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(onRefreshListener);

		ListView platoonListView = mPullRefreshListView.getRefreshableView();
		sneakingAdapter = new SneakingAdapter(this);
		platoonListView.setAdapter(sneakingAdapter);

		platoonListView.setOnItemClickListener(onItemClickListener);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 自动刷
		mPullRefreshListView.setRefreshing(false);
	}

	/**
	 * true锁住点击事件
	 */
	private boolean isLockItemClick = false;

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view,
				int position, long id) {
			if (isLockItemClick) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.msg_now_proxy_running),
						Toast.LENGTH_SHORT).show();
				return;
			}
			SneakingPlatoon platoon = sneakingAdapter.getItem(position - 1);
			if ("RETURN".equals(platoon.state.value)) {
				isLockItemClick = true;
				lcf().sdop.sneaking.proxyGetResultData(platoon);
				Toast.makeText(
						getApplicationContext(),
						getString(
								R.string.msg_ready_sneaking_proxy_running_format,
								platoon.platoonId), Toast.LENGTH_SHORT).show();
			}
		}
	};

	private OnRefreshListener<ListView> onRefreshListener = new OnRefreshListenerTemplate<ListView>() {

		@Override
		protected void startRefreshWork() {
			lcf().sdop.sneaking.getSneakingMissionTopData();
		}
	};

	private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (lcf().sdop.sneaking.sneakingMissionTopDataArgs != null) {
				sneakingAdapter
						.refreshPlatoons(lcf().sdop.sneaking.sneakingMissionTopDataArgs.platoonDataList);
			}
			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshListView.onRefreshComplete();
			isLockItemClick = false;
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(refreshReceiver);
	}
}
