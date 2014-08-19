package kr.poturns.blink.external;

import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import android.os.Bundle;

public interface IServiceContolActivity {
	/*
	 * Bundle을 통해 전달되어 올 가능성이 있는 데이터를 나타내는 이름, bundle에 이러한 이름의 데이터가 존재한다면, 이
	 * 데이터와 관련된 사항을 우선적으로 보여주어야 한다.
	 */
	/** 기기를 나타내는 Extra name */
	public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
	/** 앱을 나타내는 Extra name */
	public static final String EXTRA_DEVICE_APP = "EXTRA_DEVICE_APP";

	public static final int REQUEST_PREFERENCE = 9090;
	public static final int RESULT_SERVICE = 8473;
	public static final String RESULT_EXTRA = "RESULT_EXTRA";

	/**
	 * Fragment를 전환한다.
	 * 
	 * @param position
	 *            현재 Activity에서의 List의 번호
	 * @param arguments
	 *            추가적으로 전달할 arguments
	 * 
	 */
	public void transitFragment(int position, Bundle arguments);

	/**
	 * InternalService에 Message를 전달한다.
	 * 
	 * @param message
	 *            전달할 Message정보가 담긴 {@link Bundle}
	 * 
	 * @return 성공 여부
	 */
	public boolean sendMessageToService(Bundle message);

	public BlinkServiceInteraction getServiceInteration();
}
