package com.example.servicetestapp;

import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.service.BlinkDatabaseServiceListener;
import kr.poturns.blink.service.BlinkDatabaseServiceManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity implements android.view.View.OnClickListener{
	private final String tag = "MainActivity";
	BlinkDatabaseServiceManager mBlinkDatabaseServiceManager = null;
	TestArchive mTestArchive = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		
		mBlinkDatabaseServiceManager = new BlinkDatabaseServiceManager(this,new BlinkDatabaseServiceListener(){

			@Override
			public void onServiceConnected() {
				mTestArchive.run();
			}

			@Override
			public void onServiceDisconnected() {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		mTestArchive = new TestArchive(mBlinkDatabaseServiceManager);
		mBlinkDatabaseServiceManager.connectService();
		
		Log.i(tag,"name : "+this.getApplicationInfo().loadLabel(this.getPackageManager()));
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mBlinkDatabaseServiceManager.closeService();
		super.onDestroy();
	}
	
	public void onClick(View v){
		switch (v.getId()) {
		case R.id.btn_test:
			mBlinkDatabaseServiceManager.startFuntion(new Function("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_ACTIVITY));
			break;

		default:
			break;
		}
	}

}
