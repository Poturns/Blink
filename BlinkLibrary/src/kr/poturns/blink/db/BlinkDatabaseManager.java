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
import android.os.Build;
import android.util.Log;

/**
 * 간편하게 쿼리를 날릴 수 있는 기능 추가
 * SqliteManager는 직접 raw 쿼리를 날린다.
 * @author Jiwon
 *
 */
public class BlinkDatabaseManager extends SqliteManager{
	private static final String tag = "BlinkDatabaseManager";
	
	private List<Device> mDeviceList = new ArrayList<Device>();
	private List<App> mAppList = new ArrayList<App>();
	private List<Function> mFunctionList = new ArrayList<Function>();
	private List<Measurement> mMeasurementList = new ArrayList<Measurement>();
	private List<MeasurementData> mMeasurementDataList = new ArrayList<MeasurementData>();
	
	public void testBlinkDatabaseManager(){
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
	
	public BlinkDatabaseManager(Context context) {
		super(context);
	}
	
	public BlinkDatabaseManager queryDevice(String where){
		this.mDeviceList = this.obtainDeviceList(where);
		return this;
	}
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
	
	
	public boolean checkInDevice(List<Measurement> mMeasurementList){
		mDeviceList.clear();
		if(mMeasurementList.size()==0)return true;
		
		String where = "AppId in (";
		//등록된 앱 리스트 확인
		for(int i=0;i<mMeasurementList.size();i++){
			where += mMeasurementList.get(i).AppId;
			if(i<mMeasurementList.size()-1)where += ",";
		}
		where += ")";
		
		queryApp(where);
		if(mAppList.size()==0)return true;
		//등록된 디바이스 리스트 확인
		queryDevice("DeviceId="+mAppList.get(0).DeviceId);
		if(mDeviceList.size()==0)return true;
		//디바이스 이름 비교
		for(int i=0;i<mDeviceList.size();i++){
			if(!mDeviceList.get(i).Device.contentEquals(Build.MODEL))return false;
		}
		return true;
	}
	
	public boolean checkInDevice(Function mFunction){
		mDeviceList.clear();
		//등록된 앱 리스트 확인
		queryApp("AppId="+mFunction.AppId);
		if(mAppList.size()==0)return true;
		//등록된 디바이스 리스트 확인
		queryDevice("DeviceId="+mAppList.get(0).DeviceId);
		if(mDeviceList.size()==0)return true;
		//디바이스 이름 비교
		if(mDeviceList.get(0).Device.contentEquals(Build.MODEL))return true;
		return false;
	}
	
	public boolean checkInDevice(String ClassName){
		mDeviceList.clear();
		mAppList.clear();
		mMeasurementList.clear();
		
		//등록된 앱 리스트 확인
		Class<?> mClass;
        try {
	        mClass = Class.forName(ClassName);
        } catch (ClassNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        return true;
        }
		Field[] mFields = mClass.getFields();
		for(int i=0;i<mFields.length;i++){
			mMeasurementList.addAll(obtainMeasurementList(mFields[i],CONTAIN_DEFAULT));
		}
		
		if(mMeasurementList.size()==0)return true;
		queryApp("AppId="+mMeasurementList.get(0).AppId);
		if(mAppList.size()==0)return true;
		//등록된 디바이스 리스트 확인
		queryDevice("DeviceId="+mAppList.get(0).DeviceId);
		if(mDeviceList.size()==0)return true;
		//디바이스 이름 비교
		if(mDeviceList.get(0).Device.contentEquals(Build.MODEL))return true;
		return false;
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
