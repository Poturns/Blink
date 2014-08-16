package kr.poturns.blink.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class BlinkServiceManager {
	private final static String tag = "BlinkServiceManager";
	Context mContext = null;
	Intent intent = null;
	Gson gson = null;
	
	BlinkServiceListener mBlinkServiceListener = null;
	IBlinkServiceBinder mBlinkServiceBinder = null;
	BlinkServiceConnection mBlinkServiceConnection = null;
	
	ArrayList<Device> mDeviceList = new ArrayList<Device>();
	ArrayList<App> mAppList = new ArrayList<App>();
	
	String device = "";
	String app = "";
	
	public static final String SERVICE_NAME = "kr.poturns.blink.internal.BlinkLocalService";
	
	public BlinkServiceManager(Context context,BlinkServiceListener listener){
		mContext = context;
		mBlinkServiceListener = listener;
		intent = new Intent(SERVICE_NAME); 
		mBlinkServiceConnection = new BlinkServiceConnection(this);
		gson = new GsonBuilder().setPrettyPrinting().create();
		device = Build.MODEL;
		app = mContext.getPackageName();
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
		mSystemDatabaseObject.mApp.PackageName = app;
		mSystemDatabaseObject.mDevice.Device = device;
		try {
				mBlinkServiceBinder.registerSystemDatabase(mSystemDatabaseObject);
				return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public SystemDatabaseObject obtainSystemDatabase(){
		return obtainSystemDatabase(device,app);
	}
	
	public SystemDatabaseObject obtainSystemDatabase(String device,String app){
		try {
			return mBlinkServiceBinder.obtainSystemDatabase(device, app);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public List<SystemDatabaseObject> obtainSystemDatabaseAll(){
		try {
			return mBlinkServiceBinder.obtainSystemDatabaseAll();
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
			mBlinkServiceBinder.registerMeasurementData(mSystemDatabaseObject, ClassName,jsonObj);
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
			String json = mBlinkServiceBinder.obtainMeasurementData(ClassName, DateTimeFrom, DateTimeTo, ContainType);
			return gson.fromJson(json,type);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public List<MeasurementData> obtainMeasurementData(List<Measurement> mDeviceAppMeasurementList,String DateTimeFrom,String DateTimeTo){
		try{
			return mBlinkServiceBinder.obtainMeasurementDataById(mDeviceAppMeasurementList, DateTimeFrom, DateTimeTo);
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
			mBlinkServiceBinder.registerLog(Device, App, Type, Content);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public List<BlinkLog> obtainLog(String Device,String App,int Type,String DateTimeFrom,String DateTimeTo){
		try {
			return mBlinkServiceBinder.obtainLog(Device, App, Type, DateTimeFrom, DateTimeTo);
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
}
