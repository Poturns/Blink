package kr.poturns.blink.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BlinkDatabase {
	private final static String tag = "SystemDatabase";

	public static void createBlinkDatabase(SQLiteDatabase db) {
		// DB에 테이블 생성하기
		String sql = "";

		// Create DeviceAppList table sql statement
		sql = "create table 'DeviceAppList' ("
				+ "'DeviceAppId' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "'Device' TEXT NOT NULL," + "'App' TEXT NOT NULL,"
				+ "'Description' TEXT NOT NULL,"
				+ "'UUID' TEXT,"
				+ "'MacAddress' TEXT,"
				+ "'Version' INTEGER NOT NULL,"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime')),"
				+ "UNIQUE ('Device','App','Version')"
				+ "); ";
		db.execSQL(sql);

		// Create DeviceAppMeasurement table sql statement
		sql = "create table 'DeviceAppMeasurement' ("
				+ "'DeviceAppId' INTEGER NOT NULL,"
				+ "'MeasurementId' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "'Measurement' TEXT NOT NULL," + "'Type' TEXT NOT NULL,"
				+ "'Description' TEXT NOT NULL,"
				+ "UNIQUE ('DeviceAppId','Measurement'),"
				+ "FOREIGN KEY('DeviceAppId') REFERENCES DeviceAppList('DeviceAppId')"
				+ ");";
		// sql문 실행하기
		db.execSQL(sql);

		// Create DeviceAppFunction table sql statement
		sql = "create table 'DeviceAppFunction' ("
				+ "'DeviceAppId' INTEGER NOT NULL,"
				+ "'Function' TEXT NOT NULL," + "'Description' TEXT,"
				+ "PRIMARY KEY ('DeviceAppId','Function'),"
				+ "FOREIGN KEY('DeviceAppId') REFERENCES DeviceAppList('DeviceAppId')"
				+ ");";
		db.execSQL(sql);

		Log.i(tag, "createSystemDatabase ok");
		
		sql = "create table 'MeasurementData' ("
				+ "'MeasurementId' INTEGER NOT NULL,"
				+ "'GroupId' INTEGER,"
				+ "'Data' TEXT NOT NULL,"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime')),"
				+ "FOREIGN KEY('MeasurementId') REFERENCES DeviceAppMeasurement('MeasurementId')"
				+ ");";
		db.execSQL(sql);

		Log.i(tag, "createMeasurementDatabase ok");
		
		sql = "create table 'DeviceAppLog' ("
				+ "'DeviceAppId' INTEGER NOT NULL,"
				+ "'Content' TEXT NOT NULL,"
				+ "'DateTime' DATETIME DEFAULT (datetime('now','localtime')),"
				+ "FOREIGN KEY('DeviceAppId') REFERENCES DeviceAppList('DeviceAppId')"
				+ ");";
		db.execSQL(sql);

		Log.i(tag, "logDatabase ok");
	}

	public static void updateBlinkDatabase(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXSITS DeviceAppList");
		db.execSQL("DROP TABLE IF EXSITS DeviceAppMeasurement");
		db.execSQL("DROP TABLE IF EXSITS DeviceAppFunction");
		db.execSQL("DROP TABLE IF EXSITS MeasurementData");
		// 새로 생성될 수 있도록 onCreate() 메소드를 생성한다.
		createBlinkDatabase(db);
	}
}
