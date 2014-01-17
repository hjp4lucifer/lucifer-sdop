package cn.lucifer.sdop;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import cn.lucifer.sdop.dispatch.ex.AutoBattle;
import cn.lucifer.sdop.dispatch.ex.ExecuteActionCommand;
import cn.lucifer.sdop.dispatch.ex.InitRaidBossOutlineList;
import cn.lucifer.sdop.domain.ActionOrder;
import cn.lucifer.sdop.domain.ActiveSkill;
import cn.lucifer.sdop.domain.Card;
import cn.lucifer.sdop.domain.CardWithoutWeapon;
import cn.lucifer.sdop.domain.Item;
import cn.lucifer.sdop.domain.Ms;
import cn.lucifer.sdop.domain.PassiveSkill;

public class AI extends LcfExtend {
	public CardWithoutWeapon attackMember, helpMember;
	public Ms[] playerMsList;
	public Item[] itemList;

	private final ActionCode[] actionCode;

	public AI() {
		actionCode = new ActionCode[] { new ActionCode(0, "普通攻击"),
				new ActionCode(1, "给队友加攻击状态"), new ActionCode(2, "给队友加速度状态"),
				new ActionCode(3, "单体技能攻击", "敵単体に") };

	}

	/**
	 * 最少血量
	 */
	protected final int Least_Hp = 5000000;

	/**
	 * 
	 * @param bosses
	 * @return 可为null
	 */
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
				if (_currentBoss.currentHp < Least_Hp) {//判断血量
					continue;
				}
				if (target.level > _currentBoss.level) {// 判断等级
					continue;
				}
				if (target.level < _currentBoss.level) {
					target = _currentBoss;
					continue;
				}
				if (target.currentHp < _currentBoss.currentHp) {// 判断剩余血量
					target = _currentBoss;
				}
			} else {
				// if (_currentBoss.level == 1) {
				// continue;
				// }
				if (_currentBoss.currentHp < Least_Hp) {
					continue;
				}
				target = _currentBoss;
			}
		}
		return target;
	}

	/**
	 * 选择非倍机成员
	 * 
	 * @param members
	 * @param other
	 * @return
	 */
	public CardWithoutWeapon getNotAttackMember(CardWithoutWeapon[] members,
			CardWithoutWeapon other) {
		CardWithoutWeapon nextMember = null;
		for (CardWithoutWeapon m : members) {
			if (m.id == lcf().sdop.myUserId) {// 不能是自己
				continue;
			}
			if ((null != other) && (m.id == other.id)) {// 不能是已选成员
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
			if (nextMember == null) {// 默认没有选择时
				nextMember = m;
				continue;
			}
			if (nextMember.lcf_attack > m.lcf_attack) {// 倍数级别高
				nextMember = m;
				continue;
			} else if (nextMember.lcf_attack < m.lcf_attack) {
				continue;
			}
			// 倍数级别一样
			if (nextMember.attack < m.attack) {// 攻击低
				continue;
			}

			nextMember = m;
		}

		return nextMember;
	}

	/**
	 * 选择合适的倍机
	 * 
	 * @param members
	 * @return
	 */
	public CardWithoutWeapon getFixAttackMember(CardWithoutWeapon[] members) {
		CardWithoutWeapon attackMember = null;
		for (CardWithoutWeapon m : members) {
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

	public int getTrueSpeed(CardWithoutWeapon m) {
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
	public CardWithoutWeapon getFixHelpMember(CardWithoutWeapon[] members,
			CardWithoutWeapon attackMember) {
		if (attackMember == null) {
			Log.e("Lucifer", "attackMember is null !");
			printStackTrace();
			return null;
		}
		try {
			attackMember.lcf_speed = getTrueSpeed(attackMember);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CardWithoutWeapon helpMember = null;
		ActiveSkill skill;
		for (CardWithoutWeapon m : members) {
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
		// String SDPATH = Environment.getExternalStorageDirectory().getPath();
		// Writer output = new FileWriter(SDPATH + "/tmp/" +
		// System.currentTimeMillis() + ".txt");
		// IOUtils.write(memberCardList, output );
		// IOUtils.closeQuietly(output);

		CardWithoutWeapon[] members = lcf().gson.fromJson(
				battleArgs.getString("memberCardList"),
				CardWithoutWeapon[].class);

		switch (lcf().sdop.boss.currentType) {
		case 0:
			lcf().sdop.boss.isAutoBattle = true;
			attackMember = getNotAttackMember(members, null);
			helpMember = getNotAttackMember(members, attackMember);
			break;

		default:
			lcf().sdop.boss.isAutoBattle = false;
			attackMember = getFixAttackMember(members);
			helpMember = getFixHelpMember(members, attackMember);
			break;
		}
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
		return checkAttackSkill4Card(player.card);
	}

	/**
	 * 返回true表示有攻击性技能
	 * 
	 * @param card
	 * @return
	 */
	public boolean checkAttackSkill4Card(Card card) {
		ActiveSkill[] activeSkillList = card.pilot.activeSkillList;
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

	public Ms getPlayerByOwnerId(int ownerId) {
		for (Ms player : playerMsList) {
			if (ownerId == player.id) {
				return player;
			}
		}
		return null;
	}

	private int ownerId;
	private Ms _player;

	public void autoBattle(JSONObject battleArgs) throws JSONException {
		JSONArray actionOrderList = battleArgs.getJSONArray("actionOrderList");
		ActionOrder actionOrder = lcf().gson.fromJson(
				actionOrderList.getString(actionOrderList.length() - 1),
				ActionOrder.class);
		// 仅针对超总的判断, 或者可根据data.resultDate是否为空来判断
		if (!battleArgs.isNull("resultData")) {
			Log.i("Lucifer", battleArgs.toString());
			lcf().sdop.log("Boss战结束！");
			return;
		}

		_player = getPlayerByOwnerId(actionOrder.ownerId);
		if (_player == null) {
			lcf().sdop.log("没有对应的ownerId：" + actionOrder.ownerId);
			return;
		}
		ownerId = actionOrder.ownerId;

		checkSp(AutoBattle.procedure);
	}

	public void autoBattleAttack() {
		int actionCode = _player.getActionCode();
		itemTurn = false;
		Ms targetPlayer;
		int targetId, actionId;
		Skill skill;
		StringBuffer logMsg;
		if (_player.lcf_attack > 1) {
			logMsg = new StringBuffer(
					lcf().sdop.getRedBoldMsg(_player.card.userName));
		} else {
			logMsg = new StringBuffer("【").append(_player.card.userName)
					.append("】");
		}

		switch (actionCode) {
		case 0:// 普通攻击
			targetId = 1;
			actionId = _player.card.weaponList[0].id;
			lcf().sdop.boss.executeActionCommand(lcf().sdop.boss.battleId,
					lcf().sdop.boss.actionType[2], targetId, ownerId, actionId,
					ExecuteActionCommand.procedure);
			logMsg.append("对Boss使用武器").append(_player.card.weaponList[0].name)
					.append("进行攻击！");
			break;
		case 1:// 给队友加攻击状态
			targetPlayer = getFixAttackPlayerInBattle();
			targetId = targetPlayer.id;
			actionId = 21001;
			skill = lcf().sdop.pilot.activeSkill.get(actionId);
			lcf().sdop.currentSp -= skill.cost;
			lcf().sdop.boss.executeActionCommand(lcf().sdop.boss.battleId,
					lcf().sdop.boss.actionType[1], targetId, ownerId, actionId,
					ExecuteActionCommand.procedure);
			logMsg.append("对队友【").append(targetPlayer.card.userName)
					.append("】使用技能【").append(skill.prefix).append("】, 消耗SP：")
					.append(skill.cost).append(", 剩余SP：")
					.append(lcf().sdop.currentSp).append("！");
			break;
		case 2:// 给队友加速度状态
			targetPlayer = getFixAttackPlayerInBattle();
			targetId = targetPlayer.id;
			actionId = 21002;
			skill = lcf().sdop.pilot.activeSkill.get(actionId);
			lcf().sdop.currentSp -= skill.cost;
			lcf().sdop.boss.executeActionCommand(lcf().sdop.boss.battleId,
					lcf().sdop.boss.actionType[1], targetId, ownerId, actionId,
					ExecuteActionCommand.procedure);
			logMsg.append("对队友【").append(targetPlayer.card.userName)
					.append("】使用技能【").append(skill.prefix).append("】, 消耗SP：")
					.append(skill.cost).append(", 剩余SP：")
					.append(lcf().sdop.currentSp).append("！");
			break;
		case 3:// 单体技能攻击
			targetId = 1;
			actionId = _player.card.weaponList[0].id;
			int actionTypeIndex = 2;
			ActiveSkill attackSkill = getAttackSkill(_player);
			if (attackSkill != null) {
				actionId = attackSkill.id;
				actionTypeIndex = 1;
				lcf().sdop.currentSp -= attackSkill.cost;
				logMsg.append("对Boss使用技能【").append(attackSkill.description)
						.append("】, 消耗SP：").append(attackSkill.cost)
						.append(", 剩余SP：").append(lcf().sdop.currentSp)
						.append("！");
			} else {
				logMsg.append("对Boss使用武器")
						.append(_player.card.weaponList[0].name)
						.append("进行攻击！");
			}
			lcf().sdop.boss.executeActionCommand(lcf().sdop.boss.battleId,
					lcf().sdop.boss.actionType[actionTypeIndex], targetId,
					ownerId, actionId, ExecuteActionCommand.procedure);
			break;
		}
		_player.AITurn++;
		lcf().sdop.log(logMsg.toString());
	}

	/**
	 * 获取合适的倍机
	 * 
	 * @return
	 */
	public Ms getFixAttackPlayerInBattle() {
		Ms _fixPlayer = null;
		for (Ms _player : attackPlayers) {
			if (_fixPlayer == null) {
				_fixPlayer = _player;
				continue;
			}
			if (_fixPlayer.lcf_attack_buff > _player.lcf_attack_buff) {// 状态的次数多
				_fixPlayer = _player;
				continue;
			}
			if (_fixPlayer.lcf_attack < _player.lcf_attack) {// 相同时比较倍数
				_fixPlayer = _player;
				continue;
			}
		}
		return _fixPlayer;
	}

	private boolean itemTurn;

	/**
	 * 检查当前sp, 根据条件执行
	 * 
	 * @param callback
	 */
	public void checkSp(String callback) {
		lcf().sdop.log("开始校验SP！当前SP：" + lcf().sdop.currentSp + ", 最大SP："
				+ lcf().sdop.maxSp + ", 上一个动作是否使用物品：" + itemTurn);
		if (lcf().sdop.maxSp - lcf().sdop.currentSp <= 30 || itemTurn) {
			lcf().sdop.checkCallback(callback);
			return;
		}

		int _itemId = 20006;
		if (lcf().sdop.maxSp - lcf().sdop.currentSp > 30) {
			for (Item _item : itemList) {
				if (_item.id == _itemId) {
					if (_item.num > 0) {
						_item.num--;
						lcf().sdop.currentSp += 30;
						itemTurn = true;
						lcf().sdop.log("使用【" + _item.name + "】，当前SP："
								+ lcf().sdop.currentSp);

						lcf().sdop.boss.executeActionCommand(
								lcf().sdop.boss.battleId,
								lcf().sdop.boss.actionType[0], ownerId,
								ownerId, _itemId, callback);
						return;
					}
				}
			}
		}

		_itemId = 20013;
		if (lcf().sdop.currentSp < 30) {
			for (Item _item : itemList) {
				if (_item.id == _itemId) {
					if (_item.num > 0) {
						_item.num--;
						lcf().sdop.currentSp += 60;
						itemTurn = true;
						lcf().sdop.log("使用【" + _item.name + "】，当前SP："
								+ lcf().sdop.currentSp);

						lcf().sdop.boss.executeActionCommand(
								lcf().sdop.boss.battleId,
								lcf().sdop.boss.actionType[0], ownerId,
								ownerId, _itemId, callback);
						return;
					}
				}
			}
		}

		lcf().sdop.checkCallback(callback);
	}

	public ActiveSkill getAttackSkill(Ms player) {
		ActiveSkill[] activeSkillList = player.card.pilot.activeSkillList;
		if (activeSkillList == null) {
			return null;
		}
		for (ActiveSkill activeSkill : activeSkillList) {
			if (activeSkill.description.startsWith(actionCode[3].prefix)) {
				return activeSkill;
			}
		}
		return null;
	};

	/**
	 * 开始自动超总, UI调用
	 */
	public void startAutoSuperRaidBoss() {
		lcf().sdop.clearAllJob();
		lcf().sdop.auto.setting.boss = true;
		lcf().sdop.boss.currentType = 1;
		lcf().sdop.boss
				.initRaidBossOutlineList(InitRaidBossOutlineList.procedure);
	}

	/**
	 * 开始自动总力, UI调用
	 */
	public void startAutoNormalRaidBoss() {
		lcf().sdop.clearAllJob();
		lcf().sdop.auto.setting.boss = true;
		lcf().sdop.boss.currentType = 0;
		lcf().sdop.boss
				.initRaidBossOutlineList(InitRaidBossOutlineList.procedure);
	}

	/**
	 * 取消自动超总, UI调用
	 */
	public void cancelAutoSuperRaidBoss() {
		lcf().sdop.clearAllJob();
		lcf().sdop.log("自动超总停止成功！");
	}
}
