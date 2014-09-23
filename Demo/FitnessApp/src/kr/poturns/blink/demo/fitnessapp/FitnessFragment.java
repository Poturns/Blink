package kr.poturns.blink.demo.fitnessapp;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import kr.poturns.blink.schema.HeartBeat;
import kr.poturns.blink.schema.PushUp;
import kr.poturns.blink.schema.SitUp;
import kr.poturns.blink.schema.Squat;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.Toast;

public class FitnessFragment extends SwipeEventFragment implements
		SensorEventListener {
	private static final String TAG = FitnessFragment.class.getSimpleName();
	private SQLiteHelper mSqLiteHelper;
	private SensorManager mSensorManager;
	private Sensor mAccelerormeterSensor;
	private int mCountOfPushUps = 0;
	private int mCountOfSitUps = 0;
	private int mCountOfSquats = 0;
	private boolean mSensorActive = false;
	private boolean mFitnessStart = false;
	private AtomicBoolean mSensorMovementReturning = new AtomicBoolean();
	private long mSensorLastTime;
	private float mSensorMovementSpeed;
	private float mSensorLastX;
	private float mSensorLastY;
	private float mSensorLastZ;
	private TextView mCountTextView;
	private TextView mTitleTextView;
	private TextView mHeartBeatTextView;
	private int mCurrentCount = 0;
	private Gson mGson = new GsonBuilder().setPrettyPrinting().create();
	/** 현재 측정중인 운동 */
	private String mCurrentDisplayDbTable = SQLiteHelper.TABLE_PUSH_UP;

	/** 심장박동수를 측정하고, 관련 애니메이션 작업을 처리하는 Thread */
	private Thread mHeartBeatBackgroundThread;
	/** 현재 측정된 BPM */
	private int mBpmCurrent = 0;
	/** 이전에 측정된 BPM */
	private int mBpmPrev = 0;

	/** 센서가 움직임을 감지할 최소한의 속도 */
	private static final int SHAKE_THRESHOLD = 800;
	/** 심장박동수가 위험한 정도임을 알리기위해 필요한 심장박동수 변화값의 최소량 */
	private static final int DIFF_COUNT_OF_HEART_BEAT_NOTIFIED = 20;
	/** 심장박동수를 측정하기까지 걸리는 시간 (초) */
	private static final int HEART_BEAT_COUNT_INTERVAL = 10;
	/** 센서가 한번 측정 후, 다시 측정하기까지 걸리는 시간 */
	private static final int SENSOR_ACTIVATE_TIME_THRESHOLD = 100;
	/** remote device 에 전달 요청 코드 */
	private static final int REQUEST_CODE = 1;
	/** remote app package name */
	private static final String REMOTE_APP_PACKAGE_NAME = "kr.poturns.blink.demo.visualizer";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		mAccelerormeterSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Bundle arg = getArguments();
		if (arg != null) {
			mCurrentDisplayDbTable = arg.getString("fitness");
		} else if (savedInstanceState != null) {
			// TODO restore state
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_fitness, container, false);
		mCountTextView = (TextView) v.findViewById(R.id.fitness_count);
		mCountTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
				R.drawable.ic_launcher, 0, 0);
		mCountTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mFitnessStart) {
					stopCounting();
				} else {
					startCounting();
				}
			}
		});
		mTitleTextView = (TextView) v.findViewById(R.id.fitness_title);
		mHeartBeatTextView = (TextView) v.findViewById(R.id.fitness_heart_beat);
		mHeartBeatBackgroundThread = new HeartBeatActionThread();
		return v;
	}

	private class HeartBeatActionThread extends Thread {
		private int progress = 0;
		private ScaleAnimation anim = new ScaleAnimation(1.0f, 1.0f, 1.2f, 1.2f);
		private Runnable mBeatingAction = new Runnable() {

			@Override
			public void run() {
				mHeartBeatTextView.startAnimation(anim);
			}
		};

		@Override
		public void run() {
			FitnessFragment.this.getActivity().runOnUiThread(
					mHeartBeatStartAction);
			progress = 0;
			while (true) {
				mHeartBeatTextView.post(mBeatingAction);
				synchronized (this) {
					try {
						this.wait(1000);
					} catch (InterruptedException e) {
						break;
					}
				}
				if (progress == HEART_BEAT_COUNT_INTERVAL) {
					mBpmCurrent = generateHeartBeat();
					FitnessFragment.this.getActivity().runOnUiThread(
							mHeartBeatCountAction);
					progress = 0;
				} else
					progress++;
			}
			FitnessFragment.this.getActivity().runOnUiThread(
					mHeartBeatStopAction);
			return;
		}
	};

	/** 심장박동 측정 관련 작업의 UI를 시작하는 Runnable */
	private Runnable mHeartBeatStartAction = new Runnable() {
		@Override
		public void run() {
			mHeartBeatTextView.setText("");
			mBpmCurrent = 0;
			mBpmPrev = 0;
		}
	};

	/** 심장박동 측정 관련 작업의 UI를 종료하는 Runnable */
	private Runnable mHeartBeatStopAction = new Runnable() {

		@Override
		public void run() {
			mHeartBeatTextView.setText("");
			mBpmCurrent = 0;
			mBpmPrev = 0;
		}
	};
	/** 심장박동 측정하는 Runnable */
	private Runnable mHeartBeatCountAction = new Runnable() {

		@Override
		public void run() {
			if (mBpmPrev != 0
					&& Math.abs(mBpmPrev - mBpmCurrent) > DIFF_COUNT_OF_HEART_BEAT_NOTIFIED) {
				notifyHeartBeatAlert();
			}
			mBpmPrev = mBpmCurrent;
			mHeartBeatTextView.setText(String.valueOf(mBpmCurrent));
			putHeartBeat(mBpmCurrent);
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		changeFitness(mCurrentDisplayDbTable);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mFitnessStart)
			startCounting();
		SQLiteHelper.closeDB();
		mSqLiteHelper = SQLiteHelper.getInstance(getActivity());
	}

	@Override
	public void onPause() {
		super.onPause();
		stopCounting();
		SQLiteHelper.closeDB();
	}

	/**
	 * 구성
	 * 
	 * <br>
	 * SITUP <br>
	 * | <br>
	 * pushup <br>
	 * | <br>
	 * SQUAT
	 */
	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT: // 운동 종료
			if (mFitnessStart) {
				showAlertMessage("운동을 종료합니다.");
				stopCounting();
				return true;
			} else {
				terminateFitness();
				mActivityInterface.returnToMain();
				return true;
			}
		case DOWN_TO_UP: // 운동 변경
			if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP)) {
				changeFitness(SQLiteHelper.TABLE_SQUAT);
				return true;
			} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP)) {
				changeFitness(SQLiteHelper.TABLE_PUSH_UP);
				return true;
			}
			return false;
		case UP_TO_DOWN: // 운동 변경
			if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP)) {
				changeFitness(SQLiteHelper.TABLE_SIT_UP);
				return true;
			} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SQUAT)) {
				changeFitness(SQLiteHelper.TABLE_PUSH_UP);
				return true;
			}
			return false;
		default:
			break;
		}
		return false;
	}

	/** 센서 측정을 시작하고, 카운터 버튼으로 운동 횟수를 측정하도록 설정한다. */
	private void registerListener() {
		mSensorManager.registerListener(this, mAccelerormeterSensor,
				SensorManager.SENSOR_DELAY_GAME);
		mSensorActive = true;
		mCountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
		updateCountNumber();
	}

	/** 센서 측정을 종료하고, 카운터 버튼을 '시작'으로 설정한다. */
	private void unregisterListner() {
		mSensorManager.unregisterListener(this);
		mSensorActive = false;
		mCountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
		mCountTextView.setText("시작");
	}

	/** 운동 측정을 종료한다. */
	private void stopCounting() {
		unregisterListner();
		mFitnessStart = false;
		if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP)) {
			mCountOfPushUps += mCurrentCount;
		} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP)) {
			mCountOfSitUps += mCurrentCount;
		} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SQUAT)) {
			mCountOfSquats += mCurrentCount;
		}
		mCountTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0,
				0);
		mCurrentCount = 0;
		mHeartBeatBackgroundThread.interrupt();
	}

	/** 운동 측정을 시작한다. */
	private void startCounting() {
		mFitnessStart = true;
		int resId = 0;
		mCurrentCount = 0;
		if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP)) {
			resId = R.drawable.ic_action_health_push_up;
			mCurrentCount = mCountOfPushUps;
		} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP)) {
			resId = R.drawable.ic_action_health_sit_up;
			mCurrentCount = mCountOfSitUps;
		} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SQUAT)) {
			resId = R.drawable.ic_action_health_squat;
			mCurrentCount = mCountOfSquats;
		}
		Drawable drawable = getResources().getDrawable(resId);
		drawable.setBounds(0, 0, 200, 200);
		mCountTextView.setCompoundDrawablesRelative(null, drawable, null, null);
		registerListener();
		mHeartBeatBackgroundThread = new HeartBeatActionThread();
		mHeartBeatBackgroundThread.start();
	}

	/** Toast를 보여준다. */
	private void showAlertMessage(String msg) {
		Toast toast = Toast.makeText(getActivity(), msg, 1000);
		View toastFrameView = toast.getView();
		toastFrameView.setBackgroundResource(R.drawable.rectangle_box_shadow);
		toastFrameView.setPaddingRelative(20, 20, 20, 20);
		TextView view = (TextView) toastFrameView
				.findViewById(android.R.id.message);
		view.setGravity(Gravity.CENTER);
		view.setTextColor(getResources().getColor(R.color.main));
		view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		view.setShadowLayer(0, 0, 0, 0);
		view.setPaddingRelative(30, 30, 30, 30);
		toast.show();
	}

	/** 해당 운동으로 바꾼다. */
	private void changeFitness(String table) {
		if (mSensorActive) {
			showAlertMessage("먼저 운동을 종료해주세요.");
		} else {
			mCurrentCount = 0;
			mCurrentDisplayDbTable = table;
			mTitleTextView.setText(mCurrentDisplayDbTable);
			stopCounting();
		}
	}

	/** 심장박동수를 생성하기위한 Random */
	private Random mRandom = new Random(System.currentTimeMillis());

	/** 심장박동수를 생성한다. 범위는 50-150 */
	private int generateHeartBeat() {
		return mRandom.nextInt(20) + mRandom.nextInt(20) + mRandom.nextInt(20)
				+ mRandom.nextInt(20) + mRandom.nextInt(20) + 50;
	}

	/** 내부 DB와 BlinkDB에 심장박동수를 입력한다. */
	private void putHeartBeat(int bpm) {
		Log.i("fitness", "putHeartBeat");
		mSqLiteHelper.insert(bpm);
		mActivityInterface.getBlinkServiceInteraction().local
				.registerMeasurementData(new HeartBeat(bpm, DateTimeUtil.get()));

		for (BlinkAppInfo info : mActivityInterface
				.getBlinkServiceInteraction().local.obtainBlinkAppAll()) {
			if (info.mApp.PackageName.equals(REMOTE_APP_PACKAGE_NAME)) {
				Log.i("fitness", "send HeartBeat");
				mActivityInterface.getBlinkServiceInteraction().remote
						.sendMeasurementData(info, mGson.toJson(new HeartBeat(
								bpm, DateTimeUtil.get())), REQUEST_CODE);
				Log.d(TAG, "send HeartBeat : " + bpm + " // to "
						+ REMOTE_APP_PACKAGE_NAME);
				return;
			}
		}
		Log.e(TAG, "Cannot reach remote device : " + REMOTE_APP_PACKAGE_NAME);
	}

	/** 심장박동 변화가 위험할 때, 알림 설정이 되어있는 경우 알림으로 알린다. */
	private void notifyHeartBeatAlert() {
		boolean notify = PreferenceManager.getDefaultSharedPreferences(
				getActivity()).getBoolean(
				SettingFragment.KEY_ALERT_HEART_BEAT_IMPACT, false);
		if (notify) {
			Notification noti = new Notification.Builder(getActivity())
					.setAutoCancel(true)
					.setContentText("심장박동의 변화가 위험할정도로 큽니다.")
					.setContentTitle(getString(R.string.app_name))
					.setSmallIcon(R.drawable.ic_action_health_heart).build();
			NotificationManager manager = (NotificationManager) getActivity()
					.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(9090, noti);
			notifyHeartBeatAlertToOtherDevice();
		}
	}

	private void notifyHeartBeatAlertToOtherDevice() {
		// TODO blink sendmsg
	}

	/** 운동을 종료하고 그동안 운동한 횟수를 기록한다. */
	private void terminateFitness() {
		stopCounting();
		mSqLiteHelper.insert(SQLiteHelper.TABLE_PUSH_UP, mCountOfPushUps);
		mSqLiteHelper.insert(SQLiteHelper.TABLE_SIT_UP, mCountOfSitUps);
		mSqLiteHelper.insert(SQLiteHelper.TABLE_SQUAT, mCountOfSquats);

		// Blink Service에 data 등록
		if (mCountOfPushUps > 0) {
			mActivityInterface.getBlinkServiceInteraction().local
					.registerMeasurementData(new PushUp(mCountOfPushUps,
							DateTimeUtil.get()));
		}
		if (mCountOfSitUps > 0) {
			mActivityInterface.getBlinkServiceInteraction().local
					.registerMeasurementData(new SitUp(mCountOfSitUps,
							DateTimeUtil.get()));
		}
		if (mCountOfSquats > 0) {
			mActivityInterface.getBlinkServiceInteraction().local
					.registerMeasurementData(new Squat(mCountOfSquats,
							DateTimeUtil.get()));
		}
	}

	/** 운동 횟수를 업데이트한다 */
	private void updateCountNumber() {
		mCountTextView.post(mCountAction);
	}

	/** 운동 횟수를 업데이트하는 Runnable */
	private final Runnable mCountAction = new Runnable() {

		@Override
		public void run() {
			mCountTextView.setText(String.valueOf(mCurrentCount));
		}
	};

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
						mCurrentCount++;
						updateCountNumber();
						mSensorMovementReturning.getAndSet(false);
					} else {
						mSensorMovementReturning.getAndSet(true);
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

}