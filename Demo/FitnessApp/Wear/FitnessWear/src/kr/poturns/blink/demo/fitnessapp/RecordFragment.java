package kr.poturns.blink.demo.fitnessapp;

import java.util.Calendar;

import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import kr.poturns.blink.demo.fitnesswear.R;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.ImageReference;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 기록된 운동량을 보여주는 Fragment
 * 
 * @author Myungjin.Kim
 */
public class RecordFragment extends SwipeEventFragment {
	private SQLiteHelper mSqLiteHelper;
	int mYear, mMonth, mDay;
	GridViewPagerAdapter mAdapter;
	GridViewPager mPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH) + 1;
		mDay = c.get(Calendar.DATE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mSqLiteHelper = SQLiteHelper.getInstance(getActivity());
		View v = inflater.inflate(R.layout.fragment_record, container, false);
		mPager = (GridViewPager) v.findViewById(R.id.fragment_record_pager);
		mAdapter = new GridViewPagerAdapter(getChildFragmentManager());
		mPager.setAdapter(mAdapter);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		SQLiteHelper.closeDB();
		mSqLiteHelper = SQLiteHelper.getInstance(getActivity());
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT:
			if (mPager.getCurrentItem().x == 0) {
				mActivityInterface.returnToMain();
				return true;
			}
			return false;
		default:
			return false;
		}
	}

	class GridViewPagerAdapter extends FragmentGridPagerAdapter {
		/** 현재 날짜에서 하루 전 날짜 */
		private int prevYear, prevMonth, prevDay;

		public GridViewPagerAdapter(FragmentManager fm) {
			super(fm);
			prevYear = RecordFragment.this.mYear;
			prevMonth = RecordFragment.this.mMonth;
			prevDay = RecordFragment.this.mDay - 1;

			if (prevDay < 1) {
				prevMonth--;
				if (prevMonth < 1) {
					prevYear--;
					prevMonth = 12;
				}
				prevDay = SQLiteHelper.getDayOfMonth(prevMonth);
			}
		}

		@Override
		public Fragment getFragment(int row, int column) {
			boolean showCalorie = column == 0 ? false : true;
			String table;

			switch (row) {
			case 0:
				table = SQLiteHelper.TABLE_SQUAT;
				break;
			case 1:
				table = SQLiteHelper.TABLE_PUSH_UP;
				break;
			default:
				table = SQLiteHelper.TABLE_SIT_UP;
				break;
			}

			int prevCount = RecordFragment.this.mSqLiteHelper.select(table,
					String.valueOf(prevYear), prevMonth, prevDay);
			float prevData = showCalorie ? (float) FitnessUtil
					.calculateCalorie(table, prevCount) : prevCount;
			int currentCount = RecordFragment.this.mSqLiteHelper.select(table,
					String.valueOf(RecordFragment.this.mYear),
					RecordFragment.this.mMonth, RecordFragment.this.mDay);
			float currentData = showCalorie ? (float) FitnessUtil
					.calculateCalorie(table, currentCount) : currentCount;

			//int color;
			int iconRes;
			if (table.equals(SQLiteHelper.TABLE_PUSH_UP)) {
				//color = getResources().getColor(R.color.orange);
				iconRes = R.drawable.ic_action_health_push_up;
			} else if (table.equals(SQLiteHelper.TABLE_SIT_UP)) {
				//color = getResources().getColor(R.color.green);
				iconRes = R.drawable.ic_action_health_sit_up;
			} else if (table.equals(SQLiteHelper.TABLE_SQUAT)) {
				//color = getResources().getColor(R.color.blue);
				iconRes = R.drawable.ic_action_health_squat;
			} else {
				//color = getResources().getColor(R.color.orange);
				iconRes = R.drawable.ic_action_health_push_up;
			}
			String title = table;
			if (showCalorie)
				title += " (KCal)";
			String text = "어제 : " + prevData + "\n오늘 : " + currentData;
			CardFragment card = CardFragment.create(title, text, iconRes);
			card.setCardGravity(Gravity.CENTER);
			card.setExpansionEnabled(true);
			card.setExpansionDirection(CardFragment.EXPAND_UP);
			card.setExpansionFactor(1.5f);
			return card;
		}

		@Override
		public int getColumnCount(int row) {
			return 2;
		}

		@Override
		public int getRowCount() {
			return 3;
		}

		@Override
		public ImageReference getBackground(int row, int column) {
			return ImageReference.forDrawable(IMAGES[row % IMAGES.length]);
		}

	}

	static final int[] IMAGES = { R.drawable.image_balance,
			R.drawable.image_barbell, R.drawable.image_gym };
}
