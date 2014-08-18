package kr.poturns.blink.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkProfile;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

/**
 * 블루투스 스캔 및 연결, 데이터통신 작업을 수행한다.
 * 
 * 
 * @author YeonHo.Kim
 * @since 2014.07.17
 *
 */
class BluetoothAssistant extends Handler{
	
	private boolean onLog = true;

	// *** CONSTANT DECLARATION *** //
	public final static String TAG = "BluetoothAssistant";
	
	private final static int SERVER_ACCEPT_TIMEOUT = 60000;
	
	final static int MESSAGE_READ_STREAM = 0x1;

	
	
	// *** STATIC DECLARATION *** //
	/**
	 * BluetoothAssistant의 Singleton-인스턴스
	 */
	private static BluetoothAssistant sInstance = null;
	
	/**
	 * BluetoothAssistant의 Singleton-인스턴스를 반환한다.
	 * 
	 * @param manager
	 * @return manager가 Null일 경우, 할당되어 있는 Instance를 반환한다.
	 */
	public static BluetoothAssistant getInstance(InterDeviceManager manager) {
		if (sInstance == null && manager != null)
			sInstance = new BluetoothAssistant(manager);
		return sInstance;
	}

	/**
	 * 블루투스 통신에서 BroadcastReceiver가 감지해야하는 IntentFilter를 얻어온다.
	 * 
	 * @return 
	 */
	public static IntentFilter obtainIntentFilter() {
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);				// 블루투스 상태 변화 
		mIntentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);			// 블루투스 탐색 모드 변화
		mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);			// 블루투스 탐색 시작
		mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);		// 블루투스 탐색 종료
		mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);						// 블루투스 탐색시 디바이스 발견
		mIntentFilter.addAction(BluetoothDevice.ACTION_UUID);						// 블루투스 UUID 발견

		mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);				// 블루투스 ACL 연결
		mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);			// 블루투스 ACL 해제
		mIntentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);	// 블루투스 LE 연결 상태 변화

		mIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		
		return mIntentFilter;
	}

	
	
	// *** FIELD DECLARATION *** //
	final InterDeviceManager INTER_DEV_MANAGER;
	final ThreadGroup CONNECTION_GROUP;
	
	//TODO : HashMap, ArrayList는 Thread-Safe하지 않으므로, Thread에 따른 데이터 정합성을 체크해야함!
	private final HashMap<String, BluetoothGatt> LE_CONN_MAP;
	private final HashMap<String, ClassicLinkThread> CLASSIC_CONN_MAP;
	private final ArrayList<BlinkDevice> DISCOVERY_DEV_LIST;
	
	public BluetoothAssistant(InterDeviceManager manager) {
		INTER_DEV_MANAGER = manager;
		CONNECTION_GROUP = new ThreadGroup(TAG);
		
		LE_CONN_MAP = new HashMap<String, BluetoothGatt>();
		CLASSIC_CONN_MAP = new HashMap<String, ClassicLinkThread>();
		DISCOVERY_DEV_LIST = new ArrayList<BlinkDevice>();
	}
	
	private BluetoothManager mBluetoothManager;
	private FunctionOperator mFunctionOperator;
	private DeviceAnalyzer.Identity mIdentity;
	private boolean isLeSupported;
	
	/**
	 * 현 객체를 초기화한다.
	 */
	void inititiate() {
		BlinkLocalBaseService mContext = INTER_DEV_MANAGER.MANAGER_CONTEXT;
		
		mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
		mFunctionOperator = new FunctionOperator(mContext);
		mIdentity = DeviceAnalyzer.getInstance(mContext).getCurrentIdentity();
		
		isLeSupported = DeviceAnalyzer.getInstance(mContext).hasBluetoothLE;
		
		int state = BluetoothAdapter.getDefaultAdapter().getState();
		switch (state) {
		case BluetoothAdapter.STATE_OFF:
			onBluetoothStateOff();
			break;
			
		case BluetoothAdapter.STATE_ON:
			onBluetoothStateOn();
			break;
		}
		
		LE_CONN_MAP.clear();
		CLASSIC_CONN_MAP.clear();
		DISCOVERY_DEV_LIST.clear();
	}
	
	/**
	 * 현 객체를 파괴한다.
	 */
	void destroy() {
		BlinkDevice.clearCache();
	}

	/**
	 * 블루투스가 Off상태로 전환 중일 때, 수행할 기능을 정의한다.
	 */
	void onBluetoothStateTurningOff() {
		stopListeningServer();
	}
	
	/**
	 * 블루투스가 Off상태가 되었을 때, 수행할 기능을 정의한다.
	 */
	void onBluetoothStateOff() {
		Intent mIntent = (DeviceAnalyzer.Identity.CORE.equals(mIdentity))? 
				new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE) : 
					new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE) ;
					
		mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		INTER_DEV_MANAGER.MANAGER_CONTEXT.startActivity(mIntent);
	}
	
	/**
	 * 블루투스가 On상태가 되었을 떄, 수행할 기능을 정의한다.
	 */
	void onBluetoothStateOn() {
		mBluetoothManager = (BluetoothManager) INTER_DEV_MANAGER.MANAGER_CONTEXT.getSystemService(Context.BLUETOOTH_SERVICE);
		startListeningServer(true);
	}
	
	
	private boolean isServerActivated = false;
	private Thread mServerThread = null;
	private BluetoothGattServer mGattServer;
	
	/**
	 * 
	 * @param secure
	 */
	public void startListeningServer(boolean secure) {
		startClassicServer(BlinkProfile.UUID_BLINK, secure);
		
		// TODO : BLE 
//		if (isLeSupported)
//			startLeServer(new BluetoothGattService(
//					BlinkProfile.UUID_BLINK, BluetoothGattService.SERVICE_TYPE_PRIMARY));
	}
	
	/**
	 * 
	 */
	public void stopListeningServer() {
		stopClassicServer();
		
		// TODO : BLE
//		if (isLeSupported) 
//			stopLeServer();
	}
	
	/**
	 * 
	 * @param uuid
	 * @param secure
	 */
	public synchronized void startClassicServer(final UUID uuid, final boolean secure) {
		if (mServerThread != null) 
			if (mServerThread.isAlive())
				return;
		
		isServerActivated = true;
		
		mServerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				String name = BlinkProfile.SERVICE_NAME;
				BluetoothAdapter adapter = mBluetoothManager.getAdapter();
				
				BluetoothServerSocket mServerSocket;
				try {
					mServerSocket = secure? 
							adapter.listenUsingRfcommWithServiceRecord(name, uuid) :
								adapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid);
							
					while (isServerActivated) {
						try {
							BluetoothSocket mBluetoothSocket = mServerSocket.accept(SERVER_ACCEPT_TIMEOUT);
							BlinkDevice device  = BlinkDevice.load(mBluetoothSocket.getRemoteDevice());
							
							ClassicLinkThread thread = new ClassicLinkThread(sInstance, device, mBluetoothSocket, false);
							thread.startListening();
							
							CLASSIC_CONN_MAP.put(device.getAddress(), thread);
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					Log.d("BluetoothAssistant_ServerThread", "OUT!!");
					mServerSocket.close();
					
				} catch (IOException e) {
					
				} finally {
					mServerSocket = null;
				}
				
			}	
		}, "ClassicServerThread");
		mServerThread.start();
	}
	
	/**
	 * 
	 */
	public synchronized void stopClassicServer() {
		isServerActivated = false;

		Log.d("BluetoothAssistant_ServerThread", "STOP!!");
		if (mServerThread != null) {
			try {
				//mServerThread.interrupt();
				mServerThread.join(3000);
					
			} catch (InterruptedException e) {
			} finally {
				mServerThread = null;
			}
		}
	}

	/**
	 * 
	 * @param service
	 */
	public void startLeServer(BluetoothGattService service) {
		if (!isLeSupported)
			return;
		
		Log.d("BluetoothAssistant", "StartLEServer");
		
		if (mGattServer == null) {
			mGattServer = mBluetoothManager.openGattServer(INTER_DEV_MANAGER.MANAGER_CONTEXT, mBluetoothGattServerCallback);
		}
		
		if (mGattServer != null && service != null)
			mGattServer.addService(service);
	}
	
	/**
	 * 
	 */
	public void stopLeServer() {
		if (mGattServer != null) {
			mGattServer.clearServices();
			mGattServer.close();
		}
		mGattServer = null;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			
		}
		
	}
	
	private boolean isLeScanning = false;
	
	/**
	 * {@link #startDiscovery(type, clear)}의 [ clear = true ] Default 값을 같는다. 
	 * @param type
	 */
	public void startDiscovery(int type) {
		startDiscovery(type, true);
	}
	
	/**
	 * 현재 수행 중인 Discovery를 중단하고, 새 블루투스 Discovery를 시작한다. 
	 * 
	 * <p>매개변수로 전달받는 type에 따라 해당 Discovery를 수행한다. <br>
	 * {@link BluetoothDevice#DEVICE_TYPE_CLASSIC}는 Classic Discovery를,
	 * {@link BluetoothDevice#DEVICE_TYPE_LE}는 LE Discovery를 수행한다.
	 * 
	 * <p>매개변수 clear 여부에 따라 {@link #DISCOVERY_DEV_LIST}의 초기화 여부가 결정된다.
	 * 
	 * @param type : {@link BluetoothDevice}의 타입 상수를 받는다.
	 * @param clear
	 */
	public void startDiscovery(int type, boolean clear) {
		final BluetoothAdapter mAdapter = mBluetoothManager.getAdapter();

		if (clear)
			DISCOVERY_DEV_LIST.clear();

		// Ongoing Discovery Stop.
		if (isLeScanning) {
			removeCallbacksAndMessages(null);
			mAdapter.stopLeScan(INTER_DEV_MANAGER);
		
		} else if (mAdapter.isDiscovering())
			mAdapter.cancelDiscovery();
		
		
		// New Discovery Start.
		if (BluetoothDevice.DEVICE_TYPE_LE == type) {
			postDelayed(new Runnable() {
				@Override
				public void run() {
					isLeScanning = false;
					mAdapter.stopLeScan(INTER_DEV_MANAGER);
				}
			}, 10000);
			
			isLeScanning = true;
			mAdapter.startLeScan(INTER_DEV_MANAGER);
			
		} else if (BluetoothDevice.DEVICE_TYPE_CLASSIC == type) {
			isLeScanning = false;
			mAdapter.startDiscovery();
		}
	}
	
	/**
	 * 현재 수행 중인 Discovery를 중지한다.
	 */
	public void stopDiscovery() {
		BluetoothAdapter mAdapter = mBluetoothManager.getAdapter();
		removeCallbacksAndMessages(null);

		if (isLeScanning && (isLeScanning = false))
			mAdapter.stopLeScan(INTER_DEV_MANAGER);	
		
		if (mAdapter.isDiscovering())
			mAdapter.cancelDiscovery();
	}

	/**
	 * 
	 * @return
	 */
	public BlinkDevice[] obtainCurrentDiscoveryList() {
		BlinkDevice[] lists = new BlinkDevice[DISCOVERY_DEV_LIST.size()];
		
		for (int i=0; i<lists.length; i++) {
			lists[i] = DISCOVERY_DEV_LIST.get(i);
		}
		return lists;
	}
	
	/**
	 * 
	 * @return
	 */
	public BlinkDevice[] obtainConnectedDeviceList() {
		BlinkDevice[] lists = new BlinkDevice[LE_CONN_MAP.size() + CLASSIC_CONN_MAP.size()];
		
		for (int i=0; i<lists.length; i++) {
			
		}
		
		return lists;
	}
	
	/**
	 * 
	 * @param deviceX
	 */
	public void connectToDeviceAsClient(BlinkDevice deviceX) {
		// TODO :
		//UUID uuid = deviceX.getDevice().getUuids()[0].getUuid();
		//connectToDeviceAsClient(deviceX, uuid);
	}
	
	/**
	 * 
	 * @param deviceX
	 * @param uuid
	 */
	public void connectToDeviceAsClient(BlinkDevice deviceX, UUID uuid) {
		BluetoothDevice device = deviceX.obtainBluetoothDevice();
		
		if (deviceX.isLESupported()) {
			device.connectGatt(INTER_DEV_MANAGER.MANAGER_CONTEXT, deviceX.isAutoConnect(), mBluetoothGattCallback);

		} else {
			BluetoothSocket mBluetoothSocket;
			try {
				if (deviceX.isSecureConnect()) 
					mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
				else
					mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
				mBluetoothSocket.connect();
				
				ClassicLinkThread thread = new ClassicLinkThread(this, deviceX, mBluetoothSocket, true);
				thread.startListening();
		
				CLASSIC_CONN_MAP.put(deviceX.getAddress(), thread);
				
			} catch (IOException e) {
				// TODO : 연결 요청했던 경우엔, 해당 요청 어플리케이션에게만 호출하는게 나을듯.
				int size = INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.beginBroadcast();
				for (int i = 0; i < size; i++) {
					try {
						INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.getBroadcastItem(i).onDeviceConnectionFailed(deviceX);
						
					} catch (RemoteException ee) { ; }
				}
				INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.finishBroadcast();
			}
		}
	}
	
	/**
	 * 
	 * @param deviceX
	 */
	public void disconnectFromDeviceAsClient(BlinkDevice deviceX) {
		Log.d("InterDeviceManager_disconnectFromDeviceAsClient()", "Disconnect");
		if (deviceX.isLESupported()) {
			BluetoothGatt mGatt = LE_CONN_MAP.get(deviceX.getAddress());
			mGatt.close();
			
		} else {
			try {
				ClassicLinkThread mThread = CLASSIC_CONN_MAP.get(deviceX.getAddress());
				mThread.destroy();
				mThread.join(1000);
				
			} catch (InterruptedException e) {
				
			}
		}
		
	}
	
	
	/**
	 * 
	 * @param deviceX
	 */
	void onDeviceDiscovered(BlinkDevice deviceX) {
		DISCOVERY_DEV_LIST.add(deviceX);
	}

	/**
	 * 
	 * @param deviceX
	 */
	void onDeviceConnected(BlinkDevice deviceX) {
		
	}
	
	/**
	 * 
	 * @param deviceX
	 */
	void onDeviceDisconnected(BlinkDevice deviceX) {
		if (deviceX.isLESupported())
			LE_CONN_MAP.remove(deviceX.getAddress());
		else
			CLASSIC_CONN_MAP.remove(deviceX.getAddress());
	}

	/**
	 * 
	 * @param deviceX
	 */
	synchronized void onMessageReceivedFrom(String json, BlinkDevice deviceX) {
		if (onLog)
			Log.d("BluetoothAssistant_onMessageReceivedFrom", json + " [from ]");
		mFunctionOperator.acceptJsonData(json, deviceX);
	}

	/**
	 * 
	 * @param json
	 * @param deviceX
	 */
	void onMessageSentTo(String json, BlinkDevice deviceX) {
		if (deviceX.isLESupported()) {
			
			
		} else {
			CLASSIC_CONN_MAP.get(deviceX.getAddress()).sendMessageToDevice(json);
			
		}

	}

	
	// *** CALLBACK FIELD DECLARATION *** //
	/**
	 * Bluetooth Low-Energy Client Callback instance.
	 */
	private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
		
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			super.onCharacteristicChanged(gatt, characteristic);
		}
		
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			super.onCharacteristicRead(gatt, characteristic, status);
		}
		
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			super.onCharacteristicWrite(gatt, characteristic, status);
		}
		
		@Override 
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

			BlinkDevice deviceX = BlinkDevice.load(gatt.getDevice());
			
			int size = INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.beginBroadcast();
			if (status == BluetoothGatt.GATT_SUCCESS) {
				switch (newState) {
					case BluetoothGatt.STATE_CONNECTED:
						LE_CONN_MAP.put(deviceX.getAddress(), gatt);
						
						for (int i = 0; i < size; i++) {
							try {
								INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.getBroadcastItem(i).onDeviceConnected(deviceX);
								
							} catch (RemoteException e) { ; }
						}
						break;
						
					case BluetoothGatt.STATE_DISCONNECTED:
						LE_CONN_MAP.remove(deviceX.getAddress());
						
						for (int i = 0; i < size; i++) {
							try {
								INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.getBroadcastItem(i).onDeviceDisconnected(deviceX);
									
							} catch (RemoteException e) { ; }
						}
						break;
					}
					
			} else {
				switch (newState) {
				//case BluetoothGatt.STATE_CONNECTED:
				case BluetoothGatt.STATE_CONNECTING:
					for (int i = 0; i < size; i++) {
						try {
							INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.getBroadcastItem(i).onDeviceConnectionFailed(deviceX);
								
						} catch (RemoteException e) { ; }
					}
					break;
				}
			}
			INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.finishBroadcast();
		}
		
		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			super.onDescriptorRead(gatt, descriptor, status);
		}
		
		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			super.onDescriptorWrite(gatt, descriptor, status);
		}
		
		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			// TODO Auto-generated method stub
			super.onReadRemoteRssi(gatt, rssi, status);
		}
		
		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			super.onReliableWriteCompleted(gatt, status);
		}
		
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			super.onServicesDiscovered(gatt, status);
		}
	}; 
	
	/**
	 * Bluetooth Low-Energy Server Callback instance.
	 */
	private BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {
		
		@Override
		public void onCharacteristicReadRequest(BluetoothDevice device,
				int requestId, int offset,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
		}
		
		@Override
		public void onCharacteristicWriteRequest(BluetoothDevice device,
				int requestId, BluetoothGattCharacteristic characteristic,
				boolean preparedWrite, boolean responseNeeded, int offset,
				byte[] value) {
			// TODO Auto-generated method stub
			super.onCharacteristicWriteRequest(device, requestId, characteristic,
					preparedWrite, responseNeeded, offset, value);
		}
		
		@Override
		public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
			BlinkDevice deviceX = BlinkDevice.load(device);
			
			int size = INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.beginBroadcast();
			if (status == BluetoothGatt.GATT_SUCCESS) {
				switch (newState) {
					case BluetoothGatt.STATE_CONNECTED:
						// TODO : LE Server Callback에서는 Gatt를 받지 않는다!??
						LE_CONN_MAP.put(deviceX.getAddress(), null);
						
						for (int i = 0; i < size; i++) {
							try {
								INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.getBroadcastItem(i).onDeviceConnected(deviceX);
								
							} catch (RemoteException e) { ; }
						}
						break;
						
					case BluetoothGatt.STATE_DISCONNECTED:
						LE_CONN_MAP.remove(deviceX.getAddress());
						
						for (int i = 0; i < size; i++) {
							try {
								INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.getBroadcastItem(i).onDeviceDisconnected(deviceX);
									
							} catch (RemoteException e) { ; }
						}
						break;
					}
					
			} else {
				switch (newState) {
				//case BluetoothGatt.STATE_CONNECTED:
				case BluetoothGatt.STATE_CONNECTING:
					for (int i = 0; i < size; i++) {
						try {
							INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.getBroadcastItem(i).onDeviceConnectionFailed(deviceX);
								
						} catch (RemoteException e) { ; }
					}
					break;
				}
			}
			INTER_DEV_MANAGER.EVENT_CALLBACK_LIST.finishBroadcast();
		}
		
		@Override
		public void onDescriptorReadRequest(BluetoothDevice device,
				int requestId, int offset, BluetoothGattDescriptor descriptor) {
			// TODO Auto-generated method stub
			super.onDescriptorReadRequest(device, requestId, offset, descriptor);
		}
		
		@Override
		public void onDescriptorWriteRequest(BluetoothDevice device,
				int requestId, BluetoothGattDescriptor descriptor,
				boolean preparedWrite, boolean responseNeeded, int offset,
				byte[] value) {
			// TODO Auto-generated method stub
			super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite,
					responseNeeded, offset, value);
		}
		
		@Override
		public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
			// TODO Auto-generated method stub
			super.onExecuteWrite(device, requestId, execute);
		}
		
		@Override
		public void onServiceAdded(int status, BluetoothGattService service) {
			// TODO Auto-generated method stub
			super.onServiceAdded(status, service);
		}
	};

}
