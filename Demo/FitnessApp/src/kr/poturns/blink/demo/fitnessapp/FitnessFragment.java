package kr.poturns.blink.demo.fitnessapp;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class FitnessFragment extends SwipeEventFragment implements
		SensorEventListener {
	private SQLiteHelper mSqLiteHelper;
	private SensorManager mSensorManager;
	private Sensor mAccelerormeterSensor;
	private int mCountOfPushUps = 0;
	private int mCountOfSitUps = 0;
	private int mCountOfSquats = 0;
	private boolean mSensorActive = false;
	private boolean mFitnessStart = false;
	private AtomicBoolean isReturning = new AtomicBoolean();
	private long lastTime;
	private float speed;
	private float lastX;
	private float lastY;
	private float lastZ;
	private float x, y, z;
	private TextView mCountTextView;
	private TextView mTitleTextview;
	private int mCurrentCount = 0;
	private String mCurrentDisplayDbTable = SQLiteHelper.TABLE_PUSH_UP;
	private static final int SHAKE_THRESHOLD = 800;

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
		mTitleTextview = (TextView) v.findViewById(R.id.fitness_title);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		changeFitness(mCurrentDisplayDbTable);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mFitnessStart)
			registerListener();
		SQLiteHelper.closeDB();
		mSqLiteHelper = SQLiteHelper.getInstance(getActivity());
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterListner();
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
		case LEFT_TO_RIGHT:
			if (mFitnessStart) {
				showAlertMessage("운동을 종료합니다.");
				stopCounting();
				return true;
			} else {
				terminateFitness();
				mActivityInterface.returnToMain();
				return true;
			}
		case DOWN_TO_UP:
			if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP)) {
				changeFitness(SQLiteHelper.TABLE_SQUAT);
				return true;
			} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP)) {
				changeFitness(SQLiteHelper.TABLE_PUSH_UP);
				return true;
			}
			return false;
		case UP_TO_DOWN:
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

	private void registerListener() {
		mSensorManager.registerListener(this, mAccelerormeterSensor,
				SensorManager.SENSOR_DELAY_GAME);
		mSensorActive = true;
		mCountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
		updateCountNumber();
	}

	private void unregisterListner() {
		mSensorManager.unregisterListener(this);
		mSensorActive = false;
		mCountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
		mCountTextView.setText("시작");
	}

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

	private void startCounting() {
		mFitnessStart = true;
		int resId = 0;
		mCurrentCount = 0;
		if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP)) {
			resId = R.drawable.ic_action_action_dumbbell;
			mCurrentCount = mCountOfPushUps;
		} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP)) {
			resId = R.drawable.res_blink_ic_action_action_search;
			mCurrentCount = mCountOfSitUps;
		} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SQUAT)) {
			resId = R.drawable.res_blink_ic_action_android;
			mCurrentCount = mCountOfSquats;
		}
		mCountTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
				resId, 0, 0);

		registerListener();
	}

	private void showAlertMessage(String msg) {
		Toast toast = Toast.makeText(getActivity(), msg, 1000);
		View toastFrameView = toast.getView();
		toastFrameView.setBackgroundResource(R.drawable.rectangle_box_shadow);
		TextView view = (TextView) toastFrameView
				.findViewById(android.R.id.message);
		view.setTextColor(Color.BLACK);
		view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		view.setPaddingRelative(20, 20, 20, 20);
		toast.show();
	}

	private void changeFitness(String table) {
		if (mSensorActive) {
			showAlertMessage("먼저 운동을 종료해주세요.");
		} else {
			mCurrentCount = 0;
			mCurrentDisplayDbTable = table;
			mTitleTextview.setText(mCurrentDisplayDbTable);
			stopCounting();
		}
	}

	private Random mRandom = new Random(System.currentTimeMillis());

	private int generateHeartBeat() {
		return mRandom.nextInt(55) + 20;
	}

	private void putHeartBeat(int bpm) {
		mSqLiteHelper.insert(bpm);
	}

	private void notifyHeartBeatAlert() {

	}

	private void terminateFitness() {
		stopCounting();
		mSqLiteHelper.insert(mCurrentDisplayDbTable, mCountOfPushUps);
		mSqLiteHelper.insert(mCurrentDisplayDbTable, mCountOfSitUps);
		mSqLiteHelper.insert(mCurrentDisplayDbTable, mCountOfSquats);
		// TODO Blink Service에 data 등록
	}

	private void updateCountNumber() {
		mCountTextView.post(mCountAction);
	}

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
			long gabOfTime = (currentTime - lastTime);
			if (gabOfTime > 100) {
				lastTime = currentTime;
				x = event.values[0];
				y = event.values[1];
				z = event.values[2];

				speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime
						* 10000;

				if (speed > SHAKE_THRESHOLD) {
					if (isReturning.get()) {
						// 이벤트발생!!
						mCurrentCount++;
						updateCountNumber();
						isReturning.getAndSet(false);
					} else {
						isReturning.getAndSet(true);
					}
				}

				lastX = x;
				lastY = y;
				lastZ = z;
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

}