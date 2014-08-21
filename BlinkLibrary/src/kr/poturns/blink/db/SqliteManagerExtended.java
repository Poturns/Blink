package kr.poturns.blink.db;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class SqliteManagerExtended extends SqliteManager {

	public SqliteManagerExtended(Context context) {
		super(context);
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
				"MeasurementData", null, "MesurementId=?",
				new String[] { String.valueOf(measurement.MeasurementId) },
				null, null, null), MeasurementData.class);
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
			measurementData = new MeasurementData();
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

	public <T> boolean register(T... datas) {
		if (datas == null)
			return false;
		boolean result = false, check;
		mSQLiteDatabase.beginTransaction();
		if (datas[0] instanceof App) {
			for (App app : (App[]) datas) {
				ContentValues values = new ContentValues();
				values.put("DeviceId", app.DeviceId);
				values.put("PackageName", app.PackageName);
				values.put("AppName", app.AppName);
				values.put("Version", app.Version);
				check = mSQLiteDatabase.insert("App", null, values) == -1;
			}
			result = true;
		} else if (datas[0] instanceof Measurement) {
			for (Measurement measurement : (Measurement[]) datas) {
				ContentValues values = new ContentValues();
				values.put("AppId", "" + measurement.AppId);
				values.put("Measurement", "" + measurement.Measurement);
				values.put("Type", "" + measurement.Type);
				values.put("Description", "" + measurement.Description);
				check = mSQLiteDatabase.insert("Measurement", null, values) == -1;
			}
		} else if (datas[0] instanceof MeasurementData) {
		}
		mSQLiteDatabase.setTransactionSuccessful();
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

	public boolean removeCurrentDeviceData() {
		Device device = obtainDevice(BlinkDevice.obtainHostDevice());
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
				mSQLiteDatabase.delete("MeasurementData", "MesurementId=?",
						args);
			}
			for (App app : appList) {
				args[0] = String.valueOf(app.AppId);
				mSQLiteDatabase.delete("Fuction", "AppId=?", args);
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

	public boolean removeCurrentAppData(Context context) {
		Device device = obtainDevice(BlinkDevice.obtainHostDevice());
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
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			mSQLiteDatabase.endTransaction();
		}
	}
}
