package com.example.servicetestapp;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;

import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.schema.Eye;
import kr.poturns.blink.service.BlinkServiceListener;
import kr.poturns.blink.service.BlinkServiceManager;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends Activity {
	private final String tag = "MainActivity";
	BlinkServiceManager mBlinkServiceManager = null;
	TestArchive mTestArchive = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mBlinkServiceManager = new BlinkServiceManager(this,new BlinkServiceListener(){

			@Override
			public void onServiceConnected() {
				mTestArchive.run();
			}

			@Override
			public void onServiceDisconnected() {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		mTestArchive = new TestArchive(mBlinkServiceManager);
		mBlinkServiceManager.connectService();
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
//		mBlinkServiceManager.closeService();
		super.onDestroy();
	}
}
