package kr.poturns.blink.db;

import java.util.ArrayList;

import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;

public class BlinkDatabaseManager {
	private ArrayList<SystemDatabaseObject> mSystemDatabaseObjectList = new ArrayList<SystemDatabaseObject>();
	private ArrayList<Device> mDeviceList = new ArrayList<Device>();
	private ArrayList<App> mAppList = new ArrayList<App>();
	private ArrayList<Function> mFunctionList = new ArrayList<Function>();
	private ArrayList<Measurement> mMeasurementList = new ArrayList<Measurement>();
	private ArrayList<MeasurementData> mMeasurementDataList = new ArrayList<MeasurementData>();
	private ArrayList<BlinkLog> mBlinkLogList = new ArrayList<BlinkLog>();
	
	SqliteManager mSqliteManager = null;
	public BlinkDatabaseManager(SqliteManager mSqliteManager){
		this.mSqliteManager = mSqliteManager;
	}
	
	public BlinkDatabaseManager queryDevice(String where){
		this.mDeviceList = this.mSqliteManager.obtainDeviceList(where);
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
				
		this.mAppList = this.mSqliteManager.obtainAppList(NewWhere);
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
				
		this.mFunctionList = this.mSqliteManager.obtainFunctionList(NewWhere);
		
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
				
		this.mMeasurementList = this.mSqliteManager.obtainMeasurementList(NewWhere);
		
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
				
		this.mMeasurementDataList = this.mSqliteManager.obtainMeasurementDataList(NewWhere);
		
		return this;
	}
	public BlinkDatabaseManager queryBlinkLog(){
		return this;
	}
	public BlinkDatabaseManager querySystemDatabaseObject(){
		return this;
	}
	
	
	
	
	/**
	 * getter , setter methods
	 */

	public ArrayList<SystemDatabaseObject> getSystemDatabaseObjectList() {
		return mSystemDatabaseObjectList;
	}

	public void setSystemDatabaseObjectList(
			ArrayList<SystemDatabaseObject> mSystemDatabaseObjectList) {
		this.mSystemDatabaseObjectList = mSystemDatabaseObjectList;
	}

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

	public ArrayList<BlinkLog> getBlinkLogList() {
		return mBlinkLogList;
	}

	public void setBlinkLogList(ArrayList<BlinkLog> mBlinkLogList) {
		this.mBlinkLogList = mBlinkLogList;
	}
}
