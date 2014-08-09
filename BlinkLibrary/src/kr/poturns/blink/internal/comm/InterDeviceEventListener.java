package kr.poturns.blink.internal.comm;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014. 07. 28
 *
 */
public interface InterDeviceEventListener {

	public void onDeviceDiscovered(BluetoothDeviceExtended deviceX);
	
	public void onDeviceConnectionFailed(BluetoothDeviceExtended deviceX);
	
	public void onDeviceConnected(BluetoothDeviceExtended deviceX);
	
	public void onDeviceDisconnected(BluetoothDeviceExtended deviceX);
}
