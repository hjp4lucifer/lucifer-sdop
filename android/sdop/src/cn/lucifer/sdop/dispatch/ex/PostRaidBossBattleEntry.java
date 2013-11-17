package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class PostRaidBossBattleEntry extends BaseDispatch {

	public static final String procedure = "postRaidBossBattleEntry";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			String message = args.getString("message");
			if (message.startsWith("BPが不足しています")) {
				return;
			}
			failCallback();
			return;
		}
		lcf().sdop.log("进入boss成功！");
		lcf().sdop.checkCallback(callback);
	}

	@Override
	public void callback(Object[] args) {
		lcf().sdop.boss.getRaidBossBattleData(GetRaidBossBattleData.procedure);
	}

	public void failCallback() {
		lcf().sdop.checkCallback(AutoSuperRaidBoss.procedure, null);
	}

}
