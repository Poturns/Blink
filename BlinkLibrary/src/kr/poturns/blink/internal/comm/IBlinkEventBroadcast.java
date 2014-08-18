package kr.poturns.blink.internal.comm;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.08.19
 *
 */
public interface IBlinkEventBroadcast {

	// *** BROADCAST ACTIONs *** //
	/**
	 * 
	 */
	public static final String BROADCAST_DEVICE_DISCOVERED = "kr.poturns.blink.internal.event.device_discovered";
	
	/**
	 * 
	 */
	public static final String BROADCAST_DEVICE_CONNECTED = "kr.poturns.blink.internal.event.device_connected";
	
	/**
	 * 
	 */
	public static final String BROADCAST_DEVICE_DISCONNECTED = "kr.poturns.blink.internal.event.device_disconnected";
	
	
	
	
	// *** EXTRAs *** //
	/**
	 * Broadcast Intent에 블루투스 디바이스의 정보가 담긴 Extra Key.
	 * 
	 * @see {@link BlinkDevice}
	 */
	public static final String EXTRA_DEVICE = "kr.poturns.blink.internal.extra.device";
	
	
	
	
	// *** PERMISSIONs *** //
	/**
	 * 
	 */
	public static final String PERMISSION_LISTEN_STATE_MESSAGE = "kr.poturns.blink.permission.LISTEN_STATE_MESSAGE";
	
}
