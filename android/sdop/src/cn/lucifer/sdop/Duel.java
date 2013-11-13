package cn.lucifer.sdop;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import cn.lucifer.sdop.dispatch.ex.ExecuteDuelBattle;
import cn.lucifer.sdop.dispatch.ex.GetDuelData;
import cn.lucifer.sdop.dispatch.ex.GetEntryData;

public class Duel extends LcfExtend {
	public String targetUnitAttribute = "FIGHT";

	public void getEntryData(String callback) {
		String url = lcf().sdop.httpUrlPrefix + "/GetForDuel/getEntryData?"
				+ lcf().sdop.createGetParams();
		lcf().sdop.get(url, GetEntryData.procedure, callback);
	}

	public void getDuelData(String callback) {
		String url = lcf().sdop.httpUrlPrefix + "/GetForDuel/getDuelData?"
				+ lcf().sdop.createGetParams();
		lcf().sdop.get(url, GetDuelData.procedure, callback);
	}

	public void executeDuelBattle(int targetId) {
		if (targetId < 100) {
			Log.e("Lucifer", "executeDuelBattle targetId is null !");
			return;
		}

		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForQuestBattle/executeDuelBattle?ssid="
				+ lcf().sdop.ssid;

		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					ExecuteDuelBattle.procedure,
					new JSONObject().put("isEncount", false)
							.put("id", targetId));
			lcf().sdop.post(url, payload.toString(),
					ExecuteDuelBattle.procedure, null);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void checkAndExecute(){
		getDuelData(GetDuelData.procedure);
	}
}
