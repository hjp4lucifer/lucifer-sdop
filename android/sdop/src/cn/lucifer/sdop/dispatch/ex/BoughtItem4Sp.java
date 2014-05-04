package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.HeaderDetail;
import cn.lucifer.sdop.domain.Item;
import cn.lucifer.sdop.domain.Item4Shop;

public class BoughtItem4Sp extends BaseDispatch {
	public static final String procedure = "boughtItem";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		Item4Shop[] itemList = lcf().gson.fromJson(
				args.getString("shopItemList"), Item4Shop[].class);

		StringBuffer logMsg = new StringBuffer("购买SP药成功! 可购买: ");
		Item4Shop item;
		for (int i = 0; i < itemList.length; i++) {
			item = itemList[i];
			if (item.maxItemNum > 0) {
				logMsg.append("<br>　　").append(item.name).append("：")
						.append(item.maxItemNum);
			}
		}
		lcf().sdop.log(logMsg.toString());
		lcf().sdop.checkCallback(callback, new Object[] { itemList });

		if (callback == null) {
			lcf().sdop.item.onBoughtItem4Sp = false;
		}
	}

	private Integer gold;
	private final int item_30_sp_id = 20006;
	private final int item_30_sp_cost = 200;
	private final int item_30_sp_min_count = 60;

	private final int item_60_sp_id = 20013;
	private final int item_60_sp_cost = 600;
	private final int item_60_sp_min_count = 28;

	/**
	 * <ol>
	 * <li>购买最低的30sp数量</li>
	 * <li>购买最低的60sp数量</li>
	 * </ol>
	 */
	private int step;

	@Override
	public void callback(Object[] args) {
		Object obj = args[0];
		// from InitRaidBossOutlineList
		if (obj instanceof HeaderDetail) {// 获取GP信息
			HeaderDetail headerDetail = (HeaderDetail) obj;
			this.gold = headerDetail.gold;
			lcf().sdop.log("当前gold : " + gold);
			
			if (gold > 200) {// 最低购买需求
				lcf().sdop.item.equipItem4Sp(procedure);
			} else {
				lcf().sdop.log("GP不足, 停止执行【购买sp药】!");
			}
			return;
		}

		// from EquipItem4Sp
		if (obj instanceof Item[]) {// 获取当前sp的数量
			Item[] items = (Item[]) obj;
			int item30Count = 0, item60Count = 0;
			for (Item item : items) {
				if (item.id == item_30_sp_id) {
					item30Count = item.currentStock;
					if (item.currentStock < item_30_sp_min_count) {// 30sp
						bought(item, item_30_sp_id, item_30_sp_cost,
								item_30_sp_min_count);
						step = 1;
						return;
					}
				}

				if (item.id == item_60_sp_id) {
					item60Count = item.currentStock;
					if (item.currentStock < item_60_sp_min_count) {// 60sp
						bought(item, item_60_sp_id, item_60_sp_cost,
								item_60_sp_min_count);
						step = 2;
						return;
					}
				}
			}

			// 满足药品数量最低要求, 更改购买方案
			step = 0;
			if (item30Count < 96) {
				lcf().sdop.item.boughtItem4Sp(item_30_sp_id, 1, procedure);
				return;
			}
			if (item60Count < 96) {
				lcf().sdop.item.boughtItem4Sp(item_60_sp_id, 1, procedure);
				return;
			}
		}

		gold = null;

		// from this
		if (obj instanceof Item4Shop[]) {// 购买成功后的数量
			if (step == 1) {// 重新执行可定位
				lcf().sdop.item.boughtItem4SpAI();
				return;
			}
			Item4Shop[] shopItems = (Item4Shop[]) obj;
			for (Item4Shop shopItem : shopItems) {
				if (shopItem.maxItemNum == 0) {
					continue;
				}
				if (shopItem.id == item_30_sp_id) {
					lcf().sdop.item.boughtItem4Sp(item_30_sp_id,
							shopItem.maxItemNum, procedure);
					return;
				}
				if (shopItem.id == item_60_sp_id) {
					lcf().sdop.item.boughtItem4Sp(item_60_sp_id,
							shopItem.maxItemNum, procedure);
					return;
				}
			}
		}

		lcf().sdop.item.onBoughtItem4Sp = false;
		lcf().sdop.log("SP药购买完毕！");
	}

	private void bought(Item item, int itemId, int cost, int minCount) {
		int num = minCount - item.currentStock;
		int costGold = num * cost;
		if (costGold < gold) {// 够GP
			lcf().sdop.item.boughtItem4Sp(itemId, num, procedure);
		} else {// 不够GP
			num = gold / cost;
			lcf().sdop.item.boughtItem4Sp(itemId, num, null);
		}
		gold = null;
	}

}
