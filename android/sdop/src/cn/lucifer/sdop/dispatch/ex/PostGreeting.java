package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class PostGreeting extends BaseDispatch {

	public static final String procedure = "postGreeting";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		// JSONObject json = new JSONObject(new String(response));
		lcf().sdop.log("hello 成功");
	}

	@Override
	public void callback(Object[] args) {
		// TODO Auto-generated method stub
		
	}

}
