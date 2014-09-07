package kr.poturns.blink.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkProfile;
import kr.poturns.blink.internal.comm.IBlinkEventBroadcast;
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
import android.util.Log;

/**
 * 블루투스 스캔 및 연결, 데이터통신 작업을 수행한다.
 * 
 * 
 * @author YeonHo.Kim
 * @since 2014.07.17
 *
 */
class BluetoothAssistant extends Handler {
	
	// *** CONSTANT DECLARATION *** //
	public final static String TAG = "BluetoothAssistant";
	
	private final static int DISCOVERY_TIMEOUT = 10000;	// ms
	private final static int ACCEPT_TIMEOUT = 60000;	// ms
	private final static int JOIN_TIMEOUT = 1000;		// ms
	
	
	
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
	 * 블루투스 통신에서 {@link InterDeviceManager}가 감지해야하는 IntentFilter를 얻어온다.
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

		mIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);			// 블루투스 페어링 상태 변화
		mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);				// 블루투스 ACL 연결
		mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);			// 블루투스 ACL 해제
		
		mIntentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);	// 블루투스 LE 연결 상태 변화

		return mIntentFilter;
	}

	
	
	// *** FIELD DECLARATION *** //
	final InterDeviceManager INTER_DEV_MANAGER;
	final ThreadGroup CONNECTION_GROUP;
	
	public BluetoothAssistant(InterDeviceManager manager) {
		INTER_DEV_MANAGER = manager;
		CONNECTION_GROUP = new ThreadGroup(TAG);
	}
	
	private BluetoothManager mBluetoothManager;
	private ServiceKeeper mServiceKeeper;
	private boolean isLeSupported;
	
	/**
	 * BluetoothAssistant를 초기화한다.
	 */
	void inititiate() {
		BlinkLocalBaseService mContext = INTER_DEV_MANAGER.MANAGER_CONTEXT;
		
		mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
		mServiceKeeper = ServiceKeeper.getInstance(mContext);
		
		isLeSupported = DeviceAnalyzer.getInstance(mContext).isAvailableBluetoothLE();
		
		int state = BluetoothAdapter.getDefaultAdapter().getState();
		switch (state) {
		case BluetoothAdapter.STATE_OFF:
			onBluetoothStateOff();
			break;
			
		case BluetoothAdapter.STATE_ON:
			onBluetoothStateOn();
			break;
		}
		
		mServiceKeeper.clearDiscovery();
		//mServiceKeeper.disconnectAllConnection();
	}
	
	/**
	 * BluetoothAssistant를 파괴한다.
	 * 블루투스 사용 상태를 정리하고, 서비스 종료 절차를 밟는다.
	 * 
	 * <p><b>(직접 호출하지 말것 ! Service 종료시 자동으로 호출됨.)</b>
	 */
	void destroy() {
		stopDiscovery();
		stopListeningServer();
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
		Intent mIntent = (BlinkDevice.HOST.isCenterDevice())? 
				new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE) : 
					new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE) ;
		mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		INTER_DEV_MANAGER.MANAGER_CONTEXT.startActivity(mIntent);
	}
	
	/**
	 * 블루투스가 On상태가 되었을 떄, 수행할 기능을 정의한다.
	 */
	void onBluetoothStateOn() {
		startListeningServer(true);
	}
	
	
	private boolean isClassicServerActivated = false;
	private Thread mClassicServerThread = null;
	private BluetoothServerSocket mServerSocket = null;
	private BluetoothGattServer mGattServer;
	
	/**
	 * Bluetooth 서버를 실행한다.
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
	 * Bluetooth 서버를 중지한다.
	 */
	public void stopListeningServer() {
		stopClassicServer();
		
		// TODO : BLE
//		if (isLeSupported) 
//			stopLeServer();
	}
	
	/**
	 * Bluetooth Classic 서버를 실행한다.
	 * 
	 * @param uuid
	 * @param secure
	 */
	public synchronized void startClassicServer(final UUID uuid, final boolean secure) {
		if (mClassicServerThread != null) 
			if (mClassicServerThread.isAlive())
				return;
		
		isClassicServerActivated = true;
		
		mClassicServerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				String name = BlinkProfile.SERVICE_NAME;
				BluetoothAdapter adapter = mBluetoothManager.getAdapter();
				
				try {
					mServerSocket = secure? 
							adapter.listenUsingRfcommWithServiceRecord(name, uuid) :
								adapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid);
							
					while (isClassicServerActivated) {
						try {
							if (mServerSocket == null)
								return;
							
							BluetoothSocket mBluetoothSocket = mServerSocket.accept(ACCEPT_TIMEOUT);
							BlinkDevice device  = BlinkDevice.load(mBluetoothSocket.getRemoteDevice());
							
							ClassicLinkThread thread = new ClassicLinkThread(sInstance, device, mBluetoothSocket, false);
							thread.startThread();
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					Log.d("BluetoothAssistant_ServerThread", "OUT!!");
					mServerSocket.close();
					
				} catch (Exception e) {
					try {
						if (mServerSocket != null)
							mServerSocket.close();
						
					} catch (IOException ee) { }
					
				} finally {
					mServerSocket = null;
				}
			}	
		}, "ClassicServerThread");
		mClassicServerThread.start();
	}
	
	/**
	 * Bluetooth Classic 서버를 중지한다.
	 */
	public synchronized void stopClassicServer() {
		isClassicServerActivated = false;

		Log.d("BluetoothAssistant_ServerThread", "STOP!!");
		if (mClassicServerThread != null && mServerSocket != null) {
			try {
				mServerSocket.close();
				mClassicServerThread.join(JOIN_TIMEOUT);
				
			} catch (IOException e) {
			} catch (InterruptedException e) {
			} finally {
				mClassicServerThread = null;
				mServerSocket = null;
			}
		}
	}

	/**
	 * Bluetooth LE 서버를 실행한다.
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
	 * Bluetooth LE 서버를 중지한다.
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
	 * startDiscovery(type, true).
	 * 
	 * @see #startDiscovery(int, boolean)
	 * @param type
	 */
	public void startDiscovery(int type) {
		startDiscovery(type, true);
	}
	
	/**
	 * 블루투스 Discovery를 시작한다. 현재 Discovery가 진행 중이라면 그대로 리턴한다.
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

		if (clear && !isLeScanning && !mAdapter.isDiscovering()) 
			mServiceKeeper.clearDiscovery();
		
		// New Discovery Start.
		if (BluetoothDevice.DEVICE_TYPE_LE == type && !isLeScanning) {
			postDelayed(new Runnable() {
				@Override
				public void run() {
					// 10초간 LE Discovery
					mAdapter.stopLeScan(INTER_DEV_MANAGER);
					isLeScanning = false;
				}
			}, DISCOVERY_TIMEOUT);
			
			mAdapter.startLeScan(INTER_DEV_MANAGER);
			isLeScanning = true;
			
		} else if (BluetoothDevice.DEVICE_TYPE_CLASSIC == type && !mAdapter.isDiscovering()) {
			isLeScanning = false;
			mAdapter.startDiscovery();
		}
	}
	
	/**
	 * 현재 수행 중인 Discovery를 중지한다.
	 */
	public void stopDiscovery() {
		removeCallbacksAndMessages(null);
		
		BluetoothAdapter mAdapter = mBluetoothManager.getAdapter();
		if (isLeScanning)
			mAdapter.stopLeScan(INTER_DEV_MANAGER);	
		
		if (mAdapter.isDiscovering())
			mAdapter.cancelDiscovery();
		
		isLeScanning = false;
	}

	/**
	 * 해당 BlinkDevice에 BLINK 서비스 연결을 요청한다.
	 * <br>connectToDeviceFromClient(BlinkDevice, BlinkProfile.UUID_BLINK)
	 * 
	 * @see #connectToDeviceFromClient(BlinkDevice, UUID)
	 * @param device
	 */
	public void connectToDeviceFromClient(BlinkDevice device) {
		connectToDeviceFromClient(device, BlinkProfile.UUID_BLINK);
	}
	
	/**
	 * 해당 BlinkDevice에 다음의 UUID로 연결을 요청한다.
	 * 
	 * @param device
	 * @param uuid
	 */
	public void connectToDeviceFromClient(BlinkDevice device, UUID uuid) {
		BluetoothDevice origin = device.obtainBluetoothDevice();
		
		if (device.isLESupported()) {
			origin.connectGatt(INTER_DEV_MANAGER.MANAGER_CONTEXT, device.isAutoConnect(), mBluetoothGattCallback);

		} else {
			BluetoothSocket mBluetoothSocket;
			try {
				if (device.isSecureConnect()) 
					mBluetoothSocket = origin.createRfcommSocketToServiceRecord(uuid);
				else
					mBluetoothSocket = origin.createInsecureRfcommSocketToServiceRecord(uuid);
				mBluetoothSocket.connect();
				
				final ClassicLinkThread thread = new ClassicLinkThread(this, device, mBluetoothSocket, true);
				thread.startThread();

				
			} catch (IOException e) {
				Intent mIntent = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_CONNECTION_FAILED);
				mIntent.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) device);
				INTER_DEV_MANAGER.MANAGER_CONTEXT.sendBroadcast(mIntent, IBlinkEventBroadcast.PERMISSION_LISTEN_STATE_MESSAGE);
			}
		}
	}
	
	/**
	 * 해당 디바이스와의 연결을 해제한다.
	 * 
	 * @see {@link ServiceKeeper#removeConnection(BlinkDevice)}
	 * @param device
	 */
	public void disconnectDevice(BlinkDevice device) {
		Log.d("InterDeviceManager_disconnectFromDeviceAsClient()", "Disconnect");
		mServiceKeeper.removeConnection(device);
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

			BlinkDevice device = BlinkDevice.load(gatt.getDevice());
			
			if (status == BluetoothGatt.GATT_SUCCESS) {
				
				Intent mIntent; 
				switch (newState) {
				case BluetoothGatt.STATE_CONNECTED:
					mServiceKeeper.addConnection(device, gatt);

					mIntent = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_CONNECTED);
					break;
						
				case BluetoothGatt.STATE_DISCONNECTED:
					mServiceKeeper.removeConnection(device);

					mIntent = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_DISCONNECTED);
					break;
					
				default:
					return;
				}
				
				mIntent.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) device);
				INTER_DEV_MANAGER.MANAGER_CONTEXT.sendBroadcast(mIntent, IBlinkEventBroadcast.PERMISSION_LISTEN_STATE_MESSAGE);
					
			} else {
				switch (newState) {
				//case BluetoothGatt.STATE_CONNECTED:
				case BluetoothGatt.STATE_CONNECTING:
					Intent mIntent = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_CONNECTION_FAILED);
					mIntent.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) device);
					INTER_DEV_MANAGER.MANAGER_CONTEXT.sendBroadcast(mIntent, IBlinkEventBroadcast.PERMISSION_LISTEN_STATE_MESSAGE);
					break;
				}
			}
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
		public void onConnectionStateChange(BluetoothDevice origin, int status, int newState) {
			BlinkDevice device = BlinkDevice.load(origin);

			if (status == BluetoothGatt.GATT_SUCCESS) {
				
				Intent mIntent; 
				switch (newState) {
				case BluetoothGatt.STATE_CONNECTED:
					// TODO : Gatt == null??
					BluetoothGatt gatt = null;
					mServiceKeeper.addConnection(device, gatt);

					mIntent = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_CONNECTED);
					break;
						
				case BluetoothGatt.STATE_DISCONNECTED:
					mServiceKeeper.removeConnection(device);

					mIntent = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_DISCONNECTED);
					break;
					
				default:
					return;
				}
				
				mIntent.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) device);
				INTER_DEV_MANAGER.MANAGER_CONTEXT.sendBroadcast(mIntent, IBlinkEventBroadcast.PERMISSION_LISTEN_STATE_MESSAGE);
					
			} else {
				switch (newState) {
				//case BluetoothGatt.STATE_CONNECTED:
				case BluetoothGatt.STATE_CONNECTING:
					Intent mIntent = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_CONNECTION_FAILED);
					mIntent.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) device);
					INTER_DEV_MANAGER.MANAGER_CONTEXT.sendBroadcast(mIntent, IBlinkEventBroadcast.PERMISSION_LISTEN_STATE_MESSAGE);
					break;
				}
			}
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
