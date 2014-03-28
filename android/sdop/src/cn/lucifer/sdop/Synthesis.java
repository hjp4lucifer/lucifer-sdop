package cn.lucifer.sdop;

import android.content.Intent;
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
		lcf().sdop.get(url, GetMSCardEnhancedSynthesisData.procedure, null);
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

}
