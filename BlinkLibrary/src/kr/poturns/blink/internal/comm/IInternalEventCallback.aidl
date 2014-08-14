package kr.poturns.blink.internal.comm;

import kr.poturns.blink.internal.comm.BluetoothDeviceExtended;

/**
 *
 * @author Yeonho.Kim
 * @since 2014.08.05
 *
 */
interface IInternalEventCallback {

	oneway void onDeviceDiscovered(inout BluetoothDeviceExtended deviceX);
	
	oneway void onDeviceConnected(inout BluetoothDeviceExtended deviceX);
	
	oneway void onDeviceDisconnected(inout BluetoothDeviceExtended deviceX);

	oneway void onDeviceConnectionFailed(inout BluetoothDeviceExtended deviceX);
}