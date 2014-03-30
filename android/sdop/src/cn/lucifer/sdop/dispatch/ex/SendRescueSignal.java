package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class SendRescueSignal extends BaseDispatch {
	public static final String procedure = "sendRescueSignal";

	protected Integer lastRaidBossId;
	protected int resendCount;

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			// 这里需要慎重处理, 最少等1分钟后再次发送
			if (resendCount > 2) {
				resendCount = 0;
				return;
			}
			if (lastRaidBossId != null) {
				resendCount++;
				lcf().sdop.checkCallback(procedure, resendCount * 60000,
						new Object[] { lastRaidBossId });
			}
			return;
		}

		lastRaidBossId = null;
		resendCount = 0;
		lcf().sdop.checkCallback(callback);
		lcf().sdop.log("成功发送Help信息!");
	}

	/**
	 * 延迟执行：发送help信息
	 */
	@Override
	public void callback(Object[] args) {
		lastRaidBossId = (Integer) args[0];
		lcf().sdop.boss.sendRescueSignal(lastRaidBossId, null);
	}

}
