package cn.lucifer.sdop.dispatch;

import org.json.JSONException;

/**
 * 产品接口
 * 
 * @author Lucifer
 * 
 */
public interface IProcedure {

	public void callback(byte[] response) throws JSONException;

}
