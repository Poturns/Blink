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
	private final String tag = "BlinkLocalService";
	public SqliteManager mSqliteManager = null;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.i(tag, "onCreate");
		super.onCreate();
		mSqliteManager = SqliteManager.getSqliteManager(this);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new BlinkDatabaseServiceBinder(this,mSqliteManager).asBinder();
	}

}
