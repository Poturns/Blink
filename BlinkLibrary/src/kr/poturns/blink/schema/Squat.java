package kr.poturns.blink.schema;

import kr.poturns.blink.schema.DefaultSchema;

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
