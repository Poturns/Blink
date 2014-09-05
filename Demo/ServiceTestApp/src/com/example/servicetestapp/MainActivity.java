package com.example.servicetestapp;

import java.util.List;

import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IBlinkEventBroadcast;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import kr.poturns.blink.schema.Eye;
import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends Activity implements android.view.View.OnClickListener{
	private final String tag = "MainActivity";
	BlinkServiceInteraction mBlinkServiceInteraction = null;
	TestArchive mTestArchive = null;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mBlinkServiceInteraction = new BlinkServiceInteraction(this,mBlinkEventBroadcast,mInternalEventCallback){

			@Override
            public void onServiceConnected(IInternalOperationSupport iSupport) {
	            // TODO Auto-generated method stub
				Log.i(tag, "onServiceConnected!!");
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
		
		mTestArchive = new TestArchive(mBlinkServiceInteraction,this);
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
			mBlinkServiceInteraction.local.startFunction(new Function("TestAcitivity", "두번째 액티비티 실행","com.example.servicetestapp.TestActivity",Function.TYPE_ACTIVITY));
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
	
	/**
	 * 이클립스에서 아래의 바인더가 오버라이드되면 안된다.
	 * public IBinder asBinder()
	 */
	IInternalEventCallback mInternalEventCallback = new IInternalEventCallback.Stub(){

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
		public void onReceiveData(int arg0, CallbackData arg1)
				throws RemoteException {
			// TODO Auto-generated method stub
			Log.i(tag, "Code : "+arg0);
			Log.i(tag, "InDeviceData : "+arg1.InDeviceData);
			Log.i(tag, "OutDeviceData : "+arg1.OutDeviceData);
			Log.i(tag, "Error : "+arg1.Error);
			
			if(arg0==0){
				if(arg1.InDeviceData!=null){
					List<Eye> mEyeList = gson.fromJson(arg1.InDeviceData, new TypeToken<List<Eye>>(){}.getType());
					for(int i=0;i<mEyeList.size();i++){
						Log.i(tag, "Eye - left_sight : "+mEyeList.get(i).left_sight+" right_sight : "+mEyeList.get(i).right_sight+ " DateTime : "+mEyeList.get(i).DateTime);
					}
				}
				if(arg1.OutDeviceData!=null){
					List<Eye> mEyeList = gson.fromJson(arg1.OutDeviceData, new TypeToken<List<Eye>>(){}.getType());
					for(int i=0;i<mEyeList.size();i++){
						Log.i(tag, "Eye - left_sight : "+mEyeList.get(i).left_sight+" right_sight : "+mEyeList.get(i).right_sight+ " DateTime : "+mEyeList.get(i).DateTime);
					}
				}
			}
			else if(arg0==1){
				if(arg1.InDeviceData!=null){
					List<MeasurementData> mMeasurementList = gson.fromJson(arg1.InDeviceData, new TypeToken<List<MeasurementData>>(){}.getType());
					Log.i(tag, mMeasurementList.toString());
				}
				if(arg1.OutDeviceData!=null){
					List<MeasurementData> mMeasurementList = gson.fromJson(arg1.OutDeviceData, new TypeToken<List<MeasurementData>>(){}.getType());
					Log.i(tag, mMeasurementList.toString());
				}
			}
		}

	};

}
