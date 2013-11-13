package cn.lucifer.sdop.dispatch;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import cn.lucifer.sdop.callback.ICallback;
import cn.lucifer.sdop.dispatch.ex.Enter;
import cn.lucifer.sdop.dispatch.ex.GetEntryData;
import cn.lucifer.sdop.dispatch.ex.PostGreeting;

import android.util.Log;

public final class DF {

	protected static Map<String, BaseDispatch> map;

	public static void init() {
		if (map != null) {
			return;
		}
		map = new HashMap<String, BaseDispatch>();
		
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

	public static BaseDispatch get(String procedure) {
		return map.get(procedure);
	}

	public static void dispatch(String procedure, byte[] response,
			String callback) {
		if (procedure == null) {
			Log.e("Lucifer", "procedure is null");
		}
		if (response == null) {
			Log.e("Lucifer", "no response !");
		}
		IProcedure iProcedure = get(procedure);
		if (iProcedure == null) {
			Log.e("Lucifer", "iProcedure is null");
		}

		try {
			iProcedure.process(response, callback);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void callback(String procedure, Object[] args) {
		if (procedure == null) {
			Log.e("Lucifer", "callback is null");
		}
		ICallback iCallback = get(procedure);
		if (iCallback == null) {
			Log.e("Lucifer", "iCallback is null");
		}

		iCallback.callback(args);
	}
}
