package kr.poturns.blink.internal;

import java.io.Serializable;

import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.IBlinkEventBroadcast;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;

/**
 * 자신의 디바이스를 분석하여, {@link Identity}를 도출하는 모듈.
 * <br>Identity 관련 계산을 수행하는 메소드들을 제공한다.
 * 
 * @author Yeonho.Kim
 * @since 2014.07.17
 */
public class DeviceAnalyzer {
	
	// *** CONSTANT DECLARATION *** //
	/**
	 * 디바이스의 역할 코드.
	 * 
	 * <p>{@link #UNKNOWN} : 디바이스에 대한 Identity를 파악할 수 없거나, Blink 기능을 수행하기에 부적합.
	 * <br>{@link #PERIHERAL} : 주변부 디바이스. PROXY는 가능하지만, MAIN의 역할을 수행할 수 없다.
	 * <br>{@link #CORE} : 일반 디바이스. PROXY & MAIN의 역할을 모두 수행할 수 있다.
	 * <br>{@link #PROXY} : 현재 네트워크 그룹에서 임시적으로 중심부 역할을 대리한다.
	 * <br>{@link #MAIN} : 현재 네트워크 그룹에서 중심부 역할을 담당한다. 
	 * 
	 * @author Yeonho.Kim 
	 *
	 */
	public enum Identity {
		/**
		 * 
		 */
		UNKNOWN,
		/**
		 * 
		 */
		PERIPHERAL,
		/**
		 * 
		 */
		CORE,
		/**
		 * <b>[ CENTER ]</b> 
		 * <br>: 네트워크 그룹에서의 중심부. 연결된 각 디바이스간의 데이터를 중개하고, 연결해준다.
		 * 
		 * <p>
		 */
		PROXY,
		/**
		 * <b>[ CENTER ]</b> 
		 * <br>: 네트워크 그룹에서의 중심부. 연결된 각 디바이스간의 데이터를 중개하고, 연결해준다.
		 * 
		 * <p>
		 * 서버와의 연결 작업을 수행한다.
		 */
		MAIN
	}

	private static final int IDENTITY_POINTLINE_USER = 1024 * 1024 * 32;
	static final int IDENTITY_POINTLINE_MAIN = 1024 * 1024 * 16;
	static final int IDENTITY_POINTLINE_PROXY = 1024 * 1024 * 8;
	static final int IDENTITY_POINTLINE_CORE = 1024 * 1;

	private static final int IDENTITY_POINTLINE_MOBILE = 1024 * 128;
	private static final int IDENTITY_POINTLINE_WIFIDIRECT = 1024 * 4;
	private static final int IDENTITY_POINTLINE_WIFI = 1024 * 2;
	private static final int IDENTITY_POINTLINE_ETHERNET = 1024 * 1;
	
	//private static final int IDENTITY_POINTLINE_STORAGE = 64;
	private static final int IDENTITY_POINTLINE_BLUETOOTH_LE = 2;
	private static final int IDENTITY_POINTLINE_BLUETOOTH_CLASSIC = 1;
	private static final int IDENTITY_POINTLINE_NONE = 0;
	
	
	
	// *** STATIC DECLARATION *** //
	/**
	 * Singleton Instance.
	 */
	private static DeviceAnalyzer sInstance = null;
	
	/**
	 * Singleton Instance를 반환한다.
	 * 
	 * @param service
	 * @return
	 */
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
	 * 자신의 디바이스 Feature를 분석하여, IdentityPoint를 도출한다.
	 * 
	 * @return
	 */
	int analyze() {
		ConnectivityManager mConnManager = (ConnectivityManager) ANALYZER_CONTEXT.getSystemService(Context.CONNECTIVITY_SERVICE);
		PackageManager mPackageManager = ANALYZER_CONTEXT.getPackageManager();
		
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
	 * IdentityPoint를 계산하여 {@link Identity}를 결정한다.
	 * 
	 * @see {@link Identity}
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
			mIdentity = Identity.PERIPHERAL;
		
		else
			mIdentity = Identity.UNKNOWN;
			
		return mIdentity;
	}
	
	
	/**
	 * User로 부터 현 디바이스의 MAIN Identity를 설정한다.
<<<<<<< HEAD
	 * <p>※ Identity.CORE 이상부터 MAIN Identity가 될 수 있다.
	 * 
=======
>>>>>>> branch 'service' of https://github.com/Poturns/Blink.git
	 * @param enable
	 */
	synchronized boolean grantMainIdentityFromUser(boolean enable) {
		if (BlinkDevice.HOST != null) {
			
			int mIdentityPoint = BlinkDevice.HOST.getIdentityPoint();
			if (mIdentityPoint < IDENTITY_POINTLINE_CORE)
				return false;
			
			mIdentityPoint = enable? 
					(mIdentityPoint | IDENTITY_POINTLINE_USER) 
					: (mIdentityPoint ^ IDENTITY_POINTLINE_USER);
					
			Identity mIdentity = calculate(mIdentityPoint);
			BlinkDevice.HOST.setIdentityPoint(mIdentityPoint);
			BlinkDevice.HOST.setIdentity(mIdentity.ordinal());

			broadcastIdentityChanged(BlinkDevice.HOST, mIdentity);
			return true;
		}
		return false;
	}
	
	/**
	 * 현 디바이스의 MAIN Identity를 설정한다.
	 * <p>※ Identity.CORE 이상부터 MAIN Identity가 될 수 있다.
	 * 
	 * @param enable
	 * @return
	 */
	synchronized boolean grantMainIdentity(boolean enable) {
		if (BlinkDevice.HOST != null) {
			
			int mIdentityPoint = BlinkDevice.HOST.getIdentityPoint();
			if (mIdentityPoint < IDENTITY_POINTLINE_CORE)
				return false;
			
			if (mIdentityPoint >= IDENTITY_POINTLINE_CORE) {
				mIdentityPoint = enable? 
						(mIdentityPoint | IDENTITY_POINTLINE_MAIN ^ IDENTITY_POINTLINE_PROXY) 
						: (mIdentityPoint ^ IDENTITY_POINTLINE_MAIN);
				
				Identity mIdentity = calculate(mIdentityPoint);
				BlinkDevice.HOST.setIdentityPoint(mIdentityPoint);
				BlinkDevice.HOST.setIdentity(mIdentity.ordinal());

				broadcastIdentityChanged(BlinkDevice.HOST, mIdentity);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 현 디바이스의 PROXY Identity를 설정한다.
	 * <p>※ Identity.MAIN은 Proxy Identity가 될 수 없다.
	 * 
	 * @param enable
	 * @return
	 */
	synchronized boolean grantProxyIdentity(boolean enable) {
		if (BlinkDevice.HOST != null) {
			
			int mIdentityPoint = BlinkDevice.HOST.getIdentityPoint();
			if (mIdentityPoint > IDENTITY_POINTLINE_MAIN)
				return false;
			
			mIdentityPoint = enable? 
					(mIdentityPoint | IDENTITY_POINTLINE_PROXY) 
					: (mIdentityPoint ^ IDENTITY_POINTLINE_PROXY);
				
			Identity mIdentity = calculate(mIdentityPoint);
			BlinkDevice.HOST.setIdentityPoint(mIdentityPoint);
			BlinkDevice.HOST.setIdentity(mIdentity.ordinal());
			
			broadcastIdentityChanged(BlinkDevice.HOST, mIdentity);
			return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * 결과값은 device1 - device2에 해당하는 차이 값이다. 
	 * 따라서 결과값이 양수일 경우 device1이 더 크고, 음수일 경우 device2가 더 크다. 
	 * 
	 * @param device1
	 * @param device2
	 * @return
	 */
	int compareForIdentity(BlinkDevice device1, BlinkDevice device2) {
		int mIdentityPointDev1 = device1.getIdentityPoint() ^ IDENTITY_POINTLINE_PROXY;
		int mIdentityPointDev2 = device2.getIdentityPoint() ^ IDENTITY_POINTLINE_PROXY;
		
		if (mIdentityPointDev1 != mIdentityPointDev2) {
			if (mIdentityPointDev1 < mIdentityPointDev2) {
				
			}
			
			return mIdentityPointDev1 - mIdentityPointDev2;
			
		} else {
			long mTimestampDev1 = device1.getTimestamp();
			long mTimestampDev2 = device2.getTimestamp();
			
			if (mTimestampDev1 < mTimestampDev2) {
				
			}
			
			return (int)(mTimestampDev1 - mTimestampDev2);
		}
	}
	
	/**
	 * Identity가 변경되었음을 현 디바이스에게 알린다.
	 * 
	 * @param device
	 * @param identity
	 */
	public void broadcastIdentityChanged(BlinkDevice device, Identity identity) {
		Intent intent = new Intent(IBlinkEventBroadcast.BROADCAST_DEVICE_IDENTITY_CHANGED);
		intent.putExtra(IBlinkEventBroadcast.EXTRA_DEVICE, (Serializable) device);
		intent.putExtra(IBlinkEventBroadcast.EXTRA_IDENTITY, identity);
		ANALYZER_CONTEXT.sendBroadcast(intent);
	}

	/**
	 * 현 디바이스가 Bluetooth Low-Energy를 지원하는지 여부를 반환한다.
	 * @return
	 */
	public boolean isAvailableBluetoothLE() {
		return isAvailable(IDENTITY_POINTLINE_BLUETOOTH_LE);
	}

	/**
	 * 현 디바이스가 WiFi-Direct를 지원하는지 여부를 반환한다.
	 * 
	 * @return
	 */
	public boolean isAvailableWifiDirect() {
		return isAvailable(IDENTITY_POINTLINE_WIFIDIRECT);
	}
	
	/**
	 * 현 디바이스가 Internet 연결을 지원하는지 여부를 반환한다.
	 * 
	 * @return
	 */
	public boolean isAvailableInternet() {
		int internetFlag = IDENTITY_POINTLINE_WIFI | IDENTITY_POINTLINE_MOBILE | IDENTITY_POINTLINE_ETHERNET;
		
		if (BlinkDevice.HOST != null)
			return (BlinkDevice.HOST.getIdentityPoint() & internetFlag) != IDENTITY_POINTLINE_NONE;
		return false;
	}
	
	/**
	 * 현 디바이스가 해당 Feature를 지원하는지 여부를 반환한다.
	 * 해당 Feature에 해당하는 IDENTITY_POINTLINE을 매개변수로 전달한다.
	 *
	 * @see {@link BlinkDevice}
	 * @param pointLine
 	 * @return
	 */
	private boolean isAvailable(int pointLine) {
		if (BlinkDevice.HOST != null) 
			return (BlinkDevice.HOST.getIdentityPoint() & pointLine) == pointLine;
		return false;
	}
	
	/**
	 * 네트워크 그룹의 ID를 생성한다. 
	 * 
	 * @return
	 */
	int generateGroupId() {
		if (BlinkDevice.HOST == null)
			return 0;
		
		String strID = BlinkDevice.HOST.getAddress() + "@" + System.currentTimeMillis();
		return strID.toUpperCase().hashCode();
	}

}
