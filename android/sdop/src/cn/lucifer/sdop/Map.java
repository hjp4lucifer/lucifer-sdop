package cn.lucifer.sdop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.ex.EncountRaidBoss;
import cn.lucifer.sdop.dispatch.ex.ExecuteQuest;
import cn.lucifer.sdop.dispatch.ex.GetForQuestMap;
import cn.lucifer.sdop.domain.HeaderDetail;
import cn.lucifer.sdop.domain.Player;

/**
 * 
 * @author Lucifer
 * 
 */
public class Map extends LcfExtend {

	private Integer nodeId;

	public void getQuestData() {
		if (nodeId != null) {
			executeQuest(nodeId);
			return;
		}
		String url = lcf().sdop.httpUrlPrefix + "/GetForQuestMap/getQuestData?"
				+ lcf().sdop.createGetParams() + "&isEventMap=false&nodeId=0";
		lcf().sdop.get(url, GetForQuestMap.procedure, null);
	}

	public void executeQuest(int nodeId) {
		this.nodeId = nodeId;
		String url = lcf().sdop.httpUrlPrefix + "/PostForQuestMap/executeQuest";
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					ExecuteQuest.procedure,
					new JSONObject().put("nodeId", nodeId)
							.put("renderingIdList", new JSONArray())
							.put("isEventMap", false)
							.put("isAutoProgress", false));
			lcf().sdop.post(url, payload.toString(), ExecuteQuest.procedure,
					null);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	final String[] renderingTypes = { "DUEL_EFFECT", "ENCOUNT_EFFECT",
			"RAID_BOSS_EFFECT" };

	/**
	 * 因任务探索的情况太多, 在此进行处理分支
	 * 
	 * @param args
	 * @throws JSONException
	 */
	public void executeQuestResultProcess(JSONObject args) throws JSONException {
		JSONArray renderingList = args.getJSONArray("renderingList");
		JSONObject rendering;
		String renderingType = null;
		for (int i = 0, len = renderingList.length(); i < len; i++) {
			rendering = renderingList.getJSONObject(i);
			renderingType = rendering.getJSONObject("renderingType").getString(
					"value");
			if (renderingTypes[0].equals(renderingType)) {// DUEL_EFFECT
				lcf().sdop.log("GB遭遇战！");
				JSONObject duelEncountDetail = rendering
						.getJSONObject("duelEncountDetail");
				Player enemy = lcf().gson.fromJson(
						duelEncountDetail.getString("enemyInfo"), Player.class);
				lcf().sdop.duel.executeDuelBattle(enemy, true);
				return;
			} else if (renderingTypes[1].equals(renderingType)) {// ENCOUNT_EFFECT
				lcf().sdop.boss.encountType = 2;
				lcf().sdop.log("普通遭遇战！");
				lcf().sdop.boss.getBattleData(EncountRaidBoss.procedure);
				return;
			} else if (renderingTypes[2].equals(renderingType)) {// RAID_BOSS_EFFECT
				lcf().sdop.boss.encountType = 0;
				lcf().sdop.log("总力遭遇战！");
				lcf().sdop.boss.getRaidBossBattleData(0,
						EncountRaidBoss.procedure);
				return;
			}// 缺超总遭遇
				// else 忽略
			if (!rendering.isNull("headerDetail")) {
				HeaderDetail headerDetail = lcf().gson
						.fromJson(rendering.getString("headerDetail"),
								HeaderDetail.class);
				lcf().sdop.bp = headerDetail.bpDetail.currentValue;
				lcf().sdop.ep = headerDetail.energyDetail.energy;
			}
		}
		lcf().sdop.log("普通探索: " + renderingType + ", 剩余ep: " + lcf().sdop.ep
				+ ", 剩余bp: " + lcf().sdop.bp);
		if (lcf().sdop.ep > 7) {
			getQuestData();
		}
	}
}
