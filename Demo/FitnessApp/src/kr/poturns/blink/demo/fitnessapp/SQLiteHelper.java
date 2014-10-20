package kr.poturns.blink.demo.fitnessapp;

import java.util.ArrayList;

import kr.poturns.blink.schema.HeartBeat;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Blink database가 아닌 내부의 DB를 관리하는 Helper class
 * 
 * @author Myungjin.Kim
 */
public class SQLiteHelper extends SQLiteOpenHelper {
	private static final String TAG = SQLiteHelper.class.getSimpleName();
	/** 팔굽혀펴기 */
	public static final String TABLE_PUSH_UP = "PUSHUP";
	/** 스쿼트 */
	public static final String TABLE_SQUAT = "SQUAT";
	/** 윗몸일으키기 */
	public static final String TABLE_SIT_UP = "SITUP";
	/** 심장박동 */
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

	/* 입력받은 값의 유효성을 검사한다. */
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

	/**
	 * 심장 박동수를 제외한 다른 데이터를 입력한다.
	 * 
	 * @param table
	 *            <li>{@link SQLiteHelper#TABLE_PUSH_UP}</li> <li>
	 *            {@link SQLiteHelper#TABLE_SIT_UP}</li><li>
	 *            {@link SQLiteHelper#TABLE_SQUAT}</li> 중 하나
	 * @param count
	 *            입력할 값
	 * @return 입력에 성공했으면 {@code true}, 실패하였으면 {@code false}
	 */
	public boolean insert(String table, int count) {
		if (!checkIntegrity(table, count))
			return false;
		ContentValues values = new ContentValues();
		values.put(COLUMN_COUNT, count);
		try {
			if (mDatabase.insertOrThrow(table, null, values) == -1) {
				return mDatabase.update(table, values, null, null) > 0;
			}
			return true;
		} catch (Exception e) {
			return mDatabase.update(table, values, null, null) > 0;
		}
	}

	/** 심장 박동수를 입력한다. */
	public boolean insertHeartBeat(int bpm) {
		if (!checkIntegrity(TABLE_HEART_BEAT, bpm))
			return false;
		ContentValues values = new ContentValues();
		values.put(COLUMN_HEARTBEAT_BPM, bpm);
		try {
			if (mDatabase.insertOrThrow(TABLE_HEART_BEAT, null, values) == -1)
				return mDatabase.update(TABLE_HEART_BEAT, values, null, null) > 0;
			return true;
		} catch (Exception e) {
			return mDatabase.update(TABLE_HEART_BEAT, values, null, null) > 0;
		}
	}

	/**
	 * 지정하는 날짜에 해당하는 데이터들의 합을 얻는다.
	 * 
	 * @param table
	 *            <li>{@link SQLiteHelper#TABLE_PUSH_UP}</li> <li>
	 *            {@link SQLiteHelper#TABLE_SIT_UP}</li><li>
	 *            {@link SQLiteHelper#TABLE_SQUAT}</li> 중 하나
	 * @param dateTime
	 *            yyyy-mm-dd
	 * @return 해당하는 날짜의 총 운동 횟수, 자료가 없다면 {@code 0}
	 */
	public int selectSum(String table, String dateTime) {
		StringBuilder sb = new StringBuilder("SELECT SUM(")
				.append(COLUMN_COUNT).append(") AS ").append(COLUMN_COUNT);
		sb.append(" FROM ").append(table);

		sb.append(" WHERE DateTime >= '").append(dateTime).append(" 00:00:00'");
		sb.append(" AND ");
		sb.append("DateTime <= '").append(dateTime).append(" 23:59:59'");

		Cursor cursor = mDatabase.rawQuery(sb.toString(), null);

		if (cursor.moveToNext())
			return cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT));
		else
			return 0;
	}

	// FIXME Calander를 활용하는 방식으로 수정되어야함
	/**
	 * 해당 월의 날짜 수를 반환한다.
	 * 
	 * @param month
	 *            날짜 수를 얻기 원하는 달
	 * @return 해당 월의 날짜 수
	 */
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
		String monthString = (month > 9 ? Integer.toString(month) : "0" + month);
		StringBuilder sb = new StringBuilder("SELECT MAX(");
		sb.append(COLUMN_COUNT).append(") AS ").append(COLUMN_COUNT);
		sb.append(" FROM ").append(table);
		sb.append(" WHERE DateTime >= '").append(year).append("-")
				.append(monthString).append("-").append("01")
				.append(" 00:00:00'");
		sb.append(" AND ");
		sb.append("DateTime <= '").append(year).append("-").append(monthString)
				.append("-").append(monthSize).append(" 23:59:59'");

		Cursor cursor = mDatabase.rawQuery(sb.toString(), null);
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
		return selectSum(table, year + "-" + (month > 9 ? month : "0" + month)
				+ "-" + (day > 9 ? day : ("0" + day)));
	}

	/**
	 * DB에 저장된 심박수를 가져온다.
	 * 
	 * @param dateTimeFrom
	 *            조회 시작 시각, yyyy-mm-dd hh:MM 형식
	 * @param dateTimeEnd
	 *            조회 종료 시각, yyyy-mm-dd hh:MM 형식
	 * 
	 * @throws SQLiteException
	 *             dateTime 형식이 잘못되었을 때 발생
	 */
	public ArrayList<HeartBeat> selectHeartBeat(String dateTimeFrom,
			String dateTimeEnd) {
		StringBuilder sb = new StringBuilder("SELECT * FROM ")
				.append(TABLE_HEART_BEAT);
		sb.append(" WHERE DateTime >= '").append(dateTimeFrom).append(":00'");
		sb.append(" AND ");
		sb.append("DateTime <= '").append(dateTimeEnd).append(":59'");

		Cursor cursor = mDatabase.rawQuery(sb.toString(), null);

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

	/** 모든 테이블의 내용을 삭제한다. */
	public void dropAllTable() {
		onUpgrade(mDatabase, 1, 1);
	}

	/** 열려있는 DB를 닫고, 기타 자원을 해제한다 */
	public static final void closeDB() {
		synchronized (mLock) {
			if (sInstance != null) {
				sInstance.mDatabase.close();
				sInstance = null;
			}
		}
	}
}
