package kr.poturns.blink.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import kr.poturns.blink.internal.DeviceAnalyzer.Identity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.bluetooth.BluetoothGatt;

/**
 * Blink Network를 나타내는 클래스.
 * NetworkMap은 Linked와 Connected로 연결 상태를 구분하는데,
 * Linked 상태는 자신과는 직접적으로 연결되어 있지 않지만 연결되어있는 디바이스를 통해 연결되어 있는 상태이다.
 * Connected 상태는 자신과 직접적으로 연결되어 있는 상태이다. 
 * 
 * <p>또한 NetworkMap은 네트워크를 그룹 별로 관리한다.
 * 
 * @author Yeonho.Kim
 * @since 2014.09.07
 *
 */
class NetworkMap {

	// *** FIELD DECLARATION *** //
	private String GroupID;
	private String GroupName;
	
	private final ArrayList<BlinkDevice> LINKED_LIST;
	private final HashMap<BlinkDevice, Object> CONNECTED_MAP;
	private final HashMap<BlinkDevice, Object> LINKED_MAP;

	public NetworkMap(String groupID) {
		GroupID = groupID;
		
		LINKED_LIST = new ArrayList<BlinkDevice>();
		LINKED_LIST.add(BlinkDevice.HOST);
		
		CONNECTED_MAP = new HashMap<BlinkDevice, Object>();
		LINKED_MAP = new HashMap<BlinkDevice, Object>();
	}
	
	/**
	 * 
	 * @param device
	 * @param thread
	 */
	void addLink(BlinkDevice device) {
		LINKED_MAP.put(device, CONNECTED_MAP.get(device));
		
		if (LINKED_LIST.add(device))
			Collections.sort(LINKED_LIST);
	}

	/**
	 * 
	 * @param device
	 * @param passage
	 */
	void addLink(BlinkDevice device, BlinkDevice passage) {
		LINKED_MAP.put(device, CONNECTED_MAP.get(passage));

		if (LINKED_LIST.add(device))
			Collections.sort(LINKED_LIST);
	}
	
	/**
	 * 
	 * @param device
	 * @return
	 */
	Object removeLink(BlinkDevice device) {
		if (LINKED_LIST.remove(device))
			Collections.sort(LINKED_LIST);
		
		return LINKED_MAP.remove(device);
	}
	
	/**
	 * 
	 * @param device
	 * @param obj
	 */
	void addConnection(BlinkDevice device, Object obj) {
		if (obj instanceof ClassicLinkThread || obj instanceof BluetoothGatt) {
			CONNECTED_MAP.put(device, obj);
			
			if (LINKED_LIST.add(device))
				Collections.sort(LINKED_LIST);
		}
	}
	
	/**
	 * 
	 * @param device
	 * @return
	 */
	Object removeConnection(BlinkDevice device) {
		if (LINKED_LIST.remove(device))
			Collections.sort(LINKED_LIST);
		
		return CONNECTED_MAP.remove(device);
	}

	/**
	 * 
	 */
	void disconnectAllConnection() {
		for (Object obj : CONNECTED_MAP.values()) {
			if (obj != null) {
				if (obj instanceof BluetoothGatt)
					((BluetoothGatt) obj).close();
				
				else if (obj instanceof ClassicLinkThread) 
					((ClassicLinkThread) obj).destroyThread();
			}
		}
	}

	/**
	 * LINKED_LIST를 정렬한다.
	 */
	public void refresh() {
		Collections.sort(LINKED_LIST);
	}
	

	/**
	 * 
	 * @return
	 */
	public BlinkDevice getLinkedCenterDevice() {
		return LINKED_LIST.get(LINKED_LIST.size()-1);
	}

	/**
	 * 
	 * @param identity
	 * @return
	 */
	public BlinkDevice[] obtainLinkedDevicesWithIdentity(Identity identity) {
		ArrayList<BlinkDevice> mList = new ArrayList<BlinkDevice>();
		for (BlinkDevice device : LINKED_LIST) {
			if (device.getIdentity() == identity)
				mList.add(device);
		}
		
		BlinkDevice[] mArray = new BlinkDevice[mList.size()];
		return mList.toArray(mArray);
	}
	
	/**
	 * 
	 * @return
	 */
	public BlinkDevice[] obtainLinkedDevices() {
		BlinkDevice[] mArray = new BlinkDevice[LINKED_LIST.size()];
		return LINKED_LIST.toArray(mArray);
	}
	
	/**
	 * 
	 * @return
	 */
	public BlinkDevice[] obtainConnectedDevices() {
		BlinkDevice[] lists = new BlinkDevice[CONNECTED_MAP.size()];
		return CONNECTED_MAP.keySet().toArray(lists);
	}
	
	/**
	 * 
	 * @param device
	 * @return
	 */
	Object getConnectionObject(BlinkDevice device) {
		if (device == null)
			return null;
		
		return CONNECTED_MAP.get(device);
	}
	
	

	// *** Getter & Setter *** //
	public final String getGroupID() {
		return GroupID;
	}
	
	public final String getGroupName() {
		return GroupName;
	}
	
	public void setGroupName(String name) {
		GroupName = name;
	}
}
