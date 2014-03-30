package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class EnhancedSynthesis extends BaseDispatch {

	public static final String procedure = "enhancedSynthesis";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			lcf().sdop.synthesis.refreshMSCardListAndBroadcast(null);
			return;
		}

		lcf().sdop.checkCallback(callback, new Object[] { lcf().sdop.synthesis
				.getMsCardList(args.getString("msCardList")) });
	}

	@Override
	public void callback(Object[] args) {
		// TODO Auto-generated method stub

	}

}
