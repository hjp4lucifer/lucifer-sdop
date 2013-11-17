package cn.lucifer.sdop.domain;

public class Ms {
	public int id;

	public Card card;

	// AI扩展
	public int[] AIType;
	public int AITurn;
	public int lcf_attack;
	public int lcf_attack_buff;

	/**
	 * 获取对应的行动代号
	 * 
	 * @return
	 */
	public int getActionCode() {
		return AIType[AITurn];
	}
}
