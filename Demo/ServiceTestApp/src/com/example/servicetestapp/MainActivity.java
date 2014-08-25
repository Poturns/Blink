package com.example.servicetestapp;

import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.BlinkSupportBinder;
import kr.poturns.blink.internal.comm.IBlinkEventBroadcast;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import kr.poturns.blink.schema.Eye;
import kr.poturns.blink.util.ClassUtil;
import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity implements android.view.View.OnClickListener{
	private final String tag = "MainActivity";
	BlinkServiceInteraction mBlinkServiceInteraction = null;
	TestArchive mTestArchive = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mBlinkServiceInteraction = new BlinkServiceInteraction(this,mBlinkEventBroadcast,mInternalEventCallback){

			@Override
            public void onServiceConnected(IInternalOperationSupport iSupport) {
	            // TODO Auto-generated method stub
				mTestArchive.run();
            }

			@Override
            public void onServiceDisconnected() {
	            // TODO Auto-generated method stub
	            
            }

			@Override
            public void onServiceFailed() {
	            // TODO Auto-generated method stub
	            
            }
			
		};
		
		mTestArchive = new TestArchive(mBlinkServiceInteraction);
		mBlinkServiceInteraction.startService();
		
		
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mBlinkServiceInteraction.stopService();
		super.onDestroy();
	}
	
	public void onClick(View v){
		switch (v.getId()) {
		case R.id.btn_test:
			mBlinkServiceInteraction.startFuntion(new Function("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_ACTIVITY),BlinkSupportBinder.REQUEST_TYPE_IN_DEVICE,0);
			break;

		default:
			break;
		}
	}
	

	IBlinkEventBroadcast mBlinkEventBroadcast = new IBlinkEventBroadcast(){

		@Override
        public void onDeviceDiscovered(BlinkDevice device) {
            // TODO Auto-generated method stub
            
        }

		@Override
        public void onDeviceConnected(BlinkDevice device) {
            // TODO Auto-generated method stub
            
        }

		@Override
        public void onDeviceDisconnected(BlinkDevice device) {
            // TODO Auto-generated method stub
            
        }
		
	};
	
	IInternalEventCallback mInternalEventCallback = new IInternalEventCallback(){

		@Override
        public IBinder asBinder() {
	        // TODO Auto-generated method stub
	        return null;
        }

		@Override
        public void onDeviceConnected(BlinkDevice arg0) throws RemoteException {
	        // TODO Auto-generated method stub
	        
        }

		@Override
        public void onDeviceConnectionFailed(BlinkDevice arg0)
                throws RemoteException {
	        // TODO Auto-generated method stub
	        
        }

		@Override
        public void onDeviceDisconnected(BlinkDevice arg0)
                throws RemoteException {
	        // TODO Auto-generated method stub
	        
        }

		@Override
        public void onDeviceDiscovered(BlinkDevice arg0) throws RemoteException {
	        // TODO Auto-generated method stub
	        
        }

		@Override
        public void onReceiveMeasurementData(int arg0, String arg1)
                throws RemoteException {
	        // TODO Auto-generated method stub
	        
        }

	};

}
