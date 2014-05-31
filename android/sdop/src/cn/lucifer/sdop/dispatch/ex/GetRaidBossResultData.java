package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.Award;

public class GetRaidBossResultData extends BaseDispatch {

	public static final String procedure = "getRaidBossResultData";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}

		Award award = lcf().gson.fromJson(args.getString("award"), Award.class);
		String logMsg = String.format("获得%dGP，当前GP：%d。获得【%s】抽取机会【%d】次！",
				award.awardGp, award.currentGp, award.kind.value, award.num);
		lcf().sdop.log(logMsg);
		lcf().sdop.checkCallback(callback, 100, new Object[] { award });
	}

	@Override
	public void callback(Object[] args) {
		Award award = (Award) args[0];
		if (award != null && award.num > 0) {
			lcf().sdop.boss.raidBossGacha(
					lcf().sdop.boss.currentGachaRaidBossId,
					DeleteRaidBossList.procedure);
		} else {
			lcf().sdop.boss.deleteRaidBossList(
					lcf().sdop.boss.currentGachaRaidBossId, AutoRaidBossResult.procedure);
		}
	}
}
