package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

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
		lcf().sdop.ep = headerDetail.energyDetail.energy;

		lcf().sdop.log("获取总力战初始化信息成功！当前MS：" + lcf().sdop.ms.current + "/"
				+ lcf().sdop.ms.max);

		lcf().sdop.checkCallback(callback, new Object[] { headerDetail });
	}

	@Override
	public void callback(Object[] args) {
		HeaderDetail headerDetail = (HeaderDetail) args[0];
		StringBuffer logMsg = new StringBuffer("当前BP：");
		logMsg.append(lcf().sdop.bp).append("(ep: ").append(lcf().sdop.ep)
				.append(")");

		int delayTime;
		if (lcf().sdop.bp >= 10) {
			lcf().sdop.boss.autoSuperRaidBoss();
			logMsg.append(", 满足").append(lcf().sdop.boss.getCurrentTypeName())
					.append("要求！");
			delayTime = 180;
		} else {
			int recoveryTime = headerDetail.bpDetail.getTrueRecoveryTime();
			delayTime = (recoveryTime - 60);
			if (delayTime < 0) {
				delayTime = recoveryTime / 2;
			}
			logMsg.append(", 不满足").append(lcf().sdop.boss.getCurrentTypeName())
					.append("要求！").append(delayTime).append("秒后再尝试！");
		}
		lcf().sdop.log(logMsg.toString());

		if (lcf().sdop.auto.setting.ep) {
			int baseDelay = 16 * 60;
			if (delayTime > baseDelay) {
				baseDelay *= 1000;// 转换成毫秒

				Log.d(lcf().LOG_TAG, "define ep auto mentoh! max : " + baseDelay);
				// 延迟8分钟调度ep消耗方法
				lcf().sdop.checkCallback(GetQuestData.procedure,
						baseDelay >> 1, null);
				// 延迟16分钟调度ep消耗方法
				lcf().sdop.checkCallback(GetQuestData.procedure, baseDelay,
						null);
			}
		}
		// 里面有clearAllJob的调用
		lcf().sdop.checkCallback(StartAutoSuperRaidBoss.procedure,
				delayTime * 1000, null);
	}

}
