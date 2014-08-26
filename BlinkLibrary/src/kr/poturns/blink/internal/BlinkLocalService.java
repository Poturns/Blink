package kr.poturns.blink.internal;

import java.util.HashMap;

import kr.poturns.blink.R;
import kr.poturns.blink.db.BlinkDatabaseManager;
import kr.poturns.blink.db.archive.Function;
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

	public static final String INTENT_ACTION_NAME = "kr.poturns.blink.internal.BlinkLocalService";
	public static final int NOTIFICATION_ID = 0x2009920;
	private final String tag = "BlinkLocalService";
	
	public final HashMap<String, RemoteCallbackList<IInternalEventCallback>> CALLBACK_MAP = new HashMap<String, RemoteCallbackList<IInternalEventCallback>>();
	private BlinkDatabaseManager mBlinkDatabaseManager;
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
		
		try {
			BlinkSupportBinder binder = new BlinkSupportBinder(this);
			BINDER_MAP.put(packageName, binder);
			return binder.asBinder();
			
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		String packageName = intent.getStringExtra(INTENT_EXTRA_SOURCE_PACKAGE);
		if (packageName == null)
			return false;
		
		CALLBACK_MAP.remove(packageName);
		return (BINDER_MAP.remove(packageName) != null);
	}
	
	private void initiate() {

		PendingIntent mPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, 
				new Intent(this, ServiceControlActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
		
		mBlinkDatabaseManager = new BlinkDatabaseManager(this);
		
		Notification mBlinkNotification = new Notification.Builder(this)
										.setSmallIcon(R.drawable.ic_launcher)
										.setContentTitle("Blink Service")
										.setContentText("Running Blink-Service")
										.setContentIntent(mPendingIntent)
										.build();
		
		startForeground(NOTIFICATION_ID, mBlinkNotification);
	}
	
	/**
	 * MessageProcessor로부터 받은 메시지를 처리하는 매소드
	 * @param message
	 */
	public String receiveMessageFromProcessor(String message){
		return null;
	}
	
	public void sendMessageToProcessor(){
		
	}
	/**
	 * 함수를 실행시켜주는 매소드
	 * 바인더나 MessageProcessor로부터 호출된다.
	 * @param function
	 */
	public void startFunction(Function function){
		if(function .Type==Function.TYPE_ACTIVITY)
			startActivity(new Intent(function.Action).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		else if(function .Type==Function.TYPE_SERIVCE)
			startService(new Intent(function.Action));
		else if(function.Type==Function.TYPE_BROADCAST)
			sendBroadcast(new Intent(function.Action));
	}
}
