package cn.lucifer.sdop.ui;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import cn.lucifer.sdop.R;
import cn.lucifer.sdop.adt.SneakingAdapter;
import cn.lucifer.sdop.domain.SneakingPlatoon;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
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

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view,
				int position, long id) {
			SneakingPlatoon platoon = sneakingAdapter.getItem(position - 1);
			String txt;
			if ("RETURN".equals(platoon.state.value)) {
				txt = "执行返回, 并重新潜入, " + platoon.platoonId;
				lcf().sdop.sneaking.proxyGetResultData(platoon);
			} else {
				txt = "platoonId : " + platoon.platoonId;
			}
			Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT)
					.show();
		}
	};

	private OnRefreshListener<ListView> onRefreshListener = new OnRefreshListener<ListView>() {
		@Override
		public void onRefresh(PullToRefreshBase<ListView> refreshView) {
			String label = DateUtils.formatDateTime(getApplicationContext(),
					System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
							| DateUtils.FORMAT_SHOW_DATE
							| DateUtils.FORMAT_ABBREV_ALL);

			// Update the LastUpdatedLabel
			refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

			// Do work to refresh the list here.
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
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(refreshReceiver);
	}
}
