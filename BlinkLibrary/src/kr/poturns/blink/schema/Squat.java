package kr.poturns.blink.schema;

/** category : fitness */
public class Squat extends DefaultSchema {
	public int count;

	public Squat() {
	}

	public Squat(int count, String dateTime) {
		this.count = count;
		this.DateTime = dateTime;
	}
}
