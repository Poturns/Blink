package kr.poturns.blink.internal.comm;

import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.IInternalEventCallback;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.App;

/**
 * Blink Service Process와 통신하는 IPC Binder
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
	 * 등록된 {@link IInternalEventCallback}을 제거한다.
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
	 * @return BlinkDevice[] : 탐색된 Device 배열
	 *
	 */
	BlinkDevice[] obtainCurrentDiscoveryList();
	
	void startListeningAsServer();
	
	void stopListeningAsServer();

	/**
	 * 기기와 해당 {@code BlinkDevice}를 연결한다.
	 *
	 */
	void connectDevice(inout BlinkDevice device);
	
	/**
	 * 기기와 해당 {@code BlinkDevice}를 연결해제 한다.
	 *
	 */
	void disconnectDevice(inout BlinkDevice device);
	
	/**
	 * 현재 연결되어 있는 디바이스들을 받아온다.
	 *
	 * @return BlinkDevice[] : 연결된 Device 배열
	 */
	BlinkDevice[] obtainConnectedDeviceList();
	
	/**
	 * 해당 디바이스로 Blink Message를 보낸다.
	 *
	 * @param target : 
	 * @param jsonMsg : 
	 *
	 */
	void sendBlinkMessages(inout BlinkDevice target, String jsonMsg);
	
	/**
	 * ControlActivity를 호출한다.
	 * <br>이 때, 해당 애플리케이션의 AndroidManifest.xml에 ControlActivity에 대한 Component가 명시되어 있어야 한다.
	 *
	 */
	void openControlActivity(); 
	
	/**
	 * Sqlite에 데이터를 저장하거나 가져오는 매서드
	 * 필요할 경우 바인더에서 블루투스쪽으로 데이터를 요청한다.
	 * @see BlinkDatabaseManager
	 * @see SqliteManager
	 * @author Jiwon
	 *
	 */
	void setRequestPolicy(int requestPolicy);
	void registerApplicationInfo(String PackageName,String AppName);
	BlinkDevice getBlinkDevice();
	void registerBlinkApp(in BlinkAppInfo mBlinkAppInfo);
	void obtainMeasurementData(String ClassName,String DateTimeFrom,String DateTimeTo,int ContainType,int requestCode);
	void obtainMeasurementDataById(inout List<Measurement> mMeasurementList,String DateTimeFrom,String DateTimeTo,int requestCode);
	void startFunction(inout Function function,int requestCode);
	void sendMeasurementData(inout BlinkAppInfo targetBlinkAppInfo,String json,int requestCode);
	
	/**
	 * 테스트를 위한 임시 매소드
	 */
	 void SyncBlinkApp();
	 void SyncMeasurementData();
}
