package kr.poturns.blink.demo.fitnessapp;

import java.util.ArrayList;
import java.util.Calendar;

import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.BarGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.bargraph.BarGraph;
import com.handstudio.android.hzgrapherlib.vo.bargraph.BarGraphVO;

/**
 * 기록된 운동량을 그래프로 보여주는 Fragment
 * 
 * @author Myungjin.Kim
 */
public class RecordFragment extends SwipeEventFragment {
	private SQLiteHelper mSqLiteHelper;
	String mCurrentDisplayDbTable = SQLiteHelper.TABLE_PUSH_UP;
	int mYear, mMonth, mDay;
	private boolean mShowCalorie = false;
	FragmentGridPagerAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH) + 1;
		mDay = c.get(Calendar.DATE);
		if (getArguments() != null)
			setDateFromArguments();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mSqLiteHelper = SQLiteHelper.getInstance(getActivity());
		View v = inflater.inflate(R.layout.fragment_record, container, false);
		final GridViewPager pager = (GridViewPager) v
				.findViewById(R.id.fragment_record_pager);
		pager.setAdapter(new GridViewPagerAdapter(getChildFragmentManager()));
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		SQLiteHelper.closeDB();
		mSqLiteHelper = SQLiteHelper.getInstance(getActivity());
	}

	private void setDateFromArguments() {
		Bundle arg = getArguments();
		mCurrentDisplayDbTable = arg.getString("mCurrentDisplayDbTable");
		mShowCalorie = arg.getBoolean("calorie");
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT:
			if (!mShowCalorie)
				mActivityInterface.returnToMain();
			return true;
		default:
			return false;
		}
	}

	class GridViewPagerAdapter extends FragmentGridPagerAdapter {
		private int mCurrentRow;

		public GridViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getFragment(int row, int column) {
			RecordFragment.this.mShowCalorie = column == 0 ? false : true;
			if (row >= mCurrentRow) {
				switch (row) {
				case 0:
					RecordFragment.this.mCurrentDisplayDbTable = SQLiteHelper.TABLE_SQUAT;
					break;
				case 1:
					RecordFragment.this.mCurrentDisplayDbTable = SQLiteHelper.TABLE_PUSH_UP;
					break;
				default:
					RecordFragment.this.mCurrentDisplayDbTable = SQLiteHelper.TABLE_SIT_UP;
					break;
				}
			} else {
				switch (row) {
				case 0:
					RecordFragment.this.mCurrentDisplayDbTable = SQLiteHelper.TABLE_SIT_UP;
					break;
				case 1:
					RecordFragment.this.mCurrentDisplayDbTable = SQLiteHelper.TABLE_PUSH_UP;
					break;
				default:
					RecordFragment.this.mCurrentDisplayDbTable = SQLiteHelper.TABLE_SQUAT;
					break;
				}
			}

			mCurrentRow = row;
			return new InnerGraphFragment();
		}

		@Override
		public int getColumnCount(int row) {
			return 2;
		}

		@Override
		public int getRowCount() {
			return 3;
		}

	}

	class InnerGraphFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater
					.inflate(
							R.layout.res_blink_fragment_dataview_measurement_data_graph,
							container, false);
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			drawGraph(RecordFragment.this.mCurrentDisplayDbTable);
		}

		private void drawGraph(String tableName) {
			ViewGroup vg = (ViewGroup) getView();
			vg.removeAllViews();
			vg.addView(
					new BarGraphView(getActivity(), getBarGraphVO(tableName)),
					0);
			vg.invalidate();
		}

		private BarGraphVO getBarGraphVO(String tableName) {
			BarGraphVO vo = null;
			ArrayList<Float> list = new ArrayList<Float>();
			int prevYear = RecordFragment.this.mYear;
			int prevMonth = RecordFragment.this.mMonth;
			int prevDay = RecordFragment.this.mDay - 1;

			if (prevDay < 1) {
				prevMonth--;
				if (prevMonth < 1) {
					prevYear--;
					prevMonth = 12;
				}
				prevDay = SQLiteHelper.getDayOfMonth(prevMonth);
			}
			int prevCount = RecordFragment.this.mSqLiteHelper.select(tableName,
					String.valueOf(prevYear), prevMonth, prevDay);
			list.add(RecordFragment.this.mShowCalorie ? (float) FitnessUtil
					.calculateCalorie(tableName, prevCount) : prevCount);
			int currentCount = RecordFragment.this.mSqLiteHelper.select(
					tableName, String.valueOf(RecordFragment.this.mYear),
					RecordFragment.this.mMonth, RecordFragment.this.mDay);
			list.add(RecordFragment.this.mShowCalorie ? (float) FitnessUtil
					.calculateCalorie(tableName, currentCount) : currentCount);
			String[] legendArr = { "어제", "오늘" };

			float[] array = new float[] { list.get(0), list.get(1) };
			String title = "";
			if (tableName.equals(SQLiteHelper.TABLE_PUSH_UP))
				title = "팔굽혀펴기";
			else if (tableName.equals(SQLiteHelper.TABLE_SIT_UP))
				title = "윗몸일으키기";
			else if (tableName.equals(SQLiteHelper.TABLE_SQUAT))
				title = "스쿼트";
			if (RecordFragment.this.mShowCalorie)
				title += " (KCal)";

			int color;
			if (RecordFragment.this.mCurrentDisplayDbTable
					.equals(SQLiteHelper.TABLE_PUSH_UP)) {
				color = getResources().getColor(R.color.orange);
			} else if (RecordFragment.this.mCurrentDisplayDbTable
					.equals(SQLiteHelper.TABLE_SIT_UP)) {
				color = getResources().getColor(R.color.green);
			} else if (RecordFragment.this.mCurrentDisplayDbTable
					.equals(SQLiteHelper.TABLE_SQUAT)) {
				color = getResources().getColor(R.color.blue);
			} else {
				color = getResources().getColor(R.color.orange);
			}

			ArrayList<BarGraph> arrGraph = new ArrayList<BarGraph>();
			arrGraph.add(new BarGraph(title, color, array));

			vo = new BarGraphVO();
			vo.setLegendArr(legendArr);
			vo.setArrGraph(arrGraph);
			vo.setBarWidth(10);
			int max = (int) (Math.max(list.get(0), list.get(1)) + 10);
			vo.setMaxValueX(10);
			vo.setMaxValueY(max);
			int increment = (int) Math.abs(list.get(0) - list.get(1));
			vo.setIncrementX(4);
			vo.setIncrementY(increment < 1 ? 10 : increment);
			vo.setGraphNameBox(new GraphNameBox());
			vo.setAnimation(new GraphAnimation(GraphAnimation.LINEAR_ANIMATION,
					GraphAnimation.DEFAULT_DURATION));
			vo.setAnimationShow(true);
			vo.setXAxisTextSize(10);
			vo.setYAxisTextSize(10);
			GraphNameBox box = new GraphNameBox();
			box.setNameboxTextSize(10);
			box.setNameboxColor(Color.BLACK);
			box.setNameboxPadding(1);
			vo.setGraphNameBox(box);
			return vo;
		}
	}
}
