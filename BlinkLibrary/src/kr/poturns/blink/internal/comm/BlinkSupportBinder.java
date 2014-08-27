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
import kr.poturns.blink.internal.ServiceKeeper;
import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 서비스에 어떻게 요청할 지 정하며 Log를 남긴다.
 * 만약 다른 디바이스에 해당하는 데이터면 블루투스쪽으로 넘겨야 한다.
 * 다른 디바이스와 통신이 필요한 매서드는 두 가지이다.
 * 1. obtainMeasurementData
 * 2. startFunction
 * 
 * @author Jiwon.Kim
 */
public class BlinkSupportBinder extends ConnectionSupportBinder {
	private final String tag = "BlinkDatabaseBinder";
	
	private static final int REQUEST_TYPE_IN_DEVICE = 1;
	public static final int REQUEST_TYPE_OUT_DEVICE = 2;
	public static final int REQUEST_TYPE_DUAL_DEVICE = 3;
	
	String mDeviceName, mPackageName, mAppName;
	
	int requestPolicy = REQUEST_TYPE_DUAL_DEVICE;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	BlinkDatabaseManager mBlinkDatabaseManager;
	BlinkDevice mBlinkDevice;
	
	HashMap<Integer, CallbackData> CALLBACK_DATA_MAP = new HashMap<Integer, CallbackData>();
	
	public BlinkSupportBinder(BlinkLocalService context) throws Exception {
		super(context);
		mBlinkDatabaseManager = new BlinkDatabaseManager(context);
		mBlinkDevice = BlinkDevice.HOST;
		if(mBlinkDevice==null)Log.i(tag, "BlinkDevice.HOST : null");
	}

	@Override
	public void registerApplicationInfo(String PackageName, String AppName)
			throws RemoteException {
		// TODO Auto-generated method stub
		this.mPackageName = PackageName;
		this.mAppName = AppName;
		if(CONTEXT.CALLBACK_MAP.get(mPackageName)==null)
			CONTEXT.CALLBACK_MAP.put(mPackageName, new RemoteCallbackList<IInternalEventCallback>());
	}
	
	@Override
	public void setRequestPolicy(int requestPolicy){
		this.requestPolicy = requestPolicy;
	}
	
	@Override
	public BlinkDevice getBlinkDevice() throws RemoteException {
		// TODO Auto-generated method stub
		return mBlinkDevice;
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
				return CONTEXT.CALLBACK_MAP.get(mPackageName).unregister(callback);
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
	public void obtainMeasurementData(String ClassName,
			String DateTimeFrom, String DateTimeTo, int ContainType,int requestCode)
			throws RemoteException {
		// TODO Auto-generated method stub
		// Check need bluetooth communicate
		mBlinkDatabaseManager.registerLog(mDeviceName, mPackageName, mBlinkDatabaseManager.LOG_OBTAIN_MEASUREMENT, ClassName);
		CALLBACK_DATA_MAP.remove(requestCode);
		
		CallbackData mCallbackData = new CallbackData();
		
		try {
			Class<?> mClass = Class.forName(ClassName);
			
			//외부 디바이스에서만 데이터 검색
			if(requestPolicy==REQUEST_TYPE_OUT_DEVICE){
				if(!mBlinkDatabaseManager.checkInDevice(mClass)){
					//요청하는 코드 추가				
					callbackData(requestCode, null); 
					
				}else {
					callbackData(requestCode, null);
				}
			}
			//외부와 내부 디바이스 모두에서 데이터 검색
			else if(requestPolicy==REQUEST_TYPE_DUAL_DEVICE){
				//나중에 콜백될 때 요청 코드에 따라서 내부 디바이스 데이터를 합쳐서 콜백
				//내부 디바이스 데이터
				mCallbackData.InDeviceData = mBlinkDatabaseManager.obtainMeasurementData(mClass, DateTimeFrom, DateTimeTo, ContainType);
				CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
				//찾으려는 데이터가 다른 디바이스에 있는 경우
				if(!mBlinkDatabaseManager.checkInDevice(mClass)){
					//요청하는 코드 추가
					callbackData(requestCode, null); 
				}
				//찾으려는 데이터가 다른 디바이스에 없는 경우
				else {
					callbackData(requestCode, null); 
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			callbackData(requestCode, null);
		}
	}
	
	
	@Override
	public void obtainMeasurementDataById(
			List<Measurement> mMeasurementList,
			String DateTimeFrom, String DateTimeTo,int requestCode) throws RemoteException {
		// TODO Auto-generated method stub
		mBlinkDatabaseManager.registerLog(mDeviceName, mPackageName, mBlinkDatabaseManager.LOG_OBTAIN_MEASUREMENT, "By Id");
		CALLBACK_DATA_MAP.remove(requestCode);
		
		CallbackData mCallbackData = new CallbackData();
		
		//외부 디바이스에서만 데이터 검색
		if(requestPolicy==REQUEST_TYPE_OUT_DEVICE){
			if(!mBlinkDatabaseManager.checkInDevice(mMeasurementList)){
				//요청하는 코드 추가				
				callbackData(requestCode, null); 
				
			}else {
				callbackData(requestCode, null);
			}
		}
		//외부와 내부 디바이스 모두에서 데이터 검색
		else if(requestPolicy==REQUEST_TYPE_DUAL_DEVICE){
			//나중에 콜백될 때 요청 코드에 따라서 내부 디바이스 데이터를 합쳐서 콜백
			//내부 디바이스 데이터
			List<MeasurementData> InDeviceData = mBlinkDatabaseManager.obtainMeasurementData(mMeasurementList, DateTimeFrom, DateTimeTo);
			mCallbackData.InDeviceData =  gson.toJson(InDeviceData);
			CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
			//찾으려는 데이터가 다른 디바이스에 있는 경우
			if(!mBlinkDatabaseManager.checkInDevice(mMeasurementList)){
				//요청하는 코드 추가
				callbackData(requestCode, null); 
				
			}
			//찾으려는 데이터가 다른 디바이스에 없는 경우
			else {
				callbackData(requestCode, null); 
			}
		}
	}

	@Override
	public void startFunction(Function function,int requestCode) throws RemoteException {
		// TODO Auto-generated method stub
		// Check need bluetooth communicate
		CallbackData mCallbackData = new CallbackData();
		CALLBACK_DATA_MAP.remove(requestCode);
		
		//외부 디바이스에서만 데이터 검색
		if(requestPolicy==REQUEST_TYPE_OUT_DEVICE){
			Device device = mBlinkDatabaseManager.obtainDevice(function);
			App app = mBlinkDatabaseManager.obtainApp(function);
			//자신의 디바이스가 아니고 다른곳에 function이 존재하면
			if(device!=null && !device.MacAddress.contentEquals(mBlinkDevice.getAddress())){
				//요청하는 코드 추가	
				//target
//				device.MacAddress;
//				app.PackageName;
				//source
//				mBlinkDevice.getAddress();
//				mPackageName;
				//message
//				gson.toJson(function);
				
			}else {
				callbackData(requestCode, null);
			}
		}
		//외부와 내부 디바이스 모두에서 데이터 검색
		else if(requestPolicy==REQUEST_TYPE_DUAL_DEVICE){
			//나중에 콜백될 때 요청 코드에 따라서 내부 디바이스 데이터를 합쳐서 콜백
			CONTEXT.startFunction(function);
			mCallbackData.InDeviceData = "success";
			CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
			//찾으려는 데이터가 다른 디바이스에 있는 경우
			if(!mBlinkDatabaseManager.checkInDevice(function)){
				//요청하는 코드 추가
				Log.i(tag, "if");
				callbackData(requestCode, null); 
				
			}
			//찾으려는 데이터가 다른 디바이스에 없는 경우
			else {
				Log.i(tag, "else");
				callbackData(requestCode, null); 
			}
		}
	}

}
