package kr.poturns.blink.internal;

import java.util.HashMap;
import java.util.HashSet;

import kr.poturns.blink.internal.DeviceAnalyzer.Identity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import android.bluetooth.BluetoothGatt;
import android.os.RemoteCallbackList;
import android.util.Log;


/**
 * 서비스가 관리하는 리소스의 현황을 관리하고 접근할 수 있는 모듈.
 * 
 * @author Yeonho.Kim
 * @since 2014.08.20
 *
 */
public class ServiceKeeper {

	// *** STATIC DECLARATION *** //
	private static ServiceKeeper sInstance = null;
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static ServiceKeeper getInstance(BlinkLocalBaseService context) {
		if (sInstance == null && context != null)
			sInstance = new ServiceKeeper(context);
		return sInstance;
	}

	
	// *** FIELD DECLARATION *** //
	private final BlinkLocalBaseService KEEPER_CONTEXT;
	private final HashSet<BlinkDevice> DISCOVERY_SET;
	
	private final HashMap<BlinkDevice, Identity> BLINK_NETWORK_MAP;
	private final HashMap<BlinkDevice, ClassicLinkThread> CLASSIC_CONN_MAP;
	private final HashMap<BlinkDevice, BluetoothGatt> LE_CONN_MAP;
	
	private final HashMap<String, ConnectionSupportBinder> BINDER_MAP;
	private final RemoteCallbackList<IInternalEventCallback> EVENT_CALLBACK_LIST;
	
	private BlinkDevice mSelfDevice;
	
	private ServiceKeeper(BlinkLocalBaseService context) {
		KEEPER_CONTEXT = context;
		
		DISCOVERY_SET = new HashSet<BlinkDevice>();
		BLINK_NETWORK_MAP = new HashMap<BlinkDevice, Identity>();
		CLASSIC_CONN_MAP = new HashMap<BlinkDevice, ClassicLinkThread>();
		LE_CONN_MAP = new HashMap<BlinkDevice, BluetoothGatt>();
		BINDER_MAP = new HashMap<String, ConnectionSupportBinder>();
		EVENT_CALLBACK_LIST = new RemoteCallbackList<IInternalEventCallback>();
	}
	
	void destroy() {
		
	}
	
	void addDiscovery(BlinkDevice device) {
		DISCOVERY_SET.add(device);
	}

	void clearDiscovery() {
		DISCOVERY_SET.clear();
	}
	
	void addConnection(BlinkDevice device, ClassicLinkThread thread) {
		CLASSIC_CONN_MAP.put(device, thread);
		
	}
	
	void addConnection(BlinkDevice device, BluetoothGatt gatt) {
		LE_CONN_MAP.put(device, gatt);
	}

	void removeConnection(BlinkDevice device) {
		LE_CONN_MAP.remove(device);
		CLASSIC_CONN_MAP.remove(device);
	}
	
	Object getConnectionObject(BlinkDevice device) {
		if (device.isLESupported()) {
			return LE_CONN_MAP.get(device);
			
		} else
			return CLASSIC_CONN_MAP.get(device);
	}
	
	
	void clearConnection() {
		LE_CONN_MAP.clear();
		CLASSIC_CONN_MAP.clear();
	}
	
	void registerBinder(String packageName, ConnectionSupportBinder binder) {
		BINDER_MAP.put(packageName, binder);
	}
	
	/**
	 * 
	 * @return
	 */
	public BlinkDevice[] obtainDiscoveryArray() {
		BlinkDevice[] lists = new BlinkDevice[DISCOVERY_SET.size()];
		return DISCOVERY_SET.toArray(lists);
	}

	/**
	 * 
	 * @return
	 */
	public BlinkDevice[] obtainConnectedArray() {
		BlinkDevice[] lists = new BlinkDevice[LE_CONN_MAP.size() + CLASSIC_CONN_MAP.size()];
		
		int index = 0; 
		for (Object device : LE_CONN_MAP.keySet().toArray()) 
			lists[index++] = (BlinkDevice) device;
		for (Object device : CLASSIC_CONN_MAP.keySet().toArray())
			lists[index++] = (BlinkDevice) device;
		
		return lists;
	}
	
	public BlinkDevice getSelfDevice() {
		return mSelfDevice;
	}
	
	public void setSelfDevice(BlinkDevice device) {
		mSelfDevice = device;
	}

	void introduceToBlinkNetwork(BlinkDevice device) {
		sendMessageToDevice(device, mSelfDevice);
	}
	
	void updateBlinkNetwork(BlinkDevice device) {
		// TODO :
		Log.e("ServiceKeeper_updateNetworkMap", device.toString());
		
		Identity identity = BLINK_NETWORK_MAP.get(BlinkDevice.load(device.getAddress()));
		if (identity == null) {
			Identity mOtherDeviceIdentity = device.getIdentity();
			
			// 새로 Identity를 등록한다.
			if (Identity.UNKNOWN.equals(mOtherDeviceIdentity)) {
				
				
			} else if (Identity.PERIPHERALS.equals(mOtherDeviceIdentity)){
				BLINK_NETWORK_MAP.put(device, Identity.PERIPHERALS);
			
			} else if (Identity.CORE.equals(mOtherDeviceIdentity)) {
				
			}
			
			
		} else if (identity.equals(device.getIdentity())) {
			// 등록된 Identity가 같을 경우, 데이터 갱신 & Pass
			BlinkDevice.load(device);
			
			
		} else {
			// 등록된 Identity와 다를 경우, 
			
		}
		
	}
	
	private Identity calculate(BlinkDevice otherDevice) {

		int mSelfDevicePoint = mSelfDevice.getIdentityPoint();
		int mOtherDevicePoint = otherDevice.getIdentityPoint();
		Identity mSelfDeviceIdentity = mSelfDevice.getIdentity();
		Identity mOtherDeviceIdentity = otherDevice.getIdentity();

		if (mSelfDeviceIdentity.ordinal() == mOtherDeviceIdentity.ordinal()) {
			
			if (mSelfDeviceIdentity.equals(Identity.CORE)) {
				
				
			}
			
			if (mSelfDevicePoint > mOtherDevicePoint) {
				
				
			} else if (mSelfDevicePoint < mOtherDevicePoint) {
				
				
			} else {
				// 포인트도 같을 경우, User의 판단:
			}
			
		} else {
			
		}
		
		return otherDevice.getIdentity();
	}
	
	public void sendMessageToDevice(BlinkDevice device, Object msg) {
		if (device.isLESupported()) {
			
			
		} else {
			ClassicLinkThread thread = CLASSIC_CONN_MAP.get(device);
			if (thread != null)
				thread.sendMessageToDevice(msg);
			
		}
	}
}
