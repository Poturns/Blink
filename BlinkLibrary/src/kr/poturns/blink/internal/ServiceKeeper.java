
package kr.poturns.blink.internal;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

import kr.poturns.blink.internal.DeviceAnalyzer.Identity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkMessage;
import kr.poturns.blink.internal.comm.BlinkSupportBinder;
import kr.poturns.blink.internal.comm.IBlinkMessagable;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import android.bluetooth.BluetoothGatt;
import android.os.RemoteCallbackList;
import android.util.Log;


/**
 * 서비스가 관리하는 연결 리소스의 현황을 관리하고 접근할 수 있는 모듈.
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
	//private final TreeMap<BlinkDevice, Entry<Identity, Object>> OLD_NETWORK_MAP;
	private final HashMap<Integer, NetworkMap> BLINK_NETWORK_MAP;
	private final NetworkMap NETWORK_MAP;
	
	private final HashMap<String, BlinkSupportBinder> BINDER_MAP;
	private final HashMap<String, RemoteCallbackList<IInternalEventCallback>> CALLBACK_MAP;
	
	private ServiceKeeper(BlinkLocalBaseService context) {
		KEEPER_CONTEXT = context;
		
		DISCOVERY_SET = new HashSet<BlinkDevice>();
		//OLD_NETWORK_MAP = new TreeMap<BlinkDevice, Entry<Identity, Object>>();
		BLINK_NETWORK_MAP = new HashMap<Integer, NetworkMap>();
		NETWORK_MAP = new NetworkMap(0);
		BINDER_MAP = new HashMap<String, BlinkSupportBinder>();
		CALLBACK_MAP = new HashMap<String,RemoteCallbackList<IInternalEventCallback>>();
		
		//OLD_NETWORK_MAP.put(BlinkDevice.HOST, new SimpleEntry<Identity, Object>(BlinkDevice.HOST.getIdentity(), null));
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
			//BLINK_NETWORK_MAP.put(device, new SimpleEntry<Identity, Object>(device.getIdentity(), thread));
			NETWORK_MAP.addConnection(device, thread);
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
			//BLINK_NETWORK_MAP.put(device, new SimpleEntry<Identity, Object>(device.getIdentity(), gatt));
			NETWORK_MAP.addConnection(device, gatt);
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
		//Object mConnObj = BLINK_NETWORK_MAP.remove(device);
		Object mConnObj = BLINK_NETWORK_MAP.get(device.getGroupID()).removeConnection(device);
		if (mConnObj != null) {
			if (device.isLESupported())
				((BluetoothGatt) mConnObj).close();
			else
				((ClassicLinkThread) mConnObj).destroyThread();
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
		
		return NETWORK_MAP.getConnectionObject(device);
	}
	
	/**
	 * 
	 */
	void disconnectAllConnection() {
		NETWORK_MAP.disconnectAllConnection();
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
	 * @param packageName
	 * @param callback
	 */
	public void addRemoteCallbackList(String packageName, IInternalEventCallback callback){
		if(CALLBACK_MAP.get(packageName)==null)
			CALLBACK_MAP.put(packageName, new RemoteCallbackList<IInternalEventCallback>());
		RemoteCallbackList<IInternalEventCallback> mRemoteCallbackList = CALLBACK_MAP.get(packageName);
		mRemoteCallbackList.register(callback);
	}
	
	/**
	 * 
	 * @param packageName
	 * @param callback
	 * @return
	 */
	public boolean clearRemoteCallbackList(String packageName, IInternalEventCallback callback){
		if(CALLBACK_MAP.get(packageName)==null)return false;
		RemoteCallbackList<IInternalEventCallback> mRemoteCallbackList = CALLBACK_MAP.get(packageName);
		return mRemoteCallbackList.unregister(callback);
	}
	
	/**
	 * 
	 * @param packageName
	 * @return
	 */
	public RemoteCallbackList<IInternalEventCallback> obtainRemoteCallbackList(String packageName){
		return CALLBACK_MAP.get(packageName);
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
		// TODO : 
		if (BLINK_NETWORK_MAP.isEmpty())
			return null;
		
		return BLINK_NETWORK_MAP.get(BlinkDevice.HOST.getGroupID()).obtainConnectedDevices();
	}
	
	/**
	 * 
	 * @param identity
	 * @return
	 */
	public BlinkDevice[] obtainIdentityDevices(Identity identity) {
		// TODO : 
		if (BLINK_NETWORK_MAP.isEmpty())
			return null;
		
		return BLINK_NETWORK_MAP.get(BlinkDevice.HOST.getGroupID()).obtainLinkedDevicesWithIdentity(identity);
	}
	
	/**
	 * 현재 Center 역할을 하고 있는 Device를 반환한다.
	 * 
	 * @return
	 */
	public BlinkDevice obtainCurrentCenterDevice() {
		return BLINK_NETWORK_MAP.get(BlinkDevice.HOST.getGroupID()).getLinkedCenterDevice();
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
	 * 타겟 디바이스에 전달받은 Object 메세지를 보낸다.
	 * 
	 * @param targetDevice
	 * @param msg
	 */
	public void sendMessageToDevice(BlinkDevice targetDevice, Object msg) {
		if (targetDevice.isLESupported()) {
			//BluetoothGatt gatt = (BluetoothGatt) OLD_NETWORK_MAP.get(targetDevice).getValue();
			BluetoothGatt gatt = (BluetoothGatt) BLINK_NETWORK_MAP.get(targetDevice.getGroupID()).getConnectionObject(targetDevice);
			if (gatt != null) {
				
			}
			
		} else {
			//ClassicLinkThread thread = (ClassicLinkThread) OLD_NETWORK_MAP.get(targetDevice).getValue();
			ClassicLinkThread thread;
			if (BLINK_NETWORK_MAP.isEmpty())
				thread = (ClassicLinkThread) NETWORK_MAP.getConnectionObject(targetDevice);
			else
				thread = (ClassicLinkThread) BLINK_NETWORK_MAP.get(targetDevice.getGroupID()).getConnectionObject(targetDevice);
			
			if (thread != null)
				thread.sendMessageToDevice(msg);
		}
	}

	/**
	 * 
	 * @param targetDevice
	 * @param identity
	 */
	void transferSystemSync(BlinkDevice targetDevice, boolean identity) {
		//if (!identity && BlinkDevice.HOST != OLD_NETWORK_MAP.lastKey())
		if (!identity && !BlinkDevice.HOST.isCenterDevice())
			return;
		
		Log.e("ServiceKeeper_transferSystemSync", targetDevice.getAddress() + " // " + identity);
		
		BlinkMessage mBlinkMessage = new BlinkMessage.Builder()
										.setSourceDevice(BlinkDevice.HOST)
										.setDestinationDevice(targetDevice)
										.setType(identity? 
												BlinkMessage.TYPE_REQUEST_IDENTITY_SYNC : 
													BlinkMessage.TYPE_REQUEST_NETWORK_SYNC)
										.setMessage(identity?
												BlinkDevice.HOST : 
													//OLD_NETWORK_MAP)
													BLINK_NETWORK_MAP.get(targetDevice.getGroupID()).obtainLinkedDevices())
										.build();

		Log.e("ServiceKeeper_transferSystemSync", mBlinkMessage.getMessage());
		
		sendMessageToDevice(targetDevice, mBlinkMessage);
	}
	
	/**
	 * 
	 * @param otherDevice
	 */
	void handleIdentitySync(BlinkDevice otherDevice) {
		int myDevGroupID = BlinkDevice.HOST.getGroupID();
		int otherDevGroupID = otherDevice.getGroupID();

		DeviceAnalyzer mAnalyzer = DeviceAnalyzer.getInstance(KEEPER_CONTEXT);
		
		if (BLINK_NETWORK_MAP.containsKey(otherDevGroupID)) {
			// otherDevice와 이전에 연결되어 Group을 형성한 적이 있음.
			NetworkMap mNetworkMap = BLINK_NETWORK_MAP.get(otherDevGroupID);
			
			int compare = mAnalyzer.compareForIdentity(BlinkDevice.HOST, otherDevice);
			if (compare > 0) {
				switch (BlinkDevice.HOST.getIdentity()) {
				case MAIN:
				case PROXY: {
					// TODO : 그룹별 관리를 할 경우로 변경 작업 필요.
					// 하나의 그룹만 다룸. 향후 그룹별 관리가 적용될 때, 삭제해야함.
					otherDevice.setGroupID(myDevGroupID);
					mNetworkMap.addConnection(BlinkDevice.update(otherDevice), NETWORK_MAP.removeConnection(otherDevice));

					transferSystemSync(otherDevice, false);
				} break;
				
//				case CORE: 
//				case PERIPHERAL: {
//					// 네트워크 형성한적이 없을 경우,
//					// TODO : 그룹별 관리를 할 경우로 변경 작업 필요.
//					otherDevice.setGroupID(myDevGroupID);
//					mNetworkMap.addConnection(BlinkDevice.update(otherDevice), NETWORK_MAP.removeConnection(otherDevice));
//					
//				} break;
					
				default:
				}
				
				
			} else if (BlinkDevice.HOST.getIdentity() == Identity.PROXY)
				mAnalyzer.grantProxyIdentity(false);
			
			
		} else {
			// otherDevice와 이전에 연결되어 Group을 형성한 적이 없음!
			int compare = mAnalyzer.compareForIdentity(BlinkDevice.HOST, otherDevice);
			if (compare > 0) {
				switch (BlinkDevice.HOST.getIdentity()) {
				case MAIN:
				case PROXY: {
					// TODO : 그룹별 관리를 할 경우로 변경 작업 필요.
					// 하나의 그룹만 다룸. 향후 그룹별 관리가 적용될 때, 삭제해야함.
					NetworkMap mNetworkMap = BLINK_NETWORK_MAP.get(myDevGroupID);
					otherDevice.setGroupID(myDevGroupID);
					mNetworkMap.addConnection(BlinkDevice.update(otherDevice), NETWORK_MAP.removeConnection(otherDevice));
					
					transferSystemSync(otherDevice, false);
				} break;
				
				case CORE: 
				case PERIPHERAL: {
					// 네트워크 형성한적이 없을 경우,
					// TODO : 그룹별 관리를 할 경우로 변경 작업 필요.
					if (BLINK_NETWORK_MAP.isEmpty()) {
						if (BlinkDevice.HOST.getIdentity() == Identity.CORE)	
							mAnalyzer.grantMainIdentity(true);
						else
							mAnalyzer.grantProxyIdentity(true);
						
						int mGroupID = mAnalyzer.generateGroupId();
						BlinkDevice.HOST.setGroupID(mGroupID);
						otherDevice.setGroupID(mGroupID);
						
						NetworkMap mNetworkMap = new NetworkMap(mGroupID);
						mNetworkMap.addConnection(BlinkDevice.update(otherDevice), NETWORK_MAP.removeConnection(otherDevice));
						
						BLINK_NETWORK_MAP.put(mGroupID, mNetworkMap);

						transferSystemSync(otherDevice, false);
						
					// 네트워크 형성한 적이 있을 경우,
					// TODO : 그룹별 관리를 할 경우로 변경 작업 필요.
					} else {
						// 하나의 그룹만 다룸. 향후 그룹별 관리가 적용될 때, 삭제해야함.
						//NetworkMap mNetworkMap = (NetworkMap)BLINK_NETWORK_MAP.values().toArray()[0];
//						NetworkMap mNetworkMap = BLINK_NETWORK_MAP.get(myDevGroupID);
//						otherDevice.setGroupID(myDevGroupID);
//						mNetworkMap.addConnection(BlinkDevice.update(otherDevice), NETWORK_MAP.removeConnection(otherDevice));
						((ClassicLinkThread) NETWORK_MAP.getConnectionObject(otherDevice)).destroyThread();
					}
				} break;
					
				default:
				}
				
			} else if (BlinkDevice.HOST.getIdentity() == Identity.PROXY)
				mAnalyzer.grantProxyIdentity(false);
			
		}
	}
	
//	void requestNetworkSync(int groupID) {
//		NetworkMap mNetworkMap = BLINK_NETWORK_MAP.get(groupID);
//		
//		for (BlinkDevice device : mNetworkMap.obtainConnectedDevices()) {
//			BlinkMessage message = new BlinkMessage.Builder()
//									.setSourceDevice(BlinkDevice.HOST)
//									.setDestinationDevice(device)
//									.setType(IBlinkMessagable.TYPE_REQUEST_NETWORK_SYNC)
//									.setMessage(null)
//									.build();
//			
//			sendMessageToDevice(device, message);
//		}
//	}
	
	/**
	 * 
	 * @param device
	 */
	void handleNetworkSync(HashSet<BlinkDevice> set) {
		for(BlinkDevice device : set) {
			BlinkDevice mDevice = BlinkDevice.update(device);
			
		}
	}
	
	
}

