package cn.lucifer.sdop;

import java.io.File;
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

	public void executeDuelBattle(Player enemy) {
		if (enemy == null) {
			Log.e("Lucifer", "executeDuelBattle targetId is null !");
			return;
		}

		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForQuestBattle/executeDuelBattle?ssid="
				+ lcf().sdop.ssid;

		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					ExecuteDuelBattle.procedure,
					new JSONObject().put("isEncount", false).put("id",
							enemy.playerId));
			checkEnemy(enemy);
			lcf().sdop.post(url, payload.toString(),
					ExecuteDuelBattle.procedure, null);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void checkAndExecute() {
		if (!lcf().sdop.auto.setting.duel) {
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
				Log.i("Lucifer", "autoDuel ----------");
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
		duelDB.execSQL("create table if not exists duel_enemy (id int primary key, name varchar(50), win int , lost int, gap int, lastTime timestamp, winTime timestamp, lostTime timestamp, unitAttribute varchar(10))");

		recordMode = true;
		lcf().sdop.log("成功开启GB记录模式！");
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

	/**
	 * 检查记录是否存在, 不存在则添加记录
	 * 
	 * @param enemy
	 */
	protected void checkEnemy(Player enemy) {
		if (null == duelDB) {
			return;
		}
		if (!duelDB.isOpen()) {
			if (!startRecordMode()) {
				return;
			}
		}
		String sql = "select id from duel_enemy where id = " + enemy.playerId;
		Cursor cursor = duelDB.rawQuery(sql, null);
		if (!cursor.moveToFirst()) {// 没有记录
			sql = "insert into duel_enemy (id, name, win, lost, gap) values(?,?,0,0,0)";// 因为没找到建表时是否能使用默认值的说明
			duelDB.execSQL(sql,
					new Object[] { enemy.playerId, enemy.playerName });
		}
		sql = null;
	}

	/**
	 * 
	 * @param isWin
	 * @param name
	 *            挑战的name, 因为返回结果那里, 并没有id, 所以只能通过name进行匹配
	 * @param unitAttribute
	 *            对方的属性
	 */
	public void updateDuelRecord(boolean isWin, String name,
			String unitAttribute) {
		if (null == duelDB) {
			return;
		}
		if (!duelDB.isOpen()) {
			if (!startRecordMode()) {
				return;
			}
		}
		String sql;
		if (isWin) {
			sql = "update duel_enemy set win = win + 1, gap = gap + 1, winTime = ?, lastTime = ?, unitAttribute = ? where name = ?";
		} else {
			sql = "update duel_enemy set lost = lost + 1, gap = gap - 1, lostTime = ?, lastTime = ?, unitAttribute = ? where name = ?";
		}
		Date now = new Date();
		duelDB.execSQL(sql, new Object[] { now, now, unitAttribute, name });
		sql = null;
	}
}
