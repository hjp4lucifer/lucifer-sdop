package cn.lucifer.sdop.callback;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public final class CB {

	protected static Map<String, ICallback> map;

	public static void init() {
		if (map != null) {
			return;
		}
		map = new HashMap<String, ICallback>();
	}

	private static void put(String callback, ICallback impl) {
		if (map.containsKey(callback)) {
			throw new RuntimeException("callback key : " + callback + " 重复！");
		}
		map.put(callback, impl);
	}

	public static ICallback get(String callback) {
		return map.get(callback);
	}

	public static void dispatch(String callback, Object... args) {
		if (callback == null) {
			Log.e("Lucifer", "procedure is null");
		}
		ICallback iCallback = get(callback);
		if (iCallback == null) {
			Log.e("Lucifer", "iProcedure is null");
		}

		iCallback.callback(args);
	}
}
