package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

/**
 * 执行新的潜入任务
 * @author Lucifer
 *
 */
public class SortieTroops extends BaseDispatch {
	public static final String procedure = "sortieTroops";

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
		lcf().sdop.sneaking.getSneakingMissionTopData();
	}

}
