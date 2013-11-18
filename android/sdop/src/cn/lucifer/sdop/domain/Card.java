package cn.lucifer.sdop.domain;

public class Card extends BaseCard {

	public int attack;

	public Value attribute;

	public int beforeAttack;

	public int beforeCurrentHp;

	public int beforeMaxHp;

	public int beforeSpeed;

	public Characteristic[] characteristicList;

	public int currentEp;

	public int currentHp;

	public int currentSp;

	public int defense;

	public boolean isUsableSpecialWeapon;

	public int maxEp;

	public int maxHp;

	public int maxSp;

	public Pilot pilot;

	public MsType type;

	public int waitTurn;

	public int weakUnitDamageRate;

	public Weapon[] weaponList;

	// 其他地方的扩展
	public int coolTime;
	public String userName;

	public int lcf_attack;
	public int lcf_speed;
	public boolean lcf_isHelp;

}
