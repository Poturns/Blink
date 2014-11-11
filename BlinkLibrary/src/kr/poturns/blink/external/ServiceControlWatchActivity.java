package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.external.SwipeListener.Direction;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/** Swipe 모션 이벤트를 받는 리스너 */
interface SwipeListener {
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

/** Wearable을 위한 추가적인 기능이 있는 인터페이스 */
interface IServiceContolWatchActivity extends IServiceContolActivity {
	/**
	 * {@link #transitFragment(int, Bundle)}를 위한 상수<br>
	 * <br>
	 * <b>홈 화면</b>
	 */
	static final int POSITION_MAIN = 0;
	/**
	 * {@link #transitFragment(int, Bundle)}를 위한 상수<br>
	 * <br>
	 * <b>연결 상태 화면</b>
	 */
	static final int POSITION_CONNECTION = 1;
	/**
	 * {@link #transitFragment(int, Bundle)}를 위한 상수<br>
	 * <br>
	 * <b>설정 화면</b>
	 */
	static final int POSITION_PREFERENCE = 2;

	/** 홈 화면으로 이동한다. */
	void returnToMain(Bundle arguments);
}

/** TitleBar를 Long Click 했을 때, 호출되는 인터페이스 */
interface OnTitleBarLongClickListener {
	/**
	 * TitleBar를 Long Click 했을 때, 호출된다.
	 * 
	 * @param titleView
	 *            TitleView
	 * @return 작업의 처리 여부, false를 반환할 경우 기본 동작은 Activity가 종료되는 것이다.
	 * */
	boolean onTitleViewLongClick(View titleView);
}

/**
 * Service에서 실행 되어, Blink Service와 일부 상호작용하는 {@link android.app.Activity}<br>
 * Wearable 화면을 위해 수행할 수 있는 기능이 간략화 되었다.<br>
 * <br>
 * 이 {@link android.app.Activity}를 통해 다음과 같은 작업을 수행 할 수 있다. <br>
 * <li>{@link BlinkDevice}의 연결 상태 표시 및 관리</li><br>
 * <li>
 * Service 설정 값 변경</li>
 * 
 * @author myungjin
 */
public class ServiceControlWatchActivity extends Activity implements
		IServiceContolWatchActivity {
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

		// titleBar
		findViewById(android.R.id.text1).setOnLongClickListener(
				new View.OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						if (mSwipeListener instanceof OnTitleBarLongClickListener) {
							if (!((OnTitleBarLongClickListener) mSwipeListener)
									.onTitleViewLongClick(v)) {
								finish();
							}
						} else {
							finish();
						}
						return true;
					}
				});

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

	@Override
	public void transitFragment(int position, Bundle arguments) {
		Fragment f;
		switch (position) {
		case POSITION_CONNECTION:
			f = ConnectionFragment.getFragment();
			break;
		case POSITION_PREFERENCE:
			f = PreferenceExternalFragment.getFragment();
			break;
		case POSITION_MAIN:
			f = new HomeFragment();
			break;
		default:
			return;
		}
		if (f instanceof SwipeListener) {
			mSwipeListener = (SwipeListener) f;
		} else {
			throw new RuntimeException("not implement SwipeLitener");
		}

		getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(android.R.animator.fade_in,
						android.R.animator.fade_out)
				.replace(R.id.res_blink_activity_main_fragment_content, f)
				.commit();
	}

	@Override
	public void returnToMain(Bundle arguments) {
		transitFragment(0, arguments);
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

final class HomeFragment extends Fragment implements SwipeListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ListView listView = (ListView) inflater.inflate(
				R.layout.res_blink_view_listview, container, false);
		String[] titles = getResources().getStringArray(
				R.array.res_blink_activity_sercive_control_menu_array);
		listView.setAdapter(new ArrayAdapter<String>(getActivity(),
				R.layout.res_blink_list_watch_home, android.R.id.text1,
				new String[] { titles[0], titles[3] }) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView tv = (TextView) view.findViewById(android.R.id.text1);
				tv.setText(getItem(position));
				switch (position) {
				case 0:
					tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
							R.drawable.res_blink_ic_action_device_access_bluetooth_connected,
							0, 0, 0);
					break;
				case 1:
					tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
							R.drawable.res_blink_ic_action_android, 0, 0, 0);
					break;
				default:
					break;
				}
				return view;
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				((IServiceContolActivity) getActivity()).transitFragment(
						position + 1, null);
			}
		});
		listView.setPaddingRelative(30, 30, 10, 30);
		return listView;
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT:
			getActivity().finish();
			return true;
		default:
			return false;
		}
	}

}
