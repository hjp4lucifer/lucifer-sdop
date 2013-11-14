package cn.lucifer.sdop;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.ex.InitRaidBossOutlineList;

public class Boss extends LcfExtend {

	private JSONObject currentType;

	public final int x3 = 250037;
	public final int x6 = 250038;

	public JSONObject getCurrentType() {
		if (currentType == null) {
			try {
				currentType = lcf().sdop.loadJsonObject("boss_super.json");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return currentType;
	}

	private JSONObject initRaidBossOutlineList_args;

	private JSONObject getInitRaidBossOutlineList_args() {
		if (initRaidBossOutlineList_args == null) {
			try {
				initRaidBossOutlineList_args = lcf().sdop
						.loadJsonObject("initRaidBossOutlineList_args.json");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return initRaidBossOutlineList_args;
	}

	public void initRaidBossOutlineList(String callback) {
		String url = lcf().sdop.httpUrlPrefix
				+ "/PostForRaidBossList/initRaidBossOutlineList?ssid="
				+ lcf().sdop.ssid;

		try {
			JSONObject payload = lcf().sdop.createBasePayload(
					"initRaidBossOutlineList",
					getInitRaidBossOutlineList_args());

			lcf().sdop.post(url, payload.toString(),
					InitRaidBossOutlineList.procedure, callback);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
