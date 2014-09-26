package kr.poturns.blink.internal.comm;

import kr.poturns.blink.db.archive.CallbackData;

/**
 * 외부 디바이스로 부터 데이터를 받았을 때 호출되는 콜백을 정의하는 인터페이스
 * @author Yeonho.Kim
 * @since 2014.08.05
 *
 */
interface IInternalEventCallback {
	/**
	 * 외부 디바이스로 부터 데이터를 받았을 때 호출되는 콜백
	 * <br>
	 * <br>
	 * <b>*</b> 이 콜백이 실행되는 Thread가 UI Thread가 아닐수도 있다.
	 * @param responseCode
	 *            : 어플리케이션으로부터 받은 requestCode와 동일한 값으로 어떤 요청인지 구분하기 위한 값
	 * @param data
	 *            : 외부 디바이스로부터 온 데이터
	 */
	oneway void onReceiveData(int responseCode, inout CallbackData data);
}
