package cn.lucifer.sdop;

import cn.lucifer.sdop.domain.Card;
import cn.lucifer.sdop.domain.CustomizeInfo;
import cn.lucifer.sdop.domain.Pilot;

public class Ms extends LcfExtend {

	public final String[] unitAttribute = { "FIGHT", "SPECIAL", "SHOOT" };

	/**
	 * 获取被克制的属性
	 * 
	 * @param unitAttr
	 *            当前选择属性
	 * @return
	 */
	String getReverseUnitAttribute(String unitAttr) {
		int unitAttributeIndex = 0;
		for (int i = 0, len = unitAttribute.length; i < len; i++) {
			if (unitAttribute[i].equals(unitAttr)) {
				unitAttributeIndex = i;
				break;
			}
		}
		unitAttributeIndex--;// 按照属性定义, 被克制的排列
		if (unitAttributeIndex < 0) {// 就是第一个
			unitAttributeIndex = unitAttribute.length - 1;// 那应该就是最后一个
		}
		return unitAttribute[unitAttributeIndex];
	}

	public int max;
	public int current;

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
					.append('【').append(card.type.name).append("】, level：")
					.append(card.level).append(", 属性：")
					.append(card.attribute.value).append(", attack：")
					.append(card.attack).append(", max HP：").append(card.maxHp)
					.append(", speed：").append(card.speed);

			for (int j = 0; j < card.characteristicList.length; j++) {
				logMsg.append("<br>　　插件").append(j).append("：")
						.append(card.characteristicList[j].briefDescription);
			}
			if (null != card.customizeInfo) {
				CustomizeInfo customizeInfo = card.customizeInfo;
				logMsg.append("<br>　　custom信息").append("：")
						.append(customizeInfo.customizeCount).append('/')
						.append(customizeInfo.maxCustomizeCount)
						.append(", allocationPoint：")
						.append(customizeInfo.currentAllocationPoint)
						.append('/').append(customizeInfo.maxAllocationPoint);
				if (customizeInfo.customizeAttack > 0) {
					logMsg.append("<br>　　　customizeAttack:").append(
							customizeInfo.customizeAttack);
				}
				if (customizeInfo.customizeHP > 0) {
					logMsg.append("<br>　　　customizeHP:").append(
							customizeInfo.customizeHP);
				}
				if (customizeInfo.customizeSpeed > 0) {
					logMsg.append("<br>　　　customizeSpeed:").append(
							customizeInfo.customizeSpeed);
				}
				if (null != customizeInfo.specialEffectList) {
					for (int j = 0, len = customizeInfo.specialEffectList.length; j < len; j++) {
						logMsg.append("<br>　　　specialEffect:").append(
								customizeInfo.specialEffectList[j]);
					}
				}
			}

			pilot = card.pilot;
			if (pilot != null) {
				logMsg.append("<br>　pilot：").append(pilot.rarity).append('c')
						.append(pilot.cost).append('【').append(pilot.type.name)
						.append("】, level：").append(pilot.level);

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
		}

		return logMsg.toString();
	}

}
