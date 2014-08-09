package kr.poturns.blink.service;

import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.DeviceAppMeasurement;

interface IBlinkServiceBinder {
	void registerApplicationInfo(String device,String app);
	SystemDatabaseObject obtainSystemDatabase(String device,String app);
	List<SystemDatabaseObject> obtainSystemDatabaseAll();
	void registerSystemDatabase(inout SystemDatabaseObject mSystemDatabaseObject);
	void registerMeasurementData(inout SystemDatabaseObject mSystemDatabaseObject,String ClassName,String JsonObj);
	String obtainMeasurementData(String ClassName,String DateTimeFrom,String DateTimeTo,int ContainType);
	List<MeasurementData> obtainMeasurementDataById(inout List<DeviceAppMeasurement> mDeviceAppMeasurementList,String DateTimeFrom,String DateTimeTo);
}