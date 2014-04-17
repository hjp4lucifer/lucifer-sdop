package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import cn.lucifer.sdop.dispatch.BaseDispatch;

public class DrawChancePanel extends BaseDispatch {

	public static final String procedure = "drawChancePanel";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		// Log.d(lcf().LOG_TAG, args.toString());
		lcf().sdop.log("抽奖结果: " + args.getString("reward"));
		lcf().sdop.map.clearNodeId();
		lcf().sdop.checkCallback(callback);
	}

	@Override
	public void callback(Object[] args) {
	}

}
