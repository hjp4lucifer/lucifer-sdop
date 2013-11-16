package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class GetRaidBossBattleData extends BaseDispatch {
	public static final String procedure = "getRaidBossBattleData";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}

		// var battleId = data.args.battleId;
		// var members = data.args.memberCardList;
		lcf().sdop.checkCallback(callback, new Object[] { args });
	}

	@Override
	public void callback(Object[] args) {
		JSONObject battleArgs = (JSONObject) args[0];
		try {
			lcf().sdop.boss.AI.setFixMember(battleArgs);
			lcf().sdop.equipItem4Sp(EquipItem4Sp.procedure);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
