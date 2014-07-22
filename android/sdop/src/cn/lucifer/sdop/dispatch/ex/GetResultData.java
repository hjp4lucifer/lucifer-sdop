package cn.lucifer.sdop.dispatch.ex;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import cn.lucifer.sdop.dispatch.BaseDispatch;

/**
 * 潜入任务获取结果
 * 
 * @author Lucifer
 * 
 */
public class GetResultData extends BaseDispatch {
	public static final String procedure = "getResultData";

	@Override
	public void process(byte[] response, String callback) throws JSONException {
		JSONObject args = getArgs(response);
		if (lcf().sdop.checkError(args, procedure)) {
			return;
		}
		JSONObject resultData = args.getJSONObject("resultData");
		StringBuilder logMsg = new StringBuilder("潜入【");
		logMsg.append(resultData.getString("destinationName"))
				.append("】: ")
				.append(resultData.getJSONObject("resultType").getString(
						"value"));
		List<String> report = lcf().getMatchChildren(
				resultData.getString("report"), "<font .*?>(.*?)</font>");
		for (String str : report) {
			if (str.indexOf('-') == StringUtils.INDEX_NOT_FOUND) {
				logMsg.append(str);
				continue;
			}
			str = StringUtils.remove(str, '-');
			logMsg.append("<br>").append(str).append('：');
		}
		lcf().sdop.log(logMsg.toString());

		lcf().sdop.checkCallback(callback);
	}

	@Override
	public void callback(Object[] args) {
		lcf().sdop.sneaking.sortieTroops();
	}

}
