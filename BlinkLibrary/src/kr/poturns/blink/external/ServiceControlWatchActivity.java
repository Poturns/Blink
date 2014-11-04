package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.external.ServiceControlWatchActivity.SwipeListener.Direction;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Watch를 위한 Activity
 * 
 * @author myungjin
 */
public class ServiceControlWatchActivity extends Activity implements
		IServiceContolActivity {
	BlinkServiceInteraction mInteraction;
	IInternalOperationSupport mBlinkOperation;
	SqliteManagerExtended mSqliteManagerExtended;
	private GestureDetector mGestureDetector;
	SwipeListener mSwipeListener;
	static final int THRESHHOLD_NOT_DETECT_SWIPE = 20;
	static final int THRESHHOLD_DETECT_SWIPE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.res_blink_activity_service_control_watch);

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

						if (mSwipeListener != null && direction != null) {
							return mSwipeListener.onSwipe(direction);
						}
						return false;
					}
				});
		transitFragment(0, null);
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
	protected void onDestroy() {
		if (mSqliteManagerExtended != null)
			mSqliteManagerExtended.close();
		super.onDestroy();
	}

	/** Swipe 모션 이벤트를 받는 리스너 */
	static interface SwipeListener {
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

	@Override
	public void transitFragment(int position, Bundle arguments) {
		Fragment f;
		switch (position) {
		case 0:
			f = new ConnectionFragment();
			break;
		case 1:
			f = new PreferenceExternalFragment();
			break;
		default:
			return;
		}
		if (f instanceof SwipeListener) {
			mSwipeListener = (SwipeListener) f;
		}else{
			//throw new RuntimeException("not implement SwipeLitener");
		}

		getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(android.R.animator.fade_in,
						android.R.animator.fade_out)
				.replace(R.id.res_blink_activity_main_fragment_content, f)
				.commit();
	}

	@Override
	public BlinkServiceInteraction getServiceInteration() {
		return mInteraction;
	}

	@Override
	public void setServiceInteration(BlinkServiceInteraction interaction) {
		mInteraction = interaction;
	}

	@Override
	public void setInternalOperationSupport(
			IInternalOperationSupport blinkOperation) {
		mBlinkOperation = blinkOperation;
	}

	@Override
	public IInternalOperationSupport getInternalOperationSupport() {
		return mBlinkOperation;
	}

	@Override
	public SqliteManagerExtended getDatabaseHandler() {
		return mSqliteManagerExtended;
	}

}
