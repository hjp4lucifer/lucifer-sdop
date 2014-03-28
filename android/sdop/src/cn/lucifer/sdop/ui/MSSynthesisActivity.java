package cn.lucifer.sdop.ui;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import cn.lucifer.sdop.R;
import cn.lucifer.sdop.adt.MSSynthesisAdapter;
import cn.lucifer.sdop.ui.extend.OnRefreshListenerTemplate;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ListView;

/**
 * MS合成
 * 
 * @author Lucifer
 * 
 */
public class MSSynthesisActivity extends BaseActivity {
	private PullToRefreshListView mPullRefreshListView;
	MSSynthesisAdapter synthesisAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ms_synthesis);

		IntentFilter filter = new IntentFilter();
		filter.addAction(lcf().sdop.synthesis.REFRESH_RECEIVER_MS_ACTION);
		registerReceiver(refreshReceiver, filter);

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(onRefreshListener);

		ListView cardListView = mPullRefreshListView.getRefreshableView();
		synthesisAdapter = new MSSynthesisAdapter(this);
		cardListView.setAdapter(synthesisAdapter);

	}

	@Override
	protected void onStart() {
		super.onStart();
		// 自动刷
		mPullRefreshListView.setRefreshing(false);
	}

	private OnRefreshListener<ListView> onRefreshListener = new OnRefreshListenerTemplate<ListView>() {

		@Override
		protected void startRefreshWork() {
			lcf().sdop.synthesis.getMSCardEnhancedSynthesisData();
		}
	};

	private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (lcf().sdop.synthesis.msCardList != null) {
				synthesisAdapter
						.refreshPlatoons(lcf().sdop.synthesis.msCardList);
				lcf().sdop.synthesis.msCardList = null;
			}
			mPullRefreshListView.onRefreshComplete();
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(refreshReceiver);
	}
}
