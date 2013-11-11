package cn.lucifer.sdop;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Lcf {

	private static Lcf _instance;

	private Lcf() {
		// TODO Auto-generated constructor stub
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

}
