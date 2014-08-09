package kr.poturns.blink.external.tab.logview;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.DeviceApp;
import kr.poturns.blink.db.archive.DeviceAppLog;
import kr.poturns.blink.db.archive.SystemDatabaseObject;

public class LogHelper {
	//TODO 
	private SqliteManager mSqliteManager;
	private SystemDatabaseObject mSystemDatabaseObject;

	public LogHelper(Context context) {
		mSqliteManager = SqliteManager.getSqliteManager(context);

		// TODO 전체에 대한 DB을 얻어옴
		mSystemDatabaseObject = mSqliteManager.obtainSystemDatabase("", "");
	}

	/**
	 * 로그 데이터를 얻는다.
	 * 
	 * @param dateTimeFrom
	 *            얻어올 로그 데이터의 기준점이 되는 시간, null이 될 수 있음
	 * @param dateTimeTo
	 *            얻어올 로그 데이터의 마지막 시간, null이 될 수 있음
	 */
	public List<ExternalDeviceAppLog> getLog(String dateTimeFrom,
			String dateTimeTo) {
		return null;
	}

	/**
	 * Device를 기준으로 로그 데이터를 얻는다.
	 * 
	 * @param dateTimeFrom
	 *            얻어올 로그 데이터의 기준점이 되는 시간, null이 될 수 있음
	 * @param dateTimeTo
	 *            얻어올 로그 데이터의 마지막 시간, null이 될 수 있음
	 */
	public List<ExternalDeviceAppLog> getLogByDevice(String device,
			String dateTimeFrom, String dateTimeTo) {
		return null;
	}

	/**
	 * App을 기준으로 로그 데이터를 얻는다.
	 * 
	 * @param dateTimeFrom
	 *            얻어올 로그 데이터의 기준점이 되는 시간, null이 될 수 있음
	 * @param dateTimeTo
	 *            얻어올 로그 데이터의 마지막 시간, null이 될 수 있음
	 */
	public List<ExternalDeviceAppLog> getLogByApp(String device, String app,
			String dateTimeFrom, String dateTimeTo) {
		return null;
	}

	public void close() {
		mSqliteManager.close();
	}

	/** DB에서 얻어온 DeviceAppLog를 표시하기 편리한 ExternalDeviceAppLog로 변환한다. */
	private List<ExternalDeviceAppLog> converLogData(List<DeviceAppLog> rawLog) {
		return null;
	}

	public static class DateStringBuilder {
		public int year;
		public int month;
		public int day;
		public int hour;
		public int minute;
		public int second;

		public DateStringBuilder() {
			Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH) + 1;
			day = c.get(Calendar.DAY_OF_MONTH);
			hour = c.get(Calendar.HOUR_OF_DAY);
			minute = c.get(Calendar.MINUTE);
			second = c.get(Calendar.SECOND);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(year).append(
					"-" + ensureString(month) + "-" + ensureString(day) + " ");
			sb.append(ensureString(hour) + ":" + ensureString(minute) + ":"
					+ ensureString(second));
			return sb.toString();
		}

		private String ensureString(int field) {
			if (field < 10)
				return "0" + field;
			else
				return String.valueOf(field);
		}
	}

}
