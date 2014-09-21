package kr.poturns.blink.demo.fitnessapp.measurement;

public class PushUp {
	public int count;
	public String DateTime;

	public PushUp() {
	}

	public PushUp(int count) {
		this.count = count;
		DateTime = DateTimeUtil.get();
	}
}
