
package kr.poturns.blink.internal;

import java.util.HashMap;
import java.util.HashSet;

import kr.poturns.blink.internal.DeviceAnalyzer.Identity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkMessage;
import kr.poturns.blink.internal.comm.BlinkSupportBinder;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import android.bluetooth.BluetoothGatt;
import android.os.RemoteCallbackList;
import android.util.Log;


/**
 * 서비스가 관리하는 연결 리소스의 현황을 관리하고 접근할 수 있는 모듈.
 * ServiceKeeper에서 관리하는 BlinkDevice 객체들은 BlinkDevice CACHE를 참조한다.
 * 
 * @author Yeonho.Kim
 * @since 2014.08.20
 *
 */
public class ServiceKeeper {

	// *** STATIC DECLARATION *** //
	/**
	 * ServiceKeeper의 Singleton-인스턴스
	 */
	private static ServiceKeeper sInstance = null;
	
	/**
	 * ServiceKeeper의 Singleton-인스턴스를 반환한다. 
	 *  
	 * @param context ( :{@link BlinkLocalBaseService} )
	 * @return context가 Null일 경우, 기존의 Instance를 반환한다.
	 */
	public static ServiceKeeper getInstance(BlinkLocalBaseService context) {
		if (sInstance == null && context != null)
			sInstance = new ServiceKeeper(context);
		return sInstance;
	}

	
	
	// *** FIELD DECLARATION *** //
	private final BlinkLocalBaseService KEEPER_CONTEXT;
	
	/**
	 * 탐색된 디바이스들을 담는 HashSet.
	 */
	private final HashSet<BlinkDevice> DISCOVERY_SET;
	/**
	 * 디바이스와 연결되어 있는 외부 디바이스 목록을 관리하는 HashMap.
	 * GroupID별 {@link NetworkMap}를 관리한다.
	 */
	private final HashMap<String, NetworkMap> BLINK_NETWORK_MAP;
	/**
	 * 디바이스와 연결되어 있는 Binder들을 관리하는 HashMap.
	 * Application의 패키지명으로 Binder를 관리한다.
	 */
	private final HashMap<String, BlinkSupportBinder> BINDER_MAP;
	/**
	 * 연결된 Binder의 Callback 객체들을 관리하는 HashMap.
	 * Application의 패키지명으로 BinderCallback 객체를 관리한다.
	 */
	private final HashMap<String, RemoteCallbackList<IInternalEventCallback>> CALLBACK_MAP;
	
	
	/**
	 * Constructor 생성자.
	 * 
	 * @param context
	 */
	private ServiceKeeper(BlinkLocalBaseService context) {
		KEEPER_CONTEXT = context;
		
		DISCOVERY_SET = new HashSet<BlinkDevice>();
		BLINK_NETWORK_MAP = new HashMap<String, NetworkMap>();
		BLINK_NETWORK_MAP.put(null, new NetworkMap(null));
		
		BINDER_MAP = new HashMap<String, BlinkSupportBinder>();
		CALLBACK_MAP = new HashMap<String,RemoteCallbackList<IInternalEventCallback>>();
	}

	/**
	 * ServiceKeeper를 파괴한다.
	 * 관리하던 리소스를 정리하고, 서비스 종료 절차를 밟는다.
	 * 
	 * <p><b>(직접 호출하지 말것 ! Service 종료시 자동으로 호출됨.)</b>
	 */
	void destroy() {
		clearDiscovery();
		
	}
	
	/**
	 * 탐색된 디바이스를 목록에 추가한다.
	 * 
	 * @param device
	 */
	void addDiscovery(BlinkDevice device) {
		if (device != null && device.isDiscovered())
			DISCOVERY_SET.add(device);
	}

	/**
	 * 탐색 목록을 초기화한다.
	 * 이 때, 해당 디바이스들에 설정되어 있던 {@link BlinkDevice#Discovered} 필드를 조정한다.
	 */
	void clearDiscovery() {
		for (BlinkDevice device : DISCOVERY_SET)
			device.setDiscovered(false);
		DISCOVERY_SET.clear();
	}
	
	/**
	 * 연결이 성립되었을 경우, 해당 디바이스의 GroupID에 맞게 항목을 추가한다.
	 * 
	 * @param device
	 * @param thread
	 */
	void addConnection(BlinkDevice device, ClassicLinkThread thread) {
		if (device != null && device.isConnected()) 
			// TODO : GroupID 가 복수 일 때??
			BLINK_NETWORK_MAP.get(device.getGroupID()).addConnection(device, thread);
	}
	
	/**
	 * 연결이 성립되었을 경우, 해당 디바이스의 GroupID에 맞게 항목을 추가한다.
	 * 
	 * @param device
	 * @param gatt
	 */
	void addConnection(BlinkDevice device, BluetoothGatt gatt) {
		if (device != null && device.isConnected()) 
			// TODO : GroupID 가 복수 일 때??
			BLINK_NETWORK_MAP.get(device.getGroupID()).addConnection(device, gatt);
	}

	/**
	 * 해당 디바이스의 연결을 해제하고, 맵에서 항목을 제거한다.
	 * 
	 * @param device
	 * @return true : 제거 성공, false : 제거 못함.
	 */
	boolean removeConnection(BlinkDevice device) {
		if (device == null)
			return false;

		Object mConnObj = BLINK_NETWORK_MAP.get(device.getGroupID()).removeConnection(device);
		if (mConnObj != null) {
			if (device.isLESupported())
				((BluetoothGatt) mConnObj).close();
			else
				((ClassicLinkThread) mConnObj).destroyThread();
			
			return true;
		}
		return false;
	}
	
	/**
	 * 연결되어 있는 해당 디바이스의 연결 객체를 반환한다.
	 * 디바이스에 해당하는 연결 객체가 존재하지 않을 경우, null을 반환한다.
	 * 
	 * @param device
	 * @return
	 */
	Object getConnectionObject(BlinkDevice device) {
		if (device == null || !device.isConnected())
			return null;
		
		return BLINK_NETWORK_MAP.get(device.getGroupID()).getConnectionObject(device);
	}
	
	/**
	 * 해당 Group에서 연결되어 있는 모든 디바이스와 연결을 해제한다.
	 */
	void disconnectAllConnection(String groupID) {
		BLINK_NETWORK_MAP.get(groupID).disconnectAllConnection();
	}
	
	/**
	 * Application 패키지명과 {@link BlinkSupportBinder}를 맵핑하여 항목을 추가한다.
	 * 
	 * @param packageName
	 * @param binder
	 */
	void registerBinder(String packageName, BlinkSupportBinder binder) {
		if (packageName != null && binder != null)
			BINDER_MAP.put(packageName, binder);
	}
	
	/**
	 * 패키지명에 해당하는 Binder를 반환한다.
	 * 패키지명에 해당하는 Binder가 존재하지 않을 경우, null을 반환한다.
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
	 * 패키지명에 해당하는 바인더를 해제한다.
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
	 * 패키지명에 해당하는 Callback 객체 항목을 추가한다.
	 * 
	 * @param packageName
	 * @param callback
	 */
	public void addRemoteCallbackList(String packageName, IInternalEventCallback callback){
		if(CALLBACK_MAP.get(packageName) == null)
			CALLBACK_MAP.put(packageName, new RemoteCallbackList<IInternalEventCallback>());
		
		RemoteCallbackList<IInternalEventCallback> mRemoteCallbackList = CALLBACK_MAP.get(packageName);
		mRemoteCallbackList.register(callback);
	}

	/**
	 * 패키지명에 해당하는 Callback 객체를 반환한다.
	 * 
	 * @param packageName
	 * @return
	 */
	public RemoteCallbackList<IInternalEventCallback> obtainRemoteCallbackList(String packageName){
		return CALLBACK_MAP.get(packageName);
	}
	
	/**
	 * 패키지명에 해당하는 Callback 객체 항목을 제거한다.
	 * 
	 * @param packageName
	 * @param callback
	 * @return
	 */
	public boolean removeRemoteCallbackList(String packageName, IInternalEventCallback callback){
		if(CALLBACK_MAP.get(packageName) == null)
			return false;
		
		RemoteCallbackList<IInternalEventCallback> mRemoteCallbackList = CALLBACK_MAP.get(packageName);
		return mRemoteCallbackList.unregister(callback);
	}
	
	/**
	 * 탐색된 디바이스들의 목록을 배열로 반환한다.
	 * 
	 * @return
	 */
	public BlinkDevice[] obtainDiscoveredDevices() {
		BlinkDevice[] lists = new BlinkDevice[DISCOVERY_SET.size()];
		return DISCOVERY_SET.toArray(lists);
	}

	/**
	 * 연결된 디바이스들의 목록을 배열로 반환한다.
	 * 
	 * @return
	 */
	public BlinkDevice[] obtainConnectedDevices() {
		// TODO : 
		return BLINK_NETWORK_MAP.get(BlinkDevice.HOST.getGroupID()).obtainConnectedDevices();
	}
	
	/**
	 * 연결된 디바이스들의 목록 중에서 해당 Identity를 갖는 디바이스를 반환한다.
	 * 
	 * @param identity
	 * @return
	 */
	public BlinkDevice[] obtainIdentityDevices(Identity identity) {
		// TODO : 
		return BLINK_NETWORK_MAP.get(BlinkDevice.HOST.getGroupID()).obtainLinkedDevicesWithIdentity(identity);
	}
	
	/**
	 * 현 Group에서 Center 역할을 하는 디바이스를 반환한다.
	 * 
	 * @return
	 */
	public BlinkDevice obtainCurrentCenterDevice() {
		return BLINK_NETWORK_MAP.get(BlinkDevice.HOST.getGroupID()).getLinkedCenterDevice();
	}
	
	/**
	 * 타겟 디바이스에 전달받은 Object 메세지를 보낸다.
	 * 
	 * @param targetDevice
	 * @param msg
	 */
	public void sendMessageToDevice(BlinkDevice targetDevice, Object msg) {
		if (targetDevice.isLESupported()) {
			BluetoothGatt gatt = (BluetoothGatt) BLINK_NETWORK_MAP.get(targetDevice.getGroupID()).getConnectionObject(targetDevice);
			if (gatt != null) {
				
			}
			
		} else {
			ClassicLinkThread thread = (ClassicLinkThread) BLINK_NETWORK_MAP.get(targetDevice.getGroupID()).getConnectionObject(targetDevice);
			
			if (thread != null)
				thread.sendMessageToDevice(msg);
		}
	}

	/**
	 * 시스템 동기화 메세지를 전송한다.
	 * 
	 * @param targetDevice
	 * @param identity
	 */
	void transferSystemSync(BlinkDevice targetDevice, boolean identity) {
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
	 * 상대 디바이스와 Identity 동기화를 수행한다.
	 * 
	 * @param otherDevice
	 */
	void handleIdentitySync(BlinkDevice otherDevice) {
		String myDevGroupID = BlinkDevice.HOST.getGroupID();
		String otherDevGroupID = otherDevice.getGroupID();

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
					mNetworkMap.addConnection(BlinkDevice.update(otherDevice), BLINK_NETWORK_MAP.get(myDevGroupID).removeConnection(otherDevice));

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
					mNetworkMap.addConnection(BlinkDevice.update(otherDevice), BLINK_NETWORK_MAP.get(myDevGroupID).removeConnection(otherDevice));

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
						
						String mGroupID = mAnalyzer.generateGroupId();
						BlinkDevice.HOST.setGroupID(mGroupID);
						otherDevice.setGroupID(mGroupID);
						
						NetworkMap mNetworkMap = new NetworkMap(mGroupID);
						mNetworkMap.addConnection(BlinkDevice.update(otherDevice), BLINK_NETWORK_MAP.get(myDevGroupID).removeConnection(otherDevice));

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
						((ClassicLinkThread) BLINK_NETWORK_MAP.get(otherDevGroupID).getConnectionObject(otherDevice)).destroyThread();
					}
				} break;
					
				default:
				}
				
			} else if (BlinkDevice.HOST.getIdentity() == Identity.PROXY)
				mAnalyzer.grantProxyIdentity(false);
			
		}
	}
	
	/**
	 * 상대 디바이스와 Network 동기화를 수행한다.
	 * 
	 * @param device
	 */
	void handleNetworkSync(HashSet<BlinkDevice> set) {
		for(BlinkDevice device : set) {
			BlinkDevice mDevice = BlinkDevice.update(device);
			
		}
	}
	
	
}

