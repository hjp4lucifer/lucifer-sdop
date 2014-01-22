package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.Ms;
import cn.lucifer.sdop.e.CannotOpenDBException;

public class ExecuteDuelBattle extends BaseDispatch {
	public static final String procedure = "executeDuelBattle";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			// lcf().sdop.auto.setting.duel = false;
			return;
		}
		JSONObject data = args.getJSONObject("data");

		boolean isWin = args.getJSONObject("result").getBoolean("isWin");
		String result;
		if (isWin) {
			result = lcf().sdop.getRedMsg("胜利");
		} else {
			result = lcf().sdop.getBlueMsg("失败");
		}
		StringBuffer logMsg = new StringBuffer();

		//Log.i("Lucifer", data.getJSONObject("enemyData").toString());
		JSONObject enemyData = data.getJSONObject("enemyData");
		String name = enemyData.getString("name");
		String unitAttribute = enemyData.getJSONObject("unitAttribute")
				.getString("value");
		logMsg.append("挑战").append(enemyData.getString("rankName")).append("【")
				.append(name).append("】 ");
		logMsg.append(result);
		logMsg.append("! 对方MS阵容：");

		Ms[] enemyMsList = lcf().gson.fromJson(data.getString("enemyMsList"),
				Ms[].class);

		logMsg.append(lcf().sdop.ms.logMsList(enemyMsList));

		lcf().sdop.log(logMsg.toString());

		logMsg = null;
		try {
			lcf().sdop.duel.updateDuelRecord(isWin, name, unitAttribute);
		} catch (CannotOpenDBException e) {
			// 无需处理
		}
	}

	@Override
	public void callback(Object[] args) {
		// TODO Auto-generated method stub

	}

}
