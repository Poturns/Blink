package kr.poturns.blink.fitnessapp;

import kr.poturns.blink.fitnessapp.MainActivity.DirectionEventListener.Direction;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
	View mView;
	MotionEventFragment mCurrentFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final GestureDetector gd = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {

						// 가로로 움직인 폭이 일정 이상이면 무시
						if (Math.abs(e1.getX() - e2.getX()) < 100) {
							// 아래서 위로 스크롤 하는 경우
							if (e1.getY() - e2.getY() > 50) {
								mCurrentFragment.onDirectionEvent(Direction.UP);
								return true;
								// 위에서 아래로 스크롤
							} else if (e2.getY() - e1.getY() > 50) {
								mCurrentFragment
										.onDirectionEvent(Direction.DOWN);
								return true;
							}
							// 세로로 움직인 폭이 일정 이상이면 무시
						} else if (Math.abs(e1.getY() - e2.getY()) < 100) {
							if (e1.getX() - e2.getX() > 50) {
								mCurrentFragment
										.onDirectionEvent(Direction.LEFT);
								return true;
							} else if (e2.getX() - e1.getX() > 50) {
								mCurrentFragment
										.onDirectionEvent(Direction.RIGHT);
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

		mView = findViewById(android.R.id.content);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		Point size = new Point();
		getWindow().getWindowManager().getDefaultDisplay().getSize(size);
		// 화면의 가로/세로 중 작은쪽의 크기에 맞춰 정사각형 형태의 View를 생성
		if (size.x < size.y)
			lp.height = size.x;
		else
			lp.width = size.y;

		LinearLayout.LayoutParams ipp = new LinearLayout.LayoutParams(lp);
		mView.setLayoutParams(ipp);
		attachFragment(new SampleFragment());
	}

	void attachFragment(MotionEventFragment fragment) {
		this.mCurrentFragment = fragment;
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();
	}

	public static interface DirectionEventListener {
		public enum Direction {
			UP, DOWN, LEFT, RIGHT
		}

		public boolean onDirectionEvent(Direction direction);
	}

	public static abstract class MotionEventFragment extends Fragment implements
			DirectionEventListener {

	}

	class SampleFragment extends MotionEventFragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_sample, container, false);
		}

		@Override
		public boolean onDirectionEvent(Direction direction) {
			switch (direction) {
			case DOWN:
				getView().setBackgroundColor(Color.BLACK);
				return true;
			case LEFT:
				getView().setBackgroundColor(Color.BLUE);
				return true;
			case UP:
				getView().setBackgroundColor(Color.GRAY);
				return true;
			case RIGHT:
				getView().setBackgroundColor(Color.CYAN);
				return true;
			default:
				return false;
			}
		}
	}
}
