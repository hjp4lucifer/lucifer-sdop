package cn.lucifer.sdop.domain;

import java.util.List;

public class Pilot {

	public List<ActiveSkill> activeSkillList;

	public int arousal;
	public int battleForce;
	public int beforeArousal;
	public int beforeShooting;
	public int beforeSpeed;
	public int beforeWrestling;
	public int cost;

	public int currentExp;
	public int id;
	public int level;
	public int nextExp;

	public List<PassiveSkill> passiveSkillList;

	public int rarity;
	public int shooting;
	public int speed;
	public int totalExp;
	
	public PilotType type;
	
	public int wrestling;
}
