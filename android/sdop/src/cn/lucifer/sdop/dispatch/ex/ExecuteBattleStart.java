package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.Item;
import cn.lucifer.sdop.domain.Ms;

public class ExecuteBattleStart extends BaseDispatch {
	public static final String procedure = "executeBattleStart";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		lcf().sdop.log("boss战开始！是否自动：" + lcf().sdop.boss.isAutoBattle);
		lcf().sdop.checkCallback(callback, new Object[] { args });
	}

	@Override
	public void callback(Object[] args) {
		JSONObject battleArgs = (JSONObject) args[0];
		try {
			lcf().sdop.boss.AI.fixPlayerMsListAI(battleArgs);
			lcf().sdop.log("对Boss选择阵容："
					+ lcf().sdop.ms.logMsList(lcf().sdop.boss.AI.playerMsList));
			lcf().sdop.boss.AI.itemList = lcf().gson.fromJson(
					battleArgs.getString("itemList"), Item[].class);
			
			Ms[] playerMsList = lcf().sdop.boss.AI.playerMsList;
			lcf().sdop.currentSp = playerMsList[0].card.currentSp + playerMsList[0].card.currentSubSp;
			playerMsList = null;
			
			lcf().sdop.boss.AI.autoBattle(battleArgs);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
