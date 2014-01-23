package cn.lucifer.sdop.domain.args;

import cn.lucifer.sdop.domain.Value;

public class ExecuteActionCommandArgs {

	public int battleId;
	public Value actionType;
	public Value mode;
	public int targetId;
	public int playerId;
	public boolean isAutoBattle;
	public int actionId;
	/**
	 * @time 2014/1/23更新后新增
	 */
	public boolean isRaidBossChargeAttack;

	public ExecuteActionCommandArgs(int battleId, String actionTypeValue,
			String modeValue, int targetId, int playerId, int actionId) {
		super();
		this.battleId = battleId;
		this.actionType = new Value(actionTypeValue);
		this.mode = new Value(modeValue);
		this.targetId = targetId;
		this.playerId = playerId;
		this.actionId = actionId;
	}

}
