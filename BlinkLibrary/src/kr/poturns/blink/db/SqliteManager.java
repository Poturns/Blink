package kr.poturns.blink.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.util.ClassUtil;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Sqlite에 직접 쿼리를 날리는 클래스 또한 여러 Sqlite 쿼리와 DB 변경에 관한 URI, 몇 가지 TYPE, DB경로를 정의하고
 * 있다.
 * 
 * @author mementohora
 * 
 */
public class SqliteManager extends SQLiteOpenHelper implements IBlinkDatabase {
	private final String tag = "SqliteManager";

	/**
	 * Sqlite에 쿼리를 날릴 때 사용되는 기본 쿼리문 뒤에 조건을 붙여서 사용된다.
	 */
	protected final String SQL_SELECT_DEVICE = "SELECT * FROM Device ";
	protected final String SQL_SELECT_APP = "SELECT * FROM App ";
	protected final String SQL_SELECT_FUNCTION = "SELECT * FROM Function ";
	protected final String SQL_SELECT_MEASUREMENT = "SELECT * FROM Measurement ";
	protected final String SQL_SELECT_MEASUREMENTDATA = "SELECT * FROM MeasurementData ";
	protected final String SQL_SELECT_SYNCMEASUREMENTDATA = "SELECT * FROM SyncMeasurementData ";
	protected final String SQL_SELECT_GROUPID = "SELECT max(GroupId) FROM MeasurementData ";
	protected final String SQL_SELECT_MEASUREMENTDATAID = "SELECT max(MeasurementId) FROM MeasurementData ";
	protected final String SQL_DELETE_DEVICE = "delete from Device ";
	protected final String SQL_DELETE_APP = "delete from App ";
	protected final String SQL_DELETE_FUNCTION = "delete from Function ";
	protected final String SQL_DELETE_MEASUREMENT = "delete from Measurement ";
	protected final String SQL_DELETE_MEASUREMENTDATA = "delete from MeasurementData ";
	protected final String SQL_SELECT_LOG = "SELECT * FROM BlinkLog ";

	Context CONTEXT;
	SQLiteDatabase mSQLiteDatabase;
	Gson gson;

	private SqliteManager(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		mSQLiteDatabase = this.getWritableDatabase();
	}

	/**
	 * SQLiteOpenHelper의 생성자에 기본적인 버전(1), DB 경로를 지정하여 호출한다.
	 * 
	 * @param context
	 */
	public SqliteManager(Context context) {
		super(context, EXTERNAL_DB_FILE_PATH + EXTERNAL_DB_FILE_NAME, null, 1);
		mSQLiteDatabase = this.getWritableDatabase();
		gson = new GsonBuilder().setPrettyPrinting().create();
		CONTEXT = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createBlinkDatabase(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		updateBlinkDatabase(db);
	}

	/**
	 * SQLiteDatabase에 테이블을 생성한다.
	 * 
	 * @param db
	 */
	public void createBlinkDatabase(SQLiteDatabase db) {
		// DB에 테이블 생성하기
		String sql = "";

		// Create DeviceAppList table sql statement
		// TODO FK에 ON UPDATE ,ON DELETE CASCADE 를 적용? 
		sql = "create table 'Device' ("
				+ "'DeviceId' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "'Device' TEXT NOT NULL," + "'UUID' TEXT,"
				+ "'MacAddress' TEXT,"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime')),"
				+ "UNIQUE ('MacAddress')" + "); ";
		db.execSQL(sql);

		sql = "create table 'App' ("
				+ "'AppId' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "'DeviceId' INTEGER NOT NULL,"
				+ "'PackageName' TEXT NOT NULL," + "'AppName' TEXT NOT NULL,"
				+ "'AppIcon' BLOB," + "'Version' INTEGER NOT NULL DEFAULT (1),"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime')),"
				+ "UNIQUE ('DeviceId','PackageName'),"
				+ "FOREIGN KEY('DeviceId') REFERENCES Device('DeviceId')"
				+ "); ";
		db.execSQL(sql);

		// Create DeviceAppMeasurement table sql statement
		sql = "create table 'Measurement' (" + "'AppId' INTEGER NOT NULL,"
				+ "'MeasurementId' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "'MeasurementName' TEXT NOT NULL,"
				+ "'Measurement' TEXT NOT NULL," + "'Type' TEXT NOT NULL,"
				+ "'Description' TEXT NOT NULL,"
				+ "UNIQUE ('AppId','Measurement'),"
				+ "FOREIGN KEY('AppId') REFERENCES App('AppId')" + ");";
		// sql문 실행하기
		db.execSQL(sql);

		// Create DeviceAppFunction table sql statement
		sql = "create table 'Function' (" + "'AppId' INTEGER NOT NULL,"
				+ "'Function' TEXT NOT NULL," + "'Description' TEXT,"
				+ "'Action' TEXT NOT NULL,"
				+ "'Type' INTEGER NOT NULL DEFAULT (1),"
				+ "PRIMARY KEY ('AppId','Action','Type'),"
				+ "FOREIGN KEY('AppId') REFERENCES App('AppId')" + ");";
		db.execSQL(sql);

		Log.i(tag, "createSystemDatabase ok");

		sql = "create table 'MeasurementData' ("
				+ "'MeasurementId' INTEGER NOT NULL,"
				+ "'MeasurementDataId' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "'GroupId' INTEGER,"
				+ "'Data' TEXT NOT NULL,"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime')),"
				+ "UNIQUE ('MeasurementId','GroupId'),"
				+ "FOREIGN KEY('MeasurementId') REFERENCES Measurement('MeasurementId')"
				+ ");";
		db.execSQL(sql);

		Log.i(tag, "createMeasurementDatabase ok");

		sql = "create table 'BlinkLog' ("
				+ "'LogId' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "'Device' TEXT NOT NULL," + "'App' TEXT NOT NULL,"
				+ "'Type' INTEGER NOT NULL," + "'Content' TEXT NOT NULL,"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime'))"
				+ ");";
		db.execSQL(sql);

		Log.i(tag, "logDatabase ok");

		sql = "create table 'SyncMeasurementData' ("
				+ "'DeviceId' INTEGER PRIMARY KEY ,"
				+ "'MeasurementDataId' INTEGER NOT NULL,"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime')),"
				+ "FOREIGN KEY('MeasurementDataId') REFERENCES MeasurementData('MeasurementDataId')"
				+ ");";
		db.execSQL(sql);

		Log.i(tag, "SynchronizeDatabase ok");
	}

	/**
	 * 기존 테이블을 모두 삭제한 후 다시 생성한다.
	 * 
	 * @param db
	 */
	public void updateBlinkDatabase(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS Device");
		db.execSQL("DROP TABLE IF EXISTS App");
		db.execSQL("DROP TABLE IF EXISTS Measurement");
		db.execSQL("DROP TABLE IF EXISTS Function");
		db.execSQL("DROP TABLE IF EXISTS Data");
		db.execSQL("DROP TABLE IF EXISTS MeasurementData");
		db.execSQL("DROP TABLE IF EXISTS Log");
		// 새로 생성될 수 있도록 onCreate() 메소드를 생성한다.
		createBlinkDatabase(db);
	}

	/**
	 * 주어진 파라미터의 BlinkAppInfo를 Database에 등록한다. 등록하면서 자동적으로 부여되는 데이터를 얻기 위해 등록 후
	 * 다시 obtain- 매소드를 호출한다. 등록이 완료될 때 URI_OBSERVER_BLINKAPP에 notifyChange를
	 * 호출한다.
	 * 
	 * @param mBlinkAppInfo
	 */
	public void registerBlinkApp(BlinkAppInfo mBlinkAppInfo) {
		registerDevice(mBlinkAppInfo);
		obtainDeviceList(mBlinkAppInfo);
		registerApp(mBlinkAppInfo);
		obtainApp(mBlinkAppInfo);
		registerFunction(mBlinkAppInfo);
		registerMeasurement(mBlinkAppInfo);
		CONTEXT.getContentResolver().notifyChange(URI_OBSERVER_BLINKAPP, null);
		Log.i(tag, "registerBlinkApp OK");
	}

	/**
	 * BlinkAppSync에서 동기화를 위해 호출하는 매소드 URI_OBSERVER_BLINKAPP를 통해 알리지 않는다.
	 */
	public void registerBlinkAppSync(BlinkAppInfo mBlinkAppInfo) {
		registerDevice(mBlinkAppInfo);
		obtainDeviceList(mBlinkAppInfo);
		registerApp(mBlinkAppInfo);
		obtainApp(mBlinkAppInfo);
		registerFunction(mBlinkAppInfo);
		registerMeasurement(mBlinkAppInfo);
		CONTEXT.getContentResolver().notifyChange(URI_OBSERVER_SYNC, null);
		Log.i(tag, "registerBlinkAppSync OK");
	}

	/**
	 * 주어진 device, PackageName으로 BlinkAppInfo를 검색한다. 없을 경우 기본 값들을 설정하고 isExist에
	 * false를 설정하여 리턴한다. 사용자는 isExist를 확인하여 없을 경우 사용할 Function과 Measurement를
	 * 추가하고 registerBlinkApp()를 통해 등록 해야한다.
	 * 
	 * @param device
	 * @param app
	 * @return
	 */
	public BlinkAppInfo obtainBlinkApp(String device, String PackageName) {
		BlinkAppInfo mBlinkAppInfo = new BlinkAppInfo();
		Device mDevice = mBlinkAppInfo.mDevice;
		App mApp = mBlinkAppInfo.mApp;
		mDevice.Device = device;
		mApp.PackageName = PackageName;
		// 기존에 등록된 값이 있으면 해당 값을 찾아서 리턴

		if (obtainDeviceList(mBlinkAppInfo) && obtainApp(mBlinkAppInfo)) {
			mBlinkAppInfo.isExist = true;
			obtainFunction(mBlinkAppInfo);
			obtainMeasurement(mBlinkAppInfo);
		}
		// 기존에 등록된 값이 없으면
		else {
			mApp.Version = 1;
			mApp.PackageName = PackageName;
			mApp.DeviceId = mDevice.DeviceId;
			mApp.AppName = "";
			mBlinkAppInfo.isExist = false;
			return mBlinkAppInfo;
		}
		return mBlinkAppInfo;
	}

	/**
	 * 모든 BlinkAppInfo를 검색하여 리턴한다.
	 * 
	 * @return
	 */
	public ArrayList<BlinkAppInfo> obtainBlinkApp() {
		ArrayList<BlinkAppInfo> mBlinkAppInfoList = new ArrayList<BlinkAppInfo>();
		ArrayList<Device> mDeviceList = obtainDeviceList("");
		ArrayList<App> mAppList = obtainAppList("");
		BlinkAppInfo mBlinkAppInfo = null;
		for (int i = 0; i < mDeviceList.size(); i++) {
			for (int j = 0; j < mAppList.size(); j++) {
				if (mDeviceList.get(i).DeviceId == mAppList.get(j).DeviceId) {
					mBlinkAppInfo = new BlinkAppInfo();
					mBlinkAppInfo.mDevice = mDeviceList.get(i);
					mBlinkAppInfo.mApp = mAppList.get(j);
					obtainFunction(mBlinkAppInfo);
					obtainMeasurement(mBlinkAppInfo);
					mBlinkAppInfo.isExist = true;
					mBlinkAppInfoList.add(mBlinkAppInfo);
				}
			}
		}
		return mBlinkAppInfoList;
	}

	/**
	 * Device을 검색한다. 검색 조건과 결과는 매개변수로 넘긴 BlinkAppInfo 객체에 저장된다. Device를 등록했을 때
	 * ID를 비롯한 자동 생성되는 데이터를 얻어오기 위해 사용된다. 사용자가 직접 호출할 일은 없다.
	 * 
	 * @param mBlinkAppInfo
	 * @return
	 */
	private boolean obtainDeviceList(BlinkAppInfo mBlinkAppInfo) {
		Device mDevice = mBlinkAppInfo.mDevice;
		String query = SQL_SELECT_DEVICE + "where Device=?";
		String[] args = { mDevice.Device };
		Cursor mCursor = mSQLiteDatabase.rawQuery(query, args);
		if (mCursor.moveToNext()) {
			mDevice.DeviceId = mCursor.getInt(mCursor
					.getColumnIndex("DeviceId"));
			mDevice.Device = mCursor
					.getString(mCursor.getColumnIndex("Device"));
			mDevice.UUID = mCursor.getString(mCursor.getColumnIndex("UUID"));
			mDevice.MacAddress = mCursor.getString(mCursor
					.getColumnIndex("MacAddress"));
			mDevice.DateTime = mCursor.getString(mCursor
					.getColumnIndex("DateTime"));
			return true;
		}
		return false;
	}

	/**
	 * where 조건에 일치하는 Device의 리스트를 얻어온다.
	 * 
	 * @param where
	 * @return
	 */
	public ArrayList<Device> obtainDeviceList(String where) {
		if (where == null || where.equals(""))
			where = "";
		else
			where = "where " + where;
		ArrayList<Device> mDeviceList = new ArrayList<Device>();
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_DEVICE + where,
				null);
		Device mDevice = null;
		while (mCursor.moveToNext()) {
			mDevice = new Device();
			mDevice.DeviceId = mCursor.getInt(mCursor
					.getColumnIndex("DeviceId"));
			mDevice.Device = mCursor
					.getString(mCursor.getColumnIndex("Device"));
			mDevice.UUID = mCursor.getString(mCursor.getColumnIndex("UUID"));
			mDevice.MacAddress = mCursor.getString(mCursor
					.getColumnIndex("MacAddress"));
			mDevice.DateTime = mCursor.getString(mCursor
					.getColumnIndex("DateTime"));
			mDeviceList.add(mDevice);
		}
		return mDeviceList;
	}

	/**
	 * App을 검색한다. 검색 조건과 결과는 매개변수로 넘긴 BlinkAppInfo 객체에 저장된다. App을 등록했을 때 ID를 비롯한
	 * 자동 생성되는 데이터를 얻어오기 위해 사용된다. 사용자가 직접 호출할 일은 없다.
	 * 
	 * @param mBlinkAppInfo
	 * @return
	 */
	private boolean obtainApp(BlinkAppInfo mBlinkAppInfo) {
		App mApp = mBlinkAppInfo.mApp;
		String query = SQL_SELECT_APP + "where DeviceId=? and PackageName=?";
		String[] args = { String.valueOf(mBlinkAppInfo.mDevice.DeviceId),
				mBlinkAppInfo.mApp.PackageName };
		Cursor mCursor = mSQLiteDatabase.rawQuery(query, args);
		if (mCursor.moveToNext()) {
			mApp.AppId = mCursor.getInt(mCursor.getColumnIndex("AppId"));
			mApp.DeviceId = mCursor.getInt(mCursor.getColumnIndex("DeviceId"));
			mApp.PackageName = mCursor.getString(mCursor
					.getColumnIndex("PackageName"));
			mApp.AppName = mCursor.getString(mCursor.getColumnIndex("AppName"));
			mApp.AppIcon = mCursor.getBlob(mCursor.getColumnIndex("AppIcon"));
			mApp.Version = mCursor.getInt(mCursor.getColumnIndex("Version"));
			mApp.DateTime = mCursor.getString(mCursor
					.getColumnIndex("DateTime"));
			return true;
		}
		return false;
	}

	/**
	 * where 조건에 일치하는 App의 리스트를 얻어온다.
	 * 
	 * @param where
	 * @return
	 */
	public ArrayList<App> obtainAppList(String where) {
		if (where == null || where.equals(""))
			where = "";
		else
			where = "where " + where;
		ArrayList<App> mAppList = new ArrayList<App>();
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_APP + where, null);
		App mApp = null;
		while (mCursor.moveToNext()) {
			mApp = new App();
			mApp.AppId = mCursor.getInt(mCursor.getColumnIndex("AppId"));
			mApp.DeviceId = mCursor.getInt(mCursor.getColumnIndex("DeviceId"));
			mApp.PackageName = mCursor.getString(mCursor
					.getColumnIndex("PackageName"));
			mApp.AppName = mCursor.getString(mCursor.getColumnIndex("AppName"));
			mApp.AppIcon = mCursor.getBlob(mCursor.getColumnIndex("AppIcon"));
			mApp.Version = mCursor.getInt(mCursor.getColumnIndex("Version"));
			mApp.DateTime = mCursor.getString(mCursor
					.getColumnIndex("DateTime"));
			mAppList.add(mApp);
		}
		return mAppList;
	}

	/**
	 * Device를 등록한다. registerBlinkApp()에서 호출된다.
	 * 
	 * @param mBlinkAppInfo
	 */
	private void registerDevice(BlinkAppInfo mBlinkAppInfo) {
		Device mDevice = mBlinkAppInfo.mDevice;
		ContentValues values = new ContentValues();
		values.put("Device", mDevice.Device);
		values.put("UUID", mDevice.UUID);
		values.put("MacAddress", mDevice.MacAddress);
		mSQLiteDatabase.insert("Device", null, values);
	}

	/**
	 * App를 등록한다. registerBlinkApp()에서 호출된다.
	 * 
	 * @param mBlinkAppInfo
	 */
	private void registerApp(BlinkAppInfo mBlinkAppInfo) {
		Device mDevice = mBlinkAppInfo.mDevice;
		App mApp = mBlinkAppInfo.mApp;
		ContentValues values = new ContentValues();
		values.put("DeviceId", mDevice.DeviceId);
		values.put("PackageName", mApp.PackageName);
		values.put("AppName", mApp.AppName);
		values.put("AppIcon", mApp.AppIcon);
		values.put("Version", mApp.Version);
		mSQLiteDatabase.insert("App", null, values);
	}

	/**
	 * Function를 등록한다. registerBlinkApp()에서 호출된다.
	 * 
	 * @param mBlinkAppInfo
	 */
	private void registerFunction(BlinkAppInfo mBlinkAppInfo) {
		App mApp = mBlinkAppInfo.mApp;
		ArrayList<Function> mFunctionList = mBlinkAppInfo.mFunctionList;
		Function mFunction;
		for (int i = 0; i < mFunctionList.size(); i++) {
			mFunction = mFunctionList.get(i);
			mFunction.AppId = mApp.AppId;
			ContentValues values = new ContentValues();
			values.put("AppId", "" + mFunction.AppId);
			values.put("Function", mFunction.Function);
			values.put("Description", mFunction.Description);
			values.put("Action", mFunction.Action);
			values.put("Type", mFunction.Type);
			mSQLiteDatabase.insert("Function", null, values);
			Log.i(tag, "registerFunction OK");
		}
	}

	/**
	 * Measurement를 등록한다. registerBlinkApp()에서 호출된다.
	 * 
	 * @param mBlinkAppInfo
	 */
	private void registerMeasurement(BlinkAppInfo mBlinkAppInfo) {
		App mApp = mBlinkAppInfo.mApp;
		ArrayList<Measurement> mMeasurementList = mBlinkAppInfo.mMeasurementList;
		Measurement mMeasurement;
		for (int i = 0; i < mMeasurementList.size(); i++) {
			mMeasurement = mMeasurementList.get(i);
			mMeasurement.AppId = mApp.AppId;
			// if(mMeasurement.Measurement.endsWith("/DateTime"))continue;
			ContentValues values = new ContentValues();
			values.put("AppId", "" + mMeasurement.AppId);
			values.put("MeasurementName", "" + mMeasurement.MeasurementName);
			values.put("Measurement", "" + mMeasurement.Measurement);
			values.put("Type", "" + mMeasurement.Type);
			values.put("Description", "" + mMeasurement.Description);
			mSQLiteDatabase.insert("Measurement", null, values);
			Log.i(tag, "registerMeasurement OK");
		}
	}

	/**
	 * Function을 검색한다. 검색 조건과 결과는 매개변수로 넘긴 BlinkAppInfo 객체에 저장된다. Function을 등록했을
	 * 때 ID를 비롯한 자동 생성되는 데이터를 얻어오기 위해 사용된다. 사용자가 직접 호출할 일은 없다.
	 * 
	 * @param mBlinkAppInfo
	 * @return
	 */
	private void obtainFunction(BlinkAppInfo mBlinkAppInfo) {
		App mApp = mBlinkAppInfo.mApp;
		String sql = SQL_SELECT_FUNCTION + "where AppId=?";
		String[] args = { String.valueOf(mApp.AppId) };
		Cursor mCursor = mSQLiteDatabase.rawQuery(sql, args);
		Function mFunction;
		while (mCursor.moveToNext()) {
			mFunction = new Function();
			mFunction.AppId = mCursor.getInt(mCursor.getColumnIndex("AppId"));
			mFunction.Function = mCursor.getString(mCursor
					.getColumnIndex("Function"));
			mFunction.Description = mCursor.getString(mCursor
					.getColumnIndex("Description"));
			mFunction.Action = mCursor.getString(mCursor
					.getColumnIndex("Action"));
			mFunction.Type = mCursor.getInt(mCursor.getColumnIndex("Type"));
			mBlinkAppInfo.mFunctionList.add(mFunction);
		}
	}

	/**
	 * where 조건에 일치하는 Function의 리스트를 얻어온다.
	 * 
	 * @param where
	 * @return
	 */
	public ArrayList<Function> obtainFunctionList(String where) {
		if (where == null || where.equals(""))
			where = "";
		else
			where = "where " + where;
		ArrayList<Function> mFunctionList = new ArrayList<Function>();
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_FUNCTION + where,
				null);
		Function mFunction = null;
		while (mCursor.moveToNext()) {
			mFunction = new Function();
			mFunction.AppId = mCursor.getInt(mCursor.getColumnIndex("AppId"));
			mFunction.Function = mCursor.getString(mCursor
					.getColumnIndex("Function"));
			mFunction.Description = mCursor.getString(mCursor
					.getColumnIndex("Description"));
			mFunction.Action = mCursor.getString(mCursor
					.getColumnIndex("Action"));
			mFunction.Type = mCursor.getInt(mCursor.getColumnIndex("Type"));
			mFunctionList.add(mFunction);
		}
		return mFunctionList;
	}

	/**
	 * Measurement을 검색한다. 검색 조건과 결과는 매개변수로 넘긴 BlinkAppInfo 객체에 저장된다.
	 * Measurement을 등록했을 때 ID를 비롯한 자동 생성되는 데이터를 얻어오기 위해 사용된다. 사용자가 직접 호출할 일은 없다.
	 * 
	 * @param mBlinkAppInfo
	 * @return
	 */
	private void obtainMeasurement(BlinkAppInfo mBlinkAppInfo) {
		App mApp = mBlinkAppInfo.mApp;
		String[] args = { String.valueOf(mApp.AppId) };
		String sql = SQL_SELECT_MEASUREMENT + "where AppId=?";
		Cursor mCursor = mSQLiteDatabase.rawQuery(sql, args);
		Measurement mMeasurement;
		while (mCursor.moveToNext()) {
			mMeasurement = new Measurement();
			mMeasurement.AppId = mCursor
					.getInt(mCursor.getColumnIndex("AppId"));
			mMeasurement.MeasurementId = mCursor.getInt(mCursor
					.getColumnIndex("MeasurementId"));
			mMeasurement.MeasurementName = mCursor.getString(mCursor
					.getColumnIndex("MeasurementName"));
			mMeasurement.Measurement = mCursor.getString(mCursor
					.getColumnIndex("Measurement"));
			mMeasurement.Type = mCursor.getString(mCursor
					.getColumnIndex("Type"));
			mMeasurement.Description = mCursor.getString(mCursor
					.getColumnIndex("Description"));
			mBlinkAppInfo.mMeasurementList.add(mMeasurement);
		}
	}

	/**
	 * 매개변수로 주어진 clazz의 필드와 containType으로 일치하는 Measurement 리스트를 검색하여 리턴한다.
	 * 
	 * @param clazz
	 * @param containType
	 *            <li>{@link IBlinkDatabase#CONTAIN_DEFAULT}</li> <li>
	 *            {@link IBlinkDatabase#CONTAIN_PARENT}</li> <li>
	 *            {@link IBlinkDatabase#CONTAIN_FIELD}</li> <br>
	 *            중 하나
	 * @return Measurement의 리스트
	 */
	public ArrayList<Measurement> obtainMeasurementList(Class<?> clazz,
			int containType) {
		ArrayList<Measurement> mMeasurementList = new ArrayList<Measurement>();

		Field[] mFields = clazz.getFields();
		for (int i = 0; i < mFields.length; i++) {
			mMeasurementList.addAll(obtainMeasurementList(mFields[i],
					containType));
		}
		return mMeasurementList;
	}

	/**
	 * 매개변수로 주어진 필드와 containType으로 일치하는 Measurement 리스트를 검색하여 리턴한다.
	 * 
	 * @param Measurement
	 * @param containType
	 *            <li>{@link IBlinkDatabase#CONTAIN_DEFAULT}</li> <li>
	 *            {@link IBlinkDatabase#CONTAIN_PARENT}</li> <li>
	 *            {@link IBlinkDatabase#CONTAIN_FIELD}</li> <br>
	 *            중 하나
	 * @return
	 */
	public ArrayList<Measurement> obtainMeasurementList(Field Measurement,
			int containType) {
		String where = "";
		switch (containType) {
		case CONTAIN_DEFAULT:
			where = "Measurement='" + ClassUtil.obtainFieldSchema(Measurement)
					+ "'";
			break;
		case CONTAIN_FIELD:
			where = "Measurement like '%/" + Measurement.getName() + "'";
			break;

		case CONTAIN_PARENT:
			where = SQL_SELECT_MEASUREMENT + "where Measurement like '%"
					+ ClassUtil.obtainParentSchema(Measurement) + "'";
			break;
		default:
			// TODO default 처리할 것
			break;
		}

		return obtainMeasurementList(where);
	}

	/**
	 * where 조건과 일치하는 Measurement 리스트를 검색하여 리턴한다.
	 * 
	 * @param where
	 * @return
	 */
	public ArrayList<Measurement> obtainMeasurementList(String where) {
		if (where == null || where.equals(""))
			where = "";
		else
			where = "where " + where;
		ArrayList<Measurement> mMeasurementList = new ArrayList<Measurement>();
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_MEASUREMENT
				+ where, null);
		Measurement mMeasurement = null;
		while (mCursor.moveToNext()) {
			mMeasurement = new Measurement();
			mMeasurement.AppId = mCursor
					.getInt(mCursor.getColumnIndex("AppId"));
			mMeasurement.Description = mCursor.getString(mCursor
					.getColumnIndex("Description"));
			mMeasurement.Measurement = mCursor.getString(mCursor
					.getColumnIndex("Measurement"));
			mMeasurement.MeasurementId = mCursor.getInt(mCursor
					.getColumnIndex("MeasurementId"));
			mMeasurement.Type = mCursor.getString(mCursor
					.getColumnIndex("Type"));
			mMeasurementList.add(mMeasurement);
		}
		return mMeasurementList;
	}

	/**
	 * where 조건과 일치하는 MeasurementData 리스트를 검색하여 리턴한다.
	 * 
	 * @param where
	 * @return
	 */
	public ArrayList<MeasurementData> obtainMeasurementDataList(String where) {
		if (where == null || where.equals(""))
			where = "";
		else
			where = "where " + where;
		ArrayList<MeasurementData> mMeasurementDataList = new ArrayList<MeasurementData>();
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_MEASUREMENTDATA
				+ where, null);
		MeasurementData mMeasurementData = null;
		while (mCursor.moveToNext()) {
			mMeasurementData = new MeasurementData();
			mMeasurementData.MeasurementId = mCursor.getInt(mCursor
					.getColumnIndex("MeasurementId"));
			mMeasurementData.MeasurementDataId = mCursor.getInt(mCursor
					.getColumnIndex("MeasurementDataId"));
			mMeasurementData.GroupId = mCursor.getInt(mCursor
					.getColumnIndex("GroupId"));
			mMeasurementData.Data = mCursor.getString(mCursor
					.getColumnIndex("Data"));
			//TODO review
			mMeasurementData.setDateTime(mCursor.getString(mCursor
					.getColumnIndex("DateTime")));
			mMeasurementDataList.add(mMeasurementData);
		}
		return mMeasurementDataList;
	}

	/**
	 * mMeasurementList에 속한 MeasurementData의 리스트를 반환한다. <br>
	 * 조건으로 시간을 받으며 시간이 null일 경우 조건에 추가되지 않는다.
	 * 
	 * @param mMeasurementList
	 * @param DateTimeFrom
	 *            시작 시간
	 * @param DateTimeTo
	 *            종료 시간
	 * @return
	 */
	public List<MeasurementData> obtainMeasurementData(
			List<Measurement> mMeasurementList, String DateTimeFrom,
			String DateTimeTo) {
		String where = "where ";
		ArrayList<String> condition = new ArrayList<String>();
		String MeasurementIdcondition = "";
		ArrayList<MeasurementData> mMeasurementDataList = new ArrayList<MeasurementData>();
		if (mMeasurementList.size() == 0)
			return mMeasurementDataList;

		for (int i = 0; i < mMeasurementList.size(); i++) {
			MeasurementIdcondition += mMeasurementList.get(i).MeasurementId;
			if (i != mMeasurementList.size() - 1) {
				MeasurementIdcondition += ",";
			}
		}

		if (DateTimeFrom != null && !DateTimeFrom.equals(""))
			condition.add("DateTime >= '" + DateTimeFrom + "'");
		if (DateTimeTo != null && !DateTimeTo.equals(""))
			condition.add("DateTime <= '" + DateTimeTo + "'");
		condition.add("MeasurementId in (" + MeasurementIdcondition + ")");

		for (int i = 0; i < condition.size(); i++) {
			where += condition.get(i);
			if (i + 1 < condition.size())
				where += " and ";
		}

		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_MEASUREMENTDATA
				+ where, null);
		MeasurementData mMeasurementData;
		while (mCursor.moveToNext()) {
			mMeasurementData = new MeasurementData();
			mMeasurementData.MeasurementId = mCursor.getInt(mCursor
					.getColumnIndex("MeasurementId"));
			mMeasurementData.GroupId = mCursor.getInt(mCursor
					.getColumnIndex("GroupId"));
			mMeasurementData.Data = mCursor.getString(mCursor
					.getColumnIndex("Data"));
			//TODO review
			mMeasurementData.setDateTime(mCursor.getString(mCursor
					.getColumnIndex("DateTime")));
			mMeasurementDataList.add(mMeasurementData);
		}
		return mMeasurementDataList;
	}

	/**
	 * MeasurementData 테이블의 GroupId 칼럼에서 가장 큰 값을 찾아준다. 만약 없으면 0을 리턴한다.
	 * MeasurementData를 새로 등록할 때 사용되는 값으로 사용자가 호출할 일은 없다.
	 * 
	 * @return
	 */
	private int obtainMeasurementDataGroupId() {
		String sql = SQL_SELECT_GROUPID;
		Cursor mCursor = mSQLiteDatabase.rawQuery(sql, null);
		if (mCursor.moveToNext()) {
			return mCursor.getInt(0);
		}
		return 0;
	}

	/**
	 * BlinkAppInfo와 측정값을 등록할 Object와 비교하여 MeasurementId를 구한 후에 Id가 있으면 각각의 값들을
	 * DB에 등록한다. <br>
	 * 한 객체의 필드들을 동일한 객체(그룹)으로 묶어주기 위해 GroupId를 이용한다. 즉 GroupId가 같으면 동일한 객체에 속한
	 * 데이터이다. <br>
	 * 데이터가 모두 등록된 후에 {@link IBlinkDatabase#URI_OBSERVER_MEASUREMENTDATA}로
	 * {@link ContentResolver#notifyChange(android.net.Uri, android.database.ContentObserver)}
	 * 를 호출한다.
	 * 
	 * @param mBlinkAppInfo
	 * @param obj
	 */
	public void registerMeasurementData(BlinkAppInfo mBlinkAppInfo, Object obj)
			throws IllegalAccessException, IllegalArgumentException {
		MeasurementData mMeasurementData = new MeasurementData();
		ContentValues values = new ContentValues();
		ArrayList<Measurement> mMeasurementList = mBlinkAppInfo.mMeasurementList;
		Field[] mFields = obj.getClass().getFields();
		int GroupId = obtainMeasurementDataGroupId() + 1;
		Log.i(tag, "GroupId : " + GroupId);
		for (int i = 0; i < mFields.length; i++) {
			for (int j = 0; j < mMeasurementList.size(); j++) {
				if (mMeasurementList.get(j).Measurement.contentEquals(ClassUtil
						.obtainFieldSchema(mFields[i]))) {
					mMeasurementData.MeasurementId = mMeasurementList.get(j).MeasurementId;
					mMeasurementData.Data = mFields[i].get(obj).toString();
					// GroupId, MeasurementId, Data 등록
					values.put("GroupId", GroupId);
					values.put("MeasurementId", ""
							+ mMeasurementData.MeasurementId);
					values.put("Data", mMeasurementData.Data);
					mSQLiteDatabase.insert("MeasurementData", null, values);
					Log.i(tag, "registerMeasurementData OK : " + GroupId + "/"
							+ mMeasurementData.MeasurementId + "/"
							+ mMeasurementData.Data);
				}
			}
		}
		CONTEXT.getContentResolver().notifyChange(URI_OBSERVER_MEASUREMENTDATA,
				null);
	}

	/**
	 * obj 클래스를 Measurement에서 obj의 필드와 일치하는 데이터를 검색한 후 클래스와 일치하는 데이터가 있으면 데이터를
	 * 읽어온 후 해당 클래스에 데이터를 대입하여 ArrayList로 돌려준다. <br>
	 * <br>
	 * 해당 매소드를 사용할 때 대입되는쪽에 타입을 ArrayList로 해주어야 한다.<br>
	 * <br>
	 * {@code example : ArrayList<Eye> mEyeList = mSqliteManager.obtainMeasurementData(Eye.class);}
	 * 
	 * @param obj
	 * @return
	 * @throws InstantiationException
	 *             : ins = c.newInstance(); 부분에서 인스턴스화하지 못했을 경우
	 * @throws IllegalAccessException
	 *             : private 타입에 데이터를 대입할때 생기는 오류
	 * @throws ClassNotFoundException
	 *             : Class c = Class.forName(obj.getName());에서 해당 클래스를 얻어오지 못했을
	 *             경우
	 */
	public String obtainMeasurementData(Class<?> obj)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		return obtainMeasurementData(obj, null, null, CONTAIN_DEFAULT);
	}

	/**
	 * obj 클래스를 Measurement에서 obj의 필드와 일치하는 데이터를 검색한 후 클래스와 일치하는 데이터가 있으면 데이터를
	 * 읽어온 후 해당 클래스에 데이터를 대입하여 ArrayList로 돌려준다.<br>
	 * <br>
	 * 해당 매소드를 사용할 때 대입되는쪽에 타입을 ArrayList로 해주어야 한다.<br>
	 * <br>
	 * 시간 조건의 경우 null을 넣으면 조건에 추가되지 않는다. 또한 그 형식은 "yyyy-mm-dd HH:MM:SS" 이어야 한다.<br>
	 * <br>
	 * {@code example : 
	 * ArrayList<Eye> mEyeList = mSqliteManager.obtainMeasurementData(Eye.class,"2014-01-01 00:00:00:00","2014-01-02 00:00:00");}
	 * 
	 * @param obj
	 * @return
	 * @throws InstantiationException
	 *             : ins = c.newInstance(); 부분에서 인스턴스화하지 못했을 경우
	 * @throws IllegalAccessException
	 *             : private 타입에 데이터를 대입할때 생기는 오류
	 * @throws ClassNotFoundException
	 *             : Class c = Class.forName(obj.getName());에서 해당 클래스를 얻어오지 못했을
	 *             경우
	 */
	public String obtainMeasurementData(Class<?> obj, String DateTimeFrom,
			String DateTimeTo) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		return obtainMeasurementData(obj, DateTimeFrom, DateTimeTo,
				CONTAIN_DEFAULT);
	}

	/**
	 * Measurement에서 주어진 클래스의 필드와 일치하는 데이터를 검색한다.<br>
	 * 클래스와 일치하는 데이터가 있으면 데이터를 읽어온 후 해당 클래스에 데이터를 대입하여 ArrayList로 돌려준다.<br>
	 * <br>
	 * 해당 매소드를 사용할 때 대입되는쪽에 타입을 ArrayList로 해주어야 한다. 시간 조건의 경우 null을 넣으면 조건에 추가되지
	 * 않는다.<br>
	 * <br>
	 * 시간 조건의 경우 "yyyy-mm-dd HH:MM:SS" 형식이어야 한다.<br>
	 * <br>
	 * 
	 * <b>example</b>
	 * 
	 * <pre class="prettyprint">
	 * ArrayList&lt;Eye&gt; mEyeList = mSqliteManager.obtainMeasurementData(Eye.class,
	 * 		&quot;2014-01-01 00:00:00:00&quot;, &quot;2014-01-02 00:00:00&quot;, CONTAIN_DEFAULT);
	 * </pre>
	 * 
	 * @param clazz
	 * @param containType
	 *            <li>{@link IBlinkDatabase#CONTAIN_DEFAULT}</li> <li>
	 *            {@link IBlinkDatabase#CONTAIN_PARENT}</li> <li>
	 *            {@link IBlinkDatabase#CONTAIN_FIELD}</li> 중 하나
	 * @return
	 * @throws InstantiationException
	 *             : ins = c.newInstance(); 부분에서 인스턴스화하지 못했을 경우
	 * @throws IllegalAccessException
	 *             : private 타입에 데이터를 대입할때 생기는 오류
	 * @throws ClassNotFoundException
	 *             : {@code Class.forName(obj.getName());} 에서 해당 클래스를 얻어오지 못했을
	 *             경우
	 */
	public String obtainMeasurementData(Class<?> clazz, String DateTimeFrom,
			String DateTimeTo, int containType) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		ArrayList<java.lang.Object> retObject = new ArrayList<java.lang.Object>();
		Field[] mFields = clazz.getFields();
		Field mDateTimeField = null;
		// Measurement 리스트를 얻어온다.
		ArrayList<Measurement> mMeasurementList = new ArrayList<Measurement>();
		ArrayList<Measurement> tempMeasurementList;
		// Field 맵을 만든다.
		HashMap<Integer, Field> mFieldMap = new HashMap<Integer, Field>();
		// 필드와 일치하는 MeasurementId를 구하고 mMeasurementList에 저장해둔다.
		for (int i = 0; i < mFields.length; i++) {
			// 데이터가 저장된 시간인 DateTime을 얻기위한 필드
			if (mFields[i].getName().contentEquals("DateTime"))
				mDateTimeField = mFields[i];
			// 일반 필드면 데이터베이스에서 값을 얻어온다.
			else {
				tempMeasurementList = obtainMeasurementList(mFields[i],
						containType);
				for (int j = 0; j < tempMeasurementList.size(); j++) {
					mFieldMap.put(tempMeasurementList.get(j).MeasurementId,
							mFields[i]);
				}
				mMeasurementList.addAll(tempMeasurementList);
			}
		}

		// mMeasurementList에 저장한 ID로 mMeasurementDataList를 가져온다.
		List<MeasurementData> mMeasurementDataList = obtainMeasurementData(
				mMeasurementList, DateTimeFrom, DateTimeTo);
		MeasurementData mMeasurementData;

		// reflect를 이용하여 obj과 같은 클래스의 인스턴스를 만들어 각각의 필드에 해당 값을 셋팅해준다.
		Class<?> c = Class.forName(clazz.getName());
		java.lang.Object tempObject = null;

		// GroupId에 따라 같은 데이터가 같은 인스턴스에 저장될 수 있도록 맵을 이용한다.
		TreeMap<Integer, java.lang.Object> mObjectMap = new TreeMap<Integer, java.lang.Object>();
		// mMeasurementDataList를 루프를 돌면서 인스턴스에 값을 저장한다.
		// Map에 데이터가 없으면 새로 인스턴스를 생성해서 값을 저장한 후 Map에 푸시한다.

		for (int i = 0; i < mMeasurementDataList.size(); i++) {
			mMeasurementData = mMeasurementDataList.get(i);
			tempObject = mObjectMap.get(mMeasurementData.GroupId);
			if (tempObject == null) {
				tempObject = c.newInstance();
			}
			setClassField(mFieldMap.get(mMeasurementData.MeasurementId),
					mMeasurementData.Data, tempObject);
			// 클래스의 DateTime을 측정값이 등록된 시간으로 한다.
			// TODO review plz
			if (mDateTimeField != null)
				mDateTimeField.set(tempObject,
						mMeasurementData.obtainDateTime());
			mObjectMap.put(mMeasurementData.GroupId, tempObject);
		}

		// mObjectMap에 넣은 데이터를 리턴할 ArrayList에 넣어준다.
		for (Integer key : mObjectMap.keySet()) {
			retObject.add(mObjectMap.get(key));
		}
		return gson.toJson(retObject);
	}

	/**
	 * mField의 타입에 따라서 mData를 타입 캐스팅 한 후 obj의 필드에 해당 데이터를 넣어준다.
	 * obtainMeasurementData()에서 사용되며 사용자가 직접 호출할 일은 없다.
	 * 
	 * @param mField
	 * @param mData
	 * @param obj
	 * @throws NumberFormatException
	 *             : String을 다른 타입으로 parse할 때 오류가 생길 경우
	 * @throws IllegalAccessException
	 *             : private타입에 데이터를 대입할때 생기는 오류
	 * @throws IllegalArgumentException
	 *             : Field의 set 매소드에 obj와 다른 타입의 Argument를 대입하려고 할 경우 생기는 오류(즉
	 *             타입이 다름)
	 */
	private void setClassField(Field mField, String mData, Object obj)
			throws NumberFormatException, IllegalAccessException,
			IllegalArgumentException {
		// 타입명을 먼저 얻는다.
		String type = mField.getType().getName();
		// 타입에 따라서 캐스팅을 한 후 해당 필드에 대입한다.
		if (type.contentEquals("double")) {
			mField.setDouble(obj, Double.parseDouble(mData));
		} else if (type.contentEquals("int")) {
			mField.setInt(obj, Integer.parseInt(mData));
		} else if (type.contentEquals("boolean")) {
			mField.setBoolean(obj, Boolean.parseBoolean(mData));
		} else if (type.contentEquals("byte")) {
			mField.setByte(obj, Byte.parseByte(mData));
		} else if (type.contentEquals("float")) {
			mField.setFloat(obj, Float.parseFloat(mData));
		} else if (type.contentEquals("long")) {
			mField.setLong(obj, Long.parseLong(mData));
		} else if (type.contentEquals("short")) {
			mField.setShort(obj, Short.parseShort(mData));
		} else {
			mField.set(obj, mData);
		}
	}

	/**
	 * class에 해당하는 데이터를 DB에서 삭제한다. 조건으로 시간을 설정할 수 있으며 null이면 조건에 추가되지 않는다.
	 * 
	 * @param obj
	 * @param DateTimeFrom
	 * @param DateTimeTo
	 * @return 삭제한 개수
	 */
	public int removeMeasurementData(Class<?> obj, String DateTimeFrom,
			String DateTimeTo) {
		Field[] mFields = obj.getFields();
		// Measurement 리스트를 얻어온다.
		ArrayList<Measurement> mMeasurementList = new ArrayList<Measurement>();
		// 필드와 일치하는 MeasurementId를 구하고 mMeasurementList에 저장해둔다.
		for (int i = 0; i < mFields.length; i++) {
			mMeasurementList.addAll(obtainMeasurementList(mFields[i],
					CONTAIN_DEFAULT));
		}

		String where = "";
		ArrayList<String> condition = new ArrayList<String>();
		String MeasurementIdcondition = "";
		if (mMeasurementList.size() == 0)
			return 0;

		for (int i = 0; i < mMeasurementList.size(); i++) {
			MeasurementIdcondition += mMeasurementList.get(i).MeasurementId;
			if (i != mMeasurementList.size() - 1) {
				MeasurementIdcondition += ",";
			}
		}

		if (DateTimeFrom != null)
			condition.add("DateTime >= '" + DateTimeFrom + "'");
		if (DateTimeTo != null)
			condition.add("DateTime <= '" + DateTimeTo + "'");
		condition.add("MeasurementId in (" + MeasurementIdcondition + ")");

		for (int i = 0; i < condition.size(); i++) {
			where += condition.get(i);
			if (i + 1 < condition.size())
				where += " and ";
		}
		int ret = mSQLiteDatabase.delete("MeasurementData", where, null);
		CONTEXT.getContentResolver().notifyChange(URI_OBSERVER_BLINKAPP, null);
		return ret;
	}

	/**
	 * 로그를 등록한다. String을 기준으로 등록되며 BlinkAppInfo와는 별개로 저장된다.
	 * 
	 * @param Device
	 * @param App
	 * @param Type
	 * @param Content
	 */
	public void registerLog(String device, String app, int type, String content) {
		if (device == null || app == null)
			return;
		ContentValues values = new ContentValues();
		values.put("Device", device);
		values.put("App", app);
		values.put("Type", type);
		values.put("Content", content);
		mSQLiteDatabase.insert("BlinkLog", null, values);
		Log.i(tag, "Log OK");
	}

	/**
	 * 로그를 얻어온다. String을 기준으로 등록되며 BlinkAppInfo와는 별개로 저장된다.
	 * 
	 * @param Device
	 * @param App
	 * @param Type
	 * @param Content
	 */
	public List<BlinkLog> obtainLog(String Device, String App, int Type,
			String DateTimeFrom, String DateTimeTo) {
		String where = "";
		ArrayList<String> condition = new ArrayList<String>();

		if (Device != null)
			condition.add("Device='" + Device + "'");
		if (App != null)
			condition.add("App='" + App + "'");
		if (Type != -1)
			condition.add("Type=" + Type);
		if (DateTimeFrom != null)
			condition.add("DateTime >= '" + DateTimeFrom + "'");
		if (DateTimeTo != null)
			condition.add("DateTime <= '" + DateTimeTo + "'");

		if (condition.size() > 0)
			where += "where ";
		for (int i = 0; i < condition.size(); i++) {
			where += condition.get(i);
			if (i + 1 < condition.size())
				where += " and ";
		}

		ArrayList<BlinkLog> mBlinkLogList = new ArrayList<BlinkLog>();
		BlinkLog mBlinkLog = null;
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_LOG + where, null);
		while (mCursor.moveToNext()) {
			mBlinkLog = new BlinkLog();
			mBlinkLog.LogId = mCursor.getInt(mCursor.getColumnIndex("LogId"));
			mBlinkLog.Device = mCursor.getString(mCursor
					.getColumnIndex("Device"));
			mBlinkLog.App = mCursor.getString(mCursor.getColumnIndex("App"));
			mBlinkLog.Type = mCursor.getInt(mCursor.getColumnIndex("Type"));
			mBlinkLog.Content = mCursor.getString(mCursor
					.getColumnIndex("Content"));
			mBlinkLog.DateTime = mCursor.getString(mCursor
					.getColumnIndex("DateTime"));
			mBlinkLogList.add(mBlinkLog);
		}
		return mBlinkLogList;
	}

	/**
	 * 로그를 얻어온다. String을 기준으로 등록되며 BlinkAppInfo와는 별개로 저장된다.
	 * 
	 * @param Device
	 * @param App
	 * @param Type
	 * @param Content
	 */
	public List<BlinkLog> obtainLog(String Device, String App,
			String DateTimeFrom, String DateTimeTo) {
		return obtainLog(Device, App, -1, DateTimeFrom, DateTimeTo);
	}

	/**
	 * 로그를 얻어온다. String을 기준으로 등록되며 BlinkAppInfo와는 별개로 저장된다.
	 * 
	 * @param Device
	 * @param App
	 * @param Type
	 * @param Content
	 */
	public List<BlinkLog> obtainLog(String Device, String DateTimeFrom,
			String DateTimeTo) {
		return obtainLog(Device, null, -1, DateTimeFrom, DateTimeTo);
	}

	/**
	 * 로그를 얻어온다. String을 기준으로 등록되며 BlinkAppInfo와는 별개로 저장된다.
	 * 
	 * @param Device
	 * @param App
	 * @param Type
	 * @param Content
	 */
	public List<BlinkLog> obtainLog(String DateTimeFrom, String DateTimeTo) {
		return obtainLog(null, null, -1, DateTimeFrom, DateTimeTo);
	}

	/**
	 * 로그를 얻어온다. String을 기준으로 등록되며 BlinkAppInfo와는 별개로 저장된다.
	 * 
	 * @param Device
	 * @param App
	 * @param Type
	 * @param Content
	 */
	public List<BlinkLog> obtainLog() {
		return obtainLog(null, null, -1, null, null);
	}

}
