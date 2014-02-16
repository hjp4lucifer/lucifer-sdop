package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class GetQuestData extends BaseDispatch {

	public static final String procedure = "getQuestData";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		int playerExist = args.getInt("playerExist");
		Log.i(lcf().LOG_TAG, "playerExist : " + playerExist);
		lcf().sdop.map.executeQuest(playerExist);
	}

	/**
	 * 延迟调度方法
	 */
	@Override
	public void callback(Object[] args) {
		Log.d(lcf().LOG_TAG, "call GetQuestData#callback !");
		lcf().sdop.map.getQuestData();
	}

}
