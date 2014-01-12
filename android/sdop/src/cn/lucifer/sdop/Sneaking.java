package cn.lucifer.sdop;

import java.util.Date;

import android.content.Intent;
import cn.lucifer.sdop.dispatch.ex.GetSneakingMissionTopData;
import cn.lucifer.sdop.domain.args.GetSneakingMissionTopDataArgs;

/**
 * 潜入任务
 * 
 * @author Lucifer
 * 
 */
public class Sneaking extends LcfExtend {

	public final String REFRESH_RECEIVER_ACTION = "lcf.sdop.ui.Sneaking";
	public GetSneakingMissionTopDataArgs sneakingMissionTopDataArgs;

	/**
	 * 获取当前的潜入任务信息
	 */
	public void getSneakingMissionTopData() {
		String url = lcf().sdop.httpUrlPrefix
				+ "/GetForSneakingMission/getSneakingMissionTopData?"
				+ lcf().sdop.createGetParams();
		lcf().sdop.get(url, GetSneakingMissionTopData.procedure, null);
	}

	/**
	 * 刷新数据, 并发送广播
	 * 
	 * @param sneakingMissionTopDataArgs
	 */
	public void refreshMissonAndBroadcast(
			GetSneakingMissionTopDataArgs sneakingMissionTopDataArgs) {
		if (sneakingMissionTopDataArgs != null) {
			this.sneakingMissionTopDataArgs = sneakingMissionTopDataArgs;
		}
		if (null == lcf().sdop.context) {
			return;
		}
		Intent intent = new Intent(REFRESH_RECEIVER_ACTION);
		lcf().sdop.context.sendBroadcast(intent);
	}

}
