package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.CardSynthesis;

public class GetMSCardEnhancedSynthesisData extends BaseDispatch {

	public static final String procedure = "getMSCardEnhancedSynthesisData";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		CardSynthesis[] msCardList = lcf().gson.fromJson(
				args.getString("msCardList"), CardSynthesis[].class);
		if (null == callback) {// UI直接调用, 非AI
			lcf().sdop.synthesis.refreshMSCardListAndBroadcast(msCardList);
		}
	}

	@Override
	public void callback(Object[] args) {
		// TODO Auto-generated method stub

	}

}
