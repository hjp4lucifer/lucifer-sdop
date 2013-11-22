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

	public void checkAndExecute() {
		if (!lcf().sdop.auto.setting.duel) {
			return;
		}
		getDuelData(GetDuelData.procedure);
	}

	public void startAutoDuel() {
		lcf().sdop.clearAllJob();
		lcf().sdop.auto.setting.duel = true;
		lcf().sdop.log("针对【" + targetUnitAttribute + "】的自动GB开始！");
		lcf().sdop.startJob(new Runnable() {
			@Override
			public void run() {
				Log.i("Lucifer", "autoDuel ----------");
				lcf().sdop.duel.checkAndExecute();
			}
		}, 0, 300000);
	}

	public void cancelAutoDuel() {
		lcf().sdop.auto.setting.duel = false;
		lcf().sdop.clearAllJob();
		lcf().sdop.log("自动GB停止成功！");
	}
}
