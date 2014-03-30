package cn.lucifer.sdop.ui;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import cn.lucifer.sdop.R;
import cn.lucifer.sdop.adt.MSSynthesisAdapter;
import cn.lucifer.sdop.adt.MSSynthesisViewHolder;
import cn.lucifer.sdop.domain.CardSynthesis;
import cn.lucifer.sdop.ui.extend.OnRefreshListenerTemplate;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

/**
 * MS合成
 * 
 * @author Lucifer
 * 
 */
public class MSSynthesisActivity extends BaseActivity {
	private PullToRefreshListView mPullRefreshListView;
	private ListView cardListView;
	private MSSynthesisAdapter synthesisAdapter;
	private MSSynthesisViewHolder chooseViewHolder;
	private Button btnMsMerger;
	protected ProgressDialog progressDialog;

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

		cardListView = mPullRefreshListView.getRefreshableView();
		synthesisAdapter = new MSSynthesisAdapter(this);
		cardListView.setAdapter(synthesisAdapter);

		cardListView.setOnItemClickListener(onItemClickListener);

		View chooseView = findViewById(R.id.ms_choose);
		chooseView.setOnClickListener(chooseViewOnClickListener);
		chooseViewHolder = new MSSynthesisViewHolder(chooseView);
		chooseViewHolder.selectStatus.setVisibility(View.INVISIBLE);

		btnMsMerger = (Button) findViewById(R.id.btn_ms_merger);
		btnMsMerger.setOnClickListener(btnMsMergerOnClickListener);

		setMainChooseView();

		// 自动刷
		mPullRefreshListView.setRefreshing(false);
	}

	private boolean isMsChoose;

	private void setMainChooseView() {
		if (isMsChoose) {
			chooseViewHolder.setMainChoose();
			btnMsMerger.setVisibility(View.VISIBLE);
			serMsMergerButtonText();
		} else {
			chooseViewHolder.cancelMainChoose();
			btnMsMerger.setVisibility(View.INVISIBLE);
		}
	}

	protected void serMsMergerButtonText() {
		btnMsMerger.setText(getString(R.string.msg_ms_synthesis_format,
				synthesisAdapter.getChooseCount()));
	}

	private OnClickListener btnMsMergerOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (isMsChoose) {
				SimpleEntry<Integer, List<Integer>> choose = synthesisAdapter
						.getChoose();
				if (choose == null) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.msg_ms_synthesis_no_choose),
							Toast.LENGTH_SHORT).show();
					return;
				}
				progressDialog = ProgressDialog.show(MSSynthesisActivity.this,
						getString(R.string.msg_loading),
						getString(R.string.msg_please_wait), true, false);
				lcf().sdop.synthesis.enhancedSynthesis(choose.getKey(),
						choose.getValue());
			}
		}
	};

	private OnClickListener chooseViewOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (isMsChoose) {
				cancelMainChoose();
			}
		}
	};

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			int itemIndex = position - 1;
			if (isMsChoose) {
				synthesisAdapter.setChooseItem(itemIndex);
				serMsMergerButtonText();
			} else {
				CardSynthesis card = synthesisAdapter.setMainChoose(itemIndex);
				if (card == null) {
					return;
				}
				isMsChoose = true;
				chooseViewHolder.setBaseShow(card);
				cardListView.setSelection(1);
				setMainChooseView();
			}
		}

	};

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
				cancelMainChoose();
			} else {// 异常
				Toast.makeText(getApplicationContext(),
						getString(R.string.msg_ms_synthesis_error),
						Toast.LENGTH_SHORT).show();
			}

			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			mPullRefreshListView.onRefreshComplete();
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(refreshReceiver);
	}

	protected void cancelMainChoose() {
		synthesisAdapter.cancelMainChoose();
		cardListView.setSelection(1);
		isMsChoose = false;
		setMainChooseView();
	}
}
