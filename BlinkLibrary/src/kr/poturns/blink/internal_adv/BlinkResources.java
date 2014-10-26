package kr.poturns.blink.internal_adv;

import java.util.concurrent.ConcurrentHashMap;

import android.bluetooth.BluetoothAdapter;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.10.26
 *
 */
public class BlinkResources {

	private static ConcurrentHashMap<String, BlinkDevice> DeviceCache;
	static {
		DeviceCache = new ConcurrentHashMap<String, BlinkDevice>();
	}
	
	public static BlinkDevice loadDevice(String address) {
		if (!BluetoothAdapter.checkBluetoothAddress(address))
			return null;
		
		if (DeviceCache.containsKey(address))
			return DeviceCache.get(address);
		
		else {
			BlinkDevice device = null;
			
			
			return device; 
		}
	}
	

	/******************************************************************
    	CONSTRUCTORS
	 ******************************************************************/
	public BlinkResources() {
		// TODO Auto-generated constructor stub
	}
	
	
	

	/******************************************************************
    	INNER CLASSES
	 ******************************************************************/
	/**
	 * BlinkResources({@link BlinkDevice}, {@link BlinkAccount}, {@link BlinkChannel}, {@link BlinkPlayer})
	 * 등에서 발생할 수 있는 예외 클래스.
	 * 
	 * @author Yeonho.Kim
	 * @since 2014.10.26
	 *
	 */
	public static abstract class BlinkResourceException extends Exception {
		/*-----------------------------------------------------------------
	    	CONSTANTS
		 -----------------------------------------------------------------*/
		private static final long serialVersionUID = -5552741045516201230L;
		
		
		/*-----------------------------------------------------------------
	    	FIELDS
		 -----------------------------------------------------------------*/
		protected int ExceptionCode;

		
		/*-----------------------------------------------------------------
	    	CONSTRUCTORS
		 -----------------------------------------------------------------*/
		public BlinkResourceException(int code) {
			this.ExceptionCode = code;
		}
		

		/*-----------------------------------------------------------------
	    	OVERRIDES
		 -----------------------------------------------------------------*/
		@Override
		public abstract String getMessage();
	}
}
