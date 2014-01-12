package cn.lucifer.sdop.domain;

public class SneakingPlatoon {

	/**
	 * 最大使用cost
	 */
	public int cost;

	public String destinationName;

	public int[] msCardIdList;
	public String platoonName;
	public String report;
	/**
	 * 剩余second
	 */
	public int requiredTime;
	/**
	 * <ul>
	 * <li>ATTACK</li>
	 * <li>RETURN</li>
	 * <li>WAIT</li>
	 * </ul>
	 */
	public Value state;

}
