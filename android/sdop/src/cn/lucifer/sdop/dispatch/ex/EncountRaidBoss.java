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
		Log.i(lcf().LOG_TAG, "EncountRaidBoss callback start ! encountType : "
				+ lcf().sdop.boss.encountType);
		try {
			switch (lcf().sdop.boss.encountType) {
			case 0:// 总力 or 超总
				JSONObject raidBossData = battleArgs
						.getJSONObject("raidBossData");
				int raidBossId = raidBossData.getInt("id");
				String raidBossDataKind = raidBossData.getJSONObject("kind")
						.getString("value");
				int raidBossLv = raidBossData.getInt("level");
				if (!"NORMAL".equals(raidBossDataKind)) {// 超总
					lcf().sdop.log("遭遇变更! 超总遭遇(" + raidBossDataKind + "), Lv :"
							+ raidBossLv);
					lcf().sdop.boss.targetBossId = raidBossId;
					lcf().sdop.boss.encountType = 1;
					lcf().sdop.boss.AI.setFixMember(battleArgs, true);
					lcf().sdop.item.equipItem4Sp(EquipItem4Sp.procedure);
					return;
				}
				lcf().sdop.log("总力遭遇(" + raidBossDataKind + "), Lv :"
						+ raidBossLv);
				CardWithoutWeapon[] members = lcf().sdop.boss.AI.setFixMember(
						battleArgs, false);

				lcf().sdop.boss.executeBattleStart(members,
						lcf().sdop.boss.battleId,
						lcf().sdop.boss.getCurrentMode(), true, null);
				// 这里进行并发请求
				lcf().sdop.checkCallback(SendRescueSignal.procedure, 2000,
						new Object[] { raidBossId });
				// lcf().sdop.boss.sendRescueSignal(raidBossId, null);
				return;
			case 2:// 普通战斗, 不使用member了, 避免全部进入coolTime
				lcf().sdop.myUserId = battleArgs.getInt("leaderCardId");
				lcf().sdop.boss.executeBattleStart(null,
						battleArgs.getInt("battleId"),
						battleArgs.getJSONObject("mode"), true, null);
				if (lcf().sdop.map.isEventMap()) {// 因为是赶路模式, 所以需要重新判断进度
					lcf().sdop.map.clearNodeId();
				}
				return;
			default:
				break;
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
