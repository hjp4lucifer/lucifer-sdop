package cn.lucifer.sdop;

public class Auto {

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
	}

}
