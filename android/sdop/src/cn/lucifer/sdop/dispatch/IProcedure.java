package cn.lucifer.sdop.dispatch;

import org.json.JSONException;

/**
 * 产品接口
 * 
 * @author Lucifer
 * 
 */
public interface IProcedure {

	public void process(byte[] response, String callback) throws JSONException;

	public void callback(Object[] args);

}
