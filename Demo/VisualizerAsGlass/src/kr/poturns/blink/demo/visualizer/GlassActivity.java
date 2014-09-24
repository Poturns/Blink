package kr.poturns.blink.demo.visualizer;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.demo.visualizer.map.SupportMapActivity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class GlassActivity extends SupportMapActivity {
	
	private BlinkServiceInteraction mInteraction;
	
	private ImageView mHeartbeatImageView;
	private TextView mHeartbeatTextView;
	private ListView mAlertList;
	private GlassAlertAdapter mAlertAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 상위 액티비티에서 setContentView() 수행. 
		
		initiateComponent();
		
		IInternalEventCallback.Stub mIInternalEventCallback = new IInternalEventCallback.Stub() {
		      
		      @Override
		      public void onReceiveData(int arg0, CallbackData arg1) throws RemoteException {
		    	  if (mAlertAdapter == null)
		    		  return;
		    	  
		    	  // Data 받음..
		    	  String data = arg1.InDeviceData == null? arg1.OutDeviceData : (arg1.InDeviceData + arg1.OutDeviceData);
		    	  mAlertAdapter.pushNewMessage(data);
		    	  
		    	  onHeartbeat(100);
		    	  
		    	  Log.i("Glass", "onReceiveData");
		    	  Toast.makeText(GlassActivity.this, data, Toast.LENGTH_SHORT).show();
		      }
		   }; 
		
		mInteraction = new BlinkServiceInteraction(this, null, mIInternalEventCallback) {
			
			@Override
			public void onServiceFailed() {
				Toast.makeText(getApplicationContext(), "Failed...", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onServiceDisconnected() {
				Toast.makeText(getApplicationContext(), "Disconnected...", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onServiceConnected(IInternalOperationSupport iSupport) {
				Log.i("Glass", "onServiceConnected");
				Toast.makeText(getApplicationContext(), "Binder Service Connected!", Toast.LENGTH_SHORT).show();

				BlinkAppInfo mBlinkAppInfo = mInteraction.obtainBlinkApp();
				
				if(!mBlinkAppInfo.isExist){
					//TODO: Type은 추가할 수 있는 상수 타입으로.. (C에서 다른 타입명으로 정의하듯..String이지만 타입을 다르게.. )
					mBlinkAppInfo.addMeasurement("Location", "Location_Axis", "String", "Location Axis");
					//TODO: AppInfo에 Function을 등록하더라도 실제 Function을 제공하는 것에 대한 신뢰성 보장이 되지 않음..
					mBlinkAppInfo.addFunction("LightOn", "Turn On the Light", "kr.poturns.blink.demo.visualizer.action.lighton", Function.TYPE_BROADCAST);
					mInteraction.registerBlinkApp(mBlinkAppInfo);
				}
				
				if (mInteraction != null) {
					mInteraction.startBroadcastReceiver();
					
					boolean isDeviceConnected = mInteraction.isDeviceConnected();
					setControlActivityVisibility(!isDeviceConnected);
					setMapVisibility(!isDeviceConnected);
				}
			}
			
			@Override
			public void onDeviceConnected(BlinkDevice device) {
				boolean isDeviceConnected = mInteraction.isDeviceConnected();
				setControlActivityVisibility(!isDeviceConnected);
				setMapVisibility(isDeviceConnected);
			}
			
			@Override
			public void onDeviceDisconnected(BlinkDevice device) {
				boolean isDeviceConnected = mInteraction.isDeviceConnected();
				setControlActivityVisibility(!isDeviceConnected);
				setMapVisibility(isDeviceConnected);
			}
			
		};
		
		if (mInteraction != null) {
			mInteraction.startService();
		}

	}
	
	
	@Override
	protected void onResume() {
		super.onResume();

		if (mInteraction != null) {
			mInteraction.startBroadcastReceiver();
			
			boolean isDeviceConnected = mInteraction.isDeviceConnected();
			setControlActivityVisibility(!isDeviceConnected);
			setMapVisibility(!isDeviceConnected);
		}
		
		// TEST
		mAlertAdapter.pushNewMessage("HELLO");
	}
	
	@Override
	protected void onPause() {
		if (mInteraction != null)
			mInteraction.stopBroadcastReceiver();
		
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		if (mInteraction != null)
			mInteraction.stopService();
		
		super.onDestroy();
	}

	private void initiateComponent() {

		
		Button mControllerBtn = (Button) findViewById(R.id.glass_btn_controller);
		mControllerBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mInteraction != null)
					mInteraction.openControlActivity();
			}
		});
		
		mAlertList = (ListView) findViewById(R.id.glass_alertlist);
		mAlertAdapter = new GlassAlertAdapter(this);
		mAlertList.setAdapter(mAlertAdapter);
		mAlertList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onHeartbeat(100);
			}
		});
		
		mHeartbeatImageView = (ImageView) findViewById(R.id.heartbeat_image);
		mHeartbeatTextView = (TextView) findViewById(R.id.heartbeat_figure);
	}
	
	private void setControlActivityVisibility(boolean enabled) {
		Button mControllerBtn = (Button) findViewById(R.id.glass_btn_controller);
		mControllerBtn.setVisibility(enabled? View.VISIBLE : View.INVISIBLE);
		mControllerBtn.setClickable(enabled);
	}
	
	private void onHeartbeat(int heartbeat) {
		mHeartbeatImageView.setImageResource(R.drawable.heartbeat2);
		mHeartbeatImageView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mHeartbeatImageView.setImageResource(R.drawable.heartbeat1);
			}
		}, 1000);
		
		mHeartbeatTextView.setText(String.valueOf(heartbeat));
		mHeartbeatTextView.postDelayed(new Runnable(){
			@Override
			public void run() {
				mHeartbeatTextView.setText(null);
			}
		}, 1000);
	}
}
