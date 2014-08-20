package kr.poturns.blink.internal;

import java.util.HashMap;
import java.util.HashSet;

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
	
	private final HashMap<BlinkDevice, ClassicLinkThread> CLASSIC_CONN_MAP;
	private final HashMap<BlinkDevice, BluetoothGatt> LE_CONN_MAP;
	
	private final HashMap<String, ConnectionSupportBinder> BINDER_MAP;
	private final RemoteCallbackList<IInternalEventCallback> EVENT_CALLBACK_LIST;
	
	private BlinkDevice mSelfDevice;
	
	private ServiceKeeper(BlinkLocalBaseService context) {
		KEEPER_CONTEXT = context;
		
		DISCOVERY_SET = new HashSet<BlinkDevice>();
		CLASSIC_CONN_MAP = new HashMap<BlinkDevice, ClassicLinkThread>();
		LE_CONN_MAP = new HashMap<BlinkDevice, BluetoothGatt>();
		BINDER_MAP = new HashMap<String, ConnectionSupportBinder>();
		EVENT_CALLBACK_LIST = new RemoteCallbackList<IInternalEventCallback>();
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
	
	public void updateNetworkMap(BlinkDevice device) {
		// TODO :
		Log.e("ServiceKeeper_updateNetworkMap", device.toString());
		
		BlinkDevice.load(device);
	}
	
	public void sendSelfDeviceTo(BlinkDevice device) {
		sendMessageToDevice(device, mSelfDevice);
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
