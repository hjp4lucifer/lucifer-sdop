package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.args.GetSneakingMissionTopDataArgs;

public class GetSneakingMissionTopData extends BaseDispatch {
	public static final String procedure = "GetSneakingMissionTopData";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		GetSneakingMissionTopDataArgs dataArgs = lcf().gson.fromJson(
				args.toString(), GetSneakingMissionTopDataArgs.class);
		if (null == callback) {//UI直接调用, 非AI
			lcf().sdop.sneaking.refreshMissonAndBroadcast(dataArgs);
		}
	}

	@Override
	public void callback(Object[] args) {
		// TODO Auto-generated method stub

	}

}
