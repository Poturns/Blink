package kr.poturns.blink.internal;

import java.util.List;

import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.service.IBlinkServiceBinder;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author Yeonho.Kim
 *
 */
public final class BlinkLocalService extends BlinkLocalBaseService {
	private final String tag = "BlinkLocalService";
	public SqliteManager mSqliteManager = null;
	public static final String INTENT_ACTION_NAME = "";
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.i(tag, "onCreate");
		super.onCreate();
		mSqliteManager = SqliteManager.getSqliteManager(this);
	}
	
	class BlinkServiceBinder extends IBlinkServiceBinder.Stub {
		String mDeviceName, mPackageName, mAppName;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		@Override
		public SystemDatabaseObject obtainSystemDatabase(String DeviceName,
				String PackageName) throws RemoteException {
			// TODO Auto-generated method stub
			Log.i(tag, "obtainSystemDatabase");
			mSqliteManager.registerLog(DeviceName, PackageName, mSqliteManager.LOG_OBTAIN_SYSTEMDATABASE, "");
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
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return new BlinkServiceBinder().asBinder();
	}

}
