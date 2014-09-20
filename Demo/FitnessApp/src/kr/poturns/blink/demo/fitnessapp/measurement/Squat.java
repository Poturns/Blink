package kr.poturns.blink.demo.fitnessapp.measurement;

public class Squat {
	public int count;
	public String DateTime;

	public Squat(int count) {
		this.count = count;
		DateTime = DateTimeUtil.get();
	}

	public Squat() {
	}
}
