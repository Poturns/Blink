package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

class PrivateUtil {
	/**
	 * 구동중인 장비의 화면의 크기가 작은 크기인지의 여부를 반환한다. <br>
	 * 
	 * @return {@link Configuration#SCREENLAYOUT_SIZE_NORMAL} 또는
	 *         {@link Configuration#SCREENLAYOUT_SIZE_SMALL} 인 경우 <b>true</b> <br>
	 *         아닐경우 <b>false</b>
	 */
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

	/** {@link Measurement}의 {@link Measurement#Measurement}에서 이름만 얻어온다. */
	public static String obtainSplitMeasurementSchema(Measurement measurement) {
		String name = measurement.Measurement;
		String[] parsed = name.split("/");
		if (parsed.length > 1)
			name = parsed[1];
		return name;
	}

	/** {@link App}의 {@link App#AppIcon}을 나타내는 {@link Drawable}객체를 얻는다. */
	public static Drawable obtainAppIcon(App app, Resources resources) {
		Drawable drawable = null;
		if (app.AppIcon != null) {
			try {
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inSampleSize = 2;
				Bitmap bitmap = BitmapFactory.decodeByteArray(app.AppIcon, 0,
						app.AppIcon.length, opt);
				drawable = new BitmapDrawable(resources, bitmap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (drawable == null) {
			drawable = resources.getDrawableForDensity(
					R.drawable.ic_action_android,
					resources.getDisplayMetrics().densityDpi * 96);
		}

		return drawable;
	}
}
