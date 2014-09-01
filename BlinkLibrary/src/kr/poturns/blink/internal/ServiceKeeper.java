
package kr.poturns.blink.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

import kr.poturns.blink.internal.DeviceAnalyzer.Identity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkSupportBinder;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import android.bluetooth.BluetoothGatt;
import android.os.RemoteCallbackList;


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
	
	private final TreeMap<BlinkDevice, Identity> BLINK_NETWORK_MAP;
	private final HashMap<BlinkDevice, ClassicLinkThread> CLASSIC_CONN_MAP;
	private final HashMap<BlinkDevice, BluetoothGatt> LE_CONN_MAP;
	
	private final HashMap<String, BlinkSupportBinder> BINDER_MAP;
	private final HashMap<String, RemoteCallbackList<IInternalEventCallback>> CALLBACK_MAP;
	
	private ServiceKeeper(BlinkLocalBaseService context) {
		KEEPER_CONTEXT = context;
		
		DISCOVERY_SET = new HashSet<BlinkDevice>();
		BLINK_NETWORK_MAP = new TreeMap<BlinkDevice, Identity>();
		CLASSIC_CONN_MAP = new HashMap<BlinkDevice, ClassicLinkThread>();
		LE_CONN_MAP = new HashMap<BlinkDevice, BluetoothGatt>();
		BINDER_MAP = new HashMap<String, BlinkSupportBinder>();
		CALLBACK_MAP = new HashMap<String,RemoteCallbackList<IInternalEventCallback>>();
	}
	
	void destroy() {
		clearDiscovery();
		
	}
	
	/**
	 * 
	 * @param device
	 */
	void addDiscovery(BlinkDevice device) {
		if (device != null && device.isDiscovered()) {
			device.setDiscovered(true);
			DISCOVERY_SET.add(device);
		}
	}

	/**
	 * 
	 */
	void clearDiscovery() {
		for (BlinkDevice device : DISCOVERY_SET)
			device.setDiscovered(false);
		
		DISCOVERY_SET.clear();
	}
	
	/**
	 * 
	 * @param device
	 * @param thread
	 */
	void addConnection(BlinkDevice device, ClassicLinkThread thread) {
		if (device != null) {
			device.setConnected(true);
			CLASSIC_CONN_MAP.put(device, thread);
		}
	}
	
	/**
	 * 
	 * @param device
	 * @param gatt
	 */
	void addConnection(BlinkDevice device, BluetoothGatt gatt) {
		if (device != null) {
			device.setConnected(true);
			LE_CONN_MAP.put(device, gatt);
		}
	}

	/**
	 * 
	 * @param device
	 */
	void removeConnection(BlinkDevice device) {
		if (device == null)
			return ;

		device.setConnected(false);
		
		if (device.isLESupported()) {
			BluetoothGatt mGatt = LE_CONN_MAP.remove(device);
			if (mGatt != null) {
				mGatt.close();
			}
			
		} else {
			ClassicLinkThread mLinkThread = CLASSIC_CONN_MAP.remove(device);
			if (mLinkThread != null) {
				mLinkThread.destroyThread();
			}
		}
	}
	
	/**
	 * 
	 * @param device
	 * @return
	 */
	Object getConnectionObject(BlinkDevice device) {
		if (device == null)
			return null;
		
		if (device.isLESupported()) {
			return LE_CONN_MAP.get(device);
			
		} else
			return CLASSIC_CONN_MAP.get(device);
	}
	
	/**
	 * 
	 */
	void clearConnection() {
		for (BluetoothGatt gatt : LE_CONN_MAP.values())
			gatt.close();
		
		for (ClassicLinkThread thread : CLASSIC_CONN_MAP.values())
			thread.destroyThread();
	}
	
	/**
	 * 
	 * @param packageName
	 * @param binder
	 */
	void registerBinder(String packageName, BlinkSupportBinder binder) {
		BINDER_MAP.put(packageName, binder);
	}
	
	/**
	 * 
	 * @param packageName
	 * @return
	 */
	BlinkSupportBinder obtainBinder(String packageName) {
		if (packageName != null)
			return BINDER_MAP.get(packageName);
		return null;
	}
	
	/**
	 * 
	 * @param packageName
	 * @return
	 */
	boolean releaseBinder(String packageName) {
		if (packageName != null)
			return (BINDER_MAP.remove(packageName) != null);
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public BlinkDevice[] obtainDiscoveredDevices() {
		BlinkDevice[] lists = new BlinkDevice[DISCOVERY_SET.size()];
		return DISCOVERY_SET.toArray(lists);
	}

	/**
	 * 
	 * @return
	 */
	public BlinkDevice[] obtainConnectedDevices() {
		BlinkDevice[] lists = new BlinkDevice[LE_CONN_MAP.size() + CLASSIC_CONN_MAP.size()];
		
		int index = 0; 
		for (Object device : LE_CONN_MAP.keySet().toArray()) 
			lists[index++] = (BlinkDevice) device;
		for (Object device : CLASSIC_CONN_MAP.keySet().toArray())
			lists[index++] = (BlinkDevice) device;
		
		return lists;
	}
	
	/**
	 * 
	 * @param identity
	 * @return
	 */
	public BlinkDevice[] obtainIdentityDevices(Identity identity) {
		ArrayList<BlinkDevice> mDeviceList = new ArrayList<BlinkDevice>();
		for (Entry<BlinkDevice, Identity> entry : BLINK_NETWORK_MAP.entrySet()) {
			if (entry.getValue() == identity)
				mDeviceList.add(entry.getKey());
		}
		
		BlinkDevice[] lists = new BlinkDevice[mDeviceList.size()];
		return mDeviceList.toArray(lists);
	}
	
	/**
	 * 현재 Center 역할을 하고 있는 Device를 반환한다.
	 * 
	 * @return
	 */
	public BlinkDevice obtainCurrentCenterDevice() {
		
		return BLINK_NETWORK_MAP.lastKey();
	}
	
	/**
	 * 본 디바이스의 정보를 타겟 디바이스에게 전달한다.
	 * 
	 * @param device
	 */
	void introduceToBlinkNetwork(BlinkDevice device) {
		sendMessageToDevice(device, BlinkDevice.HOST);
	}
	
//	/**
//	 * BlinkNetwork의 Identity를 제어하는 메소드.
//	 * 
//	 * @param device
//	 */
//	void updateBlinkNetwork(BlinkDevice device) {
//		// TODO :
//
//		int myDevicePoint = BlinkDevice.HOST.getIdentityPoint();
//		int myDeviceGroupID = BlinkDevice.HOST.getGroupID();
//		boolean isMyFirst = (myDeviceGroupID == 0);
//		Identity myDeviceIdentity = BlinkDevice.HOST.getIdentity();
//
//		int otherDevicePoint = device.getIdentityPoint();
//		int otherDeviceGroupID = device.getGroupID();
//		boolean isOtherFirst = (otherDeviceGroupID == 0);
//		Identity otherDeviceIdentity = device.getIdentity();
//
//		Log.e("ServiceKeeper_updateNetworkMap", myDeviceIdentity + " vs. " + otherDeviceIdentity);
//		
//		
//		switch (myDeviceIdentity) {
//		case MAIN:
//			BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
//			break;
//			
//		case PROXY:
//			if (myDeviceGroupID == otherDeviceGroupID) {
//				if (myDevicePoint < otherDevicePoint) {
//					DeviceAnalyzer.getInstance(KEEPER_CONTEXT).grantIdentity(Identity.CORE);
//					// TODO : 전권 이양 작업.... 작업 동기화
//
//					BLINK_NETWORK_MAP.put(BlinkDevice.HOST, Identity.CORE);
//					BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
//					
//				} else if (myDevicePoint > otherDeviceGroupID) {
//					
//					if (myDeviceIdentity == otherDeviceIdentity)
//						device.setIdentity(Identity.CORE.ordinal());
//					
//					BLINK_NETWORK_MAP.put(BlinkDevice.load(device), device.getIdentity());
//				}
//					
//			}
//			break;
//			
//		case CORE:
//			if (isMyFirst) {
//				if (isOtherFirst) {
//					if (myDevicePoint > otherDevicePoint) {
//						
//						DeviceAnalyzer.getInstance(KEEPER_CONTEXT).grantIdentity(Identity.MAIN);
//						
//						device.setGroupID(BlinkDevice.HOST.getGroupID());
//						
//						BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
//						BLINK_NETWORK_MAP.put(BlinkDevice.HOST, Identity.MAIN);
//						
//						sendMessageToDevice(device, BlinkDevice.HOST);
//						
//					} else if (myDevicePoint == otherDevicePoint) {
//						// 선택 유도 Dialog
//					}
//					
//					
//				} else if (otherDevicePoint > DeviceAnalyzer.IDENTITY_POINTLINE_PROXY) {
//					BlinkDevice.HOST.setGroupID(device.getGroupID());
//					BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
//					
//				} else {
//					// 다른 그룹에 속해있는 비결정권자 디바이스가 본 디바이스에 접근한 경우..
//					// >> 연결 해제.
//					//BluetoothAssistant.getInstance(InterDeviceManager.getInstance(KEEPER_CONTEXT)).disconnectDevice(device);
//				}
//				
//				
//			} else if (myDeviceGroupID == otherDeviceGroupID) {
//				switch (otherDeviceIdentity) {
//				case MAIN:
//				case PROXY:
//					BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
//					break;
//					
//				default:
//				}
//				
//			} else {
//				// 동일한 그룹의 디바이스가 아닌 경우...
//				// >> 연결 해제.
//				//BluetoothAssistant.getInstance(InterDeviceManager.getInstance(KEEPER_CONTEXT)).disconnectDevice(device);
//			}
//
//			break;	
//			
//		default:
//			if (isMyFirst) {
//				if (!isOtherFirst){
//					switch (otherDeviceIdentity){
//					case MAIN:
//					case PROXY:
//						BlinkDevice.HOST.setGroupID(device.getGroupID());
//						new DeviceAnalyzer(KEEPER_CONTEXT).grantIdentity(Identity.PERIPHERALS);
//
//						BLINK_NETWORK_MAP.put(BlinkDevice.HOST, Identity.PERIPHERALS);
//						BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
//						break;
//						
//					default:
//					}
//				}
//				
//				
//			} else if (myDeviceGroupID == otherDeviceGroupID) {
//				switch (otherDeviceIdentity) {
//				case MAIN:
//				case PROXY:
//					BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
//					break;
//					
//				default:
//				}
//			}
//		}
//	}
	
	/**
	 * 타겟 디바이스에 전달받은 Object 메세지를 보낸다.
	 * 
	 * @param targetDevice
	 * @param msg
	 */
	public void sendMessageToDevice(BlinkDevice targetDevice, Object msg) {
		if (targetDevice.isLESupported()) {
			
			
		} else {
			ClassicLinkThread thread = CLASSIC_CONN_MAP.get(targetDevice);
			if (thread != null)
				thread.sendMessageToDevice(msg);
			
		}
	}
	
	/**
	 * 콜백 리스트 관련 매소드
	 */
	public void addRemoteCallbackList(String packageName, IInternalEventCallback callback){
		if(CALLBACK_MAP.get(packageName)==null)
			CALLBACK_MAP.put(packageName, new RemoteCallbackList<IInternalEventCallback>());
		RemoteCallbackList<IInternalEventCallback> mRemoteCallbackList = CALLBACK_MAP.get(packageName);
		mRemoteCallbackList.register(callback);
	}
	
	public boolean clearRemoteCallbackList(String packageName, IInternalEventCallback callback){
		if(CALLBACK_MAP.get(packageName)==null)return false;
		RemoteCallbackList<IInternalEventCallback> mRemoteCallbackList = CALLBACK_MAP.get(packageName);
		return mRemoteCallbackList.unregister(callback);
	}
	
	public RemoteCallbackList<IInternalEventCallback> obtainRemoteCallbackList(String packageName){
		return CALLBACK_MAP.get(packageName);
	}
}

