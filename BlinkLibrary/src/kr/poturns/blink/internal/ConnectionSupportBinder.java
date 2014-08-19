package kr.poturns.blink.internal;

import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkProfile;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.bluetooth.BluetoothDevice;
import android.os.RemoteException;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.08.19
 *
 */
public abstract class ConnectionSupportBinder extends IInternalOperationSupport.Stub {

	protected final BlinkLocalService CONTEXT;
	
	private BluetoothAssistant mAssistant;
	
	protected ConnectionSupportBinder(BlinkLocalService context) throws Exception {
		CONTEXT = context;
		
		if (context == null)
			throw new Exception();
		
		mAssistant = BluetoothAssistant.getInstance(InterDeviceManager.getInstance(context));
	}

	@Override
	public final boolean registerCallback(IInternalEventCallback callback) throws RemoteException {
		if (callback != null)
			return CONTEXT.EVENT_CALLBACK_LIST.register(callback);
		return false;
	}
	
	@Override
	public final boolean unregisterCallback(IInternalEventCallback callback) throws RemoteException {
		if (callback != null) 
			return CONTEXT.EVENT_CALLBACK_LIST.unregister(callback);
		return false;
	}

	@Override
	public void startDiscovery(int type) throws RemoteException {
		if (mAssistant == null) {
			return;
		}
		
		switch (InterDeviceManager.getInstance(CONTEXT).mStartDiscoveryType = type) {
		case BluetoothDevice.DEVICE_TYPE_CLASSIC:
		case BluetoothDevice.DEVICE_TYPE_DUAL:
		case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
				mAssistant.startDiscovery(BluetoothDevice.DEVICE_TYPE_CLASSIC);
			break;
			
		case BluetoothDevice.DEVICE_TYPE_LE:
			mAssistant.startDiscovery(BluetoothDevice.DEVICE_TYPE_LE);
			break;
		}
	}
	
	@Override
	public void stopDiscovery() throws RemoteException {
		if (mAssistant != null)
			mAssistant.stopDiscovery();
	}

	@Override
	public BlinkDevice[] obtainCurrentDiscoveryList() throws RemoteException {
		if (mAssistant != null)
			return mAssistant.obtainCurrentDiscoveryList();
		return null;
	}

	@Override
	public void startListeningAsServer() throws RemoteException {
		if (mAssistant != null)
			mAssistant.startListeningServer(false);
	}
	
	@Override
	public void stopListeningAsServer() throws RemoteException {
		if (mAssistant != null)
			mAssistant.stopListeningServer();
	}

	@Override
	public void connectDevice(BlinkDevice device) throws RemoteException {
		BluetoothDevice origin = device.obtainBluetoothDevice();
		
		if (origin.getBondState() == BluetoothDevice.BOND_NONE)
			origin.createBond();
			
		if (mAssistant != null)
			mAssistant.connectToDeviceAsClient(device, BlinkProfile.UUID_BLINK);
	}

	@Override
	public void disconnectDevice(BlinkDevice deviceX) throws RemoteException {
		if (mAssistant != null)
			mAssistant.disconnectFromDeviceAsClient(deviceX);
	}

	@Override
	public BlinkDevice[] obtainConnectedDeviceList() throws RemoteException {
		if (mAssistant != null)
			mAssistant.obtainConnectedDeviceList();
		return null;
	}

	@Override
	public void sendBlinkMessages(BlinkDevice target, String jsonMsg) throws RemoteException {
		if (mAssistant != null)
			mAssistant.onMessageSentTo(jsonMsg, target);
	}
	
}