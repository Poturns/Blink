package kr.poturns.blink.external;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.DeviceAppLog;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import android.content.Context;

public class DBHelper {
	private static DBHelper sInstance;
	private SqliteManager mManager;
	private List<SystemDatabaseObject> mDatabaseObjectList;
	private Map<String, List<SystemDatabaseObject>> mDeviceMap;

	public synchronized static DBHelper getInstance(Context context) {
		if (sInstance == null)
			sInstance = new DBHelper(context);
		return sInstance;
	}

	private DBHelper(Context context) {
		refresh(context);
	}

	public SqliteManager getManager() {
		return mManager;
	}

	public synchronized void refresh(Context context) {
		mManager = SqliteManager.getSqliteManager(context);
		mDatabaseObjectList = mManager.obtainSystemDatabase();
		mDeviceMap = new Hashtable<String, List<SystemDatabaseObject>>();
		for (SystemDatabaseObject obj : mDatabaseObjectList) {
			String deviceName = obj.mDeviceApp.Device;
			if (mDeviceMap.containsKey(deviceName)) {
				mDeviceMap.get(deviceName).add(obj);
			} else {
				List<SystemDatabaseObject> list = new ArrayList<SystemDatabaseObject>();
				list.add(obj);
				mDeviceMap.put(deviceName, list);
			}
		}
	}

	/** 해당 Device에 속하는 SystemDatabaseObject들을 반환한다. */
	public List<SystemDatabaseObject> getSystemDatabaseObjectByDevice(
			String device) {
		return mDeviceMap.get(device);
	}

	/** 현재 BlinkDB의 Device-App으로 구성된 Map을 얻는다. */
	public Map<String, List<SystemDatabaseObject>> getDeviceMap() {
		return mDeviceMap;
	}

	/** 현재 BlinkDB에 있는 Device의 목록을 반환한다. */
	public Set<String> getDeviceSet() {
		return mDeviceMap.keySet();
	}

	public SystemDatabaseObject getSystemDatabaseObjectByApp(String device,
			String app) {
		for (SystemDatabaseObject obj : mDeviceMap.get(device)) {
			if (obj.mDeviceApp.App.equals(app))
				return obj;
		}
		return null;
	}

	public synchronized static void close() {
		if (sInstance != null) {
			sInstance.mManager.close();
			sInstance = null;
		}
	}

	public List<SystemDatabaseObject> getAllDB() {
		return mManager.obtainSystemDatabase();
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
		return convertLog(mManager.obtainLog(dateTimeFrom, dateTimeTo));
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
		return convertLog(mManager.obtainLog(device, dateTimeFrom, dateTimeTo));
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
		return convertLog(mManager.obtainLog(device, app, dateTimeFrom,
				dateTimeTo));
	}

	private List<ExternalDeviceAppLog> convertLog(List<DeviceAppLog> logs) {
		ArrayList<ExternalDeviceAppLog> list = new ArrayList<ExternalDeviceAppLog>();
		for (DeviceAppLog log : logs) {
			list.add(new ExternalDeviceAppLog(log));
		}
		return list;
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
