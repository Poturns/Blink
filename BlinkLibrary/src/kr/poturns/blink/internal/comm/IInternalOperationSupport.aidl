package kr.poturns.blink.internal.comm;

import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.IInternalEventCallback;

import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.App;

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
	 * @return BlinkDevice[] : 탐색된 Device 배열
	 *
	 */
	BlinkDevice[] obtainCurrentDiscoveryList();
	
	void startListeningAsServer();
	
	void stopListeningAsServer();

	/**
	 *
	 *
	 */
	void connectDevice(inout BlinkDevice device);
	
	/**
	 *
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
	 * Sqlite에 데이터를 저장하거나 가져오는 매서드
	 * 필요할 경우 바인더에서 블루투스쪽으로 데이터를 요청한다.
	 * 관련 클래스는 BlinkDatabaseManager, SqliteManager이다.
	 * @author Jiwon
	 *
	 */
	void registerApplicationInfo(String DeviceName,String PackageName,String AppName);
	SystemDatabaseObject obtainSystemDatabase(String DeviceName,String PackageName);
	List<SystemDatabaseObject> obtainSystemDatabaseAll();
	void registerSystemDatabase(inout SystemDatabaseObject mSystemDatabaseObject);
	void registerMeasurementData(inout SystemDatabaseObject mSystemDatabaseObject,String ClassName,String JsonObj);
	String obtainMeasurementData(String ClassName,String DateTimeFrom,String DateTimeTo,int ContainType);
	List<MeasurementData> obtainMeasurementDataById(inout List<Measurement> mMeasurementList,String DateTimeFrom,String DateTimeTo);
	void registerLog(String Device,String App,int Type,String Content);
	List<BlinkLog> obtainLog(String Device,String App,int Type,String DateTimeFrom,String DateTimeTo);
	void startFunction(inout Function mFunction);
	
	/**
	 * BlinkDatabaseManager 관련 매서드
	 * @author Jiwon
	 *
	 */	
	IInternalOperationSupport queryDevice(String where);
	IInternalOperationSupport queryApp(String where);
	IInternalOperationSupport queryFunction(String where);
	IInternalOperationSupport queryMeasurement(String where);
	IInternalOperationSupport queryMeasurementData(String where);
	boolean checkInDeviceByMeasureList(inout List<Measurement> mMeasurementList);
	boolean checkInDeviceByFunction(inout Function mFunction);
	boolean checkInDeviceByClass(String ClassName);
	List<Device> getDeviceList();
	void setDeviceList(inout List<Device> mDeviceList);
	List<App> getAppList();
	void setAppList(inout List<App> mAppList);
	List<Function> getFunctionList();
	void setFunctionList(inout List<Function> mFunctionList);
	List<Measurement> getMeasurementList();
	void setMeasurementList(inout List<Measurement> mMeasurementList);
	List<MeasurementData> getMeasurementDataList();
	void setMeasurementDataList(inout List<MeasurementData> mMeasurementDataList);
}