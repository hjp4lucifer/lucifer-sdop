package cn.lucifer.sdop;

import java.util.concurrent.ScheduledFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;
import cn.lucifer.sdop.dispatch.ex.GetSneakingMissionTopData;
import cn.lucifer.sdop.dispatch.ex.GetResultData;
import cn.lucifer.sdop.dispatch.ex.SortieTroops;
import cn.lucifer.sdop.domain.SneakingMap;
import cn.lucifer.sdop.domain.SneakingMapDestination;
import cn.lucifer.sdop.domain.SneakingPlatoon;
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

	public final Runnable autoRunnable = new Runnable() {
		@Override
		public void run() {
			getSneakingMissionTopData(GetSneakingMissionTopData.procedure);

		}
	};

	protected Long nextCheckSneakingTime;

	protected ScheduledFuture<?> scheduledFuture;

	/**
	 * 恢复定时任务
	 */
	public void resumeAutoSneaking() {
		if (null == nextCheckSneakingTime) {
			startAutoSneaking();
			return;
		}
		long delayMillis = nextCheckSneakingTime - System.currentTimeMillis();
		Log.d(lcf().LOG_TAG, "resumeAutoSneaking, next time : " + delayMillis);
		scheduledFuture = lcf().sdop.delayJob(autoRunnable, delayMillis);
	}

	/**
	 * 开始自动潜入, UI调用
	 */
	public void startAutoSneaking() {
		lcf().sdop.auto.setting.sneaking = true;
		stopJob();
		lcf().sdop.log("自动潜入开始！");
		getSneakingMissionTopData(GetSneakingMissionTopData.procedure);
	}

	protected void stopJob() {
		if (null != scheduledFuture) {
			scheduledFuture.cancel(true);
			scheduledFuture = null;
		}
	}

	/**
	 * 取消自动超总, UI调用
	 */
	public void cancelAutoSneaking() {
		if (null != scheduledFuture) {
			scheduledFuture.cancel(true);
			scheduledFuture = null;
		}
		lcf().sdop.auto.setting.sneaking = false;
		lcf().sdop.log("自动潜入停止成功！");
	}

	public void resetAuto() {
		int requiredTime = 0;
		SneakingPlatoon[] platoonDataList = sneakingMissionTopDataArgs.platoonDataList;
		for (SneakingPlatoon platoon : platoonDataList) {
			if ("RETURN".equals(platoon.state.value)) {
				lcf().sdop.sneaking.proxyGetResultData(platoon);
				return;
			}
			if (platoon.requiredTime > 0) {
				if (0 == requiredTime || platoon.requiredTime < requiredTime) {
					requiredTime = platoon.requiredTime;
				}
			}
		}
		if (requiredTime > 0) {
			int delaySecond = requiredTime;
			if (delaySecond > 3600) {// 1小时查看一次
				delaySecond = 3600;
			}
			long delayMillis = delaySecond * 1000;
			nextCheckSneakingTime = System.currentTimeMillis() + delayMillis;
			scheduledFuture = lcf().sdop.delayJob(autoRunnable, delayMillis);
			lcf().sdop.log(String.format("无可执行的自动潜入! %d秒后再查看!", delaySecond));
			return;
		}
		lcf().sdop.log("没有合适的自动潜入team! 请登录游戏手动执行!");
	}

	/**
	 * 获取当前的潜入任务信息
	 */
	public void getSneakingMissionTopData(String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/GetForSneakingMission/getSneakingMissionTopData?"
				+ lcf().sdop.createGetParams();
		lcf().sdop.get(url, GetSneakingMissionTopData.procedure, callback);
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

		proxyPlatoon = null;
		destination = null;
	}

	SneakingPlatoon proxyPlatoon;
	SneakingMapDestination destination;

	/**
	 * 代理执行查看结果, 并重新潜入
	 * 
	 * @param platoon
	 */
	public void proxyGetResultData(SneakingPlatoon platoon) {
		nextCheckSneakingTime = null;
		lcf().sdop.log(String.format("准备查看潜入结果, 并重新潜入, %d", platoon.platoonId));
		proxyPlatoon = platoon;
		fixDestination();
		getResultData(platoon.platoonId, GetResultData.procedure);
	}

	protected void fixDestination() {
		for (SneakingMap map : sneakingMissionTopDataArgs.mapDataList) {
			for (SneakingMapDestination destination : map.destinationDataList) {
				if (destination.sortieplatoonId != null
						&& proxyPlatoon.platoonId == destination.sortieplatoonId) {
					this.destination = destination;
					return;
				}
			}
		}
	}

	public void getResultData(int platoonId, String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForSneakingMission/getResultData";
		try {
			JSONObject payload = lcf().sdop.createBasePayload("getResultData",
					new JSONObject().put("platoonId", platoonId));
			lcf().sdop.post(url, payload.toString(), GetResultData.procedure,
					callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	/**
	 * AI方法
	 */
	public void sortieTroops() {
		// [20012,20006,20013,20016]
		int[] itemIdList;
		if (destination.requiredTime > 10800) {
			itemIdList = new int[] { 20006, 20013, 20012 };
		} else {
			//itemIdList = new int[] { 20006, 20013 };
			itemIdList = new int[] { 20017 };
		}
		int[] msCardIdList = proxyPlatoon.msCardIdList;
		sortieTroops(proxyPlatoon.platoonId, destination.destinationId,
				msCardIdList, itemIdList, SortieTroops.procedure);
	}

	public void sortieTroops(int platoonId, int destinationId,
			int[] msCardIdList, int[] itemIdList, String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForSneakingMission/sortieTroops";
		try {
			JSONObject payload = lcf().sdop
					.createBasePayload(
							"sortieTroops",
							new JSONObject()
									.put("platoonId", platoonId)
									.put("itemIdList",
											new JSONArray(lcf().gson
													.toJson(itemIdList)))
									.put("msCardIdList",
											new JSONArray(lcf().gson
													.toJson(msCardIdList)))
									.put("destinationId", destinationId));
			lcf().sdop.log("准备潜入" + platoonId);
			lcf().sdop.post(url, payload.toString(), SortieTroops.procedure,
					callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
