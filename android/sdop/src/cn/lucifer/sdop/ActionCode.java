package cn.lucifer.sdop;

public class ActionCode {
	public int code;
	public String name;
	public String prefix;

	public ActionCode(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public ActionCode(int code, String name, String prefix) {
		this(code, name);
		this.prefix = prefix;
	}

}
