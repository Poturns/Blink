package kr.poturns.blink.internal;

import java.util.List;

import kr.poturns.blink.R;
import kr.poturns.blink.db.BlinkDatabaseManager;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.db.SyncDatabaseManager;
import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.DatabaseMessage;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.external.ServiceControlActivity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkMessage;
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
	
	private SyncDatabaseManager mSyncDatabaseManager;
	public MessageProcessor mMessageProcessor;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	ServiceKeeper mServiceKeeper;
	
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
		
		mSyncDatabaseManager = new SyncDatabaseManager(this);
		mMessageProcessor = new MessageProcessor(this);
		mServiceKeeper = ServiceKeeper.getInstance(this);
		
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
		//클래스를 통한 데이터 검색일 경우
		if(mDatabaseMessage.getType()==DatabaseMessage.OBTAIN_DATA_BY_CLASS){
			try {
				Class<?> mClass = Class.forName(mDatabaseMessage.getCondition());
	            return mSyncDatabaseManager.obtainMeasurementData(mClass,mDatabaseMessage.getDateTimeFrom(), mDatabaseMessage.getDateTimeTo(), mDatabaseMessage.getContainType());
            } catch (Exception e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            } 
		}
		//ID를 통한 데이터 검색일 경우
		else if(mDatabaseMessage.getType()==DatabaseMessage.OBTAIN_DATA_BY_ID){
			List<Measurement> mMeasurementList = gson.fromJson(mDatabaseMessage.getCondition(),new TypeToken<List<Measurement>>(){}.getType());
			return gson.toJson(mSyncDatabaseManager.obtainMeasurementData(mMeasurementList,mDatabaseMessage.getDateTimeFrom(), mDatabaseMessage.getDateTimeTo()));
		} 
		//BlinkApp 동기화일 경우
		else if(mDatabaseMessage.getType()==DatabaseMessage.SYNC_BLINKAPP){
			List<BlinkAppInfo> mBlinkAppInfoList = gson.fromJson(mDatabaseMessage.getData(),new TypeToken<List<BlinkAppInfo>>(){}.getType());
			//자기 자신이 메인 디바이스일 경우
			if(BlinkDevice.HOST.getAddress().contentEquals(mServiceKeeper.obtainCurrentCenterDevice().getAddress())){
				return ""+mSyncDatabaseManager.main.syncBlinkDatabase(mBlinkAppInfoList);
			}
			//메인 디바이스가 아닐 경우
			else {
				return ""+mSyncDatabaseManager.wearable.syncBlinkDatabase(mBlinkAppInfoList);
			}
		}
		//MeasurementData 저장일 경우
		else if(mDatabaseMessage.getType()==DatabaseMessage.SYNC_MEASUREMENT){
			List<MeasurementData> mMeasurementDataList = gson.fromJson(mDatabaseMessage.getData(),new TypeToken<List<MeasurementData>>(){}.getType());
			mSyncDatabaseManager.main.insertMeasurementData(mMeasurementDataList);
			return "true";
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
	
	/**
	 * 서비스에서 Database 변경에 대한 Observer 이벤트를 받으면 관련 기능을 호출한다.
	 */
	private ContentObserver mContentObserver = new ContentObserver(new Handler()){
		public void onChange(boolean selfChange, Uri uri) {
			//새로운 BlinkApp이 추가되면 메인에 Sync 요청
			if(uri.equals(SqliteManager.URI_OBSERVER_BLINKAPP)){
				//DatabaseMessage 생성
				DatabaseMessage mDatabaseMessage = new DatabaseMessage.Builder()
				.setType(DatabaseMessage.SYNC_BLINKAPP)
				.setData(gson.toJson(mSyncDatabaseManager.obtainBlinkApp()))
				.build();
				
				//BlinkMessage 생성
				BlinkMessage mBlinkMessage = new BlinkMessage.Builder()
										.setDestinationDevice(null)
										.setDestinationApplication(null)
										.setSourceDevice(BlinkDevice.HOST)
										.setSourceApplication("kr.poturns.blink.internal.BlinkLocalService")
										.setMessage(gson.toJson(mDatabaseMessage))
										.setCode(0)
										.build();
				mMessageProcessor.sendBlinkMessageTo(mBlinkMessage, null);
			}
			//새로운 MeasruementData가 추가되면 메인에 데이터 전송
			else if(uri.equals(SqliteManager.URI_OBSERVER_MEASUREMENTDATA)){
				//DatabaseMessage 생성
				BlinkDevice CenterDevice = mServiceKeeper.obtainCurrentCenterDevice();
				List<MeasurementData> mMeasurementDataList = mSyncDatabaseManager.wearable.obtainMeasurementDatabase(CenterDevice);
				String SendData = gson.toJson(mMeasurementDataList);
				
				DatabaseMessage mDatabaseMessage = new DatabaseMessage.Builder()
				.setType(DatabaseMessage.SYNC_BLINKAPP)
				.setData(SendData)
				.build();
				
				//BlinkMessage 생성
				BlinkMessage mBlinkMessage = new BlinkMessage.Builder()
										.setDestinationDevice(null)
										.setDestinationApplication(null)
										.setSourceDevice(BlinkDevice.HOST)
										.setSourceApplication("kr.poturns.blink.internal.BlinkLocalService")
										.setMessage(gson.toJson(mDatabaseMessage))
										.setCode(0)
										.build();
				mMessageProcessor.sendBlinkMessageTo(mBlinkMessage, null);
			}
			
		};
	};
}
