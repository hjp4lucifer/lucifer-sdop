package cn.lucifer.sdop.dispatch;

public class CallbackThread extends Thread {

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
