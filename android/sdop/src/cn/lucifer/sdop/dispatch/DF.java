package cn.lucifer.sdop.dispatch;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import android.util.Log;

public final class DF {

	protected static Map<String, IProcedure> map;

	public static void init() {
		if (map != null) {
			return;
		}
		map = new HashMap<String, IProcedure>();
		map.put(Enter.procedure, new Enter());
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

	public static void dispatch(String callback, byte[] response) {
		if (callback == null) {
			Log.e("Lucifer", "callback is null");
		}
		if (response == null) {
			Log.e("Lucifer", "no response !");
		}
		IProcedure iProcedure = get(callback);
		if (iProcedure == null) {
			Log.e("Lucifer", "iProcedure is null");
		}

		try {
			iProcedure.callback(response);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
