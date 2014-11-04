package kr.poturns.blink.demo.fitnessapp;

import java.util.concurrent.atomic.AtomicBoolean;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.db.archive.CallbackData;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import kr.poturns.blink.demo.fitnesswear.R;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class FunctionTestFragment extends SwipeEventFragment implements
		IInternalEventCallback {
	TextView mCenterButton;
	boolean mButtonClickState = false, mActivated = false;
	private SensorManager mSensorManager;
	private Sensor mAccelerormeterSensor;
	/** 센서가 측정한 시간 */
	private long mSensorLastTime;
	/** 센서가 측정한 속도 */
	private float mSensorMovementSpeed;
	/** 센서가 측정한 위치 값 */
	private float mSensorLastX, mSensorLastY, mSensorLastZ;
	private AtomicBoolean mSensorMovementReturning = new AtomicBoolean();
	/** 마지막으로 사진 측정을 요청한 시간 */
	private long mTimeStamp;
	/** 센서가 움직임을 감지할 최소한의 속도 */
	private static final int SHAKE_THRESHOLD = 800;
	/** 센서가 한번 측정 후, 다시 측정하기까지 걸리는 시간 */
	private static final int SENSOR_ACTIVATE_TIME_THRESHOLD = 100;
	private static final String ACTION_TAKE_PICTURE = "action.takepicture";
	private static final String TAG = FunctionTestFragment.class
			.getSimpleName();
	private static final int TEXT_SIZE_READY = 30;
	private static final int TEXT_SIZE_START = 25;
	private static final long INTERVAL_REQUEST_SECOND = 3000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		mAccelerormeterSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_function_test, container,
				false);
		mCenterButton = (TextView) v.findViewById(android.R.id.text1);
		mCenterButton.setText("Ready");
		mCenterButton.setTextSize(TEXT_SIZE_READY);
		mCenterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mButtonClickState) {
					if (mActivated) {
						runFunctionTest();
						mActivated = false;
					}
					mButtonClickState = false;
				} else {
					mCenterButton.setTextSize(TEXT_SIZE_START);
					mCenterButton.setText("Touch\nOR\nShake");
					mCenterButton
							.setBackgroundResource(R.drawable.circle_green);
					mActivated = true;
					mButtonClickState = true;
				}
			}
		});
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		mSensorManager.registerListener(mSensorListener, mAccelerormeterSensor,
				SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	public void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(mSensorListener);
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT:
			mActivityInterface.returnToMain();
			break;
		default:
			break;
		}
		return false;
	}

	void runFunctionTest() {
		Log.d(TAG, "function test");
		if (System.currentTimeMillis() - mTimeStamp < INTERVAL_REQUEST_SECOND) {
			Toast.makeText(getActivity(), "이미 요청을 보냈습니다. 잠시후에 시도하세요",
					Toast.LENGTH_SHORT).show();
			onTestFinished();
			return;
		}
		if (mActivityInterface.getBlinkServiceInteraction() != null) {
			int count = 0;
			for (BlinkAppInfo info : mActivityInterface
					.getBlinkServiceInteraction().local.obtainBlinkAppAll()) {
				for (Function function : info.mFunctionList) {
					if (function.Action.contains(ACTION_TAKE_PICTURE)) {
						Log.d(TAG,
								"---- startFunction ----\n"
										+ function.toString()
										+ "-----------------------");
						mActivityInterface.getBlinkServiceInteraction().remote
								.startFunction(function, 0x01);
						mTimeStamp = System.currentTimeMillis();
						count++;
					}
				}
			}
			if (count == 0)
				onTestFinished();
		} else {
			onTestFinished();
		}
	}

	private void onTestFinished() {
		mCenterButton.setText("Ready");
		mCenterButton.setTextSize(TEXT_SIZE_READY);
		mCenterButton.setBackgroundResource(R.drawable.circle_blue);
		mButtonClickState = false;
		mSensorMovementReturning.getAndSet(true);
	}

	private SensorEventListener mSensorListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				long currentTime = System.currentTimeMillis();
				long gabOfTime = (currentTime - mSensorLastTime);
				if (gabOfTime > SENSOR_ACTIVATE_TIME_THRESHOLD) {
					mSensorLastTime = currentTime;
					float x = event.values[0];
					float y = event.values[1];
					float z = event.values[2];

					mSensorMovementSpeed = Math.abs(x + y + z - mSensorLastX
							- mSensorLastY - mSensorLastZ)
							/ gabOfTime * 10000;

					if (mSensorMovementSpeed > SHAKE_THRESHOLD) {
						if (mSensorMovementReturning.get()) {
							// 이벤트발생!!
							mSensorMovementReturning.getAndSet(false);
							runFunctionTest();
						}
					}

					mSensorLastX = x;
					mSensorLastY = y;
					mSensorLastZ = z;
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

	};

	@Override
	public IBinder asBinder() {
		return new IInternalEventCallback.Stub() {

			@Override
			public void onReceiveData(int arg0, CallbackData arg1)
					throws RemoteException {
				FunctionTestFragment.this.onReceiveData(arg0, arg1);
			}
		};
	}

	@Override
	public void onReceiveData(int responseCode, CallbackData data)
			throws RemoteException {
		switch (responseCode) {
		case 0x01:
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(getActivity(), "사진 찍기 완료!",
							Toast.LENGTH_SHORT).show();
					onTestFinished();
				}
			});
			break;
		default:
			break;
		}
	}
}
