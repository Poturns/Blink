package kr.poturns.blink.internal.comm;

import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.CallbackData;

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

	oneway void onReceiveData(int responseCode,inout CallbackData data);
}
