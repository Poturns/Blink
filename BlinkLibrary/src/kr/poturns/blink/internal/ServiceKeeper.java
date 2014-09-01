package kr.poturns.blink.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import kr.poturns.blink.internal.DeviceAnalyzer.Identity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkSupportBinder;
import kr.poturns.blink.internal.comm.IBlinkEventBroadcast;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
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
	
	private final HashMap<String, BlinkSupportBinder> BINDER_MAP;
	private final RemoteCallbackList<IInternalEventCallback> EVENT_CALLBACK_LIST;
	
	private ServiceKeeper(BlinkLocalBaseService context) {
		KEEPER_CONTEXT = context;
		
		DISCOVERY_SET = new HashSet<BlinkDevice>();
		BLINK_NETWORK_MAP = new HashMap<BlinkDevice, Identity>();
		CLASSIC_CONN_MAP = new HashMap<BlinkDevice, ClassicLinkThread>();
		LE_CONN_MAP = new HashMap<BlinkDevice, BluetoothGatt>();
		BINDER_MAP = new HashMap<String, BlinkSupportBinder>();
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
		if (device == null)
			return ;
		
		BluetoothGatt mGatt = LE_CONN_MAP.remove(device);
		if (mGatt != null)
			mGatt.close();
		
		ClassicLinkThread mLinkThread = CLASSIC_CONN_MAP.remove(device);
		if (mLinkThread != null) {
			mLinkThread.destroyThread();
		}
	}
	
	Object getConnectionObject(BlinkDevice device) {
		if (device.isLESupported()) {
			return LE_CONN_MAP.get(device);
			
		} else
			return CLASSIC_CONN_MAP.get(device);
	}
	
	
	void clearConnection() {
		LE_CONN_MAP.clear();
		
		for (ClassicLinkThread thread : CLASSIC_CONN_MAP.values())
			thread.destroyThread();
		CLASSIC_CONN_MAP.clear();
	}
	
	void registerBinder(String packageName, BlinkSupportBinder binder) {
		BINDER_MAP.put(packageName, binder);
	}
	
	BlinkSupportBinder obtainBinder(String packageName) {
		if (packageName != null)
			return BINDER_MAP.get(packageName);
		return null;
	}
	
	boolean releaseBinder(String packageName) {
		if (packageName != null)
			return (BINDER_MAP.remove(packageName) != null);
		return false;
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
	
	/**
	 * 본 디바이스의 정보를 타겟 디바이스에게 전달한다.
	 * 
	 * @param device
	 */
	void introduceToBlinkNetwork(BlinkDevice device) {
		sendMessageToDevice(device, BlinkDevice.HOST);
	}
	
	/**
	 * BlinkNetwork의 Identity를 제어하는 메소드.
	 * 
	 * @param device
	 */
	void updateBlinkNetwork(BlinkDevice device) {
		// TODO :

		int myDevicePoint = BlinkDevice.HOST.getIdentityPoint();
		int myDeviceGroupID = BlinkDevice.HOST.getGroupID();
		boolean isMyFirst = (myDeviceGroupID == 0);
		Identity myDeviceIdentity = BlinkDevice.HOST.getIdentity();

		int otherDevicePoint = device.getIdentityPoint();
		int otherDeviceGroupID = device.getGroupID();
		boolean isOtherFirst = (otherDeviceGroupID == 0);
		Identity otherDeviceIdentity = device.getIdentity();

		Log.e("ServiceKeeper_updateNetworkMap", myDeviceIdentity + " vs. " + otherDeviceIdentity);
		
		
		switch (myDeviceIdentity) {
		case MAIN:
			BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
			break;
			
		case SUB:
			if (myDeviceGroupID == otherDeviceGroupID) {
				if (myDevicePoint < otherDevicePoint) {
					// TODO : 전권 이양 작업.... 작업 동기화
				}

				BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
			}
			break;
			
		case AUX:
			if (myDeviceGroupID == otherDeviceGroupID) {
				if (myDevicePoint < otherDevicePoint) {
					DeviceAnalyzer.getInstance(KEEPER_CONTEXT).grantIdentity(Identity.COREABLE);
					// TODO : 전권 이양 작업.... 작업 동기화

					BLINK_NETWORK_MAP.put(BlinkDevice.HOST, Identity.COREABLE);
					BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
					
				} else if (myDevicePoint > otherDeviceGroupID) {
					
					if (myDeviceIdentity == otherDeviceIdentity)
						device.setIdentity(Identity.COREABLE.ordinal());
					
					BLINK_NETWORK_MAP.put(BlinkDevice.load(device), device.getIdentity());
				}
					
			}
			break;
			
		case COREABLE:
			if (isMyFirst) {
				if (isOtherFirst) {
					if (myDevicePoint > otherDevicePoint) {
						
						DeviceAnalyzer.getInstance(KEEPER_CONTEXT).grantIdentity(Identity.MAIN);
						
						device.setGroupID(BlinkDevice.HOST.getGroupID());
						
						BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
						BLINK_NETWORK_MAP.put(BlinkDevice.HOST, Identity.MAIN);
						
						sendMessageToDevice(device, BlinkDevice.HOST);
						
					} else if (myDevicePoint == otherDevicePoint) {
						// 선택 유도 Dialog
					}
					
					
				} else if (otherDevicePoint > DeviceAnalyzer.IDENTITY_POINTLINE_AUX) {
					BlinkDevice.HOST.setGroupID(device.getGroupID());
					BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
					
				} else {
					// 다른 그룹에 속해있는 비결정권자 디바이스가 본 디바이스에 접근한 경우..
					// >> 연결 해제.
					//BluetoothAssistant.getInstance(InterDeviceManager.getInstance(KEEPER_CONTEXT)).disconnectDevice(device);
				}
				
				
			} else if (myDeviceGroupID == otherDeviceGroupID) {
				switch (otherDeviceIdentity) {
				case MAIN:
				case SUB:
				case AUX:
					BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
					break;
					
				default:
				}
				
			} else {
				// 동일한 그룹의 디바이스가 아닌 경우...
				// >> 연결 해제.
				//BluetoothAssistant.getInstance(InterDeviceManager.getInstance(KEEPER_CONTEXT)).disconnectDevice(device);
			}

			break;	
			
		default:
			if (isMyFirst) {
				if (!isOtherFirst){
					switch (otherDeviceIdentity){
					case MAIN:
					case SUB:
					case AUX:
						BlinkDevice.HOST.setGroupID(device.getGroupID());
						DeviceAnalyzer.getInstance(KEEPER_CONTEXT).grantIdentity(Identity.PERIPHERALS);

						BLINK_NETWORK_MAP.put(BlinkDevice.HOST, Identity.PERIPHERALS);
						BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
						break;
						
					default:
					}
				}
				
				
			} else if (myDeviceGroupID == otherDeviceGroupID) {
				switch (otherDeviceIdentity) {
				case MAIN:
				case SUB:
				case AUX:
					BLINK_NETWORK_MAP.put(BlinkDevice.load(device), otherDeviceIdentity);
					break;
					
				default:
				}
			}
		}
	}
	
	/**
	 * 타겟 디바이스에 전달받은 Object 메세지를 보낸다.
	 * 
	 * @param targetDevice
	 * @param msg
	 */
	public void sendMessageToDevice(BlinkDevice targetDevice, Object msg) {
		//toDevice가 있어야함 왜냐면 targetMessage의 targetMac의 BlinkDevice 정보를 가져오면 Main으로 전송을 못함.
		if (targetDevice.isLESupported()) {
			
			
		} else {
			ClassicLinkThread thread = CLASSIC_CONN_MAP.get(targetDevice);
			if (thread != null)
				thread.sendMessageToDevice(msg);
			
		}
	}
	
}
