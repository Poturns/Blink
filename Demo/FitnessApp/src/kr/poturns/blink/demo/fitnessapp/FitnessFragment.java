package kr.poturns.blink.demo.fitnessapp;

import java.util.concurrent.atomic.AtomicBoolean;

import kr.poturns.blink.demo.fitnessapp.MainActivity.OnHeartBeatEventListener;
import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import kr.poturns.blink.schema.PushUp;
import kr.poturns.blink.schema.SitUp;
import kr.poturns.blink.schema.Squat;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 운동 측정을 하고, 관련 데이터를 저장한다.
 * 
 * @author Myungjin.Kim
 */
public class FitnessFragment extends SwipeEventFragment implements
		SensorEventListener, OnHeartBeatEventListener {
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
	/** 현재 측정중인 운동 */
	private String mCurrentDisplayDbTable = SQLiteHelper.TABLE_PUSH_UP;

	/** 센서가 움직임을 감지할 최소한의 속도 */
	private static final int SHAKE_THRESHOLD = 800;
	/** 센서가 한번 측정 후, 다시 측정하기까지 걸리는 시간 */
	private static final int SENSOR_ACTIVATE_TIME_THRESHOLD = 100;

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
		return v;
	}

	int bpm;

	@Override
	public void onHeartBeat(int bpm) {
		getActivity().runOnUiThread(mHeartBeatAction);
		this.bpm = bpm;

	}

	private Runnable mHeartBeatAction = new Runnable() {
		private ScaleAnimation anim = new ScaleAnimation(1.0f, 1.0f, 1.2f, 1.2f);

		@Override
		public void run() {
			mHeartBeatTextView.startAnimation(anim);
			mHeartBeatTextView.setText(Integer.toString(bpm));
			return;
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