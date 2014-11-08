package kr.poturns.blink.demo.fitnessapp;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import kr.poturns.blink.demo.fitnessapp.MainActivity.OnHeartBeatEventListener;
import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import kr.poturns.blink.demo.fitnesswear.R;

public class HeartBeatFragment extends SwipeEventFragment implements
		OnHeartBeatEventListener {
	/** 심장박동 애니메이션을 보여줄 Thread */
	private Thread mHeartBeatingThread;
	private TextView mHeartBeatTextView, mActionTextView;
	/** 서비스가 측정한 BPM */
	int mMeasuredBpm;
	private int mColorRed, mColorWhite;
	private boolean mMeasuring = false;

	private final ScaleAnimation BEATING_AMINATION = new ScaleAnimation(1f,
			1f, .9f, 1.1f, 0f, -1f);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mMeasuring = getHeartBeatPreferenceValue();
		mColorRed = getResources().getColor(R.color.red);
		mColorWhite = getResources().getColor(android.R.color.white);
		final View v = inflater.inflate(R.layout.fragment_heartbeat, container,
				false);
		mHeartBeatTextView = (TextView) v.findViewById(R.id.heartbeat_heart);
		mActionTextView = (TextView) v.findViewById(android.R.id.button1);
		mActionTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mMeasuring) {
					// stop measure
					disableMeasureUI();
					mMeasuring = false;
				} else {
					// start measure
					enableMeasureUI();
					mMeasuring = true;
				}

			}
		});
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mMeasuring) {
			enableMeasureUI();
		} else {
			disableMeasureUI();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mHeartBeatingThread != null) {
			mHeartBeatingThread.interrupt();
			mHeartBeatingThread = null;
		}
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

	void enableMeasureUI() {
		mActivityInterface.startOrStopService(true);
		putHeartBeatPreferenceValue(true);
		mActionTextView.setText("Stop");
		mActionTextView.setBackgroundColor(mColorWhite);
		mActionTextView.setTextColor(mColorRed);
		if (mHeartBeatingThread != null) {
			mHeartBeatingThread.interrupt();
			mHeartBeatingThread = null;
		}
		mHeartBeatingThread = new HeartBeatAction();
		mHeartBeatingThread.start();
	}

	void disableMeasureUI() {
		mActivityInterface.startOrStopService(false);
		putHeartBeatPreferenceValue(false);
		mActionTextView.setText("Start");
		mActionTextView.setTextColor(mColorWhite);
		mActionTextView.setBackgroundColor(mColorRed);
		if (mHeartBeatingThread != null) {
			mHeartBeatingThread.interrupt();
			mHeartBeatingThread = null;
		}
	}

	/**
	 * {@link SettingFragment#KEY_MEASURE_HEARTBEAT} 설정값을 가져온다.
	 * 
	 * @return 기록된 설정값
	 */
	boolean getHeartBeatPreferenceValue() {
		return PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getBoolean(SettingFragment.KEY_MEASURE_HEARTBEAT, false);
	}

	/**
	 * {@link SettingFragment#KEY_MEASURE_HEARTBEAT} 설정값을 입력한다.
	 * 
	 * @return true - 설정값이 정상적으로 입력 됨.
	 */
	boolean putHeartBeatPreferenceValue(boolean value) {
		return PreferenceManager.getDefaultSharedPreferences(getActivity())
				.edit()
				.putBoolean(SettingFragment.KEY_MEASURE_HEARTBEAT, value)
				.commit();
	}

	@Override
	public void onHeartBeat(int bpm) {
		this.mMeasuredBpm = bpm;
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mHeartBeatTextView.setText(Integer.toString(mMeasuredBpm));
				mHeartBeatTextView.startAnimation(BEATING_AMINATION);
			}
		});
	}

	/** 심장박동 애니메이션을 보여줄 Thread */
	private class HeartBeatAction extends Thread {
		private static final String TAG = "HeartBeatAction";

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
				// mHeartBeatTextView.post(mAnimation);
			}
			Log.d(TAG, "end");
			return;
		}
	};
}
