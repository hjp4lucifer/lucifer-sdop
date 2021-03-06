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
			// lcf().sdop.auto.setting.duel = false;
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
		if (lcf().sdop.bp < 5) {
			lcf().sdop.log(String.format("当前bp为%d (ep: %d), 等待下次检查！",
					lcf().sdop.bp, lcf().sdop.ep));

			// 暂时把开启记录模式的, 且开启自动GB的, 作为默认调用探索的条件
			if (lcf().sdop.auto.setting.ep && lcf().sdop.ep > 0) {
				lcf().sdop.map.getQuestData();
			}
			return;
		}
		lcf().sdop.duel.getEntryData(GetEntryData.procedure);
	}

}
