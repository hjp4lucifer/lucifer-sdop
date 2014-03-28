package cn.lucifer.sdop.domain;

public class CardSynthesis extends Card {

	public String name;

	public boolean isDuelDeck;

	public boolean isProtect;

	/**
	 * 是否选中, lcf扩展
	 */
	public boolean isSelect;

	/**
	 * 
	 * @return true表示满级
	 */
	public boolean isMaxLv() {
		if (nextExp == 0) {
			for (Characteristic c : characteristicList) {
				if (c.level < 10) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
