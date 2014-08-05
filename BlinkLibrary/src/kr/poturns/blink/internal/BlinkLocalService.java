package kr.poturns.blink.internal;

import kr.poturns.blink.internal.comm.ISupportInternalOperation;
import android.content.Intent;
import android.os.IBinder;

/**
 * 
 * @author Yeonho.Kim
 *
 */
public final class BlinkLocalService extends BlinkLocalBaseService {
	
	public static final String INTENT_ACTION_NAME = "";
	
	protected ISupportInternalOperation.Stub mBindingStub = new ISupportInternalOperation.Stub() {
		
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		
		if (InterDeviceManager.ACTION_NAME.equals(intent.getAction())) {
			return LinkStatusHandler.getInstance(mInterDeviceManager).getBinder();
		}
		
		return mBindingStub.asBinder();
	}
}
