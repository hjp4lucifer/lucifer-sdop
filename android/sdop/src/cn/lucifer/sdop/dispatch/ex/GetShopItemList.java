package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.Item4Shop;

public class GetShopItemList extends BaseDispatch {

	public static final String procedure = "getShopItemList";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		// Log.i(lcf().LOG_TAG, args.toString());
		Item4Shop[] shopItemList = lcf().gson.fromJson(
				args.getString("shopItemList"), Item4Shop[].class);
		lcf().sdop.checkCallback(callback, shopItemList);
	}

	/**
	 * 获取甲板信息
	 */
	@Override
	public void callback(Object[] args) {
		lcf().sdop.cardPlatoon.getCardPlatoonData(GetCardPlatoonData.procedure);
	}

}
