package kr.poturns.blink.internal.comm;

/**BlinkLibrary에 사용되는 메세시 프로토콜 중 기본 TYPE을 정의함.
 * 각 종류마다 REQUEST/REPONSE/FAIL TYPE으로 나뉜다.
 * 
 * @author Yeonho.Kim
 * @author Ho Kwon
 * @since 2014.08.28
 *
 */
public interface IBlinkMessagable {
	
	public static final int TYPE_REQUEST_FUNCTION = 0x1;
	public static final int TYPE_RESPONSE_FUNCTION_SUCCESS = 0x2;
	public static final int TYPE_RESPONSE_FUNCTION_FAIL = 0x3;
	
	public static final int TYPE_REQUEST_MEASUREMENTDATA = 0x4;
	public static final int TYPE_RESPONSE_MEASUREMENTDATA_SUCCESS = 0x5;
	public static final int TYPE_RESPONSE_MEASUREMENTDATA_FAIL = 0x6;
	
	public static final int TYPE_REQUEST_BlinkAppInfo_SYNC = 0x7;
	public static final int TYPE_RESPONSE_BlinkAppInfo_SYNC_SUCCESS = 0x8;
	public static final int TYPE_RESPONSE_BlinkAppInfo_SYNC_FAIL = 0x9;
	
	public static final int TYPE_REQUEST_MEASUREMENTDATA_SYNC = 0x10;
	public static final int TYPE_RESPONSE_MEASUREMENTDATA_SYNC_SUCCESS = 0x11;
	public static final int TYPE_RESPONSE_MEASUREMENTDATA_SYNC_FAIL = 0x12;
	
	public static final int TYPE_ACCEPT_CONNECTION = 0x20;
	
	static final int TYPE_REQUEST_IDENTITY_SYNC = 2009920011;
	static final int TYPE_RESPONSE_IDENTITY_SUCCESS = 2009920005;
	static final int TYPE_RESPONSE_IDENTITY_SYNC_FAIL = 2009920000;
	
	static final int TYPE_REQUEST_NETWORK_SYNC = 2009920012;
	
	static final int TYPE_REQUEST_REDIRECT_CONNECTION =2009920015;
	
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
