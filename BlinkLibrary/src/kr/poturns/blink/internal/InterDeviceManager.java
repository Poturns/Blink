package kr.poturns.blink.internal;

import java.util.UUID;

import kr.poturns.blink.internal.comm.BlinkProfile;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

/**
 * 디바이스 간의 통신을 담당하는 모듈. 
 * BroadcastReceiver로서 필요한 Broadcast를 필터링한다. 
 * 블루투스 커뮤니케이션을 관리하고 조절한다 ({@link BluetoothAssistant}).
 * 
 * @author Yeonho.Kim
 * @since 2014.07.12
 *
 */
public class InterDeviceManager extends BroadcastReceiver implements LeScanCallback {
	// DEVELOPING **************
	static boolean onLog = true;
	
	
	// *** CONSTANT DECLARATION *** //
	/**
	 * BroadcastReceiver Intent Action명 
	 */
	public static final String ACTION_NAME = "kr.poturns.blink.internal.action.InterDeviceManager";
	
	
	
	// *** STATIC DECLARATION *** //
	/**
	 * InterDeviceManager의 Singleton-인스턴스
	 */
	private static InterDeviceManager sInstance = null;
	
	/**
	 * InterDeviceManager의 Singleton-인스턴스를 반환한다. 
	 *  
	 * @param context ( :{@link BlinkLocalBaseService} )
	 * @return context가 Null일 경우, 기존의 Instance를 반환한다.
	 */
	static InterDeviceManager getInstance(Context context) {
		if (sInstance == null && context != null)
			sInstance = new InterDeviceManager(context);
		return sInstance;
	}
	
	

	// *** FIELD DECLARATION *** //
	final Context MANAGER_CONTEXT;
	final RemoteCallbackList<IInternalEventCallback> EVENT_CALLBACK_LIST;
	
	private InterDeviceManager(Context context) {
		this.MANAGER_CONTEXT = context;
		this.EVENT_CALLBACK_LIST = ((BlinkLocalBaseService) context).EVENT_CALLBACK_LIST;
		
		initiate();
	}
	
	private BluetoothAssistant mAssistant;
	
	private boolean isInitiated = false;
	void initiate() {
		if (!isInitiated && (isInitiated = true)) {
			MANAGER_CONTEXT.registerReceiver(this, BluetoothAssistant.obtainIntentFilter());
			
			mAssistant = BluetoothAssistant.getInstance(this);
			mAssistant.inititiate();
		}
	}
	
	private boolean isDestroyed = false;
	void destroy() {
		if (!isDestroyed && (isDestroyed = true)) {
			MANAGER_CONTEXT.unregisterReceiver(this);
			
			mAssistant.destroy();
			mAssistant = null;
			
			sInstance = null;
		}
	}

	
	
	// *** CALLBACK DECLARATION *** //
	@Override
	protected void finalize() throws Throwable {
		this.destroy();
		super.finalize();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (onLog)
			Log.d("InterDeviceManager_onReceive()", ": "+action+" :" );
		
		if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
			// 블루투스 상태 변화 감지
			int curr_state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			int prev_state = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.ERROR);
			
			handleStateChanged(prev_state, curr_state);
				
		} else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
			// 블루투스 스캔 모드 변화 감지
			
		} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			if (onLog)
				Log.d("InterDeviceManager", intent.getStringExtra(BluetoothDevice.EXTRA_NAME) + " : " );

			// 블루투스 Discovery, 디바이스 발견
			BluetoothDevice mOrigin = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			BlinkDevice deviceX = BlinkDevice.load(mOrigin);
			
			if (onLog)
				Log.d("InterDeviceManager", " Address = " + deviceX.getAddress());
			
			mAssistant.onDeviceDiscovered(deviceX);
			
			// 등록된 Callback을 통해 전달.
			int size = EVENT_CALLBACK_LIST.beginBroadcast();
			for (int i = 0; i < size; i++) {	
				try {
					EVENT_CALLBACK_LIST.getBroadcastItem(i).onDeviceDiscovered(deviceX);
					
				} catch (RemoteException e) { ; }
			}
			EVENT_CALLBACK_LIST.finishBroadcast();

			
		} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
			// 블루투스 (Classic?) Discovery 시작
			
		} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
			// 블루투스 (Classic?) Discovery 완료
			switch (mStartDiscoveryType) {
			case BluetoothDevice.DEVICE_TYPE_DUAL:
			case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
				if (mAssistant != null)
					mAssistant.startDiscovery(BluetoothDevice.DEVICE_TYPE_LE, false);
			}
		
			
		} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
			// 해당 디바이스와 블루투스 연결 성립

			BluetoothDevice mOrigin = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			BlinkDevice deviceX = BlinkDevice.load(mOrigin);
			
			// 등록된 Callback을 통해 전달.
			int size = EVENT_CALLBACK_LIST.beginBroadcast();
			for (int i = 0; i < size; i++) {	
				try {
					EVENT_CALLBACK_LIST.getBroadcastItem(i).onDeviceConnected(deviceX);
								
				} catch (RemoteException e) { ; }
			}
			EVENT_CALLBACK_LIST.finishBroadcast();
			
		} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
			// 해당 디바이스와 블루투스 연결 해제

			BluetoothDevice mOrigin = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			BlinkDevice deviceX = BlinkDevice.load(mOrigin);
			
			mAssistant.onDeviceDisconnected(deviceX);

			// 등록된 Callback을 통해 전달.
			int size = EVENT_CALLBACK_LIST.beginBroadcast();
			for (int i = 0; i < size; i++) {	
				try {
					EVENT_CALLBACK_LIST.getBroadcastItem(i).onDeviceDisconnected(deviceX);
								
				} catch (RemoteException e) { ; }
			}
			EVENT_CALLBACK_LIST.finishBroadcast();
			
		}
	}

	/**
	 * 
	 * @param prev_state 이전 상태
	 * @param curr_state 현재 상태
	 */
	private void handleStateChanged(int prev_state, int curr_state) {
		switch (curr_state) {
		case BluetoothAdapter.STATE_ON:
			if (prev_state != curr_state && mAssistant != null)
				mAssistant.onBluetoothStateOn();
			break;
	
		case BluetoothAdapter.STATE_OFF:
			if (prev_state != curr_state && mAssistant != null)
				mAssistant.onBluetoothStateOff();
			break;

		case BluetoothAdapter.STATE_TURNING_OFF:
			if (prev_state != curr_state && mAssistant != null) 
				mAssistant.onBluetoothStateTurningOff();
			break;
			
		case BluetoothAdapter.STATE_TURNING_ON: 
		}
	}

	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		if (onLog)
			Log.d("InterDeviceManager", "[LE] " + device.getName() + " : " + device.getUuids()[0]);
		
		BlinkDevice deviceX = BlinkDevice.load(device);

		// 등록된 Callback을 통해 전달.
		int size = EVENT_CALLBACK_LIST.beginBroadcast();
		for (int i = 0; i < size; i++) {	
			try {
				EVENT_CALLBACK_LIST.getBroadcastItem(i).onDeviceDiscovered(deviceX);
							
			} catch (RemoteException e) { ; }
		}
		EVENT_CALLBACK_LIST.finishBroadcast();
	}
	
	public void startListeningAsServer() {
		BluetoothAssistant assistant = BluetoothAssistant.getInstance(this);
		assistant.startClassicServer(BlinkProfile.UUID_BLINK, true);
		
		if (DeviceAnalyzer.getInstance(MANAGER_CONTEXT).hasBluetoothLE)
			assistant.startLeServer(new BluetoothGattService(
					BlinkProfile.UUID_BLINK, BluetoothGattService.SERVICE_TYPE_PRIMARY));
	}
	
	public void stopListeningAsServer() {
		BluetoothAssistant assistant = BluetoothAssistant.getInstance(this);
		assistant.stopClassicServer();

		if (DeviceAnalyzer.getInstance(MANAGER_CONTEXT).hasBluetoothLE)
			assistant.stopLeServer();
	}
	
	
	
	
	// *** BINDING DECLARATION *** //
	private int mStartDiscoveryType = BluetoothDevice.DEVICE_TYPE_UNKNOWN;
	
	
	final IInternalOperationSupport.Stub mInternalOperationSupporter = new IInternalOperationSupport.Stub() {

		@Override
		public boolean registerCallback(IInternalEventCallback callback) throws RemoteException {
			if (callback != null)
				return EVENT_CALLBACK_LIST.register(callback);
			
			return false;
		}
		
		@Override
		public boolean unregisterCallback(IInternalEventCallback callback) throws RemoteException {
			if (callback != null) 
				return EVENT_CALLBACK_LIST.unregister(callback);
			
			return false;
		}

		@Override
		public void startDiscovery(int type) throws RemoteException {
			switch (mStartDiscoveryType = type) {
			
			case BluetoothDevice.DEVICE_TYPE_CLASSIC:
			case BluetoothDevice.DEVICE_TYPE_DUAL:
			case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
				if (mAssistant != null)
					mAssistant.startDiscovery(BluetoothDevice.DEVICE_TYPE_CLASSIC);
				break;
				
			case BluetoothDevice.DEVICE_TYPE_LE:
				if (mAssistant != null) 
					mAssistant.startDiscovery(BluetoothDevice.DEVICE_TYPE_LE);
				
			}
		}
		
		@Override
		public void stopDiscovery() throws RemoteException {
			if (mAssistant != null)
				mAssistant.stopDiscovery();
		}

		@Override
		public BlinkDevice[] obtainCurrentDiscoveryList() throws RemoteException {
			if (mAssistant != null)
				return mAssistant.obtainCurrentDiscoveryList();
			
			return null;
		}

		@Override
		public void startListeningAsServer() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void stopListeningAsServer() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void connectDevice(BlinkDevice deviceX) throws RemoteException {
			// TODO Auto-generated method stub
			if (mAssistant != null)
				mAssistant.connectToDeviceAsClient(deviceX, BlinkProfile.UUID_BLINK);
		}

		@Override
		public void disconnectDevice(BlinkDevice deviceX) throws RemoteException {
			// TODO Auto-generated method stub
			if (mAssistant != null)
				mAssistant.disconnectFromDeviceAsClient(deviceX);
		}

		@Override
		public BlinkDevice[] obtainConnectedDeviceList() throws RemoteException {
			// TODO Auto-generated method stub
			return mAssistant.obtainCurrentDiscoveryList();
		}

		@Override
		public void sendBlinkMessages(BlinkDevice target, String jsonMsg) throws RemoteException {

			mAssistant.onMessageSentTo(jsonMsg, target);
		}
		
	};
}
