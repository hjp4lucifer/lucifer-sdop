package cn.lucifer.sdop;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import cn.lucifer.sdop.dispatch.ex.DeleteRaidBossList;
import cn.lucifer.sdop.dispatch.ex.ExecuteActionCommand;
import cn.lucifer.sdop.dispatch.ex.ExecuteBattleStart;
import cn.lucifer.sdop.dispatch.ex.GetBattleData;
import cn.lucifer.sdop.dispatch.ex.GetRaidBossBattleData;
import cn.lucifer.sdop.dispatch.ex.GetRaidBossOutlineList;
import cn.lucifer.sdop.dispatch.ex.GetRaidBossOutlineList4Finish;
import cn.lucifer.sdop.dispatch.ex.GetRaidBossResultData;
import cn.lucifer.sdop.dispatch.ex.InitRaidBossOutlineList;
import cn.lucifer.sdop.dispatch.ex.PostRaidBossBattleEntry;
import cn.lucifer.sdop.dispatch.ex.RaidBossGacha;
import cn.lucifer.sdop.dispatch.ex.SendRescueSignal;
import cn.lucifer.sdop.domain.CardWithoutWeapon;
import cn.lucifer.sdop.domain.Unit;
import cn.lucifer.sdop.domain.Value;
import cn.lucifer.sdop.domain.args.ExecuteActionCommandArgs;

public class Boss extends LcfExtend {

	private JSONObject _normal;
	private JSONObject _super;
	/**
	 * 0表示总力, 1表示超总
	 */
	public int currentType = 1;
	/**
	 * 0表示总力, 1表示超总, 2表示普通遭遇战
	 */
	public Integer encountType;
	private final String currentModeValue = "RAID_BOSS";
	private JSONObject currentMode;
	public final int x3 = 250063;
	public final int x6 = 250064;

	public final AI AI = new AI();

	public Integer targetBossId;
	public Integer battleId;

	public String getCurrentTypeName() {
		switch (currentType) {
		case 0:
			return "总力";

		default:
			return "超总";
		}
	}

	public JSONObject getCurrentType() {
		try {
			switch (currentType) {
			case 0:
				if (_normal == null) {
					_normal = lcf().sdop.loadJsonObject("boss_normal.json");
				}
				return _normal;
			default:
				if (_super == null) {
					_super = lcf().sdop.loadJsonObject("boss_super.json");
				}
				return _super;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject getCurrentMode() {
		if (currentMode == null) {
			Value mode = new Value();
			mode.value = currentModeValue;
			try {
				currentMode = new JSONObject(lcf().gson.toJson(mode));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return currentMode;
	}

	/**
	 * 检查是否x6机体
	 * 
	 * @param m
	 *            members中的member, playerList中的player.card
	 * @return
	 */
	public boolean checkX6(CardWithoutWeapon m) {
		for (int j = 0; j < m.characteristicList.length; j++) {
			if (m.characteristicList[j].id == x6) {
				m.lcf_attack = 6;
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查是否x3机体
	 * 
	 * @param m
	 *            members中的member, playerList中的player.card
	 * @return
	 */
	public boolean checkX3(CardWithoutWeapon m) {
		for (int j = 0; j < m.characteristicList.length; j++) {
			if (m.characteristicList[j].id == x3) {
				m.lcf_attack = 3;
				return true;
			}
		}
		return false;
	}

	private JSONObject initRaidBossOutlineList_args;

	private JSONObject getInitRaidBossOutlineList_args() {
		if (initRaidBossOutlineList_args == null) {
			try {
				initRaidBossOutlineList_args = lcf().sdop
						.loadJsonObject("initRaidBossOutlineList_args.json");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
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
			e.printStackTrace();
		}
	}

	/**
	 * 自动处理总力战结果
	 */
	public void autoRaidBossResult() {
		try {
			JSONObject args = lcf().sdop.loadJsonObject("boss_outline.json");
			getRaidBossOutlineList(args,
					GetRaidBossOutlineList4Finish.procedure,
					GetRaidBossOutlineList4Finish.procedure);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int currentGachaRaidBossId;

	/**
	 * 获取单个boss的结果
	 * 
	 * @param bossId
	 * @param callback
	 */
	public void getRaidBossResultData(int bossId, String callback) {
		String url = new StringBuilder(lcf().sdop.httpUrlPrefix)
				.append("/GetForRaidBossResult/getRaidBossResultData?")
				.append(lcf().sdop.createGetParams()).append("&raidBossId=")
				.append(bossId).toString();
		currentGachaRaidBossId = bossId;
		lcf().sdop.get(url, GetRaidBossResultData.procedure, callback);
	}

	/**
	 * boss抽奖
	 * 
	 * @param raidBossId
	 * @param callback
	 */
	public void raidBossGacha(int raidBossId, String callback) {
		if (lcf().sdop.ms.current >= lcf().sdop.ms.max) {
			lcf().sdop.log("MS数量已满，停止自动！");
			return;
		}
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForRaidBossResult/raidBossGacha";
		currentGachaRaidBossId = raidBossId;
		try {
			JSONObject payload = lcf().sdop.createBasePayload("raidBossGacha",
					new JSONObject().put("raidBossId", raidBossId));
			lcf().sdop.post(url, payload.toString(), RaidBossGacha.procedure,
					callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除boss结果
	 * 
	 * @param raidBossId
	 * @param callback
	 */
	public void deleteRaidBossList(int raidBossId, String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForRaidBossList/deleteRaidBossList";
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					"deleteRaidBossList",
					new JSONObject().put("id", raidBossId));
			lcf().sdop.post(url, payload.toString(),
					DeleteRaidBossList.procedure, callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * true战斗自动, 不进行AI判定
	 */
	public boolean isAutoBattle = false;

	/**
	 * 选好人, 并开始boss战斗
	 * 
	 * @param members
	 *            参阅{@link AI#setFixMember(JSONObject, boolean)}
	 * @param battleId
	 * @param isAutoBattle
	 * @param callback
	 */
	public void executeBattleStart(CardWithoutWeapon[] members, int battleId,
			JSONObject mode, boolean isAutoBattle, String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForQuestBattle/executeBattleStart";
		this.isAutoBattle = isAutoBattle;
		Log.d(lcf().LOG_TAG, "executeBattleStart isAutoBattle: " + isAutoBattle
				+ ", mode : " + mode.toString());
		try {
			ArrayList<Unit> unitList = new ArrayList<Unit>(3);
			Unit unit = new Unit();
			unit.id = lcf().sdop.myUserId;
			unit.setArrangementValue(0);
			unit.isLeader = true;
			unitList.add(unit);

			if (members != null) {
				if (members[0] != null) {
					unit = new Unit();
					unit.id = members[0].id;
					unit.setArrangementValue(1);
					unitList.add(unit);

					if (members[1] != null) {
						unit = new Unit();
						unit.id = members[1].id;
						unit.setArrangementValue(2);
						unitList.add(unit);
					}
				}
			}

			JSONObject payload = lcf().sdop.createBasePayload(
					"executeBattleStart",
					new JSONObject()
							.put("battleId", battleId)
							.put("unitList",
									new JSONArray(lcf().gson.toJson(unitList)))
							.put("isAutoBattle", isAutoBattle)
							.put("mode", mode));
			lcf().sdop.post(url, payload.toString(),
					ExecuteBattleStart.procedure, callback);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param targetBossId
	 *            0时, 表示遭遇战
	 * @param callback
	 */
	public void getRaidBossBattleData(Integer targetBossId, String callback) {
		if (targetBossId == null) {
			Log.e(lcf().LOG_TAG, "targetBossId is null ! " + targetBossId);
			return;
		}
		String url = lcf().sdop.httpUrlPrefix
				+ "/GetForQuestBattle/getRaidBossBattleData?"
				+ lcf().sdop.createGetParams() + "&raidBossId=" + targetBossId;
		lcf().sdop.get(url, GetRaidBossBattleData.procedure, callback);
	}

	public void postRaidBossBattleEntry(int bossId, String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForRaidBossList/postRaidBossBattleEntry";
		targetBossId = bossId;
		try {
			JSONObject payload = lcf().sdop
					.createBasePayload(
							"postRaidBossBattleEntry",
							new JSONObject().put("id", bossId).put(
									"isChargeBp", false));
			lcf().sdop.post(url, payload.toString(),
					PostRaidBossBattleEntry.procedure, callback);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void getRaidBossOutlineList(JSONObject args, String procedure,
			String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForRaidBossList/getRaidBossOutlineList";
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					"getRaidBossOutlineList", args);
			lcf().sdop.post(url, payload.toString(), procedure, callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 自动超总
	 */
	public void autoSuperRaidBoss() {
		if (!lcf().sdop.auto.setting.boss) {
			Log.i(lcf().LOG_TAG, "auto.setting.boss : "
					+ lcf().sdop.auto.setting.boss);
			return;
		}

		// 注意, 这里开始后, 因为要连续各种不同的参数, 所以代码中存在线性安全问题, 但同样的, 理论上这是对一个帐号的处理,
		// 不应该会同时触发相同的事件, 所以线性安全性实际上不存在
		// targetBossId = null;
		getRaidBossOutlineList(getCurrentType(),
				GetRaidBossOutlineList.procedure,
				GetRaidBossOutlineList.procedure);
	}

	public final String[] actionType = { "ITEM", "SKILL", "ATTACK" };

	/**
	 * 执行技能
	 * 
	 * @param battleId
	 * @param actionTypeValue
	 *            对应{@link #actionType}
	 * @param targetId
	 *            1为boss, 2-4为参战队友
	 * @param playerId
	 *            同targetId
	 * @param actionId
	 *            技能代号, 30sp药为20006
	 * @param callback
	 */
	public void executeActionCommand(int battleId, String actionTypeValue,
			int targetId, int playerId, int actionId, String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForQuestBattle/executeActionCommand";
		try {
			JSONObject args = new JSONObject(
					lcf().gson.toJson(new ExecuteActionCommandArgs(battleId,
							actionTypeValue, currentModeValue, targetId,
							playerId, actionId)));
			JSONObject payload = lcf().sdop.createBasePayload(
					"executeActionCommand", args);
			lcf().sdop.post(url, payload.toString(),
					ExecuteActionCommand.procedure, callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 断网后，重新获取boss战斗数据的方法, 注意: 该方法好像有好多限制, 暂证明能使用在普通遭遇战斗
	 * 
	 * @param callback
	 */
	public void getBattleData(String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/GetForQuestBattle/getBattleData?"
				+ lcf().sdop.createGetParams();
		lcf().sdop.get(url, GetBattleData.procedure, callback);
	}

	/**
	 * 发送求助信息
	 * 
	 * @param raidBossId
	 */
	public void sendRescueSignal(int raidBossId, String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForRaidBoss/sendRescueSignal";

		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					"sendRescueSignal",
					new JSONObject().put("isForAllPlayer", true)
							.put("comment", "help~~~!!!")
							.put("raidBossId", raidBossId));
			lcf().sdop.post(url, payload.toString(),
					SendRescueSignal.procedure, callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
