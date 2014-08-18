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
	
	public void queryDevice(){
		
	}
	public void queryApp(){
		
	}
	public void queryFunction(){
		
	}
	public void queryMeasurement(){
		
	}
	public void queryMeasurementData(){
		
	}
	public void queryBlinkLog(){
		
	}
	public void querySystemDatabaseObject(){
		
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
