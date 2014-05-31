package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.Boss;

public class GetRaidBossOutlineList4Finish extends BaseDispatch {

	public static final String procedure = "cn.lucifer.GetRaidBossOutlineList4Finish";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		int finish = args.getJSONObject("listNum").getInt("finish");
		String logMsg = String.format("总力战剩余%s个结果待处理！",
				lcf().sdop.getBoldMsg(String.valueOf(finish)));
		lcf().sdop.log(logMsg);

		if (args.isNull("list")) {
			return;
		}
		Boss[] bosses = lcf().gson.fromJson(args.getString("list"),
				Boss[].class);
		lcf().sdop.checkCallback(callback, 100, new Object[] { bosses });
	}

	@Override
	public void callback(Object[] args) {
		Boss[] bosses = (Boss[]) args[0];
		if (bosses == null || bosses.length == 0) {
			lcf().sdop.log("总力战结果已全部处理完毕！");
		}
		StringBuilder logMsg;
		for (Boss boss : bosses) {
			logMsg = new StringBuilder("正在处理【").append(boss.comment)
					.append("】，state：").append(boss.state.value);

			logMsg.append("，可抽").append(boss.restGacha).append("回。");

			lcf().sdop.log(logMsg.toString());

			lcf().sdop.boss.getRaidBossResultData(boss.id,
					GetRaidBossResultData.procedure);
			break;// 只执行一次
		}
	}
}
