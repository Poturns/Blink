package kr.poturns.blink.internal.comm;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.SyncDatabaseManager;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.db.archive.DatabaseMessage;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.internal.BlinkLocalService;
import kr.poturns.blink.internal.ConnectionSupportBinder;
import kr.poturns.blink.internal.DeviceAnalyzer;
import kr.poturns.blink.internal.ServiceKeeper;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 서비스에 어떻게 요청할 지 정하며 Log를 남긴다.<br>
 * 만약 다른 디바이스에 해당하는 데이터면 블루투스쪽으로 넘겨야 한다.<br>
 * <br>
 * 다른 디바이스와 통신이 필요한 메소드는 세 가지이다.<br>
 * <li>
 * {@link BlinkSupportBinder#obtainMeasurementData(String, String, String, int, int)}
 * : class 이름을 통해 데이터를 검색하는 메소드</li> <br>
 * <br>
 * <li>
 * {@link BlinkSupportBinder#obtainMeasurementDataById(List, String, String, int)}
 * : Measurement 리스트를 통해 데이터를 검색하는 메소드</li><br>
 * <br>
 * <li>{@link BlinkSupportBinder#startFunction(Function, int)} : 기능을 호출하는 메소드</li>
 * <br>
 * 
 * @author Jiwon.Kim
 */
public class BlinkSupportBinder extends ConnectionSupportBinder {
	private static final String TAG = "BlinkDatabaseBinder";

	/**
	 * 데이터 요청 정책 타입이다. 현재 쓰이지 않는다.
	 */
	private static final int REQUEST_TYPE_IN_DEVICE = 1;
	/**
	 * 데이터 요청 정책 타입이다. 현재 쓰이지 않는다.
	 */
	public static final int REQUEST_TYPE_OUT_DEVICE = 2;
	/**
	 * 데이터 요청 정책 타입이다. 현재 쓰이지 않는다.
	 */
	public static final int REQUEST_TYPE_DUAL_DEVICE = 3;

	/**
	 * 바인더와 연결된 어플리케이션의 디바이스명, 패키지명, 앱 이름
	 */

	int requestPolicy = REQUEST_TYPE_DUAL_DEVICE;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	SyncDatabaseManager mBlinkDatabaseManager;
	BlinkDevice mBlinkDevice;

	/**
	 * 어플리케이션에 돌려줘야 할 콜백 데이터를 가지고 있는 해쉬맵</br> 요청코드별로 가지고 있으며 후에 다른 디바이스로부터 데이터가
	 * 오면 내부에서 검색한 데이터와 합치기 위해 임시 저장해둔다.
	 */
	HashMap<Integer, CallbackData> CALLBACK_DATA_MAP = new HashMap<Integer, CallbackData>();

	public BlinkSupportBinder(BlinkLocalService context) throws Exception {
		super(context);
		mBlinkDatabaseManager = new SyncDatabaseManager(context);
		mBlinkDevice = BlinkDevice.HOST;
		if (mBlinkDevice == null)
			Log.i(TAG, "BlinkDevice.HOST : null");
	}

	/**
	 * 데이터 요청 정책을 설정한다. 현재 사용하지 않는다.
	 */
	@Override
	public void setRequestPolicy(int requestPolicy) {
		this.requestPolicy = requestPolicy;
	}

	@Override
	public BlinkDevice getBlinkDevice() throws RemoteException {

		return mBlinkDevice;
	}

	/**
	 * 어플리케이션으로부터 콜백을 받아서 등록한다. 콜백은 ServiceKeeper에 저장된다.
	 */
	@Override
	public final boolean registerCallback(IInternalEventCallback callback,String packageName)
			throws RemoteException {
		ServiceKeeper mServiceKeeper = ServiceKeeper.getInstance(CONTEXT);
		if (callback != null) {
			mServiceKeeper.addRemoteCallbackList(packageName, callback);
			return true;
		}
		return false;
	}

	/**
	 * 어플리케이션으로부터 콜백을 제거한다. 콜백은 ServiceKeeper에 저장되어 있다.
	 */
	@Override
	public final boolean unregisterCallback(IInternalEventCallback callback,String packageName)
			throws RemoteException {
		ServiceKeeper mServiceKeeper = ServiceKeeper.getInstance(CONTEXT);
		if (callback != null) {
			return mServiceKeeper.removeRemoteCallbackList(packageName,
					callback);
		}
		return false;
	}

	/**
	 * 클라이언트로 콜백해주는 매소드이다. <br>
	 * {@link BlinkSupportBinder#registerCallback(IInternalEventCallback)}을 통헤
	 * 어플리케이션에서 등록한 콜백을 호출해준다.
	 * 
	 * @param responseCode
	 *            : 어플리케이션으로부터 받은 requestCode와 동일한 값으로 어떤 요청인지 구분하기 위한 값
	 * @param data
	 *            : 외부 디바이스로부터 온 데이터
	 * @param result
	 *            : 통신이 정상적으로 되었는지 결과
	 */
	public void callbackData(int responseCode, String data, boolean result,String packageName) {
		Log.i(TAG, "Binder callbackData");
		ServiceKeeper mServiceKeeper = ServiceKeeper.getInstance(CONTEXT);
		RemoteCallbackList<IInternalEventCallback> mRemoteCallbackList = mServiceKeeper
				.obtainRemoteCallbackList(packageName);
		if (mRemoteCallbackList == null) {
			Log.i(TAG, "Binder mRemoteCallbackList is null");
			return;
		}
		CallbackData mCallbackData = CALLBACK_DATA_MAP.get(responseCode);
		if (mCallbackData == null) {
			mCallbackData = new CallbackData();
		}
		mCallbackData.OutDeviceData = data;
		mCallbackData.Result = result;

		int N = mRemoteCallbackList.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				Log.i(TAG, "Binder call onReceiveData");
				mRemoteCallbackList.getBroadcastItem(i).onReceiveData(
						responseCode, mCallbackData);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		mRemoteCallbackList.finishBroadcast();
	}

	/**
	 * BlinkAppInfo를 데이터베이스 등록하는 매서드
	 */
	@Override
	public void registerBlinkApp(BlinkAppInfo mBlinkAppInfo)
			throws RemoteException {
		PackageManager mPackageManager = CONTEXT.getPackageManager();
		try {
			Bitmap bitmap = ((BitmapDrawable)mPackageManager.getApplicationIcon(mBlinkAppInfo.mApp.PackageName)).getBitmap();
			bitmap = Bitmap.createScaledBitmap(bitmap,72,72,true);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			mBlinkAppInfo.mApp.AppIcon = stream.toByteArray();
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			mBlinkAppInfo.mApp.AppIcon = null;
		}
		mBlinkDatabaseManager.registerBlinkApp(mBlinkAppInfo);
	}

	/**
	 * ClassName을 통해 데이터 검색을 요청한다. 콜백으로 데이터가 반환되며 내부 디바이스에서도 데이터를 검색한다. <br>
	 * 만약 자신이 Center 디바이스면 에러코드를 설정한 후 내부 디바이스의 데이터를 설정하고 콜백을 호출한다.
	 */
	@Override
	public void obtainMeasurementData(String ClassName, String DateTimeFrom,
			String DateTimeTo, int ContainType, int requestCode,String packageName)
			throws RemoteException {
		Log.i(TAG, "Binder obtainMeasurementData");
	
		CallbackData mCallbackData = new CallbackData();
	
		CALLBACK_DATA_MAP.remove(requestCode);
	
		try {
			// FIXME 라이브러리에 등록되지 않은 클래스일 경우
			// 클래스 인스턴스가 생성되지 않음.
			// 
			Class<?> mClass = Class.forName(ClassName);
	
			BlinkMessage mBlinkMessage;
			// 자신의 디바이스가 아니고 다른곳에 데이터가 존재하면 메인으로 보낸다.
			if (mBlinkDatabaseManager.checkOutDevice(mClass,
					mBlinkDevice.getAddress())) {
				
				// DatabaseMessage 생성
				DatabaseMessage mDatabaseMessage = new DatabaseMessage.Builder()
						.setCondition(ClassName).setDateTimeFrom(DateTimeFrom)
						.setDateTimeTo(DateTimeTo)
						.setType(DatabaseMessage.OBTAIN_DATA_BY_CLASS).build();
				
				// BlinkMessage 생성
				mBlinkMessage = new BlinkMessage.Builder()
						.setDestinationDevice((String) null)
						.setDestinationApplication(null)
						.setSourceDevice(mBlinkDevice)
						.setSourceApplication(packageName)
						.setMessage(gson.toJson(mDatabaseMessage))
						.setType(IBlinkMessagable.TYPE_REQUEST_MEASUREMENTDATA)
						.setCode(requestCode).build();
				
				if (requestPolicy == REQUEST_TYPE_DUAL_DEVICE) {
					mCallbackData.InDeviceData = mBlinkDatabaseManager
							.obtainMeasurementData(mClass, DateTimeFrom,
									DateTimeTo, ContainType);
					CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
				}
				// 자기 자신이 center 디바이스면 메시지를 보내지 않고 에러코드를 설정한다.
				if (ServiceKeeper.getInstance(CONTEXT)
						.obtainCurrentCenterDevice().getAddress()
						.contentEquals(mBlinkDevice.getAddress())) {
					Log.i(TAG, "Binder obtainMeasurementData : I am center");
					mCallbackData.ResultDetail = CallbackData.ERROR_CENTER_DEVICE;
					callbackData(requestCode, null, false,packageName);
				} else {
					Log.i(TAG, "Binder obtainMeasurementData : I am not center");
					CONTEXT.mMessageProcessor.sendBlinkMessageTo(mBlinkMessage,
							null);
				}
			} else {
				Log.i(TAG, "Binder obtainMeasurementData : no out device");
				mCallbackData.ResultDetail = CallbackData.ERROR_NO_OUT_DEVICE;
				if (requestPolicy == REQUEST_TYPE_DUAL_DEVICE) {
					mCallbackData.InDeviceData = mBlinkDatabaseManager
							.obtainMeasurementData(mClass, DateTimeFrom,
									DateTimeTo, ContainType);
					CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
				}
				callbackData(requestCode, null, false,packageName);
			}
		} catch (Exception e) {
			e.printStackTrace();
			callbackData(requestCode, null, false,packageName);
		}
	
	}

	/**
	 * MeasurementList를 통해 데이터 검색을 요청한다. 콜백으로 데이터가 반환되며 내부 디바이스에서도 데이터를 검색한다. 만약
	 * 자신이 Center 디바이스면 에러코드를 설정한 후 내부 디바이스의 데이터를 설정하고 콜백을 호출한다.
	 */
	@Override
	public void obtainMeasurementDataById(List<Measurement> mMeasurementList,
			String DateTimeFrom, String DateTimeTo, int requestCode,String packageName)
			throws RemoteException {

		CallbackData mCallbackData = new CallbackData();

		CALLBACK_DATA_MAP.remove(requestCode);

		BlinkMessage mBlinkMessage;

		// 자신의 디바이스가 아니고 다른곳에 MeasurementData가 존재하면
		if (mBlinkDatabaseManager.checkOutDevice(mMeasurementList,
				mBlinkDevice.getAddress())) {
			// BlinkMessage 생성
			DatabaseMessage mDatabaseMessage = new DatabaseMessage.Builder()
					.setCondition(gson.toJson(mMeasurementList))
					.setDateTimeFrom(DateTimeFrom).setDateTimeTo(DateTimeTo)
					.setType(DatabaseMessage.OBTAIN_DATA_BY_ID).build();

			String temp = gson.toJson(mDatabaseMessage);
			Log.i(TAG + " : test", temp);
			mBlinkMessage = new BlinkMessage.Builder()
					.setDestinationDevice((String) null)
					.setDestinationApplication(null)
					.setSourceDevice(mBlinkDevice)
					.setSourceApplication(packageName)
					.setMessage(gson.toJson(mDatabaseMessage))
					.setType(IBlinkMessagable.TYPE_REQUEST_MEASUREMENTDATA)
					.setCode(requestCode).build();
			
			if (requestPolicy == REQUEST_TYPE_DUAL_DEVICE) {
				CONTEXT.mMessageProcessor.sendBlinkMessageTo(mBlinkMessage,
						null);
				List<MeasurementData> InDeviceData = mBlinkDatabaseManager
						.obtainMeasurementData(mMeasurementList, DateTimeFrom,
								DateTimeTo);
				mCallbackData.InDeviceData = gson.toJson(InDeviceData);
				CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
			}
			
			// 자기 자신이 center 디바이스면 메시지를 보내지 않고 에러코드를 설정한다.
			if (ServiceKeeper.getInstance(CONTEXT).obtainCurrentCenterDevice()
					.getAddress().contentEquals(mBlinkDevice.getAddress())) {
				mCallbackData.ResultDetail = CallbackData.ERROR_CENTER_DEVICE;
				callbackData(requestCode, null, false,packageName);
			} else
				CONTEXT.mMessageProcessor.sendBlinkMessageTo(mBlinkMessage,
						null);
			
		} else {
			mCallbackData.ResultDetail = CallbackData.ERROR_NO_OUT_DEVICE;
			if (requestPolicy == REQUEST_TYPE_DUAL_DEVICE) {
				// 외부 디바이스에 데이터가 없으면 에러코드를 설정하고 내부에서 검색한다.
				List<MeasurementData> InDeviceData = mBlinkDatabaseManager
						.obtainMeasurementData(mMeasurementList, DateTimeFrom,
								DateTimeTo);
				mCallbackData.InDeviceData = gson.toJson(InDeviceData);
				CALLBACK_DATA_MAP.put(requestCode, mCallbackData);
			}
			callbackData(requestCode, null, false,packageName);
		}
	}

	/**
	 * 외부 디바이스로 기능을 호출하는 메시지를 보낸다.
	 */
	@Override
	public void startFunction(Function function, int requestCode,String packageName)
			throws RemoteException {

		CallbackData mCallbackData = new CallbackData();

		CALLBACK_DATA_MAP.remove(requestCode);

		Device device = mBlinkDatabaseManager.obtainDevice(function);
		App app = mBlinkDatabaseManager.obtainApp(function);

		BlinkMessage mBlinkMessage;
		// 자신의 디바이스가 아니고 다른곳에 function이 존재하면
		if (device != null
				&& !device.MacAddress.contentEquals(mBlinkDevice.getAddress())) {
			// BlinkMessage 생성
			mBlinkMessage = new BlinkMessage.Builder()
					.setDestinationDevice(BlinkDevice.load(device.MacAddress))
					.setDestinationApplication(app.PackageName)
					.setSourceDevice(BlinkDevice.update(mBlinkDevice))
					.setSourceApplication(packageName)
					.setMessage(gson.toJson(function))
					.setType(IBlinkMessagable.TYPE_REQUEST_FUNCTION)
					.setCode(requestCode).build();
			CONTEXT.mMessageProcessor.sendBlinkMessageTo(mBlinkMessage,
					BlinkDevice.load(device.MacAddress));
		} else {
			mCallbackData.ResultDetail = CallbackData.ERROR_NO_OUT_DEVICE;
			callbackData(requestCode, null, false,packageName);
		}
	}

	/**
	 * 타겟 어플리케이션에 단일 데이터를 전송한다.
	 * 
	 * @param targetBlinkAppInfo
	 *            : 전송할 타겟 어플리케이션
	 * @param mMeasurementData
	 *            : 전송할 MeasurementData
	 */
	@Override
	public void sendMeasurementData(BlinkAppInfo targetBlinkAppInfo,
			String json, int requestCode,String packageName) throws RemoteException {
		Log.i("Blink", "sendMeasurementData");

		CallbackData mCallbackData = new CallbackData();

		CALLBACK_DATA_MAP.remove(requestCode);

		BlinkMessage mBlinkMessage;

		// 해당 BlinkApp이 없을 경우
		if (!targetBlinkAppInfo.isExist) {
			mCallbackData.ResultDetail = CallbackData.ERROR_CONNECT_FAIL;
			callbackData(requestCode, null, false,packageName);
		}
		// 타겟 디바이스가 자신이 아니면 메시지를 보낸다.
		else if (!targetBlinkAppInfo.mDevice.MacAddress
				.contentEquals(mBlinkDevice.getAddress())) {
			// BlinkMessage 생성
			Log.i("Blink", "sendMeasurementData remote!");
			mBlinkMessage = new BlinkMessage.Builder()
					.setDestinationDevice(
							BlinkDevice
									.load(targetBlinkAppInfo.mDevice.MacAddress))
					.setDestinationApplication(
							targetBlinkAppInfo.mApp.PackageName)
					.setSourceDevice(BlinkDevice.update(mBlinkDevice))
					.setSourceApplication(packageName)
					.setMessage(json)
					.setType(
							IBlinkMessagable.TYPE_RESPONSE_MEASUREMENTDATA_SUCCESS)
					.setCode(requestCode).build();
			CONTEXT.mMessageProcessor.sendBlinkMessageTo(mBlinkMessage,
					BlinkDevice.load(targetBlinkAppInfo.mDevice.MacAddress));
		}
		// 타겟 디바이스가 자기 자신이면 직접 콜백을 호출한다.
		else if (targetBlinkAppInfo.mDevice.MacAddress
				.contentEquals(mBlinkDevice.getAddress())) {
			// 다른 어플리케이션의 콜백 호출
			ServiceKeeper.getInstance(CONTEXT)
					.obtainBinder()
					.callbackData(requestCode, json, true,targetBlinkAppInfo.mApp.PackageName);
			// 자기 자신에게 reponse를 보냄
			mCallbackData.ResultDetail = CallbackData.ERROR_NO_OUT_DEVICE;
			callbackData(requestCode, null, true,packageName);
		}
	}

	/**
	 * 통신 테스트를 위핸 임시 매소드, sync 메시지를 만들어 보낸다.
	 */
	@Override
	public void SyncBlinkApp() throws RemoteException {
		Log.i("Blink", "SyncBlinkApp");
		CONTEXT.mContentObserver.onChange(true,
				SqliteManager.URI_OBSERVER_BLINKAPP);

	}

	@Override
	public void SyncMeasurementData() throws RemoteException {
		Log.i("Blink", "SyncMeasurementData");
		CONTEXT.mContentObserver.onChange(true,
				SqliteManager.URI_OBSERVER_MEASUREMENTDATA);
	}

}
