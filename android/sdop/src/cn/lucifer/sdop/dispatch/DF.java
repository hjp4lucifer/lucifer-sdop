package cn.lucifer.sdop.dispatch;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import cn.lucifer.sdop.dispatch.ex.AutoBattle;
import cn.lucifer.sdop.dispatch.ex.AutoSuperRaidBoss;
import cn.lucifer.sdop.dispatch.ex.Enter;
import cn.lucifer.sdop.dispatch.ex.EquipItem4Sp;
import cn.lucifer.sdop.dispatch.ex.ExecuteActionCommand;
import cn.lucifer.sdop.dispatch.ex.ExecuteBattleStart;
import cn.lucifer.sdop.dispatch.ex.ExecuteDuelBattle;
import cn.lucifer.sdop.dispatch.ex.GetDuelData;
import cn.lucifer.sdop.dispatch.ex.GetEntryData;
import cn.lucifer.sdop.dispatch.ex.GetRaidBossBattleData;
import cn.lucifer.sdop.dispatch.ex.GetRaidBossOutlineList;
import cn.lucifer.sdop.dispatch.ex.InitRaidBossOutlineList;
import cn.lucifer.sdop.dispatch.ex.PostGreeting;
import cn.lucifer.sdop.dispatch.ex.PostRaidBossBattleEntry;

public final class DF {

	protected static Map<String, IProcedure> map;

	public static void init() {
		if (map != null) {
			return;
		}
		map = new HashMap<String, IProcedure>();

		put(Enter.procedure, new Enter());
		put(PostGreeting.procedure, new PostGreeting());

		put(GetEntryData.procedure, new GetEntryData());
		put(GetDuelData.procedure, new GetDuelData());
		put(ExecuteDuelBattle.procedure, new ExecuteDuelBattle());

		put(EquipItem4Sp.procedure, new EquipItem4Sp());

		put(AutoBattle.procedure, new AutoBattle());
		put(AutoSuperRaidBoss.procedure, new AutoSuperRaidBoss());
		put(ExecuteActionCommand.procedure, new ExecuteActionCommand());
		put(ExecuteBattleStart.procedure, new ExecuteBattleStart());
		put(GetRaidBossBattleData.procedure, new GetRaidBossBattleData());
		put(GetRaidBossOutlineList.procedure, new GetRaidBossOutlineList());
		put(InitRaidBossOutlineList.procedure, new InitRaidBossOutlineList());
		put(PostRaidBossBattleEntry.procedure, new PostRaidBossBattleEntry());

	}

	private static void put(String procedure, BaseDispatch impl) {
		if (map.containsKey(procedure)) {
			throw new RuntimeException("procedure key : " + procedure + " 重复！");
		}
		map.put(procedure, impl);
	}

	public static IProcedure get(String procedure) {
		return map.get(procedure);
	}

	public static void dispatch(String procedure, byte[] response,
			String callback) {
		IProcedure iProcedure = get(procedure);

		try {
			iProcedure.process(response, callback);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void callback(String procedure, Object[] args) {
		IProcedure iProcedure = get(procedure);

		iProcedure.callback(args);
	}
}
