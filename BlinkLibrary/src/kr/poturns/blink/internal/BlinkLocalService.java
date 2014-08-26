package kr.poturns.blink.internal;

import kr.poturns.blink.R;
import kr.poturns.blink.external.ServiceControlActivity;
import kr.poturns.blink.internal.comm.BlinkSupportBinder;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;

/**
 * 
 * @author Yeonho.Kim
 *
 */
public final class BlinkLocalService extends BlinkLocalBaseService {

	private static final String NAME = "BlinkLocalService";
	
	public static final String INTENT_ACTION_NAME = "kr.poturns.blink.internal.BlinkLocalService";
	
	public static final int NOTIFICATION_ID = 0x2009920;
	
	public final RemoteCallbackList<IInternalEventCallback> EVENT_CALLBACK_LIST = new RemoteCallbackList<IInternalEventCallback>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		initiate();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		String packageName = intent.getStringExtra(INTENT_EXTRA_SOURCE_PACKAGE);
		if (packageName == null)
			return null;
		
		ServiceKeeper mServiceKeeper = ServiceKeeper.getInstance(this);
		try {
			BlinkSupportBinder mBinder = mServiceKeeper.obtainBinder(packageName);
			if (mBinder == null) {
				mBinder = new BlinkSupportBinder(this);
				mServiceKeeper.registerBinder(packageName, mBinder);
			}
			
			return mBinder.asBinder();
			
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		String packageName = intent.getStringExtra(INTENT_EXTRA_SOURCE_PACKAGE);
		
		return ServiceKeeper.getInstance(this).releaseBinder(packageName);
	}
	
	/**
	 * 
	 */
	private void initiate() {

		PendingIntent mPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, 
				new Intent(this, ServiceControlActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
		
		Notification mBlinkNotification = new Notification.Builder(this)
										.setSmallIcon(R.drawable.ic_launcher)
										.setContentTitle(NAME)
										.setContentText("Running Blink-Service")
										.setContentIntent(mPendingIntent)
										.build();
		
		startForeground(NOTIFICATION_ID, mBlinkNotification);
	}
}
