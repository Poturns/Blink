package kr.poturns.blink.internal.comm;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.List;

import kr.poturns.blink.db.BlinkDatabaseManager;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.internal.BlinkLocalService;
import kr.poturns.blink.internal.DeviceAnalyzer;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author Jiwon.Kim
 * @author Yeonho.Kim
 * @since 2014.08.19
 * 
 */
public abstract class BlinkServiceInteraction implements ServiceConnection, IBlinkEventBroadcast {
	private final String tag = "BlinkServiceInteraction";
	
	private final Context CONTEXT;
	private final EventBroadcastReceiver EVENT_BR;
	private final IntentFilter FILTER;

	private IBlinkEventBroadcast mBlinkEventBroadcast;
	private IInternalOperationSupport mInternalOperationSupport;
	private IInternalEventCallback mIInternalEventCallback;
	private BlinkDatabaseManager mBlinkDatabaseManager;
	/**
	 * Application Info
	 */
	// 바인더 컨넥션시 획득
	private BlinkDevice mBlinkDevice;
	// 생성자에서 초기화
	private String mPackageName = "";
	private String mAppName = "";
	
	public BlinkAppInfo mBlinkAppInfo;
	public Local local;
	public Remote remote;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public BlinkServiceInteraction(Context context,
			IBlinkEventBroadcast iBlinkEventBroadcast,
			IInternalEventCallback iInternalEventCallback) {
		CONTEXT = context;
		EVENT_BR = new EventBroadcastReceiver();
		FILTER = new IntentFilter();

		FILTER.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); // 블루투스 탐색 시작
		FILTER.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); // 블루투스 탐색 종료

		FILTER.addAction(BROADCAST_DEVICE_DISCOVERED);
		FILTER.addAction(BROADCAST_DEVICE_CONNECTED);
		FILTER.addAction(BROADCAST_DEVICE_DISCONNECTED);
		FILTER.addAction(BROADCAST_DEVICE_IDENTITY_CHANGED);

		FILTER.addAction(BROADCAST_CONFIGURATION_CHANGED);
		FILTER.addAction(BROADCAST_MESSAGE_RECEIVED_FOR_TEST);	// FOR TEST

		mBlinkEventBroadcast = iBlinkEventBroadcast;
		mIInternalEventCallback = iInternalEventCallback;
		mBlinkDatabaseManager = new BlinkDatabaseManager(context);
		
		/**
		 * Database Sync Event 받기
		 */
		CONTEXT.getContentResolver().registerContentObserver(SqliteManager.URI_OBSERVER_SYNC, false, mContentObserver);
		
		local = new Local();
		remote = new Remote();
		
		/**
		 * Setting Application Info
		 */
		mPackageName = context.getPackageName();
		mAppName = context.getApplicationInfo()
				.loadLabel(context.getPackageManager()).toString();
	}

	public BlinkServiceInteraction(Context context) {
		this(context, null, null);
	}

	@Override
	public final void onServiceConnected(ComponentName name, IBinder service) {
		CONTEXT.registerReceiver(EVENT_BR, FILTER);

		if (service == null)
			onServiceFailed();
		else {
			mInternalOperationSupport = BlinkSupportBinder.asInterface(service);
			if (mInternalOperationSupport == null) {
				onServiceFailed();

			} else {
				try {
					mInternalOperationSupport.registerApplicationInfo(mPackageName, mAppName);
					mBlinkDevice = mInternalOperationSupport.getBlinkDevice();

					if (mIInternalEventCallback != null) {
						mInternalOperationSupport
								.registerCallback(mIInternalEventCallback);
					}
					// 어플리케이션 관련 정보 교환

				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

			onServiceConnected(mInternalOperationSupport);
		}
	}

	@Override
	public final void onServiceDisconnected(ComponentName name) {
		CONTEXT.unregisterReceiver(EVENT_BR);
		onServiceDisconnected();
	}

	public final void startService() {
		Intent intent = new Intent(BlinkLocalService.INTENT_ACTION_NAME);
		intent.putExtra(BlinkLocalService.INTENT_EXTRA_SOURCE_PACKAGE,
				CONTEXT.getPackageName());

		CONTEXT.startService(intent);
		CONTEXT.bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	public final void stopService() {
		Intent intent = new Intent(BlinkLocalService.INTENT_ACTION_NAME);
		intent.putExtra(BlinkLocalService.INTENT_EXTRA_SOURCE_PACKAGE,
				CONTEXT.getPackageName());

		CONTEXT.unbindService(this);
		CONTEXT.unregisterReceiver(EVENT_BR);
		// CONTEXT.stopService(intent);
	}

	public final void startBroadcastReceiver() {
		CONTEXT.registerReceiver(EVENT_BR, FILTER);
	}

	public final void stopBroadcastReceiver() {
		CONTEXT.unregisterReceiver(EVENT_BR);
	}

	public final void requestConfigurationChange(String... keys) {
		if (keys != null) {
			for (String key : keys) {

			}
		}

		Intent intent = new Intent(BROADCAST_REQUEST_CONFIGURATION_CHANGE);
		CONTEXT.sendBroadcast(intent, PERMISSION_LISTEN_STATE_MESSAGE);
	}

	public final void setOnBlinkEventBroadcast(
			IBlinkEventBroadcast iBlinkEventBroadcast) {
		mBlinkEventBroadcast = iBlinkEventBroadcast;
	}

	private class EventBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				onDiscoveryStarted();
				return;

			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				onDiscoveryFinished();
				return;
			}

			BlinkDevice device = (BlinkDevice) intent
					.getSerializableExtra(EXTRA_DEVICE);

			if (BROADCAST_DEVICE_DISCOVERED.equals(action)) {
				onDeviceDiscovered(device);

			} else if (BROADCAST_DEVICE_CONNECTED.equals(action)) {
				onDeviceConnected(device);

			} else if (BROADCAST_DEVICE_DISCONNECTED.equals(action)) {
				onDeviceDisconnected(device);

			} else if (BROADCAST_DEVICE_IDENTITY_CHANGED.equals(action)) {
				onIdentityChanged(device.getIdentity());

			} else if (BROADCAST_CONFIGURATION_CHANGED.equals(action)) {

			} else if (BROADCAST_MESSAGE_RECEIVED_FOR_TEST.equals(action)) {
				Toast.makeText(CONTEXT, intent.getStringExtra("content"), Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>
	 * 블루투스 탐색이 시작되었을 때, 호출된다.
	 * 
	 */
	public void onDiscoveryStarted() {
	}

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>
	 * 블루투스 탐색 수행시, 디바이스가 발견되었을 때 호출된다. <br>
	 * Override할 경우, 등록한 {@link IBlinkEventBroadcast}은 동작하지 않는다.
	 * <hr>
	 * 
	 * @param device
	 */
	public void onDeviceDiscovered(BlinkDevice device) {
		if (mBlinkEventBroadcast != null)
			mBlinkEventBroadcast.onDeviceDiscovered(device);
	}

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>
	 * 블루투스 탐색이 종료되었을 때, 호출된다.
	 * 
	 */
	public void onDiscoveryFinished() {
	}

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>
	 * 블루투스 디바이스가 연결되었을 때 호출된다. <br>
	 * Override할 경우, 등록한 {@link IBlinkEventBroadcast}은 동작하지 않는다.
	 * <hr>
	 * 
	 * @param device
	 */
	public void onDeviceConnected(BlinkDevice device) {
		if (mBlinkEventBroadcast != null)
			mBlinkEventBroadcast.onDeviceConnected(device);
	}

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>
	 * 블루투스 디바이스가 해제되었을 때 호출된다. <br>
	 * Override할 경우, 등록한 {@link IBlinkEventBroadcast}은 동작하지 않는다.
	 * <hr>
	 * 
	 * @param device
	 */
	public void onDeviceDisconnected(BlinkDevice device) {
		if (mBlinkEventBroadcast != null)
			mBlinkEventBroadcast.onDeviceDisconnected(device);

	}

	/**
	 * 
	 * @param identity
	 */
	public void onIdentityChanged(DeviceAnalyzer.Identity identity) {
	}

	/**
	 * 
	 */
	public void onConfigurationChanged() {
	}

	/**
	 * Service에 Binding 되었을 때 호출된다.
	 * 
	 * @param iSupport
	 */
	public abstract void onServiceConnected(IInternalOperationSupport iSupport);

	/**
	 * Service에서 Unbinding 되었을 때 호출된다.
	 */
	public abstract void onServiceDisconnected();

	/**
	 * Service에서 Binding이 실패하였을 때 호출된다.
	 */
	public abstract void onServiceFailed();

	/**
	 * Database Interaction
	 */

	/**
	 * Database Sync가 발생했을 때 호출된다.
	 * 기존에 가지고 있던 BlinkAppInfo를 변경해야 한다.
	 */
	private ContentObserver mContentObserver = new ContentObserver(new Handler()){
		public void onChange(boolean selfChange, Uri uri) {
			Log.i(tag, "Uri : "+uri);
			//새로운 BlinkApp이 추가되면 실행
			if(uri.equals(SqliteManager.URI_OBSERVER_SYNC)){
				Log.i(tag, "if : URI_OBSERVER_SYNC");
				mBlinkAppInfo = local.obtainBlinkApp();
			}
		};
	};
	
	public boolean registerBlinkApp(
			BlinkAppInfo mBlinkAppInfo) {
		mBlinkAppInfo.mDevice.Device = mBlinkDevice.getName();
		mBlinkAppInfo.mDevice.MacAddress = mBlinkDevice.getAddress();
		mBlinkAppInfo.mApp.PackageName = mPackageName;
		mBlinkAppInfo.mApp.AppName = mAppName;
		PackageManager mPackageManager = CONTEXT.getPackageManager();
		
		try {
			Bitmap bitmap = ((BitmapDrawable)mPackageManager.getApplicationIcon(mPackageName)).getBitmap();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			mBlinkAppInfo.mApp.AppIcon = stream.toByteArray();
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			mBlinkAppInfo.mApp.AppIcon = null;
		}
		
		try {
			mInternalOperationSupport
					.registerBlinkApp(mBlinkAppInfo);
			mBlinkAppInfo = local.obtainBlinkApp();
			return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	// 예제를 위한 테스트 코드
	public boolean registerExternalBlinkApp(
			BlinkAppInfo mBlinkAppInfo) {
		try {
			mInternalOperationSupport
					.registerBlinkApp(mBlinkAppInfo);
			return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Local로 동작하는 매소드
	 * 
	 * @author mementohora
	 * 
	 */
	public class Local {
		public BlinkAppInfo obtainBlinkApp() {
			mBlinkAppInfo = obtainBlinkApp(mBlinkDevice.getName(), mPackageName);
			return mBlinkAppInfo;
		}

		public BlinkAppInfo obtainBlinkApp(String DeviceName,
				String PackageName) {
			return mBlinkDatabaseManager.obtainBlinkApp(DeviceName,
					PackageName);
		}

		public List<BlinkAppInfo> obtainBlinkAppAll() {
			return mBlinkDatabaseManager.obtainBlinkApp();
		}

		public void registerMeasurementData(Object obj) {
			try {
				mBlinkDatabaseManager.registerMeasurementData(
						mBlinkAppInfo, obj);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public <Object> Object obtainMeasurementData(Class<?> obj, Type type) {
			return obtainMeasurementData(obj, null, null,
					SqliteManager.CONTAIN_DEFAULT, type);
		}

		public <Object> Object obtainMeasurementData(Class<?> obj,
				int ContainType, Type type) {
			return obtainMeasurementData(obj, null, null, ContainType, type);
		}

		public <Object> Object obtainMeasurementData(Class<?> obj,
				String DateTimeFrom, String DateTimeTo, int ContainType,
				Type type) {
			String json;
			try {
				json = mBlinkDatabaseManager.obtainMeasurementData(obj,
						DateTimeFrom, DateTimeTo, ContainType);
				return gson.fromJson(json, type);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		public List<MeasurementData> obtainMeasurementData(
				List<Measurement> mMeasurementList, String DateTimeFrom,
				String DateTimeTo) {
			return mBlinkDatabaseManager.obtainMeasurementData(
					mMeasurementList, DateTimeFrom, DateTimeTo);
		}

		public void startFunction(Function function) {
			if (function.Type == Function.TYPE_ACTIVITY)
				CONTEXT.startActivity(new Intent(function.Action)
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			else if (function.Type == Function.TYPE_SERIVCE)
				CONTEXT.startService(new Intent(function.Action));
			else if (function.Type == Function.TYPE_BROADCAST)
				CONTEXT.sendBroadcast(new Intent(function.Action));
		}

		public List<BlinkLog> obtainLog(String Device, String App, int Type,
				String DateTimeFrom, String DateTimeTo) {
			return mBlinkDatabaseManager.obtainLog(Device, App, Type,
					DateTimeFrom, DateTimeTo);
		}

		public List<BlinkLog> obtainLog(String Device, String App,
				String DateTimeFrom, String DateTimeTo) {
			return obtainLog(Device, App, -1, DateTimeFrom, DateTimeTo);
		}

		public List<BlinkLog> obtainLog(String Device, String DateTimeFrom,
				String DateTimeTo) {
			return obtainLog(Device, null, -1, DateTimeFrom, DateTimeTo);
		}

		public List<BlinkLog> obtainLog(String DateTimeFrom, String DateTimeTo) {
			return obtainLog(null, null, -1, DateTimeFrom, DateTimeTo);
		}

		public List<BlinkLog> obtainLog() {
			return obtainLog(null, null, -1, null, null);
		}

		public Local queryDevice(String where) {
			// TODO Auto-generated method stub
			mBlinkDatabaseManager.queryDevice(where);
			return this;
		}

		public Local queryApp(String where) {
			// TODO Auto-generated method stub
			mBlinkDatabaseManager.queryApp(where);
			return this;
		}

		public Local queryFunction(String where) {
			// TODO Auto-generated method stub
			mBlinkDatabaseManager.queryFunction(where);
			return this;
		}

		public Local queryMeasurement(String where) {
			// TODO Auto-generated method stub
			mBlinkDatabaseManager.queryMeasurement(where);
			return this;
		}

		public Local queryMeasurementData(String where) {
			// TODO Auto-generated method stub
			mBlinkDatabaseManager.queryMeasurementData(where);
			return this;
		}

//		public boolean checkInDevice(List<Measurement> mMeasurementList) {
//			// TODO Auto-generated method stub
//			return mBlinkDatabaseManager.checkInDevice(mMeasurementList);
//		}
//
//		public boolean checkInDevice(Function mFunction) {
//			// TODO Auto-generated method stub
//			return mBlinkDatabaseManager.checkInDevice(mFunction);
//		}
//
//		public boolean checkInDevice(Class<?> obj) {
//			// TODO Auto-generated method stub
//			return mBlinkDatabaseManager.checkInDevice(obj);
//		}

		public List<Device> getDeviceList() {
			// TODO Auto-generated method stub
			return mBlinkDatabaseManager.getDeviceList();
		}

		public void setDeviceList(List<Device> mDeviceList) {
			// TODO Auto-generated method stub
			mBlinkDatabaseManager.setDeviceList(mDeviceList);
		}

		public List<App> getAppList() {
			// TODO Auto-generated method stub
			return mBlinkDatabaseManager.getAppList();
		}

		public void setAppList(List<App> mAppList) {
			// TODO Auto-generated method stub
			mBlinkDatabaseManager.setAppList(mAppList);
		}

		public List<Function> getFunctionList() {
			// TODO Auto-generated method stub
			return mBlinkDatabaseManager.getFunctionList();
		}

		public void setFunctionList(List<Function> mFunctionList) {
			// TODO Auto-generated method stub
			mBlinkDatabaseManager.setFunctionList(mFunctionList);
		}

		public List<Measurement> getMeasurementList() {
			// TODO Auto-generated method stub
			return mBlinkDatabaseManager.getMeasurementList();
		}

		public void setMeasurementList(List<Measurement> mMeasurementList) {
			// TODO Auto-generated method stub
			mBlinkDatabaseManager.setMeasurementList(mMeasurementList);
		}

		public List<MeasurementData> getMeasurementDataList() {
			// TODO Auto-generated method stub
			return mBlinkDatabaseManager.getMeasurementDataList();
		}

		public void setMeasurementDataList(
				List<MeasurementData> mMeasurementDataList) {
			// TODO Auto-generated method stub
			mBlinkDatabaseManager.setMeasurementDataList(mMeasurementDataList);
		}
	}

	/**
	 * Remote와 통신할 수 있는 코드 결과는 callback으로 넘겨진다.
	 * 
	 * @author mementohora
	 * 
	 */
	public class Remote {
		private void setRequestPolicy(int requestPolicy) {
			try {
				mInternalOperationSupport.setRequestPolicy(requestPolicy);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * Blink 기본 schema에 있는 클래스만 사용 가능
		 * 
		 * @param obj
		 * @param type
		 * @param RequestType
		 * @param RequestCode
		 * @return
		 */
		public void obtainMeasurementData(Class<?> obj, int RequestCode) {
			obtainMeasurementData(obj, null, null,
					SqliteManager.CONTAIN_DEFAULT, RequestCode);
		}

		public void obtainMeasurementData(Class<?> obj, int ContainType, int RequestCode) {
			obtainMeasurementData(obj, null, null, ContainType, RequestCode);
		}

		public void obtainMeasurementData(Class<?> obj, String DateTimeFrom, String DateTimeTo, int ContainType, int RequestCode) {
			String ClassName = obj.getName();
			try {
				mInternalOperationSupport.obtainMeasurementData(ClassName,
						DateTimeFrom, DateTimeTo, ContainType, RequestCode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 기본타입
		 * 
		 * @param mMeasurementList
		 * @param DateTimeFrom
		 * @param DateTimeTo
		 * @param RequestType
		 * @param RequestCode
		 * @return
		 */
		public void obtainMeasurementData(List<Measurement> mMeasurementList,
				String DateTimeFrom, String DateTimeTo, int RequestCode) {
			try {
				mInternalOperationSupport
						.obtainMeasurementDataById(mMeasurementList,
								DateTimeFrom, DateTimeTo, RequestCode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void startFunction(Function function, int requestCode) {
			try {
				mInternalOperationSupport.startFunction(function, requestCode);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
