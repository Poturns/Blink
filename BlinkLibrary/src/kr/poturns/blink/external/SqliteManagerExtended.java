package kr.poturns.blink.external;

import java.util.ArrayList;
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
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/** UI에서만 사용되는 추가적인 Database 쿼리가 적용된, Database에 접근하는 클래스. */
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

	/**
	 * {@link App}을 통해 {@link Device}를 얻는다.
	 * 
	 * @return {@link Device}, 없으면 null
	 */
	public Device obtainDeviceByApp(App app) {
		try {
			return obtainDataListFromCursor(
					mSQLiteDatabase.rawQuery(
							"SELECT * FROM Device WHERE DeviceId = "
									+ app.DeviceId, null), Device.class).get(0);
		} catch (Exception e) {
			return null;
		}
	}

	/** Database안의 Device의 모든 List를 얻는다. */
	public List<Device> obtainDeviceList() {
		return obtainDataListFromCursor(
				mSQLiteDatabase.rawQuery("SELECT * FROM Device ", null),
				Device.class);
	}

	/**
	 * {@link Measurement}로 부터 App 객체를 얻어온다.
	 * 
	 * @return {@link App}, 없으면 null
	 */
	public App obtainAppByMeasurement(Measurement measurement) {
		try {
			return obtainDataListFromCursor(
					mSQLiteDatabase.rawQuery("SELECT * FROM App WHERE AppId = "
							+ measurement.AppId, null), App.class).get(0);
		} catch (Exception e) {
			return null;
		}
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
	public List<Measurement> obtainMeasurementList(App app) {
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

	/**
	 * DB에서 주어진 Measurement의 MeasuremenData List의 크기를 얻는다.
	 * 
	 * @return MeasurementData의 개수, 없으면 0
	 */
	public int obtainMeasurementDataListSize(Measurement measurement) {
		Cursor cursor = mSQLiteDatabase.rawQuery(
				"SELECT COUNT(*) AS count FROM MeasurementData WHERE MeasurementId = "
						+ measurement.MeasurementId, null);
		int size;
		if (cursor.moveToFirst()) {
			size = cursor.getInt(cursor.getColumnIndex("count"));
		} else {
			size = 0;
		}
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
			try {
				measurement.MeasurementName = cursor.getString(cursor
						.getColumnIndex("MeasurementName"));
			} catch (Exception e) {
				measurement.MeasurementName = PrivateUtil
						.obtainSplitMeasurementSchema(measurement);
			}
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
			try {
				app.AppIcon = cursor.getBlob(cursor.getColumnIndex("AppIcon"));
			} catch (Exception e) {
				app.AppIcon = null;
			}
			app.DateTime = cursor.getString(cursor.getColumnIndex("DateTime"));
		} else {
			return null;
		}
		return object;
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
			measurementList.addAll(obtainMeasurementList(app));
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

		List<Measurement> measurementList = obtainMeasurementList(app);

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
	 * @param limit
	 *            반환될 List의 개수의 제한, SQLite의 LIMIT cause, 0 이하의 값인 경우 5개 이다.
	 */
	public List<Measurement> obtainRecentModifiedMeasurement(int limit) {
		if (limit < 0)
			limit = 5;
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT * FROM Measurement WHERE MeasurementId IN ")
				.append("( SELECT DISTINCT MeasurementId FROM MeasurementData ORDER BY DateTime DESC ");
		query.append("LIMIT ").append(limit).append(" )");
		Cursor cursor = mSQLiteDatabase.rawQuery(query.toString(), null);
		return obtainDataListFromCursor(cursor, Measurement.class);
	}

	/**
	 * 주어진 {@link Measurement}의 {@link MeasurementData}중 에서 가장 최근에 변경된 것의
	 * DateTime을 가져온다.
	 */
	public String obtainMeasurementDataDateTime(Measurement measurement) {
		Cursor cursor = mSQLiteDatabase.rawQuery(
				"SELECT MAX(DateTime) AS DateTime FROM MeasurementData WHERE MeasurementId = "
						+ measurement.MeasurementId, null);
		String dateTime;
		if (cursor.moveToNext()) {
			dateTime = cursor.getString(cursor.getColumnIndex("DateTime"));
		} else {
			dateTime = null;
		}
		cursor.close();
		return dateTime;

	}
}
