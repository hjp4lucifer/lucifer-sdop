package cn.lucifer.sdop.domain;

public class Unit {
	public int id;
	public Value arrangement;
	public boolean isUseChargePoint;
	public boolean isNpc;
	public boolean isLeader;
	public boolean isUseGoldPoint;

	public void setArrangementValue(int key) {
		arrangement = new Value();
		switch (key) {
		case 0:
			arrangement.value = "MIDDLE_FRONT";
			break;
		case 1:
			arrangement.value = "BOTTOM_FRONT";
			break;
		case 2:
			arrangement.value = "TOP_FRONT";
			break;

		default:
			break;
		}
	}
}
