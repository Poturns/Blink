package kr.poturns.blink.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BlinkDatabase {
	private final static String tag = "SystemDatabase";

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
				+ "'Measurement' TEXT NOT NULL," + "'Type' TEXT NOT NULL,"
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
				+ "'MeasurementDataId' INTEGER NOT NULL,"
				+ "'GroupId' INTEGER,"
				+ "'Data' TEXT NOT NULL,"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime')),"
				+ "PRIMARY KEY ('MeasurementId','MeasurementDataId'),"
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
		
		sql = "create table 'Synchronize' ("
				+ "'DeviceId' INTEGER PRIMARY KEY ,"
				+ "'Sequence' INTEGER NOT NULL,"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime'))"
				+ ");";
		db.execSQL(sql);

		Log.i(tag, "logDatabase ok");
	}

	public static void updateBlinkDatabase(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXSITS Device");
		db.execSQL("DROP TABLE IF EXSITS App");
		db.execSQL("DROP TABLE IF EXSITS Measurement");
		db.execSQL("DROP TABLE IF EXSITS Function");
		db.execSQL("DROP TABLE IF EXSITS Data");
		db.execSQL("DROP TABLE IF EXSITS MeasurementData");
		db.execSQL("DROP TABLE IF EXSITS Log");
		// 새로 생성될 수 있도록 onCreate() 메소드를 생성한다.
		createBlinkDatabase(db);
	}
}
