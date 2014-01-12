package cn.lucifer.sdop.adt;

import java.util.ArrayList;
import java.util.Arrays;

import cn.lucifer.sdop.Lcf;
import cn.lucifer.sdop.R;
import cn.lucifer.sdop.domain.SneakingPlatoon;

import android.content.Context;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SneakingAdapter extends BaseAdapter {

	protected Context context;
	protected LayoutInflater inflater;
	private ArrayList<SneakingPlatoon> platoons = new ArrayList<SneakingPlatoon>();

	public SneakingAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return platoons.size();
	}

	@Override
	public SneakingPlatoon getItem(int position) {
		return platoons.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void refreshPlatoons(SneakingPlatoon[] platoons) {
		if (platoons == null) {
			return;
		}
		this.platoons.clear();
		this.platoons.addAll(Arrays.asList(platoons));
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater
					.inflate(R.layout.item_sneaking_platoon, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		SneakingPlatoon platoon = platoons.get(position);
		if (platoon != null) {
			holder.id.setText(String.valueOf(platoon.platoonId));
			holder.name.setText(platoon.platoonName);
			holder.requiredTime.setText(DateUtils
					.formatElapsedTime(platoon.requiredTime));
			holder.report.setText(platoon.report);
			if (platoon.isSecret) {
				holder.state.setText(Html.fromHtml(platoon.state.value + "　"
						+ Lcf.getInstance().sdop.getRedBoldMsg("Secret")));
			} else {
				holder.state.setText(platoon.state.value);
			}
		}
		return convertView;
	}

	class ViewHolder {
		TextView id, name, requiredTime, report, state;

		public ViewHolder(View convertView) {
			id = (TextView) convertView.findViewById(R.id.platoon_id);
			name = (TextView) convertView.findViewById(R.id.platoon_name);
			requiredTime = (TextView) convertView
					.findViewById(R.id.platoon_required_time);
			report = (TextView) convertView.findViewById(R.id.platoon_report);
			state = (TextView) convertView.findViewById(R.id.platoon_state);
		}
	}
}
