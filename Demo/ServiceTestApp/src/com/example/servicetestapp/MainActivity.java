package com.example.servicetestapp;

import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IBlinkEventBroadcast;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Activity;
import android.os.Bundle;
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
		
		
		
		mBlinkServiceInteraction = new BlinkServiceInteraction(this,new IBlinkEventBroadcast() {
			
			@Override
			public void onDeviceDiscovered(BlinkDevice device) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDeviceDisconnected(BlinkDevice device) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDeviceConnected(BlinkDevice device) {
				// TODO Auto-generated method stub
				
			}
		},null){

			@Override
            public void onServiceConnected(IInternalOperationSupport iSupport) {
	            // TODO Auto-generated method stub
				mTestArchive.run();
            }

			@Override
            public void onServiceDisconnected() {
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
			mBlinkServiceInteraction.startFuntion(new Function("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_ACTIVITY));
			break;

		default:
			break;
		}
	}

}
