package kr.poturns.blink.external;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class SqliteManagerExtended extends SqliteManager {
	private SQLiteDatabase mSQLiteDatabase;

	public SqliteManagerExtended(Context context) {
		super(context);
		mSQLiteDatabase = getReadableDatabase();
	}

	/**
	 * BlinkDevice를 통해 DB의 device를 얻는다.
	 * 
	 * @param device
	 *            - {@link BlinkDevice}
	 * @return 인자로 주어진 device와 MacAddress가 일치하는 {@link Device}
	 */
	public Device obtainDevice(BlinkDevice device) {
		if (device == null)
			return null;
		List<Device> deviceList = obtainDataListFromCursor(
				mSQLiteDatabase.rawQuery(
						"SELECT * FROM Device WHERE Device = '"
								+ device.getName() + "'", null), Device.class);
		for (Device dbDevice : deviceList) {
			if (dbDevice.MacAddress.equals(device.getAddress()))
				return dbDevice;
		}
		return null;
	}

	/** Database안의 Device의 모든 List를 얻는다. */
	public List<Device> obtainDeviceList() {
		return obtainDataListFromCursor(
				mSQLiteDatabase.rawQuery("SELECT * FROM Device ", null),
				Device.class);
	}

	/** {@link Measurement}로 부터 App 객체를 얻어온다. */
	public App obtainAppByMeasurement(Measurement measurement) {
		return null;
	}

	/** DB에서 모든 App의 List를 얻는다. */
	public List<App> obtainAppList() {
		return obtainDataListFromCursor(
				mSQLiteDatabase.rawQuery("SELECT * FROM App ", null), App.class);
	}

	/** DB에서 주어진 device에 속하는 App의 List를 얻는다. */
	public List<App> obtainAppList(Device device) {
		if (device == null) {
			return new ArrayList<App>();
		}
		Cursor cursor = mSQLiteDatabase.query("App", null, "DeviceId=?",
				new String[] { String.valueOf(device.DeviceId) }, null, null,
				null);
		return obtainDataListFromCursor(cursor, App.class);
	}

	/** DB에서 주어진 App에 속하는 Function의 List를 얻는다. */
	public List<Function> obtainFunctionList(App app) {
		if (app == null) {
			return new ArrayList<Function>();
		}
		return obtainDataListFromCursor(mSQLiteDatabase.query("Function", null,
				"AppId=?", new String[] { String.valueOf(app.AppId) }, null,
				null, null), Function.class);
	}

	/** DB에서 주어진 App에 속하는 Measurement의 List를 얻는다. */
	public List<Measurement> obtainMesurementList(App app) {
		if (app == null) {
			return new ArrayList<Measurement>();
		}
		return obtainDataListFromCursor(mSQLiteDatabase.query("Measurement",
				null, "AppId=?", new String[] { String.valueOf(app.AppId) },
				null, null, null), Measurement.class);
	}

	/** DB에서 주어진 Measurement의 MeasuremenData List를 얻는다. */
	public List<MeasurementData> obtainMeasurementDataList(
			Measurement measurement) {
		if (measurement == null) {
			return new ArrayList<MeasurementData>();
		}
		return obtainDataListFromCursor(mSQLiteDatabase.query(
				"MeasurementData", null, "MeasurementId=?",
				new String[] { String.valueOf(measurement.MeasurementId) },
				null, null, null), MeasurementData.class);
	}

	/** DB에서 주어진 Measurement의 MeasuremenData List의 크기를 얻는다. */
	public int obtainMeasurementDataListSize(Measurement measurement) {
		Cursor cursor = mSQLiteDatabase.query("MeasurementData", null,
				"MeasurementId=?",
				new String[] { String.valueOf(measurement.MeasurementId) },
				null, null, null);
		int size = cursor.getCount();
		cursor.close();
		return size;
	}

	/* 주어진 class에 따라 Cursor에서 데이터 리스트를 얻어온다. */
	private static final <T> List<T> obtainDataListFromCursor(Cursor cursor,
			Class<T> classInfo) {
		ArrayList<T> list = new ArrayList<T>();
		try {
			while (cursor.moveToNext()) {
				list.add(retriveDataFromCursor(cursor, classInfo.newInstance()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			cursor.close();
		}
		return list;
	}

	/* 주어진 class에 따라 Cursor에서 데이터 하나를 얻어온다. */
	private static final <T> T retriveDataFromCursor(Cursor cursor, T object) {
		if (object instanceof MeasurementData) {
			MeasurementData measurementData = (MeasurementData) object;
			measurementData.MeasurementId = cursor.getInt(cursor
					.getColumnIndex("MeasurementId"));
			measurementData.GroupId = cursor.getInt(cursor
					.getColumnIndex("GroupId"));
			measurementData.Data = cursor.getString(cursor
					.getColumnIndex("Data"));
			measurementData.DateTime = cursor.getString(cursor
					.getColumnIndex("DateTime"));
		} else if (object instanceof Measurement) {
			Measurement measurement = (Measurement) object;
			measurement.AppId = cursor.getInt(cursor.getColumnIndex("AppId"));
			measurement.Description = cursor.getString(cursor
					.getColumnIndex("Description"));
			measurement.Measurement = cursor.getString(cursor
					.getColumnIndex("Measurement"));
			measurement.MeasurementId = cursor.getInt(cursor
					.getColumnIndex("MeasurementId"));
			measurement.Type = cursor.getString(cursor.getColumnIndex("Type"));
		} else if (object instanceof Function) {
			Function function = (Function) object;
			function.AppId = cursor.getInt(cursor.getColumnIndex("AppId"));
			function.Function = cursor.getString(cursor
					.getColumnIndex("Function"));
			function.Description = cursor.getString(cursor
					.getColumnIndex("Description"));
			function.Action = cursor.getString(cursor.getColumnIndex("Action"));
			function.Type = cursor.getInt(cursor.getColumnIndex("Type"));
		} else if (object instanceof Device) {
			Device device = (Device) object;
			device.DeviceId = cursor.getInt(cursor.getColumnIndex("DeviceId"));
			device.Device = cursor.getString(cursor.getColumnIndex("Device"));
			device.UUID = cursor.getString(cursor.getColumnIndex("UUID"));
			device.MacAddress = cursor.getString(cursor
					.getColumnIndex("MacAddress"));
			device.DateTime = cursor.getString(cursor
					.getColumnIndex("DateTime"));
		} else if (object instanceof App) {
			App app = (App) object;
			app.AppId = cursor.getInt(cursor.getColumnIndex("AppId"));
			app.DeviceId = cursor.getInt(cursor.getColumnIndex("DeviceId"));
			app.PackageName = cursor.getString(cursor
					.getColumnIndex("PackageName"));
			app.AppName = cursor.getString(cursor.getColumnIndex("AppName"));
			app.Version = cursor.getInt(cursor.getColumnIndex("Version"));
			app.DateTime = cursor.getString(cursor.getColumnIndex("DateTime"));
		} else {
			return null;
		}
		return object;
	}

	public boolean register(Object... datas) {
		if (datas == null)
			return false;
		boolean result = true;
		mSQLiteDatabase.beginTransaction();
		for (Object obj : datas) {
			if (obj instanceof App) {
				App app = (App) obj;
				ContentValues values = new ContentValues();
				values.put("DeviceId", app.DeviceId);
				values.put("PackageName", app.PackageName);
				values.put("AppName", app.AppName);
				values.put("Version", app.Version);
				result &= mSQLiteDatabase.insert("App", null, values) == -1;
			} else if (obj instanceof Measurement) {
				Measurement measurement = (Measurement) obj;
				ContentValues values = new ContentValues();
				values.put("AppId", String.valueOf(measurement.AppId));
				values.put("Measurement", measurement.Measurement);
				values.put("Type", "" + measurement.Type);
				values.put("Description", measurement.Description);
				result &= mSQLiteDatabase.insert("Measurement", null, values) == -1;
			} else if (obj instanceof MeasurementData) {

			} else if (obj instanceof Function) {

			} else if (obj instanceof Device) {

			} else
				continue;
			if (!result)
				break;
		}
		if (result) {
			mSQLiteDatabase.setTransactionSuccessful();
		}
		mSQLiteDatabase.endTransaction();
		return result;
	}

	/**
	 * Database에서 Device와 App의 관계를 Map으로 나타낸다.
	 */
	public Map<Device, List<App>> obtainDeviceMap() {
		List<Device> deviceList = obtainDeviceList();
		Map<Device, List<App>> map = new Hashtable<Device, List<App>>();
		for (Device device : deviceList) {
			map.put(device, obtainAppList(device));
		}
		return map;
	}

	/** Database에서 현재 Device의 Data를 삭제한다 */
	public boolean removeCurrentDeviceData() {
		Device device = obtainDevice(obtainHostDevice());
		List<App> appList = obtainAppList(device);
		List<Measurement> measurementList = new ArrayList<Measurement>();

		for (App app : appList) {
			measurementList.addAll(obtainMesurementList(app));
		}

		mSQLiteDatabase.beginTransaction();
		try {
			String[] args = new String[1];
			for (Measurement measurement : measurementList) {
				args[0] = String.valueOf(measurement.MeasurementId);
				mSQLiteDatabase.delete("MeasurementData", "MeasurementId=?",
						args);
			}
			for (App app : appList) {
				args[0] = String.valueOf(app.AppId);
				mSQLiteDatabase.delete("Function", "AppId=?", args);
				mSQLiteDatabase.delete("Measurement", "AppId=?", args);
			}
			args[0] = String.valueOf(device.DeviceId);
			mSQLiteDatabase.delete("App", "DeviceId=?", args);
			mSQLiteDatabase.setTransactionSuccessful();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			mSQLiteDatabase.endTransaction();
		}
	}

	/** @hide */
	public boolean removeCurrentAppData(Context context) {
		Device device = obtainDevice(obtainHostDevice());
		List<App> appList = obtainAppList(device);
		App app = null;
		for (App tempApp : appList) {
			if (tempApp.PackageName.equals(context.getPackageName())) {
				app = tempApp;
				break;
			}
		}
		if (app == null)
			return false;

		List<Measurement> measurementList = obtainMesurementList(app);

		mSQLiteDatabase.beginTransaction();
		try {
			String[] args = new String[1];
			for (Measurement measurement : measurementList) {
				args[0] = String.valueOf(measurement.MeasurementId);
				mSQLiteDatabase.delete("MeasurementData", "MesurementId=?",
						args);
			}

			args[0] = String.valueOf(app.AppId);
			mSQLiteDatabase.delete("Fuction", "AppId=?", args);
			mSQLiteDatabase.delete("Measurement", "AppId=?", args);
			mSQLiteDatabase.setTransactionSuccessful();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			mSQLiteDatabase.endTransaction();
		}
	}

	/** 현재 장비를 나타내는 BlinkDevice를 얻는다. */
	private static BlinkDevice obtainHostDevice() {
		return BlinkDevice.HOST;
	}

	/**
	 * 주어진 시간 이후에 변경이 감지된 {@link Measurement}의 리스트를 반환한다.
	 * 
	 * @param lastTime
	 *            변경을 감지할 기준점이 될 시간
	 * @param limit
	 *            반환될 List의 개수의 제한, SQLite의 LIMIT cause, 0 이하의 값인 경우 제한이 없다.
	 */
	public List<Measurement> obtainRecentModifiedMeasurement(Date lastTime,
			int limit) {
		if (lastTime == null)
			lastTime = new Date();
		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(lastTime);
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM Measurement WHERE MeasurementId IN ")
				.append("( SELECT DISTINCT MeasurementId FROM MeasurementData WHERE DateTime > ? )");
		if (limit > 0)
			query.append(" LIMIT ").append(limit);
		Cursor cursor = mSQLiteDatabase.rawQuery(query.toString(),
				new String[] { time });
		return obtainDataListFromCursor(cursor, Measurement.class);
	}
}