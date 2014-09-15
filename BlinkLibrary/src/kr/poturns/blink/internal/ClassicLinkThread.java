package kr.poturns.blink.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkMessage;
import kr.poturns.blink.internal.comm.IBlinkEventBroadcast;
import kr.poturns.blink.internal.comm.IBlinkMessagable;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * 스레드를 시작할 때는 start() 대신 startThread()을 사용한다.
 * 스레드를 중지할 때는 pauseThread()를 사용한다.
 * 스레드를 종료할 때는 destroyThread()을 호출한다.
 * 
 * @author Yeonho.Kim
 * @since 2014. 08. 01
 *
 */
public class ClassicLinkThread extends Thread {

	private final InterDeviceManager INTER_DEV_MANAGER;
	private final BluetoothAssistant ASSISTANT;
	private final MessageProcessor MSG_PROCESSOR;
	private final BlinkDevice DEVICE;
	
	private boolean isClient;
	private BluetoothSocket mBluetoothSocket;
	
	private ObjectInputStream mInputStream;
	private ObjectOutputStream mOutputStream;
	
	private boolean isRunning;
	private boolean isPaused;
	
	public ClassicLinkThread(BluetoothAssistant assistant, BlinkDevice device, BluetoothSocket socket, boolean client) {
		this(assistant, device);
		this.mBluetoothSocket = socket;
		this.isClient =  client;
		
		init();
		
		// Connection Broadcasting...
		BlinkLocalBaseService mContext = INTER_DEV_MANAGER.MANAGER_CONTEXT;
		
		Intent mActionConnected = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_CONNECTED);
		mActionConnected.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) device);
		mContext.sendBroadcast(mActionConnected, IBlinkEventBroadcast.PERMISSION_LISTEN_STATE_MESSAGE);
		
		ServiceKeeper.getInstance(mContext).addConnection(device, this);
	}
	
	private ClassicLinkThread(BluetoothAssistant assistant, BlinkDevice device) {
		super(assistant.CONNECTION_GROUP, device.getName());

		ASSISTANT = assistant;
		INTER_DEV_MANAGER = ASSISTANT.INTER_DEV_MANAGER;
		MSG_PROCESSOR = new MessageProcessor(INTER_DEV_MANAGER.MANAGER_CONTEXT);
		DEVICE = device;
	}
	
	private void init() {
		Log.d("ClassicLinkThread_init()", "");
		// TODO : ObjectStream을 Reader/Writer로 바꿀 것... (JSON String 교환)
		try {
			if (isClient) {
				mOutputStream = new ObjectOutputStream(mBluetoothSocket.getOutputStream());
				mInputStream = new ObjectInputStream(mBluetoothSocket.getInputStream());
				
			} else {
				mInputStream = new ObjectInputStream(mBluetoothSocket.getInputStream());
				mOutputStream = new ObjectOutputStream(mBluetoothSocket.getOutputStream());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		isRunning = false;
	}
	
	@Override
	public void run() {
		Log.d("ClassicLinkThread_run()", "START : " + DEVICE.toString());
		
		// 연결 성립시, 상대의 디바이스로 자신의  BlinkDevice를 넣어 Identity 동기화 요청 메세지를 전송한다.
		ServiceKeeper.getInstance(INTER_DEV_MANAGER.MANAGER_CONTEXT)
						.transferSystemSync(DEVICE, IBlinkMessagable.TYPE_REQUEST_IDENTITY_SYNC);
		
		// Read Operation
		while (isRunning) {
			try {
				Object obj = mInputStream.readObject();
				
				if (obj instanceof BlinkMessage) {
					BlinkMessage msg = (BlinkMessage) obj;
					MSG_PROCESSOR.acceptBlinkMessage(msg, DEVICE);
					
					Intent intent = new Intent(IBlinkEventBroadcast.BROADCAST_MESSAGE_RECEIVED_FOR_TEST);
					intent.putExtra("content", msg.getMessage());
					INTER_DEV_MANAGER.MANAGER_CONTEXT.sendBroadcast(intent);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Log.d("ClassicLinkThread_run()", "END");
	}

	/**
	 * 이 메소드는 아무런 기능을 수행하지 않는다.
	 * <br> {@link #startThread()}로 기능을 수행한다.
	 * 
	 * @see #startThread()
	 * @see #pauseThread()
	 */
	@Override
	@Deprecated
	public synchronized void start() {}
	
	/**
	 * 본 스레드를 시작한다.
	 */
	synchronized void startThread() {
		Log.d("ClassicLinkThread_startListening()", "");
		
		if (!isRunning && (isRunning = true))
			super.start();
		
		if (isPaused && (isPaused = false))
			this.notify();
	}
	
	/**
	 * 본 스레드를 중지한다.
	 */
	synchronized void pauseThread() {
		if (isRunning && !isPaused && (isPaused = true)) {
			try {
				this.wait();
				
			} catch (InterruptedException e) {
				destroyThread();
			}
		}
	}

	/**
	 * 이 메소드는 아무런 기능을 수행하지 않는다.
	 * <br> {@link #destroyThread()}로 기능을 수행한다.
	 * 
	 * @see #destroyThread()
	 */
	@Deprecated
	@Override
	public void destroy() { }
	
	/**
	 * 본 스레드를 파괴하고, 해당 디바이스와의 연결을 해제한다.
	 */
	void destroyThread() {
		Log.d("ClassicLinkThread_destroy()", "DESTROY");
		isRunning = false;
		interrupt();
		
		try {
			if (mInputStream != null)
				mInputStream.close();
			
		} catch (IOException e) {
		} finally {
			mInputStream = null;
		}
		
		try {
			if (mOutputStream != null)
				mOutputStream.close();
			
		} catch (IOException e) {
		} finally {
			mOutputStream = null;
		}
		
		try {
			if (mBluetoothSocket != null)
				mBluetoothSocket.close();
			
		} catch (IOException e) {
		} finally {
			mBluetoothSocket = null;
		}
	}

	/**
	 * 연결되어있는 상대 디바이스에게 Object 메세지를 전송한다.
	 * 
	 * @param obj
	 */
	void sendMessageToDevice(Object obj) {
		if ((mOutputStream != null) && (obj != null)) {
			try {
				Log.d("InterDeviceManager_sendBlinkMessage()", DEVICE.getName() + " : " + ((BlinkMessage)obj).getMessage().toString());
				mOutputStream.writeObject(obj);
				mOutputStream.flush();
				
			} catch (IOException e) {
			}
		}
	}

}
