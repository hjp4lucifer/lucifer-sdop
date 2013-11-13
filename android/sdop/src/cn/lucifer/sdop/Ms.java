package cn.lucifer.sdop;

import cn.lucifer.sdop.domain.Card;
import cn.lucifer.sdop.domain.Pilot;

public class Ms extends LcfExtend {

	public final String[] unitAttribute = { "FIGHT", "SPECIAL", "SHOOT" };

	public String logMsList(cn.lucifer.sdop.domain.Ms[] msList) {
		StringBuffer logMsg = new StringBuffer();
		Card card;
		Pilot pilot;
		for (int i = 0; i < msList.length; i++) {
			card = msList[i].card;
			logMsg.append("<br>").append(i).append("） ");
			if (card.userName != null) {
				if (card.lcf_attack > 1) {
					logMsg.append(lcf().sdop.getRedBoldMsg(card.userName));
				} else {
					logMsg.append(lcf().sdop.getBoldMsg(card.userName));
				}
			}
			logMsg.append(card.rarity).append('c').append(card.cost)
					.append('【').append(card.type.name).append("】，level：")
					.append(card.level).append("，属性：")
					.append(card.attribute.value).append("，attack：")
					.append(card.attack).append("，max HP：").append(card.maxHp)
					.append("，speed：").append(card.speed);

			for (int j = 0; j < card.characteristicList.length; j++) {
				logMsg.append("<br>　　插件").append(j).append("：")
						.append(card.characteristicList[j].briefDescription);
			}

			pilot = card.pilot;
			logMsg.append("<br>　pilot：").append(pilot.rarity).append('c')
					.append(pilot.cost).append('【').append(pilot.type.name)
					.append("】，level：").append(pilot.level);

			if (pilot.activeSkillList != null) {
				for (int j = 0; j < pilot.activeSkillList.length; j++) {
					logMsg.append("<br>　　主动技能：").append(
							pilot.activeSkillList[j].description);
				}
			}

			if (pilot.passiveSkillList != null) {
				for (int j = 0; j < pilot.passiveSkillList.length; j++) {
					logMsg.append("<br>　　被动技能：").append(
							pilot.passiveSkillList[j].description);
				}
			}
		}

		return logMsg.toString();
	}

}
