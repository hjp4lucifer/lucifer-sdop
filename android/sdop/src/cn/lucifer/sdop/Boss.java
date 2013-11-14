package cn.lucifer.sdop;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class Boss extends LcfExtend {

	private JSONObject currentType;
	
	public JSONObject getCurrentType() {
		if (currentType == null) {
			try {
				InputStream input =lcf().sdop. context.getAssets().open("boss_super.json");
				List<String> lines = IOUtils.readLines(input);
				IOUtils.closeQuietly(input);
				String bossType = lines.get(0);// 这里处理过gson, 所以只有一行
				lines = null;
				
				currentType = new JSONObject(bossType);
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
	
}
