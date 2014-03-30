package cn.lucifer.sdop.domain;

public class CardSynthesis extends Card {

	public String name;

	public boolean isDuelDeck;

	public boolean isProtect;

	/**
	 * 是否选中, lcf扩展
	 */
	public boolean isChoose;

	/**
	 * 是否作为合并选中, lcf扩展
	 */
	public boolean isMainChoose;

	/**
	 * 反转选择
	 */
	public void reverseChoose() {
		isChoose = !isChoose;
	}

	/**
	 * 
	 * @return true表示满级
	 */
	public boolean isMaxLv() {
		if (level == 1) {// 个人需求, lv1的不升级
			return true;
		}
		if (characteristicList == null) {
			return true;
		}
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

	/**
	 * 
	 * @return true受保护, 不被合并
	 */
	public boolean isProtectLock() {
		if (level > 1) {
			return true;
		}
		if (rarity > 2) {
			return true;
		}
		return isProtect;
	}
}
