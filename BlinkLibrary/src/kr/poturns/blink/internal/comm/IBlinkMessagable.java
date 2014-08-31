package kr.poturns.blink.internal.comm;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.08.28
 *
 */
public interface IBlinkMessagable {
	
	public static final int TYPE_REQUEST_FUNCTION = 0x1;
	
	public static final int TYPE_REQUEST_MEASUREMENT = 0x2;
	
	static final int TYPE_REQUEST_IDENTITY_SYNC = 2009920011;
	
	
	
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
