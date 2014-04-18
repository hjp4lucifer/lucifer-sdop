package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.ChancePanelReward;

public class DrawChancePanel extends BaseDispatch {

	public static final String procedure = "drawChancePanel";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		// Log.d(lcf().LOG_TAG, args.toString());
		ChancePanelReward reward = lcf().gson.fromJson(args.getString("reward"), ChancePanelReward.class);
		lcf().sdop.log("抽奖结果: " + reward.getInfo());
		lcf().sdop.map.clearNodeId();
		lcf().sdop.checkCallback(callback);
	}

	@Override
	public void callback(Object[] args) {
	}

}
