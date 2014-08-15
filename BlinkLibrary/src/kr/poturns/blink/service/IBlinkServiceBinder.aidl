package kr.poturns.blink.service;

import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.BlinkLog;

interface IBlinkServiceBinder {
	void registerApplicationInfo(String device,String app);
	SystemDatabaseObject obtainSystemDatabase(String device,String app);
	List<SystemDatabaseObject> obtainSystemDatabaseAll();
	void registerSystemDatabase(inout SystemDatabaseObject mSystemDatabaseObject);
	void registerMeasurementData(inout SystemDatabaseObject mSystemDatabaseObject,String ClassName,String JsonObj);
	String obtainMeasurementData(String ClassName,String DateTimeFrom,String DateTimeTo,int ContainType);
	List<MeasurementData> obtainMeasurementDataById(inout List<Measurement> mMeasurementList,String DateTimeFrom,String DateTimeTo);
	void registerLog(String Device,String App,int Type,String Content);
	List<BlinkLog> obtainLog(String Device,String App,int Type,String DateTimeFrom,String DateTimeTo);
}
