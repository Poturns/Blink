package kr.poturns.blink.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class BlinkDatabaseServiceConnection implements ServiceConnection {
	BlinkDatabaseServiceManager mBlinkServiceManager;
	
	public BlinkDatabaseServiceConnection(BlinkDatabaseServiceManager mBlinkServiceManager){
		
		this.mBlinkServiceManager = mBlinkServiceManager;
	}
	
	@Override
	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		// TODO Auto-generated method stub
		mBlinkServiceManager.mBlinkDatabaseServiceBinder = IBlinkDatabaseServiceBinder.Stub.asInterface(arg1);
		try {
			mBlinkServiceManager.mBlinkDatabaseServiceBinder.registerApplicationInfo(mBlinkServiceManager.mDeviceName, mBlinkServiceManager.mPackageName,mBlinkServiceManager.mAppName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mBlinkServiceManager.mBlinkServiceListener.onServiceConnected();
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		// TODO Auto-generated method stub
		
	}

}
