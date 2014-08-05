package kr.poturns.blink.internal;

import kr.poturns.blink.internal.comm.BluetoothDeviceExtended;
import kr.poturns.blink.internal.comm.InterDeviceEventListener;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
	static InterDeviceManager getInstance(Context context) {
		if (sInstance == null && context != null)
			sInstance = new InterDeviceManager(context);
		return sInstance;
	}
	

	// *** FIELD DECLARATION *** //
	private final Context MANAGER_CONTEXT;
	
	private InterDeviceManager(Context context) {
		this.MANAGER_CONTEXT = context;
		
		init();
	}
	
	void init() {
		MANAGER_CONTEXT.registerReceiver(this, BluetoothAssistant.obtainIntentFilter());
		
		BluetoothAssistant mAssistant = BluetoothAssistant.getInstance(this);
		mAssistant.init();
	}
	
	void destroy() {
		MANAGER_CONTEXT.unregisterReceiver(this);
		
		sInstance = null;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		
		destroy();
	}
	
	
	// *** CALLBACK DECLARATION *** //
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
			int curr_state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			int prev_state = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.ERROR);
			
			handleStateMonitoring(prev_state, curr_state);
			
		} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			BluetoothDeviceExtended deviceX = new BluetoothDeviceExtended(
					(BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
			
			BluetoothAssistant.getInstance(this).addDiscoveryDevice(deviceX);
			
			if (mInterDeviceListener != null)
				mInterDeviceListener.onDeviceDiscovered(deviceX);
			
		} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
			
			if (mInterDeviceListener != null) {
				BluetoothDeviceExtended deviceX = new BluetoothDeviceExtended(
						(BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
				
				mInterDeviceListener.onDeviceConnected(deviceX);
			}
			
		} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

			if (mInterDeviceListener != null) {
				BluetoothDeviceExtended deviceX = new BluetoothDeviceExtended(
						(BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
				
				mInterDeviceListener.onDeviceDisconnected(deviceX);
			}
		}
		
		else if (BluetoothAssistant.ACTION_STATE_ON.equals(action)) {
			BluetoothAssistant.getInstance(this).startClassicServer(null, true);
		}
	}

	private void handleStateMonitoring(int prev_state, int curr_state) {
		switch (curr_state) {
		case BluetoothAdapter.STATE_CONNECTED:
			
		case BluetoothAdapter.STATE_CONNECTING:
			
		case BluetoothAdapter.STATE_DISCONNECTING:
			
		case BluetoothAdapter.STATE_DISCONNECTED:
			
			break;
			
		case BluetoothAdapter.STATE_TURNING_ON: 
			
		case BluetoothAdapter.STATE_ON:
			
		case BluetoothAdapter.STATE_TURNING_OFF:	
		
		case BluetoothAdapter.STATE_OFF:
			
			break;
		}
	}

	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

		if (mInterDeviceListener != null) {
			BluetoothDeviceExtended deviceX = new BluetoothDeviceExtended(device);
			
			mInterDeviceListener.onDeviceDiscovered(deviceX);
		}
		
	}
	
	private InterDeviceEventListener mInterDeviceListener = null;

	public InterDeviceEventListener getInterDeviceListener() {
		return mInterDeviceListener;
	}
	
	public void setInterDeviceListener(InterDeviceEventListener mInterDeviceListener) {
		this.mInterDeviceListener = mInterDeviceListener;
	}
	
	Context getContext() {
		return MANAGER_CONTEXT;
	}
}
