package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.Boss;

public class GetRaidBossOutlineList extends BaseDispatch {

	public static final String procedure = "getRaidBossOutlineList";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		if (args.isNull("list")) {
			noList();
			return;
		}
		Boss[] bosses = lcf().gson.fromJson(args.getString("list"),
				Boss[].class);
		if (bosses == null || bosses.length == 0) {
			noList();
			return;
		}

		Boss target = lcf().sdop.boss.AI.getTopLevel(bosses);
//		_sdop.log(lcf.sdop.boss.currentKind + "目标boss等级：<b class='c_red'>" + target.level + "</b>，残余血量：" + target.currentHp + "，【" + target.comment + "】");
//		if (callback) {
//			setTimeout(callback, 100, target);
//		}
	}

	@Override
	public void callback(Object[] args) {
		// TODO Auto-generated method stub

	}

	public void noList() {
		lcf().sdop.log("没有对应等级的boss！");
		if (!lcf().sdop.auto.setting.boss) {
			return;
		}
	}

}
