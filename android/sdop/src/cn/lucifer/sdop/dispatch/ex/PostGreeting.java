package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class PostGreeting extends BaseDispatch {

	public static final String procedure = "postGreeting";

	@Override
	public void callback(byte[] response) throws JSONException {
		// JSONObject json = new JSONObject(new String(response));
		lcf().sdop.log("hello 成功");
	}

}
