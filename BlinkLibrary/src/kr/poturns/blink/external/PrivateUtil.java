package kr.poturns.blink.external;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashSet;
import java.util.Set;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

/** external package 내부에서 사용 될 Util Class */
class PrivateUtil {
	public enum DeviceType {
		HANDHELD_PHONE, HANDHELD_TABLET, WAREABLE_WATCH
	}

	/**
	 * UI를 구동중인 기기 형태를 나타낸다. <br>
	 * <b>임의로 값을 설정하면 안된다.</b>
	 */
	static DeviceType DEVICE_TYPE = null;

	public static DeviceType checkDeviceType(Context context) {
		if (DEVICE_TYPE == null) {
			if (context.getPackageManager().hasSystemFeature(
					"android.hardware.type.watch")) {
				DEVICE_TYPE = DeviceType.WAREABLE_WATCH;
			} else if (!isScreenSizeSmall(context)) {
				DEVICE_TYPE = DeviceType.HANDHELD_TABLET;
			} else {
				DEVICE_TYPE = DeviceType.HANDHELD_PHONE;
			}
		}
		return DEVICE_TYPE;
	}

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
	 *         없거나 가져오는데 실패하면 {@link R.drawable#res_blink_ic_action_android}을
	 *         가져온다.
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

	/**
	 * BlinkDevice의 BluetoothClass 타입을 얻는다.
	 * 
	 * @param device
	 * @return device의 BluetoothClass Major Type
	 * @see BluetoothClass.Device.Major
	 * @see BluetoothClass#getMajorDeviceClass()
	 */
	public static int getBlinkDeviceType(BlinkDevice device) {
		return BluetoothAdapter.getDefaultAdapter()
				.getRemoteDevice(device.getAddress()).getBluetoothClass()
				.getMajorDeviceClass();
	}

	/**
	 * BlinkDevice의 BluetoothClass 타입에 따른 drawable resource를 얻는다.
	 * 
	 * @param device
	 * @return device의 BluetoothClass Major Type 에 해당하는 drawable
	 * @see BluetoothClass.Device.Major
	 * @see BluetoothClass#getMajorDeviceClass()
	 */
	public static int getBlinkDeviceTypeIcon(BlinkDevice device) {
		switch (getBlinkDeviceType(device)) {
		case BluetoothClass.Device.Major.COMPUTER:
			return R.drawable.res_blink_ic_action_hardware_computer;
		case BluetoothClass.Device.Major.PHONE:
			return R.drawable.res_blink_ic_action_hardware_phone_android;
		case BluetoothClass.Device.Major.WEARABLE:
			return R.drawable.res_blink_ic_action_hardware_watch;
		default:
			return R.drawable.res_blink_ic_action_device_access_bluetooth;
		}
	}

	/*
	 * Bundle을 통해 전달되어 올 가능성이 있는 데이터를 나타내는 이름, bundle에 이러한 이름의 데이터가 존재한다면, 이
	 * 데이터와 관련된 사항을 우선적으로 보여주어야 한다.
	 */
	public static final class Bundles {
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

	public static final class IO {
		static final String FILE_FAVOR_SET = "favor_set";

		/** 즐겨찾기 목록을 가져온다. */
		public static final Set<String> getFavoriteSet(Context context) {
			try {
				Set<String> favorSet = readFromFile(context, FILE_FAVOR_SET);
				if (favorSet == null) {
					favorSet = new HashSet<String>();
				}
				return favorSet;
			} catch (Exception e) {
				e.printStackTrace();
				return new HashSet<String>();
			}
		}

		/** 즐겨찾기 목록을 저장한다. */
		public static final boolean saveFavoriteSet(Context context,
				Set<String> favoriteSet) {
			try {
				return saveToFile(context, FILE_FAVOR_SET, favoriteSet);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		/**
		 * 주어진 이름의 파일을 읽어온다.
		 * 
		 * @return 파일이 존재하고, 성공적으로 읽어왔을 경우 : 해당 객체 <br>
		 *         파일이 없거나 예외가 발생할 경우 : null
		 * @throws IOException
		 * @throws StreamCorruptedException
		 * @throws ClassNotFoundException
		 */
		@SuppressWarnings("unchecked")
		public static <T> T readFromFile(Context context, String fileName)
				throws StreamCorruptedException, IOException,
				ClassNotFoundException {
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			ObjectInputStream ois = null;
			T object;
			try {
				fis = context.openFileInput(fileName);
				bis = new BufferedInputStream(fis);
				ois = new ObjectInputStream(bis);
				object = (T) ois.readObject();
			} finally {
				closeStream(ois);
				closeStream(bis);
				closeStream(fis);
			}
			return object;
		}

		/**
		 * 주어진 이름으로 파일을 저장한다.
		 * 
		 * @param mode
		 *            Context 클래스의 mode 변수
		 * @param obj
		 *            저장할 객체
		 * @return 성공 여부
		 * @throws IOException
		 */
		public static boolean saveToFile(Context context, String fileName,
				Object obj) throws IOException {
			boolean state = false;
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			ObjectOutputStream oos = null;
			try {
				fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
				bos = new BufferedOutputStream(fos);
				oos = new ObjectOutputStream(bos);
				oos.writeObject(obj);
				state = true;
			} finally {
				closeStream(oos);
				closeStream(bos);
				closeStream(fos);
			}
			return state;
		}

		private static void closeStream(Closeable close) {
			if (close != null) {
				try {
					close.close();
				} catch (IOException e) {
				}
			}
		}
	}

}

/** ViewPager 코드 단축용 */
abstract class ViewPagerFragmentDelegate {
	protected TabHost mTabHost;
	protected ViewPager mViewPager;

	abstract protected String[] getTitles();

	abstract protected Activity getActivity();

	abstract protected FragmentManager getFragmentManager();

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(
				R.layout.res_blink_view_tab_with_viewpager, container, false);
		mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);
		mTabHost.setup();
		TabHost.TabContentFactory factory = new TabHost.TabContentFactory() {

			@Override
			public View createTabContent(String tag) {
				View v = new View(getActivity());
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				v.setTag(tag);
				return v;
			}
		};
		final String[] pageTitles = getTitles();

		int i = 0;
		for (String title : pageTitles) {
			mTabHost.addTab(mTabHost.newTabSpec(String.valueOf(i++))
					.setIndicator(title).setContent(factory));
		}

		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				navigateTab(Integer.valueOf(tabId), false);
			}
		});
		mViewPager = (ViewPager) v.findViewById(R.id.res_blink_viewpager);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						navigateTab(position, true);
					}
				});
		mViewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {

			@Override
			public int getCount() {
				return pageTitles.length;
			}

			@Override
			public Fragment getItem(int position) {
				return getViewPagerPage(position);
			}
		});
		return v;
	}

	/** ViewPager를 한 페이지를 구성하는 Fragment를 얻는다. */
	abstract protected Fragment getViewPagerPage(int position);

	/**
	 * {@link ViewPager}또는 {@link TabHost}의 page를 이동한다.
	 * 
	 * @param position
	 *            움직일 page또는 tab의 인덱스
	 * @param isFromPager
	 *            이동 요청이 {@link ViewPager}로 부터 왔는지 여부
	 */
	protected void navigateTab(int position, boolean isFromPager) {
		if (isFromPager) {
			mTabHost.setCurrentTab(position);
		} else {
			mViewPager.setCurrentItem(position);
		}
	}

	public void onDestroyView() {
		mTabHost = null;
		mViewPager = null;
	}
}
