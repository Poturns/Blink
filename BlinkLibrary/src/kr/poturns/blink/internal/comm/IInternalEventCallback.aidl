package kr.poturns.blink.internal.comm;

import kr.poturns.blink.internal.comm.BlinkDevice;

/**
 *
 * @author Yeonho.Kim
 * @since 2014.08.05
 *
 */
interface IInternalEventCallback {

	oneway void onDeviceDiscovered(inout BlinkDevice device);
	
	oneway void onDeviceConnected(inout BlinkDevice device);
	
	oneway void onDeviceDisconnected(inout BlinkDevice device);

	oneway void onDeviceConnectionFailed(inout BlinkDevice device);

}
