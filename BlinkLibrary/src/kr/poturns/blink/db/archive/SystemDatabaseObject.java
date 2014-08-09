package kr.poturns.blink.db.archive;

import java.lang.reflect.Field;
import java.util.ArrayList;

import kr.poturns.blink.db.JsonManager;
import kr.poturns.blink.util.ClassUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


public class SystemDatabaseObject implements Parcelable {
	private final String tag = "SystemDatabaseObject";
	
	public boolean isExist;
	public DeviceApp mDeviceApp;
	public ArrayList<DeviceAppFunction> mDeviceAppFunctionList;
	public ArrayList<DeviceAppMeasurement> mDeviceAppMeasurementList;

	public SystemDatabaseObject() {
		onCreate();
	}
	
	public void onCreate(){
		isExist = false;
		mDeviceApp = new DeviceApp();
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
		ret += mDeviceApp.toString();
		for(int i=0;i<mDeviceAppFunctionList.size();i++){
			ret += mDeviceAppFunctionList.get(i).toString();
		}
		for(int i=0;i<mDeviceAppMeasurementList.size();i++){
			ret += mDeviceAppMeasurementList.get(i).toString();
		}
		return ret;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		ArrayList<SystemDatabaseObject> mSystemDatabaseObjectList = new ArrayList<SystemDatabaseObject>();
		mSystemDatabaseObjectList.add(this);
		dest.writeString(JsonManager.obtainJsonSystemDatabaseObject(mSystemDatabaseObjectList));
	}
	public static final Parcelable.Creator<SystemDatabaseObject> CREATOR = new Parcelable.Creator<SystemDatabaseObject>() {
		 public SystemDatabaseObject createFromParcel(Parcel in) {
		 	return new SystemDatabaseObject(in);
		 }
	        
		 public SystemDatabaseObject[] newArray( int size ) {
			 return new SystemDatabaseObject[size];
		 }
	};
	public SystemDatabaseObject(Parcel in){
		readFromParcel(in);
	}
	public void readFromParcel(Parcel in){
		ArrayList<SystemDatabaseObject> mSystemDatabaseObjectList = JsonManager.obtainJsonSystemDatabaseObject(in.readString());
		CopyFromOtherObject(mSystemDatabaseObjectList.get(0));
	}
	public void CopyFromOtherObject(SystemDatabaseObject mSystemDatabaseObject){
		this.isExist = mSystemDatabaseObject.isExist;
		this.mDeviceApp = mSystemDatabaseObject.mDeviceApp;
		this.mDeviceAppFunctionList = mSystemDatabaseObject.mDeviceAppFunctionList;
		this.mDeviceAppMeasurementList = mSystemDatabaseObject.mDeviceAppMeasurementList;
	}
}
