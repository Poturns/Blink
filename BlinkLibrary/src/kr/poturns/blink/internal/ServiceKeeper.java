
package kr.poturns.blink.internal;

import java.util.HashMap;
import java.util.HashSet;

import kr.poturns.blink.db.SyncDatabaseManager;
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
	
	private final SyncDatabaseManager SYNC_DB_MANAGER;
	
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
		SYNC_DB_MANAGER = new SyncDatabaseManager(context);
		
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
		if (device != null) {
			// TODO : GroupID 가 복수 일 때??
			String devGroupID = device.getGroupID();
			if (BLINK_NETWORK_MAP.containsKey(devGroupID)) 
				BLINK_NETWORK_MAP.get(devGroupID).addConnection(device, thread);
			
			else {
				NetworkMap mNetworkMap = new NetworkMap(devGroupID);
				mNetworkMap.addConnection(device, thread);
				
				BLINK_NETWORK_MAP.put(devGroupID, mNetworkMap);
			}
		}
	}
	
	/**
	 * 연결이 성립되었을 경우, 해당 디바이스의 GroupID에 맞게 항목을 추가한다.
	 * 
	 * @param device
	 * @param gatt
	 */
	void addConnection(BlinkDevice device, BluetoothGatt gatt) {
		if (device != null && device.isConnected()) {
			// TODO : GroupID 가 복수 일 때??
			String devGroupID = device.getGroupID();
			if (BLINK_NETWORK_MAP.containsKey(devGroupID)) 
				BLINK_NETWORK_MAP.get(devGroupID).addConnection(device, gatt);
			
			else {
				NetworkMap mNetworkMap = new NetworkMap(devGroupID);
				mNetworkMap.addConnection(device, gatt);
				
				BLINK_NETWORK_MAP.put(devGroupID, mNetworkMap);
			}
		}
	}

	/**
	 * 해당 디바이스의 연결을 해제하고, 맵에서 항목을 제거한다.
	 * 
	 * @param device
	 * @return true : 제거 성공, false : 제거 못함.
	 */
	boolean removeConnection(BlinkDevice device) {
		if (device == null || !BLINK_NETWORK_MAP.containsKey(device.getGroupID()))
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
	public BlinkSupportBinder obtainBinder(String packageName) {
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
		if (targetDevice == null)
			return;
		
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
	 * SystemSync Message는 Passing되지 않는다.
	 * 
	 * @param targetDevice
	 * @param identity
	 */
	void transferSystemSync(BlinkDevice targetDevice, int type) {
		if (targetDevice == null)
			return;
		
		Log.e("ServiceKeeper_transferSystemSync", targetDevice.getAddress() + " // " + type);
		
		Object message = null;
		switch (type) {
		case BlinkMessage.TYPE_REQUEST_IDENTITY_SYNC:
			message = BlinkDevice.HOST;
			break;
			
		case BlinkMessage.TYPE_REQUEST_NETWORK_SYNC:
			if (BlinkDevice.HOST.isCenterDevice())
				message = BLINK_NETWORK_MAP.get(targetDevice.getGroupID()).obtainLinkedDevices();
			break;
			
		case BlinkMessage.TYPE_REQUEST_BlinkAppInfo_SYNC:
			if (!BlinkDevice.HOST.isCenterDevice())
				message = SYNC_DB_MANAGER.obtainBlinkApp();
			break;
			
		default:
			return;
		}
		
		BlinkMessage mBlinkMessage = new BlinkMessage.Builder()
										.setSourceDevice(BlinkDevice.HOST)
										.setDestinationDevice(targetDevice)
										.setType(type)
										.setMessage(message)
										.build();
		
		if (type != BlinkMessage.TYPE_REQUEST_NETWORK_SYNC)
			sendMessageToDevice(targetDevice, mBlinkMessage);
		else {
			//new MessageProcessor(KEEPER_CONTEXT).sendBroadcast(mBlinkMessage);
		}
		
		Log.e("ServiceKeeper_transferSystemSync", mBlinkMessage.getMessage());
	}
	
	/**
	 * 상대 디바이스와 Identity 동기화를 수행한다.
	 * 
	 * @param otherDevice (Separate-Instance)
	 */
	void handleIdentitySync(BlinkDevice otherDevice) {
		String myDevGroupID = BlinkDevice.HOST.getGroupID();
		String otherDevGroupID = otherDevice.getGroupID();

		// Identity 정보 동기화
		otherDevice = BlinkDevice.update(otherDevice);
		DeviceAnalyzer mAnalyzer = DeviceAnalyzer.getInstance(KEEPER_CONTEXT);

		if (BLINK_NETWORK_MAP.containsKey(otherDevGroupID)) {
			// otherDevice와 이전에 연결되어 Group을 형성한 적이 있거나, OpenGroup으로 연결.
			NetworkMap mNetworkMap = BLINK_NETWORK_MAP.get(otherDevGroupID);
			
			// 상대 디바이스는 OpenGroup으로서 연결 요청, 자신의 디바이스는 OpenGroup 연결을 원치 않을 때..
			//XXX String 비교
			if (myDevGroupID != otherDevGroupID) {
				if (myDevGroupID != null) {
					// 연결 종료
					mNetworkMap.removeConnection(otherDevice);
				}
				return;
			}
				
			
			
			
			// 형성된 그룹이 존재하거나, OpenGroup으로 연결.
			// TODO : 그룹 동기화 문제 분리... 그룹 동기화의 경우 [그룹 초대]를 통하여 별도의 메세지를 통해 그룹을 형성하도록 한다.
			// TODO : Default 기능으로는 OpenGroup을 형성하여, BLINK 네트워크를 형성하는 모든 디바이스가 연결 될 수 있도록 함. (단, OpenGroup Mode일 경우, Data보호 문제 필요..)
			long compare = mAnalyzer.compareForIdentity(BlinkDevice.HOST, otherDevice);
			if (compare > 0) {
				// 상대 디바이스보다 우위에 있는 경우.
				switch (BlinkDevice.HOST.getIdentity()) {
				case MAIN:
				case PROXY: {
					transferSystemSync(otherDevice, IBlinkMessagable.TYPE_REQUEST_NETWORK_SYNC);
					
				} break;
				
				case CORE: 
				case PERIPHERAL: {
					BlinkDevice mCenterDevice = mNetworkMap.getLinkedCenterDevice();
					if (mCenterDevice.equals(BlinkDevice.HOST)) {
						// 현재 GroupNetwork에서 첫 연결이 성립되는 경우..
						if (Identity.CORE.equals(mCenterDevice.getIdentity()))
							mAnalyzer.grantMainIdentity(true);
						else
							mAnalyzer.grantProxyIdentity(true);

						transferSystemSync(otherDevice, IBlinkMessagable.TYPE_REQUEST_NETWORK_SYNC);
						
					} else {
						// Center 디바이스와 다른 루트로 연결되어 있는 경우,
						// TODO : 1. Center 디바이스로 리다이렉트, 2. 자신이 Center Device에게 알려주기..
					}
				} break;
					
				default:
				}
				
			} else if (BlinkDevice.HOST.isCenterDevice()) {
				// 상대 디바이스보다 낮지만 Center역할을 하고 있었을 경우.. Center 역할 반납.
				switch (BlinkDevice.HOST.getIdentity()) {
				case MAIN:
					mAnalyzer.grantMainIdentity(false);
					break;
					
				case PROXY:
					mAnalyzer.grantProxyIdentity(false);
					break;
					
				default:
				}	
			}
			
			
		} else {
			
		}
	}
	
	/**
	 * 상대 디바이스와 Network 동기화를 수행한다.
	 * 
	 * @param set (Separate-Instance)
	 * @param groupID
	 */
	void handleNetworkSync(HashSet<BlinkDevice> set, String groupID) {
		NetworkMap mNetworkMap = BLINK_NETWORK_MAP.get(groupID);
		if (mNetworkMap == null)
			return;
		
		for(BlinkDevice device : set) {
			BlinkDevice mDevice = BlinkDevice.update(device);
			mNetworkMap.addLink(mDevice);
		}
	}
	
	
}

