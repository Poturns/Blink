package kr.poturns.blink.demo.healthmanager;
import java.util.ArrayList;

import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.schema.DefaultSchema;
import kr.poturns.blink.schema.Heart;
import android.app.Application;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


public class HealthManagerApplication extends Application {
	
	private BlinkServiceInteraction mBlinkServiceInteraction;
	@Override
	public void onCreate() {
		Log.i("Demo", "HealthManagerApplication onCreate");
		super.onCreate();
		
		mBlinkServiceInteraction = new BlinkServiceInteraction(this, null, mIInternalEventCallback);
		mBlinkServiceInteraction.startService();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		Log.i("Demo", "HealthManagerApplication onTerminate");
		mBlinkServiceInteraction.stopService();
		super.onTerminate();
	}
	public BlinkServiceInteraction getmBlinkServiceInteraction() {
		return mBlinkServiceInteraction;
	}

	public void setmBlinkServiceInteraction(BlinkServiceInteraction mBlinkServiceInteraction) {
		this.mBlinkServiceInteraction = mBlinkServiceInteraction;
	};
	
	IInternalEventCallback mIInternalEventCallback = new IInternalEventCallback() {

		@Override
        public IBinder asBinder() {
	        // TODO Auto-generated method stub
	        return null;
        }

		@Override
        public void onReceiveData(int arg0, CallbackData arg1)
                throws RemoteException {
	        // TODO Auto-generated method stub
	        
        }

	};
}
