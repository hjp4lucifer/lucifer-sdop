package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.CardSynthesis;

public class RaidBossGacha extends BaseDispatch {

	public static final String procedure = "raidBossGacha";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		int restGacha = args.getInt("restGacha");
		CardSynthesis card = lcf().gson.fromJson(args.getString("result"),
				CardSynthesis.class);
		StringBuilder logMsg = new StringBuilder("获得: ");
		if (card.rarity > 2) {
			logMsg.append(lcf().sdop.getRedBoldMsg(card.rarity + "c"
					+ card.cost));
		} else {
			logMsg.append(card.rarity).append("c").append(card.cost);
		}
		logMsg.append("【").append(card.name).append("】, ");
		lcf().sdop.ms.current++;

		logMsg.append(lcf().sdop.ms.current).append("/")
				.append(lcf().sdop.ms.max).append("，");

		if (lcf().sdop.ms.current >= lcf().sdop.ms.max) {
			logMsg.append("MS数量已满, 停止自动!");
			lcf().sdop.log(logMsg.toString());
			return;
		}
		if (restGacha > 0) {
			logMsg.append("还有").append(restGacha).append("次!");
			lcf().sdop.log(logMsg.toString());

			lcf().sdop.checkCallback(procedure, 100, new Object[] {
					lcf().sdop.boss.currentGachaRaidBossId, callback });

			return;
		}

		logMsg.append("该次已经抽完!");
		lcf().sdop.log(logMsg.toString());
		lcf().sdop.checkCallback(callback);
	}

	@Override
	public void callback(Object[] args) {
		int raidBossId = (Integer) args[0];
		String callback = (String) args[1];
		lcf().sdop.boss.raidBossGacha(raidBossId, callback);
	}
}
