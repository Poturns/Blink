package kr.poturns.blink.demo.visualizer;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.demo.visualizer.map.SupportMapActivity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;

import org.json.JSONObject;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GlassActivity extends SupportMapActivity {

	private BlinkServiceInteraction mInteraction;

	private ImageView mHeartbeatImageView;
	private TextView mHeartbeatTextView;
	private ListView mAlertList;
	private GlassAlertAdapter mAlertAdapter;
	
	private boolean isEmergency = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 상위 액티비티에서 setContentView() 수행.

		initiateComponent();

		IInternalEventCallback.Stub mIInternalEventCallback = new IInternalEventCallback.Stub() {
		      
		      @Override
		      public void onReceiveData(int arg0, CallbackData callbackData) throws RemoteException {
		    	  if (mAlertAdapter == null)
		    		  return;
		    	  
		    	  // Data 받음..
		    	  String data = callbackData.InDeviceData == null? callbackData.OutDeviceData : (callbackData.InDeviceData + callbackData.OutDeviceData);
		    	  Log.d("onReceiveData", data);
		    	  
		    	  try {
			    	  JSONObject mJsonObj = new JSONObject(data);
			    	  onHeartbeat(mJsonObj.getInt("bpm"));
			    	  
		    	  } catch (Exception e) { ; }
		      }
		   }; 
		
		mInteraction = new BlinkServiceInteraction(this, null, mIInternalEventCallback) {
			
			@Override
			public void onServiceFailed() {
				Toast.makeText(getApplicationContext(), "Failed...",
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onServiceDisconnected() {
				Toast.makeText(getApplicationContext(), "Disconnected...",
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onServiceConnected(IInternalOperationSupport iSupport) {
				Log.i("Glass", "onServiceConnected");
				Toast.makeText(getApplicationContext(),
						"Binder Service Connected!", Toast.LENGTH_SHORT).show();

				BlinkAppInfo mBlinkAppInfo = mInteraction.obtainBlinkApp();

				if (!mBlinkAppInfo.isExist) {
					// TODO: Type은 추가할 수 있는 상수 타입으로.. (C에서 다른 타입명으로
					// 정의하듯..String이지만 타입을 다르게.. )
					mBlinkAppInfo.addMeasurement("Location", "Location_Axis",
							"String", "Location Axis");
					// TODO: AppInfo에 Function을 등록하더라도 실제 Function을 제공하는 것에 대한
					// 신뢰성 보장이 되지 않음..
					mBlinkAppInfo.addFunction("LightOn", "Turn On the Light",
							"kr.poturns.blink.demo.visualizer.action.lighton",
							Function.TYPE_BROADCAST);
					mInteraction.registerBlinkApp(mBlinkAppInfo);
				}

				if (mInteraction != null) {
					mInteraction.startBroadcastReceiver();

					boolean isDeviceConnected = mInteraction
							.isDeviceConnected();
					setControlActivityVisibility(!isDeviceConnected);
				}
			}

			@Override
			public void onDeviceConnected(BlinkDevice device) {
				mAlertAdapter.pushNewMessage(device.getName() + " Connected !");
				
				boolean isDeviceConnected = mInteraction.isDeviceConnected();
				setControlActivityVisibility(!isDeviceConnected);
				
				mHeartbeatImageView.setVisibility(View.VISIBLE);
				mHeartbeatTextView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onDeviceDisconnected(BlinkDevice device) {
				mAlertAdapter.pushNewMessage(device.getName() + " Disonnected !");
				
				boolean isDeviceConnected = mInteraction.isDeviceConnected();
				setControlActivityVisibility(!isDeviceConnected);
				setMapVisibility(false);

				mHeartbeatImageView.setVisibility(View.INVISIBLE);
				mHeartbeatTextView.setVisibility(View.INVISIBLE);
			}

		};

		if (mInteraction != null) {
			mInteraction.startService();
		}
		
		// TEST
		//mHeartbeatImageView.setVisibility(View.VISIBLE);
		//mHeartbeatTextView.setVisibility(View.VISIBLE);
		//setMapVisibility(true);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mInteraction != null) {
			mInteraction.startBroadcastReceiver();

			boolean isDeviceConnected = mInteraction.isDeviceConnected();
			setControlActivityVisibility(!isDeviceConnected);
			setMapVisibility(isDeviceConnected && isEmergency);
		}

		// TEST
		//onHeartbeat(50);
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
		mAlertAdapter.pushNewMessage("Welcome to Visualizer !! ");
		mAlertAdapter.pushNewMessage("You are watching ahead on wearing glasses. ");
		mAlertList.setAdapter(mAlertAdapter);
		
		mHeartbeatImageView = (ImageView) findViewById(R.id.heartbeat_image);
		mHeartbeatImageView.setVisibility(View.INVISIBLE);
		mHeartbeatTextView = (TextView) findViewById(R.id.heartbeat_figure);
		mHeartbeatTextView.setVisibility(View.INVISIBLE);
	}

	private void setControlActivityVisibility(boolean enabled) {
		Button mControllerBtn = (Button) findViewById(R.id.glass_btn_controller);
		mControllerBtn.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
		mControllerBtn.setClickable(enabled);
	}

	private void onHeartbeat(int heartbeat) {
		mHeartbeatImageView.setImageResource(R.drawable.heartbeat2);
		mHeartbeatImageView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mHeartbeatImageView.setImageResource(R.drawable.heartbeat1);
			}
		}, 5000);
		mHeartbeatTextView.setText(String.valueOf(heartbeat));
		mHeartbeatTextView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mHeartbeatTextView.setText(null);
			}
		}, 5000);
		
		if (isEmergency(heartbeat))
			onEmergency(heartbeat);
	}

	private boolean isEmergency(int heartbeat) {
		if (isEmergency) {
			setMapVisibility(false);
			findViewById(R.id.glass_frame).setBackground(null);
		}
		
		return isEmergency = (heartbeat < 80 || heartbeat > 120);
	}
	
	private void onEmergency(int heartbeat) {
		setMapVisibility(true);
		
		findViewById(R.id.glass_frame).setBackgroundResource(R.drawable.emergency_surface);
		
		mAlertAdapter.pushNewMessage("Emergency!! Low Heartbeat : " + heartbeat);
	}
	
}
