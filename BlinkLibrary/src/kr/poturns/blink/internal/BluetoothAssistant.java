package kr.poturns.blink.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import kr.poturns.blink.internal.comm.BluetoothDeviceExtended;
import kr.poturns.blink.internal.comm.InterDeviceEventListener;
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

/**
 * 블루투스 스캔 및 연결, 데이터통신 작업을 수행한다.
 * 
 * @author YeonHo.Kim
 * @since 2014.07.17
 *
 */
class BluetoothAssistant implements InterDeviceEventListener {

	// *** CONSTANT DECLARATION *** //
	public final static String ACTION_STATE_ON = "android.bluetooth.adapter.action.STATE_ON";
	
	final static int MESSAGE_READ_STREAM = 0x1;

	
	// *** STATIC DECLARATION *** //
	private static BluetoothAssistant sInstance = null;
	
	/**
	 * BluetoothAssistant의 Singleton-인스턴스를 반환한다.
	 * @param manager ( :{@link InterDeviceManager} )
	 * @return manager가 Null일 경우, 기존의 Instance를 반환한다.
	 */
	public static BluetoothAssistant getInstance(InterDeviceManager manager) {
		if (sInstance == null && manager != null)
			sInstance = new BluetoothAssistant(manager);
		return sInstance;
	}

	/**
	 * 블루투스 통신에서 감지해야하는 IntentFilter를 얻어온다.
	 * @return 
	 */
	public static IntentFilter obtainIntentFilter() {
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

		mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		mIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		
		mIntentFilter.addAction(BluetoothAssistant.ACTION_STATE_ON);
		
		return mIntentFilter;
	}

	
	// *** FIELD DECLARATION *** //
	final InterDeviceManager INTER_DEV_MANAGER;
	final ThreadGroup CONNECTION_GROUP;
	
	private final BluetoothManager BLUETOOTH_MANAGER;
	//TODO : HashMap, ArrayList는 Thread-Safe하지 않으므로, Thread에 따른 데이터 정합성을 체크해야함!
	private final HashMap<BluetoothDeviceExtended, BluetoothGatt> LE_MAP;
	private final HashMap<BluetoothDeviceExtended, ClassicLinkThread> CLASSIC_MAP;
	private final ArrayList<BluetoothDeviceExtended> DISCOVERY_LIST;

	private FunctionOperator mFunctionOperator;
	private boolean isLeSupported;
	
	public BluetoothAssistant(InterDeviceManager manager) {
		super();
		
		INTER_DEV_MANAGER = manager;
		BLUETOOTH_MANAGER = (BluetoothManager) manager.getContext().getSystemService(Context.BLUETOOTH_SERVICE);
		
		CONNECTION_GROUP = new ThreadGroup("BluetoothAssistant");
		
		LE_MAP = new HashMap<BluetoothDeviceExtended, BluetoothGatt>();
		CLASSIC_MAP = new HashMap<BluetoothDeviceExtended, ClassicLinkThread>();
		DISCOVERY_LIST = new ArrayList<BluetoothDeviceExtended>();
	}
	
	public void init() {
		Context mContext = INTER_DEV_MANAGER.getContext();
		BluetoothAdapter mAdapter = BLUETOOTH_MANAGER.getAdapter();
		
		
		int state = mAdapter.getState();
		switch (state) {
		case BluetoothAdapter.STATE_OFF:
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
			break;
			
		case BluetoothAdapter.STATE_ON:
			//TODO : StartServer to listen connection
			startClassicServer(null, true);
			if (isLeSupported)
				startLeServer(null);
			break;
		}

		LE_MAP.clear();
		CLASSIC_MAP.clear();
		DISCOVERY_LIST.clear();
		
		mFunctionOperator = new FunctionOperator(mContext);
		isLeSupported = DeviceAnalyzer.getInstance(mContext).hasBluetoothLE;
	}

	private boolean isServerActivated = false;
	private Thread mServerThread = null;
	private BluetoothGattServer mGattServer;
	
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
				String name = "";
				BluetoothAdapter adapter = BLUETOOTH_MANAGER.getAdapter();
				
				BluetoothServerSocket mServerSocket;
				try {
					mServerSocket = secure? 
							adapter.listenUsingRfcommWithServiceRecord(name, uuid) :
								adapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid);
							
					while (isServerActivated) {
						BluetoothSocket mBluetoothSocket = mServerSocket.accept();
						BluetoothDeviceExtended deviceX = new BluetoothDeviceExtended(mBluetoothSocket.getRemoteDevice());
						
						ClassicLinkThread thread = new ClassicLinkThread(sInstance, mBluetoothSocket, false);
						thread.startListening();
						
						CLASSIC_MAP.put(deviceX, thread);
					}

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

			if (mServerThread != null) {
				try {
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
		
		if (mGattServer == null)
			mGattServer = BLUETOOTH_MANAGER.openGattServer(INTER_DEV_MANAGER.getContext(), mBluetoothGattServerCallback);
		
		else if (service != null)
			mGattServer.addService(service);
		
	}
	
	/**
	 * 
	 */
	public void stopLeServer() {
		if (mGattServer != null)
			mGattServer.close();
	}
	
	private boolean isLeScanning = false;
	
	/**
	 * 
	 */
	public void startDiscovery() {
		BluetoothAdapter mAdapter = BLUETOOTH_MANAGER.getAdapter();

		if (isLeScanning || !(isLeScanning = true))
			mAdapter.stopLeScan(INTER_DEV_MANAGER);
		
		if (mAdapter.isDiscovering())
			mAdapter.cancelDiscovery();
		
		DISCOVERY_LIST.clear();
		
		mAdapter.startLeScan(INTER_DEV_MANAGER);	
		mAdapter.startDiscovery();
	}
	
	/**
	 * 
	 */
	public void stopDiscovery() {
		BluetoothAdapter mAdapter = BLUETOOTH_MANAGER.getAdapter();

		if (isLeScanning && (isLeScanning = false))
			mAdapter.stopLeScan(INTER_DEV_MANAGER);	
		
		if (mAdapter.isDiscovering())
			mAdapter.cancelDiscovery();
	}

	/**
	 * 
	 * @param deviceX
	 */
	void addDiscoveryDevice(BluetoothDeviceExtended deviceX) {
		DISCOVERY_LIST.add(deviceX);
	}
	
	/**
	 * 
	 * @param receivedList
	 * @return 디바이스 탐색 중일 때 true, 탐색이 끝났을 때 false.
	 */
	public boolean pushDiscoveryListUntilNow(List<BluetoothDeviceExtended> receivedList) {
		
		receivedList.clear();
		receivedList.addAll(DISCOVERY_LIST);
		
		return (isLeScanning || BLUETOOTH_MANAGER.getAdapter().isDiscovering());
	}
	
	/**
	 * 
	 * @param deviceX
	 */
	public void connectToDeviceAsClient(BluetoothDeviceExtended deviceX) {
		UUID uuid = deviceX.getDevice().getUuids()[0].getUuid();
		connectToDeviceAsClient(deviceX, uuid);
		
	}
	
	/**
	 * 
	 * @param deviceX
	 * @param uuid
	 */
	public void connectToDeviceAsClient(BluetoothDeviceExtended deviceX, UUID uuid) {
		BluetoothDevice device = deviceX.getDevice();
		
		if (deviceX.isLESupported()) {
			device.connectGatt(INTER_DEV_MANAGER.getContext(), deviceX.getAutoConnect(), mBluetoothGattCallback);
			 
		} else {
			BluetoothSocket mBluetoothSocket;
			try {
				if (deviceX.getSecure()) 
					mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
				else
					mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
				mBluetoothSocket.connect();
				
				ClassicLinkThread thread = new ClassicLinkThread(this, mBluetoothSocket, true);
				thread.startListening();
		
				CLASSIC_MAP.put(deviceX, thread);
				
			} catch (IOException e) {
				InterDeviceEventListener mListener = INTER_DEV_MANAGER.getInterDeviceListener();
				if (mListener != null) 
					mListener.onDeviceConnectionFailed(deviceX);
			}
		}
	}
	
	/**
	 * 
	 * @param deviceX
	 */
	public void disconnectFromDeviceAsClient(BluetoothDeviceExtended deviceX) {
		if (deviceX.isLESupported()) {
			BluetoothGatt mGatt = LE_MAP.get(deviceX);
			mGatt.close();
			
		} else {
			ClassicLinkThread mThread = CLASSIC_MAP.get(deviceX);
			mThread.destroy();
			
			CLASSIC_MAP.remove(deviceX);
		}
	}
	
	/**
	 * 
	 * @param obj
	 */
	public void write(BluetoothDeviceExtended deviceX, String json) {
		if (deviceX.isLESupported()) {
			
			
		} else {
			CLASSIC_MAP.get(deviceX).sendMessageToDevice(json);
			
		}
	}
	
	@Override
	public void onDeviceDiscovered(BluetoothDeviceExtended deviceX) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeviceConnectionFailed(BluetoothDeviceExtended deviceX) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeviceConnected(BluetoothDeviceExtended deviceX) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeviceDisconnected(BluetoothDeviceExtended deviceX) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessageReceivedFrom(String json, BluetoothDeviceExtended deviceX) {
		mFunctionOperator.acceptJsonData(json, deviceX);
	}
	

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
			InterDeviceEventListener mListener = INTER_DEV_MANAGER.getInterDeviceListener();
			BluetoothDeviceExtended deviceX = new BluetoothDeviceExtended(gatt.getDevice());
			
			if (status == BluetoothGatt.GATT_SUCCESS) {
				switch (newState) {
				case BluetoothGatt.STATE_CONNECTED:
					LE_MAP.put(deviceX, gatt);
					
					if (mListener != null) 
						mListener.onDeviceConnected(deviceX);
					break;
					
				case BluetoothGatt.STATE_DISCONNECTED:
					LE_MAP.remove(deviceX);
					
					if (mListener != null) 
						mListener.onDeviceDisconnected(deviceX);
					break;
				}
				
			} else {
				switch (newState) {
				case BluetoothGatt.STATE_CONNECTED:
				case BluetoothGatt.STATE_CONNECTING:
					if (mListener != null)
						mListener.onDeviceConnectionFailed(deviceX);
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
		public void onConnectionStateChange(BluetoothDevice device, int status,
				int newState) {
			// TODO Auto-generated method stub
			super.onConnectionStateChange(device, status, newState);
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
		public void onExecuteWrite(BluetoothDevice device, int requestId,
				boolean execute) {
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
