package kr.poturns.blink.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

public class SyncDatabaseManager extends BlinkDatabaseManager{
	private final static String tag = "SyncDatabaseManager";
	
	public Wearable wearable;
	public Main main;
	
	private static final int TEMP_MEASUREMENT_ID = 999999;
	
	public SyncDatabaseManager(Context context) {
	    super(context);
	    wearable = new Wearable();
	    main = new Main();
    }
	
	/**
	 * 기존 MeasurementId와 변경된 MeasurementId가 저장된 HashMap을 얻는다.
	 * @return
	 */
	private HashMap<Integer, Integer> obtainMeasurementMap(List<BlinkAppInfo> SystemDatabaseObjctList){
		List<BlinkAppInfo> oldSystemDatabaseObject = obtainSystemDatabase();
		List<BlinkAppInfo> newSystemDatabaseObject = SystemDatabaseObjctList;
		
		List<Measurement> oldMeasurementList;
		List<Measurement> newMeasurementList;
		
		Measurement oldMeasurement;
		Measurement newMeasurement;
		
		HashMap<Integer, Integer> MeasurementMap = new HashMap<Integer, Integer>();
		
		//새로운 BlinkAppInfo 리스트의 인덱스
		for(int i=0;i<newSystemDatabaseObject.size();i++){
			//기존 BlinkAppInfo 리스트의 인덱스
			for(int j=0;j<oldSystemDatabaseObject.size();j++){
				//동일한 systemDatabaseObject 검색
				if(newSystemDatabaseObject.get(i).mDevice.MacAddress.contentEquals(oldSystemDatabaseObject.get(j).mDevice.MacAddress)
						&& newSystemDatabaseObject.get(i).mApp.PackageName.contentEquals(oldSystemDatabaseObject.get(j).mApp.PackageName)){
					oldMeasurementList = oldSystemDatabaseObject.get(j).mMeasurementList;
					newMeasurementList = newSystemDatabaseObject.get(i).mMeasurementList;
					
					//동일한 Measurement 검색
					for(int k=0;k<newMeasurementList.size();k++){
						newMeasurement = newMeasurementList.get(k);
						for(int l=0;l<oldMeasurementList.size();l++){
							oldMeasurement = oldMeasurementList.get(l);
							if(newMeasurement.Measurement.contentEquals(oldMeasurement.Measurement)){
								if(oldMeasurement.MeasurementId!=newMeasurement.MeasurementId)
									MeasurementMap.put(oldMeasurement.MeasurementId, newMeasurement.MeasurementId);
							}
						}
					}
				}
			}
		}
		
		return MeasurementMap;
	}
	
	/**
	 * DB에 저장되어 있는 데이터와 비교하여 추가된 SystemDatabaseObject를 얻는다.
	 * @param SystemDatabaseObjctList
	 * @return
	 */
	private List<BlinkAppInfo> obtainAddedSystemDatabaseObject(List<BlinkAppInfo> SystemDatabaseObjctList){
		List<BlinkAppInfo> MainSystemDatabaseObjectList = obtainSystemDatabase();
		List<BlinkAppInfo> WearableSystemDatabaseObjectList = SystemDatabaseObjctList;
		List<BlinkAppInfo> AddedSystemDatabaseObjectList = new ArrayList<BlinkAppInfo>();
		boolean isAdded = true;
		//웨어러블 BlinkAppInfo 리스트의 인덱스
		for(int i=0;i<WearableSystemDatabaseObjectList.size();i++){
			isAdded = true;
			//메인 BlinkAppInfo 리스트의 인덱스
			for(int j=0;j<MainSystemDatabaseObjectList.size();j++){
				//동일한 systemDatabaseObject 검색
				if(WearableSystemDatabaseObjectList.get(i).mDevice.MacAddress.contentEquals(MainSystemDatabaseObjectList.get(j).mDevice.MacAddress)
						&& WearableSystemDatabaseObjectList.get(i).mApp.PackageName.contentEquals(MainSystemDatabaseObjectList.get(j).mApp.PackageName)
						&& WearableSystemDatabaseObjectList.get(i).mApp.Version==MainSystemDatabaseObjectList.get(j).mApp.Version){
					//동일한 SystemDatabaseObject가 존재하면 추가된게 아니기 때문에 isAdded 값 false로 변경
					isAdded = false;
				}
			}
			//추가된 BlinkAppInfo 리스트에 추가
			if(isAdded)AddedSystemDatabaseObjectList.add(WearableSystemDatabaseObjectList.get(i));
		}
		
		return AddedSystemDatabaseObjectList;
	}
	
	/**
	 * SystemDatabaseObject에 있는 값들을 모두 입력한다.
	 * register는 id값을 sqlite에서 만들어서 SystemDatabaseObject에 변경해주지만 여기서는 있는 그대로 입력된다.
	 * @param mSystemDatabaseObject
	 */
	private void insertSystemDatabase(BlinkAppInfo mSystemDatabaseObject){
		insertDevice(mSystemDatabaseObject);
		insertApp(mSystemDatabaseObject);
		insertFunction(mSystemDatabaseObject);
		insertMeasurement(mSystemDatabaseObject);
	}
	
	private void insertDevice(BlinkAppInfo mSystemDatabaseObject) {
		Device mDevice = mSystemDatabaseObject.mDevice;
		ContentValues values = new ContentValues();
		values.put("DeviceId", mDevice.DeviceId);
		values.put("Device", mDevice.Device);
		values.put("UUID", mDevice.UUID);
		values.put("MacAddress", mDevice.MacAddress);
		values.put("DateTime", mDevice.DateTime);
        mSQLiteDatabase.insert("Device", null, values);
	}
	
	private void insertApp(BlinkAppInfo mSystemDatabaseObject) {
		App mApp = mSystemDatabaseObject.mApp;
		ContentValues values = new ContentValues();
		values.put("AppId", mApp.AppId);
		values.put("DeviceId", mApp.DeviceId);
		values.put("PackageName", mApp.PackageName);
		values.put("AppName", mApp.AppName);
		values.put("Version", mApp.Version);
		values.put("DateTime", mApp.DateTime);
        mSQLiteDatabase.insert("App", null, values);
	}
	
	private void insertMeasurement(BlinkAppInfo mSystemDatabaseObject) {
		for(Measurement mMeasurement : mSystemDatabaseObject.mMeasurementList){
			ContentValues values = new ContentValues();
			values.put("AppId", mMeasurement.AppId);
			values.put("MeasurementId", mMeasurement.MeasurementId);
			values.put("Measurement", mMeasurement.Measurement);
			values.put("Type", mMeasurement.Type);
			values.put("Description", mMeasurement.Description);
	        mSQLiteDatabase.insert("Measurement", null, values);
		}
	}
	
	private void insertFunction(BlinkAppInfo mSystemDatabaseObject) {
		for(Function mFunction : mSystemDatabaseObject.mFunctionList){
			ContentValues values = new ContentValues();
			values.put("AppId", mFunction.AppId);
			values.put("Function", mFunction.Function);
			values.put("Description", mFunction.Description);
			values.put("Action", mFunction.Action);
			values.put("Type", mFunction.Type);
	        mSQLiteDatabase.insert("Function", null, values);
		}
	}

	/**
	 * SystemDatabase 테이블을 모두 삭제하는 매소드
	 * Device,App,Function,Measurement 테이블을 삭제한다.
	 */
	private void removeSystemDatabaseAll(){
		mSQLiteDatabase.delete("Device",null,null);
		mSQLiteDatabase.delete("App",null,null);
		mSQLiteDatabase.delete("Function",null,null);
		mSQLiteDatabase.delete("Measurement",null,null);
	}
	
	/**
	 * 주어진 MeasurementMap으로 MeasurementData 테이블에서 기존의 Id를 새로운 Id로 변경한다. 
	 * @param MeasurementMap
	 */
	private void SyncMeasurementData(HashMap<Integer, Integer> MeasurementMap){
		String set = "MeasurementId=";
		String where = "MeasurementId=";
		int tempKey,tempValue,tempMeasurementId=TEMP_MEASUREMENT_ID;

		Set<Entry<Integer,Integer>> mapSet = MeasurementMap.entrySet();
		Iterator<Entry<Integer,Integer>> mapIterator;
		Entry<Integer,Integer> mapEntry;
		while(mapSet.size()>0){
			mapIterator = mapSet.iterator();
			if(mapIterator.hasNext()){
				mapEntry = mapIterator.next();
				
				set = "MeasurementId=";
				where = "MeasurementId=";
				
				int key = mapEntry.getKey();
				int value = mapEntry.getValue();
				
				//현재 바꾸려는 Id가 이미 존재하면 바꾸려는 Id를 가진 값을 먼저 임시 값으로 변경하고 Map을 셋팅한다.
				if(MeasurementMap.get(value)!=null){
					tempKey = value;
					tempValue = MeasurementMap.get(tempKey);
					
					//임시 MeasurementId 값 구하기
					while(MeasurementMap.get(tempMeasurementId)!=null)tempMeasurementId++;
					
					//변경할 조건 설정
					set += tempMeasurementId;
					where += tempKey;
					
					//업데이트
					updateMeasurementData(set, where);
					
					//기존 키로 되어 있는 값을 지우고 임시로 할당한 아이디로 저장한다.
					MeasurementMap.remove(tempKey);
					MeasurementMap.put(tempMeasurementId, tempValue);
					
					set = "MeasurementId=";
					where = "MeasurementId=";
				}
				
				set += value;
				where += key;
				
				updateMeasurementData(set, where);
				MeasurementMap.remove(key);
			}
			
			mapSet = MeasurementMap.entrySet();
		}
	}
	
	/**
	 * MeausrmentData 테이블을 업데이트하는 매소드
	 * @param set
	 * @param where
	 */
	private void updateMeasurementData(String set,String where){
		if(set==null||set.equals(""))return;
		if(where==null||where.equals(""))return;
				
		String query = "update MeasurementData set "+set+" where "+where;
		mSQLiteDatabase.execSQL(query);
	}
	
	public class Wearable {
		/**
		 * 주어진 systemDatabaseObjectList로 systemDatabaseObject를 업데이트한다.
		 * @return
		 */
		public boolean syncSystemDatabase(List<BlinkAppInfo> SystemDatabaseObjctList){
			//변경해야할 MeasurementId 맵을 가진 HashMap을 얻는다.
			HashMap<Integer, Integer> MeasurementMap = obtainMeasurementMap(SystemDatabaseObjctList);
			//MeasurementData 테이블을 동기화한다.
			SyncMeasurementData(MeasurementMap);
			
			//SystemDatabase를 동기화한다.
			//기존 SystemDatabase삭제 (Device,App,Function,Measurement)
			removeSystemDatabaseAll();
			
			Log.i(tag, "syncSystemDatabase");
			
			for(BlinkAppInfo mSystemDatabaseObject : SystemDatabaseObjctList){
				insertSystemDatabase(mSystemDatabaseObject);
			}
			return true;
		}
	}
	
	public class Main {
		/**
		 * 주어진 SystemDatabaseObjectList와 비교하여 새로운 부분을 추가한다.
		 */
		public boolean syncSystemDatabase(List<BlinkAppInfo> SystemDatabaseObjctList){
			//Main Device에 저장되어 있지 않은 SystemDatabaseObject를 검색한다.
			List<BlinkAppInfo> AddedSystemDatabaseObjectList = obtainAddedSystemDatabaseObject(SystemDatabaseObjctList);
			
			//SystemDatabase에 추가한다.
			for(BlinkAppInfo mSystemDatabaseObject : AddedSystemDatabaseObjectList){
				registerSystemDatabase(mSystemDatabaseObject);
			}
			return true;
		}
	}
	
	
}
