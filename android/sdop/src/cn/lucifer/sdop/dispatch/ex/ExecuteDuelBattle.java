package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.Ms;

public class ExecuteDuelBattle extends BaseDispatch {
	public static final String procedure = "executeDuelBattle";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			//lcf().sdop.auto.setting.duel = false;
			return;
		}
		JSONObject data = args.getJSONObject("data");

		String result = args.getJSONObject("result").getBoolean("isWin") ? lcf().sdop
				.getRedMsg("胜利") : lcf().sdop.getBlueMsg("失败");
		StringBuffer logMsg = new StringBuffer();
		logMsg.append("挑战【")
				.append(data.getJSONObject("enemyData").getString("name"))
				.append("】 ");
		logMsg.append(result);
		logMsg.append("! 对方MS阵容：");

		Ms[] enemyMsList = lcf().gson.fromJson(data.getString("enemyMsList"),
				Ms[].class);

		logMsg.append(lcf().sdop.ms.logMsList(enemyMsList));

		lcf().sdop.log(logMsg.toString());

		logMsg = null;
	}

	@Override
	public void callback(Object[] args) {
		// TODO Auto-generated method stub

	}

}
