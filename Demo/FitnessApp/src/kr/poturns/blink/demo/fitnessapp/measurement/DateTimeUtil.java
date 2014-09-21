package kr.poturns.blink.demo.fitnessapp.measurement;

import android.text.format.DateFormat;

public class DateTimeUtil {
	public static final String get() {
		return DateFormat.format("yyyy-MM-dd kk:mm:ss",
				System.currentTimeMillis()).toString();
	}
}
