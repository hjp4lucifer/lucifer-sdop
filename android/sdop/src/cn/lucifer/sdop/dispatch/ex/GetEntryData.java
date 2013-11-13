package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class GetEntryData extends BaseDispatch {

	public static final String procedure = "getEntryData";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			lcf().sdop.auto.setting.duel = false;
			return;
		}

		JSONArray entryList = args.getJSONArray("list");
		lcf().sdop.checkCallback(callback, entryList);
	}

}
