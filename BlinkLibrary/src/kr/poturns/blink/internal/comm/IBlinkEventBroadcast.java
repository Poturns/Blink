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
	
	/**
	 * 
	 */
	public static final String BROADCAST_DEVICE_CONNECT_FAILED = "kr.poturns.blink.internal.event.device_connectfailed";
	
	
	
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
	

	
	
	// *** CALLBACKs *** //
	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 탐색 수행시, 디바이스가 발견되었을 때 호출된다.
	 * <hr>
	 * @param device
	 */
	public void onDeviceDiscovered(BlinkDevice device);

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 디바이스가 연결되었을 때 호출된다.
	 * <hr>
	 * @param device
	 */
	public void onDeviceConnected(BlinkDevice device);

	/**
	 * [ <b>OVERRIDE IT</b>, if you want to complement some operations. ]
	 * 
	 * <p>블루투스 디바이스가 해제되었을 때 호출된다.
	 * <hr>
	 * @param device
	 */
	public void onDeviceDisconnected(BlinkDevice device);
}
