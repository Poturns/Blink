package kr.poturns.blink.internal;

import kr.poturns.blink.internal.comm.BlinkDevice;
import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.07.17
 *
 */
public class DeviceAnalyzer {
	
	// *** CONSTANT DECLARATION *** //
	/**
	 * 디바이스의 역할 코드.
	 * 
	 * @author Yeonho.Kim
	 *
	 */
	public enum Identity {
		UNKNOWN,
		PERIPHERALS,
		CORE,
		PROXY,
		MAIN
	}

	private static final int IDENTITY_POINTLINE_USER = 1024 * 1024 * 32;
	public static final int IDENTITY_POINTLINE_MAIN = 1024 * 1024 * 16;
	public static final int IDENTITY_POINTLINE_PROXY = 1024 * 1024 * 8;
	private static final int IDENTITY_POINTLINE_CORE = 1024 * 1024 * 1;

	private static final int IDENTITY_POINTLINE_ETHERNET = 1024 * 256;
	private static final int IDENTITY_POINTLINE_MOBILE = 1024 * 64;
	private static final int IDENTITY_POINTLINE_WIFIDIRECT = 1024 * 2;
	private static final int IDENTITY_POINTLINE_WIFI = 1024 * 1;
	
	//private static final int IDENTITY_POINTLINE_STORAGE = 64;
	private static final int IDENTITY_POINTLINE_BLUETOOTH_LE = 2;
	private static final int IDENTITY_POINTLINE_BLUETOOTH_CLASSIC = 1;
	private static final int IDENTITY_POINTLINE_NONE = 0;
	
	
	
	// *** STATIC DECLARATION *** //

	private static DeviceAnalyzer sInstance = null;
	
	public static DeviceAnalyzer getInstance(BlinkLocalBaseService service) {
		if (sInstance == null)
			sInstance = new DeviceAnalyzer(service);
		return sInstance;
	}
	
	
	// *** FIELD DECLARATION *** //
	private final BlinkLocalBaseService ANALYZER_CONTEXT;
	
	private DeviceAnalyzer(BlinkLocalBaseService context) {
		this.ANALYZER_CONTEXT = context;
		
		int mIdentityPoint = analyze();
		Identity mIdentity = calculate(mIdentityPoint);
		
		if (BlinkDevice.HOST != null) {
			BlinkDevice.HOST.setIdentityPoint(mIdentityPoint);
			BlinkDevice.HOST.setIdentity(mIdentity.ordinal());
			//BlinkDevice.removeDeviceCache(BlinkDevice.HOST.getAddress());
		}
	}
	
	/**
	 * Device Features 및 상태를 분석한다.
	 * 
	 * @return
	 */
	int analyze() {
		ConnectivityManager mConnManager = (ConnectivityManager) ANALYZER_CONTEXT.getSystemService(Context.CONNECTIVITY_SERVICE);
		PackageManager mPackageManager = ANALYZER_CONTEXT.getPackageManager();
		
		// TODO : TESTING LOG
		for (FeatureInfo feature : mPackageManager.getSystemAvailableFeatures()) { 
			Log.i("DeviceAnalyzer_Features", feature.name);
		}
		for (NetworkInfo network : mConnManager.getAllNetworkInfo()) { 
			Log.i("DeviceAnalyzer_Features", network.getTypeName());
		}
		
		
		int mIdentityPoint = IDENTITY_POINTLINE_NONE;

		// - Check Bluetooth Support.
		if (mConnManager.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH) != null)
			mIdentityPoint |= IDENTITY_POINTLINE_BLUETOOTH_CLASSIC;
			
		// - Check Bluetooth LE Support.
		if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
			mIdentityPoint |= IDENTITY_POINTLINE_BLUETOOTH_LE;
			
		
		if (mIdentityPoint != IDENTITY_POINTLINE_NONE) {
			
			// - Check Wifi Support.
			if (mConnManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null)
				mIdentityPoint |= IDENTITY_POINTLINE_WIFI;
				
			// - Check Wifi-Direct Support.
			if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT))
				mIdentityPoint |= IDENTITY_POINTLINE_WIFIDIRECT;

			// - Check Mobile Support.
			if (mConnManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null)
				mIdentityPoint |= IDENTITY_POINTLINE_MOBILE;

			// - Check Ethernet Support.
			if (mConnManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET) != null)
				mIdentityPoint |= IDENTITY_POINTLINE_ETHERNET;
				
		}
		
		return mIdentityPoint;
	}
	
	/**
	 * 현 IdentityPoint를 시스템에 적용한다.
	 * @return
	 */
	Identity calculate(int identityPoint) {
		Identity mIdentity;
		
		if (identityPoint > IDENTITY_POINTLINE_MAIN)
			mIdentity = Identity.MAIN;
		
		else if (identityPoint > IDENTITY_POINTLINE_PROXY)
			mIdentity = Identity.PROXY;
		
		else if (identityPoint >= IDENTITY_POINTLINE_CORE)
			mIdentity = Identity.CORE;
		
		else if (identityPoint > IDENTITY_POINTLINE_NONE) 
			mIdentity = Identity.PERIPHERALS;
		
		else
			mIdentity = Identity.UNKNOWN;
			
		return mIdentity;
	}
	
	
	/**
	 * 
	 * @param enable
	 */
	synchronized boolean grantMainIdentityFromUser(boolean enable) {
		if (BlinkDevice.HOST != null) {
			
			int mIdentityPoint = BlinkDevice.HOST.getIdentityPoint();
			mIdentityPoint = enable? 
					(mIdentityPoint | IDENTITY_POINTLINE_USER) 
					: (mIdentityPoint ^ IDENTITY_POINTLINE_USER);
			
			BlinkDevice.HOST.setIdentityPoint(mIdentityPoint);
			BlinkDevice.HOST.setIdentity(calculate(mIdentityPoint).ordinal());
			
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param enable
	 * @return
	 */
	synchronized boolean grantMainIdentity(boolean enable) {
		if (BlinkDevice.HOST != null) {
			
			int mIdentityPoint = BlinkDevice.HOST.getIdentityPoint();
			if (mIdentityPoint >= IDENTITY_POINTLINE_CORE) {
				mIdentityPoint = enable? 
						(mIdentityPoint | IDENTITY_POINTLINE_MAIN ^ IDENTITY_POINTLINE_PROXY) 
						: (mIdentityPoint ^ IDENTITY_POINTLINE_MAIN);
				
				BlinkDevice.HOST.setIdentityPoint(mIdentityPoint);
				BlinkDevice.HOST.setIdentity(calculate(mIdentityPoint).ordinal());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param enable
	 * @return
	 */
	synchronized boolean grantProxyIdentity(boolean enable) {
		if (BlinkDevice.HOST != null) {
			
			int mIdentityPoint = BlinkDevice.HOST.getIdentityPoint();
			if (mIdentityPoint >= IDENTITY_POINTLINE_CORE && mIdentityPoint < IDENTITY_POINTLINE_MAIN) {
				mIdentityPoint = enable? 
						(mIdentityPoint | IDENTITY_POINTLINE_PROXY) 
						: (mIdentityPoint ^ IDENTITY_POINTLINE_PROXY);
				
				BlinkDevice.HOST.setIdentityPoint(mIdentityPoint);
				BlinkDevice.HOST.setIdentity(calculate(mIdentityPoint).ordinal());
				return true;
			}
		}
		return false;
	}
	
	
	int compareIdentity(BlinkDevice device1, BlinkDevice device2) {
		
		return 0;
	}
	
//	/**
//	 * 
//	 * @param identity
//	 */
//	synchronized void grantIdentity(Identity identity, boolean enable) {
//		switch (identity) {
//		case MAIN:
//			mIdentityPoint |= IDENTITY_POINTLINE_MAIN;
//			mIdentityPoint ^= IDENTITY_POINTLINE_PROXY;
//			break;
//			
//		case PROXY:
//			mIdentityPoint ^= IDENTITY_POINTLINE_MAIN;
//			mIdentityPoint |= IDENTITY_POINTLINE_PROXY;
//			break;
//			
//		default:
//		}
//		
//		mIdentity = identity;
//		
//		if (BlinkDevice.HOST != null) {
//			BlinkDevice.HOST.setIdentity(mIdentity.ordinal());
//			BlinkDevice.HOST.setIdentityPoint(mIdentityPoint);
//			
//			if (BlinkDevice.HOST.getGroupID() == 0 && mIdentityPoint > IDENTITY_POINTLINE_PROXY)
//				BlinkDevice.HOST.setGroupID(generateGroupId());
//			
//			Intent intent = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_IDENTITY_CHANGED);
//			intent.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) BlinkDevice.HOST);
//			intent.putExtra(IBlinkEventBroadcast.EXTRA_IDENTITY, identity);
//			ANALYZER_CONTEXT.sendBroadcast(intent);
//		}
//	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isAvailableBluetoothLE() {
		return isAvailable(IDENTITY_POINTLINE_BLUETOOTH_LE);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isAvailableWifiDirect() {
		return isAvailable(IDENTITY_POINTLINE_WIFIDIRECT);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isAvailableInternet() {
		int internetFlag = IDENTITY_POINTLINE_WIFI | IDENTITY_POINTLINE_MOBILE | IDENTITY_POINTLINE_ETHERNET;
		
		if (BlinkDevice.HOST != null) {
			return (BlinkDevice.HOST.getIdentityPoint() & internetFlag) != IDENTITY_POINTLINE_NONE;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	private int generateGroupId() {
		if (BlinkDevice.HOST == null)
			return 0;
		
		String strID = BlinkDevice.HOST.getAddress() + "@" + System.currentTimeMillis();
		return strID.toUpperCase().hashCode();
	}

	/**
	 * 
	 * @param pointLine
	 * @return
	 */
	private boolean isAvailable(int pointLine) {
		if (BlinkDevice.HOST != null) 
			return (BlinkDevice.HOST.getIdentityPoint() & pointLine) == pointLine;
		return false;
	}

}
