package cn.lucifer.sdop.ui.extend;

import android.text.format.DateUtils;
import android.view.View;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

public abstract class OnRefreshListenerTemplate<V extends View> implements
		OnRefreshListener<V> {

	@Override
	public void onRefresh(PullToRefreshBase<V> refreshView) {
		String label = DateUtils.formatDateTime(refreshView.getContext(),
				System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);

		// Update the LastUpdatedLabel
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

		startRefreshWork();
	}

	/**
	 * Do work to refresh the list here.
	 */
	protected abstract void startRefreshWork();

}
