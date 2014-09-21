package kr.poturns.blink.schema;

import kr.poturns.blink.schema.DefaultSchema;

/** category : fitness */
public class PushUp extends DefaultSchema {
	public int count;
	public String DateTime;

	public PushUp() {
	}

	public PushUp(int count, String dateTime) {
		this.count = count;
		this.DateTime = dateTime;
	}
}
