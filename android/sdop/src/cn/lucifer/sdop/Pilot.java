package cn.lucifer.sdop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Pilot extends LcfExtend {

	public int max;
	public int current;

	public final Map<Integer, Skill> activeSkill;
	public final Map<Integer, Skill> passiveSkill;

	public Pilot() {
		activeSkill = new HashMap<Integer, Skill>(2);
		Skill[] skills;
		try {
			skills = lcf().gson.fromJson(
					lcf().sdop.loadJson("active_skill.json"), Skill[].class);
			for (Skill skill : skills) {
				activeSkill.put(skill.id, skill);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		passiveSkill = new HashMap<Integer, Skill>(2);
		try {
			skills = lcf().gson.fromJson(
					lcf().sdop.loadJson("passive_skill.json"), Skill[].class);
			for (Skill skill : skills) {
				passiveSkill.put(skill.id, skill);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		skills = null;
	}

	public int getPassiveSkillUpValue(int skillId, String skillDescription) {
		Skill skill = passiveSkill.get(skillId);
		String value = skillDescription.substring(
				skillDescription.indexOf(skill.prefix + skill.prefix.length()),
				skillDescription.indexOf(skill.suffix));
		return (int) Float.parseFloat(value);
	}
}
