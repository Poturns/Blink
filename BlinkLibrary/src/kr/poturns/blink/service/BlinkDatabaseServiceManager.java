package kr.poturns.blink.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class BlinkDatabaseServiceManager {
	private final static String tag = "BlinkServiceManager";
	Context mContext = null;
	Intent intent = null;
	Gson gson = null;
	
	BlinkDatabaseServiceListener mBlinkServiceListener = null;
	IBlinkDatabaseServiceBinder mBlinkDatabaseServiceBinder = null;
	BlinkDatabaseServiceConnection mBlinkServiceConnection = null;
	
	ArrayList<Device> mDeviceList = new ArrayList<Device>();
	ArrayList<App> mAppList = new ArrayList<App>();
	
	String mDeviceName = "";
	String mPackageName = "";
	String mAppName = "";
	
	public static final String SERVICE_NAME = "kr.poturns.blink.internal.BlinkLocalService";
	
	public BlinkDatabaseServiceManager(Context context,BlinkDatabaseServiceListener listener){
		mContext = context;
		mBlinkServiceListener = listener;
		intent = new Intent(SERVICE_NAME); 
		mBlinkServiceConnection = new BlinkDatabaseServiceConnection(this);
		gson = new GsonBuilder().setPrettyPrinting().create();
		mDeviceName = Build.MODEL;
		mPackageName = mContext.getPackageName();
		mAppName = mContext.getApplicationInfo().loadLabel(mContext.getPackageManager()).toString();
//		device = "Device3";
//		app = "App6";
	}
	
	public void connectService(){
		mContext.startService(intent);
		mContext.bindService(intent, mBlinkServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void closeService(){
		if(mBlinkServiceConnection!=null)mContext.unbindService(mBlinkServiceConnection);
	}
	
	public boolean registerSystemDatabase(SystemDatabaseObject mSystemDatabaseObject){
		mSystemDatabaseObject.mApp.PackageName = mPackageName;
		mSystemDatabaseObject.mApp.AppName = mAppName;
		mSystemDatabaseObject.mDevice.Device = mDeviceName;
		try {
				mBlinkDatabaseServiceBinder.registerSystemDatabase(mSystemDatabaseObject);
				return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public SystemDatabaseObject obtainSystemDatabase(){
		return obtainSystemDatabase(mDeviceName,mPackageName);
	}
	
	public SystemDatabaseObject obtainSystemDatabase(String DeviceName,String PackageName){
		try {
			return mBlinkDatabaseServiceBinder.obtainSystemDatabase(DeviceName, PackageName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public List<SystemDatabaseObject> obtainSystemDatabaseAll(){
		try {
			return mBlinkDatabaseServiceBinder.obtainSystemDatabaseAll();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean registerMeasurementData(SystemDatabaseObject mSystemDatabaseObject,Object obj){
		String ClassName = obj.getClass().getName();
		String jsonObj = gson.toJson(obj);
		try {
			mBlinkDatabaseServiceBinder.registerMeasurementData(mSystemDatabaseObject, ClassName,jsonObj);
			return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public <Object> Object obtainMeasurementData(Class<?> obj,Type type){
		return obtainMeasurementData(obj,null,null,SqliteManager.CONTAIN_DEFAULT,type);
	}
	
	public <Object> Object obtainMeasurementData(Class<?> obj,int ContainType,Type type){
		return obtainMeasurementData(obj,null,null,ContainType,type);
	}
 	public <Object> Object obtainMeasurementData(Class<?> obj,String DateTimeFrom,String DateTimeTo,int ContainType,Type type){
		String ClassName = obj.getName();
		try{
			String json = mBlinkDatabaseServiceBinder.obtainMeasurementData(ClassName, DateTimeFrom, DateTimeTo, ContainType);
			return gson.fromJson(json,type);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public List<MeasurementData> obtainMeasurementData(List<Measurement> mDeviceAppMeasurementList,String DateTimeFrom,String DateTimeTo){
		try{
			return mBlinkDatabaseServiceBinder.obtainMeasurementDataById(mDeviceAppMeasurementList, DateTimeFrom, DateTimeTo);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public int removeMeasurementData(Class<?> obj, String DateTimeFrom, String DateTimeTo){
		int ret = 0;
		return ret;
	}
	
	/**
	 * Log Methods
	 */
	public void registerLog(String Device,String App,int Type,String Content){
		try {
			mBlinkDatabaseServiceBinder.registerLog(Device, App, Type, Content);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public List<BlinkLog> obtainLog(String Device,String App,int Type,String DateTimeFrom,String DateTimeTo){
		try {
			return mBlinkDatabaseServiceBinder.obtainLog(Device, App, Type, DateTimeFrom, DateTimeTo);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<BlinkLog> obtainLog(String Device,String App,String DateTimeFrom,String DateTimeTo){
		return obtainLog(Device,App,-1,DateTimeFrom,DateTimeTo);
	}
	public List<BlinkLog> obtainLog(String Device,String DateTimeFrom,String DateTimeTo){
		return obtainLog(Device,null,-1,DateTimeFrom,DateTimeTo);
	}
	public List<BlinkLog> obtainLog(String DateTimeFrom,String DateTimeTo){
		return obtainLog(null,null,-1,DateTimeFrom,DateTimeTo);
	}
	public List<BlinkLog> obtainLog(){
		return obtainLog(null,null,-1,null,null);
	}
	
	public void startFuntion(Function mFunction){
		try {
			mBlinkDatabaseServiceBinder.startFunction(mFunction);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
