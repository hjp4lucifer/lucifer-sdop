package cn.lucifer.sdop.adt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.lucifer.sdop.R;
import cn.lucifer.sdop.domain.CardSynthesis;
import cn.lucifer.sdop.domain.Characteristic;
import cn.lucifer.sdop.domain.SimplePair;
import cn.lucifer.sdop.domain.Value;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

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
		card.attribute = new Value("");
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

	public void setChooseItem(int position) {
		CardSynthesis card = getItem(position);
		if (isLock(card)) {
			return;
		}
		card.reverseChoose();
		notifyDataSetChanged();
	}

	public void refreshPlatoons(CardSynthesis[] cards) {
		if (cards == null) {
			return;
		}
		this.cards.clear();
		this.cards.addAll(Arrays.asList(cards));
		notifyDataSetChanged();
	}

	/**
	 * 主合成机以选择
	 */
	private boolean isMainChoose;

	/**
	 * 设置是否选中需要升级的MS, 并重新排序
	 * 
	 * @param position
	 * @return null表示不能选择的MS
	 */
	public CardSynthesis setMainChoose(int position) {
		clearMainChoose();
		CardSynthesis card = getItem(position);
		if (card.isMaxLv()) {
			return null;
		}
		card.isMainChoose = true;
		isMainChoose = true;

		if (mainChooseComparator == null) {
			mainChooseComparator = new MainChooseComparator();
		}
		mainChooseComparator.setCard(card);
		Collections.sort(cards, mainChooseComparator);

		notifyDataSetChanged();
		return card;
	}

	protected MainChooseComparator mainChooseComparator;

	protected class MainChooseComparator implements Comparator<CardSynthesis> {

		private CardSynthesis card;
		private int typeId;

		public void setCard(CardSynthesis card) {
			this.card = card;
			typeId = 0;
			if (card.characteristicList != null) {
				for (Characteristic characteristic : card.characteristicList) {
					if (characteristic.level < 10) {
						typeId = characteristic.typeId;
					}
				}
			}
		}

		@Override
		public int compare(CardSynthesis lhs, CardSynthesis rhs) {
			if (card.id == lhs.id) {
				return -1;
			}
			if (card.id == rhs.id) {
				return 1;
			}
			if (sameTypeId(lhs)) {
				if (sameTypeId(rhs)) {
					return 0;
				}
				return -1;
			}
			if (sameTypeId(rhs)) {
				return 1;
			}
			return compareTypeId(lhs, rhs);

		}

		protected boolean sameTypeId(CardSynthesis card) {
			return card.characteristicList != null
					&& card.characteristicList.length == 1
					&& card.characteristicList[0].typeId == typeId;
		}


		protected int compareTypeId(CardSynthesis lhs, CardSynthesis rhs) {
			if (lhs.characteristicList == null
					&& rhs.characteristicList == null) {
				return 0;
			}
			if (lhs.characteristicList.length == 1
					&& rhs.characteristicList.length == 1) {
				return lhs.characteristicList[0].typeId
						- rhs.characteristicList[0].typeId;
			}
			return lhs.level - rhs.level;
		}

	}

	public void cancelMainChoose() {
		isMainChoose = false;
		clearMainChoose();
		if (cancelChooseComparator == null) {
			cancelChooseComparator = new CancelChooseComparator();
		}
		Collections.sort(cards, cancelChooseComparator);
		notifyDataSetChanged();
	}

	protected CancelChooseComparator cancelChooseComparator;

	protected class CancelChooseComparator implements Comparator<CardSynthesis> {

		@Override
		public int compare(CardSynthesis lhs, CardSynthesis rhs) {
			if (lhs.isMaxLv()) {
				if (rhs.isMaxLv()) {
					return 0;
				}
				return 1;
			}
			if (rhs.isMaxLv()) {// 两个都是true
				return -1;
			}

			return rhs.level - lhs.level;
		}

	}

	private void clearMainChoose() {
		for (CardSynthesis card : cards) {
			card.isMainChoose = card.isChoose = false;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MSSynthesisViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_ms_synthesis, null);
			holder = new MSSynthesisViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (MSSynthesisViewHolder) convertView.getTag();
		}

		CardSynthesis card = cards.get(position);
		if (card != null) {
			holder.setBaseShow(card);
			if (isLock(card)) {
				holder.lock.setVisibility(View.VISIBLE);
			} else {
				holder.lock.setVisibility(View.INVISIBLE);
			}
		}
		return convertView;
	}

	protected boolean isLock(CardSynthesis card) {
		return isMainChoose ? card.isProtectLock() : card.isMaxLv();
	}

	public int getChooseCount() {
		int count = 0;
		for (CardSynthesis card : cards) {
			if (card.isChoose) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 
	 * @return key is base, values is materials
	 */
	public SimplePair<Integer, List<Integer>> getChoose() {
		if (!isMainChoose) {
			return null;
		}
		Integer base = null;
		ArrayList<Integer> materials = new ArrayList<Integer>();

		for (CardSynthesis card : cards) {
			if (card.isMainChoose) {
				if (card.isMaxLv()) {// 2次检查, 防止某些情况导致选择错误
					return null;
				}
				base = card.id;
			} else if (card.isChoose) {
				if (card.isProtectLock()) {
					return null;
				}
				materials.add(card.id);
			}

		}

		if (base == null) {
			return null;
		}
		if (materials.isEmpty()) {
			return null;
		}

		return new SimplePair<Integer, List<Integer>>(base, materials);
	}
}
