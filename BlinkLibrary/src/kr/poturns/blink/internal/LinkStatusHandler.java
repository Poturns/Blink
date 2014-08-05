package kr.poturns.blink.internal;

import android.os.Binder;

/**
 * 블루투스 디바이스들이 연결된 상태 현황을 관리하고 모니터링한다.
 * 
 * @author YeonHo.Kim
 * @since 2014.07.17
 *
 */
public class LinkStatusHandler {

	// *** STATIC DECLARATION *** //
	public static LinkStatusHandler sInstance = null;
	
	/**
	 * 
	 * @param manager
	 * @return
	 */
	public static LinkStatusHandler getInstance(InterDeviceManager manager) {
		if (sInstance == null)
			sInstance = new LinkStatusHandler(manager);
		return sInstance;
	}
	

	// *** FIELD DECLARATION *** //
	private final InterDeviceManager INTER_DEV_MANAGER;
	private DeviceAnalyzer.Identity mIdentity;
	
	public LinkStatusHandler(InterDeviceManager manager) {
		INTER_DEV_MANAGER = manager;
		mBinder = new BlinkLocalBinder();
		
		reset();
	}
	
	public void reset() {
		
		
	}
	
	private BlinkLocalBinder mBinder;
	
	public class BlinkLocalBinder extends Binder {
		
		public LinkStatusHandler getLinkStatusHandler() {
			return LinkStatusHandler.this;
		}
	}

	public BlinkLocalBinder getBinder() {
		return mBinder;
	}

}
