package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.db.archive.BlinkAppInfo;
import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeListener.Direction;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalEventCallback;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import kr.poturns.blink.schema.HeartBeat;
import kr.poturns.blink.schema.PushUp;
import kr.poturns.blink.schema.SitUp;
import kr.poturns.blink.schema.Squat;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.DismissOverlayView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fitness App의 메인 Activity <br>
 * 기능을 제공하는데 필요한 주요 자원을 관리하고, <br>
 * 터치 이벤트를 받아 하위 fragment에 전달한다.
 * 
 * @author Myungjin.Kim
 */
public class MainActivity extends Activity implements ActivityInterface {
	SwipeListener mChildObject;
	BlinkServiceInteraction mInteraction;
	IInternalOperationSupport mISupport;
	GestureDetector mGestureDetector;
	IntentFilter mHeartBeatActionFilter = new IntentFilter(
			HeartBeatService.WIDGET_HEART_BEAT_ACTION);
	DismissOverlayView mDismissOverlay;
	static final int THRESHHOLD_NOT_DETECT_SWIPE = 20;
	static final int THRESHHOLD_DETECT_SWIPE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Blink 서비스 시작
		mInteraction = new BlinkServiceInteraction(this, null, null) {

			@Override
			public void onServiceFailed() {
			}

			@Override
			public void onServiceDisconnected() {
			}

			@Override
			public void onServiceConnected(IInternalOperationSupport iSupport) {
				mISupport = iSupport;
				// 측정 정보 등록
				BlinkAppInfo info = mInteraction.obtainBlinkApp();
				if (!info.isExist) {
					info.addMeasurement(SitUp.class, "Count of Sit Ups");
					info.addMeasurement(PushUp.class, "Count of Push Ups");
					info.addMeasurement(Squat.class, "Count of Squats");
					info.addMeasurement(HeartBeat.class,
							"Beat per Minute of HeartBeats");
					mInteraction.registerBlinkApp(info);
				}
			}
		};
		mInteraction.startBroadcastReceiver();
		mInteraction.startService();

		// 심박수 측정 서비스 시작/종료
		startOrStopService(PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(SettingFragment.KEY_MEASURE_HEARTBEAT, false));

		// 화면 제스처 등록
		mGestureDetector = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {
						Direction direction = null;
						// 가로로 움직인 폭이 일정 이상이면 무시
						if (Math.abs(e1.getX() - e2.getX()) < THRESHHOLD_DETECT_SWIPE) {
							// 아래서 위로 스크롤 하는 경우
							if (e1.getY() - e2.getY() > THRESHHOLD_NOT_DETECT_SWIPE) {
								direction = Direction.UP_TO_DOWN;
								// 위에서 아래로 스크롤
							} else if (e2.getY() - e1.getY() > THRESHHOLD_NOT_DETECT_SWIPE) {
								direction = Direction.DOWN_TO_UP;
							}
							// 세로로 움직인 폭이 일정 이상이면 무시
						} else if (Math.abs(e1.getY() - e2.getY()) < THRESHHOLD_DETECT_SWIPE) {
							if (e1.getX() - e2.getX() > THRESHHOLD_NOT_DETECT_SWIPE) {
								direction = Direction.RIGHT_TO_LEFT;
							} else if (e2.getX() - e1.getX() > THRESHHOLD_NOT_DETECT_SWIPE) {
								direction = Direction.LEFT_TO_RIGHT;
							}
						}

						if (mChildObject != null && direction != null) {
							return mChildObject.onSwipe(direction);
						}
						return false;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						mDismissOverlay.show();
					}
				});
		mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
		mDismissOverlay.setIntroText("종료?");
		mDismissOverlay.showIntroIfNecessary();

		// 홈 화면으로 이동
		attachFragment(new HomeFragment(), null, R.animator.slide_in_right,
				R.animator.slide_out_left);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		try {
			if (FitnessUtil.readInBodyFromFile(this) == null) {
				showInBodyUpdateDialog();
			}
		} catch (Exception e) {
			e.printStackTrace();
			showInBodyUpdateDialog();
		}
	}

	@Override
	protected void onResume() {
		registerReceiver(mHeartBeatReciever, mHeartBeatActionFilter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mHeartBeatReciever);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		try {
			mInteraction.stopService();
		} catch (Exception e) {
			// ignore
		}
		SQLiteHelper.closeDB();
		super.onDestroy();
	}

	private void showInBodyUpdateDialog() {
		CardFragment card = new CustomCardFragment();
		getFragmentManager().beginTransaction()
				.add(R.id.card_frame, card, "card").commit();
	}

	void dissmissInBodyUpdateDialog() {
		Fragment f = getFragmentManager().findFragmentByTag("card");
		if (f != null) {
			getFragmentManager().beginTransaction().remove(f).commit();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return mGestureDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return mGestureDetector.onTouchEvent(ev)
				|| super.dispatchTouchEvent(ev);
	}

	@Override
	public void attachFragment(Fragment fragment, Bundle arguments) {
		attachFragment(fragment, arguments, R.animator.slide_in_right,
				R.animator.slide_out_left);
	}

	@Override
	public void attachFragment(Fragment fragment, Bundle arguments, int animIn,
			int animOut) {
		if (fragment instanceof SwipeListener) {
			mChildObject = (SwipeListener) fragment;
		} else {
			throw new RuntimeException("Fragment should implement SwipeListner");
		}
		try {
			if (fragment instanceof IInternalEventCallback) {
				mInteraction
						.setIInternalEventCallback((IInternalEventCallback) fragment);
			} else {
				mInteraction.setIInternalEventCallback(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fragment.setArguments(arguments);
		getFragmentManager().beginTransaction()
				.setCustomAnimations(animIn, animOut)
				.replace(R.id.content, fragment).commit();
	}

	@Override
	public void returnToMain() {
		attachFragment(new HomeFragment(), null, R.animator.slide_in_left,
				R.animator.slide_out_right);
	}

	@Override
	public BlinkServiceInteraction getBlinkServiceInteraction() {
		return mInteraction;
	}

	@Override
	public IInternalOperationSupport getBlinkServiceSupport() {
		return mISupport;
	}

	/** 심박수 정보를 전달해 줄 인터페이스 */
	public static interface OnHeartBeatEventListener {
		/**
		 * 심박수 데이터가 왔을 때, 호출된다.
		 * 
		 * @param bpm
		 *            심박수
		 */
		public void onHeartBeat(int bpm);
	}

	/** Swipe 모션 이벤트를 받는 리스너 */
	public static interface SwipeListener {
		/** Swipe 방향 */
		public enum Direction {
			/** 위쪽에서 아래쪽 방향으로 Swipe */
			UP_TO_DOWN,
			/** 아래쪽에서 위쪽 방향으로 Swipe */
			DOWN_TO_UP,
			/** 왼쪽에서 오른쪽 방향으로 Swipe */
			LEFT_TO_RIGHT,
			/** 오른쪽에서 왼쪽 방향으로 Swipe */
			RIGHT_TO_LEFT
		}

		/**
		 * Swipe 이벤트 발생하였을 때, 호출된다.
		 * 
		 * @param direction
		 *            Swipe 방향
		 * @return 이벤트 처리 여부
		 */
		public boolean onSwipe(Direction direction);
	}

	/** 기본적으로 {@link ActivityInterface}가지고 있는 Fragment */
	public static abstract class SwipeEventFragment extends Fragment implements
			SwipeListener {
		protected ActivityInterface mActivityInterface;

		@Override
		public void onAttach(Activity activity) {
			mActivityInterface = (ActivityInterface) activity;
			super.onAttach(activity);
		}
	}

	/** {@link HeartBeatService}로 부터 심박수 측정 정보를 받을 {@link BroadcastReceiver} */
	BroadcastReceiver mHeartBeatReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					HeartBeatService.WIDGET_HEART_BEAT_ACTION)) {
				int bpm = intent.getIntExtra(
						HeartBeatService.WIDGET_HEART_BEAT_VALUE, 0);
				if (bpm < 1)
					return;

				// 심박수 측정 이벤트 전달
				if (mChildObject != null
						&& mChildObject instanceof OnHeartBeatEventListener) {
					((OnHeartBeatEventListener) mChildObject).onHeartBeat(bpm);
				}
			}
		}
	};

	@Override
	public void startOrStopService(boolean start) {
		Intent intent = new Intent(this, HeartBeatService.class);
		if (start) {
			startService(intent);
		} else {
			stopService(intent);
		}
	}

	private class CustomCardFragment extends CardFragment {
		@Override
		public View onCreateContentView(LayoutInflater inflater,
				ViewGroup parent, Bundle savedInstance) {
			View view = inflater.inflate(R.layout.main_inbody_alert, parent,
					false);
			DelayedConfirmationView delayedView = (DelayedConfirmationView) view
					.findViewById(R.id.delayed_confirm_load);
			delayedView
					.setListener(new DelayedConfirmationView.DelayedConfirmationListener() {
						@Override
						public void onTimerFinished(View view) {
							dissmissInBodyUpdateDialog();
						}

						@Override
						public void onTimerSelected(View view) {
							dissmissInBodyUpdateDialog();
							Bundle b = new Bundle();
							b.putBoolean("hasNotInbody", true);
							attachFragment(new InBodyFragment(), b);
						}
					});
			delayedView.setTotalTimeMs(2000);
			delayedView.start();
			return view;
		}
	}
}
