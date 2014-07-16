package cn.lucifer.sdop;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.ex.EquipPilot;
import cn.lucifer.sdop.dispatch.ex.GetCardPlatoonData;
import cn.lucifer.sdop.dispatch.ex.GetRaidBossField;
import cn.lucifer.sdop.dispatch.ex.SelectLeader;
import cn.lucifer.sdop.dispatch.ex.SetUseOptionalDeckList;
import cn.lucifer.sdop.domain.BaseCard;
import cn.lucifer.sdop.domain.CardSynthesis;
import cn.lucifer.sdop.domain.PlatoonPilot;
import cn.lucifer.sdop.domain.PlatoonSkill;

public class CardPlatoon extends LcfExtend {

	/**
	 * Example: in battle, the status need lock.
	 */
	public boolean lockCardPlatoon;

	/**
	 * @return true is initialize card data success
	 */
	public void initChooseCardData() {
		lcf().sdop.auto.setting.cardPlatoon = true;
		try {
			lcf().sdop.boss.getRaidBossOutlineList(
					lcf().sdop.boss.getSuperType(), GetRaidBossField.procedure,
					GetRaidBossField.procedure);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	CardSynthesis msLeaderCard;
	PlatoonPilot attackPilot, helpPilot;

	public void logChooseCards() {
		if (!lcf().sdop.auto.setting.cardPlatoon) {
			lcf().sdop.log("无需启用智能甲板!");
			return;
		}
		StringBuilder log = new StringBuilder("初始化智能甲板成功! <br>　");
		if (null == msLeaderCard) {
			log.append("no mark ms !");
		} else {
			log.append(String.format(
					"mark ms : %dc%d 【%s】 %s, Lv: %d, next exp: %d",
					msLeaderCard.rarity, msLeaderCard.cost, msLeaderCard.name,
					msLeaderCard.attribute.value, msLeaderCard.level,
					msLeaderCard.nextExp));
		}
		if (null != attackPilot) {
			logPilot(log, attackPilot, "attack");
		}
		if (null != helpPilot) {
			logPilot(log, helpPilot, "help");
		}
		lcf().sdop.log(log.toString());
	}

	protected void logPilot(StringBuilder log, PlatoonPilot pilot, String prefix) {
		log.append("<br>　").append(prefix).append(" pilot: ")
				.append(pilot.rarity).append('c').append(pilot.cost)
				.append('【').append(pilot.name).append("】, level：")
				.append(pilot.level).append(", attack point: ")
				.append(pilot.attackPoint).append(", help point: ")
				.append(pilot.helpPoint);
		if (null != pilot.skillList) {
			for (PlatoonSkill skill : pilot.skillList) {
				log.append("<br>　　Lv").append(skill.level).append(' ')
						.append(skill.description);
			}
		}
	}

	public void chooseCards(int raidUnitLeaderId, int raidPilotLeaderId,
			CardSynthesis[] msCardList, PlatoonPilot[] pilotCardList) {
		msLeaderCard = (CardSynthesis) getLeaderCard(raidUnitLeaderId,
				msCardList);
		if (lcf().sdop.boss.checkX6(msLeaderCard)) {
			lcf().sdop.auto.setting.cardPlatoon = false;
			msLeaderCard = null;
			return;
		}
		choosePilot(pilotCardList);
		if (lcf().sdop.boss.checkX3(msLeaderCard)) {
			msLeaderCard = null;
			return;
		}
		if (lcf().sdop.boss.checkX2(msLeaderCard)) {
			msLeaderCard = null;
			return;
		}
		boolean has2Times = false;
		for (CardSynthesis ms : msCardList) {
			if (lcf().sdop.boss.checkX2(ms)) {
				has2Times = true;
				break;
			}
		}
		if (!has2Times) {
			msLeaderCard = null;
			lcf().sdop.auto.setting.cardPlatoon = false;
		}
	}

	protected void choosePilot(PlatoonPilot[] pilotCardList) {
		for (PlatoonPilot pilot : pilotCardList) {
			setAttackPoint(pilot);
			setHelpAttackPoint(pilot);
		}
	}

	/**
	 * 设置攻击向Pilot
	 * 
	 * @param pilot
	 */
	protected void setAttackPoint(PlatoonPilot pilot) {
		if (null == pilot.skillList) {
			return;
		}
		for (PlatoonSkill skill : pilot.skillList) {
			switch (skill.id) {
			case 11001:// 2星敵単体に近距離武器
				// case 11002://2星敵単体に遠距離武器
				pilot.attackPoint += skill.level * 3;
				break;
			case 51001:// 2星常に攻撃力
				pilot.attackPoint += skill.level * 2;
				break;
			case 51006:// 2星常にクリティカル率
				pilot.attackPoint += skill.level;
				break;
			}
		}
		if (null == attackPilot || pilot.attackPoint > attackPilot.attackPoint) {
			attackPilot = pilot;
		}
	}

	/**
	 * 设置辅助向Pilot
	 * 
	 * @param pilot
	 */
	protected void setHelpAttackPoint(PlatoonPilot pilot) {
		if (null == pilot.skillList) {
			return;
		}
		for (PlatoonSkill skill : pilot.skillList) {
			switch (skill.id) {
			case 21001:// 2星味方1人の攻撃力を4ターンの間
				pilot.helpPoint += skill.level * 3;
				break;
			case 21002:// 2星味方1人の機動力を4ターンの間
				pilot.helpPoint += skill.level;
				break;
			case 51002:// 2星常に機動力
				pilot.helpPoint += skill.level * 2;
				break;
			}
		}

		if (null == helpPilot || pilot.helpPoint > helpPilot.helpPoint) {
			helpPilot = pilot;
		}
	}

	protected BaseCard getLeaderCard(int leaderId, BaseCard[] cardList) {
		for (BaseCard card : cardList) {
			if (leaderId == card.id) {
				return card;
			}
		}
		return null;
	}

	public void getCardPlatoonData(String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/GetForCardPlatoon/getCardPlatoonData?"
				+ lcf().sdop.createGetParams();
		lcf().sdop.get(url, GetCardPlatoonData.procedure, callback);
	}

	/**
	 * 开启全部的定制甲板
	 * 
	 * @param callback
	 */
	public void openAllOptionalDeck(String callback) {
		setUseOptionalDeckList(true, true, true, true, callback);
	}

	protected void setUseOptionalDeckList(boolean isUseRaidGroundDeck,
			boolean isUseLeagueSpaceDeck, boolean isUseLeagueGroundDeck,
			boolean isUseRaidSpaceDeck, String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForCardPlatoon/setUseOptionalDeckList?ssid="
				+ lcf().sdop.ssid;
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					"setUseOptionalDeckList", new JSONObject().put(
							"isUseOptionalDeckList",
							new JSONObject()
									.put("isUseRaidGroundDeck",
											isUseRaidGroundDeck)
									.put("isUseLeagueSpaceDeck",
											isUseLeagueSpaceDeck)
									.put("isUseLeagueGroundDeck",
											isUseLeagueGroundDeck)
									.put("isUseRaidSpaceDeck",
											isUseRaidSpaceDeck)));

			lcf().sdop.post(url, payload.toString(),
					SetUseOptionalDeckList.procedure, callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param msCardId
	 * @param deckType
	 *            <ul>
	 *            <li>COMMON</li>
	 *            <li>RAID</li>
	 *            <li>LEAGUE</li>
	 *            </ul>
	 * @param areaType
	 *            <ul>
	 *            <li>LAND</li>
	 *            <li>SPACE</li>
	 *            </ul>
	 * @param callback
	 */
	public void selectLeader(int msCardId, String deckType, String areaType,
			String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForCardPlatoon/selectLeader?ssid=" + lcf().sdop.ssid;
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					"selectLeader",
					new JSONObject()
							.put("msCardId", msCardId)
							.put("deckType",
									new JSONObject().put("value", deckType))
							.put("areaType",
									new JSONObject().put("value", areaType)));

			lcf().sdop.post(url, payload.toString(), SelectLeader.procedure,
					callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param pilotId
	 * @param deckType
	 *            <ul>
	 *            <li>COMMON</li>
	 *            <li>RAID</li>
	 *            <li>LEAGUE</li>
	 *            </ul>
	 * @param areaType
	 *            <ul>
	 *            <li>LAND</li>
	 *            <li>SPACE</li>
	 *            </ul>
	 * @param callback
	 */
	public void equipPilot(int pilotId, String deckType, String areaType,
			String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForCardPlatoon/equipPilot?ssid=" + lcf().sdop.ssid;
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					"selectLeader",
					new JSONObject()
							.put("pilotId", pilotId)
							.put("deckType",
									new JSONObject().put("value", deckType))
							.put("areaType",
									new JSONObject().put("value", areaType)));

			lcf().sdop.post(url, payload.toString(), EquipPilot.procedure,
					callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
