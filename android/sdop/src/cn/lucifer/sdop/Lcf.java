package cn.lucifer.sdop;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Environment;

import com.google.gson.Gson;

public final class Lcf {
	public final String LOG_TAG = "Lucifer";
	
	private static Lcf _instance;

	public Sdop sdop = new Sdop();

	public Gson gson = new Gson();

	private Lcf() {
	}

	public static Lcf getInstance() {
		if (_instance == null) {
			_instance = new Lcf();
		}
		return _instance;
	}

	public String getCookie(String cookies, String name) {
		String regExp = "(^| )" + name + "=([^;]*)(;|$)";
		Pattern expression = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
		Matcher matcher = expression.matcher(cookies);

		if (matcher.find()) {
			return matcher.group(2);
		} else {
			return null;
		}
	}

	public long getTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}

	/**
	 * 获取sd卡目录
	 * 
	 * @return null不存在sd卡
	 */
	public File getSdDirectory() {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			File sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
			return sdDir;
		}
		return null;
	}
}
