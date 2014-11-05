package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.external.SwipeListener.Direction;
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

interface IServiceContolWatchActivity extends IServiceContolActivity {
	static int POSITION_MAIN = 0;
	static int POSITION_CONNECTION = 1;
	static int POSITION_PREFERENCE = 2;

	void returnToMain(Bundle arguments);
}

/**
 * Watch를 위한 Activity
 * 
 * @author myungjin
 */
// FIXME 추가적으로 Watch를 위한 인터페이스를 구현하는 것은 좋지 않음,
// PreferenceFragment.create(Context); 와 같은 방식으로
// Fragment 객체 생성 시점에 적당한 객체를 반환하도록 구현할 것.
// public static final create(Context context){
// swich(deviceType){
// case handHeld:
// return new PreferenceExternalFragment();
// case watch:
// return new PreferenceWatchFragment();
// .....
//
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
