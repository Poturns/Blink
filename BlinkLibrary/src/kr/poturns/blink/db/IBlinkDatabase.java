package kr.poturns.blink.db;

import java.io.File;

import kr.poturns.blink.util.FileUtil;
import android.net.Uri;

/**
 * Sqlite Database 파일을 생성, 수정하는 매소드와 관련 변수가 정의되어 있다.
 * 
 * @author Jiwon
 * 
 */
public interface IBlinkDatabase {
	/*
	 * Database 관련 static 변수들
	 */

	/**
	 * obtainMeasurementList나 obtainMeasurementData에서 클래스를 통해 데이터를 얻어올 때 사용되는 타입<br>
	 * schema를 통해 데이터를 얻어올 때 완전히 일치하는 데이터를 가져온다.
	 */
	public final static int CONTAIN_DEFAULT = 0;
	/**
	 * obtainMeasurementList나 obtainMeasurementData에서 클래스를 통해 데이터를 얻어올 때 사용되는 타입<br>
	 * schema를 통해 데이터를 얻어올 때 부모 클래스가 일치하는 데이터를 가져온다.
	 */
	public final static int CONTAIN_PARENT = 1;
	/**
	 * obtainMeasurementList나 obtainMeasurementData에서 클래스를 통해 데이터를 얻어올 때 사용되는 타입<br>
	 * schema를 통해 데이터를 얻어올 때 필드명이 일치하는 데이터를 가져온다.
	 */
	public final static int CONTAIN_FIELD = 2;

	/**
	 * BlinkLog에 저장되는 type으로 어떤 행동을 했는지 구분하는 값이다.
	 */
	public final static int LOG_REGISTER_BLINKAPP = 1;
	public final static int LOG_OBTAIN_BLINKAPP = 2;
	public final static int LOG_REGISTER_Measurement = 3;
	public final static int LOG_OBTAIN_Measurement = 4;
	public final static int LOG_REGISTER_Function = 5;
	public final static int LOG_OBTAIN_Function = 6;
	public final static int LOG_REGISTER_MEASRUEMENT = 7;
	public final static int LOG_OBTAIN_MEASUREMENT = 8;

	/**
	 * 데이터베이스가 변화했을 때 호출되는 Observer의 Uri BlinkApp이 추가됐을 때 해당 Uri로 호출된다. 옵저버를
	 * 등록해야 사용할 수 있다. <br>
	 * example :
	 * {@code getContentResolver().registerContentObserver(SqliteManager.URI_OBSERVER_BLINKAPP, false, mContentObserver);}
	 */
	public final static Uri URI_OBSERVER_BLINKAPP = Uri
			.parse("blink://kr.poturns.blink/database/blinkappinfo");
	/**
	 * 데이터베이스가 변화했을 때 호출되는 Observer의 Uri MeasurementData가 추가됐을 때 해당 Uri로 호출된다.
	 * 옵저버를 등록해야 사용할 수 있다. <br>
	 * example :
	 * {@code getContentResolver().registerContentObserver(SqliteManager.URI_OBSERVER_MEASUREMENTDATA, false, mContentObserver);}
	 */
	public final static Uri URI_OBSERVER_MEASUREMENTDATA = Uri
			.parse("blink://kr.poturns.blink/database/measurementdata");
	/**
	 * 데이터베이스가 변화했을 때 호출되는 Observer의 Uri BlinkAppInfo가 Sync됐을 때 해당 Uri로 호출된다.
	 * 옵저버를 등록해야 사용할 수 있다. <br>
	 * example :
	 * {@code getContentResolver().registerContentObserver(SqliteManager.URI_OBSERVER_SYNC, false, mContentObserver);}
	 */
	public final static Uri URI_OBSERVER_SYNC = Uri
			.parse("blink://kr.poturns.blink/database/blinkappinfo/sync");

	/**
	 * Sqlite 데이터베이스 위치
	 */
	public static final String EXTERNAL_DB_FILE_PATH = FileUtil.EXTERNAL_ARCHIVE_DIRECTORY_PATH
			+ File.separatorChar;
	/**
	 * Sqlite 데이터베이스 파일명
	 */
	public static final String EXTERNAL_DB_FILE_NAME = "BlinkDatabase.db";

}
