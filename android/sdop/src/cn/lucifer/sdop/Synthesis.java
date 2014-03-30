package cn.lucifer.sdop;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import cn.lucifer.sdop.dispatch.ex.EnhancedSynthesis;
import cn.lucifer.sdop.dispatch.ex.GetMSCardEnhancedSynthesisData;
import cn.lucifer.sdop.domain.CardSynthesis;

/**
 * 合成
 * 
 * @author Lucifer
 * 
 */
public class Synthesis extends LcfExtend {

	public final String REFRESH_RECEIVER_MS_ACTION = "lcf.sdop.ui.MSSynthesis";
	public CardSynthesis[] msCardList;

	public void getMSCardEnhancedSynthesisData() {
		String url = lcf().sdop.httpUrlPrefix
				+ "/GetForMSCardEnhancedSynthesis/getMSCardEnhancedSynthesisData?"
				+ lcf().sdop.createGetParams() + "&isRequireTable=true";
		lcf().sdop.get(url, GetMSCardEnhancedSynthesisData.procedure,
				GetMSCardEnhancedSynthesisData.procedure);
	}

	public CardSynthesis[] getMsCardList(String msCardListJsonStr) {
		return lcf().gson.fromJson(msCardListJsonStr, CardSynthesis[].class);
	}

	/**
	 * 刷新数据, 并发送广播
	 * 
	 * @param msCardList
	 */
	public void refreshMSCardListAndBroadcast(CardSynthesis[] msCardList) {
		if (msCardList != null) {
			this.msCardList = msCardList;
		}
		if (null == lcf().sdop.context) {
			return;
		}
		Intent intent = new Intent(REFRESH_RECEIVER_MS_ACTION);
		lcf().sdop.context.sendBroadcast(intent);
	}

	public void enhancedSynthesis(int base, List<Integer> materials) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForMSCardEnhancedSynthesis/enhancedSynthesis?ssid="
				+ lcf().sdop.ssid;
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					"enhancedSynthesis",
					new JSONObject().put("materials",
							new JSONArray(lcf().gson.toJson(materials))).put(
							"base", base));
			lcf().sdop.post(url, payload.toString(),
					EnhancedSynthesis.procedure,
					GetMSCardEnhancedSynthesisData.procedure);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
