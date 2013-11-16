package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.Item;

public class EquipItem4Sp extends BaseDispatch {
	public static final String procedure = "equipItem4Sp";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		Item[] itemList = lcf().gson.fromJson(args.getString("itemList"),
				Item[].class);

		StringBuffer logMsg = new StringBuffer("自动带SP药成功！");
		Item item;
		for (int i = 0; i < itemList.length; i++) {
			item = itemList[i];
			if (item.isEquiped) {
				logMsg.append("<br>　　").append(item.name).append("剩余：")
						.append(item.currentStock);
			}
		}
		lcf().sdop.log(logMsg.toString());
		lcf().sdop.checkCallback(callback);
	}

	@Override
	public void callback(Object[] args) {
		lcf().sdop.boss.executeBattleStart(ExecuteBattleStart.procedure);
	}

}
