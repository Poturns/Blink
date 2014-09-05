package kr.poturns.blink.internal.comm;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.08.28
 *
 */
public interface IBlinkMessagable {
	
	public static final int TYPE_REQUEST_FUNCTION = 0x1;
	public static final int TYPE_RESPONSE_FUNCTION_SUCCESS = 0x2;
	public static final int TYPE_RESPONSE_FUNCTION_FAIL = 0x3;
	
	public static final int TYPE_REQUEST_MEASUREMENT = 0x4;
	public static final int TYPE_RESPONSE_MEASUREMENT_SUCCESS = 0x5;
	public static final int TYPE_RESPONSE_MEASUREMENT_FAIL = 0x6;
	
	public static final int TYPE_REQUEST_BlinkAppInfo_SYNC = 0x7;
	public static final int TYPE_RESPONSE_BlinkAppInfo_SYNC_SUCCESS = 0x8;
	public static final int TYPE_RESPONSE_BlinkAppInfo_SYNC_FAIL = 0x9;
	
	
	static final int TYPE_REQUEST_IDENTITY_SYNC = 2009920011;
	static final int TYPE_RESPONSE_IDENTITY_SUCCESS = 2009920005;
	static final int TYPE_RESPONSE_IDENTITY_SYNC_FAIL = 2009920000;
	static final int TYPE_RESPONSE_IDENTITY_SYNC = 2009920012;
	
	
	/**
	 * Bluetooth Classic에서 사용할 수 있는 Message로 변환한다.
	 * @return
	 */
	public String toClassicMessage();
	
	/**
	 * BLE에서 사용할 수 있는 Message로 변환한다.
	 * @return
	 */
	public Object toLeMessage();
}
