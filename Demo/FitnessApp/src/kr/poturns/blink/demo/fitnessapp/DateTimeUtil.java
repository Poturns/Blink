package kr.poturns.blink.demo.fitnessapp;

import android.text.format.DateFormat;

/**
 * DB에 저장되는 시간 형식의 문자열을 얻어오는 클래스
 * 
 * @author Myungjin.Kim
 */
public class DateTimeUtil {
	/**
	 * 메소드 호출 시점의 시각으로 DB에 저장되는 시간 형식의 문자열을 얻어온다.
	 * 
	 * @return 'yyyy-MM-dd kk:mm:ss' 형식의 시간 문자열
	 */
	public static final String getTimeString() {
		return DateFormat.format("yyyy-MM-dd kk:mm:ss",
				System.currentTimeMillis()).toString();
	}
}
