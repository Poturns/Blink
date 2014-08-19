package kr.poturns.blink.internal;

import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.service.BlinkDatabaseServiceBinder;
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
	public SqliteManager mSqliteManager = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(tag, "onCreate");
		mSqliteManager = SqliteManager.getSqliteManager(this);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		super.onBind(intent);
		
		//return new BlinkDatabaseServiceBinder(this,mSqliteManager).asBinder();
		return InterDeviceManager.getInstance(this).InternalOperationSupporter.asBinder();
	}

}
