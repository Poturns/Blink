package kr.poturns.blink.internal.comm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import kr.poturns.blink.schema.DefaultSchema;
import kr.poturns.blink.util.FileUtil;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Blink 어플리케이션과 서비스 간의 통신을 도와주는 클래스<br>
 * 안드로이드 서비스 구조에서 ServiceConnection이며 어플리케이션에서 서비스를 호출할 수 있도록 매소드를 정의하고 있다.<br>
 * 
 * @author Jiwon.Kim
 * @author Yeonho.Kim
 * @since 2014.08.19
 * 
 */
public class BlinkServiceInteraction implements ServiceConnection,
		IBlinkEventBroadcast {
	private final String tag = "BlinkServiceInteraction";

	private final Context CONTEXT;
	private final EventBroadcastReceiver EVENT_BR;
	private final IntentFilter FILTER;

	private IBlinkEventBroadcast mBlinkEventBroadcast;
	private IInternalOperationSupport mInternalOperationSupport;
	private IInternalEventCallback mIInternalEventCallback;
	private BlinkDatabaseManager mBlinkDatabaseManager;
	private boolean binding;
	/**
	 * Application Info
	 */
	// 바인더 컨넥션시 획득
	private BlinkDevice mBlinkDevice;
	// 생성자에서 초기화
	private String mPackageName = "";
	private String mAppName = "";

	BlinkAppInfo mBlinkAppInfo;
	/** Local device에 요청하는 객체 */
	public final Local local = new Local();
	/** Remote device에 요청하는 객체 */
	public final Remote remote = new Remote();
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	boolean isRegisteredReceiver = false;

	// Blink Library를 위한 외부 디렉토리를 생성한다.
	static {
		FileUtil.createExternalDirectory();
	}

	/**
	 * 생성자로 Boradcast와 Callback을 등록할 수 있다. 등록하고 싶지 않을 경우 null을 매개변수로 넘기면 된다.
	 * 
	 * @param context
	 *            : Android Context 객체
	 * @param iBlinkEventBroadcast
	 *            : {@link BlinkDevice}의 상태 변화가 감지되었을 때, 콜백 될 리스너 <br>
	 *            <t><t><b>* {@link #startBroadcastReceiver()}를 호출하여야 콜백이
	 *            호출된다.</b><br>
	 * @param iInternalEventCallback
	 *            : 외부 데이터를 받을 콜백
	 */
	public BlinkServiceInteraction(Context context,
			IBlinkEventBroadcast iBlinkEventBroadcast,
			IInternalEventCallback iInternalEventCallback) {
		CONTEXT = context;
		EVENT_BR = new EventBroadcastReceiver();
		FILTER = new IntentFilter();

		// 블루투스 탐색 시작
		FILTER.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		// 블루투스 탐색 종료
		FILTER.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		FILTER.addAction(BROADCAST_DEVICE_DISCOVERED);
		FILTER.addAction(BROADCAST_DEVICE_CONNECTED);
		FILTER.addAction(BROADCAST_DEVICE_DISCONNECTED);
		FILTER.addAction(BROADCAST_DEVICE_IDENTITY_CHANGED);

		FILTER.addAction(BROADCAST_CONFIGURATION_CHANGED);
		// FILTER.addAction(BROADCAST_MESSAGE_RECEIVED_FOR_TEST); // FOR TEST

		mBlinkEventBroadcast = iBlinkEventBroadcast;
		mIInternalEventCallback = iInternalEventCallback;
		mBlinkDatabaseManager = new BlinkDatabaseManager(context);

		/**
		 * Database Sync Event 받기
		 */
		CONTEXT.getContentResolver().registerContentObserver(
				SqliteManager.URI_OBSERVER_SYNC, false, mContentObserver);

		/**
		 * Setting Application Info
		 */
		mPackageName = context.getPackageName();
		mAppName = context.getApplicationInfo()
				.loadLabel(context.getPackageManager()).toString();
		setBinding(false);
	}

	/**
	 * Broadcast와 Callback을 등록하지 않는 생성자<br>
	 * <br>
	 * {@code BlinkServiceInteration(context, null, null)}을 호출하는 것과 동일하다.
	 * 
	 * @param context
	 */
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
					mBlinkDevice = mInternalOperationSupport.getBlinkDevice();

					if (mIInternalEventCallback != null) {
						mInternalOperationSupport
								.registerCallback(mIInternalEventCallback,mPackageName);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			setBinding(true);
			onServiceConnected(mInternalOperationSupport);
		}
	}

	@Override
	public final void onServiceDisconnected(ComponentName name) {
		CONTEXT.unregisterReceiver(EVENT_BR);
		setBinding(false);
		onServiceDisconnected();
	}

	/**
	 * Blink 서비스와 바인드한다. 서비스가 실행 중이지 않을 경우 자동으로 생성한다. 바인드가 완료되면
	 * onServiceConnected()가 호출된다.
	 */
	public final void startService() {
		Log.i("Blink", "interaction start service");
		Intent intent = new Intent(BlinkLocalService.INTENT_ACTION_NAME);
		intent.putExtra(BlinkLocalService.INTENT_EXTRA_SOURCE_PACKAGE,
				CONTEXT.getPackageName());

		CONTEXT.startService(intent);
		CONTEXT.bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	/**
	 * 언바인드한다. 다른 바인드된 어플리케이션이 있을 경우 종료되지 않는다.
	 */
	public final void stopService() {
		Log.i("Blink", "interaction stopService");
		Intent intent = new Intent(BlinkLocalService.INTENT_ACTION_NAME);
		intent.putExtra(BlinkLocalService.INTENT_EXTRA_SOURCE_PACKAGE,
				CONTEXT.getPackageName());
		try {
			mInternalOperationSupport.unregisterCallback(mIInternalEventCallback, mPackageName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CONTEXT.unbindService(this);
		
		stopBroadcastReceiver();
		// CONTEXT.stopService(intent);
	}

	/**
	 * {@link BlinkDevice}의 연결 상태가 변했을 때 {@link IBlinkEventBroadcast} 콜백이 호출 되도록
	 * 설정한다.
	 */
	public final void startBroadcastReceiver() {
		if (!isRegisteredReceiver)
			CONTEXT.registerReceiver(EVENT_BR, FILTER);

		isRegisteredReceiver = true;
	}

	/**
	 * {@link BlinkDevice}의 연결 상태가 변했을 때 {@link IBlinkEventBroadcast} 콜백이 호출 되지
	 * 않도록 설정한다.
	 */
	public final void stopBroadcastReceiver() {
		if (isRegisteredReceiver)
			CONTEXT.unregisterReceiver(EVENT_BR);

		isRegisteredReceiver = false;
	}

	/** Blink Service의 설정값을 변경하도록 요청한다. */
	public final void requestConfigurationChange(String... keys) {
		if (keys != null) {
			for (String key : keys) {
				// TODO config setting
			}
		}

		Intent intent = new Intent(BROADCAST_REQUEST_CONFIGURATION_CHANGE);
		CONTEXT.sendBroadcast(intent, PERMISSION_LISTEN_STATE_MESSAGE);
	}

	/**
	 * {@link BlinkDevice}의 연결 상태가 변했을 때 호출 되는 콜백인{@link IBlinkEventBroadcast}를
	 * 설정한다.
	 */
	public final void setOnBlinkEventBroadcast(
			IBlinkEventBroadcast iBlinkEventBroadcast) {
		mBlinkEventBroadcast = iBlinkEventBroadcast;
	}

	/**
	 * Blink Service를 통해 외부 디바이스에서 데이터가 온 것을 감지하면 호출되는 콜백인
	 * {@link IInternalEventCallback}을 설정한다.
	 */
	public final boolean setIInternalEventCallback(
			IInternalEventCallback callback) {
		mIInternalEventCallback = callback;
		if (mIInternalEventCallback != null) {
			try {
				mInternalOperationSupport
						.registerCallback(mIInternalEventCallback,mPackageName);
			} catch (RemoteException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
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
				Toast.makeText(CONTEXT, intent.getStringExtra("content"),
						Toast.LENGTH_LONG).show();
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
	public void onServiceConnected(IInternalOperationSupport iSupport) {

	}

	/**
	 * Service에서 Unbinding 되었을 때 호출된다.
	 */
	public void onServiceDisconnected() {

	}

	/**
	 * Service에서 Binding이 실패하였을 때 호출된다.
	 */
	public void onServiceFailed() {

	}

	/**
	 * Database Interaction
	 */

	/**
	 * Database Sync가 발생했을 때 호출된다. Interaction에 가지고 있던 BlinkAppInfo를 최신화한다.
	 */
	private ContentObserver mContentObserver = new ContentObserver(
			new Handler()) {
		public void onChange(boolean selfChange, Uri uri) {
			Log.i(tag, "Uri : " + uri);
			// 새로운 BlinkApp이 추가되면 실행
			if (uri.equals(SqliteManager.URI_OBSERVER_SYNC)) {
				Log.i(tag, "if : URI_OBSERVER_SYNC");
				mBlinkAppInfo = local.obtainBlinkApp();
			}
		};
	};

	/**
	 * BlinkAppInfo를 서비스에 등록한다.
	 * 
	 * @param mBlinkAppInfo
	 *            : 어플리케이션의 BlinkAppInfo (Function과 Measurement만 추가하면 된다.)
	 * @return
	 */
	public boolean registerBlinkApp(BlinkAppInfo mBlinkAppInfo) {
		mBlinkAppInfo.mDevice.Device = mBlinkDevice.getName();
		mBlinkAppInfo.mDevice.MacAddress = mBlinkDevice.getAddress();
		mBlinkAppInfo.mApp.PackageName = mPackageName;
		mBlinkAppInfo.mApp.AppName = mAppName;
		mBlinkAppInfo.mApp.AppIcon = null;
		try {
			mInternalOperationSupport.registerBlinkApp(mBlinkAppInfo);
			mBlinkAppInfo.copyFromOtherObject(local.obtainBlinkApp());
			return true;
		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 테스트 데이터 생성을 위한 임시 매소드</br> BlinkAppInfo를 서비스에 등록한다. 호출한 어플리케이션의 정보를 기본적으로
	 * 등록하지 않고 주어진 값으로 등록한다.
	 * 
	 * @param mBlinkAppInfo
	 *            : 어플리케이션의 BlinkAppInfo
	 * @return
	 */
	public boolean registerExternalBlinkApp(BlinkAppInfo mBlinkAppInfo) {
		try {
			mInternalOperationSupport.registerBlinkApp(mBlinkAppInfo);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 자신의 어플리케이션 BlinkAppInfo 객체를 얻어온다.
	 * 
	 * @return
	 */
	public BlinkAppInfo obtainBlinkApp() {
		mBlinkAppInfo = local.obtainBlinkApp(mBlinkDevice.getName(),
				mPackageName);
		return mBlinkAppInfo;
	}

	/**
	 * BlinkLibrary의 ControllActivity를 여는 매소드
	 */
	public void openControlActivity() {
		try {
			mInternalOperationSupport.openControlActivity();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * Local로 동작하는 매소드를 가지고 있는 클래스</br> 서비스에 요청하지 않고 직접 DB로부터 데이터를 가져온다.
	 * 
	 * @author mementohora
	 * 
	 */
	public class Local {
		/**
		 * 자신의 어플리케이션 BlinkAppInfo 객체를 얻어온다.
		 * 
		 * @return
		 */
		public BlinkAppInfo obtainBlinkApp() {
			mBlinkAppInfo = obtainBlinkApp(mBlinkDevice.getName(), mPackageName);
			return mBlinkAppInfo;
		}

		/**
		 * 디바이스 이름과 패키지 이름을 통해 해당되는 BlinkAppInfo 객체를 얻는다.
		 * 
		 * @param DeviceName
		 *            : 얻고자 하는 BlinkApp의 디바이스 이름
		 * @param PackageName
		 *            : 얻고자 하는 BlinkApp의 패키지 이름
		 * @return
		 */
		public BlinkAppInfo obtainBlinkApp(String DeviceName, String PackageName) {
			return mBlinkDatabaseManager
					.obtainBlinkApp(DeviceName, PackageName);
		}

		/**
		 * 등록되어 있는 모든 BlinkAppInfo를 가져온다.
		 * 
		 * @return
		 */
		public List<BlinkAppInfo> obtainBlinkAppAll() {
			return mBlinkDatabaseManager.obtainBlinkApp();
		}

		/**
		 * MeasurementData를 객체 형태로 등록한다.<br>
		 * 반드시 등록한 BlinkAppInfo에 Measurement를 등록했어야 한다.
		 * 
		 * @param obj
		 *            : 등록할 데이터를 가지고 있는 객체
		 */
		public void registerMeasurementData(Object obj) {
			try {
				if (mBlinkAppInfo == null)
					obtainBlinkApp();
				mBlinkDatabaseManager.registerMeasurementData(mBlinkAppInfo,
						obj);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Class를 통해 데이터를 얻어온다.<br>
		 * 반환 받을 데이터 리스트의 타입을 명시해 주어야 한다.<br>
		 * <br>
		 * {@code obtainMeasurementData(Class<T> obj, SqliteManager.CONTAIN_DEFAULT)}
		 * 를 호출하는 것과 동일하다.
		 * 
		 * @param obj
		 *            : 얻으려는 데이터의 클래스
		 * @param type
		 *            : 반환받을 클래스 타입
		 * @return 원하는 데이터의 리스트, 없으면 빈 리스트 또는 null
		 */
		public <T> List<T> obtainMeasurementData(Class<T> obj) {
			return obtainMeasurementData(obj, null, null,
					SqliteManager.CONTAIN_DEFAULT);
		}

		/**
		 * Class를 통해 데이터를 얻어온다.<br>
		 * 반환 받을 데이터 리스트의 타입을 명시해 주어야 한다.
		 * 
		 * @param obj
		 *            : 얻으려는 데이터의 클래스
		 * @param ContainType
		 *            : 검색 타입 (SqliteManager.CONTAIN~)
		 * @return 원하는 데이터의 리스트, 없으면 빈 리스트 또는 null
		 */
		public <T> List<T> obtainMeasurementData(Class<T> obj, int ContainType) {
			return obtainMeasurementData(obj, null, null, ContainType);
		}

		/**
		 * Class를 통해 데이터를 얻어온다.<br>
		 * 반환 받을 데이터 리스트의 타입을 명시해 주어야 한다.
		 * 
		 * @param obj
		 *            : 얻으려는 데이터의 클래스
		 * @param DateTimeFrom
		 *            : 데이터 시작 일시
		 * @param DateTimeTo
		 *            : 데이터 종료 일시
		 * @param ContainType
		 *            : 검색 타입 (SqliteManager.CONTAIN~)
		 * @return 원하는 데이터의 리스트, 없으면 빈 리스트, 예외가 발생했을 경우 null
		 */
		public <T> List<T> obtainMeasurementData(Class<T> obj,
				String DateTimeFrom, String DateTimeTo, int ContainType) {
			String json;
			try {
				json = mBlinkDatabaseManager.obtainMeasurementData(obj,
						DateTimeFrom, DateTimeTo, ContainType);
				Log.i("HealthManager", json);
				// TODO class casting이 잘 되는지 확인할 것
				return gsonTreeMapConvert(obj,
						gson.fromJson(json, new TypeToken<ArrayList<T>>() {
						}.getType()));
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}

		/** Gson으로 얻은 객체를 변환한다. 
		 * @author Myungjin*/
		private final <T> List<T> gsonTreeMapConvert(Class<T> clazz,
				Object gsonTreeObject) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> list = (List<Map<String, Object>>) gsonTreeObject;
			List<T> dataList = new ArrayList<T>();

			for (Map<String, Object> map : list) {
				try {
					T data = clazz.newInstance();
					for (Entry<String, Object> entry : map.entrySet()) {
						try {
							Field field = clazz.getField(entry.getKey());
							field.setAccessible(true);
							// check primitive type
							checkTypeAndPut(field, data, entry.getValue());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					dataList.add(data);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return dataList;
		}

		/**
		 * object.field = value
		 * 
		 * @param field
		 *            object의 한 필드
		 * @param object
		 *            field가 속한 객체
		 * @param value
		 *            설정할 field의 값
		 */
		private void checkTypeAndPut(Field field, Object object, Object value)
				throws Exception {
			if (field.getType().equals(Integer.TYPE)) {
				field.setInt(object, ((Double) value).intValue());
			} else if (field.getType().equals(Byte.TYPE)) {
				field.setByte(object, (Byte) value);
			} else if (field.getType().equals(Double.TYPE)) {
				field.setDouble(object, (Double) value);
			} else if (field.getType().equals(Short.TYPE)) {
				field.setShort(object, (Short) value);
			} else if (field.getType().equals(Long.TYPE)) {
				field.setLong(object, (Long) value);
			} else if (field.getType().equals(Float.TYPE)) {
				field.setFloat(object, (Float) value);
			} else {
				field.set(object, value);
			}
		}

		/**
		 * Measurement 리스트를 통해서 데이터를 얻어온다. 시간을 조건을 줄 수 있다.
		 * 
		 * @param mMeasurementList
		 *            : 검색할 데이터의 Measurement 리스트
		 * @param DateTimeFrom
		 *            : 데이터 시작 일시
		 * @param DateTimeTo
		 *            : 데이터 종료 일시
		 * @return
		 */
		public List<MeasurementData> obtainMeasurementData(
				List<Measurement> mMeasurementList, String DateTimeFrom,
				String DateTimeTo) {
			return mBlinkDatabaseManager.obtainMeasurementData(
					mMeasurementList, DateTimeFrom, DateTimeTo);
		}

		/** Function 실행 요청을 보낸다. */
		public void startFunction(Function function) {
			if (function.Type == Function.TYPE_ACTIVITY)
				CONTEXT.startActivity(new Intent(function.Action)
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			else if (function.Type == Function.TYPE_SERIVCE)
				CONTEXT.startService(new Intent(function.Action));
			else if (function.Type == Function.TYPE_BROADCAST)
				CONTEXT.sendBroadcast(new Intent(function.Action));
		}

		/**
		 * 로그를 얻어온다.
		 * 
		 * @param Device
		 *            : 디바이스 이름
		 * @param App
		 *            : 패키지 이름
		 * @param Type
		 *            : 타입
		 * @param DateTimeFrom
		 *            : 로그 시작 일시
		 * @param DateTimeTo
		 *            : 로그 종료 일시
		 * @return
		 */
		public List<BlinkLog> obtainLog(String Device, String App, int Type,
				String DateTimeFrom, String DateTimeTo) {
			return mBlinkDatabaseManager.obtainLog(Device, App, Type,
					DateTimeFrom, DateTimeTo);
		}

		/**
		 * 로그를 얻어온다.
		 * 
		 * @param Device
		 *            : 디바이스 이름
		 * @param App
		 *            : 패키지 이름
		 * @param DateTimeFrom
		 *            : 로그 시작 일시
		 * @param DateTimeTo
		 *            : 로그 종료 일시
		 * @return
		 */
		public List<BlinkLog> obtainLog(String Device, String App,
				String DateTimeFrom, String DateTimeTo) {
			return obtainLog(Device, App, -1, DateTimeFrom, DateTimeTo);
		}

		/**
		 * 로그를 얻어온다.
		 * 
		 * @param Device
		 *            : 디바이스 이름
		 * @param DateTimeFrom
		 *            : 로그 시작 일시
		 * @param DateTimeTo
		 *            : 로그 종료 일시
		 * @return
		 */
		public List<BlinkLog> obtainLog(String Device, String DateTimeFrom,
				String DateTimeTo) {
			return obtainLog(Device, null, -1, DateTimeFrom, DateTimeTo);
		}

		/**
		 * 로그를 얻어온다.
		 * 
		 * @param DateTimeFrom
		 *            : 로그 시작 일시
		 * @param DateTimeTo
		 *            : 로그 종료 일시
		 * @return
		 */
		public List<BlinkLog> obtainLog(String DateTimeFrom, String DateTimeTo) {
			return obtainLog(null, null, -1, DateTimeFrom, DateTimeTo);
		}

		/**
		 * 모든 로그를 얻어온다.
		 * 
		 * @return
		 */
		public List<BlinkLog> obtainLog() {
			return obtainLog(null, null, -1, null, null);
		}

		/**
		 * 기존에 저장되어 있는 결과들을 지우고 새로운 쿼리를 날릴 수 있도록 한다.
		 * 
		 * @return
		 */
		public Local clear() {
			mBlinkDatabaseManager.clear();
			return this;
		}

		/**
		 * Device를 검색한다. <br>
		 * <br>
		 * 결과는 {@link #getDeviceList()}통해 얻을 수 있다.
		 * 
		 * @param where
		 * @return
		 * @see #setDeviceList(List)
		 */
		public Local queryDevice(String where) {
			mBlinkDatabaseManager.queryDevice(where);
			return this;
		}

		/**
		 * Local instance에 저장되어있는 Device를 통해 Deivce를 검색한다. <br>
		 * <br>
		 * 결과는 {@link #getAppList()}통해 얻을 수 있다.
		 * 
		 * @param where
		 *            검색 조건
		 * @return {@code Local} instance itself
		 * @see #setDeviceList(List)
		 * @see #queryDevice(String)
		 */
		public Local queryApp(String where) {
			mBlinkDatabaseManager.queryApp(where);
			return this;
		}

		/**
		 * Local instance에 저장되어있는 App을 통해 Function을 검색한다. <br>
		 * <br>
		 * 결과는 {@link #getFunctionList()}통해 얻을 수 있다.
		 * 
		 * @param where
		 *            검색 조건
		 * @return {@code Local} instance itself
		 * @see #setAppList(List)
		 * @see #queryApp(String)
		 */
		public Local queryFunction(String where) {
			mBlinkDatabaseManager.queryFunction(where);
			return this;
		}

		/**
		 * Local instance에 저장되어있는 App을 통해 Measurement를 검색한다. <br>
		 * <br>
		 * 결과는 {@link #getMeasurementList()}통해 얻을 수 있다.
		 * 
		 * @param where
		 *            검색 조건
		 * @return {@code Local} instance itself
		 * @see #setAppList(List)
		 * @see #queryApp(String)
		 */
		public Local queryMeasurement(String where) {
			mBlinkDatabaseManager.queryMeasurement(where);
			return this;
		}

		/**
		 * Local instance에 저장되어있는 Measurement를 통해 MeasurementData를 검색한다. <br>
		 * <br>
		 * 결과는 {@link #getMeasurementDataList()}통해 얻을 수 있다.
		 * 
		 * @param where
		 *            검색 조건
		 * @return {@code Local} instance itself
		 * @see #setMeasurementList(List)
		 * @see #queryMeasurement(String)
		 */
		public Local queryMeasurementData(String where) {
			mBlinkDatabaseManager.queryMeasurementData(where);
			return this;
		}

		/**
		 ** 검색된 Device의 리스트를 반환한다.
		 * 
		 * @return {@link #setDeviceList(List)}또는 {@link #queryDevice(String)}을
		 *         통해 설정된 Device의 리스트
		 */
		public List<Device> getDeviceList() {
			return mBlinkDatabaseManager.getDeviceList();
		}

		/**
		 * DeviceList를 변경한다.
		 * 
		 * @param mDeviceList
		 *            변경할 리스트
		 */
		public void setDeviceList(List<Device> mDeviceList) {
			mBlinkDatabaseManager.setDeviceList(mDeviceList);
		}

		/**
		 * 검색된 App의 리스트를 반환한다.
		 * 
		 * @return {@link #setAppList(List)}또는 {@link #queryApp(String)}을 통해 설정된
		 *         App의 리스트
		 */
		public List<App> getAppList() {
			return mBlinkDatabaseManager.getAppList();
		}

		/**
		 * AppList를 변경한다.
		 * 
		 * @param mAppList
		 *            변경할 리스트
		 */
		public Local setAppList(List<App> mAppList) {
			mBlinkDatabaseManager.setAppList(mAppList);
			return this;
		}

		/**
		 * 검색된 Function의 리스트를 반환한다.
		 * 
		 * @return {@link #setFunctionList(List)}또는
		 *         {@link #queryFunction(String)}을 통해 설정된 Function의 리스트
		 */
		public List<Function> getFunctionList() {
			return mBlinkDatabaseManager.getFunctionList();
		}

		/**
		 * FunctionList를 변경한다.
		 * 
		 * @param mFunctionList
		 */
		public Local setFunctionList(List<Function> mFunctionList) {
			mBlinkDatabaseManager.setFunctionList(mFunctionList);
			return this;
		}

		/**
		 * 검색된 Measurement의 리스트를 반환한다.
		 * 
		 * @return {@link #setMeasurementList(List)}또는
		 *         {@link #queryMeasurement(String)}을 통해 설정된 Measurement의 리스트
		 */
		public List<Measurement> getMeasurementList() {
			return mBlinkDatabaseManager.getMeasurementList();
		}

		/**
		 * MeasurementList를 변경한다.
		 * 
		 * @param mMeasurementList
		 *            변경할 리스트
		 */
		public Local setMeasurementList(List<Measurement> mMeasurementList) {
			mBlinkDatabaseManager.setMeasurementList(mMeasurementList);
			return this;
		}

		/**
		 * 검색된 MeasurementData의 리스트를 반환한다.
		 * 
		 * @return {@link #setMeasurementDataList(List)}또는
		 *         {@link #queryMeasurementData(String)}을 통해 설정된 Measurement의
		 *         리스트
		 */
		public List<MeasurementData> getMeasurementDataList() {
			return mBlinkDatabaseManager.getMeasurementDataList();
		}

		/**
		 * MeasurementDataList를 변경한다.
		 * 
		 * @param mMeasurementDataList
		 *            변경할 리스트
		 */
		public Local setMeasurementDataList(
				List<MeasurementData> mMeasurementDataList) {
			mBlinkDatabaseManager.setMeasurementDataList(mMeasurementDataList);
			return this;
		}
	}

	/**
	 * Remote와 통신할 수 있는 코드를 가지고 있는 클래스<br>
	 * 결과는 callback으로 넘겨진다.
	 * 
	 * @author mementohora
	 * 
	 */
	public class Remote {
		/**
		 * 요청 정책을 설정하는 매소드 현재 사용하지 않는다.
		 * 
		 * @param requestPolicy
		 */
		private void setRequestPolicy(int requestPolicy) {
			try {
				mInternalOperationSupport.setRequestPolicy(requestPolicy);
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		/**
		 * 외부 디바이스로 데이터 검색을 요청한다. 서비스가 클래스를 알고 있어야 하기 때문에 Blink 기본 schema에 있는
		 * 클래스만 사용 가능하다. 요청 결과는 콜백으로 반환된다.
		 * 
		 * @param obj
		 *            : 클래스 (기본 클래스만 사용 가능)
		 * @param RequestCode
		 *            : 요청을 구분할 수 있는 코드로 콜백에서 responseCode와 동일하다.
		 */
		public void obtainMeasurementData(Class<? extends DefaultSchema> obj,
				int RequestCode) {
			obtainMeasurementData(obj, null, null,
					SqliteManager.CONTAIN_DEFAULT, RequestCode);
		}

		/**
		 * 외부 디바이스로 데이터 검색을 요청한다. 서비스가 클래스를 알고 있어야 하기 때문에 Blink 기본 schema에 있는
		 * 클래스만 사용 가능하다. 요청 결과는 콜백으로 반환된다.
		 * 
		 * @param obj
		 *            : 클래스 (기본 클래스만 사용 가능)
		 * @param ContainType
		 *            : 데이터 검색 타입
		 * @param RequestCode
		 *            : 요청을 구분할 수 있는 코드로 콜백에서 responseCode와 동일하다.
		 */
		public void obtainMeasurementData(Class<? extends DefaultSchema> obj,
				int ContainType, int RequestCode) {
			obtainMeasurementData(obj, null, null, ContainType, RequestCode);
		}

		/**
		 * 외부 디바이스로 데이터 검색을 요청한다. 서비스가 클래스를 알고 있어야 하기 때문에 Blink 기본 schema에 있는
		 * 클래스만 사용 가능하다. 요청 결과는 콜백으로 반환된다.
		 * 
		 * @param obj
		 *            : 클래스 (기본 클래스만 사용 가능)
		 * @param DateTimeFrom
		 *            : 데이터 시작 일시
		 * @param DateTimeTo
		 *            : 데이터 종료 일시
		 * @param ContainType
		 *            : 데이터 검색 타입
		 * @param RequestCode
		 *            : 요청을 구분할 수 있는 코드로 콜백에서 responseCode와 동일하다.
		 */
		public void obtainMeasurementData(Class<? extends DefaultSchema> obj,
				String DateTimeFrom, String DateTimeTo, int ContainType,
				int RequestCode) {
			String ClassName = obj.getName();
			try {
				mInternalOperationSupport.obtainMeasurementData(ClassName,
						DateTimeFrom, DateTimeTo, ContainType, RequestCode,mPackageName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * MeasurementList를 기준으로 데이터를 검색한다.
		 * 
		 * @param mMeasurementList
		 *            : 검색할 데이터의 Measurement 리스트
		 * @param DateTimeFrom
		 *            : 데이터 시작 일시
		 * @param DateTimeTo
		 *            : 데이터 종료 일시
		 * @param RequestType
		 *            : 데이터 검색 타입
		 * @param RequestCode
		 *            : 요청을 구분할 수 있는 코드로 콜백에서 responseCode와 동일하다.
		 * @return
		 */
		public void obtainMeasurementData(List<Measurement> mMeasurementList,
				String DateTimeFrom, String DateTimeTo, int RequestCode) {
			try {
				mInternalOperationSupport
						.obtainMeasurementDataById(mMeasurementList,
								DateTimeFrom, DateTimeTo, RequestCode,mPackageName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 외부 디바이스의 기능을 수행한다.
		 * 
		 * @param function
		 *            : 기능을 수행할 Function 객체
		 * @param requestCode
		 *            : 요청을 구분할 수 있는 코드로 콜백에서 responseCode와 동일하다.
		 */
		public void startFunction(Function function, int requestCode) {
			try {
				mInternalOperationSupport.startFunction(function, requestCode,mPackageName);
			} catch (Exception e) {
				e.printStackTrace();
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
		public void sendMeasurementData(final BlinkAppInfo targetBlinkAppInfo,
				final String json, final int requestCode) {
			// 전송시간이 오래걸릴 경우 block 되어 어플리케이션이 죽는 경우가 발생하여 thread로 처리
			new Thread() {
				public void run() {
					try {
						targetBlinkAppInfo.mApp.AppIcon = null;
						mInternalOperationSupport.sendMeasurementData(
								targetBlinkAppInfo, json, requestCode,mPackageName);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			}.start();
		}
	}

	/**
	 * Sync 메시지 전송을 위한 임시 매소드
	 */
	public void SyncBlinkApp() {
		try {
			Log.i("test", "btn_sendMessage");
			mInternalOperationSupport.SyncBlinkApp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void SyncMeasurementData() {
		try {
			Log.i("test", "btn_sendMessage");
			mInternalOperationSupport.SyncMeasurementData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isBinding() {
		return binding;
	}

	private void setBinding(boolean binding) {
		this.binding = binding;
	}

	/**
	 * 현재 연결된 디바이스가 있는지 여부를 알려준다.
	 * 
	 * @return
	 */
	public boolean isDeviceConnected() {
		BlinkDevice[] devices = null;
		try {
			if (mInternalOperationSupport != null)
				devices = mInternalOperationSupport.obtainConnectedDeviceList();
		} catch (RemoteException e) {
		}
		return !(devices == null || devices.length == 0);
	}
}
