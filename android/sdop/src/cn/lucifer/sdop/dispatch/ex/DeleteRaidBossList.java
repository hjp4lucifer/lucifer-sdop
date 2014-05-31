package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class DeleteRaidBossList extends BaseDispatch {

	public static final String procedure = "deleteRaidBossList";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		lcf().sdop.log("Boss结果删除成功！");
		lcf().sdop.checkCallback(callback, 100, null);
	}

	@Override
	public void callback(Object[] args) {
		lcf().sdop.boss.deleteRaidBossList(
				lcf().sdop.boss.currentGachaRaidBossId, null);
	}
}
