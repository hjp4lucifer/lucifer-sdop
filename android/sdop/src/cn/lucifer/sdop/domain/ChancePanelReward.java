package cn.lucifer.sdop.domain;

public class ChancePanelReward {
	public int id;

	public int pp;

	public int gp;

	public Integer msCardId;

	public String userName;

	public String getInfo() {
		if (msCardId != null) {
			return "msCardId: " + msCardId;
		}
		if (pp > 0) {
			return "pp: " + pp;
		}
		if (gp > 0) {
			return "gp: " + gp;
		}
		return null;
	}

}
