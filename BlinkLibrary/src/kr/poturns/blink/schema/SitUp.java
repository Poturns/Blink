package kr.poturns.blink.schema;

import kr.poturns.blink.schema.DefaultSchema;

/** category : fitness */
public class SitUp extends DefaultSchema {
	public int count;

	public SitUp() {
	}

	public SitUp(int count, String dateTime) {
		this.count = count;
		this.DateTime = dateTime;
	}
}
