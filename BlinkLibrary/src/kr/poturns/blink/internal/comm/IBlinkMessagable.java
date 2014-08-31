package kr.poturns.blink.internal.comm;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.08.28
 *
 */
public interface IBlinkMessagable {
	
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
