package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.HeaderDetail;

public class GetDuelData extends BaseDispatch {

	public static final String procedure = "getDuelData";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			lcf().sdop.auto.setting.duel = false;
			lcf().sdop.bp = 0;
			lcf().sdop.ep = 0;
			return;
		}
		HeaderDetail headerDetail = lcf().gson.fromJson(
				args.getString("headerDetail"), HeaderDetail.class);
		lcf().sdop.bp = headerDetail.bpDetail.currentValue;
		lcf().sdop.ep = headerDetail.energyDetail.energy;

		headerDetail = null;

		lcf().sdop.checkCallback(callback);
	}

	@Override
	public void callback(Object[] args) {
		// TODO Auto-generated method stub
		
	}

}
