package kr.poturns.blink.demo.fitnessapp;

import java.util.concurrent.atomic.AtomicBoolean;

import kr.poturns.blink.demo.fitnessapp.MainActivity.OnHeartBeatEventListener;
import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import kr.poturns.blink.schema.PushUp;
import kr.poturns.blink.schema.SitUp;
import kr.poturns.blink.schema.Squat;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.util.Log;
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
	/** 측정한 운동 횟수 */
	private int mCountOfPushUps = 0, mCountOfSitUps = 0, mCountOfSquats = 0;
	private boolean mSensorActive = false;
	private boolean mFitnessStart = false;
	private AtomicBoolean mSensorMovementReturning = new AtomicBoolean();
	/** 센서가 측정한 시간 */
	private long mSensorLastTime;
	/** 센서가 측정한 속도 */
	private float mSensorMovementSpeed;
	/** 센서가 측정한 위치 값 */
	private float mSensorLastX, mSensorLastY, mSensorLastZ;
	private TextView mCountTextView;
	private TextView mTitleTextView;
	private TextView mHeartBeatTextView;
	/** 현재 측정중인 운동 횟수 */
	private int mCurrentCount = 0;
	/** 서비스가 측정한 BPM */
	int mMeasuredBpm;
	/** 현재 측정중인 운동 */
	private String mCurrentDisplayDbTable = SQLiteHelper.TABLE_PUSH_UP;
	/** 심장박동 애니메이션을 보여줄 Thread */
	private Thread mHeartBeatingThread;
	/** 센서가 움직임을 감지할 최소한의 속도 */
	private static final int SHAKE_THRESHOLD = 800;
	/** 센서가 한번 측정 후, 다시 측정하기까지 걸리는 시간 */
	private static final int SENSOR_ACTIVATE_TIME_THRESHOLD = 100;
	/** Bundle을 통해 전달한 운동을 가리키는 EXTRA_NAME */
	public static final String EXTRA_STRING_FITNESS = "fitness";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		mAccelerormeterSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		if (savedInstanceState != null) {
			mCountOfPushUps = savedInstanceState
					.getInt(SQLiteHelper.TABLE_PUSH_UP);
			mCountOfSitUps = savedInstanceState
					.getInt(SQLiteHelper.TABLE_SIT_UP);
			mCountOfSquats = savedInstanceState
					.getInt(SQLiteHelper.TABLE_SQUAT);
			mCurrentCount = savedInstanceState.getInt("current");
			mCurrentDisplayDbTable = savedInstanceState.getString(
					EXTRA_STRING_FITNESS, SQLiteHelper.TABLE_PUSH_UP);
		}
		Bundle arg = getArguments();
		if (arg != null) {
			mCountOfPushUps = arg.getInt(SQLiteHelper.TABLE_PUSH_UP,
					mCountOfPushUps);
			mCountOfSitUps = arg.getInt(SQLiteHelper.TABLE_SIT_UP,
					mCountOfSitUps);
			mCountOfSquats = arg.getInt(SQLiteHelper.TABLE_SQUAT,
					mCountOfSquats);
			mCurrentCount = arg.getInt("current", mCurrentCount);
			mCurrentDisplayDbTable = arg.getString(EXTRA_STRING_FITNESS);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SQLiteHelper.TABLE_PUSH_UP, mCountOfPushUps);
		outState.putInt(SQLiteHelper.TABLE_SIT_UP, mCountOfSitUps);
		outState.putInt(SQLiteHelper.TABLE_SQUAT, mCountOfSquats);
		outState.putInt("current", mCurrentCount);
		outState.putString(EXTRA_STRING_FITNESS, mCurrentDisplayDbTable);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_fitness, container, false);
		mCountTextView = (TextView) v.findViewById(R.id.fitness_count);
		if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP)) {
			mCountTextView.setBackgroundResource(R.drawable.circle_orange);
		} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP)) {
			mCountTextView.setBackgroundResource(R.drawable.circle_green);
		} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SQUAT)) {
			mCountTextView.setBackgroundResource(R.drawable.circle_blue);
		}
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
		mTitleTextView.setText(mCurrentDisplayDbTable);
		mHeartBeatTextView = (TextView) v.findViewById(R.id.fitness_heart_beat);
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
			startCounting();
		if (getHeartBeatPreferenceValue()) {
			mHeartBeatingThread = new HeartBeatAction();
			mHeartBeatingThread.start();
		}
		SQLiteHelper.closeDB();
		mSqLiteHelper = SQLiteHelper.getInstance(getActivity());
	}

	@Override
	public void onPause() {
		super.onPause();
		stopCounting();
		if (mHeartBeatingThread != null)
			mHeartBeatingThread.interrupt();
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
			String table = null;
			if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP)) {
				table = SQLiteHelper.TABLE_SQUAT;
			} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP)) {
				table = SQLiteHelper.TABLE_PUSH_UP;
			} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SQUAT)) {
				table = SQLiteHelper.TABLE_SIT_UP;
			} else
				return false;
			if (changeFitness(table)) {
				Bundle bundle = new Bundle();
				onSaveInstanceState(bundle);
				mActivityInterface.attachFragment(new FitnessFragment(),
						bundle, R.animator.slide_in_bottom,
						R.animator.slide_out_up);
				return true;
			}
			return false;
		case UP_TO_DOWN: // 운동 변경
			String table1 = null;
			if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP)) {
				table1 = SQLiteHelper.TABLE_SIT_UP;
			} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SQUAT)) {
				table1 = SQLiteHelper.TABLE_PUSH_UP;
			} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP)) {
				table1 = SQLiteHelper.TABLE_SQUAT;
			} else
				return false;
			if (changeFitness(table1)) {
				Bundle bundle = new Bundle();
				onSaveInstanceState(bundle);
				mActivityInterface.attachFragment(new FitnessFragment(),
						bundle, R.animator.slide_in_up,
						R.animator.slide_out_bottom);
				return true;
			}
			return false;
		default:
			return false;
		}
	}

	@Override
	public void onHeartBeat(int bpm) {
		this.mMeasuredBpm = bpm;
		getActivity().runOnUiThread(mHeartBeatTextAction);
	}

	/** 비동기적으로 심장박동수를 TextView에 표현하는 Action */
	private Runnable mHeartBeatTextAction = new Runnable() {

		@Override
		public void run() {
			mHeartBeatTextView.setText(Integer.toString(mMeasuredBpm));
		}
	};

	/** 심장박동 애니메이션을 보여줄 Thread */
	private class HeartBeatAction extends Thread {
		private ScaleAnimation anim = new ScaleAnimation(1.0f, 1.0f, 1.2f, 1.2f);
		private static final String TAG = "HeartBeatAction";

		private Runnable mAnimation = new Runnable() {

			@Override
			public void run() {
				mHeartBeatTextView.startAnimation(anim);
			}
		};

		@Override
		public void run() {
			Log.d(TAG, "start");
			while (getHeartBeatPreferenceValue()) {
				try {
					synchronized (this) {
						wait(900);
					}
				} catch (InterruptedException e) {
					break;
				}
				mHeartBeatTextView.post(mAnimation);
			}
			Log.d(TAG, "end");
			return;
		}
	};

	/** {@link SettingFragment#KEY_MEASURE_HEARTBEAT} 설정값을 가져온다. */
	boolean getHeartBeatPreferenceValue() {
		return PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getBoolean(SettingFragment.KEY_MEASURE_HEARTBEAT, false);
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
		Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
		View toastFrameView = toast.getView();
		toastFrameView.setBackgroundResource(R.drawable.rectangle_box_shadow);
		toastFrameView.setPaddingRelative(20, 20, 20, 20);
		TextView view = (TextView) toastFrameView
				.findViewById(android.R.id.message);
		view.setGravity(Gravity.CENTER);
		view.setTextColor(getResources().getColor(R.color.orange));
		view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		view.setShadowLayer(0, 0, 0, 0);
		view.setPaddingRelative(30, 30, 30, 30);
		toast.show();
	}

	/** 해당 운동으로 바꾼다. */
	private boolean changeFitness(String table) {
		if (mSensorActive) {
			showAlertMessage("먼저 운동을 종료해주세요.");
			return false;
		} else {
			mCurrentCount = 0;
			mCurrentDisplayDbTable = table;
			stopCounting();
			return true;
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
							DateTimeUtil.getTimeString()));
		}
		if (mCountOfSitUps > 0) {
			mActivityInterface.getBlinkServiceInteraction().local
					.registerMeasurementData(new SitUp(mCountOfSitUps,
							DateTimeUtil.getTimeString()));
		}
		if (mCountOfSquats > 0) {
			mActivityInterface.getBlinkServiceInteraction().local
					.registerMeasurementData(new Squat(mCountOfSquats,
							DateTimeUtil.getTimeString()));
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

	public class FitnessGridPagerAdapter extends FragmentGridPagerAdapter {
		private int mPrevRow = 0;
		
		public FitnessGridPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getFragment(int row, int column) {
			Fragment f;
			String table;
			if(mPrevRow < row){
				if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP)) {
					table = SQLiteHelper.TABLE_SQUAT;
				} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP)) {
					table = SQLiteHelper.TABLE_PUSH_UP;
				} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SQUAT)) {
					table = SQLiteHelper.TABLE_SIT_UP;
				} else
					return null;
			}else{
				if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP)) {
					table = SQLiteHelper.TABLE_SIT_UP;
				} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SQUAT)) {
					table = SQLiteHelper.TABLE_PUSH_UP;
				} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP)) {
					table = SQLiteHelper.TABLE_SQUAT;
				} else
					return null;
			}
			if (changeFitness(table)) {
				Bundle bundle = new Bundle();
				onSaveInstanceState(bundle);
				f = new FitnessFragment();
				f.setArguments(bundle);
			}
			return null;
		}

		@Override
		public int getColumnCount(int row) {
			return 1;
		}

		@Override
		public int getRowCount() {
			return Integer.MAX_VALUE;
		}

	}
	
	class InnerFragment extends Fragment{
		
	}
	
	
	
}
