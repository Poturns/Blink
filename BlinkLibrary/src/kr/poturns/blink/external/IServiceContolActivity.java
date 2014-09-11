package kr.poturns.blink.external;

import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.os.Bundle;

/** ServiceControlActivity가 관리하는 자원을 다른 Fragment 클래스가 참조할 수 있게 하는 Interface 클래스. */
interface IServiceContolActivity {
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

	public BlinkServiceInteraction getServiceInteration();

	public void setServiceInteration(BlinkServiceInteraction interaction);

	public void setInternalOperationSupport(
			IInternalOperationSupport blinkOperation);

	public IInternalOperationSupport getInternalOperationSupport();

	public SqliteManagerExtended getDatabaseHandler();
}
