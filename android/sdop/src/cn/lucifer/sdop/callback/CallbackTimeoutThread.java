package cn.lucifer.sdop.callback;

import cn.lucifer.sdop.dispatch.DF;

public class CallbackTimeoutThread extends Thread {

	private String callback;
	private Object[] args;

	public CallbackTimeoutThread(String callback, Object[] args) {
		super();
		this.callback = callback;
		this.args = args;
	}

	@Override
	public void run() {
		DF.callback(callback, args);
	}
}
