package cn.lucifer.sdop.adt;

import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import cn.lucifer.sdop.R;
import cn.lucifer.sdop.domain.CardSynthesis;
import cn.lucifer.sdop.domain.Characteristic;

public class MSSynthesisViewHolder {

	public CheckBox selectStatus;
	public TextView name, info, lock;
	public TextView[] characteristicList;

	public MSSynthesisViewHolder(View convertView) {
		selectStatus = (CheckBox) convertView
				.findViewById(R.id.card_select_status);
		name = (TextView) convertView.findViewById(R.id.card_name);
		info = (TextView) convertView.findViewById(R.id.card_info);

		characteristicList = new TextView[2];
		characteristicList[0] = (TextView) convertView
				.findViewById(R.id.characteristic0);
		characteristicList[1] = (TextView) convertView
				.findViewById(R.id.characteristic1);

		lock = (TextView) convertView.findViewById(R.id.card_lock);
	}

	public void setBaseShow(CardSynthesis card) {
		selectStatus.setChecked(card.isChoose);
		name.setText(card.name);
		info.setText(String.format("%dc%d, Lv: %d, next exp: %d", card.rarity,
				card.cost, card.level, card.nextExp));

		clearCharacteristicList();

		if (card.characteristicList != null) {
			Characteristic characteristic;
			TextView holderCharacteristic;
			int index = 0;
			for (; index < card.characteristicList.length; index++) {
				characteristic = card.characteristicList[index];
				holderCharacteristic = characteristicList[index];
				holderCharacteristic.setText(String.format("☆%d %s, %s",
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

		}
	}

	public void cancelMainChoose() {
		lock.setText(R.string.lable_plase_set_main_choose);
		lock.setBackgroundColor(0x66FCFCB2);
		lock.setTextColor(Color.BLACK);
		selectStatus.setChecked(false);
		name.setText(null);
		info.setText(null);
		clearCharacteristicList();
	}

	public void setMainChoose() {
		lock.setText(R.string.lable_click_cancel);
	}

	public void clearCharacteristicList() {
		for (TextView textView : characteristicList) {
			textView.setText(null);
		}
	}
}
