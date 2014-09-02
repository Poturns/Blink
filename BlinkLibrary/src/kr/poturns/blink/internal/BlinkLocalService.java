package kr.poturns.blink.internal;

import java.util.List;

import kr.poturns.blink.R;
import kr.poturns.blink.db.BlinkDatabaseManager;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.archive.DatabaseMessage;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.external.ServiceControlActivity;
import kr.poturns.blink.internal.comm.BlinkSupportBinder;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * @author Yeonho.Kim
 *
 */
public final class BlinkLocalService extends BlinkLocalBaseService {

	private static final String NAME = "BlinkLocalService";
	
	public static final String INTENT_ACTION_NAME = "kr.poturns.blink.internal.BlinkLocalService";
	
	public static final int NOTIFICATION_ID = 0x2009920;
	
	private BlinkDatabaseManager mBlinkDatabaseManager;
	public MessageProcessor mMessageProcessor;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
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
		
		mBlinkDatabaseManager = new BlinkDatabaseManager(this);
		mMessageProcessor = new MessageProcessor(this);
		
		Notification mBlinkNotification = new Notification.Builder(this)
										.setSmallIcon(R.drawable.ic_launcher)
										.setContentTitle(NAME)
										.setContentText("Running Blink-Service")
										.setContentIntent(mPendingIntent)
										.build();
		
		startForeground(NOTIFICATION_ID, mBlinkNotification);
		getContentResolver().registerContentObserver(SqliteManager.URI_OBSERVER_BLINKAPP, false, mContentObserver);
		getContentResolver().registerContentObserver(SqliteManager.URI_OBSERVER_MEASUREMENTDATA, false, mContentObserver);
	}
	
	/**
	 * MessageProcessor로부터 받은 메시지를 처리하는 매소드
	 * @param message
	 */
	public String receiveMessageFromProcessor(String message){
		DatabaseMessage mDatabaseMessage = gson.fromJson(message, DatabaseMessage.class);
		if(mDatabaseMessage.getType()==DatabaseMessage.OBTAIN_DATA_BY_CLASS){
			try {
				Class<?> mClass = Class.forName(mDatabaseMessage.getCondition());
	            return mBlinkDatabaseManager.obtainMeasurementData(mClass,mDatabaseMessage.getDateTimeFrom(), mDatabaseMessage.getDateTimeTo(), mDatabaseMessage.getContainType());
            } catch (Exception e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            } 
		} else if(mDatabaseMessage.getType()==DatabaseMessage.OBTAIN_DATA_BY_ID){
			List<Measurement> mMeasurementList = gson.fromJson(mDatabaseMessage.getCondition(),new TypeToken<List<Measurement>>(){}.getType());
			return gson.toJson(mBlinkDatabaseManager.obtainMeasurementData(mMeasurementList,mDatabaseMessage.getDateTimeFrom(), mDatabaseMessage.getDateTimeTo()));
		}
		return null;
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
	
	private ContentObserver mContentObserver = new ContentObserver(new Handler()){
		public void onChange(boolean selfChange, Uri uri) {
			Log.i(NAME, "Uri : "+uri);
		};
	};
}
