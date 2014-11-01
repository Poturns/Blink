package kr.poturns.blink.internal_adv;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 
 * @author Yeonho.Kim
 * @since 2014.10.31
 *
 */
public abstract class BlinkEngine extends Service {
	
	/******************************************************************
    	LIFE CYCLE
	 ******************************************************************/
	/** */
	@Override
	public void onCreate() {
		super.onCreate();
		// TODO Auto-generated method stub
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	
	
	/******************************************************************
		OVERRIDES
	 ******************************************************************/
	/** */
	//public abstract void openEngineTuner();
}
