package cn.lucifer.sdop;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * AI方法
	 */
	public void sortieTroops() {
		// [20012,20006,20013,20016]
		int[] itemIdList = { 20006, 20013, 20012 };
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
			lcf().sdop.post(url, payload.toString(), SortieTroops.procedure,
					callback);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
