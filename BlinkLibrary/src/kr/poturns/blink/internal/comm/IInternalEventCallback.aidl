package kr.poturns.blink.internal.comm;

import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.BlinkLog;
import kr.poturns.blink.db.archive.Function;

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

	oneway void onReceiveMeasurementData(int responseCode,String data);
}
