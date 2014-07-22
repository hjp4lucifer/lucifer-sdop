package cn.lucifer.sdop.domain;

/**
 * 潜入目标地图, 真正的请求参数
 * 
 * @author Lucifer
 * 
 */
public class SneakingMapDestination {

	/**
	 * <ul>
	 * <li>SPACE</li>
	 * <li>LAND</li>
	 * </ul>
	 */
	public Value areaType;
	public int destinationId;
	public String destinationName;
	public boolean isRelease;
	public boolean isSecret;
	/**
	 * 完成度, 100表示100%
	 */
	public int progress;
	/**
	 * 需要时间, 单位: 秒
	 */
	public int requiredTime;
	public String report;
	/**
	 * 对应的探索小队id, null表明无队伍
	 */
	public Integer sortieplatoonId;
}
