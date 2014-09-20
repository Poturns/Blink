package kr.poturns.blink.demo.fitnessapp.measurement;

public class SitUp {
	public int count;
	public String DateTime;

	public SitUp(int count) {
		this.count = count;
		DateTime = DateTimeUtil.get();
	}

	public SitUp() {
	}
}
