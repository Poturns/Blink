package kr.poturns.blink.demo.healthmanager;

import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {

	boolean bindService = false;
	BlinkServiceInteraction mBlinkServiceInteraction;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		mBlinkServiceInteraction = new BlinkServiceInteraction(this, null, mIInternalEventCallback);
		mBlinkServiceInteraction.startService();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button_setting:
			mBlinkServiceInteraction.openControlActivity();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		mBlinkServiceInteraction.stopService();
		super.onDestroy();
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
