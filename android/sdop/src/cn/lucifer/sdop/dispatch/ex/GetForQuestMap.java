package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class GetForQuestMap extends BaseDispatch {

	public static final String procedure = "GetForQuestMap";

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

	@Override
	public void callback(Object[] args) {
		
	}

}
