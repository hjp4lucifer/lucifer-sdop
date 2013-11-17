package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class StartAutoSuperRaidBoss extends BaseDispatch {

	public static final String procedure = "cn.lucifer.StartAutoSuperRaidBoss";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		// TODO Auto-generated method stub

	}

	@Override
	public void callback(Object[] args) {
		lcf().sdop.clearAllJob();
		lcf().sdop.boss
				.initRaidBossOutlineList(InitRaidBossOutlineList.procedure);
	}

}
