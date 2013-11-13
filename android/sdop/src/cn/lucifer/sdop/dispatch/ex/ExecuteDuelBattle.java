package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class ExecuteDuelBattle extends BaseDispatch {
	public static final String procedure = "executeDuelBattle";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		JSONObject data = args.getJSONObject("data");

		String result = args.getJSONObject("result").getBoolean("isWin") ? lcf().sdop
				.getRedMsg("胜利") : lcf().sdop.getBlueMsg("失败");
		StringBuffer logMsg = new StringBuffer();
		logMsg.append("挑战【");
		logMsg.append(data.getJSONObject("enemyData").getString("name"));
		logMsg.append(result);
		logMsg.append("！对方MS阵容：");

		String enemyMsList = data.getString("enemyMsList");
	}

}
