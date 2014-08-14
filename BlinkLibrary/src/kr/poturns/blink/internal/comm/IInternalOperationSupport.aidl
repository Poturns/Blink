package kr.poturns.blink.internal.comm;

import kr.poturns.blink.internal.comm.BluetoothDeviceExtended;
import kr.poturns.blink.internal.comm.IInternalEventCallback;

/**
 *
 * @author Yeonho.Kim
 * @since 2014.08.05
 *
 */
interface IInternalOperationSupport {

	/**
	 * 서비스로부터 요청한 명령에 대한 반응을 얻을 수 있는  {@link IInternalEventCallback}을 등록한다.
	 */
	boolean registerCallback(IInternalEventCallback callback);
	
	/**
	 * 등록된 {@link IInternalEventCallback}을 등록한다.
	 */
	boolean unregisterCallback(IInternalEventCallback callback);
	
	/**
	 * 블루투스 Discovery를 시작한다. 
	 * 
	 * <p>매개변수로 전달받는 type에 따라 해당 Discovery를 수행한다. <br>
	 * {@link BluetoothDevice#DEVICE_TYPE_CLASSIC}는 Classic Discovery를,
	 * {@link BluetoothDevice#DEVICE_TYPE_LE}는 LE Discovery를,
	 * {@link BluetoothDevice#DEVICE_TYPE_DUAL} 및 {@link BluetoothDevice#DEVICE_TYPE_UNKNOWN}은 
	 * Classic Discovery 후 LE Discovery를 수행한다.
	 * 
	 * @param type : {@link BluetoothDevice}의 타입 상수를 받는다.
	 * 
	 */
	void startDiscovery(int type);
	
	/**
	 * 블루투스 Discovery를 중단한다.
	 */
	void stopDiscovery();
	
	/**
	 * 현재까지 수집된 Discovery 디바이스들을 받아온다.  
	 *
	 * @return BluetoothDeviceExtended[] : 탐색된 Device 배열
	 *
	 */
	BluetoothDeviceExtended[] obtainCurrentDiscoveryList();
	
	void startListeningAsServer();
	
	void stopListeningAsServer();

	/**
	 *
	 *
	 */
	void connectDevice(inout BluetoothDeviceExtended deviceX);
	
	/**
	 *
	 *
	 */
	void disconnectDevice(inout BluetoothDeviceExtended deviceX);
	
	/**
	 * 현재 연결되어 있는 디바이스들을 받아온다.
	 *
	 * @return BluetoothDeviceExtended[] : 연결된 Device 배열
	 */
	BluetoothDeviceExtended[] obtainConnectedDeviceList();
	
	/**
	 * 해당 디바이스로 Blink Message를 보낸다.
	 *
	 * @param target : 
	 * @param jsonMsg : 
	 *
	 */
	void sendBlinkMessages(inout BluetoothDeviceExtended target, String jsonMsg);
}