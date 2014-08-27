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
	 * <p>발생하는 현 Broadcast의 Intent에는  다음의 Extra 값이 첨부된다.
	 * <br>{@link #EXTRA_DEVICE} : {@link BlinkDevice} (Serializable)
	 */
	public static final String BROADCAST_DEVICE_DISCOVERED = "kr.poturns.blink.internal.event.device_discovered";
	
	/**
	 * 
	 * <p>발생하는 현 Broadcast의 Intent에는  다음의 Extra 값이 첨부된다.
	 * <br>{@link #EXTRA_DEVICE} : {@link BlinkDevice} (Serializable)
	 */
	public static final String BROADCAST_DEVICE_CONNECTED = "kr.poturns.blink.internal.event.device_connected";
	
	/**
	 * 
	 * <p>발생하는 현 Broadcast의 Intent에는  다음의 Extra 값이 첨부된다.
	 * <br>{@link #EXTRA_DEVICE} : {@link BlinkDevice} (Serializable)
	 */
	public static final String BROADCAST_DEVICE_DISCONNECTED = "kr.poturns.blink.internal.event.device_disconnected";
	
	/**
	 * 
	 * <p>발생하는 현 Broadcast의 Intent에는  다음의 Extra 값이 첨부된다.
	 * <br>{@link #EXTRA_DEVICE} : {@link BlinkDevice} (Serializable)
	 */
	public static final String BROADCAST_DEVICE_CONNECTION_FAILED = "kr.poturns.blink.internal.event.device_connection_failed";
	
	/**
	 * 
	 * <p>발생하는 현 Broadcast의 Intent에는  다음의 Extra 값이 첨부된다.
	 * <br>{@link #EXTRA_DEVICE} : {@link BlinkDevice} (Serializable)
	 * <br>{@link #EXTRA_IDENTITY} : {@link kr.poturns.blink.internal.DeviceAnalyzer.Identity} (Serializable) 
	 */
	public static final String BROADCAST_DEVICE_IDENTITY_CHANGED = "kr.poturns.blink.internal.event.device_identity_changed";
	
	/**
	 * 서비스에 환경설정 값 변경을 요청하는 Broadcast.
	 */
	public static final String BROADCAST_REQUEST_CONFIGURATION_CHANGE = "kr.poturns.blink.internal.request.configuration_change"; 
	
	/**
	 * 
	 */
	public static final String BROADCAST_CONFIGURATION_CHANGED = "kr.poturns.blink.internal.event.configuration_changed";
	
	
	
	
	
	
	// *** EXTRAs *** //
	/**
	 * Broadcast Intent에 블루투스 디바이스의 정보가 담긴 Extra Key. (Serializable)
	 * 
	 * @see {@link BlinkDevice}
	 */
	public static final String EXTRA_DEVICE = "kr.poturns.blink.internal.extra.device";
	
	/**
	 * 
	 * 
	 * @see #BROADCAST_DEVICE_IDENTITY_CHANGED
	 */
	public static final String EXTRA_IDENTITY = "kr.poturns.blink.internal.extra.identity";
	
	
	
	
	
	
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
