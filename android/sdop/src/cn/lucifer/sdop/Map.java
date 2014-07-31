package cn.lucifer.sdop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.ex.DrawChancePanel;
import cn.lucifer.sdop.dispatch.ex.EncountRaidBoss;
import cn.lucifer.sdop.dispatch.ex.ExecuteQuest;
import cn.lucifer.sdop.dispatch.ex.GetQuestData;
import cn.lucifer.sdop.domain.ChancePanelReward;
import cn.lucifer.sdop.domain.HeaderDetail;
import cn.lucifer.sdop.domain.NodeOutline;
import cn.lucifer.sdop.domain.Player;

/**
 * 
 * @author Lucifer
 * 
 */
public class Map extends LcfExtend {

	private Integer nodeId;
	private boolean isEventMap;

	public void clearNodeId() {
		this.nodeId = null;
	}

	/**
	 * 设置是否特殊任务, 每次设置, 若不同, 则地图信息会被清空
	 * 
	 * @param isEventMap
	 */
	public void setEventMap(boolean isEventMap) {
		if (this.isEventMap != isEventMap) {
			this.isEventMap = isEventMap;
			clearNodeId();
		}
	}

	public boolean isEventMap() {
		return isEventMap;
	}

	public void getQuestData() {
		if (nodeId != null) {
			executeQuest(nodeId);
			return;
		}
		String url = lcf().sdop.httpUrlPrefix + "/GetForQuestMap/getQuestData?"
				+ lcf().sdop.createGetParams() + "&isEventMap=" + isEventMap
				+ "&nodeId=0";
		lcf().sdop.get(url, GetQuestData.procedure, null);
	}

	final String[] nodeTypes = { "WARP", "NORMAL", "BOSS" };

	public int processEventQuestData(int playerExist,
			NodeOutline[] nodeOutlineList) {
		NodeOutline nodeOutline;
		for (int i = 0, len = nodeOutlineList.length; i < len; i++) {
			nodeOutline = nodeOutlineList[i];
			// Log.d(lcf().LOG_TAG, nodeOutline.nodeId + " : "
			// + nodeOutline.nodeType.value);
			if (playerExist == nodeOutline.nodeId) {
				if (nodeTypes[0].equals(nodeOutline.nodeType.value)) {
					if (i < len - 1) {// 非最后
						playerExist++;
						continue;
					}
					// else 准备去下一个地图
				} else if (nodeOutline.progress == 100) {
					playerExist++;
					continue;
				}
			}
		}
		return playerExist;
	}

	public void executeQuest(int nodeId) {
		this.nodeId = nodeId;
		String url = lcf().sdop.httpUrlPrefix + "/PostForQuestMap/executeQuest";
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					ExecuteQuest.procedure,
					new JSONObject().put("nodeId", nodeId)
							.put("renderingIdList", new JSONArray())
							.put("isEventMap", isEventMap)
							.put("isAutoProgress", true));
			lcf().sdop.post(url, payload.toString(), ExecuteQuest.procedure,
					null);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	final String[] renderingTypes = { "DUEL_EFFECT", "ENCOUNT_EFFECT",
			"RAID_BOSS_EFFECT", "NODE_COMPLETE", "AREA_CLEAR_EFFECT",
			"EVENT_MAP_CHANCE_PANEL" };

	/**
	 * 因任务探索的情况太多, 在此进行处理分支
	 * 
	 * @param args
	 * @throws JSONException
	 */
	public void executeQuestResultProcess(JSONObject args) throws JSONException {
		JSONArray renderingList = args.getJSONArray("renderingList");
		JSONObject rendering;
		String renderingType4Log = null;
		String renderingType;
		int _oldNodeId = nodeId;
		for (int i = 0, len = renderingList.length(); i < len; i++) {
			rendering = renderingList.getJSONObject(i);
			renderingType = rendering.getJSONObject("renderingType").getString(
					"value");
			// Log.i(lcf().LOG_TAG, "renderingType : " + renderingType);
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
			} else if (isEventMap && i == 0
					&& renderingTypes[3].equals(renderingType)) {
				// 特殊任务 and 小地图完成
				renderingType4Log = renderingType;
				nodeId++;
				// 不进行return
			} else if (renderingTypes[5].equals(renderingType)) {// 理论上event才会触发
				ChancePanelReward[] chancePanelRewardList = lcf().gson
						.fromJson(rendering.getString("chancePanelRewardList"),
								ChancePanelReward[].class);
				drawChancePanel(chancePanelRewardList, GetQuestData.procedure);
				return;
			}

			// if (renderingType.indexOf("EFFECT") > -1) {
			// Log.d(lcf().LOG_TAG, args.toString());
			// }
			// else 忽略
			if (renderingType4Log == null) {
				renderingType4Log = renderingType;
			}
			if (!rendering.isNull("headerDetail")) {
				HeaderDetail headerDetail = lcf().gson
						.fromJson(rendering.getString("headerDetail"),
								HeaderDetail.class);
				lcf().sdop.bp = headerDetail.bpDetail.currentValue;
				lcf().sdop.ep = headerDetail.energyDetail.energy;
			}
		}
		StringBuilder log = new StringBuilder(isEventMap ? "特殊任务: " : "普通探索: ");
		log.append(renderingType4Log).append(", nodeId: ").append(_oldNodeId);
		log.append(", 剩余ep: ").append(lcf().sdop.ep);
		log.append(", 剩余bp: ").append(lcf().sdop.bp);
		lcf().sdop.log(log.toString());
		if (lcf().sdop.ep > 6) {
			lcf().sdop.checkCallback(GetQuestData.procedure, 2000, null);
			return;
		}
		if (lcf().sdop.auto.setting.boss && lcf().sdop.ep > 2) {
			lcf().sdop.checkCallback(GetQuestData.procedure, 2000, null);
			return;
		}
	}

	public void drawChancePanel(ChancePanelReward[] chancePanelRewardList,
			String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForQuestMap/drawChancePanel?ssid=" + lcf().sdop.ssid;
		int chooseId = 1;
		int maxPP = 0;

		StringBuilder log = new StringBuilder("特殊任务抽奖！");

		String chanceInfo;
		for (ChancePanelReward chancePanelReward : chancePanelRewardList) {
			// Log.d(lcf().LOG_TAG, "id : " + chancePanelReward.id
			// + " , userName : " + chancePanelReward.userName + " , pp :"
			// + chancePanelReward.pp);
			chanceInfo = chancePanelReward.getInfo();
			if (chanceInfo != null) {
				log.append("<br>　　id: ").append(chancePanelReward.id).append(", ")
						.append(chanceInfo);
			}
			if (chancePanelReward.pp > maxPP) {// 选择最多PP的那一项
				chooseId = chancePanelReward.id;
				maxPP = chancePanelReward.pp;
				continue;
			}
			if (maxPP == 0) {
				if (chancePanelReward.userName == null
						|| chancePanelReward.userName.length() == 0) {// 选择没有人选择过的一项
					chooseId = chancePanelReward.id;
				}
			}
		}
		log.append("　　AI选择id: ").append(chooseId);
		lcf().sdop.log(log.toString());
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					DrawChancePanel.procedure,
					new JSONObject().put("id", chooseId));
			lcf().sdop.post(url, payload.toString(), DrawChancePanel.procedure,
					callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
