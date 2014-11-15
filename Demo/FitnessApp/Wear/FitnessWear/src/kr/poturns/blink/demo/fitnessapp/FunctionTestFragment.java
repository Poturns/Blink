package kr.poturns.blink.demo.fitnessapp;

import java.util.List;

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
import android.os.AsyncTask;
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
	private SensorManager mSensorManager;
	private Sensor mAccelerormeterSensor;
	FunctionTester mFunctionTester = new FunctionTester();
	/** 마지막으로 사진 측정을 요청한 시간 */
	private long mTimeStamp;
	/**
	 * Fragment 의 생명 주기 내에서만 Function 요청에 대한 응답을 처리하기 위한 변수, Fragment가 파괴되면
	 * 새로생성된 Fragment에서의 이 변수값이 증가하여 이전 요청은 처리하지 않게 된다.
	 */
	private static int sTempRequestCode = 0;
	private static final String ACTION_TAKE_PICTURE = "action.takepicture";
	// private static final String TAG = FunctionTestFragment.class
	// .getSimpleName();
	private static final long INTERVAL_REQUEST_SECOND = 3000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		mAccelerormeterSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sTempRequestCode++;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_function_test, container,
				false);
		mCenterButton = (TextView) v.findViewById(android.R.id.text1);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		mFunctionTester.ready();
	}

	@Override
	public void onPause() {
		super.onPause();
		mFunctionTester.stop();
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT:
			mActivityInterface.returnToMain();
			return true;
		default:
			return false;
		}
	}

	/** Ready - Test - Finished -> 반복 */
	private class FunctionTester {
		private static final int TEXT_SIZE_READY = 30;
		private static final int TEXT_SIZE_START = 25;
		private static final int TEXT_SIZE_FINISHED = 25;
		private static final String TAG = "FunctionTester";

		/** Function 요청을 보내는 준비 화면에 진입한다. (Ready) */
		public void ready() {
			setButtonStateReady();
		}

		/**
		 * Function 요청을 종료하는 화면에 진입하고, 센서를 비활성화시킨다. 저장한 센서값도 초기화시킨다. (Finished) <br>
		 * <br>
		 * % 이 시점에서 mStop Action이 제거된다.
		 */
		public void stop() {
			mSensorManager.unregisterListener(mSensorListener);
			setButtonStateFinish();
			sensorValueReset();
			mCenterButton.removeCallbacks(mStop);
		}

		/** 버튼을 'Ready' 상태로 만든다. (터치하면 'Test'상태 진입) */
		private void setButtonStateReady() {
			Log.d(TAG, "button : ready");
			mCenterButton.setText("Ready");
			mCenterButton.setTextSize(TEXT_SIZE_READY);
			mCenterButton.setBackgroundResource(R.drawable.circle_blue);
			mCenterButton.setOnClickListener(mReady);
		}

		private final View.OnClickListener mReady = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setButtonStateTest();
			}
		};

		/**
		 * 버튼을 'Test' 상태로 만든다. (터치하면 실제 테스트를 진행하고, 'Wait'상태 진입)<br>
		 * <br>
		 * % 이 시점에서 Sensor Listener가 등록된다.
		 */
		private void setButtonStateTest() {
			Log.d(TAG, "button : test");
			mCenterButton.setTextSize(TEXT_SIZE_START);
			mCenterButton.setText("Touch\nOR\nShake");
			mCenterButton.setBackgroundResource(R.drawable.circle_green);
			mCenterButton.setOnClickListener(mTest);
			mSensorManager.registerListener(mSensorListener,
					mAccelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);
		}

		private final View.OnClickListener mTest = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				runFunctionTest();
			}
		};

		/** 버튼을 'Finish' 상태로 만든다. (터치하면 'Ready'상태 진입) */
		private void setButtonStateFinish() {
			Log.d(TAG, "button : finished(retry)");
			mCenterButton.setTextSize(TEXT_SIZE_FINISHED);
			mCenterButton.setText("Touch here to retry");
			mCenterButton.setBackgroundResource(R.drawable.circle_orange);
			mCenterButton.setOnClickListener(mFinish);
		}

		private final View.OnClickListener mFinish = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setButtonStateReady();
			}
		};

		/** 버튼을 'Wait' 상태로 만든다. (터치불가) */
		private void setButtonStateWating() {
			Log.d(TAG, "button : sending & waiting");
			mCenterButton.setTextSize(TEXT_SIZE_FINISHED);
			mCenterButton.setText("Sending...");
			mCenterButton
					.setBackgroundResource(R.drawable.res_blink_drawable_rounded_circle_red);
			mCenterButton.setOnClickListener(null);

			// 30초 후 종료 상태로 설정한다.
			// (정상적으로 요청 응답이 와서 onReceiveData() 가 호출되면
			// 이 Action은 제거된다.)
			mCenterButton.postDelayed(mStop, 1000 * 30);
		}

		private final Runnable mStop = new Runnable() {

			@Override
			public void run() {
				stop();
			}
		};

		void runFunctionTest() {
			Log.d(TAG, "function test");
			if (System.currentTimeMillis() - mTimeStamp < INTERVAL_REQUEST_SECOND) {
				Toast.makeText(getActivity(), "이미 요청을 보냈습니다. 잠시후에 시도하세요",
						Toast.LENGTH_SHORT).show();
				stop();
				return;
			}
			if (mActivityInterface.getBlinkServiceInteraction() != null) {
				// 버튼을 대기 상태로
				setButtonStateWating();

				// 비동기적으로 Function 검색 및 요청
				AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {

					@Override
					public void run() {
						List<BlinkAppInfo> appInfos = null;
						try {
							appInfos = mActivityInterface
									.getBlinkServiceInteraction().local
									.obtainBlinkAppAll();
						} catch (Exception e) {
							e.printStackTrace();
							getActivity().runOnUiThread(mStop);
							return;
						}
						if (appInfos == null) {
							getActivity().runOnUiThread(mStop);
							return;
						}
						int count = 0;
						for (BlinkAppInfo info : appInfos) {
							for (Function function : info.mFunctionList) {
								if (function.Action
										.contains(ACTION_TAKE_PICTURE)) {
									Log.d(TAG, "---- startFunction ----\n"
											+ function.toString()
											+ "\n-----------------------\n\n");
									mActivityInterface
											.getBlinkServiceInteraction().remote
											.startFunction(function,
													sTempRequestCode);
									count++;
								}
							}
						}
						Log.d(TAG, "Function Test : send count - " + count);
						if (count == 0) {
							getActivity().runOnUiThread(mStop);
						} else {
							mTimeStamp = System.currentTimeMillis();
						}
					}
				});
			} else {
				stop();
			}
		}
	}

	private boolean isAgain = false;
	/** 센서가 측정한 시간 */
	private long mSensorLastTime;
	/** 센서가 측정한 속도 */
	private float mSensorMovementSpeed;
	/** 센서가 측정한 위치 값 */
	private float[] mSensorLasts = { 0, 0, 0 };

	private void sensorValueReset() {
		isAgain = false;
		mSensorLastTime = 0;
		mSensorMovementSpeed = 0;
		mSensorLasts = new float[] { 0, 0, 0 };
	}

	private final SensorEventListener mSensorListener = new SensorEventListener() {

		/** 센서가 움직임을 감지할 최소한의 속도 */
		private static final int SHAKE_THRESHOLD = 800;
		/** 센서가 한번 측정 후, 다시 측정하기까지 걸리는 시간 */
		private static final int SENSOR_ACTIVATE_TIME_THRESHOLD = 100;

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				long currentTime = System.currentTimeMillis();
				long gabOfTime = (currentTime - mSensorLastTime);
				if (gabOfTime > SENSOR_ACTIVATE_TIME_THRESHOLD) {
					mSensorLastTime = currentTime;
					float[] newSensorValues = { event.values[0],
							event.values[1], event.values[2] };

					float newSum = 0, oldSum = 0;
					for (int i = 0; i < 3; i++) {
						newSum += newSensorValues[i];
						oldSum += mSensorLasts[i];
					}
					mSensorMovementSpeed = Math.abs(newSum - oldSum)
							/ gabOfTime * 10000;

					if (mSensorMovementSpeed > SHAKE_THRESHOLD) {
						if (isAgain) {
							isAgain = false;
							mFunctionTester.runFunctionTest();
						} else {
							isAgain = true;
						}
					}

					mSensorLasts = newSensorValues;
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
		if (responseCode == sTempRequestCode) {
			getActivity().runOnUiThread(mResponseAction);
		}
	}

	private final Runnable mResponseAction = new Runnable() {

		@Override
		public void run() {
			Toast.makeText(getActivity(), "사진 찍기 완료!", Toast.LENGTH_SHORT)
					.show();
			mFunctionTester.stop();
		}
	};
}
