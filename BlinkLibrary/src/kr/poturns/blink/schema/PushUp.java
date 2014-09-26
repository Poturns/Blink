package kr.poturns.blink.schema;

/** category : fitness */
public class PushUp extends DefaultSchema {
	public int count;

	public PushUp() {
	}

	public PushUp(int count, String dateTime) {
		this.count = count;
		this.DateTime = dateTime;
	}
}
