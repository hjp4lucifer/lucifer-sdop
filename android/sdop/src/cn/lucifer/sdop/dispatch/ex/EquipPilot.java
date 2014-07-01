package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class EquipPilot extends BaseDispatch {
	public static final String procedure = "equipPilot";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}

		lcf().sdop.checkCallback(callback);
	}

	@Override
	public void callback(Object[] args) {
		// TODO Auto-generated method stub

	}

}
