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
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
						if (Math.abs(e1.getX() - e2.getX()) < 100) {
							// 아래서 위로 스크롤 하는 경우
							if (e1.getY() - e2.getY() > 50) {
								direction = Direction.UP_TO_DOWN;
								// 위에서 아래로 스크롤
							} else if (e2.getY() - e1.getY() > 50) {
								direction = Direction.DOWN_TO_UP;
							}
							// 세로로 움직인 폭이 일정 이상이면 무시
						} else if (Math.abs(e1.getY() - e2.getY()) < 100) {
							if (e1.getX() - e2.getX() > 50) {
								direction = Direction.RIGHT_TO_LEFT;
							} else if (e2.getX() - e1.getX() > 50) {
								direction = Direction.LEFT_TO_RIGHT;
							}
						}

						if (mChildObject != null && direction != null) {
							return mChildObject.onSwipe(direction);
						}
						return false;
					}
				});

		// View 설정 & 화면 크기 제한
		View container = findViewById(R.id.root).findViewById(R.id.container);

		RelativeLayout.LayoutParams layoutParam = (RelativeLayout.LayoutParams) container
				.getLayoutParams();

		layoutParam.addRule(RelativeLayout.CENTER_IN_PARENT,
				RelativeLayout.TRUE);

		int pageColumnMargin = getResources().getDimensionPixelSize(
				R.dimen.page_column_margin);
		int pageRowMargin = getResources().getDimensionPixelSize(
				R.dimen.page_row_margin);
		Point size = new Point();
		getWindow().getWindowManager().getDefaultDisplay().getSize(size);
		// 화면의 가로/세로 중 작은쪽의 크기에 맞춰 정사각형 형태의 View를 생성
		if (size.x < size.y)
			layoutParam.height = layoutParam.width = size.x - pageColumnMargin;
		else
			layoutParam.width = layoutParam.height = size.y - pageRowMargin;
		container.setLayoutParams(layoutParam);

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
		View content = View.inflate(this, R.layout.main_inbody_alert, null);

		final AlertDialog dialog = new AlertDialog.Builder(this)
				.setView(content).setCancelable(true).create();
		dialog.show();
		content.findViewById(R.id.button_load).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
						Bundle b = new Bundle();
						b.putBoolean("hasNotInbody", true);
						attachFragment(new InBodyFragment(), b);
					}
				});

		// 5초 뒤 다이얼로그 종료
		content.postDelayed(new Runnable() {

			@Override
			public void run() {
				dialog.dismiss();
			}
		}, 5000);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mGestureDetector.onTouchEvent(ev))
			return true;
		else
			return super.dispatchTouchEvent(ev);
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
		if (fragment instanceof IInternalEventCallback) {
			mInteraction
					.setIInternalEventCallback((IInternalEventCallback) fragment);
		} else {
			mInteraction.setIInternalEventCallback(null);
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

	/** 웨어러블 메인 화면 */
	public static class HomeFragment extends SwipeEventFragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.fragment_home, container, false);
			ListView listView = (ListView) v.findViewById(android.R.id.list);
			listView.setAdapter(new ArrayAdapter<String>(getActivity(),
					R.layout.list_home, android.R.id.text1, getResources()
							.getStringArray(R.array.title_entry)) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View v = super.getView(position, convertView, parent);
					TextView tv = (TextView) v.findViewById(android.R.id.text1);
					switch (position) {
					case 0:
						tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
								R.drawable.ic_action_social_person, 0, 0, 0);
						break;
					case 1:
						tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
								R.drawable.ic_action_action_dumbbell, 0, 0, 0);
						break;
					case 2:
						tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
								R.drawable.ic_action_statistics, 0, 0, 0);
						break;
					case 3:
						tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
								R.drawable.ic_action_image_camera, 0, 0, 0);
						break;
					case 4:
						tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
								R.drawable.ic_action_setup, 0, 0, 0);
						break;
					}
					return v;
				}
			});
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					switch (position) {
					case 0:
						mActivityInterface.attachFragment(new InBodyFragment(),
								null);
						break;
					case 1:
						mActivityInterface.attachFragment(
								new FitnessFragment(), null);
						break;
					case 2:
						mActivityInterface.attachFragment(new RecordFragment(),
								null);
						break;
					case 3:
						mActivityInterface.attachFragment(
								new FunctionTestFragment(), null);
						break;
					case 4:
						mActivityInterface.attachFragment(
								new SettingFragment(), null);
						break;
					default:
						break;
					}
				}
			});
			return v;
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
}
