package cn.lucifer.sdop;

import android.util.Log;

public class Auto extends LcfExtend {

	/**
	 * 针对{@link Sdop#clearAllJob()}后, 对自动项的恢复
	 */
	public void resume() {
		if (lcf().sdop.auto.setting.sneaking) {
			lcf().sdop.sneaking.resumeAutoSneaking();
		}
	}

	/**
	 * 针对重新登录后全部自动项的重新加载
	 */
	public void resumeAll() {
		resume();
		if (lcf().sdop.auto.setting.boss) {
			Log.i(lcf().LOG_TAG, "auto boss");
			switch (lcf().sdop.boss.currentType) {
			case 0:
				lcf().sdop.boss.AI.startAutoNormalRaidBoss();
				break;
			default:
				lcf().sdop.boss.AI.startAutoSuperRaidBoss();
				break;
			}
			return;
		}
		Log.d(lcf().LOG_TAG, "no auto boss");

		if (lcf().sdop.auto.setting.duel) {
			Log.d(lcf().LOG_TAG, "auto duel");
			lcf().sdop.duel.startAutoDuel();
			return;
		}
		Log.d(lcf().LOG_TAG, "no auto duel");
	}

	public Setting setting = new Setting();

	public class Setting {
		public boolean duel;
		public boolean boss;
		public boolean ep;
		/**
		 * true is auto choose leader MS card and leader pilot in raid boss.
		 * <ol>
		 * <li>Check current leader MS card</li>
		 * <li>If the MS card is six times damage, nothing to do.</li>
		 * <li>Else if the MS card is three times.
		 * <ol style="list-style-type: lower-alpha;">
		 * <li>Find attack and help pilot, and mark.</li>
		 * <li>In battle before start, find the member has six times damage MS
		 * card.</li>
		 * <li>If has six times damage MS, change pilot for help.</li>
		 * <li>In battle finished, change pilot for attack.</li>
		 * </ol>
		 * </li>
		 * <li>Else, find two times damage MS card in all. if none, nothing to
		 * do.</li>
		 * <li>If has two times damage MS card.
		 * <ol>
		 * <li>If leader card is two times damage MS card, don't mark the card.</li>
		 * <li>Else, mark the two times damage MS card, and current leader card.
		 * </li>
		 * <li>Mark the attack and help pilot.</li>
		 * <li>In battle before start, find the three or six times damage MS
		 * card.</li>
		 * <li>If has, go battle.</li>
		 * <li>Else, change attack pilot, and two times damage MS card, go
		 * battle. In battle finished, change the user choose pilot and MS card.
		 * </li>
		 * </ol>
		 * </li>
		 * </ol>
		 */
		public boolean cardPlatoon;

		public boolean sneaking;
	}

}
