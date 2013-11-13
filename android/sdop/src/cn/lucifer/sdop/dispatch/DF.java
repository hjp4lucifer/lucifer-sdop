package cn.lucifer.sdop.dispatch;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import cn.lucifer.sdop.dispatch.ex.Enter;
import cn.lucifer.sdop.dispatch.ex.GetEntryData;
import cn.lucifer.sdop.dispatch.ex.PostGreeting;

import android.util.Log;

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
