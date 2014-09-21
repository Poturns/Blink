package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
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
import android.os.Bundle;

/** external package 내부에서 사용 될 Util Class */
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

	/**
	 * {@link App}의 {@link App#AppIcon}을 나타내는 {@link Drawable}객체를 얻는다.
	 * 
	 * @param app
	 *            Icon을 가져올 App
	 * @param resources
	 *            display metric 정보를 가져올 resources
	 * @return App Icon이 존재하면 해당 App Icon을 나타내는 Drawable,<br>
	 *         없거나 가져오는데 실패하면 {@link R.drawable#res_blink_ic_action_android}을 가져온다.
	 */
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
			int density = (int) resources.getDisplayMetrics().density;
			drawable = resources.getDrawableForDensity(
					R.drawable.res_blink_ic_action_android, density * 96);
		}

		return drawable;
	}

	/*
	 * Bundle을 통해 전달되어 올 가능성이 있는 데이터를 나타내는 이름, bundle에 이러한 이름의 데이터가 존재한다면, 이
	 * 데이터와 관련된 사항을 우선적으로 보여주어야 한다.
	 */
	/** Device를 나타내는 Extra name */
	public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
	/** App을 나타내는 Extra name */
	public static final String EXTRA_DEVICE_APP = "EXTRA_DEVICE_APP";
	/** Measurement를 나타내는 Extra name */
	public static final String EXTRA_DEVICE_MEASUREMENT = "EXTRA_MEASUREMENT";

	/**
	 * 주어진 {@link Device}와 {@link App}을 {@link Bundle}에 저장한다.
	 * 
	 * @param device
	 *            Bundle에 저장할 Device
	 * @param app
	 *            Bundle에 저장할 App
	 * @return Device와 App이 저장되어있는 Bundle<br>
	 * <br>
	 *         * 각각 저장된 이름은 {@link PrivateUtil#EXTRA_DEVICE},
	 *         {@link PrivateUtil#EXTRA_DEVICE_APP}이다.
	 */
	public static Bundle toBundle(Device device, App app) {
		if (device == null)
			return null;
		Bundle bundle = new Bundle();
		bundle.putParcelable(EXTRA_DEVICE, device);
		if (app != null)
			bundle.putParcelable(EXTRA_DEVICE_APP, app);
		return bundle;
	}

	/**
	 * 주어진 {@link Device}와 {@link App}, {@link Measurement}를 {@link Bundle}에
	 * 저장한다.
	 * 
	 * @param device
	 *            Bundle에 저장할 Device
	 * @param app
	 *            Bundle에 저장할 App
	 * @param measurement
	 *            Bundle에 저장할 Measurement
	 * @return Device와 App, Measurement가 저장되어있는 Bundle<br>
	 * <br>
	 *         * 각각 저장된 이름은 {@link PrivateUtil#EXTRA_DEVICE},
	 *         {@link PrivateUtil#EXTRA_DEVICE_APP},
	 *         {@link PrivateUtil#EXTRA_DEVICE_MEASUREMENT}이다
	 * */
	public static Bundle toBundle(Device device, App app,
			Measurement measurement) {
		Bundle bundle = toBundle(device, app);
		if (bundle != null)
			bundle.putParcelable(EXTRA_DEVICE_MEASUREMENT, measurement);
		return bundle;
	}

	/**
	 * 주어진 Bundle에서 Device를 가져온다.
	 * 
	 * @param bundle
	 *            Device 정보를 가져올 Bundle
	 * @return Bundle안에 Device가 저장되어 있다면 해당 Device객체, 없으면 null
	 */
	public static Device obtainDevice(Bundle bundle) {
		if (bundle == null)
			return null;
		return bundle.getParcelable(EXTRA_DEVICE);
	}

	/**
	 * 주어진 Bundle에서 App을 가져온다.
	 * 
	 * @param bundle
	 *            App 정보를 가져올 Bundle
	 * @return Bundle안에 App이 저장되어 있다면 해당 App객체, 없으면 null
	 */
	public static App obtainApp(Bundle bundle) {
		if (bundle == null)
			return null;
		return bundle.getParcelable(EXTRA_DEVICE_APP);
	}

	/**
	 * 주어진 Bundle에서 Measurement를 가져온다.
	 * 
	 * @param bundle
	 *            Measurement 정보를 가져올 Bundle
	 * @return Bundle안에 Measurement가 저장되어 있다면 해당 Measurement객체, 없으면 null
	 */
	public static Measurement obtainMeasurement(Bundle bundle) {
		if (bundle == null)
			return null;
		return bundle.getParcelable(EXTRA_DEVICE_MEASUREMENT);
	}
}
