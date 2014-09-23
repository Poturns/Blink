package kr.poturns.blink.demo.fitnessapp;

import android.text.format.DateFormat;
/** @author Myungjin.Kim */
public class DateTimeUtil {
	public static final String get() {
		return DateFormat.format("yyyy-MM-dd kk:mm:ss",
				System.currentTimeMillis()).toString();
	}
}
