package kr.poturns.blink.internal;

import kr.poturns.blink.internal.comm.BlinkSupportBinder;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.util.Log;

/**
 * 
 * @author Yeonho.Kim
 *
 */
public final class BlinkLocalService extends BlinkLocalBaseService {

	public static final String INTENT_ACTION_NAME = "kr.poturns.blink.internal.BlinkLocalService";
	
	private final String tag = "BlinkLocalService";

	final RemoteCallbackList<IInternalEventCallback> EVENT_CALLBACK_LIST = new RemoteCallbackList<IInternalEventCallback>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(tag, "onCreate");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		try {
			BlinkSupportBinder binder = new BlinkSupportBinder(this);
			BINDER_MAP.put(intent.getStringExtra(INTENT_EXTRA_SOURCE_PACKAGE), binder);
			
			return binder.asBinder();
			
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		String packageName = intent.getStringExtra(INTENT_EXTRA_SOURCE_PACKAGE);
		
		BINDER_MAP.remove(packageName);
		
		return super.onUnbind(intent);
	}
}
