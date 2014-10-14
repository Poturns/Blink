package kr.poturns.blink.schema;

/** category : health */
public class HeartBeat extends DefaultSchema.Base {
	public int bpm;

	public HeartBeat(int bpm, String dateTime) {
		this.bpm = bpm;
		this.DateTime = dateTime;
	}

	public HeartBeat() {
	}
}
