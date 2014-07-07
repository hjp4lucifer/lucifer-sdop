package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.Boss;

public class GetRaidBossField extends BaseDispatch {

	public static final String procedure = "cn.lucifer.GetRaidBossSpace";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		if (args.isNull("list")) {
			lcf().sdop.log("无法获取boss信息！");
			return;
		}
		Boss[] bosses = lcf().gson.fromJson(args.getString("list"),
				Boss[].class);
		if (bosses == null || bosses.length == 0) {
			lcf().sdop.log("无法获取boss信息！");
			return;
		}

		lcf().sdop.boss.setRaidBossField(bosses[0].field.value);

		lcf().sdop.checkCallback(callback);
	}

	/**
	 * 获取甲板信息
	 */
	@Override
	public void callback(Object[] args) {
		lcf().sdop.cardPlatoon.getCardPlatoonData(GetCardPlatoonData.procedure);
	}

}
