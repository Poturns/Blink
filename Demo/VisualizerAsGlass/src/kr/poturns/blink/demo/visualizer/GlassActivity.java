package kr.poturns.blink.demo.visualizer;

import java.io.File;

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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GlassActivity extends SupportMapActivity {
	public static String ACTION_LIGHT_ON = "kr.poturns.blink.demo.visualizer.action.lighton";
	public static String ACTION_LIGHT_OFF = "kr.poturns.blink.demo.visualizer.action.lightoff";
	public static String ACTION_TAKE_PICTURE = "kr.poturns.blink.demo.visualizer.action.takepicture";

	private BlinkServiceInteraction mInteraction;
	private IInternalOperationSupport mSupport;

	private ImageView mHeartbeatImageView;
	private TextView mHeartbeatTextView;
	private ListView mAlertList;
	private GlassAlertAdapter mAlertAdapter;
	private GlassSurfaceView mGlassSurfaceView;
	private Button mControllerBtn;
	private Handler mHandler;
	private AlphaAnimation mAlphaAnimation;

	private boolean isEmergency = false;
	private boolean isMapOpened = false;

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
				Toast.makeText(getApplicationContext(), "Blink 서비스와 연결되었습니다.",
						Toast.LENGTH_SHORT).show();

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
							ACTION_LIGHT_ON, Function.TYPE_BROADCAST);
					mBlinkAppInfo.addFunction("LightOff", "Turn On the Light",
							ACTION_LIGHT_OFF, Function.TYPE_BROADCAST);
					mBlinkAppInfo.addFunction("TakePicture", "Take Picture",
							ACTION_TAKE_PICTURE, Function.TYPE_BROADCAST);
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

	}

	@Override
	protected void onResume() {
		super.onResume();
		// TEST
		/* onHeartbeat(50); */

		boolean isDeviceConnected = mInteraction.isDeviceConnected();
		setControlActivityVisibility(!isDeviceConnected);

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

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.glass_btn_controller:
				if (mInteraction != null)
					mInteraction.openControlActivity();
				break;
				
			case R.id.map_image:
				ImageView mMapBtn = (ImageView) findViewById(R.id.map_image);
				if (isMapOpened) {
					isMapOpened = false;
					mMapBtn.setImageResource(R.drawable.ic_action_map);
				} else {
					isMapOpened = true;
					mMapBtn.setImageResource(R.drawable.ic_action_map_opened);
				}
				setMapVisibility(isMapOpened);
				break;
				
				
			case R.id.camera_rotate:
				mGlassSurfaceView.rotate();
				break;
				
			case R.id.gallery:
				File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				Uri uri = Uri.fromFile(new File(sdDir, "VisualizerAsGlass"));
		        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, uri));
		        
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/-");
				startActivity(intent);
				break;
				
			default:
				break;
			}

		}

	};

	private void initiateComponent() {
		mControllerBtn = (Button) findViewById(R.id.glass_btn_controller);
		Drawable d = getResources().getDrawable(
				R.drawable.ic_action_alert_error);
		d.setBounds(0, 0, 100, 100);

		ImageView mMapBtn = (ImageView) findViewById(R.id.map_image);
		mMapBtn.setOnClickListener(mOnClickListener);
		
		ImageView mRotateBtn = (ImageView) findViewById(R.id.camera_rotate);
		mRotateBtn.setOnClickListener(mOnClickListener);
		
		ImageView mGalleryBtn = (ImageView) findViewById(R.id.gallery);
		mGalleryBtn.setOnClickListener(mOnClickListener);
		
		mGlassSurfaceView = (GlassSurfaceView) findViewById(R.id.glass_surfaceView);
		
		mControllerBtn.setCompoundDrawables(d, null, null, null);
		mControllerBtn.setOnClickListener(mOnClickListener);
		
		mAlphaAnimation = new AlphaAnimation(1f, 0f);
		mAlphaAnimation.setDuration(750);
		mAlphaAnimation.setInterpolator(this,android.R.anim.cycle_interpolator);
		mAlphaAnimation.setRepeatCount(Animation.INFINITE);
		mAlphaAnimation.setRepeatMode(Animation.RESTART);


		mAlertList = (ListView) findViewById(R.id.glass_alertlist);
		mAlertList.setSelector(R.drawable.circle_background);
		mAlertAdapter = new GlassAlertAdapter(this);
		mAlertAdapter
				.pushNewMessage("왼쪽 상단 버튼을 통해 지도를 켤 수 있습니다.");
		mAlertAdapter
		.pushNewMessage("오른쪽 하단에 심박수가 표시됩니다.");
		mAlertList.setAdapter(mAlertAdapter);

		mHeartbeatImageView = (ImageView) findViewById(R.id.heartbeat_image);
		mHeartbeatImageView.setVisibility(View.INVISIBLE);
		mHeartbeatTextView = (TextView) findViewById(R.id.heartbeat_figure);
		mHeartbeatTextView.setVisibility(View.INVISIBLE);
		setMapVisibility(isMapOpened);

		// Light on / off broadcast receiver 등록
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_LIGHT_ON);
		filter.addAction(ACTION_LIGHT_OFF);
		filter.addAction(ACTION_TAKE_PICTURE);
		registerReceiver(mBroadcastReceiver, filter);

		mHandler = new Handler();
	}

	private void setControlActivityVisibility(boolean enabled) {
		Button mControllerBtn = (Button) findViewById(R.id.glass_btn_controller);
		mControllerBtn.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
		mControllerBtn.setClickable(enabled);
		if (enabled)
			mControllerBtn.startAnimation(mAlphaAnimation);
		else
			mControllerBtn.clearAnimation();

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
			findViewById(R.id.glass_frame).setBackground(null);
		}
	};

	private void onEmergency(int heartbeat) {
		mHandler.removeCallbacks(mRunnableOnEmergency);

		findViewById(R.id.glass_frame).setBackgroundResource(
				R.drawable.emergency_surface);

		mHandler.postDelayed(mRunnableOnEmergency, 5000);

		StringBuilder builder = new StringBuilder("경고!! ");
		builder.append(heartbeat < 80 ? "심박수가 낮습니다. "
				: heartbeat > 120 ? "심박수가 높습니다. " : "");
		builder.append("심박수 : ");
		builder.append(heartbeat);

		mAlertAdapter.pushNewMessage(builder.toString());
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String action = arg1.getAction();
			GlassSurfaceView mGlassSurfaceView = (GlassSurfaceView) findViewById(R.id.glass_surfaceView);
			
			if (action.equals(ACTION_LIGHT_ON)) {
				mGlassSurfaceView.lightOn();
				
			} else if (action.equals(ACTION_LIGHT_OFF)) {
				mGlassSurfaceView.lightOff();
				
			} else if (action.equals(ACTION_TAKE_PICTURE)) {
				mGlassSurfaceView.takePicture();
			}

		}

	};

}
