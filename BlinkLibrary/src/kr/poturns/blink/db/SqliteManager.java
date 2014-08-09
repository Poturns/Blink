package kr.poturns.blink.db;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import kr.poturns.blink.db.archive.DeviceApp;
import kr.poturns.blink.db.archive.DeviceAppFunction;
import kr.poturns.blink.db.archive.DeviceAppLog;
import kr.poturns.blink.db.archive.DeviceAppMeasurement;
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
	
	private final String SQL_SELECT_DEVICEAPPLIST = "SELECT * FROM DeviceAppList ";
	private final String SQL_SELECT_DEVICEAPPFUNCTION = "SELECT * FROM DeviceAppFunction where DeviceAppId=?";
	private final String SQL_SELECT_DEVICEAPPMEASUREMENT = "SELECT * FROM DeviceAppMeasurement ";
	private final String SQL_SELECT_MEASUREMENTDATA =  "SELECT * FROM MeasurementData ";
	private final String SQL_SELECT_GROUPID =  "SELECT max(GroupId) FROM MeasurementData ";
	private final String SQL_DELETE_MEASUREMENTDATA = "delete from MeasurementData ";
	private final String SQL_SELECT_LOG =  "SELECT * FROM DeviceAppLog ";
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
		registerDeviceAppList(mSystemDatabaseObject);
		if(!obtainDeviceAppList(mSystemDatabaseObject)){
			return;
		}
		registerDeviceAppFunction(mSystemDatabaseObject);
		registerDeviceAppMeasurement(mSystemDatabaseObject);
		Log.i(tag, "registerSystemDatabase OK");
	}
	public SystemDatabaseObject obtainSystemDatabase(String device,String app){
		SystemDatabaseObject mServiceDatabaseObject = new SystemDatabaseObject();
		DeviceApp mDeviceAppList = mServiceDatabaseObject.mDeviceApp;
		mDeviceAppList.Device = device;
		mDeviceAppList.App = app;
		//기존에 등록된 값이 없으면
		if(!obtainDeviceAppList(mServiceDatabaseObject)){
			mDeviceAppList.Version = 1;
			mServiceDatabaseObject.isExist = false;
			return mServiceDatabaseObject;
		}
		//기존에 등록된 값이 있으면 해당 값을 찾아서 리턴
		else {
			mServiceDatabaseObject.isExist = true;
			obtainDeviceAppFunction(mServiceDatabaseObject);
			obtainDeviceAppMeasurement(mServiceDatabaseObject);
		}
		return mServiceDatabaseObject;
	}
	public ArrayList<SystemDatabaseObject> obtainSystemDatabase(){
		ArrayList<SystemDatabaseObject> mSystemDatabaseObjectList = new ArrayList<SystemDatabaseObject>();
		ArrayList<DeviceApp> mDeviceAppList = obtainDeviceAppList();
		//기존에 등록된 값이 없으면
		SystemDatabaseObject mServiceDatabaseObject = null;
		for(int i=0;i<mDeviceAppList.size();i++){
			mServiceDatabaseObject = new SystemDatabaseObject();
			mServiceDatabaseObject.mDeviceApp = mDeviceAppList.get(i);
			obtainDeviceAppFunction(mServiceDatabaseObject);
			obtainDeviceAppMeasurement(mServiceDatabaseObject);
			mServiceDatabaseObject.isExist = true;
			mSystemDatabaseObjectList.add(mServiceDatabaseObject);
		}
		return mSystemDatabaseObjectList;
	}
	
	private boolean obtainDeviceAppList(SystemDatabaseObject mSystemDatabaseObject){
		DeviceApp mDeviceAppList = mSystemDatabaseObject.mDeviceApp;
		String query = SQL_SELECT_DEVICEAPPLIST+"where Device=? and App=?";
		String[] args = {mDeviceAppList.Device,mDeviceAppList.App};
		Cursor mCursor = mSQLiteDatabase.rawQuery(query, args);
		Log.i(tag, "Device : "+mDeviceAppList.Device+" App : "+mDeviceAppList.App);
		if(mCursor.moveToNext()){
			mDeviceAppList.DeviceAppId = mCursor.getInt(mCursor.getColumnIndex("DeviceAppId"));
			mDeviceAppList.Device = mCursor.getString(mCursor.getColumnIndex("Device"));
			mDeviceAppList.App = mCursor.getString(mCursor.getColumnIndex("App"));
			mDeviceAppList.Description = mCursor.getString(mCursor.getColumnIndex("Description"));
			mDeviceAppList.Version = mCursor.getInt(mCursor.getColumnIndex("Version"));
			return true;
		}
		return false;
	}
	
	private ArrayList<DeviceApp> obtainDeviceAppList(){
		ArrayList<DeviceApp> mDeviceAppList = new ArrayList<DeviceApp>();
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_DEVICEAPPLIST, null);
		DeviceApp mDeviceApp = null;
		if(mCursor.moveToNext()){
			mDeviceApp = new DeviceApp();
			mDeviceApp.DeviceAppId = mCursor.getInt(mCursor.getColumnIndex("DeviceAppId"));
			mDeviceApp.Device = mCursor.getString(mCursor.getColumnIndex("Device"));
			mDeviceApp.App = mCursor.getString(mCursor.getColumnIndex("App"));
			mDeviceApp.Description = mCursor.getString(mCursor.getColumnIndex("Description"));
			mDeviceApp.Version = mCursor.getInt(mCursor.getColumnIndex("Version"));
			mDeviceAppList.add(mDeviceApp);
		}
		return mDeviceAppList;
	}
	
	private void registerDeviceAppList(SystemDatabaseObject mSystemDatabaseObject){
		DeviceApp mDeviceAppList = mSystemDatabaseObject.mDeviceApp;
		ContentValues values = new ContentValues();
		values.put("Device", mDeviceAppList.Device);  
        values.put("App", mDeviceAppList.App); 
        values.put("Description", mDeviceAppList.Description);
        values.put("Version", mDeviceAppList.Version);
        mSQLiteDatabase.insert("DeviceAppList", null, values);
	}
	
	private void registerDeviceAppFunction(SystemDatabaseObject mSystemDatabaseObject){
		DeviceApp mDeviceAppList = mSystemDatabaseObject.mDeviceApp;
		ArrayList<DeviceAppFunction> mDeviceAppFunctionList = mSystemDatabaseObject.mDeviceAppFunctionList;
		DeviceAppFunction mDeviceAppFunction;
		for(int i=0;i<mDeviceAppFunctionList.size();i++){
			mDeviceAppFunction = mDeviceAppFunctionList.get(i);
			mDeviceAppFunction.DeviceAppId = mDeviceAppList.DeviceAppId;
			ContentValues values = new ContentValues();
			values.put("DeviceAppId", ""+mDeviceAppFunction.DeviceAppId);  
	        values.put("Function", mDeviceAppFunction.Function); 
	        values.put("Description", mDeviceAppFunction.Description);
	        mSQLiteDatabase.insert("DeviceAppFunction", null, values);
	        Log.i(tag, "registerDeviceAppFunction OK");
		}
	}
	
	private void registerDeviceAppMeasurement(SystemDatabaseObject mSystemDatabaseObject){
		DeviceApp mDeviceAppList = mSystemDatabaseObject.mDeviceApp;
		ArrayList<DeviceAppMeasurement> mDeviceAppMeasurementList = mSystemDatabaseObject.mDeviceAppMeasurementList;
		DeviceAppMeasurement mDeviceAppMeasurement;
		for(int i=0;i<mDeviceAppMeasurementList.size();i++){
			mDeviceAppMeasurement = mDeviceAppMeasurementList.get(i);
			mDeviceAppMeasurement.DeviceAppId = mDeviceAppList.DeviceAppId;
			if(mDeviceAppMeasurement.Measurement.endsWith("/DateTime"))continue;
			ContentValues values = new ContentValues();
			values.put("DeviceAppId", ""+mDeviceAppMeasurement.DeviceAppId);
			values.put("Measurement", ""+mDeviceAppMeasurement.Measurement);  
			values.put("Type", ""+mDeviceAppMeasurement.Type);  
			values.put("Description", ""+mDeviceAppMeasurement.Description);  
	        mSQLiteDatabase.insert("DeviceAppMeasurement", null, values);
	        Log.i(tag, "registerDeviceAppMeasurement OK");
		}
	}
	
	private void obtainDeviceAppFunction(SystemDatabaseObject mServiceDatabaseObject){
		DeviceApp mDeviceAppList = mServiceDatabaseObject.mDeviceApp;
		String[] args = {String.valueOf(mDeviceAppList.DeviceAppId)};
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_DEVICEAPPFUNCTION, args);
		DeviceAppFunction mDeviceAppFunction;
		while(mCursor.moveToNext()){
			mDeviceAppFunction = new DeviceAppFunction();
			mDeviceAppFunction.DeviceAppId = mCursor.getInt(mCursor.getColumnIndex("DeviceAppId"));
			mDeviceAppFunction.Function = mCursor.getString(mCursor.getColumnIndex("Function"));
			mDeviceAppFunction.Description = mCursor.getString(mCursor.getColumnIndex("Description"));
			mServiceDatabaseObject.mDeviceAppFunctionList.add(mDeviceAppFunction);
		}
	}
	
	private void obtainDeviceAppMeasurement(SystemDatabaseObject mServiceDatabaseObject){
		DeviceApp mDeviceAppList = mServiceDatabaseObject.mDeviceApp;
		String[] args = {String.valueOf(mDeviceAppList.DeviceAppId)};
		String sql = SQL_SELECT_DEVICEAPPMEASUREMENT + "where DeviceAppId=?";
		Cursor mCursor = mSQLiteDatabase.rawQuery(sql, args);
		DeviceAppMeasurement mDeviceAppMeasurement;
		while(mCursor.moveToNext()){
			mDeviceAppMeasurement = new DeviceAppMeasurement();
			mDeviceAppMeasurement.DeviceAppId = mCursor.getInt(mCursor.getColumnIndex("DeviceAppId"));
			mDeviceAppMeasurement.MeasurementId = mCursor.getInt(mCursor.getColumnIndex("MeasurementId"));
			mDeviceAppMeasurement.Measurement = mCursor.getString(mCursor.getColumnIndex("Measurement"));
			mDeviceAppMeasurement.Type = mCursor.getString(mCursor.getColumnIndex("Type"));
			mDeviceAppMeasurement.Description = mCursor.getString(mCursor.getColumnIndex("Description"));
			mServiceDatabaseObject.mDeviceAppMeasurementList.add(mDeviceAppMeasurement);
		}
	}
	
	public ArrayList<DeviceAppMeasurement> obtainDeviceAppMeasurementList(Field Measurement,int ContainType){
		String[] args = new String[1];
		String sql = "";
		switch (ContainType) {
		case CONTAIN_DEFAULT:
			args[0] = ClassUtil.obtainFieldSchema(Measurement);
			sql = SQL_SELECT_DEVICEAPPMEASUREMENT + "where Measurement=?";
			break;
		case CONTAIN_FIELD:
			args[0] = Measurement.getName();
			sql = SQL_SELECT_DEVICEAPPMEASUREMENT + "where Measurement like %/?";
			break;
		
		case CONTAIN_PARENT:
			args[0] = ClassUtil.obtainParentSchema(Measurement);
			sql = SQL_SELECT_DEVICEAPPMEASUREMENT + "where Measurement like %?";
			break;
		} 
		
		Cursor mCursor = mSQLiteDatabase.rawQuery(sql, args);
		ArrayList<DeviceAppMeasurement> mDeviceAppMeasurementList = new ArrayList<DeviceAppMeasurement>();
		DeviceAppMeasurement mDeviceAppMeasurement;
		while(mCursor.moveToNext()){
			mDeviceAppMeasurement = new DeviceAppMeasurement();
			mDeviceAppMeasurement.DeviceAppId = mCursor.getInt(mCursor.getColumnIndex("DeviceAppId"));
			mDeviceAppMeasurement.MeasurementId = mCursor.getInt(mCursor.getColumnIndex("MeasurementId"));
			mDeviceAppMeasurement.Measurement = mCursor.getString(mCursor.getColumnIndex("Measurement"));
			mDeviceAppMeasurement.Type = mCursor.getString(mCursor.getColumnIndex("Type"));
			mDeviceAppMeasurement.Description = mCursor.getString(mCursor.getColumnIndex("Description"));
			mDeviceAppMeasurementList.add(mDeviceAppMeasurement);
		}
		return mDeviceAppMeasurementList;
	}
	
	//-------------------------------SystemDatabase---------------------------------------
	
	
	//-------------------------------MeasurementDatabase----------------------------------
	
	/**
	 * mDeviceAppMeasurementList에 속한 MeasurementData의 리스트를 반환한다.
	 * 조건으로 시간을 받으며 시간이 null일 경우 조건에 추가되지 않는다.
	 * @param mDeviceAppMeasurementList
	 * @param DateTimeFrom
	 * @param DateTimeTo
	 * @return
	 */
	private ArrayList<MeasurementData> obtainMeasurementData(ArrayList<DeviceAppMeasurement> mDeviceAppMeasurementList,String DateTimeFrom,String DateTimeTo){
		String where = "where ";
		ArrayList<String> condition = new ArrayList<String>();
		String MeasurementIdcondition = "";
		ArrayList<MeasurementData> mMeasurementDataList = new ArrayList<MeasurementData>();
		if(mDeviceAppMeasurementList.size()==0)return mMeasurementDataList;
		
		for(int i=0;i<mDeviceAppMeasurementList.size();i++){
			MeasurementIdcondition += mDeviceAppMeasurementList.get(i).MeasurementId;
			if(i!=mDeviceAppMeasurementList.size()-1){
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
		ArrayList<DeviceAppMeasurement> mDeviceAppMeasurementList = mSystemDatabaseObject.mDeviceAppMeasurementList;
		Field[] mFields = obj.getClass().getFields();
		int GroupId = obtainMeasurementDataGroupId()+1;
		Log.i(tag, "GroupId : "+GroupId);
		for(int i=0;i<mFields.length;i++){
			for(int j=0;j<mDeviceAppMeasurementList.size();j++){
				if(mDeviceAppMeasurementList.get(j).Measurement.contentEquals(ClassUtil.obtainFieldSchema(mFields[i]))){
					mMeasurementData.MeasurementId = mDeviceAppMeasurementList.get(j).MeasurementId;
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
	 * obj 클래스를 DeviceAppMeasurement에서 obj의 필드와 일치하는 데이터를 검색한 후 
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
		//DeviceAppMeasurement 리스트를 얻어온다.
		ArrayList<DeviceAppMeasurement> mDeviceAppMeasurementList = new ArrayList<DeviceAppMeasurement>();
		ArrayList<DeviceAppMeasurement> tempDeviceAppMeasurementList;
		//Field 맵을 만든다.
		HashMap<Integer, Field> mFieldMap = new HashMap<Integer, Field>(); 
		//필드와 일치하는 MeasurementId를 구하고 mDeviceAppMeasurementList에 저장해둔다.
		for(int i=0;i<mFields.length;i++){
			//데이터가 저장된 시간인 DateTime을 얻기위한 필드
			if(mFields[i].getName().contentEquals("DateTime"))mDateTimeField = mFields[i];
			//일반 필드면 데이터베이스에서 값을 얻어온다.
			else {
				tempDeviceAppMeasurementList = obtainDeviceAppMeasurementList(mFields[i],ContainType);
				for(int j=0;j<tempDeviceAppMeasurementList.size();j++){
					mFieldMap.put(tempDeviceAppMeasurementList.get(j).MeasurementId,mFields[i]);
				}
				mDeviceAppMeasurementList.addAll(tempDeviceAppMeasurementList);
			}
		}
		
		//mDeviceAppMeasurementList에 저장한 ID로 mMeasurementDataList를 가져온다.
		ArrayList<MeasurementData> mMeasurementDataList = obtainMeasurementData(mDeviceAppMeasurementList,DateTimeFrom,DateTimeTo);
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
		//DeviceAppMeasurement 리스트를 얻어온다.
		ArrayList<DeviceAppMeasurement> mDeviceAppMeasurementList = new ArrayList<DeviceAppMeasurement>();
		//필드와 일치하는 MeasurementId를 구하고 mDeviceAppMeasurementList에 저장해둔다.
		for(int i=0;i<mFields.length;i++){
			mDeviceAppMeasurementList.addAll(obtainDeviceAppMeasurementList(mFields[i],CONTAIN_DEFAULT));
		}

		String where = "";
		ArrayList<String> condition = new ArrayList<String>();
		String MeasurementIdcondition = "";
		if(mDeviceAppMeasurementList.size()==0)return 0;
		
		for(int i=0;i<mDeviceAppMeasurementList.size();i++){
			MeasurementIdcondition += mDeviceAppMeasurementList.get(i).MeasurementId;
			if(i!=mDeviceAppMeasurementList.size()-1){
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
	
	public void registerLog(int mDeviceAppId,String Content){
		ContentValues values = new ContentValues();
		values.put("DeviceAppId", ""+mDeviceAppId);  
	    values.put("Content", Content);
	    mSQLiteDatabase.insert("DeviceAppLog", null, values);
	    Log.i(tag, "Log OK");
	}
	
	public ArrayList<DeviceAppLog> obtainLog(ArrayList<Integer> mDeviceAppIdList, String DateTimeFrom,String DateTimeTo){
		String where = "where ";
		ArrayList<String> condition = new ArrayList<String>();
		String DeviceAppIdcondition = "";
		for(int i=0;i<mDeviceAppIdList.size();i++){
			DeviceAppIdcondition += mDeviceAppIdList.get(i);
			if(i!=mDeviceAppIdList.size()-1){
				DeviceAppIdcondition += ",";
			}
		}
		
		if(DateTimeFrom!=null)condition.add("DateTime >= '"+DateTimeFrom+"'");
		if(DateTimeTo!=null)condition.add("DateTime <= '"+DateTimeTo+"'");
		if(DeviceAppIdcondition.length()>0)condition.add("DeviceAppId in (" + DeviceAppIdcondition + ")");
		
		for(int i=0;i<condition.size();i++){
			where += condition.get(i);
			if(i+1<condition.size())where += " and ";
		}
		
		ArrayList<DeviceAppLog> mDeviceAppLogList = new ArrayList<DeviceAppLog>();
		DeviceAppLog mDeviceAppLog = null;
		Cursor mCursor = mSQLiteDatabase.rawQuery(SQL_SELECT_LOG+where, null);
		while(mCursor.moveToNext()){
			mDeviceAppLog = new DeviceAppLog();
			mDeviceAppLog.DeviceAppId = mCursor.getInt(mCursor.getColumnIndex("DeviceAppId"));
			mDeviceAppLog.Content = mCursor.getString(mCursor.getColumnIndex("Content"));
			mDeviceAppLog.DateTime = mCursor.getString(mCursor.getColumnIndex("DateTime"));
			mDeviceAppLogList.add(mDeviceAppLog);
		}
		return mDeviceAppLogList;
	}
}