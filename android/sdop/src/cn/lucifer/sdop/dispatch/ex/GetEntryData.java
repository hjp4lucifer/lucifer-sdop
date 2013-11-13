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
			lcf().sdop.auto.setting.duel = false;
			return;
		}

		Player[] entryList = lcf().gson.fromJson(args.getString("list"),
				Player[].class);
		lcf().sdop.checkCallback(callback, entryList);
	}

	@Override
	public void callback(Object[] args) {
		Player[] entryList = (Player[]) args;
		int targetId = 0;
		Player entry;
		for (int i = 0; i < entryList.length; i++) {
			entry = entryList[i];
			if (entry.unitAttribute.value
					.equals(lcf().sdop.duel.targetUnitAttribute)) {
				targetId = entry.playerId;
				lcf().sdop.log("准备对【" + entry.playerName + "】发起挑战，对方属性是【"
						+ entry.unitAttribute.value + "】" + entry.unitName
						+ "！");
				break;
			}
		}
		entryList = null;
		lcf().sdop.duel.executeDuelBattle(targetId);
	}

}
