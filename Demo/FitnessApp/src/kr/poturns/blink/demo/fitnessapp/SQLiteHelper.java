package kr.poturns.blink.demo.fitnessapp;

import java.util.ArrayList;

import kr.poturns.blink.schema.HeartBeat;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Blink database가 아닌 내부의 DB를 관리하는 Helper class
 * 
 * @author Myungjin.Kim
 */
public class SQLiteHelper extends SQLiteOpenHelper {
	private static final String TAG = SQLiteHelper.class.getSimpleName();
	public static final String TABLE_PUSH_UP = "PUSHUP";
	public static final String TABLE_SQUAT = "SQUAT";
	public static final String TABLE_SIT_UP = "SITUP";
	public static final String TABLE_HEART_BEAT = "HEARTBEAT";
	public static final String COLUMN_COUNT = "COUNT";
	public static final String COLUMN_HEARTBEAT_BPM = "BPM";
	private SQLiteDatabase mDatabase;
	private volatile static SQLiteHelper sInstance;
	private static final Object mLock = new Object();

	public static final SQLiteHelper getInstance(Context context) {
		synchronized (mLock) {
			if (sInstance == null)
				sInstance = new SQLiteHelper(context);
		}
		return sInstance;
	}

	private SQLiteHelper(Context context) {
		super(context, "fitness", null, 1);
		this.mDatabase = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sb = new StringBuilder("CREATE TABLE '");
		sb.append(TABLE_PUSH_UP).append("' ( '");
		sb.append(COLUMN_COUNT).append("' INTEGER , ");
		sb.append("'DateTime' DATETIME DEFAULT (datetime('now','localtime')) , ");
		sb.append("UNIQUE('DateTime')");
		sb.append(");");
		db.execSQL(sb.toString());

		sb = new StringBuilder("CREATE TABLE '");
		sb.append(TABLE_SIT_UP).append("' ( '");
		sb.append(COLUMN_COUNT).append("' INTEGER , ");
		sb.append("'DateTime' DATETIME DEFAULT (datetime('now','localtime')) , ");
		sb.append("UNIQUE('DateTime')");
		sb.append(");");
		db.execSQL(sb.toString());

		sb = new StringBuilder("CREATE TABLE '");
		sb.append(TABLE_SQUAT).append("' ( '");
		sb.append(COLUMN_COUNT).append("' INTEGER , ");
		sb.append("'DateTime' DATETIME DEFAULT (datetime('now','localtime')) , ");
		sb.append("UNIQUE('DateTime')");
		sb.append(");");
		db.execSQL(sb.toString());

		sb = new StringBuilder("CREATE TABLE '");
		sb.append(TABLE_HEART_BEAT).append("' ( '");
		sb.append(COLUMN_HEARTBEAT_BPM).append("' INTEGER , ");
		sb.append("'DateTime' DATETIME DEFAULT (datetime('now','localtime')) , ");
		sb.append("UNIQUE('DateTime')");
		sb.append(");");
		db.execSQL(sb.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS '" + TABLE_HEART_BEAT + '\'');
		db.execSQL("DROP TABLE IF EXISTS '" + TABLE_PUSH_UP + '\'');
		db.execSQL("DROP TABLE IF EXISTS '" + TABLE_SIT_UP + '\'');
		db.execSQL("DROP TABLE IF EXISTS '" + TABLE_SQUAT + '\'');
		onCreate(db);
	}

	private boolean checkIntegrity(String table, int i) {
		if (i < 1) {
			Log.e(TAG, table + ": checkIntegrity failed - value : " + i);
			return false;
		}
		if (!mDatabase.isOpen()) {
			Log.e(TAG, "Database already closed!");
			return false;
		}
		return true;
	}

	/** 심장 박동수를 제외한 다른 데이터를 입력한다. */
	public void insert(String table, int count) {
		if (!checkIntegrity(table, count))
			return;
		ContentValues values = new ContentValues();
		values.put(COLUMN_COUNT, count);
		try {
			if (mDatabase.insertOrThrow(table, null, values) == -1)
				mDatabase.update(table, values, null, null);
		} catch (Exception e) {
			mDatabase.update(table, values, null, null);
		}
	}

	/** 심장 박동수를 입력한다. */
	public void insert(int bpm) {
		if (!checkIntegrity(TABLE_HEART_BEAT, bpm))
			return;
		ContentValues values = new ContentValues();
		values.put(COLUMN_HEARTBEAT_BPM, bpm);
		try {
			if (mDatabase.insertOrThrow(TABLE_HEART_BEAT, null, values) == -1)
				mDatabase.update(TABLE_HEART_BEAT, values, null, null);
		} catch (Exception e) {
			mDatabase.update(TABLE_HEART_BEAT, values, null, null);
		}
	}

	/**
	 * 지정하는 날짜에 해당하는 데이터들의 합을 얻는다.
	 * 
	 * @param table
	 * @param dateTime
	 *            yyyy-mm-dd
	 */
	public int select(String table, String dateTime) {
		Cursor cursor = mDatabase.rawQuery("SELECT SUM(" + COLUMN_COUNT
				+ ") AS " + COLUMN_COUNT + " FROM " + table
				+ " WHERE DateTime >= '" + dateTime + " 00:00:00'" + " AND "
				+ "DateTime <= '" + dateTime + " 23:59:59'", null);
		if (cursor.moveToNext())
			return cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT));
		else
			return 0;
	}

	public static final int getDayOfMonth(int month) {
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			return 31;
		case 2:
			return 28;
		default:
			return 30;
		}
	}

	/** 해당 연도와 월에 해당하는 데이터의 최고값을 얻어온다. */
	public int selectMax(String table, String year, int month) {
		int monthSize = getDayOfMonth(month);
		Cursor cursor = mDatabase.rawQuery("SELECT MAX(" + COLUMN_COUNT
				+ ") AS " + COLUMN_COUNT + " FROM " + table
				+ " WHERE DateTime >= '" + year + "-"
				+ (month > 9 ? month : "0" + month) + "-" + "01" + " 00:00:00'"
				+ " AND " + "DateTime <= '" + year + "-"
				+ (month > 9 ? month : "0" + month) + "-" + monthSize
				+ " 23:59:59'", null);
		if (cursor.moveToNext())
			return cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT));
		else
			return 0;
	}

	/** 해당 연도와 월에 해당하는 데이터들을 얻어온다. */
	public ArrayList<Integer> select(String table, String year, int month) {
		int monthSize = getDayOfMonth(month);
		ArrayList<Integer> list = new ArrayList<Integer>(monthSize);
		for (int i = 1; i <= monthSize; i++) {
			list.add(select(table, year, month, i));
		}
		return list;
	}

	/** 해당 연도와 월, 날짜에 해당하는 데이터를 얻어온다. */
	public int select(String table, String year, int month, int day) {
		return select(table, year + "-" + (month > 9 ? month : "0" + month)
				+ "-" + (day > 9 ? day : ("0" + day)));
	}

	/**
	 * @param dateTime
	 *            yyyy-mm-dd hh:MM
	 */
	public ArrayList<HeartBeat> selectHeartBeat(String dateTimeFrom,
			String dateTimeEnd) {
		Cursor cursor = mDatabase.rawQuery("SELECT * " + " FROM "
				+ TABLE_HEART_BEAT + " WHERE DateTime >= '" + dateTimeFrom
				+ ":00'" + " AND " + "DateTime <= '" + dateTimeEnd + ":59'",
				null);
		ArrayList<HeartBeat> list = new ArrayList<HeartBeat>();
		HeartBeat hb;
		int indexOfBpm = cursor.getColumnIndex(COLUMN_HEARTBEAT_BPM);
		int indexOfDate = cursor.getColumnIndex("DateTime");
		while (cursor.moveToNext()) {
			hb = new HeartBeat();
			hb.bpm = cursor.getInt(indexOfBpm);
			hb.DateTime = cursor.getString(indexOfDate);
			list.add(hb);
		}
		return list;
	}

	public void dropAllTable() {
		onUpgrade(mDatabase, 1, 1);
	}

	public static final void closeDB() {
		synchronized (mLock) {
			if (sInstance != null) {
				sInstance.mDatabase.close();
				sInstance = null;
			}
		}
	}
}
