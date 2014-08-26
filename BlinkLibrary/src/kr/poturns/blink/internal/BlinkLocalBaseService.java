package kr.poturns.blink.internal;
import java.util.HashMap;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkSupportBinder;
import kr.poturns.blink.util.FileUtil;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.Toast;

/**
 * Blink의 로컬 디바이스에서 베이스기능을 수행하는 백그라운드 서비스 Part.
 * 블루투스 통신으로 전달받은 데이터들을 다루는 가교역할을 한다({@link InterDeviceManager}).
 * 
 * 
 * @author Yeonho.Kim
 * @since 2014.07.12
 *
 */
abstract class BlinkLocalBaseService extends Service {

	// *** CONSTANT DECLARATION *** //
	public static final String PRIVATE_PROCESS_NAME = "kr.poturns.blink.internal.BlinkService";
	
	/**
	 * Service의 수행을 요청하는 패키지 정보 Intent String Extra. 
	 */
	public static final String INTENT_EXTRA_SOURCE_PACKAGE = "Intent.Extra.Source.Package";
	
	/**
	 * 본 디바이스의 Identity 변경을 알리는 Intent Int Extra. 
	 */
	public static final String INTENT_EXTRA_IDENTITY_CHANGE = "Intent.Extra.Identity.Change";
	
	
	// *** LIFE CYCLE DECLARATION *** //
	final HashMap<String, BlinkSupportBinder> BINDER_MAP = new HashMap<String, BlinkSupportBinder>();

	protected DeviceAnalyzer mDeviceAnalyzer;
	protected InterDeviceManager mInterDeviceManager;

	@Override
	public void onCreate() {
		Log.e("BlinkLocalBaseService", "onCreate()");
		super.onCreate();
		
		// For Service Debugging... 
		//android.os.Debug.waitForDebugger();
		
		// Blink 서비스에 필요한 기본 디렉토리 생성.
		FileUtil.createExternalDirectory();
		
		// Blink 서비스를 위한 본 디바이스 정보 파악.
		mDeviceAnalyzer = DeviceAnalyzer.getInstance(this);
		
		DeviceAnalyzer.Identity mIdentity = mDeviceAnalyzer.getCurrentIdentity();
		if (DeviceAnalyzer.Identity.UNKNOWN.equals(mIdentity)) {
			// Identity를 확인하고, 서비스가 정상적으로 동작할 수 없는 환경이면 종료한다.
			//Toast.makeText(this, R.string.internal_baseservice_unable_alert, Toast.LENGTH_LONG).show();
			stopSelf();
			return;
		}
		
		// Device간 통신 모듈을 연결한다. 
		InterDeviceManager.getInstance(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		DeviceAnalyzer.Identity identity = mDeviceAnalyzer.getCurrentIdentity();
		
//		return (DeviceAnalyzer.Identity.UNKNOWN.equals(identity))? 
//				START_NOT_STICKY : START_STICKY;
		
		if (intent != null) {
			int mChangedIdentity = intent.getIntExtra(INTENT_EXTRA_IDENTITY_CHANGE, -1);
			if (mChangedIdentity != -1) {
				
				intent.removeExtra(INTENT_EXTRA_IDENTITY_CHANGE);
			}
		}
	
		return START_REDELIVER_INTENT; 
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.e("BlinkLocalBaseService", "onConfigurationChanged()");
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		
	}
	
	@Override
	public void onTrimMemory(int level) {
		switch (level) {
		case TRIM_MEMORY_COMPLETE:
		case TRIM_MEMORY_RUNNING_LOW:
		case TRIM_MEMORY_RUNNING_CRITICAL:
			Log.e("BlinkLocalBaseService", "onTrimMemory() : CLEAR CACHE");
			BlinkDevice.clearCache();
			break;
		}
	}
	
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Log.e("BlinkLocalBaseService", "onTaskRemoved()");
		// TODO Auto-generated method stub
		super.onTaskRemoved(rootIntent);

	}
	
	@Override
	public void onDestroy() {
		Log.e("BlinkLocalBaseService", "onDestroy()");
		InterDeviceManager.getInstance(this).destroy();
	}
	
}
