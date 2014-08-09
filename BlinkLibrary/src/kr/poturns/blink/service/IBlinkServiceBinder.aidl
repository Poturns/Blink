package kr.poturns.blink.service;

import kr.poturns.blink.db.archive.SystemDatabaseObject;

interface IBlinkServiceBinder {
	SystemDatabaseObject obtainSystemDatabase(String device,String app);
	List<SystemDatabaseObject> obtainSystemDatabaseAll();
	void registerSystemDatabase(inout SystemDatabaseObject mSystemDatabaseObject);
	void registerMeasurementData(inout SystemDatabaseObject mSystemDatabaseObject,String ClassName,String JsonObj);
	String obtainMeasurementData(String ClassName,String DateTimeFrom,String DateTimeTo,int ContainType);
}