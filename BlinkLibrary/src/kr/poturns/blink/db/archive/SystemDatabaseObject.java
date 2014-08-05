package kr.poturns.blink.db.archive;

import java.lang.reflect.Field;
import java.util.ArrayList;

import kr.poturns.blink.util.ClassUtil;

import android.util.Log;


public class SystemDatabaseObject  {
	private final String tag = "SystemDatabaseObject";
	
	public boolean isExist;
	public DeviceAppList mDeviceAppList;
	public ArrayList<DeviceAppFunction> mDeviceAppFunctionList;
	public ArrayList<DeviceAppMeasurement> mDeviceAppMeasurementList;

	public SystemDatabaseObject() {
		onCreate();
	}
	
	public void onCreate(){
		isExist = false;
		mDeviceAppList = new DeviceAppList();
		mDeviceAppFunctionList = new ArrayList<DeviceAppFunction>();
		mDeviceAppMeasurementList = new ArrayList<DeviceAppMeasurement>();
	}
	
	public void addDeviceAppFunction(String Function,String Description){
		mDeviceAppFunctionList.add(new DeviceAppFunction(Function,Description));
	}
	public void addDeviceAppMeasurement(String Measurement,String Type,String Description){
		mDeviceAppMeasurementList.add(new DeviceAppMeasurement(Measurement,Type,Description));
	}
	
	public MeasurementData obtainMeasurementData(String Measurement){
		for(int i=0;i<mDeviceAppMeasurementList.size();i++){
			if(mDeviceAppMeasurementList.get(i).Measurement.contentEquals(Measurement)){
				return mDeviceAppMeasurementList.get(i).obtainMeasurement();
			}
		}
		return null;
	}
	
	//Java reflect을 이용한 Measurement 추가 
	public void addDeviceAppMeasurement(Class<?> obj){
		Log.i(tag, "addDeviceAppMeasurement(Object obj)");
		Field[] mFields = obj.getFields();
		for(int i=0;i<mFields.length;i++){
			mDeviceAppMeasurementList.add(new DeviceAppMeasurement(ClassUtil.obtainFieldSchema(mFields[i]),mFields[i].getType().getName(),""));
		}
	}
	
	
	public String toString(){
		String ret = "";
		ret += mDeviceAppList.toString();
		for(int i=0;i<mDeviceAppFunctionList.size();i++){
			ret += mDeviceAppFunctionList.get(i).toString();
		}
		for(int i=0;i<mDeviceAppMeasurementList.size();i++){
			ret += mDeviceAppMeasurementList.get(i).toString();
		}
		return ret;
	}
}
