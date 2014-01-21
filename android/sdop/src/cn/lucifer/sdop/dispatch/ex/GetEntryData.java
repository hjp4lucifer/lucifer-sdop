package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.Player;

public class GetEntryData extends BaseDispatch {

	public static final String procedure = "getEntryData";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			// lcf().sdop.auto.setting.duel = false;
			return;
		}

		Player[] enemyList = lcf().gson.fromJson(args.getString("list"),
				Player[].class);
		lcf().sdop.checkCallback(callback, enemyList);
	}

	@Override
	public void callback(Object[] args) {
		Player[] enemyList = (Player[]) args;
		lcf().sdop.duel.findEnemyAndBattle(enemyList);
	}

}
