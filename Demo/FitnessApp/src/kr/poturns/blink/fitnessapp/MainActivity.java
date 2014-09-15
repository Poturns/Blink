package kr.poturns.blink.fitnessapp;

import java.util.Random;

import kr.poturns.blink.fitnessapp.MainActivity.SwipeListener.Direction;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Point;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends Activity {
	View mContentView;
	SwipeListener mDirectionListener;
	BlinkServiceInteraction mInteraction;
	IInternalOperationSupport mISupport;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
			}
		};
		mInteraction.startBroadcastReceiver();
		mInteraction.startService();
		final GestureDetector gd = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {

						// 가로로 움직인 폭이 일정 이상이면 무시
						if (Math.abs(e1.getX() - e2.getX()) < 100) {
							// 아래서 위로 스크롤 하는 경우
							if (e1.getY() - e2.getY() > 50) {
								mDirectionListener.onSwipe(Direction.UP);
								return true;
								// 위에서 아래로 스크롤
							} else if (e2.getY() - e1.getY() > 50) {
								mDirectionListener.onSwipe(Direction.DOWN);
								return true;
							}
							// 세로로 움직인 폭이 일정 이상이면 무시
						} else if (Math.abs(e1.getY() - e2.getY()) < 100) {
							if (e1.getX() - e2.getX() > 50) {
								mDirectionListener.onSwipe(Direction.LEFT);
								return true;
							} else if (e2.getX() - e1.getX() > 50) {
								mDirectionListener.onSwipe(Direction.RIGHT);
								return true;
							}
						}
						return false;
					}
				});
		getWindow().getDecorView().setOnTouchListener(
				new View.OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return gd.onTouchEvent(event);
					}
				});

		mContentView = findViewById(android.R.id.content);

		ViewGroup.LayoutParams lp = mContentView.getLayoutParams();
		// lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		Point size = new Point();
		getWindow().getWindowManager().getDefaultDisplay().getSize(size);
		// 화면의 가로/세로 중 작은쪽의 크기에 맞춰 정사각형 형태의 View를 생성
		if (size.x < size.y)
			lp.height = size.x;
		else
			lp.width = size.y;

		mContentView.setLayoutParams(lp);

		attachFragment(new SampleFragment());
	}

	@Override
	protected void onDestroy() {
		mInteraction.stopService();
		super.onDestroy();
	}

	/** 현재 화면을 해당 Fragment로 바꾼다. */
	void attachFragment(SwipeEventFragment fragment) {
		this.mDirectionListener = fragment;
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();
	}

	/** Swipe 모션 이벤트를 받는 리스너 */
	public static interface SwipeListener {
		/** Swipe 방향 */
		public enum Direction {
			UP, DOWN, LEFT, RIGHT
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

	public static abstract class SwipeEventFragment extends Fragment implements
			SwipeListener {
	}

	class SampleFragment extends SwipeEventFragment {
		Random random = new Random(System.currentTimeMillis());
		int index = 5;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.fragment_main, container, false);
			v.setBackground(getResources().getDrawable(
					R.drawable.debug_background_5));
			v.findViewById(android.R.id.button1).setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								mISupport.openControlActivity();
							} catch (RemoteException e) {
								Toast.makeText(
										MainActivity.this,
										"Service Remote Exception : "
												+ e.getMessage(), 1000).show();
							} catch (NullPointerException e) {
								Toast.makeText(MainActivity.this,
										"Service had not bind yet.", 1000)
										.show();
							}
						}
					});
			return v;
		}

		@Override
		public boolean onSwipe(Direction direction) {
			int rand;
			while ((rand = random.nextInt(4) + 1) == index)
				;
			index = rand;
			int drawableID;
			switch (rand) {
			default:
			case 1:
				drawableID = R.drawable.debug_background_1;
				break;
			case 2:
				drawableID = R.drawable.debug_background_2;
				break;
			case 3:
				drawableID = R.drawable.debug_background_3;
				break;
			case 4:
				drawableID = R.drawable.debug_background_4;
				break;
			case 5:
				drawableID = R.drawable.debug_background_5;
				break;

			}
			getView().setBackground(getResources().getDrawable(drawableID));
			return false;

		}
	}
}
