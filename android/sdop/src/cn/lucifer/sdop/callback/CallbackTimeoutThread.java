package cn.lucifer.sdop.callback;

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
		CB.dispatch(callback, args);
	}
}
