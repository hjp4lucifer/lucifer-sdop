package cn.lucifer.sdop.dispatch;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public final class DF {

	protected static Map<String, IProcedure> map;

	public static void init() {
		if (map != null) {
			return;
		}
		map = new HashMap<String, IProcedure>();
	}

	private static void put(String procedure, IProcedure impl) {
		if (map.containsKey(procedure)) {
			throw new RuntimeException("procedure key : " + procedure + " 重复！");
		}
		map.put(procedure, impl);
	}

	public static IProcedure get(String procedure) {
		return map.get(procedure);
	}

	public static void dispatch(String callbackProcedure, byte[] response) {
		if (callbackProcedure == null) {
			Log.e("Lucifer", "callbackProcedure is null");
		}
		if (response == null) {
			Log.e("Lucifer", "no response !");
		}
		IProcedure iProcedure = get(callbackProcedure);
		if (iProcedure == null) {
			Log.e("Lucifer", "iProcedure is null");
		}

		iProcedure.callback(response);
	}
}
