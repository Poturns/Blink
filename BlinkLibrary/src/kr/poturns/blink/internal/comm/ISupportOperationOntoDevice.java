package kr.poturns.blink.internal.comm;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014. 08. 01
 *
 */
public interface ISupportOperationOntoDevice {

	public void startDiscovery();
	
	public void stopDiscovery();
	
	public void connectDevice(BluetoothDeviceExtended deviceX);
	
	public void disconnectDevice(BluetoothDeviceExtended deviceX);
	
	public void startService();
	
	public void stopService();
}
