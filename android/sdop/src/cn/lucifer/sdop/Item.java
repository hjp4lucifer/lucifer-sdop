package cn.lucifer.sdop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.ex.BoughtItem4Sp;
import cn.lucifer.sdop.dispatch.ex.EquipItem4Sp;
import cn.lucifer.sdop.dispatch.ex.GetShopItemList;

/**
 * <ul>
 * <li>20006: 30sp</li>
 * <li>20011: 4000HP</li>
 * <li>20012: 6000HP</li>
 * <li>20013: 60sp</li>
 * <li>20016: mult 4000HP</li>
 * <li>20017: mult 6000HP</li>
 * </ul>
 * 
 * @author Lucifer
 *
 */
public class Item extends LcfExtend {

	// private final int[] itemIdList = new int[] { 20006, 20011, 20013 };
	private final String itemIdList_json = "[ 20006, 20011, 20013 ]";

	public void getShopItemList(String callback) {
		String url = lcf().sdop.httpUrlPrefix + "/GetForShop/getShopItemList?"
				+ lcf().sdop.createGetParams();
		lcf().sdop.get(url, GetShopItemList.procedure, callback);
	}

	public void equipItem4Sp(String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForCardPlatoon/equipItem?ssid=" + lcf().sdop.ssid;
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					EquipItem4Sp.procedure, new JSONObject().put("itemIdList",
							new JSONArray(itemIdList_json)));
			lcf().sdop.post(url, payload.toString(), EquipItem4Sp.procedure,
					callback);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * true, AI购买sp药进行中
	 */
	public boolean onBoughtItem4Sp;

	/**
	 * AI购买sp药
	 * <ol>
	 * <b>购买顺序</b>
	 * <li>30sp不足60的, 优先购买到60</li>
	 * <li>60sp不足20的, 优先购买到20</li>
	 * <li>GP能补充完毕的, 购买30/60sp到满</li>
	 * <li>GP不能补充完毕的, 优先购买30sp到满</li>
	 * <li>GP不能补充完毕的, 购买60sp到满</li>
	 * </ol>
	 */
	public void boughtItem4SpAI() {
		onBoughtItem4Sp = true;
		lcf().sdop.boss.initRaidBossOutlineList(BoughtItem4Sp.procedure);
	}

	/**
	 * 
	 * @param itemId
	 *            30sp: 20006; 60sp: 20013
	 * @param num
	 *            数量
	 */
	public void boughtItem4Sp(int itemId, int num, String callback) {
		String url = lcf().sdop.httpUrlPrefix + "/PostForShop/boughtItem?ssid="
				+ lcf().sdop.ssid;
		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					BoughtItem4Sp.procedure, new JSONObject().put("id", itemId)
							.put("num", num));
			lcf().sdop.post(url, payload.toString(), BoughtItem4Sp.procedure,
					callback);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
