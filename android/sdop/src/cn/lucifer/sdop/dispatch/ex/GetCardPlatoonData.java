package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.CardWithoutWeapon;
import cn.lucifer.sdop.domain.Item;
import cn.lucifer.sdop.domain.Pilot;

public class GetCardPlatoonData extends BaseDispatch {
	public static final String procedure = "getCardPlatoonData";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		Item[] itemList = lcf().gson.fromJson(args.getString("itemList"),
				Item[].class);

		int raidUnitLeaderId = args
				.getJSONObject("unitLeaderIdList")
				.getInt(lcf().sdop.boss.raidBossFieldType == 0 ? "raidSpaceDeckLeaderId"
						: "raidGroundDeckLeaderId");
		int raidPilotLeaderId = args
				.getJSONObject("pilotLeaderIdList")
				.getInt(lcf().sdop.boss.raidBossFieldType == 0 ? "raidSpaceDeckLeaderId"
						: "raidGroundDeckLeaderId");

		CardWithoutWeapon[] msCardList = lcf().gson.fromJson(
				args.getString("msCardList"), CardWithoutWeapon[].class);

		Pilot[] pilotCardList = lcf().gson.fromJson(
				args.getString("pilotCardList"), Pilot[].class);
		lcf().sdop.checkCallback(callback,
				new Object[] { itemList, raidUnitLeaderId, raidPilotLeaderId,
						msCardList, pilotCardList });
	}

	@Override
	public void callback(Object[] args) {
		if (null == args || args.length < 5) {
			return;
		}
		int raidUnitLeaderId = (Integer) args[1];
		int raidPilotLeaderId = (Integer) args[2];
		CardWithoutWeapon[] msCardList = (CardWithoutWeapon[]) args[3];
		Pilot[] pilotCardList = (Pilot[]) args[4];
		lcf().sdop.cardPlatoon.chooseCards(raidUnitLeaderId, raidPilotLeaderId,
				msCardList, pilotCardList);
	}

}
