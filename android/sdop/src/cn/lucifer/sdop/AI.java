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
import cn.lucifer.sdop.dispatch.ex.PostRaidBossBattleEntry;
import cn.lucifer.sdop.dispatch.ex.SendRescueSignal;
import cn.lucifer.sdop.domain.ActionOrder;
import cn.lucifer.sdop.domain.ActiveSkill;
import cn.lucifer.sdop.domain.Card;
import cn.lucifer.sdop.domain.CardWithoutWeapon;
import cn.lucifer.sdop.domain.Item;
import cn.lucifer.sdop.domain.Ms;
import cn.lucifer.sdop.domain.PassiveSkill;
import cn.lucifer.sdop.e.NewRequestException;

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
	 * 获取当前AI中使用的member
	 * 
	 * @return
	 */
	public CardWithoutWeapon[] getCurrentMembers() {
		return new CardWithoutWeapon[] { attackMember, helpMember };
	}

	/**
	 * 最少血量
	 */
	protected final int Least_Hp = 8000000;

	/**
	 * 最少等级
	 */
	public int Least_Lv = 3;

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
				if ("JOIN".equalsIgnoreCase(_currentBoss.state.value)) {// 已参战
					continue;
				}
				if (_currentBoss.currentHp < Least_Hp) {// 判断血量
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
				if (_currentBoss.level < Least_Lv) {
					continue;
				}
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
	 * 查找自己的信息
	 * 
	 * @param members
	 * @return
	 */
	public CardWithoutWeapon getMyInfo(CardWithoutWeapon[] members) {
		for (CardWithoutWeapon m : members) {
			if (m.id == lcf().sdop.myUserId) {// 是自己
				if (lcf().sdop.boss.checkX6(m)) {
					m.lcf_attack = 6;
				}
				return m;
			}
		}
		return null;// 理论上不存在该情况
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
			if (!checkAttackSkill4Card(m)) {
				continue;// 不选择没攻击技能的倍机
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
		return getFixHelpMember(members, attackMember, null);
	}

	/**
	 * 必须在调用完{@link #getFixAttackMember(Card[])}后才能调用
	 * 
	 * @param members
	 * @param attackMember
	 * @param noChooseId
	 *            不选择的MS的id
	 * @return
	 */
	public CardWithoutWeapon getFixHelpMember(CardWithoutWeapon[] members,
			CardWithoutWeapon attackMember, Integer noChooseId) {
		if (attackMember == null) {
			Log.e(lcf().LOG_TAG, "attackMember is null !");
			printStackTrace();
			return null;
		}
		try {
			attackMember.lcf_speed = getTrueSpeed(attackMember);
		} catch (Exception e) {
			e.printStackTrace();
		}
		CardWithoutWeapon helpMember = null;
		ActiveSkill skill;
		for (CardWithoutWeapon m : members) {
			if (m.id == lcf().sdop.myUserId) {// 不能是自己
				continue;
			}
			if (noChooseId != null && noChooseId == m.id) {// 不选择该MS
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
	 * @param setAi
	 *            true表示设置进AI里, 作为AI自动打参数
	 * @return array index 0表示attackMember, 1表示helpMember
	 * @throws JSONException
	 * @throws NewRequestException
	 */
	public CardWithoutWeapon[] setFixMember(JSONObject battleArgs, boolean setAi)
			throws JSONException, NewRequestException {
		// Log.d(lcf().LOG_TAG, "battleArgs : " + battleArgs.toString());
		lcf().sdop.myUserId = battleArgs.getInt("leaderCardId");

		Ms[] playerMsList = lcf().gson.fromJson(
				battleArgs.getString("playerMsList"), Ms[].class);

		Card myCard = playerMsList[0].card;
		lcf().sdop.currentSp = myCard.currentSp + myCard.currentSubSp;
		lcf().sdop.maxSp = myCard.maxSp;

		lcf().sdop.boss.battleId = battleArgs.getInt("battleId");

		CardWithoutWeapon[] members = lcf().gson.fromJson(
				battleArgs.getString("memberCardList"),
				CardWithoutWeapon[].class);

		CardWithoutWeapon attackMember, helpMember;
		switch (lcf().sdop.boss.currentType) {
		case 0:
			lcf().sdop.boss.isAutoBattle = true;
			attackMember = getNotAttackMember(members, null);
			helpMember = getNotAttackMember(members, attackMember);
			break;

		default: {
			lcf().sdop.boss.isAutoBattle = false;
			CardWithoutWeapon myInfo = getMyInfo(members);
			switch (myInfo.lcf_attack) {
			case 6: {
				int level = battleArgs.getJSONObject("raidBossData").getInt(
						"level");
				if (level == 5) {// lv5则出双倍机
					attackMember = getFixAttackMember(members);
					helpMember = getFixHelpMember(members, attackMember);
				} else {
					helpMember = getFixHelpMember(members, myInfo);
					attackMember = getFixHelpMember(members, myInfo,
							helpMember.id);
				}
				break;
			}
			default:
				attackMember = getFixAttackMember(members);
				helpMember = getFixHelpMember(members, attackMember);
				break;
			}
			break;
		}
		}

		if (setAi) {// 不设置Ai一般表示总力, 无需查看智能甲板
			this.attackMember = attackMember;
			processByAutoCardPlatoon(myCard, attackMember);
			this.helpMember = helpMember;
		}
		return new CardWithoutWeapon[] { attackMember, helpMember };
	}

	/**
	 * 处理智能甲板问题
	 * 
	 * @param attackMember
	 * @throws NewRequestException
	 *             有新的请求发出, 需终止当前逻辑
	 */
	public void processByAutoCardPlatoon(Card myCard,
			CardWithoutWeapon attackMember) throws NewRequestException {
		if (!lcf().sdop.auto.setting.cardPlatoon) {
			return;
		}
		if (null == attackMember) {
			return;
		}
		int id;
		if (attackMember.lcf_attack >= 3) {
			// 理论上, 自己x6时不会进入该逻辑
			id = lcf().sdop.cardPlatoon.helpPilot.id;
			if (myCard.pilot.id != id) {
				lcf().sdop.cardPlatoon.equipPilotByRaid(id,
						PostRaidBossBattleEntry.procedure);
				throw new NewRequestException();
			}
			return;
		}
		// 无倍机
		id = lcf().sdop.cardPlatoon.attackPilot.id;
		if (myCard.pilot.id != id) {// 自己更换成攻击向pilot
			lcf().sdop.cardPlatoon.equipPilotByRaid(id,
					PostRaidBossBattleEntry.procedure);
			throw new NewRequestException();
		}
		if (null == lcf().sdop.cardPlatoon.xTimesDamageMS) {
			return;
		}
		id = lcf().sdop.cardPlatoon.xTimesDamageMS.id;
		if (myCard.id != id) {
			lcf().sdop.cardPlatoon.selectLeaderByRaid(id,
					PostRaidBossBattleEntry.procedure);
			throw new NewRequestException();
		}
	}

	/**
	 * 检查自己的技能, 查看是否是符合条件的辅助技能
	 * 
	 * @param player
	 * @return
	 */
	public boolean checkMySkill(Ms player) {
		return checkMySkill4Card(player.card);
	}

	/**
	 * 检查自己的技能, 查看是否是符合条件的辅助技能
	 * 
	 * @param player
	 * @return true符合
	 */
	public boolean checkMySkill4Card(CardWithoutWeapon card) {
		ActiveSkill[] activeSkillList = card.pilot.activeSkillList;
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
	public boolean checkAttackSkill4Card(CardWithoutWeapon card) {
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
	private final int[] all_attack_help = { 1, 1, 1, 1, 0, 0, 0 };
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

		attackPlayers = new ArrayList<Ms>();

		Ms helpMember = null;
		if (setAttackAI() || setSecondAttackAIForAutoCardPlatoon()) {
			helpMember = whenMyIsAttack();
		} else {
			helpMember = setHelpAI();
		}

		if (attackPlayers.size() > 1 && helpMember != null) {// 2部倍机, 改变辅助机功能
			helpMember.AIType = all_attack_help;
		}

		if (attackPlayers.isEmpty()) {
			for (Ms player : playerMsList) {
				player.AIType = simple;
			}
		}
	}

	/**
	 * 有倍机, 但自己不是倍机时, 设置辅助机的AI
	 * 
	 * @return null没有辅助(不算自己)
	 */
	protected Ms setHelpAI() {
		Ms helpMember = null;
		for (Ms player : playerMsList) {
			if (player.card.id == lcf().sdop.myUserId) {
				if (checkMySkill(player)) {
					player.AIType = me;
				}
				continue;
			}
			if (player.lcf_attack > 0) {// 倍机, 无需变更AI
				continue;
			}
			if (checkHelpSkill(player)) {
				helpMember = player;
				player.AIType = help;
			}
		}
		return helpMember;
	}

	/**
	 * 这里模拟用户角度, 先看看出场者的倍机情况
	 * 
	 * @return true自己也是倍机
	 */
	protected boolean setAttackAI() {
		boolean myIsAttack = false;
		for (Ms player : playerMsList) {
			player.AIType = simple;
			player.AITurn = 0;
			if (lcf().sdop.boss.checkX6(player.card)
					|| lcf().sdop.boss.checkX3(player.card)) {
				if (checkRealAttack(player)) {
					if (player.card.id == lcf().sdop.myUserId) {// 判断自己是否倍机
						myIsAttack = true;
					}
				}
				continue;
			}
		}
		return myIsAttack;
	}

	/**
	 * 第二次查看并设置攻击向AI, 当启用了智能甲板后
	 * 
	 * @return true自己也是倍机
	 */
	protected boolean setSecondAttackAIForAutoCardPlatoon() {
		boolean myIsAttack = false;
		if (lcf().sdop.auto.setting.cardPlatoon && attackPlayers.isEmpty()) {
			// 查看是否有攻击向x2
			for (Ms player : playerMsList) {
				if (lcf().sdop.boss.checkX2(player.card)
						&& checkRealAttack(player)) {
					if (player.card.id == lcf().sdop.myUserId) {// 判断自己是否倍机
						myIsAttack = true;
					}
				}
			}
		}
		return myIsAttack;
	}

	/**
	 * 检查是否带攻击技能的倍机(真正的倍机), 假若有, 设置AI, 并加入倍机List
	 * 
	 * @param player
	 * @return true有
	 */
	protected boolean checkRealAttack(Ms player) {
		if (checkAttackSkill(player)) {
			player.AIType = attack;
			player.lcf_attack = player.card.lcf_attack;// 设置倍数信息
			attackPlayers.add(player);
			return true;
		}
		return false;
	}

	/**
	 * 当自己的是倍机时, 设置辅助机的AI
	 * 
	 * @return null表示没有辅助机
	 */
	protected Ms whenMyIsAttack() {
		Ms helpMember = null;
		boolean noDoubleHelp = true;
		for (Ms player : playerMsList) {
			if (checkHelpSkill(player)) {
				helpMember = player;
				if (noDoubleHelp && checkMySkill(player)) {// 当前无双辅助机
					player.AIType = me;
					noDoubleHelp = false;
					continue;
				}
				player.AIType = help;
			}
		}
		return helpMember;
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
			// Log.d(lcf().LOG_TAG, battleArgs.toString());
			lcf().sdop.log("Boss战结束！");

			if (lcf().sdop.boss.targetBossId != null
					&& lcf().sdop.boss.encountType == 1) {// 遭遇发送help信息
				lcf().sdop.checkCallback(SendRescueSignal.procedure, 2000,
						new Object[] { lcf().sdop.boss.targetBossId });
				lcf().sdop.boss.targetBossId = null;
				lcf().sdop.boss.encountType = null;
			}
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
		if (_fixPlayer != null) {
			_fixPlayer.lcf_attack_buff++;
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
		lcf().sdop.clearAllJobWithResume();
		lcf().sdop.auto.setting.boss = true;
		lcf().sdop.boss.currentType = 1;
		lcf().sdop.boss
				.initRaidBossOutlineList(InitRaidBossOutlineList.procedure);
	}

	/**
	 * 开始自动总力, UI调用
	 */
	public void startAutoNormalRaidBoss() {
		lcf().sdop.clearAllJobWithResume();
		lcf().sdop.auto.setting.boss = true;
		lcf().sdop.boss.currentType = 0;
		lcf().sdop.boss
				.initRaidBossOutlineList(InitRaidBossOutlineList.procedure);
	}

	/**
	 * 取消自动超总, UI调用
	 */
	public void cancelAutoSuperRaidBoss() {
		lcf().sdop.clearAllJobWithResume();
		lcf().sdop.log("自动超总停止成功！");
	}
}
