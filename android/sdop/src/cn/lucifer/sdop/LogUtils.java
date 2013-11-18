package cn.lucifer.sdop;

import android.util.Log;

public class LogUtils {

	private static final String TAG = "Lucifer";

	public static void log(){
		Throwable ex = new Throwable();
		ex.printStackTrace();
	}
	
	protected void log(Object... args) {
		Throwable ex = new Throwable();
		StackTraceElement[] stackElements = ex.getStackTrace();

		if (stackElements != null) {
			if (stackElements.length >= 2) {
				StringBuilder stack = new StringBuilder();
				stack.append(stackElements[1].getClassName()).append(".");
				stack.append(stackElements[1].getMethodName()).append("|");
				stack.append(stackElements[1].getLineNumber());
				Log.d(TAG, stack.toString());
			}
		}
		StringBuilder log = new StringBuilder(">> ");
		if (args != null) {
			int index = 0;
			for (Object object : args) {
				if (index != 0)
					log.append("|");
				index++;
				log.append(object);
			}
		}
		Log.d(TAG, log.toString());
		Log.d(TAG, "############################################");
	}
}
