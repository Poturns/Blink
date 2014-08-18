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
	public Device mDevice;
	public App mApp;
	public ArrayList<Function> mFunctionList;
	public ArrayList<Measurement> mMeasurementList;

	public SystemDatabaseObject() {
		onCreate();
	}
	
	public void onCreate(){
		isExist = false;
		mDevice = new Device();
		mApp = new App();
		mFunctionList = new ArrayList<Function>();
		mMeasurementList = new ArrayList<Measurement>();
	}
	
	public void addFunction(String Function,String Description,String Action,int Type){
		mFunctionList.add(new Function(Function,Description, Action, Type));
	}
	public void addMeasurement(String Measurement,String Type,String Description){
		mMeasurementList.add(new Measurement(Measurement,Type,Description));
	}
	
	public MeasurementData obtainMeasurementData(String Measurement){
		for(int i=0;i<mMeasurementList.size();i++){
			if(mMeasurementList.get(i).Measurement.contentEquals(Measurement)){
				return mMeasurementList.get(i).obtainMeasurement();
			}
		}
		return null;
	}
	
	//Java reflect을 이용한 Measurement 추가 
	public void addMeasurement(Class<?> obj){
		Log.i(tag, "addDeviceAppMeasurement(Object obj)");
		Field[] mFields = obj.getFields();
		for(int i=0;i<mFields.length;i++){
			mMeasurementList.add(new Measurement(ClassUtil.obtainFieldSchema(mFields[i]),mFields[i].getType().getName(),""));
		}
	}
	
	
	public String toString(){
		String ret = "";
		ret += mDevice.toString();
		ret += mApp.toString();
		for(int i=0;i<mFunctionList.size();i++){
			ret += mFunctionList.get(i).toString();
		}
		for(int i=0;i<mMeasurementList.size();i++){
			ret += mMeasurementList.get(i).toString();
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
		dest.writeString(JsonManager.gson.toJson(this));
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
		SystemDatabaseObject mSystemDatabaseObject = JsonManager.gson.fromJson(in.readString(),SystemDatabaseObject.class);
		CopyFromOtherObject(mSystemDatabaseObject);
	}
	public void CopyFromOtherObject(SystemDatabaseObject mSystemDatabaseObject){
		this.isExist = mSystemDatabaseObject.isExist;
		this.mDevice = mSystemDatabaseObject.mDevice;
		this.mApp = mSystemDatabaseObject.mApp;
		this.mFunctionList = mSystemDatabaseObject.mFunctionList;
		this.mMeasurementList = mSystemDatabaseObject.mMeasurementList;
	}
}
