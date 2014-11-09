package kr.poturns.blink.demo.fitnessapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.text.format.DateFormat;

/**
 * DB에 저장되는 시간 형식의 문자열을 얻어오는 클래스
 * 
 * @author Myungjin.Kim
 */
public class DateTimeUtil {
	public static final String FORMAT = "yyyy-MM-dd kk:mm:ss";

	/**
	 * 메소드 호출 시점의 시각으로 DB에 저장되는 시간 형식의 문자열을 얻어온다.
	 * 
	 * @return 'yyyy-MM-dd kk:mm:ss' 형식의 시간 문자열
	 */
	public static final String getTimeString() {
		return DateFormat.format(FORMAT, System.currentTimeMillis()).toString();
	}

	public static final String getTimeString(long timeInMills) {
		return DateFormat.format(FORMAT, timeInMills).toString();
	}
	
	@Deprecated
	/** 제대로 된 시간을 반환하지 않는다.*/
	public static final String getTimeStringNano(long timeInNanos){
		long millis = TimeUnit.MILLISECONDS.convert(timeInNanos, TimeUnit.NANOSECONDS); 
		java.text.DateFormat formatter = new SimpleDateFormat(FORMAT);
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = null;
		try {
			date = formatter.parse(getTimeString());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		long newTimeInmillis = date.getTime() + millis;

		Date date2 = new Date(newTimeInmillis);
		return DateFormat.format(FORMAT, date2).toString();
	}
}
