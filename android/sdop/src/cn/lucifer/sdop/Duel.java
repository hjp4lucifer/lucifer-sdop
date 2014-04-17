package cn.lucifer.sdop;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import cn.lucifer.sdop.dispatch.ex.ExecuteDuelBattle;
import cn.lucifer.sdop.dispatch.ex.GetDuelData;
import cn.lucifer.sdop.dispatch.ex.GetEntryData;
import cn.lucifer.sdop.domain.Player;
import cn.lucifer.sdop.e.CannotOpenDBException;

public class Duel extends LcfExtend {
	public String targetUnitAttribute = "FIGHT";

	public void getEntryData(String callback) {
		String url = lcf().sdop.httpUrlPrefix + "/GetForDuel/getEntryData?"
				+ lcf().sdop.createGetParams();
		lcf().sdop.get(url, GetEntryData.procedure, callback);
	}

	public void getDuelData(String callback) {
		String url = lcf().sdop.httpUrlPrefix + "/GetForDuel/getDuelData?"
				+ lcf().sdop.createGetParams();
		lcf().sdop.get(url, GetDuelData.procedure, callback);
	}

	/**
	 * 请求决斗数据的次数, 基于始终无法找到对手, 而设置的数量控制
	 */
	private int requestEneryDataCount = 0;

	/**
	 * 查找合适的敌人进行挑战
	 * 
	 * @param enemyList
	 */
	public void findEnemyAndBattle(Player[] enemyList) {
		requestEneryDataCount++;
		Player enemy;
		if (recordMode) {
			enemy = findByRecords(enemyList);
		} else {
			enemy = findBySimple(enemyList);
		}

		executeDuelBattle(enemy);
	}

	/**
	 * 
	 * @param enemy
	 * @return true是选择的属性
	 */
	protected boolean checkUnitAttribute(Player enemy) {
		return enemy.unitAttribute.value
				.equals(lcf().sdop.duel.targetUnitAttribute);
	}

	/**
	 * 普通查找
	 * 
	 * @param enemyList
	 * @return
	 */
	protected Player findBySimple(Player[] enemyList) {
		for (Player enemy : enemyList) {
			if (checkUnitAttribute(enemy)) {
				return enemy;
			}
		}
		Player player = null;
		if (requestEneryDataCount > 3) {
			String tmpUnitAttr = lcf().sdop.ms
					.getReverseUnitAttribute(targetUnitAttribute);
			lcf().sdop.log("3次都无法找到目标, 更改目标为：" + tmpUnitAttr);
			for (Player enemy : enemyList) {
				if (player == null) {
					player = enemy;
				}
				if (enemy.unitRarity > 2
						&& enemy.unitAttribute.value.equals(tmpUnitAttr)) {
					return enemy;
				}
			}
		}
		return player;
	}

	/**
	 * 优先从记录中查找, 若无法找到, 则从{@link #findBySimple(Player[])}中查找
	 * 
	 * @param enemyList
	 * @return
	 */
	protected Player findByRecords(Player[] enemyList) {
		try {
			checkAndOpenDB();
		} catch (CannotOpenDBException e) {
			return findBySimple(enemyList);
		}
		// 这里有两种构想:
		// 1. 先遍历所有的enemyList, 提取id后使用sql的in语法进行查找
		// 2. 先sql找出符合条件的enemy id集合, 然后通过遍历enemyList找到第一个匹配的enemy
		// 基于方法一永远是一样的查询数, 而方法二应该会在当天逐渐减少, 所以这里使用的是方法二

		// 每天挑战2次就好了, 否则就作弊得太厉害了
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, -12);
		long beginTime = calendar.getTimeInMillis();
		String sql = "select id, win, lost from duel_enemy where gap > 0 and lastTime < "
				+ beginTime + " order by gap desc";
		Cursor cursor = duelDB.rawQuery(sql, null);

		// List<Integer> targetIds = new ArrayList<Integer>();
		int id;
		int findCount = 0;
		while (cursor.moveToNext()) {
			id = cursor.getInt(0);
			for (Player enemy : enemyList) {
				findCount++;
				if (id == enemy.playerId
				// && checkUnitAttribute(enemy)
				) {// 防止对方更改属性
					String msg = String
							.format("Record mode: Get recommend: 【%s】（win: %d, lost: %d）! Find count : %d",
									enemy.playerName, cursor.getInt(1),
									cursor.getInt(2), findCount);
					Log.i(lcf().LOG_TAG, msg);
					lcf().sdop.log(msg);
					cursor.close();
					return enemy;
				}
			}
		}
		String msg = "Record mode: No recommend! Find count : " + findCount;
		Log.i(lcf().LOG_TAG, msg);
		lcf().sdop.log(msg);
		cursor.close();

		// return enhancedRecordMode(enemyList);
		return findBySimple(enemyList);
	}

	/**
	 * 增强型的记录模式
	 * 
	 * @param enemyList
	 * @return
	 * @deprecated Ace S以下一般不要使用该模式
	 */
	protected Player enhancedRecordMode(Player[] enemyList) {
		if (requestEneryDataCount < 5) {
			return null;
		}

		// 排除操作
		int id;
		int findCount = 0;
		String sql = "select id from duel_enemy where gap < -1";
		Cursor cursor = duelDB.rawQuery(sql, null);
		String msg;
		Player enemyBackup = null;
		String tmpUnitAttr = lcf().sdop.ms
				.getReverseUnitAttribute(targetUnitAttribute);
		while (cursor.moveToNext()) {
			id = cursor.getInt(0);
			for (Player enemy : enemyList) {
				findCount++;
				if (id == enemy.playerId) {
					continue;
				}
				if (checkUnitAttribute(enemy)) {
					msg = "Record mode: Exclude Mode! Find count : "
							+ findCount;
					Log.i(lcf().LOG_TAG, msg);
					lcf().sdop.log(msg);
					cursor.close();
					return enemy;
				} else if (enemy.unitRarity > 2
						&& enemy.unitAttribute.value.equals(tmpUnitAttr)) {
					enemyBackup = enemy;
				}
			}
		}
		msg = "Record mode: Exclude Mode! Find count : " + findCount;
		Log.i(lcf().LOG_TAG, msg);
		lcf().sdop.log(msg);
		cursor.close();
		if (enemyBackup != null) {
			lcf().sdop.log("3次以上都无法找到目标, 更改目标为：" + tmpUnitAttr);
			return enemyBackup;
		}

		return findBySimple(enemyList);
	}

	protected void executeDuelBattle(Player enemy) {
		if (enemy == null) {
			lcf().sdop
					.log("executeDuelBattle targetId is null ! try again ! -->"
							+ requestEneryDataCount);
			checkAndExecute();
			return;
		}
		requestEneryDataCount = 0;

		try {
			checkEnemy(enemy);
		} catch (CannotOpenDBException e) {
			// 不需要处理
		}

		executeDuelBattle(enemy, false);
	}

	/**
	 * 
	 * @param enemy
	 * @param isEncount
	 *            true遭遇战
	 */
	public void executeDuelBattle(Player enemy, boolean isEncount) {
		lcf().sdop.log(String.format("准备对【%s】 发起挑战, 对方属性是【%s】 %s!",
				enemy.playerName, enemy.unitAttribute.value, enemy.unitName));

		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForQuestBattle/executeDuelBattle?ssid="
				+ lcf().sdop.ssid;
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					ExecuteDuelBattle.procedure,
					new JSONObject().put("isEncount", isEncount).put("id",
							enemy.playerId));
			lcf().sdop.post(url, payload.toString(),
					ExecuteDuelBattle.procedure, null);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void checkAndExecute() {
		if (!lcf().sdop.auto.setting.duel) {
			requestEneryDataCount = 0;
			return;
		}
		getDuelData(GetDuelData.procedure);
	}

	public void startAutoDuel() {
		lcf().sdop.clearAllJob();
		lcf().sdop.auto.setting.duel = true;
		lcf().sdop.log("针对【" + targetUnitAttribute + "】的自动GB开始！");
		lcf().sdop.startJob(new Runnable() {
			@Override
			public void run() {
				//Log.d("Lucifer", "autoDuel ----------");
				lcf().sdop.duel.checkAndExecute();
			}
		}, 0, 300000);
	}

	public void cancelAutoDuel() {
		lcf().sdop.auto.setting.duel = false;
		lcf().sdop.clearAllJob();
		lcf().sdop.log("自动GB停止成功！");
	}

	/**
	 * 记录模式, true开启
	 */
	public boolean recordMode;
	public SQLiteDatabase duelDB;

	/**
	 * 开启记录模式
	 */
	public boolean startRecordMode() {
		if (recordMode) {
			lcf().sdop.log("已开启记录模式！");
			return false;
		}
		File dir = lcf().getSdDirectory();
		if (dir == null) {
			lcf().sdop.log("没有SD卡不能开启记录模式！");
			return false;
		}
		duelDB = SQLiteDatabase.openOrCreateDatabase(dir.getPath()
				+ "/lucifer_sdop_duel.db", null);
		// 检查表结构
		duelDB.execSQL("create table if not exists duel_enemy (id INTEGER primary key, name TEXT, win INTEGER , lost INTEGER, gap INTEGER, lastTime TEXT, winTime TEXT, lostTime TEXT, unitAttribute TEXT, rankName TEXT)");

		recordMode = true;
		lcf().sdop.log("成功开启GB记录模式！");
		// testDateQuery();
		return true;
	}

	/**
	 * 释放记录模式
	 */
	public void releasesRecordMode() {
		if (null == duelDB) {
			return;
		}
		duelDB.close();
	}

	protected void checkAndOpenDB() throws CannotOpenDBException {
		if (null == duelDB) {
			throw new CannotOpenDBException();
		}
		if (!duelDB.isOpen()) {
			if (!startRecordMode()) {
				throw new CannotOpenDBException();
			}
		}
	}

	/**
	 * 检查记录是否存在, 不存在则添加记录
	 * 
	 * @param enemy
	 * @throws CannotOpenDBException
	 */
	protected void checkEnemy(Player enemy) throws CannotOpenDBException {
		checkAndOpenDB();
		String sql = "select id from duel_enemy where id = " + enemy.playerId;
		Cursor cursor = duelDB.rawQuery(sql, null);
		if (!cursor.moveToFirst()) {// 没有记录
			sql = "insert into duel_enemy (id, name, win, lost, gap) values(?,?,0,0,0)";// 因为没找到建表时是否能使用默认值的说明
			duelDB.execSQL(sql,
					new Object[] { enemy.playerId, enemy.playerName });
		}
		cursor.close();
	}

	/**
	 * 
	 * @param isWin
	 * @param name
	 *            挑战的name, 因为返回结果那里, 并没有id, 所以只能通过name进行匹配
	 * @param unitAttribute
	 *            对方的属性
	 * @param rankName
	 *            对方级别
	 * @throws CannotOpenDBException
	 */
	public void updateDuelRecord(boolean isWin, String name,
			String unitAttribute, String rankName) throws CannotOpenDBException {
		checkAndOpenDB();
		String sql;
		if (isWin) {
			sql = "update duel_enemy set win = win + 1, gap = gap + 1, winTime = ?, lastTime = ?, unitAttribute = ?, rankName = ? where name = ?";
		} else {
			sql = "update duel_enemy set lost = lost + 1, gap = gap - 1, lostTime = ?, lastTime = ?, unitAttribute = ?, rankName = ? where name = ?";
		}
		long now = System.currentTimeMillis();
		duelDB.execSQL(sql, new Object[] { now, now, unitAttribute, rankName,
				name });
	}

	/**
	 * 测试日期查询
	 * 
	 * @deprecated 一般不要进行调用
	 */
	protected void testDateQuery() {
		try {
			checkAndOpenDB();
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR_OF_DAY, -24);
			Date time = calendar.getTime();
			Log.d("Lucifer",
					lcf().sdop.timeFormat.format(time) + " : " + time.getTime());
			String sql = "select id, lastTime from duel_enemy where lastTime > "
					+ time.getTime();// 今天打了多少场
			Cursor cursor = duelDB.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				// 为什么可以getLong出来呢？我明明声明的是Text类型
				Log.d(lcf().LOG_TAG,
						cursor.getInt(0) + " : " + cursor.getLong(1));
			}
			cursor.close();
		} catch (CannotOpenDBException e) {
			e.printStackTrace();
		}
	}
}
