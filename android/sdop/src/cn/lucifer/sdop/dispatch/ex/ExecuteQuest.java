package cn.lucifer.sdop.dispatch.ex;

import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

public class ExecuteQuest extends BaseDispatch {

	public static final String procedure = "executeQuest";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			if (lcf().sdop.map.isEventMap()) {// 非赶路模式无需清除
				lcf().sdop.map.clearNodeId();
			}
			return;
		}
		lcf().sdop.map.executeQuestResultProcess(args);
	}

	@Override
	public void callback(Object[] args) {
		// TODO Auto-generated method stub

	}

}
