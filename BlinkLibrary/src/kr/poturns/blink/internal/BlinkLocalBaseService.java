package kr.poturns.blink.internal;
import kr.poturns.blink.R;
import kr.poturns.blink.util.FileUtil;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
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
abstract class BlinkLocalBaseService extends Service /*implements InterDeviceEventListener*/ {

	protected DeviceAnalyzer.Identity mIdentity;
	protected InterDeviceManager mInterDeviceManager;
	
	
	@Override
	public void onCreate() {
		super.onCreate();

		FileUtil.createExternalDirectory();
		
		// Identity를 확인하고, 서비스가 정상적으로 동작할 수 없는 환경이면 종료한다.
		mIdentity = DeviceAnalyzer.getInstance(this).getCurrentIdentity();
		if (DeviceAnalyzer.Identity.UNKNOWN.equals(mIdentity)) {
			Toast.makeText(this, R.string.internal_baseservice_unable_alert, Toast.LENGTH_LONG).show();
			stopSelf();
			return;
		}
		
		// Device간 통신 모듈을 연결한다. 
		mInterDeviceManager = InterDeviceManager.getInstance(this);
		if (mInterDeviceManager == null) {
			
		}
		//mInterDeviceManager.setInterDeviceListener(this);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return (DeviceAnalyzer.Identity.UNKNOWN.equals(mIdentity))? 
				START_NOT_STICKY : START_STICKY;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		
	}
	
	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		
		super.onRebind(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub

		return super.onUnbind(intent);
	}

	@Override
	public void onTrimMemory(int level) {
		// TODO Auto-generated method stub

		super.onTrimMemory(level);
	}
	
	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub

		super.onLowMemory();
	}
	
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		// TODO Auto-generated method stub
		super.onTaskRemoved(rootIntent);

	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (mInterDeviceManager != null)
			mInterDeviceManager.destroy();
		
		super.onDestroy();
	}
	
}
