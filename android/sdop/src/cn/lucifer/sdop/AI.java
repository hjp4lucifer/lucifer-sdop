package cn.lucifer.sdop;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.domain.ActiveSkill;
import cn.lucifer.sdop.domain.Card;
import cn.lucifer.sdop.domain.Item;
import cn.lucifer.sdop.domain.Ms;
import cn.lucifer.sdop.domain.PassiveSkill;

public class AI extends LcfExtend {
	public Card attackMember, helpMember;
	public Ms[] playerMsList;
	public Item[] itemList;

	private final ActionCode[] actionCode;

	public AI() {
		actionCode = new ActionCode[] { new ActionCode(0, "普通攻击"),
				new ActionCode(1, "给队友加攻击状态"), new ActionCode(2, "给队友加速度状态"),
				new ActionCode(3, "单体技能攻击", "敵単体に") };

	}

	public cn.lucifer.sdop.domain.Boss getTopLevel(
			cn.lucifer.sdop.domain.Boss[] bosses) {
		cn.lucifer.sdop.domain.Boss target = null;
		for (cn.lucifer.sdop.domain.Boss _currentBoss : bosses) {
			if (target != null) {
				// if (_currentBoss.isForRecommend) {
				// if (!target.isForRecommend) {//目标是不推荐
				// target = _currentBoss;
				// continue;
				// }
				// } else {//不推荐
				// continue;
				// }
				if (target.level > _currentBoss.level) {// 判断等级
					continue;
				} else if (target.level < _currentBoss.level) {
					target = _currentBoss;
					continue;
				}
				if (target.currentHp < _currentBoss.currentHp) {// 判断剩余血量
					target = _currentBoss;
				}
			} else {
				target = _currentBoss;
			}
		}
		return target;
	}

	public Card getFixAttackMember(Card[] members) {
		Card attackMember = null;
		Card m;
		for (int i = 0; i < members.length; i++) {
			m = members[i];
			if (m.id == lcf().sdop.myUserId) {// 不能是自己
				continue;
			}
			if (m.coolTime != 0) {// 没有冷却
				continue;
			}
			// 校验是否倍机
			if (lcf().sdop.boss.checkX6(m)) {
				m.lcf_attack = 6;
			} else if (lcf().sdop.boss.checkX3(m)) {
				m.lcf_attack = 3;
			} else {
				m.lcf_attack = 1;
			}
			if (attackMember == null) {// 默认没有选择时
				attackMember = m;
				continue;
			}
			if (attackMember.lcf_attack > m.lcf_attack) {// 倍数级别高
				continue;
			} else if (attackMember.lcf_attack < m.lcf_attack) {
				attackMember = m;
				continue;
			}
			// 倍数级别一样
			if (attackMember.attack > m.attack) {// 攻击高
				continue;
			}
			attackMember = m;
		}

		return attackMember;
	}

	public int getTrueSpeed(Card m) {
		int speed = m.speed;
		if (m.pilot.passiveSkillList == null) {
			return speed;
		}
		PassiveSkill skill;
		for (int j = 0; j < m.pilot.passiveSkillList.length; j++) {
			skill = m.pilot.passiveSkillList[j];
			if (skill.id == 51002) {// 被动加速
				speed += speed
						* (lcf().sdop.pilot.getPassiveSkillUpValue(51002,
								skill.description) / 100F);
			}
		}
		return speed;
	}

	/**
	 * 必须在调用完{@link #getFixAttackMember(Card[])}后才能调用
	 * 
	 * @param members
	 * @param attackMember
	 * @return
	 */
	public Card getFixHelpMember(Card[] members, Card attackMember) {
		if (attackMember == null) {
			return null;
		}
		attackMember.lcf_speed = getTrueSpeed(attackMember);
		Card helpMember = null;
		ActiveSkill skill;
		Card m;
		for (int i = 0; i < members.length; i++) {
			m = members[i];
			if (m.id == lcf().sdop.myUserId) {// 不能是自己
				continue;
			}
			if (m.id == attackMember.id || m.lcf_attack > 1) {// 不能是倍机
				continue;
			}
			if (m.coolTime != 0) {// 没有冷却
				continue;
			}
			if (m.pilot.activeSkillList == null) {// 没有主动技能
				m.lcf_isHelp = false;
			} else {
				for (int j = 0; j < m.pilot.activeSkillList.length; j++) {
					skill = m.pilot.activeSkillList[j];
					if (skill.id == 21001) {
						m.lcf_isHelp = true;
						break;
					} else {
						m.lcf_isHelp = false;
					}
				}

			}
			m.lcf_speed = getTrueSpeed(m);

			if (helpMember == null) {// 默认没有选择时
				helpMember = m;
				continue;
			}
			if (helpMember.lcf_isHelp) {// 选定的是辅助机
				if (helpMember.lcf_speed >= attackMember.lcf_speed) {// 辅助机已经快过倍机
					break;
				}
				if (m.lcf_isHelp) {// 当前是辅助机
					if (helpMember.lcf_speed > m.lcf_speed) {// 选定机比当前机快
						continue;
					} else {
						helpMember = m;
						continue;
					}
				}
			}
			if (m.lcf_isHelp) {// 当前是辅助机, 选定机不是
				helpMember = m;
				continue;
			}
			// 剩余的规则没想好
		}

		return helpMember;
	}

	/**
	 * 分析最适合的超总人选, 进入前, 请先确保myUserId是有值
	 * 
	 * @param battleArgs
	 * @throws JSONException
	 */
	public void setFixMember(JSONObject battleArgs) throws JSONException {
		lcf().sdop.myUserId = battleArgs.getInt("leaderCardId");

		Ms[] playerMsList = lcf().gson.fromJson(
				battleArgs.getString("playerMsList"), Ms[].class);
		Card myCard = playerMsList[0].card;
		lcf().sdop.currentSp = myCard.currentSp;
		lcf().sdop.maxSp = myCard.maxSp;

		lcf().sdop.boss.battleId = battleArgs.getInt("battleId");
		Card[] members = lcf().gson.fromJson(
				battleArgs.getString("memberCardList"), Card[].class);
		attackMember = getFixAttackMember(members);
		helpMember = getFixHelpMember(members, attackMember);
	}

	/**
	 * 检查自己的技能, 查看是否是符合条件的辅助技能
	 * 
	 * @param player
	 * @return
	 */
	public boolean checkMySkill(Ms player) {
		ActiveSkill[] activeSkillList = player.card.pilot.activeSkillList;
		for (ActiveSkill activeSkill : activeSkillList) {
			if (activeSkill.id == 21001) {
				continue;
			}
			if (activeSkill.id == 21002) {
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * 返回true表示有攻击性技能
	 * 
	 * @param player
	 */
	public boolean checkAttackSkill(Ms player) {
		ActiveSkill[] activeSkillList = player.card.pilot.activeSkillList;

		for (ActiveSkill activeSkill : activeSkillList) {
			if (activeSkill.description.startsWith(actionCode[3].prefix)) {
				return true;
			}
		}
		return false;
	}

	public boolean checkHelpSkill(Ms player) {
		ActiveSkill[] activeSkillList = player.card.pilot.activeSkillList;
		for (ActiveSkill activeSkill : activeSkillList) {
			if (activeSkill.id == 21001) {
				return true;
			}
		}
		return false;
	}

	private final int[] simple = { 0, 0, 0, 0, 0, 0 };
	private final int[] me = { 1, 2, 2, 0, 0, 0 };
	private final int[] help = { 1, 0, 0, 0, 0, 0 };
	private final int[] attack = { 3, 3, 3, 3, 3, 3 };
	private List<Ms> attackPlayers;

	/**
	 * 设置适应的行动AI
	 * 
	 * @param battleArgs
	 * @throws JSONException
	 */
	public void fixPlayerMsListAI(JSONObject battleArgs) throws JSONException {
		playerMsList = lcf().gson.fromJson(
				battleArgs.getString("playerMsList"), Ms[].class);

		boolean hasAttack = false;
		attackPlayers = new ArrayList<Ms>();

		for (Ms player : playerMsList) {
			player.AIType = simple;
			player.AITurn = 0;
			if (player.card.id == lcf().sdop.myUserId) {
				if (checkMySkill(player)) {
					player.AIType = me;
				}
				continue;
			}
			if (lcf().sdop.boss.checkX6(player.card)
					|| lcf().sdop.boss.checkX3(player.card)) {
				if (checkAttackSkill(player)) {
					player.AIType = attack;
					player.lcf_attack = player.card.lcf_attack;// 设置倍数信息
					attackPlayers.add(player);
					hasAttack = true;
				}
				continue;
			}
			if (checkHelpSkill(player)) {
				player.AIType = help;
			}
		}

		if (!hasAttack) {
			for (Ms player : playerMsList) {
				player.AIType = simple;
			}
		}
	}
	
	public void autoBattle(JSONObject battleArgs){
		
	}

	public void startAutoSuperRaidBoss(int delayTime) {

	}
}
