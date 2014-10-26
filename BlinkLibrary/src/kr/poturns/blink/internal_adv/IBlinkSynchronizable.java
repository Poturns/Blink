package kr.poturns.blink.internal_adv;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.10.26
 *
 */
public interface IBlinkSynchronizable {
	
	/**
	 * 동기화할 내용을 JSON형태의 String으로 만들어준다.
	 * 
	 * @return 
	 */
	public String obtainJsonSyncContents();
}
