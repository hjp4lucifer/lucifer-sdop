package cn.lucifer.sdop.dispatch;

import java.util.TimerTask;

public class CallbackThread extends TimerTask {

	private String callback;
	private Object[] args;

	public CallbackThread(String callback, Object[] args) {
		this.callback = callback;
		this.args = args;
	}

	@Override
	public void run() {
		DF.callback(callback, args);
	}
}
