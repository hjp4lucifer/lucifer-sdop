package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class Enter extends BaseDispatch {

	public static final String procedure = "enter";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject json = new JSONObject(new String(response));
		JSONObject args = json.getJSONObject("args");
		String tokenId = args.getString("tokenId");
		lcf().sdop.setTokenId(tokenId);
		
		if (lcf().sdop.auto.setting.boss) {
			Log.i(lcf().LOG_TAG, "auto boss");
			switch (lcf().sdop.boss.currentType) {
			case 0:
				lcf().sdop.boss.AI.startAutoNormalRaidBoss();
				break;
			default:
				lcf().sdop.boss.AI.startAutoSuperRaidBoss();
				break;
			}
			return;
		}
		Log.i(lcf().LOG_TAG, "no auto boss");
		if (lcf().sdop.auto.setting.duel) {
			Log.i(lcf().LOG_TAG, "auto duel");
			lcf().sdop.duel.startAutoDuel();
			return;
		}
		Log.i(lcf().LOG_TAG, "no auto duel");
	}

	@Override
	public void callback(Object[] args) {
		// TODO Auto-generated method stub
		
	}

}
