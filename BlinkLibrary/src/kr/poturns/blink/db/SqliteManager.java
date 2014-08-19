package kr.poturns.blink.db;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.util.ClassUtil;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/*
 * DB 생성 및 업그레이드를 도와주는 도우미 클래스 만들기
 *  - SQLiteOpenHelper 추상 클래스를 상속받아서 만든다. - 
 */
public class SqliteManager extends SQLiteOpenHelper {
	private final String tag = "SqliteManager";
	
	public final static int CONTAIN_DEFAULT = 0;
	public final static int CONTAIN_PARENT = 1;
	public final static int CONTAIN_FIELD = 2;
	
	public final static int LOG_REGISTER_SYSTEMDATABASE = 1;
	public final static int LOG_OBTAIN_SYSTEMDATABASE = 2;
	public final static int LOG_REGISTER_Measurement = 3;
	public final static int LOG_OBTAIN_Measurement = 4;
	public final static int LOG_REGISTER_Function = 5;
	public final static int LOG_OBTAIN_Function = 6;
	public final static int LOG_REGISTER_MEASRUEMENT = 7;
	public final static int LOG_OBTAIN_MEASUREMENT = 8;
	
	private final String SQL_SELECT_DEVICE = "SELECT * FROM Device ";
	private final String SQL_SELECT_APP = "SELECT * FROM App ";
	private final String SQL_SELECT_FUNCTION = "SELECT * FROM Function ";
	private final String SQL_SELECT_MEASUREMENT = "SELECT * FROM Measurement ";
	private final String SQL_SELECT_MEASUREMENTDATA =  "SELECT * FROM MeasurementData ";
	private final String SQL_SELECT_GROUPID =  "SELECT max(GroupId) FROM MeasurementData ";
	private final String SQL_DELETE_MEASUREMENTDATA = "delete from MeasurementData ";
	private final String SQL_SELECT_LOG =  "SELECT * FROM BlinkLog ";
	public static final String EXTERNAL_DB_FILE_PATH = Environment.getExternalStorageDirectory() + "/Blink/archive/";
	public static final String EXTERNAL_DB_FILE_NAME = "BlinkDatabase.db";
	
	SQLiteDatabase mSQLiteDatabase;
	Gson gson;
	
	
	private SqliteManager(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		mSQLiteDatabase = this.getWritableDatabase();
	}
	
	public SqliteManager(Context context){
		super(context, EXTERNAL_DB_FILE_PATH+EXTERNAL_DB_FILE_NAME, null, 1);
		mSQLiteDatabase = this.getWritableDatabase();
		gson = new GsonBuilder().setPrettyPrinting().create();
	}

	public static SqliteManager getSqliteManager(Context context){
		ensureDatabaseDir();
		return new SqliteManager(context);
	}
	private static void ensureDatabaseDir() {
		File mPreferenceDir = new File(Environment.getExternalStorageDirectory() + "/Blink");
		mPreferenceDir.mkdir();
		mPreferenceDir = new File(Environment.getExternalStorageDirectory() + "/Blink/archive/");
		mPreferenceDir.mkdir();
	}
	/**
	 * SqliteManager(this, "BlinkDatabase.db", null, 1); 호출시  BlinkDatabase.db가 없으면 호출된다.
	 * DB가 생성될 때 호출되는 메소드
	 * Sqlite 초기 설정시 자동으로호출된다.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		BlinkDatabase.createBlinkDatabase(db);
	}

	/**
	 * SqliteManager(this, "BlinkDatabase.db", null, 1); 호출시  버전이 다르면 호출된다.
	 * DB가 생성될 때 호출되는 메소드
	 * Sqlite 초기 설정시 자동으로호출된다.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		BlinkDatabase.updateBlinkDatabase(db);
	}

	//-------------------------------SystemDatabase---------------------------------------
	public void registerSystemDatabase(SystemDatabaseObject mSystemDatabaseObject){
		registerDevice(mSystemDatabaseObject);
		obtainDeviceList(mSystemDatabaseObject);
		registerApp(mSystemDatabaseObject);
		obtainAppList(mSystemDatabaseObject);
		registerFunction(mSystemDatabaseObject);
		registerMeasurement(mSystemDatabaseObject);
		Log.i(tag, "registerSystemDatabase OK");
	}
	public SystemDatabaseObject obtainSystemDatabase(String device,String app){
		SystemDatabaseObject mSystemDatabaseObject = new SystemDatabaseObject();
		Device mDevice = mSystemDatabaseObject.mDevice;
		App mApp = mSystemDatabaseObject.mApp;
		mDevice.Device = device;
		mApp.PackageName = app;
		//기존에 등록된 값이 없으면
		if(obtainDeviceList(mSystemDatabaseObject) && obtainAppList(mSystemDatabaseObject)){
			mSystemDatabaseObject.isExist = true;
			obtainFunction(mSystemDatabaseObject);
			obtainMeasurement(mSystemDatabaseObject);
		}
		//기존에 등록된 값이 있으면 해당 값을 찾아서 리턴
		else {
			mApp.Version = 1;
			mApp.PackageName = app;
			mApp.DeviceId = mDevice.DeviceId;
			mApp.AppName = app;
			mSystemDatabaseObject.isExist = false;
			return mSystemDatabaseObject;
		}
		return mSystemDatabaseObject;
	}
	
	public ArrayList<SystemDatabaseObject> obtainSystemDatabase(){
		ArrayList<SystemDatabaseObject> mSystemDatabaseObjectList = new ArrayList<SystemDatabaseObject>();
		ArrayList<Device> mDeviceList = obtainDeviceList("");
		ArrayList<App> mAppList = obtainAppList("");
		SystemDatabaseObject mServiceDatabaseObject = null;
		for(int i=0;i<mDeviceList.size();i++){
			for(int j=0;j<mAppList.size();j++){
				if(mDeviceList.get(i).DeviceId==mAppList.get(j).DeviceId){
					mServiceDatabaseObject = new SystemDatabaseObject();
					mServiceDatabaseObject.mDevice = mDeviceList.get(i);
					mServiceDatabaseObject.mApp = mAppList.get(j);
					obtainFunction(mServiceDatabaseObject);
					obtainMeasurement(mServiceDatabaseObject);
					mServiceDatabaseObject.isExist = true;
					mSystemDatabaseObjectList.add(mServiceDatabaseObject);
				}
			}
		}
		return mSystemDatabaseObjectList;
	}
	
	private boolean obtainDeviceList(SystemDatabaseObject mSystemDatabaseObject){
		Device mDevice = mSystemDatabaseObject.mDevice;
		String query = SQL_SELECT_DEVICE+"where Device=?";
		String[] args = {mDevice.Device};
		Cursor mCursor = mSQLiteDatabase.rawQuery(query, args);
		if(mCursor.moveToNext()){
			mDevice.DeviceId = mCursor.getInt(mCursor.getColumnIndex("DeviceId"));
			mDevice.Device = mCursor.getString(mCursor.getColumnIndex("Device"));
			mDevice.UUID = mCursor.getString(mCursor.getColumnIndex("UUID"));
			mDevice.MacAddress = mCursor.getString(mCursor.getColumnIndex("MacAddress"));
			mDevice.DateTime = mCursor.getString(mCursor.getColumnIndex("DateTime"));
			return true;
		}
		return false;
	}
	
	public ArrayList<Device> obtainDeviceList(String where){
		if(where==null||where.equals(""))where="";
		else where = "where " + where;
		ArrayList<Device> mDeviceList = new ArrayList<Device>();
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_DEVICE+where, null);
		Device mDevice = null;
		while(mCursor.moveToNext()){
			mDevice = new Device();
			mDevice.DeviceId = mCursor.getInt(mCursor.getColumnIndex("DeviceId"));
			mDevice.Device = mCursor.getString(mCursor.getColumnIndex("Device"));
			mDevice.UUID = mCursor.getString(mCursor.getColumnIndex("UUID"));
			mDevice.MacAddress = mCursor.getString(mCursor.getColumnIndex("MacAddress"));
			mDevice.DateTime = mCursor.getString(mCursor.getColumnIndex("DateTime"));
			mDeviceList.add(mDevice);
		}
		return mDeviceList;
	}
	
	private boolean obtainAppList(SystemDatabaseObject mSystemDatabaseObject){
		App mApp = mSystemDatabaseObject.mApp;
		String query = SQL_SELECT_APP+"where DeviceId=? and PackageName=?";
		String[] args = {String.valueOf(mSystemDatabaseObject.mDevice.DeviceId),mSystemDatabaseObject.mApp.PackageName};
		Cursor mCursor = mSQLiteDatabase.rawQuery(query, args);
		if(mCursor.moveToNext()){
			mApp.AppId = mCursor.getInt(mCursor.getColumnIndex("AppId"));
			mApp.DeviceId = mCursor.getInt(mCursor.getColumnIndex("DeviceId"));
			mApp.PackageName = mCursor.getString(mCursor.getColumnIndex("PackageName"));
			mApp.AppName = mCursor.getString(mCursor.getColumnIndex("AppName"));
			mApp.Version = mCursor.getInt(mCursor.getColumnIndex("Version"));
			mApp.DateTime = mCursor.getString(mCursor.getColumnIndex("DateTime"));
			return true;
		}
		return false;
	}
	
	public ArrayList<App> obtainAppList(String where){
		if(where==null||where.equals(""))where="";
		else where = "where " + where;
		ArrayList<App> mAppList = new ArrayList<App>();
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_APP+where, null);
		App mApp = null;
		while(mCursor.moveToNext()){
			mApp = new App();
			mApp.AppId = mCursor.getInt(mCursor.getColumnIndex("AppId"));
			mApp.DeviceId = mCursor.getInt(mCursor.getColumnIndex("DeviceId"));
			mApp.PackageName = mCursor.getString(mCursor.getColumnIndex("PackageName"));
			mApp.AppName = mCursor.getString(mCursor.getColumnIndex("AppName"));
			mApp.Version = mCursor.getInt(mCursor.getColumnIndex("Version"));
			mApp.DateTime = mCursor.getString(mCursor.getColumnIndex("DateTime"));
			mAppList.add(mApp);
		}
		return mAppList;
	}
	
	
	
	private void registerDevice(SystemDatabaseObject mSystemDatabaseObject){
		Device mDevice = mSystemDatabaseObject.mDevice;
		ContentValues values = new ContentValues();
		values.put("Device", mDevice.Device);
		values.put("UUID", mDevice.UUID);
		values.put("MacAddress", mDevice.MacAddress);
        mSQLiteDatabase.insert("Device", null, values);
	}
	
	private void registerApp(SystemDatabaseObject mSystemDatabaseObject){
		Device mDevice = mSystemDatabaseObject.mDevice;
		App mApp = mSystemDatabaseObject.mApp;
		ContentValues values = new ContentValues();
		values.put("DeviceId", mDevice.DeviceId);
		values.put("PackageName", mApp.PackageName);
		values.put("AppName", mApp.AppName);
		values.put("Version", mApp.Version);
        mSQLiteDatabase.insert("App", null, values);
	}
	
	private void registerFunction(SystemDatabaseObject mSystemDatabaseObject){
		App mApp = mSystemDatabaseObject.mApp;
		ArrayList<Function> mFunctionList = mSystemDatabaseObject.mFunctionList;
		Function mFunction;
		for(int i=0;i<mFunctionList.size();i++){
			mFunction = mFunctionList.get(i);
			mFunction.AppId = mApp.AppId;
			ContentValues values = new ContentValues();
			values.put("AppId", ""+mFunction.AppId);  
	        values.put("Function", mFunction.Function); 
	        values.put("Description", mFunction.Description);
	        values.put("Action", mFunction.Action);
	        values.put("Type", mFunction.Type);
	        mSQLiteDatabase.insert("Function", null, values);
	        Log.i(tag, "registerFunction OK");
		}
	}
	
	private void registerMeasurement(SystemDatabaseObject mSystemDatabaseObject){
		App mApp = mSystemDatabaseObject.mApp;
		ArrayList<Measurement> mMeasurementList = mSystemDatabaseObject.mMeasurementList;
		Measurement mMeasurement;
		for(int i=0;i<mMeasurementList.size();i++){
			mMeasurement = mMeasurementList.get(i);
			mMeasurement.AppId = mApp.AppId;
			if(mMeasurement.Measurement.endsWith("/DateTime"))continue;
			ContentValues values = new ContentValues();
			values.put("AppId", ""+mMeasurement.AppId);
			values.put("Measurement", ""+mMeasurement.Measurement);  
			values.put("Type", ""+mMeasurement.Type);  
			values.put("Description", ""+mMeasurement.Description);  
	        mSQLiteDatabase.insert("Measurement", null, values);
	        Log.i(tag, "registerMeasurement OK");
		}
	}
	
	private void obtainFunction(SystemDatabaseObject mServiceDatabaseObject){
		App mApp = mServiceDatabaseObject.mApp;
		String sql = SQL_SELECT_FUNCTION + "where AppId=?";
		String[] args = {String.valueOf(mApp.AppId)};
		Cursor mCursor = mSQLiteDatabase.rawQuery(sql, args);
		Function mFunction;
		while(mCursor.moveToNext()){
			mFunction = new Function();
			mFunction.AppId = mCursor.getInt(mCursor.getColumnIndex("AppId"));
			mFunction.Function = mCursor.getString(mCursor.getColumnIndex("Function"));
			mFunction.Description = mCursor.getString(mCursor.getColumnIndex("Description"));
			mFunction.Action = mCursor.getString(mCursor.getColumnIndex("Action"));
			mFunction.Type = mCursor.getInt(mCursor.getColumnIndex("Type"));
			mServiceDatabaseObject.mFunctionList.add(mFunction);
		}
	}
	
	public ArrayList<Function> obtainFunctionList(String where){
		if(where==null||where.equals(""))where="";
		else where = "where " + where;
		ArrayList<Function> mFunctionList = new ArrayList<Function>();
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_FUNCTION+where, null);
		Function mFunction = null;
		while(mCursor.moveToNext()){
			mFunction = new Function();
			mFunction.AppId = mCursor.getInt(mCursor.getColumnIndex("AppId"));
			mFunction.Function = mCursor.getString(mCursor.getColumnIndex("Function"));
			mFunction.Description = mCursor.getString(mCursor.getColumnIndex("Description"));
			mFunction.Action = mCursor.getString(mCursor.getColumnIndex("Action"));
			mFunction.Type = mCursor.getInt(mCursor.getColumnIndex("Type"));
			mFunctionList.add(mFunction);
		}
		return mFunctionList;
	}
	
	private void obtainMeasurement(SystemDatabaseObject mServiceDatabaseObject){
		App mApp = mServiceDatabaseObject.mApp;
		String[] args = {String.valueOf(mApp.AppId)};
		String sql = SQL_SELECT_MEASUREMENT + "where AppId=?";
		Cursor mCursor = mSQLiteDatabase.rawQuery(sql, args);
		Measurement mMeasurement;
		while(mCursor.moveToNext()){
			mMeasurement = new Measurement();
			mMeasurement.AppId = mCursor.getInt(mCursor.getColumnIndex("AppId"));
			mMeasurement.MeasurementId = mCursor.getInt(mCursor.getColumnIndex("MeasurementId"));
			mMeasurement.Measurement = mCursor.getString(mCursor.getColumnIndex("Measurement"));
			mMeasurement.Type = mCursor.getString(mCursor.getColumnIndex("Type"));
			mMeasurement.Description = mCursor.getString(mCursor.getColumnIndex("Description"));
			mServiceDatabaseObject.mMeasurementList.add(mMeasurement);
		}
	}
	
	public ArrayList<Measurement> obtainMeasurementList(Field Measurement,int ContainType){
		String[] args = new String[1];
		String sql = "";
		switch (ContainType) {
		case CONTAIN_DEFAULT:
			args[0] = ClassUtil.obtainFieldSchema(Measurement);
			sql = SQL_SELECT_MEASUREMENT + "where Measurement=?";
			break;
		case CONTAIN_FIELD:
			args[0] = Measurement.getName();
			sql = SQL_SELECT_MEASUREMENT + "where Measurement like %/?";
			break;
		
		case CONTAIN_PARENT:
			args[0] = ClassUtil.obtainParentSchema(Measurement);
			sql = SQL_SELECT_MEASUREMENT + "where Measurement like %?";
			break;
		} 
		
		Cursor mCursor = mSQLiteDatabase.rawQuery(sql, args);
		ArrayList<Measurement> mMeasurementList = new ArrayList<Measurement>();
		Measurement mMeasurement;
		while(mCursor.moveToNext()){
			mMeasurement = new Measurement();
			mMeasurement.AppId = mCursor.getInt(mCursor.getColumnIndex("AppId"));
			mMeasurement.MeasurementId = mCursor.getInt(mCursor.getColumnIndex("MeasurementId"));
			mMeasurement.Measurement = mCursor.getString(mCursor.getColumnIndex("Measurement"));
			mMeasurement.Type = mCursor.getString(mCursor.getColumnIndex("Type"));
			mMeasurement.Description = mCursor.getString(mCursor.getColumnIndex("Description"));
			mMeasurementList.add(mMeasurement);
		}
		return mMeasurementList;
	}
	
	public ArrayList<Measurement> obtainMeasurementList(String where){
		if(where==null||where.equals(""))where="";
		else where = "where " + where;
		ArrayList<Measurement> mMeasurementList = new ArrayList<Measurement>();
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_MEASUREMENT+where, null);
		Measurement mMeasurement = null;
		while(mCursor.moveToNext()){
			mMeasurement = new Measurement();
			mMeasurement.AppId = mCursor.getInt(mCursor.getColumnIndex("AppId"));
			mMeasurement.Description = mCursor.getString(mCursor.getColumnIndex("Description"));
			mMeasurement.Measurement = mCursor.getString(mCursor.getColumnIndex("Measurement"));
			mMeasurement.MeasurementId = mCursor.getInt(mCursor.getColumnIndex("MeasurementId"));
			mMeasurement.Type = mCursor.getString(mCursor.getColumnIndex("Type"));
			mMeasurementList.add(mMeasurement);
		}
		return mMeasurementList;
	}
	
	//-------------------------------SystemDatabase---------------------------------------
	
	
	//-------------------------------MeasurementDatabase----------------------------------
	
	public ArrayList<MeasurementData> obtainMeasurementDataList(String where){
		if(where==null||where.equals(""))where="";
		else where = "where " + where;
		ArrayList<MeasurementData> mMeasurementDataList = new ArrayList<MeasurementData>();
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_MEASUREMENTDATA+where, null);
		MeasurementData mMeasurementData = null;
		while(mCursor.moveToNext()){
			mMeasurementData = new MeasurementData();
			mMeasurementData.MeasurementId = mCursor.getInt(mCursor.getColumnIndex("MeasurementId"));
			mMeasurementData.GroupId = mCursor.getInt(mCursor.getColumnIndex("GroupId"));
			mMeasurementData.Data = mCursor.getString(mCursor.getColumnIndex("Data"));
			mMeasurementData.DateTime = mCursor.getString(mCursor.getColumnIndex("DateTime"));
			mMeasurementDataList.add(mMeasurementData);
		}
		return mMeasurementDataList;
	}
	
	/**
	 * mMeasurementList에 속한 MeasurementData의 리스트를 반환한다.
	 * 조건으로 시간을 받으며 시간이 null일 경우 조건에 추가되지 않는다.
	 * @param mMeasurementList
	 * @param DateTimeFrom
	 * @param DateTimeTo
	 * @return
	 */
	public List<MeasurementData> obtainMeasurementData(List<Measurement> mMeasurementList,String DateTimeFrom,String DateTimeTo){
		String where = "where ";
		ArrayList<String> condition = new ArrayList<String>();
		String MeasurementIdcondition = "";
		ArrayList<MeasurementData> mMeasurementDataList = new ArrayList<MeasurementData>();
		if(mMeasurementList.size()==0)return mMeasurementDataList;
		
		for(int i=0;i<mMeasurementList.size();i++){
			MeasurementIdcondition += mMeasurementList.get(i).MeasurementId;
			if(i!=mMeasurementList.size()-1){
				MeasurementIdcondition += ",";
			}
		}
		
		if(DateTimeFrom!=null)condition.add("DateTime >= '"+DateTimeFrom+"'");
		if(DateTimeTo!=null)condition.add("DateTime <= '"+DateTimeTo+"'");
		condition.add("MeasurementId in (" + MeasurementIdcondition + ")");
		
		for(int i=0;i<condition.size();i++){
			where += condition.get(i);
			if(i+1<condition.size())where += " and ";
		}
		
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_MEASUREMENTDATA+where, null);
		MeasurementData mMeasurementData;
		while(mCursor.moveToNext()){
			mMeasurementData = new MeasurementData();
			mMeasurementData.MeasurementId = mCursor.getInt(mCursor.getColumnIndex("MeasurementId"));
			mMeasurementData.GroupId = mCursor.getInt(mCursor.getColumnIndex("GroupId"));
			mMeasurementData.Data = mCursor.getString(mCursor.getColumnIndex("Data"));
			mMeasurementData.DateTime = mCursor.getString(mCursor.getColumnIndex("DateTime"));
			mMeasurementDataList.add(mMeasurementData);
		}
		return mMeasurementDataList;
	}
	
	//-------------------------------reflect 적용----------------------------------
	/**
	 * MeasurementData 테이블의 GroupId 칼럼에서 가장 큰 값을 찾아준다.
	 * 만약 없으면 0을 리턴한다.
	 * @return
	 */
	private int obtainMeasurementDataGroupId(){
		String sql = SQL_SELECT_GROUPID;
		Cursor mCursor = mSQLiteDatabase.rawQuery(sql, null);
		if(mCursor.moveToNext()){
			return mCursor.getInt(0);
		}
		return 0;
	}
	
	/**
	 * SystemDatabaseObject와 측정값을 등록할 Object와 비교하여 MeasurementId를 구한 후에 Id가 있으면 각각의 값들을 DB에 등록
	 * 한 객체의 필드들을 묶어주기 위해 GroupId를 구해서 +1을 해준 후 등록한다.  
	 * @param mSystemDatabaseObject
	 * @param obj
	 * @return 
	 */
	public void registerMeasurementData(SystemDatabaseObject mSystemDatabaseObject,Object obj) throws IllegalAccessException, IllegalArgumentException{
		MeasurementData mMeasurementData = new MeasurementData();
		ContentValues values = new ContentValues();
		ArrayList<Measurement> mMeasurementList = mSystemDatabaseObject.mMeasurementList;
		Field[] mFields = obj.getClass().getFields();
		int GroupId = obtainMeasurementDataGroupId()+1;
		Log.i(tag, "GroupId : "+GroupId);
		for(int i=0;i<mFields.length;i++){
			for(int j=0;j<mMeasurementList.size();j++){
				if(mMeasurementList.get(j).Measurement.contentEquals(ClassUtil.obtainFieldSchema(mFields[i]))){
					mMeasurementData.MeasurementId = mMeasurementList.get(j).MeasurementId;
					mMeasurementData.Data = mFields[i].get(obj).toString();
					//GroupId, MeasurementId, Data 등록
					values.put("GroupId", GroupId);
					values.put("MeasurementId", ""+mMeasurementData.MeasurementId);  
			        values.put("Data", mMeasurementData.Data);
			        mSQLiteDatabase.insert("MeasurementData", null, values);
			        Log.i(tag, "registerMeasurementData OK : "+GroupId+"/"+mMeasurementData.MeasurementId+"/"+mMeasurementData.Data);
				}
			}
		}
	}
	
	/**
	 * obj 클래스를 Measurement에서 obj의 필드와 일치하는 데이터를 검색한 후 
	 * 클래스와 일치하는 데이터가 있으면 데이터를 읽어온 후 해당 클래스에 데이터를 대입하여 ArrayList로 돌려준다.
	 * 해당 매소드를 사용할 때 대입되는쪽에 타입을 ArrayList로 해주어야 한다.
	 * example : ArrayList<Eye> mEyeList = mSqliteManager.obtainMeasurementData(Eye.class);   
	 * @param obj
	 * @return
	 * @throws InstantiationException : ins = c.newInstance(); 부분에서 인스턴스화하지 못했을 경우
	 * @throws IllegalAccessException : private 타입에 데이터를 대입할때 생기는 오류
	 * @throws ClassNotFoundException : Class c = Class.forName(obj.getName());에서 해당 클래스를 얻어오지 못했을 경우
	 */
	public String obtainMeasurementData(Class<?> obj) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		return obtainMeasurementData(obj,null,null,CONTAIN_DEFAULT);
	}
	public String obtainMeasurementData(Class<?> obj,String DateTimeFrom,String DateTimeTo) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		return obtainMeasurementData(obj,DateTimeFrom,DateTimeTo,CONTAIN_DEFAULT);
	}
	/**
	 * obtainMeasurementData 함수에서 추가적으로 시간을 검색 조건에 줄 수 있으며
	 * 시간이 null일 경우 조건에 포함되지 않는다.
	 * @param obj
	 * @param DateTimeFrom
	 * @param DateTimeTo
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public String obtainMeasurementData(Class<?> obj,String DateTimeFrom,String DateTimeTo,int ContainType) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		ArrayList<java.lang.Object> retObject = new ArrayList<java.lang.Object>();
		Field[] mFields = obj.getFields();
		Field mDateTimeField = null;
		//Measurement 리스트를 얻어온다.
		ArrayList<Measurement> mMeasurementList = new ArrayList<Measurement>();
		ArrayList<Measurement> tempMeasurementList;
		//Field 맵을 만든다.
		HashMap<Integer, Field> mFieldMap = new HashMap<Integer, Field>(); 
		//필드와 일치하는 MeasurementId를 구하고 mMeasurementList에 저장해둔다.
		for(int i=0;i<mFields.length;i++){
			//데이터가 저장된 시간인 DateTime을 얻기위한 필드
			if(mFields[i].getName().contentEquals("DateTime"))mDateTimeField = mFields[i];
			//일반 필드면 데이터베이스에서 값을 얻어온다.
			else {
				tempMeasurementList = obtainMeasurementList(mFields[i],ContainType);
				for(int j=0;j<tempMeasurementList.size();j++){
					mFieldMap.put(tempMeasurementList.get(j).MeasurementId,mFields[i]);
				}
				mMeasurementList.addAll(tempMeasurementList);
			}
		}
		
		//mMeasurementList에 저장한 ID로 mMeasurementDataList를 가져온다.
		List<MeasurementData> mMeasurementDataList = obtainMeasurementData(mMeasurementList,DateTimeFrom,DateTimeTo);
		MeasurementData mMeasurementData;
		
		//reflect를 이용하여 obj과 같은 클래스의 인스턴스를 만들어 각각의 필드에 해당 값을 셋팅해준다.
		Class c = Class.forName(obj.getName());
		java.lang.Object tempObject = null;
		
		//GroupId에 따라 같은 데이터가 같은 인스턴스에 저장될 수 있도록 맵을 이용한다.
		HashMap<Integer, java.lang.Object> mObjectMap = new HashMap<Integer, java.lang.Object>();
		//mMeasurementDataList를 루프를 돌면서 인스턴스에 값을 저장한다.
		//Map에 데이터가 없으면 새로 인스턴스를 생성해서 값을 저장한 후 Map에 푸시한다.
		
		for(int i=0;i<mMeasurementDataList.size();i++){
			mMeasurementData = mMeasurementDataList.get(i);
			tempObject = mObjectMap.get(mMeasurementData.GroupId);
			if(tempObject==null){
				tempObject = c.newInstance();
			}
			setClassField(mFieldMap.get(mMeasurementData.MeasurementId),mMeasurementData.Data,tempObject);
			//클래스의 DateTime을 측정값이 등록된 시간으로 한다.
			if(mDateTimeField!=null)mDateTimeField.set(tempObject, mMeasurementData.DateTime);
			mObjectMap.put(mMeasurementData.GroupId, tempObject);
		}
		
		//mObjectMap에 넣은 데이터를 리턴할 ArrayList에 넣어준다.
        for( Integer key : mObjectMap.keySet() ){
        	retObject.add(mObjectMap.get(key));
        }
		return gson.toJson(retObject);
	}
	
	/**
	 * mField의 타입에 따라서 mData를 타입 캐스팅 한 후 obj의 필드에 해당 데이터를 넣어준다.
	 * @param mField
	 * @param mData
	 * @param obj
	 * @throws NumberFormatException : String을 다른 타입으로 parse할 때 오류가 생길 경우
	 * @throws IllegalAccessException : private타입에 데이터를 대입할때 생기는 오류
	 * @throws IllegalArgumentException : Field의 set 매소드에 obj와 다른 타입의 Argument를 대입하려고 할 경우 생기는 오류(즉 타입이 다름)
	 */
	public void setClassField(Field mField,String mData,Object obj) throws NumberFormatException, IllegalAccessException, IllegalArgumentException{
		//타입명을 먼저 얻는다.
		String type = mField.getType().getName();
		//타입에 따라서 캐스팅을 한 후 해당 필드에 대입한다.
		if(type.contentEquals("double")){
			mField.setDouble(obj, Double.parseDouble(mData));
		}else if(type.contentEquals("int")){
			mField.setInt(obj, Integer.parseInt(mData));
		}else if(type.contentEquals("boolean")){
			mField.setBoolean(obj, Boolean.parseBoolean(mData));
		}else if(type.contentEquals("byte")){
			mField.setByte(obj, Byte.parseByte(mData));
		}else if(type.contentEquals("float")){
			mField.setFloat(obj, Float.parseFloat(mData));
		}else if(type.contentEquals("long")){
			mField.setLong(obj, Long.parseLong(mData));
		}else if(type.contentEquals("short")){
			mField.setShort(obj, Short.parseShort(mData));
		}else {
			mField.set(obj, mData);
		}
	}

	/**
	 * class에 해당하는 데이터를 DB에서 삭제한다.
	 * 조건으로 시간을 설정할 수 있으며 null이면 조건에 추가되지 않는다.
	 * @param obj
	 * @param DateTimeFrom
	 * @param DateTimeTo
	 * @return 삭제한 개수
	 */
	public int removeMeasurementData(Class<?> obj,String DateTimeFrom,String DateTimeTo){
		Field[] mFields = obj.getFields();
		//Measurement 리스트를 얻어온다.
		ArrayList<Measurement> mMeasurementList = new ArrayList<Measurement>();
		//필드와 일치하는 MeasurementId를 구하고 mMeasurementList에 저장해둔다.
		for(int i=0;i<mFields.length;i++){
			mMeasurementList.addAll(obtainMeasurementList(mFields[i],CONTAIN_DEFAULT));
		}

		String where = "";
		ArrayList<String> condition = new ArrayList<String>();
		String MeasurementIdcondition = "";
		if(mMeasurementList.size()==0)return 0;
		
		for(int i=0;i<mMeasurementList.size();i++){
			MeasurementIdcondition += mMeasurementList.get(i).MeasurementId;
			if(i!=mMeasurementList.size()-1){
				MeasurementIdcondition += ",";
			}
		}
		
		if(DateTimeFrom!=null)condition.add("DateTime >= '"+DateTimeFrom+"'");
		if(DateTimeTo!=null)condition.add("DateTime <= '"+DateTimeTo+"'");
		condition.add("MeasurementId in (" + MeasurementIdcondition + ")");
		
		for(int i=0;i<condition.size();i++){
			where += condition.get(i);
			if(i+1<condition.size())where += " and ";
		}
		
		return mSQLiteDatabase.delete("MeasurementData", where, null);
	}
	
	
	/**
	 * Register log to Log table. 
	 * @param Device
	 * @param App
	 * @param Type
	 * @param Content
	 */
	public void registerLog(String device,String app,int type,String content){
		if(device==null||app==null)return;
		ContentValues values = new ContentValues();
		values.put("Device", device);  
	    values.put("App", app);
	    values.put("Type", type);
	    values.put("Content", content);
	    mSQLiteDatabase.insert("BlinkLog", null, values);
	    Log.i(tag, "Log OK");
	}
	
	/**
	 * Search log from Log table
	 * @param Device
	 * @param App
	 * @param Type
	 * @param DateTimeFrom
	 * @param DateTimeTo
	 * @return
	 */
	public List<BlinkLog> obtainLog(String Device,String App,int Type,String DateTimeFrom,String DateTimeTo){
		String where = "";
		ArrayList<String> condition = new ArrayList<String>();
		
		if(Device!=null)condition.add("Device='"+Device+"'");
		if(App!=null)condition.add("App='"+App+"'");
		if(Type!=-1)condition.add("Type="+Type);
		if(DateTimeFrom!=null)condition.add("DateTime >= '"+DateTimeFrom+"'");
		if(DateTimeTo!=null)condition.add("DateTime <= '"+DateTimeTo+"'");
		
		if(condition.size()>0)where+="where ";
		for(int i=0;i<condition.size();i++){
			where += condition.get(i);
			if(i+1<condition.size())where += " and ";
		}
		
		ArrayList<BlinkLog> mBlinkLogList = new ArrayList<BlinkLog>();
		BlinkLog mBlinkLog = null;
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_LOG+where, null);
		while(mCursor.moveToNext()){
			mBlinkLog = new BlinkLog();
			mBlinkLog.LogId = mCursor.getInt(mCursor.getColumnIndex("LogId"));
			mBlinkLog.Device = mCursor.getString(mCursor.getColumnIndex("Device"));
			mBlinkLog.App = mCursor.getString(mCursor.getColumnIndex("App"));
			mBlinkLog.Type = mCursor.getInt(mCursor.getColumnIndex("Type"));
			mBlinkLog.Content = mCursor.getString(mCursor.getColumnIndex("Content"));
			mBlinkLog.DateTime = mCursor.getString(mCursor.getColumnIndex("DateTime"));
			mBlinkLogList.add(mBlinkLog);
		}
		return mBlinkLogList;
	}
	
	public List<BlinkLog> obtainLog(String Device,String App,String DateTimeFrom,String DateTimeTo){
		return obtainLog(Device,App,-1,DateTimeFrom,DateTimeTo);
	}
	public List<BlinkLog> obtainLog(String Device,String DateTimeFrom,String DateTimeTo){
		return obtainLog(Device,null,-1,DateTimeFrom,DateTimeTo);
	}
	public List<BlinkLog> obtainLog(String DateTimeFrom,String DateTimeTo){
		return obtainLog(null,null,-1,DateTimeFrom,DateTimeTo);
	}
	public List<BlinkLog> obtainLog(){
		return obtainLog(null,null,-1,null,null);
	}
}
