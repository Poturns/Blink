package kr.poturns.blink.internal.comm;

import java.util.HashMap;
import java.util.List;

import kr.poturns.blink.db.BlinkDatabaseManager;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.internal.BlinkLocalService;
import kr.poturns.blink.internal.ConnectionSupportBinder;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
<<<<<<< HEAD
 * 
 * @author Jiwon.Kim
 *
=======
 * 서비스에 어떻게 요청할 지 정하며 Log를 남긴다.
 * 만약 다른 디바이스에 해당하는 데이터면 블루투스쪽으로 넘겨야 한다.
 * 다른 디바이스와 통신이 필요한 매서드는 두 가지이다.
 * 1. obtainMeasurementData
 * 2. startFunction
 * @author Jiwon
>>>>>>> refs/remotes/origin/database
 */
public class BlinkSupportBinder extends ConnectionSupportBinder {
	private final String tag = "BlinkDatabaseBinder";
	
	public static final int REQUEST_TYPE_IN_DEVICE = 1;
	public static final int REQUEST_TYPE_OUT_DEVICE = 2;
	public static final int REQUEST_TYPE_DUAL_DEVICE = 3;
	
	String mDeviceName, mPackageName, mAppName;
	int requestPolicy = REQUEST_TYPE_IN_DEVICE;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	BlinkDatabaseManager mBlinkDatabaseManager;
	HashMap<Integer, CallbackData> CALLBACK_DATA_MAP = new HashMap<Integer, CallbackData>();
	
	public BlinkSupportBinder(BlinkLocalService context) throws Exception {
		super(context);
		mBlinkDatabaseManager = new BlinkDatabaseManager(context);
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
	public void setRequestPolicy(int RequestType){
		requestPolicy = RequestType;
	}
	
	@Override
	public final boolean registerCallback(IInternalEventCallback callback) throws RemoteException {
		if (callback != null){
			if(CONTEXT.CALLBACK_MAP.get(mPackageName)!=null){
				return CONTEXT.CALLBACK_MAP.get(mPackageName).register(callback);
			}
		}
		return false;
	}
	
	@Override
	public final boolean unregisterCallback(IInternalEventCallback callback) throws RemoteException {
		if (callback != null){
			if(CONTEXT.CALLBACK_MAP.get(mPackageName)!=null){
				return CONTEXT.CALLBACK_MAP.get(mPackageName).register(callback);
			}
		}
		return false;
	}
	
	/**
	 * 클라이언트로 콜백해주는 함수
	 * @param ClassName
	 * @param data
	 */
	public void callbackData(int responseCode,String data){
		if(CONTEXT.CALLBACK_MAP.get(mPackageName)==null)return;
		
		CallbackData mCallbackData = CALLBACK_DATA_MAP.get(responseCode);
		if(mCallbackData==null){
			mCallbackData = new CallbackData();
		}
		mCallbackData.OutDeviceData = data;
		
		int N = CONTEXT.CALLBACK_MAP.get(mPackageName).beginBroadcast();
		for(int i=0;i<N;i++){
			try {
				CONTEXT.CALLBACK_MAP.get(mPackageName).getBroadcastItem(i).onReceiveData(responseCode, mCallbackData);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * DeviceName과 PackageName을 기준으로 SystemDatabaseObject를 얻어오는 매서드
	 */
	@Override
	public SystemDatabaseObject obtainSystemDatabase(String DeviceName,
			String PackageName) throws RemoteException {
		// TODO Auto-generated method stub
		Log.i(tag, "obtainSystemDatabase");
		mBlinkDatabaseManager.registerLog(DeviceName, PackageName, SqliteManager.LOG_OBTAIN_SYSTEMDATABASE, "");
		SystemDatabaseObject sdo = mBlinkDatabaseManager.obtainSystemDatabase(DeviceName, PackageName);
		Log.i(tag, sdo.toString());
		return mBlinkDatabaseManager.obtainSystemDatabase(DeviceName, PackageName);
	}

	/**
	 * SystemDatabaseObject를 데이터베이스 등록하는 매서드
	 */
	@Override
	public void registerSystemDatabase(
			SystemDatabaseObject mSystemDatabaseObject)
			throws RemoteException {
		// TODO Auto-generated method stub
		Log.i(tag, "registerSystemDatabase");
		mBlinkDatabaseManager.registerLog(mDeviceName, mPackageName, mBlinkDatabaseManager.LOG_REGISTER_SYSTEMDATABASE, "");
		mBlinkDatabaseManager.registerSystemDatabase(mSystemDatabaseObject);
	}

	@Override
	public void registerMeasurementData(
			SystemDatabaseObject mSystemDatabaseObject, String ClassName,
			String JsonObj) throws RemoteException {
		// TODO Auto-generated method stub
		mBlinkDatabaseManager.registerLog(mDeviceName, mPackageName, mBlinkDatabaseManager.LOG_REGISTER_MEASRUEMENT, "");
		try{
			Class<?> mClass = Class.forName(ClassName);
			Object obj = new Gson().fromJson(JsonObj, mClass);
			mBlinkDatabaseManager.registerMeasurementData(mSystemDatabaseObject, obj);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 블루투스 통신이 필요할 수 있는 매서드
	 */
	@Override
	public String obtainMeasurementData(String ClassName,
			String DateTimeFrom, String DateTimeTo, int ContainType,int requestCode)
			throws RemoteException {
		// TODO Auto-generated method stub
		// Check need bluetooth communicate
		mBlinkDatabaseManager.registerLog(mDeviceName, mPackageName, mBlinkDatabaseManager.LOG_OBTAIN_MEASUREMENT, ClassName);
		
		CallbackData mCallbackData = new CallbackData();
		
		//내부 디바이스 데이터
		try{
			Class<?> mClass = Class.forName(ClassName);
			Log.i(tag, "class name : "+mClass.getName());
			mCallbackData.InDeviceData = mBlinkDatabaseManager.obtainMeasurementData(mClass, DateTimeFrom, DateTimeTo, ContainType);
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
		
		//외부 디바이스에서만 데이터 검색
		if(requestPolicy==REQUEST_TYPE_OUT_DEVICE){
			if(!mBlinkDatabaseManager.checkInDevice(ClassName)){
				//요청하는 코드 추가				
				
				
			}else {
				callbackData(requestCode, null);
			}
			return null;
		}
		//외부와 내부 디바이스 모두에서 데이터 검색
		else if(requestPolicy==REQUEST_TYPE_DUAL_DEVICE){
			//나중에 콜백될 때 요청 코드에 따라서 내부 디바이스 데이터를 합쳐서 콜백
			CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
			//찾으려는 데이터가 다른 디바이스에 있는 경우
			if(!mBlinkDatabaseManager.checkInDevice(ClassName)){
				//요청하는 코드 추가
				
				
			}
			//찾으려는 데이터가 다른 디바이스에 없는 경우
			else {
				callbackData(requestCode, null); 
			}
			return null;
		}
		//내부 디바이스에서만 데이터 검색
		else {
			return mCallbackData.InDeviceData;
		}
	}
	
	
	@Override
	public List<SystemDatabaseObject> obtainSystemDatabaseAll()
			throws RemoteException {
		// TODO Auto-generated method stub
		mBlinkDatabaseManager.registerLog(mDeviceName, mPackageName, mBlinkDatabaseManager.LOG_OBTAIN_SYSTEMDATABASE, "ALL");
		return mBlinkDatabaseManager.obtainSystemDatabase();
	}

	/**
	 * 블루투스 통신이 필요할 수 있는 매서드
	 */
	@Override
	public List<MeasurementData> obtainMeasurementDataById(
			List<Measurement> mMeasurementList,
			String DateTimeFrom, String DateTimeTo,int requestCode) throws RemoteException {
		// TODO Auto-generated method stub
		// Check need bluetooth communicate
		mBlinkDatabaseManager.registerLog(mDeviceName, mPackageName, mBlinkDatabaseManager.LOG_OBTAIN_MEASUREMENT, "By Id");
		
		CallbackData mCallbackData = new CallbackData();
		
		//내부 디바이스 데이터
		List<MeasurementData> InDeviceData = mBlinkDatabaseManager.obtainMeasurementData(mMeasurementList, DateTimeFrom, DateTimeTo);
		mCallbackData.InDeviceData =  gson.toJson(InDeviceData);
		
		//외부 디바이스에서만 데이터 검색
		if(requestPolicy==REQUEST_TYPE_OUT_DEVICE){
			if(!mBlinkDatabaseManager.checkInDevice(mMeasurementList)){
				//요청하는 코드 추가				
				
				
			}else {
				callbackData(requestCode, null);
			}
			return null;
		}
		//외부와 내부 디바이스 모두에서 데이터 검색
		else if(requestPolicy==REQUEST_TYPE_DUAL_DEVICE){
			//나중에 콜백될 때 요청 코드에 따라서 내부 디바이스 데이터를 합쳐서 콜백
			CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
			//찾으려는 데이터가 다른 디바이스에 있는 경우
			if(!mBlinkDatabaseManager.checkInDevice(mMeasurementList)){
				//요청하는 코드 추가
				
				
			}
			//찾으려는 데이터가 다른 디바이스에 없는 경우
			else {
				callbackData(requestCode, null); 
			}
			return null;
		}
		//내부 디바이스에서만 데이터 검색
		else {
			return InDeviceData;
		}
	}

	@Override
	public List<BlinkLog> obtainLog(String DeviceName, String PackageName,
			int Type, String DateTimeFrom, String DateTimeTo)
			throws RemoteException {
		// TODO Auto-generated method stub
		return mBlinkDatabaseManager.obtainLog(DeviceName, PackageName, Type, DateTimeFrom, DateTimeTo);
	}

	@Override
	public void registerLog(String DeviceName, String PackageName, int Type,
			String Content) throws RemoteException {
		// TODO Auto-generated method stub
		mBlinkDatabaseManager.registerLog(DeviceName, PackageName, Type, Content);
	}

	/**
	 * 블루투스 통신이 필요할 수 있는 매서드
	 */
	@Override
	public void startFunction(Function mFunction,int requestCode) throws RemoteException {
		// TODO Auto-generated method stub
		// Check need bluetooth communicate
		CallbackData mCallbackData = new CallbackData();
		
		if(mFunction.Type==Function.TYPE_ACTIVITY)
			CONTEXT.startActivity(new Intent(mFunction.Action).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		else if(mFunction.Type==Function.TYPE_SERIVCE)
			CONTEXT.startService(new Intent(mFunction.Action));
		else if(mFunction.Type==Function.TYPE_BROADCAST)
			CONTEXT.sendBroadcast(new Intent(mFunction.Action));
		
		mCallbackData.InDeviceData = "success";
		
		//외부 디바이스에서만 데이터 검색
		if(requestPolicy==REQUEST_TYPE_OUT_DEVICE){
			if(!mBlinkDatabaseManager.checkInDevice(mFunction)){
				//요청하는 코드 추가				
				
				
			}else {
				callbackData(requestCode, null);
			}
		}
		//외부와 내부 디바이스 모두에서 데이터 검색
		else if(requestPolicy==REQUEST_TYPE_DUAL_DEVICE){
			//나중에 콜백될 때 요청 코드에 따라서 내부 디바이스 데이터를 합쳐서 콜백
			CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
			//찾으려는 데이터가 다른 디바이스에 있는 경우
			if(!mBlinkDatabaseManager.checkInDevice(mFunction)){
				//요청하는 코드 추가
				
				
			}
			//찾으려는 데이터가 다른 디바이스에 없는 경우
			else {
				callbackData(requestCode, null); 
			}
		}
		//내부 디바이스에서만 데이터 검색
		else {
			callbackData(requestCode, null);
		}
	}

	
	/**
	 * BlinkDatabaseManager 매소드
	 * 해당 매소드들은 내부 디바이스에서만 동작한다.
	 */
	@Override
    public IInternalOperationSupport queryDevice(String where)
            throws RemoteException {
	    // TODO Auto-generated method stub
		mBlinkDatabaseManager.queryDevice(where);
	    return this;
    }

	@Override
    public IInternalOperationSupport queryApp(String where)
            throws RemoteException {
	    // TODO Auto-generated method stub
		mBlinkDatabaseManager.queryApp(where);
	    return this;
    }

	@Override
    public IInternalOperationSupport queryFunction(String where)
            throws RemoteException {
	    // TODO Auto-generated method stub
		mBlinkDatabaseManager.queryFunction(where);
	    return this;
    }

	@Override
    public IInternalOperationSupport queryMeasurement(String where)
            throws RemoteException {
	    // TODO Auto-generated method stub
		mBlinkDatabaseManager.queryMeasurement(where);
	    return this;
    }

	@Override
    public IInternalOperationSupport queryMeasurementData(String where,int RequestCode)
            throws RemoteException {
	    // TODO Auto-generated method stub
		mBlinkDatabaseManager.queryMeasurementData(where);
	    return this;
    }

	@Override
    public boolean checkInDeviceByMeasureList(List<Measurement> mMeasurementList)
            throws RemoteException {
	    // TODO Auto-generated method stub
	    return mBlinkDatabaseManager.checkInDevice(mMeasurementList);
    }

	@Override
    public boolean checkInDeviceByFunction(Function mFunction)
            throws RemoteException {
	    // TODO Auto-generated method stub
		return mBlinkDatabaseManager.checkInDevice(mFunction);
    }

	@Override
    public boolean checkInDeviceByClass(String ClassName)
            throws RemoteException {
	    // TODO Auto-generated method stub
		return mBlinkDatabaseManager.checkInDevice(ClassName);
    }

	@Override
    public List<Device> getDeviceList() throws RemoteException {
	    // TODO Auto-generated method stub
		return mBlinkDatabaseManager.getDeviceList();
    }

	@Override
    public void setDeviceList(List<Device> mDeviceList) throws RemoteException {
	    // TODO Auto-generated method stub
		mBlinkDatabaseManager.setDeviceList(mDeviceList);
    }

	@Override
    public List<App> getAppList() throws RemoteException {
	    // TODO Auto-generated method stub
		return mBlinkDatabaseManager.getAppList();
    }

	@Override
    public void setAppList(List<App> mAppList) throws RemoteException {
	    // TODO Auto-generated method stub
		mBlinkDatabaseManager.setAppList(mAppList);
    }

	@Override
    public List<Function> getFunctionList() throws RemoteException {
	    // TODO Auto-generated method stub
		return mBlinkDatabaseManager.getFunctionList();
    }

	@Override
    public void setFunctionList(List<Function> mFunctionList)
            throws RemoteException {
	    // TODO Auto-generated method stub
		mBlinkDatabaseManager.setFunctionList(mFunctionList);
    }

	@Override
    public List<Measurement> getMeasurementList() throws RemoteException {
	    // TODO Auto-generated method stub
		return mBlinkDatabaseManager.getMeasurementList();
    }

	@Override
    public void setMeasurementList(List<Measurement> mMeasurementList)
            throws RemoteException {
	    // TODO Auto-generated method stub
		mBlinkDatabaseManager.setMeasurementList(mMeasurementList);
    }

	@Override
    public List<MeasurementData> getMeasurementDataList()
            throws RemoteException {
	    // TODO Auto-generated method stub
		return mBlinkDatabaseManager.getMeasurementDataList();
    }

	@Override
    public void setMeasurementDataList(
            List<MeasurementData> mMeasurementDataList) throws RemoteException {
	    // TODO Auto-generated method stub
		mBlinkDatabaseManager.setMeasurementDataList(mMeasurementDataList);
    }
}
