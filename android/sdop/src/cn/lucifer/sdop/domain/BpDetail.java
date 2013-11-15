package cn.lucifer.sdop.domain;

public class BpDetail {
	public int currentValue;
	public int maxValue;
	public int recoveryInterval;
	public int recoveryTime;

	public int getTrueRecoveryTime() {
		return (maxValue - currentValue - 1) * recoveryInterval + recoveryTime;
	}
}
