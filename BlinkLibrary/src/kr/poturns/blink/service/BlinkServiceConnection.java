package kr.poturns.blink.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class BlinkServiceConnection implements ServiceConnection {
	BlinkServiceManager mBlinkServiceManager;
	
	public BlinkServiceConnection(BlinkServiceManager mBlinkServiceManager){
		this.mBlinkServiceManager = mBlinkServiceManager;
	}
	
	@Override
	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		// TODO Auto-generated method stub
		mBlinkServiceManager.mBlinkServiceBinder = IBlinkServiceBinder.Stub.asInterface(arg1);
		mBlinkServiceManager.mBlinkServiceListener.onServiceConnected();
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		// TODO Auto-generated method stub
		
	}

}
