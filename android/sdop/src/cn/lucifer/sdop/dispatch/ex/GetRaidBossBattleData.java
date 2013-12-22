package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class GetRaidBossBattleData extends BaseDispatch {
	public static final String procedure = "getRaidBossBattleData";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}

		Log.i("Lucifer", "GetRaidBossBattleData callback : " + callback);

		// var battleId = data.args.battleId;
		// var members = data.args.memberCardList;
		lcf().sdop.checkCallback(callback, new Object[] { args });
	}

	@Override
	public void callback(Object[] args) {
		JSONObject battleArgs = (JSONObject) args[0];
		Log.i("Lucifer", "GetRaidBossBattleData callback start ! ");
		try {
			lcf().sdop.boss.AI.setFixMember(battleArgs);
			switch (lcf().sdop.boss.currentType) {
			case 0:
				lcf().sdop.boss.executeBattleStart(null);
				return;

			default:
				break;
			}

			lcf().sdop.equipItem4Sp(EquipItem4Sp.procedure);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
