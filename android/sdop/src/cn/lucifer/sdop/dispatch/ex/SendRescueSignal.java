package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class SendRescueSignal extends BaseDispatch {
	public static final String procedure = "sendRescueSignal";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}

		lcf().sdop.checkCallback(callback);
		lcf().sdop.log("成功发送Help信息!");
	}

	@Override
	public void callback(Object[] args) {
		// TODO Auto-generated method stub

	}

}
