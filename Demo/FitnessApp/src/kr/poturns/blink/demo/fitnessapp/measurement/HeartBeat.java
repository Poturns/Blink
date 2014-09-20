package kr.poturns.blink.demo.fitnessapp.measurement;

public class HeartBeat {
	public int bpm;
	public String DateTime;

	public HeartBeat(int bpm) {
		this.bpm = bpm;
		this.DateTime = DateTimeUtil.get();
	}

	public HeartBeat() {
	}
}
