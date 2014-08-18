package kr.poturns.blink.service;

import java.util.List;

import kr.poturns.blink.db.BlinkDatabaseManager;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BlinkDatabaseServiceBinder extends IBlinkDatabaseServiceBinder.Stub {
	private final String tag = "BlinkDatabaseBinder";
	String mDeviceName, mPackageName, mAppName;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	SqliteManager mSqliteManager;
	BlinkDatabaseManager mBlinkDatabaseManager = new BlinkDatabaseManager(mSqliteManager);
	Context context = null;
	public BlinkDatabaseServiceBinder(Context context,SqliteManager mSqliteManager){
		this.mSqliteManager = mSqliteManager;
		this.context = context;
	}
	@Override
	public SystemDatabaseObject obtainSystemDatabase(String DeviceName,
			String PackageName) throws RemoteException {
		// TODO Auto-generated method stub
		Log.i(tag, "obtainSystemDatabase");
		mSqliteManager.registerLog(DeviceName, PackageName, mSqliteManager.LOG_OBTAIN_SYSTEMDATABASE, "");
		SystemDatabaseObject sdo = mSqliteManager.obtainSystemDatabase(DeviceName, PackageName);
		Log.i(tag, sdo.toString());
		return mSqliteManager.obtainSystemDatabase(DeviceName, PackageName);
	}

	@Override
	public void registerSystemDatabase(
			SystemDatabaseObject mSystemDatabaseObject)
			throws RemoteException {
		// TODO Auto-generated method stub
		Log.i(tag, "registerSystemDatabase");
		mSqliteManager.registerLog(mDeviceName, mPackageName, mSqliteManager.LOG_REGISTER_SYSTEMDATABASE, "");
		mSqliteManager.registerSystemDatabase(mSystemDatabaseObject);
	}

	@Override
	public void registerMeasurementData(
			SystemDatabaseObject mSystemDatabaseObject, String ClassName,
			String JsonObj) throws RemoteException {
		// TODO Auto-generated method stub
		mSqliteManager.registerLog(mDeviceName, mPackageName, mSqliteManager.LOG_REGISTER_MEASRUEMENT, "");
		try{
			Class<?> mClass = Class.forName(ClassName);
			Object obj = new Gson().fromJson(JsonObj, mClass);
			mSqliteManager.registerMeasurementData(mSystemDatabaseObject, obj);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public String obtainMeasurementData(String ClassName,
			String DateTimeFrom, String DateTimeTo, int ContainType)
			throws RemoteException {
		// TODO Auto-generated method stub
		mSqliteManager.registerLog(mDeviceName, mPackageName, mSqliteManager.LOG_OBTAIN_MEASUREMENT, ClassName);
		try{
			Class<?> mClass = Class.forName(ClassName);
			Log.i(tag, "class name : "+mClass.getName());
			return mSqliteManager.obtainMeasurementData(mClass, DateTimeFrom, DateTimeTo, ContainType); 
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<SystemDatabaseObject> obtainSystemDatabaseAll()
			throws RemoteException {
		// TODO Auto-generated method stub
		mSqliteManager.registerLog(mDeviceName, mPackageName, mSqliteManager.LOG_OBTAIN_SYSTEMDATABASE, "ALL");
		return mSqliteManager.obtainSystemDatabase();
	}

	@Override
	public List<MeasurementData> obtainMeasurementDataById(
			List<Measurement> mMeasurementList,
			String DateTimeFrom, String DateTimeTo) throws RemoteException {
		// TODO Auto-generated method stub
		mSqliteManager.registerLog(mDeviceName, mPackageName, mSqliteManager.LOG_OBTAIN_MEASUREMENT, "By Id");
		return mSqliteManager.obtainMeasurementData(mMeasurementList, DateTimeFrom, DateTimeTo);
	}

	@Override
	public void registerApplicationInfo(String DeviceName, String PackageName, String AppName)
			throws RemoteException {
		// TODO Auto-generated method stub
		this.mDeviceName = DeviceName;
		this.mPackageName = PackageName;
		this.mAppName = AppName;
	}

	@Override
	public List<BlinkLog> obtainLog(String DeviceName, String PackageName,
			int Type, String DateTimeFrom, String DateTimeTo)
			throws RemoteException {
		// TODO Auto-generated method stub
		return mSqliteManager.obtainLog(DeviceName, PackageName, Type, DateTimeFrom, DateTimeTo);
	}

	@Override
	public void registerLog(String DeviceName, String PackageName, int Type,
			String Content) throws RemoteException {
		// TODO Auto-generated method stub
		mSqliteManager.registerLog(DeviceName, PackageName, Type, Content);
	}

	@Override
	public void startFunction(Function mFunction) throws RemoteException {
		// TODO Auto-generated method stub
		if(mFunction.Type==Function.TYPE_ACTIVITY)
			context.startActivity(new Intent(mFunction.Action).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		else if(mFunction.Type==Function.TYPE_SERIVCE)
			context.startService(new Intent(mFunction.Action));
		else if(mFunction.Type==Function.TYPE_BROADCAST)
			context.sendBroadcast(new Intent(mFunction.Action));
		
	}
}
