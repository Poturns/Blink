package kr.poturns.blink.internal;
import kr.poturns.blink.internal.DeviceAnalyzer.Identity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.util.FileUtil;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * Blink의 로컬 디바이스에서 디바이스간 통신 기능을 베이스로 하는 백그라운드 서비스 Part.
 * 
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
	
	
	
	// *** FIELD DECLARATION *** //
	private BlinkTopView mBlinkTopView;
	
	private InterDeviceManager INTER_DEVICE_MANAGER;
	private ServiceKeeper SERVICE_KEEPER;
	
	
	
	// *** LIFE CYCLE DECLARATION *** //
	@Override
	public void onCreate() {
		Log.e("BlinkLocalBaseService", "onCreate()");
		super.onCreate();
		// For Service Debugging... 
		android.os.Debug.waitForDebugger();

		initiatate();
		
		// Blink 서비스를 위한 본 디바이스 정보 파악.
		DeviceAnalyzer.getInstance(this);
		if (BlinkDevice.HOST.getIdentity() == Identity.UNKNOWN) {
			// Identity를 확인하고, 서비스가 정상적으로 동작할 수 없는 환경이면 종료한다.
			stopSelf();		// TODO: 제대로 동작 안함...
			return;
		}
		
		// Device간 통신 모듈을 연결한다. 
		INTER_DEVICE_MANAGER = InterDeviceManager.getInstance(this);
		
		// Blink 서비스 리소스 관리 모듈을 연결한다.
		SERVICE_KEEPER = ServiceKeeper.getInstance(this);
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
		
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onTrimMemory(int level) {
		switch (level) {
		case TRIM_MEMORY_COMPLETE:
		case TRIM_MEMORY_RUNNING_LOW:
		case TRIM_MEMORY_RUNNING_CRITICAL:
			Log.e("BlinkLocalBaseService", "onTrimMemory() : CLEAR CACHE");
			BlinkDevice.clearAllCache();
			break;
			
		case TRIM_MEMORY_MODERATE:
		case TRIM_MEMORY_RUNNING_MODERATE:
			Log.e("BlinkLocalBaseService", "onTrimMemory() : REMOVE UNNECESSARY");
			BlinkDevice.removeUnnecessaryCache();
			break;
		}
	}
	
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Log.e("BlinkLocalBaseService", "onTaskRemoved()");
		
		super.onTaskRemoved(rootIntent);
	}
	
	@Override
	public void onDestroy() {
		Log.e("BlinkLocalBaseService", "onDestroy()");

		WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mWindowManager.removeView(mBlinkTopView);
		
		if (INTER_DEVICE_MANAGER != null)
			INTER_DEVICE_MANAGER.destroy();
		
		if (SERVICE_KEEPER != null)
			SERVICE_KEEPER.destroy();
	}
	
	/**
	 * 서비스에 구동에 필요한 기본 환경 요소를 초기화한다.
	 * Blink 디렉토리 생성 및 BlinkTopView 생성.
	 */
	private void initiatate() {
		
		// Blink 서비스에 필요한 기본 디렉토리 생성.
		FileUtil.createExternalDirectory();
		
		
		// Blink Top View
		mBlinkTopView = new BlinkTopView(this);
		
		WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Point mPoint = new Point();
		mWindowManager.getDefaultDisplay().getSize(mPoint);
		
		WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				mPoint.x/2,
				mPoint.y/2,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSPARENT );
		mLayoutParams.gravity = Gravity.CENTER;
		mLayoutParams.verticalMargin = 0.1f;
		mLayoutParams.horizontalMargin = 0.1f;
		
		mWindowManager.addView(mBlinkTopView, mLayoutParams);
	}
	
	/**
	 * {@link BlinkTopView}를 반환한다.
	 * 
	 * @return
	 */
	final BlinkTopView getTopView() {
		return mBlinkTopView;
	}
}
