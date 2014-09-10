package kr.poturns.blink.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import android.content.Context;
import android.util.Log;

/**
 * 간편하게 쿼리를 날릴 수 있는 기능 추가
 * SqliteManager를 상속받아 사용하며
 * 여러 조건들을 조합하여 SqliteManager에 있는 매소드를 호출한다.
 * @author Jiwon
 *
 */
public class BlinkDatabaseManager extends SqliteManager{
	private static final String tag = "BlinkDatabaseManager";
	
	/**
	 * Database에 검색한 결과를 저장하는 변수들
	 * 하위 항목을 검색할 때 조건으로 자동으로 추가된다.
	 */
	private List<Device> mDeviceList = new ArrayList<Device>();
	private List<App> mAppList = new ArrayList<App>();
	private List<Function> mFunctionList = new ArrayList<Function>();
	private List<Measurement> mMeasurementList = new ArrayList<Measurement>();
	private List<MeasurementData> mMeasurementDataList = new ArrayList<MeasurementData>();
	
	/**
	 * 테스트 매소드
	 * 현재 변수들에 저장된 데이터를 로그캣에 보여준다.
	 */
	private void testBlinkDatabaseManager(){
		Log.i(tag,"Device List :");
		for(int i=0;i<mDeviceList.size();i++){
			Log.i(tag,mDeviceList.get(i).toString());
		}
		Log.i(tag,"App List :");
		for(int i=0;i<mAppList.size();i++){
			Log.i(tag,mAppList.get(i).toString());
		}
		Log.i(tag,"Function List :");
		for(int i=0;i<mFunctionList.size();i++){
			Log.i(tag,mFunctionList.get(i).toString());
		}
		Log.i(tag,"Measurement List :");
		for(int i=0;i<mMeasurementList.size();i++){
			Log.i(tag,mMeasurementList.get(i).toString());
		}
		Log.i(tag,"MeasurementData List :");
		for(int i=0;i<mMeasurementDataList.size();i++){
			Log.i(tag,mMeasurementDataList.get(i).toString());
		}
	}
	
	/**
	 * 생성자로 별다른 기능은 없다.
	 * SqliteManager의 생성자를 호출한다.
	 * @param context
	 */
	public BlinkDatabaseManager(Context context) {
		super(context);
	}
	
	/**
	 * Device를 검색하는 쿼리로 조건을 매개변수로 받는다. 결과는 mDeviceList에 저장된다.
	 * @param where
	 * @return
	 */
	public BlinkDatabaseManager queryDevice(String where){
		this.mDeviceList = this.obtainDeviceList(where);
		return this;
	}
	
	/**
	 * App을 검색하는 쿼리로 조건을 매개변수로 받는다. 기본적으로 mDeviceList에 저장되어 있는 Device 객체의 Id를 조건으로 설정한다. 결과는 mAppList에 저장된다.
	 * @param where
	 * @return
	 */
	public BlinkDatabaseManager queryApp(String where){
		String NewWhere = "";
		if(mDeviceList.size()>0){
			NewWhere = "DeviceId in (";
			for(int i=0;i<mDeviceList.size();i++){
				NewWhere += String.valueOf(mDeviceList.get(i).DeviceId);
				if(i<mDeviceList .size()-1)NewWhere+=",";
			}
			NewWhere+=")";
			if(!(where==null||where.equals("")))NewWhere +=  " and " + where;
		}else {
			NewWhere = where;
		}
		
		Log.i(tag, "queryApp : "+NewWhere);
				
		this.mAppList = this.obtainAppList(NewWhere);
		mAppList.toString();
		
		
		testBlinkDatabaseManager();
		return this;
	}
	
	/**
	 * Function을 검색하는 쿼리로 조건을 매개변수로 받는다. 기본적으로 mAppList에 저장되어 있는 App 객체의 Id를 조건으로 설정한다. 결과는 mFunctionList에 저장된다.
	 * @param where
	 * @return
	 */
	public BlinkDatabaseManager queryFunction(String where){
		String NewWhere = "";
		if(mAppList.size()>0){
			NewWhere = "AppId in (";
			for(int i=0;i<mAppList.size();i++){
				NewWhere += String.valueOf(mAppList.get(i).AppId);
				if(i<mAppList.size()-1)NewWhere+=",";
			}
			NewWhere+=")";
			
			if(!(where==null||where.equals("")))NewWhere +=  " and " + where;
		}else {
			NewWhere = where;
		}
		
		Log.i(tag, "queryFunction : "+NewWhere);
		
		this.mFunctionList = this.obtainFunctionList(NewWhere);
		mFunctionList.toString();
		
		
		testBlinkDatabaseManager();
		return this;
	}
	
	/**
	 * Measurement을 검색하는 쿼리로 조건을 매개변수로 받는다. 기본적으로 mAppList에 저장되어 있는 App 객체의 Id를 조건으로 설정한다. 결과는 mMeasurementList에 저장된다
	 * @param where
	 * @return
	 */
	public BlinkDatabaseManager queryMeasurement(String where){
		String NewWhere = "";
		if(mAppList.size()>0){
			NewWhere = "AppId in (";
			for(int i=0;i<mAppList.size();i++){
				NewWhere += String.valueOf(mAppList.get(i).AppId);
				if(i<mAppList.size()-1)NewWhere+=",";
			}
			NewWhere+=")";
			
			if(!(where==null||where.equals("")))NewWhere +=  " and " + where;
		}else {
			NewWhere = where;
		}
		
		Log.i(tag, "queryMeasurement : "+NewWhere);
				
		this.mMeasurementList = this.obtainMeasurementList(NewWhere);
		mMeasurementList.toString();
		
		
		testBlinkDatabaseManager();
		return this;
	}
	
	/**
	 * MeasurementData을 검색하는 쿼리로 조건을 매개변수로 받는다. 기본적으로 mMeasurementList에 저장되어 있는 Measurement 객체의 Id를 조건으로 설정한다. 결과는 mMeasurementDataList에 저장된다.
	 * @param where
	 * @return
	 */
	public BlinkDatabaseManager queryMeasurementData(String where){
		String NewWhere = "";
		if(mMeasurementList.size()>0){
			NewWhere = "MeasurementId in (";
			for(int i=0;i<mMeasurementList.size();i++){
				NewWhere += String.valueOf(mMeasurementList.get(i).MeasurementId);
				if(i<mMeasurementList.size()-1)NewWhere+=",";
			}
			NewWhere+=")";
			
			if(!(where==null||where.equals("")))NewWhere +=  " and " + where;
		}else {
			NewWhere = where;
		}
			
		Log.i(tag, "queryMeasurementData : "+NewWhere);
		
		this.mMeasurementDataList = this.obtainMeasurementDataList(NewWhere);
		mMeasurementDataList.toString();
		
		
		testBlinkDatabaseManager();
		return this;
	}
	
	/**
	 * 매개변수로 전달된 Function을 가지고 해당 Funtion에 해당하는 Device를 찾아서 리턴한다.
	 * @param function
	 * @return
	 */
	public Device obtainDevice(Function function){
		//등록된 앱 리스트 확인
		App mApp = obtainApp(function);
		if(mApp==null)return null;
		queryDevice("DeviceId="+mApp.DeviceId);
		if(mDeviceList.size()==0)return null;
		//디바이스 이름 비교
		return mDeviceList.get(0);
	}
	
	/**
	 * 매개변수로 전달된 Function을 가지고 해당 Funtion에 해당하는 App를 찾아서 리턴한다.
	 * @param function
	 * @return
	 */
	public App obtainApp(Function function){
		mDeviceList.clear();
		if(function==null)return null;
		//등록된 앱 리스트 확인
		queryApp("AppId="+function.AppId);
		if(mAppList.size()==0)return null;
		//등록된 디바이스 리스트 확인
		return mAppList.get(0);
	}
	
	
	
	/**
	 * Check out device has data
	 */
	
	/**
	 * Measurement 리스트를 기준으로 상위 계층인 Device를 검색하여 디바이스 내에 있는 데이터인지 확인한다. 두 번째로 주어진 String인 MacAddress로 판단하며, 하나라도 주어진 디바이스 외부 데이터가 있으면 true를 리턴한다. 그렇지 않으면 false를 리턴한다.
	 * @param mMeasurementList
	 * @param MacAddress
	 * @return
	 */
	public boolean checkOutDevice(List<Measurement> mMeasurementList,String MacAddress){
		mDeviceList.clear();
		if(mMeasurementList.size()==0)return false;
		
		String where = "AppId in (";
		//등록된 앱 리스트 확인
		for(int i=0;i<mMeasurementList.size();i++){
			where += mMeasurementList.get(i).AppId;
			if(i<mMeasurementList.size()-1)where += ",";
		}
		where += ")";
		
		queryApp(where);
		if(mAppList.size()==0)return false;
		//등록된 디바이스 리스트 확인
		queryDevice("DeviceId="+mAppList.get(0).DeviceId);
		if(mDeviceList.size()==0)return false;
		//디바이스 이름 비교
		for(int i=0;i<mDeviceList.size();i++){
			if(!mDeviceList.get(i).Device.contentEquals(MacAddress))return true;
		}
		return false;
	}
	
	/**
	 * Function을 기준으로 상위 계층인 Device를 검색하여 디바이스 내에 있는 데이터인지 확인한다. 두 번째로 주어진 String인 MacAddress로 판단하며, 하나라도 주어진 디바이스 외부 데이터가 있으면 true를 리턴한다. 그렇지 않으면 false를 리턴한다.
	 * @param mFunction
	 * @param MacAddress
	 * @return
	 */
	public boolean checkOutDevice(Function mFunction,String MacAddress){
		mDeviceList.clear();
		//등록된 앱 리스트 확인
		queryApp("AppId="+mFunction.AppId);
		if(mAppList.size()==0)return false;
		//등록된 디바이스 리스트 확인
		queryDevice("DeviceId="+mAppList.get(0).DeviceId);
		if(mDeviceList.size()==0)return false;
		//디바이스 이름 비교
		for(int i=0;i<mDeviceList.size();i++){
			if(!mDeviceList.get(i).MacAddress.contentEquals(MacAddress))return true;
		}
		return false;
	}
	
	/**
	 * Class를 기준으로 상위 계층인 Device를 검색하여 디바이스 내에 있는 데이터인지 확인한다. 두 번째로 주어진 String인 MacAddress로 판단하며, 하나라도 주어진 디바이스 외부 데이터가 있으면 true를 리턴한다. 그렇지 않으면 false를 리턴한다. 
	 * @param obj
	 * @param MacAddress
	 * @return
	 */
	public boolean checkOutDevice(Class<?> obj,String MacAddress){
		mDeviceList.clear();
		mAppList.clear();
		mMeasurementList.clear();
		
		//등록된 앱 리스트 확인
		Field[] mFields = obj.getFields();
		for(int i=0;i<mFields.length;i++){
			mMeasurementList.addAll(obtainMeasurementList(mFields[i],CONTAIN_DEFAULT));
		}
		return checkOutDevice(mMeasurementList,MacAddress);
	}
	
	/**
	 * 기존 변수들에 저장되어 있는 것을 모두 초기화한다.
	 */
	public void clear(){
		mDeviceList.clear();
		mAppList.clear();
		mFunctionList.clear();
		mMeasurementList.clear();
		mMeasurementDataList.clear();
	}
	
	/**
	 * getter , setter methods
	 */

	public List<Device> getDeviceList() {
		return mDeviceList;
	}

	public void setDeviceList(List<Device> mDeviceList) {
		this.mDeviceList = mDeviceList;
	}

	public List<App> getAppList() {
		return mAppList;
	}

	public void setAppList(List<App> mAppList) {
		this.mAppList = mAppList;
	}

	public List<Function> getFunctionList() {
		return mFunctionList;
	}

	public void setFunctionList(List<Function> mFunctionList) {
		this.mFunctionList = mFunctionList;
	}

	public List<Measurement> getMeasurementList() {
		return mMeasurementList;
	}

	public void setMeasurementList(List<Measurement> mMeasurementList) {
		this.mMeasurementList = mMeasurementList;
	}

	public List<MeasurementData> getMeasurementDataList() {
		return mMeasurementDataList;
	}

	public void setMeasurementDataList(List<MeasurementData> mMeasurementDataList) {
		this.mMeasurementDataList = mMeasurementDataList;
	}
}
