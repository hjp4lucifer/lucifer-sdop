package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

/**
 * 潜入任务获取结果
 * 
 * @author Lucifer
 * 
 */
public class GetResultData extends BaseDispatch {
	public static final String procedure = "getResultData";

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
		lcf().sdop.sneaking.sortieTroops();
	}

}
