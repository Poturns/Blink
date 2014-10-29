package kr.poturns.blink.demo.visualizer;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.demo.visualizer.map.SupportMapActivity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IBlinkEventBroadcast;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
	public static String ACTION_LIGHT_ON = "kr.poturns.blink.demo.visualizer.action.lighton";
	public static String ACTION_LIGHT_OFF = "kr.poturns.blink.demo.visualizer.action.lightoff";
	
	private BlinkServiceInteraction mInteraction;
	private IInternalOperationSupport mSupport;

	private ImageView mHeartbeatImageView;
	private TextView mHeartbeatTextView;
	private ListView mAlertList;
	private GlassAlertAdapter mAlertAdapter;

	private Handler mHandler;

	private boolean isEmergency = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 상위 액티비티에서 setContentView() 수행.

		initiateComponent();

		IInternalEventCallback.Stub mIInternalEventCallback = new IInternalEventCallback.Stub() {

			@Override
			public void onReceiveData(int arg0, CallbackData callbackData)
					throws RemoteException {
				// Data 받음..
				final String data = callbackData.InDeviceData == null ? callbackData.OutDeviceData
						: (callbackData.InDeviceData + callbackData.OutDeviceData);
				Log.d("onReceiveData", data);

				if (mAlertAdapter == null)
					return;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							JSONObject mJsonObj = new JSONObject(data);
							Log.d("BPM", mJsonObj.getInt("bpm") + "");
							onHeartbeat(mJsonObj.getInt("bpm"));

						} catch (Exception e) {
							;
						}
					}
				});

			}
		};

		IBlinkEventBroadcast iBlinkEventBroadcast = new IBlinkEventBroadcast() {

			@Override
			public void onDeviceDiscovered(BlinkDevice device) {
			}

			@Override
			public void onDeviceDisconnected(BlinkDevice device) {
				mAlertAdapter.pushNewMessage(device.getName()
						+ " Disonnected !");

				boolean isDeviceConnected = mInteraction.isDeviceConnected();
				setControlActivityVisibility(!isDeviceConnected);
				setMapVisibility(false);

				mHeartbeatImageView.setVisibility(View.INVISIBLE);
				mHeartbeatTextView.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onDeviceConnected(BlinkDevice device) {
				mAlertAdapter.pushNewMessage(device.getName() + " Connected !");

				boolean isDeviceConnected = mInteraction.isDeviceConnected();
				setControlActivityVisibility(!isDeviceConnected);

				mHeartbeatImageView.setVisibility(View.VISIBLE);
				mHeartbeatTextView.setVisibility(View.VISIBLE);
			}
		};

		mInteraction = new BlinkServiceInteraction(this, iBlinkEventBroadcast,
				mIInternalEventCallback) {

			@Override
			public void onServiceFailed() {
				Toast.makeText(getApplicationContext(), "Failed...",
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onServiceDisconnected() {
				Toast.makeText(getApplicationContext(), "Disconnected...",
						Toast.LENGTH_SHORT).show();
				mSupport = null;
			}

			@Override
			public void onServiceConnected(IInternalOperationSupport iSupport) {
				Log.i("Glass", "onServiceConnected");
				Toast.makeText(getApplicationContext(),
						"Binder Service Connected!", Toast.LENGTH_SHORT).show();

				mSupport = iSupport;

				BlinkAppInfo mBlinkAppInfo = mInteraction.obtainBlinkApp();

				if (!mBlinkAppInfo.isExist) {
					// TODO: Type은 추가할 수 있는 상수 타입으로.. (C에서 다른 타입명으로
					// 정의하듯..String이지만 타입을 다르게.. )
					mBlinkAppInfo.addMeasurement("Location", "Location_Axis",
							"String", "Location Axis");
					// TODO: AppInfo에 Function을 등록하더라도 실제 Function을 제공하는 것에 대한
					// 신뢰성 보장이 되지 않음..
					mBlinkAppInfo.addFunction("LightOn", "Turn On the Light",
							ACTION_LIGHT_ON,
							Function.TYPE_BROADCAST);
					mBlinkAppInfo.addFunction("LightOff", "Turn On the Light",
							ACTION_LIGHT_OFF,
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
		};

		if (mInteraction != null) {
			Log.i("Blink", "mInteraction.startService()");
			mInteraction.startService();
			mInteraction.startBroadcastReceiver();
		}

		// TEST
		/*
		 * mHeartbeatImageView.setVisibility(View.VISIBLE);
		 * mHeartbeatTextView.setVisibility(View.VISIBLE);
		 * setMapVisibility(true);
		 */
	}

	@Override
	protected void onResume() {
		super.onResume();
		// TEST
		/* onHeartbeat(50); */

		boolean isDeviceConnected = mInteraction.isDeviceConnected();
		setControlActivityVisibility(!isDeviceConnected);
		setMapVisibility(isDeviceConnected && isEmergency);

		mHeartbeatImageView.setVisibility(isDeviceConnected ? View.VISIBLE
				: View.INVISIBLE);
		mHeartbeatTextView.setVisibility(isDeviceConnected ? View.VISIBLE
				: View.INVISIBLE);

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (mInteraction != null) {
			mInteraction.stopBroadcastReceiver();
			mInteraction.stopService();
		}
		unregisterReceiver(mBroadcastReceiver);
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
		mAlertAdapter
				.pushNewMessage("You are watching ahead on wearing glasses. ");
		mAlertList.setAdapter(mAlertAdapter);

		mHeartbeatImageView = (ImageView) findViewById(R.id.heartbeat_image);
		mHeartbeatImageView.setVisibility(View.INVISIBLE);
		mHeartbeatTextView = (TextView) findViewById(R.id.heartbeat_figure);
		mHeartbeatTextView.setVisibility(View.INVISIBLE);

		
		//Light on / off broadcast receiver 등록
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_LIGHT_ON);
		filter.addAction(ACTION_LIGHT_OFF);
		registerReceiver(mBroadcastReceiver, filter);
		
		mHandler = new Handler();
	}

	private void setControlActivityVisibility(boolean enabled) {
		Button mControllerBtn = (Button) findViewById(R.id.glass_btn_controller);
		mControllerBtn.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
		mControllerBtn.setClickable(enabled);
	}

	private Runnable mRunnableOnHeartbeat = new Runnable() {
		@Override
		public void run() {
			mHeartbeatImageView.setImageResource(R.drawable.heartbeat1);
			mHeartbeatTextView.setText(null);
		}

	};

	private void onHeartbeat(int heartbeat) {
		mHandler.removeCallbacks(mRunnableOnHeartbeat);

		mHeartbeatImageView.setImageResource(R.drawable.heartbeat2);
		mHeartbeatTextView.setText(String.valueOf(heartbeat));

		mHandler.postDelayed(mRunnableOnHeartbeat, 3000);

		if (isEmergency(heartbeat))
			onEmergency(heartbeat);
	}

	private boolean isEmergency(int heartbeat) {
		return isEmergency = (heartbeat < 80 || heartbeat > 120);
	}

	private Runnable mRunnableOnEmergency = new Runnable() {
		@Override
		public void run() {
			setMapVisibility(false);
			findViewById(R.id.glass_frame).setBackground(null);
		}
	};

	private void onEmergency(int heartbeat) {
		mHandler.removeCallbacks(mRunnableOnEmergency);

		setMapVisibility(true);
		findViewById(R.id.glass_frame).setBackgroundResource(
				R.drawable.emergency_surface);

		mHandler.postDelayed(mRunnableOnEmergency, 5000);

		StringBuilder builder = new StringBuilder("Emergency!! ");
		builder.append(heartbeat < 80 ? "Low " : heartbeat > 120 ? "High " : "");
		builder.append("Heartbeat : ");
		builder.append(heartbeat);

		mAlertAdapter.pushNewMessage(builder.toString());
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
		@Override
        public void onReceive(Context arg0, Intent arg1) {
	        // TODO Auto-generated method stub
			String action = arg1.getAction();
			GlassSurfaceView mGlassSurfaceView = (GlassSurfaceView)findViewById(R.id.glass_surfaceView);
			if(action.equals(ACTION_LIGHT_ON)){
				mGlassSurfaceView.lightOn();
			}
			else if(action.equals(ACTION_LIGHT_OFF)){
				mGlassSurfaceView.lightOff();
			}
			
			
        }
		
	};

}
