package kr.poturns.blink.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Sqlite Database 파일을 생성, 수정하는 매소드가 정의되어 있다.
 * @author Jiwon
 *
 */
public class BlinkDatabase {
	private final static String tag = "SystemDatabase";

	/**
	 * SQLiteDatabase에 테이블을 생성한다.
	 * @param db
	 */
	public static void createBlinkDatabase(SQLiteDatabase db) {
		// DB에 테이블 생성하기
		String sql = "";

		// Create DeviceAppList table sql statement
		sql = "create table 'Device' ("
				+ "'DeviceId' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "'Device' TEXT NOT NULL,"
				+ "'UUID' TEXT,"
				+ "'MacAddress' TEXT,"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime')),"
				+ "UNIQUE ('MacAddress')"
				+ "); ";
		db.execSQL(sql);

		sql = "create table 'App' ("
				+ "'AppId' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "'DeviceId' INTEGER NOT NULL,"
				+ "'PackageName' TEXT NOT NULL,"
				+ "'AppName' TEXT NOT NULL,"
				+ "'AppIcon' BLOB,"
				+ "'Version' INTEGER NOT NULL DEFAULT (1),"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime')),"
				+ "UNIQUE ('DeviceId','PackageName'),"
				+ "FOREIGN KEY('DeviceId') REFERENCES Device('DeviceId')"
				+ "); ";
		db.execSQL(sql);
		
		// Create DeviceAppMeasurement table sql statement
		sql = "create table 'Measurement' ("
				+ "'AppId' INTEGER NOT NULL,"
				+ "'MeasurementId' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "'MeasurementName' TEXT NOT NULL,"
				+ "'Measurement' TEXT NOT NULL,"
				+ "'Type' TEXT NOT NULL,"
				+ "'Description' TEXT NOT NULL,"
				+ "UNIQUE ('AppId','Measurement'),"
				+ "FOREIGN KEY('AppId') REFERENCES App('AppId')"
				+ ");";
		// sql문 실행하기
		db.execSQL(sql);

		// Create DeviceAppFunction table sql statement
		sql = "create table 'Function' ("
				+ "'AppId' INTEGER NOT NULL,"
				+ "'Function' TEXT NOT NULL,"
				+ "'Description' TEXT,"
				+ "'Action' TEXT NOT NULL,"
				+ "'Type' INTEGER NOT NULL DEFAULT (1),"
				+ "PRIMARY KEY ('AppId','Action','Type'),"
				+ "FOREIGN KEY('AppId') REFERENCES App('AppId')"
				+ ");";
		db.execSQL(sql);

		Log.i(tag, "createSystemDatabase ok");
		
		sql = "create table 'MeasurementData' ("
				+ "'MeasurementId' INTEGER NOT NULL,"
				+ "'MeasurementDataId' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "'GroupId' INTEGER,"
				+ "'Data' TEXT NOT NULL,"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime')),"
				+ "UNIQUE ('MeasurementId','MeasurementDataId'),"
				+ "FOREIGN KEY('MeasurementId') REFERENCES Measurement('MeasurementId')"
				+ ");";
		db.execSQL(sql);

		Log.i(tag, "createMeasurementDatabase ok");
		
		sql = "create table 'BlinkLog' ("
				+ "'LogId' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "'Device' TEXT NOT NULL,"
				+ "'App' TEXT NOT NULL,"
				+ "'Type' INTEGER NOT NULL,"
				+ "'Content' TEXT NOT NULL,"
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
	 * @param db
	 */
	public static void updateBlinkDatabase(SQLiteDatabase db) {
		// TODO Auto-generated method stub
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
}
