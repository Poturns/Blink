package kr.poturns.blink.service;

import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Function;

interface IBlinkDatabaseServiceBinder {
	void registerApplicationInfo(String DeviceName,String PackageName,String AppName);
	SystemDatabaseObject obtainSystemDatabase(String DeviceName,String PackageName);
	List<SystemDatabaseObject> obtainSystemDatabaseAll();
	void registerSystemDatabase(inout SystemDatabaseObject mSystemDatabaseObject);
	void registerMeasurementData(inout SystemDatabaseObject mSystemDatabaseObject,String ClassName,String JsonObj);
	String obtainMeasurementData(String ClassName,String DateTimeFrom,String DateTimeTo,int ContainType);
	List<MeasurementData> obtainMeasurementDataById(inout List<Measurement> mMeasurementList,String DateTimeFrom,String DateTimeTo);
	void registerLog(String Device,String App,int Type,String Content);
	List<BlinkLog> obtainLog(String Device,String App,int Type,String DateTimeFrom,String DateTimeTo);
	void startFunction(inout Function mFunction);
}
