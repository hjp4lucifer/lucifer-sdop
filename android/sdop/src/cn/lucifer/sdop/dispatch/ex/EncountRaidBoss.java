package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.CardWithoutWeapon;

public class EncountRaidBoss extends BaseDispatch {
	public static final String procedure = "cn.lucifer.EncountRaidBoss";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		// TODO Auto-generated method stub

	}

	@Override
	public void callback(Object[] args) {
		JSONObject battleArgs = (JSONObject) args[0];
		Log.i("Lucifer", "EncountRaidBoss callback start ! encountType : "
				+ lcf().sdop.boss.encountType);
		System.out.println(battleArgs.toString());
		try {
			switch (lcf().sdop.boss.encountType) {
			case 0:// 总力
				CardWithoutWeapon[] members = lcf().sdop.boss.AI.setFixMember(
						battleArgs, false);
				lcf().sdop.boss.executeBattleStart(members,
						lcf().sdop.boss.battleId,
						lcf().sdop.boss.getCurrentMode(), true, null);
				return;
			case 2:// 普通战斗, 不使用member了, 避免全部进入coolTime
				lcf().sdop.myUserId = battleArgs.getInt("leaderCardId");
				lcf().sdop.boss.executeBattleStart(null,
						battleArgs.getInt("battleId"),
						battleArgs.getJSONObject("mode"), true, null);
				return;
			default:// 超总
				lcf().sdop.boss.AI.setFixMember(battleArgs, true);
				break;
			}

			lcf().sdop.equipItem4Sp(EquipItem4Sp.procedure);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
