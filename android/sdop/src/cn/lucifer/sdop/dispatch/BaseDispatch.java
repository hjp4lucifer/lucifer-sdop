package cn.lucifer.sdop.dispatch;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.IGetLcf;
import cn.lucifer.sdop.Lcf;
import cn.lucifer.sdop.callback.ICallback;

public abstract class BaseDispatch implements IProcedure,ICallback, IGetLcf {
	@Override
	public Lcf lcf() {
		return Lcf.getInstance();
	}
	
	protected JSONObject getArgs(byte[] response) throws JSONException{
		JSONObject json = new JSONObject(new String(response));
		return json.getJSONObject("args");
	}
}
