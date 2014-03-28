package cn.lucifer.sdop.adt;

import java.util.ArrayList;
import java.util.Arrays;

import cn.lucifer.sdop.R;
import cn.lucifer.sdop.domain.CardSynthesis;
import cn.lucifer.sdop.domain.Characteristic;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class MSSynthesisAdapter extends BaseAdapter {
	protected Context context;
	protected LayoutInflater inflater;
	private ArrayList<CardSynthesis> cards = new ArrayList<CardSynthesis>();

	public MSSynthesisAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		addBlank();
	}

	/**
	 * 生成一个空的CardSynthesis
	 */
	protected void addBlank() {
		CardSynthesis card = new CardSynthesis();
		card.name = "wait loading...";
		cards.add(card);
	}

	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public CardSynthesis getItem(int position) {
		return cards.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void refreshPlatoons(CardSynthesis[] cards) {
		if (cards == null) {
			return;
		}
		this.cards.clear();
		this.cards.addAll(Arrays.asList(cards));
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_ms_synthesis, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		CardSynthesis card = cards.get(position);
		if (card != null) {
			holder.selectStatus.setChecked(card.isSelect);
			holder.name.setText(card.name);
			holder.info.setText(String.format("%dc%d, Lv: %d, next exp: %d",
					card.rarity, card.cost, card.level, card.nextExp));

			if (card.characteristicList != null) {
				Characteristic characteristic;
				TextView holderCharacteristic;
				int index = 0;
				for (; index < card.characteristicList.length; index++) {
					characteristic = card.characteristicList[index];
					holderCharacteristic = holder.characteristicList[index];
					holderCharacteristic.setText(String.format("☆%d %s %s",
							characteristic.rank, characteristic.name,
							characteristic.briefDescription));
					switch (characteristic.typeId) {
					case 1:
						holderCharacteristic.setTextColor(Color.RED);
						break;
					case 2:
						holderCharacteristic.setTextColor(Color.GREEN);
						break;
					case 3:
						holderCharacteristic.setTextColor(0xFF00FFE0);
						break;
					case 4:
						holderCharacteristic.setTextColor(0xFFE000FF);
						break;
					case 5:
						holderCharacteristic.setTextColor(0xFFDADA0A);
						break;
					default:
						holderCharacteristic.setTextColor(Color.BLACK);
						break;
					}

				}

				if (card.isMaxLv()) {
					holder.lock.setVisibility(View.VISIBLE);
					holder.selectStatus.setEnabled(false);
				}else {
					holder.lock.setVisibility(View.INVISIBLE);
					holder.selectStatus.setEnabled(true);
				}
			}
		}
		return convertView;
	}

	public class ViewHolder {
		CheckBox selectStatus;
		TextView name, info, lock;
		TextView[] characteristicList;

		public ViewHolder(View convertView) {
			selectStatus = (CheckBox) convertView
					.findViewById(R.id.card_select_status);
			name = (TextView) convertView.findViewById(R.id.card_name);
			info = (TextView) convertView.findViewById(R.id.card_info);

			characteristicList = new TextView[2];
			characteristicList[0] = (TextView) convertView
					.findViewById(R.id.characteristic0);
			characteristicList[1] = (TextView) convertView
					.findViewById(R.id.characteristic1);
			
			lock =  (TextView) convertView.findViewById(R.id.card_lock);
		}
	}

}
