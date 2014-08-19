package kr.poturns.blink.internal;

import java.util.ArrayList;

import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.07.17
 *
 */
public class DeviceAnalyzer {
	
	// *** CONSTANT DECLARATION *** //
	public enum Identity {
		UNKNOWN,
		PERIPHERALS,
		CORE,
		MAIN
	}
	
	private static final int IDENTITY_POINTLINE_USER = 1024 * 1024 * 2;
	private static final int IDENTITY_POINTLINE_CORE = 1024 * 1024 * 1;
	
	private static final int IDENTITY_POINTLINE_TELEPHONY = 1024 * 64;
	private static final int IDENTITY_POINTLINE_WIFIDIRECT = 1024 * 2;
	private static final int IDENTITY_POINTLINE_WIFI = 1024 * 1;
	
	private static final int IDENTITY_POINTLINE_STORAGE = 64;
	private static final int IDENTITY_POINTLINE_BLE = 2;
	private static final int IDENTITY_POINTLINE_BT = 1;
	private static final int NONE = 0;
	
	
	
	// *** STATIC DECLARATION *** //
	private static DeviceAnalyzer sInstance = null;
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static DeviceAnalyzer getInstance(BlinkLocalBaseService context) {
		if (sInstance == null && context != null)
			sInstance = new DeviceAnalyzer(context);
		return sInstance;
	}

	public static int getIdentityPoint() {
		return (sInstance == null)? 0 : sInstance.mIdentityPoint;
	}
	
	
	// *** FIELD DECLARATION *** //
	private final BlinkLocalBaseService ANALYZER_CONTEXT;
	private final ArrayList<String> ANALYSIS_ARRAY;
	
	private Identity mIdentity;
	private int mIdentityPoint;

	private boolean hasUserSelection;
	public boolean hasBluetoothLE;
	
	public DeviceAnalyzer(BlinkLocalBaseService context) {
		this.ANALYZER_CONTEXT = context;
		this.ANALYSIS_ARRAY = new ArrayList<String>();
		
		mIdentity = Identity.UNKNOWN;
		hasUserSelection = false;
		
		analyze();
		apply(true);
	}
	
	/**
	 * Device Features 및 상태를 분석한다.
	 * 
	 * @return
	 */
	int analyze() {
		PackageManager mPackageManager = ANALYZER_CONTEXT.getPackageManager();
		
		ANALYSIS_ARRAY.clear();
		for (FeatureInfo feature : mPackageManager.getSystemAvailableFeatures()) 
			ANALYSIS_ARRAY.add(feature.name);
		
		mIdentityPoint = hasUserSelection? IDENTITY_POINTLINE_USER : 0;
		
		// - Check Bluetooth LE Support.
		if (hasBluetoothLE = ANALYSIS_ARRAY.contains(PackageManager.FEATURE_BLUETOOTH_LE))
			mIdentityPoint |= IDENTITY_POINTLINE_BLE;
		
		// - Check Telephony Support.
		if (ANALYSIS_ARRAY.contains(PackageManager.FEATURE_TELEPHONY))
			mIdentityPoint |= IDENTITY_POINTLINE_TELEPHONY;
			
		// - Check Wifi Support.
		if (ANALYSIS_ARRAY.contains(PackageManager.FEATURE_WIFI))
			mIdentityPoint |= IDENTITY_POINTLINE_WIFI;
			
		// - Check Wifi-Direct Support.
		if (ANALYSIS_ARRAY.contains(PackageManager.FEATURE_WIFI_DIRECT))
			mIdentityPoint |= IDENTITY_POINTLINE_WIFIDIRECT;
		
		// - Check Bluetooth Support.
		if (ANALYSIS_ARRAY.contains(PackageManager.FEATURE_BLUETOOTH))
			mIdentityPoint |= IDENTITY_POINTLINE_BT;
		else
			mIdentityPoint &= NONE;
		
		return mIdentityPoint;
	}
	
	/**
	 * 현 IdentityPoint를 시스템에 적용한다.
	 * @return
	 */
	public Identity apply() {
		return apply(false);
	}
	
	/**
	 * 현 IdentityPoint를 시스템에 적용한다.
	 * 
	 * @param init
	 * @return
	 */
	private Identity apply(boolean init) {
		
		// IdentityPoint가 User_또는_Core Line에 체크될 경우, CORE Identity로 설정한다.
		if (mIdentityPoint >= IDENTITY_POINTLINE_CORE)
			mIdentity = Identity.CORE;
		
		// IdentityPoint가 None일 경우, UNKNOWN Identity로 설정한다.
		else if (mIdentityPoint == NONE)
			mIdentity = Identity.UNKNOWN;
		
		else {
			// 초기상태일 때, IdentityPoint가 WIFI+STORAGE Line에 체크될 경우, CORE Identity로 설정한다.
			if (init && (mIdentityPoint > IDENTITY_POINTLINE_WIFI + IDENTITY_POINTLINE_STORAGE)) {
				mIdentity = Identity.CORE;
			
			// 그 외의 경우, PERIPHERALS Identity로 설정한다.
			} else {
				mIdentity = Identity.PERIPHERALS;
				
			}
		}
		
		/*
		 * 
		 */
		if (!init) {
			InterDeviceManager.getInstance(ANALYZER_CONTEXT);
		}
		
		return mIdentity;
	}
	
	/**
	 * 
	 * @param feature : {@link PackageManager}의 Feature 상수.
	 * @return 
	 */
	public boolean isAvailableFeature(String feature) {
		return ANALYSIS_ARRAY.contains(feature);
	}
	
	public final void setUserSelection(boolean selection) {
		mIdentityPoint = (hasUserSelection = selection)? 
				mIdentityPoint|(IDENTITY_POINTLINE_USER) : 
					mIdentityPoint^(IDENTITY_POINTLINE_USER);
	}
	
	public Identity getCurrentIdentity() {
		return mIdentity;
	}
	
	public boolean isAvailableAsCore() {
		return (mIdentity == Identity.CORE || mIdentity == Identity.MAIN);
	}
}
