package kr.poturns.blink.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Build;

import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;

/**
 * 간편하게 쿼리를 날릴 수 있는 기능 추가
 * SqliteManager는 직접 raw 쿼리를 날린다.
 * @author Jiwon
 *
 */
public class BlinkDatabaseManager extends SqliteManager{
	private ArrayList<Device> mDeviceList = new ArrayList<Device>();
	private ArrayList<App> mAppList = new ArrayList<App>();
	private ArrayList<Function> mFunctionList = new ArrayList<Function>();
	private ArrayList<Measurement> mMeasurementList = new ArrayList<Measurement>();
	private ArrayList<MeasurementData> mMeasurementDataList = new ArrayList<MeasurementData>();
	
	public BlinkDatabaseManager(Context context) {
		super(context);
	}
	
	public BlinkDatabaseManager queryDevice(String where){
		this.mDeviceList = this.obtainDeviceList(where);
		return this;
	}
	public BlinkDatabaseManager queryApp(String where){
		String NewWhere = "DeviceId in (";
		for(int i=0;i<mDeviceList.size();i++){
			NewWhere += String.valueOf(mDeviceList.get(i).DeviceId);
			if(i<mDeviceList.size()-1)NewWhere+=",";
		}
		if(mDeviceList.size()>0)NewWhere+=") and " + where;
		else NewWhere = where;
				
		this.mAppList = this.obtainAppList(NewWhere);
		mAppList.toString();
		return this;
	}
	public BlinkDatabaseManager queryFunction(String where){
		String NewWhere = "AppId in (";
		for(int i=0;i<mAppList.size();i++){
			NewWhere += String.valueOf(mAppList.get(i).AppId);
			if(i<mAppList.size()-1)NewWhere+=",";
		}
		if(mAppList.size()>0)NewWhere+=") and " + where;
		else NewWhere = where;
				
		this.mFunctionList = this.obtainFunctionList(NewWhere);
		mFunctionList.toString();
		return this;
	}
	public BlinkDatabaseManager queryMeasurement(String where){
		String NewWhere = "AppId in (";
		for(int i=0;i<mAppList.size();i++){
			NewWhere += String.valueOf(mAppList.get(i).AppId);
			if(i<mAppList.size()-1)NewWhere+=",";
		}
		if(mAppList.size()>0)NewWhere+=") and " + where;
		else NewWhere = where;
				
		this.mMeasurementList = this.obtainMeasurementList(NewWhere);
		mMeasurementList.toString();
		return this;
	}
	public BlinkDatabaseManager queryMeasurementData(String where){
		String NewWhere = "MeasurementId in (";
		for(int i=0;i<mMeasurementList.size();i++){
			NewWhere += String.valueOf(mMeasurementList.get(i).MeasurementId);
			if(i<mMeasurementList.size()-1)NewWhere+=",";
		}
		if(mMeasurementList.size()>0)NewWhere+=") and " + where;
		else NewWhere = where;
				
		this.mMeasurementDataList = this.obtainMeasurementDataList(NewWhere);
		mMeasurementDataList.toString();
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
	
	public boolean checkInDevice(String schema){
		mDeviceList.clear();
		//등록된 앱 리스트 확인
		queryApp("AppId like *"+schema+"*");
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

	public ArrayList<Device> getDeviceList() {
		return mDeviceList;
	}

	public void setDeviceList(ArrayList<Device> mDeviceList) {
		this.mDeviceList = mDeviceList;
	}

	public ArrayList<App> getAppList() {
		return mAppList;
	}

	public void setAppList(ArrayList<App> mAppList) {
		this.mAppList = mAppList;
	}

	public ArrayList<Function> getFunctionList() {
		return mFunctionList;
	}

	public void setFunctionList(ArrayList<Function> mFunctionList) {
		this.mFunctionList = mFunctionList;
	}

	public ArrayList<Measurement> getMeasurementList() {
		return mMeasurementList;
	}

	public void setMeasurementList(ArrayList<Measurement> mMeasurementList) {
		this.mMeasurementList = mMeasurementList;
	}

	public ArrayList<MeasurementData> getMeasurementDataList() {
		return mMeasurementDataList;
	}

	public void setMeasurementDataList(ArrayList<MeasurementData> mMeasurementDataList) {
		this.mMeasurementDataList = mMeasurementDataList;
	}
}
