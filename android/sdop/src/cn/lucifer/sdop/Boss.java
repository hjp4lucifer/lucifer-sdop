package cn.lucifer.sdop;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.ex.GetRaidBossOutlineList;
import cn.lucifer.sdop.dispatch.ex.InitRaidBossOutlineList;
import cn.lucifer.sdop.dispatch.ex.PostRaidBossBattleEntry;

public class Boss extends LcfExtend {

	private JSONObject currentType;

	public final int x3 = 250037;
	public final int x6 = 250038;

	public final AI AI = new AI();

	public JSONObject getCurrentType() {
		if (currentType == null) {
			try {
				currentType = lcf().sdop.loadJsonObject("boss_super.json");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return currentType;
	}

	private JSONObject initRaidBossOutlineList_args;

	private JSONObject getInitRaidBossOutlineList_args() {
		if (initRaidBossOutlineList_args == null) {
			try {
				initRaidBossOutlineList_args = lcf().sdop
						.loadJsonObject("initRaidBossOutlineList_args.json");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return initRaidBossOutlineList_args;
	}

	public void initRaidBossOutlineList(String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForRaidBossList/initRaidBossOutlineList?ssid="
				+ lcf().sdop.ssid;

		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					"initRaidBossOutlineList",
					getInitRaidBossOutlineList_args());

			lcf().sdop.post(url, payload.toString(),
					InitRaidBossOutlineList.procedure, callback);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void postRaidBossBattleEntry(int bossId, String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForRaidBossList/postRaidBossBattleEntry";

		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					"getRaidBossOutlineList", new JSONObject()
							.put("id", bossId).put("isChargeBp", false));
			lcf().sdop.post(url, payload.toString(),
					PostRaidBossBattleEntry.procedure,
					PostRaidBossBattleEntry.procedure);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getRaidBossOutlineList(String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForRaidBossList/getRaidBossOutlineList";
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					"getRaidBossOutlineList", lcf().sdop.boss.getCurrentType());
			lcf().sdop.post(url, payload.toString(),
					GetRaidBossOutlineList.procedure, callback);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 自动超总
	 */
	public void autoSuperRaidBoss() {
		if (!lcf().sdop.auto.setting.boss) {
			return;
		}
		getRaidBossOutlineList(GetRaidBossOutlineList.procedure);
	}

}
