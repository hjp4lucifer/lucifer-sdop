package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;
import cn.lucifer.sdop.domain.HeaderDetail;

public class InitRaidBossOutlineList extends BaseDispatch {
	public static final String procedure = "initRaidBossOutlineList";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		HeaderDetail headerDetail = lcf().gson.fromJson(
				args.getString("headerDetail"), HeaderDetail.class);
		lcf().sdop.ms.max = headerDetail.msMax;
		lcf().sdop.ms.current = headerDetail.ms;
		lcf().sdop.pilot.max = headerDetail.pilotMax;
		lcf().sdop.pilot.current = headerDetail.pilot;

		lcf().sdop.bp = headerDetail.bpDetail.currentValue;

		lcf().sdop.log("获取总力战初始化信息成功！当前MS：" + lcf().sdop.ms.current + "/"
				+ lcf().sdop.ms.max);

		lcf().sdop.checkCallback(callback, new Object[] { headerDetail });
	}

	@Override
	public void callback(Object[] args) {
		HeaderDetail headerDetail = (HeaderDetail) args[0];
		StringBuffer logMsg = new StringBuffer("当前BP：");
		logMsg.append(lcf().sdop.bp);

		int delayTime;
		if (lcf().sdop.bp >= 10) {
			lcf().sdop.boss.autoSuperRaidBoss();
			logMsg.append(", 满足超总要求！");
			delayTime = 180;
		} else {
			int recoveryTime = headerDetail.bpDetail.getTrueRecoveryTime();
			delayTime = (recoveryTime - 60);
			if (delayTime < 0) {
				delayTime = recoveryTime / 2;
			}
			logMsg.append(", 不满足超总要求！").append(delayTime).append("秒后再尝试！");
		}
		lcf().sdop.log(logMsg.toString());

		lcf().sdop.checkCallback(StartAutoSuperRaidBoss.procedure,
				delayTime * 1000, null);
	}

}
