package kr.poturns.blink.external;

import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import android.os.Bundle;

class BundleResolver {
	public static Bundle toBundle(Device device, App app) {
		if (device == null)
			return null;
		Bundle bundle = new Bundle();
		bundle.putParcelable(IServiceContolActivity.EXTRA_DEVICE, device);
		if (app != null)
			bundle.putParcelable(IServiceContolActivity.EXTRA_DEVICE_APP, app);
		return bundle;
	}

	public static Device obtainDevice(Bundle bundle) {
		if (bundle == null)
			return null;
		return bundle.getParcelable(IServiceContolActivity.EXTRA_DEVICE);
	}

	public static App obtainApp(Bundle bundle) {
		if (bundle == null)
			return null;
		return bundle.getParcelable(IServiceContolActivity.EXTRA_DEVICE_APP);
	}
}
