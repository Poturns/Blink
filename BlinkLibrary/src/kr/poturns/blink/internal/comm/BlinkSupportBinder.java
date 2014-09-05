package kr.poturns.blink.internal.comm;

import java.util.HashMap;
import java.util.List;

import kr.poturns.blink.db.BlinkDatabaseManager;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.db.archive.DatabaseMessage;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.internal.BlinkLocalService;
import kr.poturns.blink.internal.ConnectionSupportBinder;
import kr.poturns.blink.internal.ServiceKeeper;
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
		ServiceKeeper mServiceKeeper = ServiceKeeper.getInstance(CONTEXT);
		if (callback != null){
			mServiceKeeper.addRemoteCallbackList(mPackageName,callback);
			return true;
		}
		return false;
	}
	
	@Override
	public final boolean unregisterCallback(IInternalEventCallback callback) throws RemoteException {
		ServiceKeeper mServiceKeeper = ServiceKeeper.getInstance(CONTEXT);
		if (callback != null){
			return mServiceKeeper.clearRemoteCallbackList(mPackageName,callback);
		}
		return false;
	}
	
	/**
	 * 클라이언트로 콜백해주는 함수
	 * @param ClassName
	 * @param data
	 */
	public void callbackData(int responseCode,String data){
		ServiceKeeper mServiceKeeper = ServiceKeeper.getInstance(CONTEXT);
		RemoteCallbackList<IInternalEventCallback> mRemoteCallbackList = mServiceKeeper.obtainRemoteCallbackList(mPackageName);
		if(mRemoteCallbackList==null)return;
		CallbackData mCallbackData = CALLBACK_DATA_MAP.get(responseCode);
		if(mCallbackData==null){
			mCallbackData = new CallbackData();
		}
		mCallbackData.OutDeviceData = data;
		
		int N = mRemoteCallbackList.beginBroadcast();
		for(int i=0;i<N;i++){
			try {
				mRemoteCallbackList.getBroadcastItem(i).onReceiveData(responseCode, mCallbackData);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * BlinkAppInfo를 데이터베이스 등록하는 매서드
	 */
	@Override
	public void registerBlinkApp(
			BlinkAppInfo mBlinkAppInfo)
			throws RemoteException {
		// TODO Auto-generated method stub
		Log.i(tag, "registerBlinkApp");
		mBlinkDatabaseManager.registerLog(mDeviceName, mPackageName, mBlinkDatabaseManager.LOG_REGISTER_BLINKAPP, "");
		mBlinkDatabaseManager.registerBlinkApp(mBlinkAppInfo);
	}

	@Override
	public void obtainMeasurementData(String ClassName,
			String DateTimeFrom, String DateTimeTo, int ContainType,int requestCode)
			throws RemoteException {
		// TODO Auto-generated method stub
		CallbackData mCallbackData = new CallbackData();
		
		CALLBACK_DATA_MAP.remove(requestCode);
		
		try {
			Class<?> mClass = Class.forName(ClassName);
			
			BlinkMessage mBlinkMessage;
			//자신의 디바이스가 아니고 다른곳에 데이터가 존재하면 메인으로 보낸다.
			if(mBlinkDatabaseManager.checkOutDevice(mClass,mBlinkDevice.getAddress())){
				//DatabaseMessage 생성
				DatabaseMessage mDatabaseMessage = new DatabaseMessage.Builder()
				.setCondition(ClassName)
				.setDateTimeFrom(DateTimeFrom)
				.setDateTimeTo(DateTimeTo)
				.setType(DatabaseMessage.OBTAIN_DATA_BY_CLASS)
				.build();
				
				//BlinkMessage 생성
				mBlinkMessage = new BlinkMessage.Builder()
										.setDestinationDevice(null)
										.setDestinationApplication(null)
										.setSourceDevice(mBlinkDevice)
										.setSourceApplication(mPackageName)
										.setMessage(gson.toJson(mDatabaseMessage))
										.setCode(requestCode)
										.build();
				if(requestPolicy==REQUEST_TYPE_DUAL_DEVICE){
					mCallbackData.InDeviceData = mBlinkDatabaseManager.obtainMeasurementData(mClass, DateTimeFrom, DateTimeTo, ContainType);
					CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
				}
				//자기 자신이 center 디바이스면 메시지를 보내지 않고 에러코드를 설정한다.
				if(ServiceKeeper.getInstance(CONTEXT).obtainCurrentCenterDevice().getAddress().contentEquals(mBlinkDevice.getAddress())){
					mCallbackData.Error = CallbackData.ERROR_CENTER_DEVICE;
				}
				else CONTEXT.mMessageProcessor.sendBlinkMessageTo(mBlinkMessage, null);
			}else {
				mCallbackData.Error = CallbackData.ERROR_NO_OUT_DEVICE;
				if(requestPolicy==REQUEST_TYPE_DUAL_DEVICE){
					mCallbackData.InDeviceData = mBlinkDatabaseManager.obtainMeasurementData(mClass, DateTimeFrom, DateTimeTo, ContainType);
					CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
				}
				callbackData(requestCode, null);
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
		CallbackData mCallbackData = new CallbackData();
		
		CALLBACK_DATA_MAP.remove(requestCode);
		
		BlinkMessage mBlinkMessage;
		
		//자신의 디바이스가 아니고 다른곳에 MeasurementData가 존재하면
		if(mBlinkDatabaseManager.checkOutDevice(mMeasurementList,mBlinkDevice.getAddress())){
			//BlinkMessage 생성
			DatabaseMessage mDatabaseMessage = new DatabaseMessage.Builder()
														.setCondition(gson.toJson(mMeasurementList))
														.setDateTimeFrom(DateTimeFrom)
														.setDateTimeTo(DateTimeTo)
														.setType(DatabaseMessage.OBTAIN_DATA_BY_ID)
														.build();

			String temp = gson.toJson(mDatabaseMessage);
			Log.i("test",temp);
			mBlinkMessage = new BlinkMessage.Builder()
									.setDestinationDevice(null)
									.setDestinationApplication(null)
									.setSourceDevice(mBlinkDevice)
									.setSourceApplication(mPackageName)
									.setMessage(gson.toJson(mDatabaseMessage))
									.setCode(requestCode)
									.build();
			if(requestPolicy==REQUEST_TYPE_DUAL_DEVICE){
				CONTEXT.mMessageProcessor.sendBlinkMessageTo(mBlinkMessage, null);
				List<MeasurementData> InDeviceData = mBlinkDatabaseManager.obtainMeasurementData(mMeasurementList, DateTimeFrom, DateTimeTo);
				mCallbackData.InDeviceData =  gson.toJson(InDeviceData);
				CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
			}
			//자기 자신이 center 디바이스면 메시지를 보내지 않고 에러코드를 설정한다.
			if(ServiceKeeper.getInstance(CONTEXT).obtainCurrentCenterDevice().getAddress().contentEquals(mBlinkDevice.getAddress())){
				mCallbackData.Error = CallbackData.ERROR_CENTER_DEVICE;
			}
			else CONTEXT.mMessageProcessor.sendBlinkMessageTo(mBlinkMessage, null);
		}else {
			mCallbackData.Error = CallbackData.ERROR_NO_OUT_DEVICE;
			if(requestPolicy==REQUEST_TYPE_DUAL_DEVICE){
				//외부 디바이스에 데이터가 없으면 에러코드를 설정하고 내부에서 검색한다.
				List<MeasurementData> InDeviceData = mBlinkDatabaseManager.obtainMeasurementData(mMeasurementList, DateTimeFrom, DateTimeTo);
				mCallbackData.InDeviceData =  gson.toJson(InDeviceData);
				CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
			}
			callbackData(requestCode, null);
		}
	}

	@Override
	public void startFunction(Function function,int requestCode) throws RemoteException {
		// TODO Auto-generated method stub
		CallbackData mCallbackData = new CallbackData();
		
		CALLBACK_DATA_MAP.remove(requestCode);
		
		Device device = mBlinkDatabaseManager.obtainDevice(function);
		App app = mBlinkDatabaseManager.obtainApp(function);
		
		BlinkMessage mBlinkMessage;
		//자신의 디바이스가 아니고 다른곳에 function이 존재하면
		if(device!=null && !device.MacAddress.contentEquals(mBlinkDevice.getAddress())){
			//BlinkMessage 생성
			mBlinkMessage = new BlinkMessage.Builder()
									.setDestinationDevice(BlinkDevice.load(device.MacAddress))
									.setDestinationApplication(app.PackageName)
									.setSourceDevice(BlinkDevice.update(mBlinkDevice))
									.setSourceApplication(mPackageName)
									.setMessage(gson.toJson(function))
									.setCode(requestCode)
									.build();
			//Remote Call
			if(requestPolicy==REQUEST_TYPE_OUT_DEVICE){
				CONTEXT.mMessageProcessor.sendBlinkMessageTo(mBlinkMessage, BlinkDevice.load(device.MacAddress));
			}
			else if(requestPolicy==REQUEST_TYPE_DUAL_DEVICE){
				CONTEXT.startFunction(function);
				mCallbackData.InDeviceData = "success";
				CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
			}
		}else {
			mCallbackData.Error = CallbackData.ERROR_NO_OUT_DEVICE;
			if(requestPolicy==REQUEST_TYPE_DUAL_DEVICE){
				CONTEXT.startFunction(function);
				mCallbackData.InDeviceData = "success";
				CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
			}
			callbackData(requestCode, null);
		}
	}

}
