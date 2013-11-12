package cn.lucifer.sdop.dispatch;

import org.json.JSONException;
import org.json.JSONObject;

public class Enter extends BaseDispatch {

	public static final String procedure = "enter";

	@Override
	public void callback(byte[] response) throws JSONException {
		JSONObject json = new JSONObject(new String(response));
		JSONObject args = json.getJSONObject("args");
		String tokenId = args.getString("tokenId");
		lcf().sdop.setTokenId(tokenId);
	}

}
