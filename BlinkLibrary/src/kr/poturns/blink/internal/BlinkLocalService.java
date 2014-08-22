package kr.poturns.blink.internal;

import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.internal.comm.BlinkSupportBinder;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author Yeonho.Kim
 *
 */
public final class BlinkLocalService extends BlinkLocalBaseService {

	public static final String INTENT_ACTION_NAME = "kr.poturns.blink.internal.BlinkLocalService";
	
	private final String tag = "BlinkLocalService";
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(tag, "onCreate");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		super.onBind(intent);
		
		BlinkSupportBinder binder = new BlinkSupportBinder(this);
		BINDER_MAP.put(intent.getStringExtra(INTENT_EXTRA_SOURCE_PACKAGE), binder);
		return binder.asBinder();
	}

}
