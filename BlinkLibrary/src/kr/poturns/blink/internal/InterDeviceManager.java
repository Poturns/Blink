package kr.poturns.blink.internal;

import java.io.Serializable;

import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.IBlinkEventBroadcast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
	static InterDeviceManager getInstance(BlinkLocalBaseService context) {
		if (sInstance == null && context != null)
			sInstance = new InterDeviceManager(context);
		return sInstance;
	}
	
	

	// *** FIELD DECLARATION *** //
	final BlinkLocalBaseService MANAGER_CONTEXT;

	private BluetoothAssistant mAssistant;
	private ServiceKeeper mServiceKeeper;
	
	private boolean isInitiated = false;
	private boolean isDestroyed = false;
	
	private InterDeviceManager(BlinkLocalBaseService context) {
		this.MANAGER_CONTEXT = context;
		
		initiate();
	}
	
	void initiate() {
		if (!isDestroyed && !isInitiated && (isInitiated = true)) {
			IntentFilter mIntentFiler = BluetoothAssistant.obtainIntentFilter();
			mIntentFiler.addAction(IBlinkEventBroadcast.BROADCAST_REQUEST_CONFIGURATION_CHANGE);
			
			MANAGER_CONTEXT.registerReceiver(this, mIntentFiler);
			
			mServiceKeeper = ServiceKeeper.getInstance(MANAGER_CONTEXT);
			mAssistant = BluetoothAssistant.getInstance(this);
			mAssistant.inititiate();
		}
		
	}
	
	void update() {
		if (!isDestroyed && isInitiated) {
			
		}
	}
	
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
		Log.d("InterDeviceManager_onReceive()", ": "+action+" :" );
		
		if (isDestroyed)
			return;
		
		if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
			// 블루투스 상태 변화 감지
			
			int curr_state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			int prev_state = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.ERROR);
			
			handleStateChanged(prev_state, curr_state);
				
			
		} else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
			// 블루투스 스캔 모드 변화 감지
			
			
		//} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
			// 블루투스 (Classic?) Discovery 시작
			
			
		} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			// 블루투스 Discovery, 디바이스 발견
			
			BluetoothDevice origin = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			origin.fetchUuidsWithSdp();

			BlinkDevice.load(origin).setDiscovered(true);
			
			Log.d("InterDeviceManager", intent.getStringExtra(BluetoothDevice.EXTRA_NAME) + " : " );
			Log.d("InterDeviceManager", " Address = " + origin.getAddress() + " / Type: " + origin.getType());

			
		} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
			// 블루투스 (Classic) Discovery 완료
			
			switch (mStartDiscoveryType) {
			case BluetoothDevice.DEVICE_TYPE_DUAL:
			case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
				if (mAssistant != null)
					mAssistant.startDiscovery(BluetoothDevice.DEVICE_TYPE_LE, false);
			}

			
		} else if (BluetoothDevice.ACTION_UUID.equals(action)) {
			// 블루투스 UUID 탐색 >> 결과 UUID 값을 BlinkDevice Cache에 갱신한다.
			
			BluetoothDevice origin = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			BlinkDevice device = BlinkDevice.load(origin);
			device.setDiscovered(true);
			
			mServiceKeeper.addDiscovery(device);
			
			// Broadcasting...
			Intent mActionFound = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_DISCOVERED);
			mActionFound.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) device);
			MANAGER_CONTEXT.sendBroadcast(mActionFound, IBlinkEventBroadcast.PERMISSION_LISTEN_STATE_MESSAGE);
			
			
		} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
			// 블루투스 Pairing 상태 변화 감지
			
			int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
			if (BluetoothDevice.BOND_BONDED == state) {
				
			}
			
			
		} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
			// 해당 디바이스와 블루투스 연결 성립

			BluetoothDevice origin = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			BlinkDevice device = BlinkDevice.load(origin);
			device.setConnected(true);

			// Broadcasting...
			Intent mActionConnected = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_CONNECTED);
			mActionConnected.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) device);
			MANAGER_CONTEXT.sendBroadcast(mActionConnected, IBlinkEventBroadcast.PERMISSION_LISTEN_STATE_MESSAGE);
			
			Log.d("InterDeviceManager", "ACTION_ACL_CONNECTED : " + origin.getAddress());
			
		} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
			// 해당 디바이스와 블루투스 연결 해제

			BluetoothDevice origin = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			BlinkDevice device = BlinkDevice.load(origin);
			device.setConnected(false);

			mServiceKeeper.removeConnection(device);
			
			// Broadcasting...
			Intent mActionConnected = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_DISCONNECTED);
			mActionConnected.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) device);
			MANAGER_CONTEXT.sendBroadcast(mActionConnected, IBlinkEventBroadcast.PERMISSION_LISTEN_STATE_MESSAGE);
			
			Log.d("InterDeviceManager", "ACTION_ACL_DISCONNECTED : " + origin.getAddress());
			
			
		} else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
			// 블루투스 LE 연결 상태 변화 감지

			BluetoothDevice origin = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			BlinkDevice device = BlinkDevice.load(origin);
			
			int curr_state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
			int prev_state = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, BluetoothAdapter.ERROR);
			
			handleConnectionChanged(device, prev_state, curr_state);
			
			
		} else if (IBlinkEventBroadcast.BROADCAST_REQUEST_CONFIGURATION_CHANGE.equals(action)) {
			// 서비스 환경설정 값 변경 요청..
			
			// TODO : Preference 에서 설정값 적용해오기.			
		}
	}

	/**
	 * 
	 * @param prev_state 이전 상태
	 * @param curr_state 현재 상태
	 */
	private void handleStateChanged(int prev_state, int curr_state) {
		if (mAssistant == null) {
			Log.e("InterDeviceManager_handleStateChanged()", "mAssistant is NULL");
			return ;
		}
		
		switch (curr_state) {
		case BluetoothAdapter.STATE_ON:
			mAssistant.onBluetoothStateOn();
			break;
	
		case BluetoothAdapter.STATE_OFF:
			mAssistant.onBluetoothStateOff();
			break;

		case BluetoothAdapter.STATE_TURNING_OFF:
			mAssistant.onBluetoothStateTurningOff();
			break;
			
		case BluetoothAdapter.STATE_TURNING_ON: 
			break;
		}
	}
	
	/**
	 * 
	 * @param device
	 * @param prev_state 이전 상태
	 * @param curr_state 현재 상태
	 */
	private void handleConnectionChanged(BlinkDevice device, int prev_state, int curr_state) {
		if (mAssistant == null) {
			Log.e("InterDeviceManager_handleStateChanged()", "mAssistant is NULL");
			return ;
		}
		
		Intent mActionIntent = null;
		switch (curr_state) {
		case BluetoothAdapter.STATE_CONNECTED:
			Log.d("InterDeviceManager_handleConnectionChanged()", "STATE_CONNECTED");
			device.setConnected(true);
			
			mActionIntent = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_CONNECTED);
			break;
	
		case BluetoothAdapter.STATE_DISCONNECTED:
			Log.d("InterDeviceManager_handleConnectionChanged()", "STATE_DISCONNECTED");
			device.setConnected(false);
			
			mActionIntent = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_DISCONNECTED);
			break;

		default:
			return;
		}

		// Broadcasting...
		mActionIntent.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) device);
		MANAGER_CONTEXT.sendBroadcast(mActionIntent, IBlinkEventBroadcast.PERMISSION_LISTEN_STATE_MESSAGE);
	}

	@Override
	public void onLeScan(BluetoothDevice origin, int rssi, byte[] scanRecord) {
		Log.d("InterDeviceManager", "[LE] " + origin.getName() + " : " + origin.getUuids()[0]);
		
		BlinkDevice device = BlinkDevice.load(origin);
		device.setDiscovered(true);
		
		if (scanRecord != null) {
			
		}
		
		mServiceKeeper.addDiscovery(device);

		// Broadcasting...
		Intent mActionDiscovered = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_DISCOVERED);
		mActionDiscovered.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) device);
		MANAGER_CONTEXT.sendBroadcast(mActionDiscovered, IBlinkEventBroadcast.PERMISSION_LISTEN_STATE_MESSAGE);
	}
	
	
	
	// *** BINDING DECLARATION *** //
	/**
	 * 
	 */
	int mStartDiscoveryType = BluetoothDevice.DEVICE_TYPE_UNKNOWN;

}
