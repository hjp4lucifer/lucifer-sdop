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
import cn.lucifer.sdop.domain.CardWithoutWeapon;
import cn.lucifer.sdop.domain.Pilot;

public class CardPlatoon extends LcfExtend {

	/**
	 * Example: in battle, the status need lock.
	 */
	public boolean lockCardPlatoon;

	/**
	 * @return true is initialize card data success
	 */
	public void initChooseCardData() {
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

	public void initChooseCardDataFinished() {
		lcf().sdop.auto.setting.cardPlatoon = true;
	}

	CardWithoutWeapon msLeaderCard;

	public void chooseCards(int raidUnitLeaderId, int raidPilotLeaderId,
			CardWithoutWeapon[] msCardList, Pilot[] pilotCardList) {
		msLeaderCard = (CardWithoutWeapon) getLeaderCard(raidUnitLeaderId,
				msCardList);
		if (lcf().sdop.boss.checkX6(msLeaderCard)) {
			msLeaderCard = null;
			return;
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
