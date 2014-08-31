package kr.poturns.blink.external;

import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.Configuration;

class PrivateUtil {
	public static boolean isScreenSizeSmall(Context context) {
		int sizeInfoMasked = context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
		switch (sizeInfoMasked) {
		case Configuration.SCREENLAYOUT_SIZE_NORMAL:
		case Configuration.SCREENLAYOUT_SIZE_SMALL:
			return true;
		default:
			return false;
		}
	}

	/** 현재 장비를 나타내는 BlinkDevice를 얻는다. */
	public static BlinkDevice obtainHostDevice() {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		final String address = adapter.getAddress();
		BlinkDevice device;
		device = BlinkDevice.load(address);
		if (device.getName() == null || device.getName().length() < 1)
			device.setName(adapter.getName());
		return device;
	}

	public static String obtainSplitMeasurementSchema(Measurement measurement) {
		String name = measurement.Measurement;
		String[] parsed = name.split("/");
		if (parsed.length > 1)
			name = parsed[1];
		return name;
	}
}
