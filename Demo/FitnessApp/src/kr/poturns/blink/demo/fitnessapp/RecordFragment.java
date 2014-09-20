package kr.poturns.blink.demo.fitnessapp;

import java.util.ArrayList;
import java.util.Calendar;

import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.BarGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.bargraph.BarGraph;
import com.handstudio.android.hzgrapherlib.vo.bargraph.BarGraphVO;

public class RecordFragment extends SwipeEventFragment {
	private SQLiteHelper mSqLiteHelper;
	private String mCurrentDisplayDbTable = SQLiteHelper.TABLE_PUSH_UP;
	private int mYear;
	private int mMonth;
	private int mDay;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mSqLiteHelper = SQLiteHelper.getInstance(getActivity());
		View v = inflater.inflate(
				R.layout.res_blink_fragment_dataview_measurement_data_graph,
				container, false);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH) + 1;
		mDay = c.get(Calendar.DATE);
		if (getArguments() == null)
			setDefaultDate();
		else
			setDateFromArguments();

		drawGraph(mCurrentDisplayDbTable);
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
	}

	private void setDefaultDate() {
	}

	private void drawGraph(String tableName) {
		ViewGroup vg = (ViewGroup) getView();
		vg.removeAllViews();
		vg.addView(new BarGraphView(getActivity(), getBarGraphVO(tableName)), 0);
		vg.invalidate();
	}

	private void drawGraphWithNewFragment(int animIn, int animOut) {
		Bundle arg = new Bundle();
		arg.putString("mCurrentDisplayDbTable", mCurrentDisplayDbTable);
		mActivityInterface.attachFragment(new RecordFragment(), arg, animIn,
				animOut);
	}

	/**
	 * 구성
	 * 
	 * <br>
	 * SITUP <br>
	 * | <br>
	 * pushup - heartbeat(예정)<br>
	 * | <br>
	 * SQUAT
	 */
	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case UP_TO_DOWN:
			if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP))
				mCurrentDisplayDbTable = SQLiteHelper.TABLE_SIT_UP;
			else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SQUAT))
				mCurrentDisplayDbTable = SQLiteHelper.TABLE_PUSH_UP;
			else
				return false;
			drawGraphWithNewFragment(R.animator.slide_in_up,
					R.animator.slide_out_bottom);
			return true;
		case DOWN_TO_UP:
			if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP))
				mCurrentDisplayDbTable = SQLiteHelper.TABLE_SQUAT;
			else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP))
				mCurrentDisplayDbTable = SQLiteHelper.TABLE_PUSH_UP;
			else
				return false;
			// mCurrentDisplayDbTable = SQLiteHelper.TABLE_HEART_BEAT;
			drawGraphWithNewFragment(R.animator.slide_in_bottom,
					R.animator.slide_out_up);
			return true;
		case LEFT_TO_RIGHT:
			mActivityInterface.returnToMain();
			return true;
		default:
			return false;
		}
	}

	private BarGraphVO getBarGraphVO(String tableName) {
		BarGraphVO vo = null;
		ArrayList<Integer> list = new ArrayList<Integer>();
		int prevYear = mYear;
		int prevMonth = mMonth;
		int prevDay = mDay - 1;

		if (prevDay < 1) {
			prevMonth--;
			if (prevMonth < 1) {
				prevYear--;
				prevMonth = 12;
			}
			prevDay = SQLiteHelper.getDayOfMonth(prevMonth);
		}

		list.add(mSqLiteHelper.select(tableName, String.valueOf(prevYear),
				prevMonth, prevDay));
		list.add(mSqLiteHelper.select(tableName, String.valueOf(mYear), mMonth,
				mDay));
		String[] legendArr = { "어제", "오늘" };

		float[] array = new float[] { list.get(0), list.get(1) };
		String title = "";
		if (tableName.equals(SQLiteHelper.TABLE_PUSH_UP))
			title = "팔굽혀펴기";
		else if (tableName.equals(SQLiteHelper.TABLE_SIT_UP))
			title = "윗몸일으키기";
		else if (tableName.equals(SQLiteHelper.TABLE_SQUAT))
			title = "스쿼트";
		int color = getResources().getColor(R.color.main);

		ArrayList<BarGraph> arrGraph = new ArrayList<BarGraph>();
		arrGraph.add(new BarGraph(title, color, array));

		vo = new BarGraphVO();
		vo.setLegendArr(legendArr);
		vo.setArrGraph(arrGraph);
		vo.setBarWidth(30);
		int max = Math.max(list.get(0), list.get(1)) + 10;
		vo.setMaxValueX(10);
		vo.setMaxValueY(max);
		int increment = Math.abs(list.get(0) - list.get(1));
		vo.setIncrementX(4);
		vo.setIncrementY(increment);
		vo.setGraphNameBox(new GraphNameBox());
		vo.setAnimation(new GraphAnimation(GraphAnimation.LINEAR_ANIMATION,
				GraphAnimation.DEFAULT_DURATION));
		vo.setAnimationShow(true);
		vo.setTextX(50);
		vo.setTextY(30);
		GraphNameBox box = new GraphNameBox();
		box.setNameboxColor(color);
		box.setNameboxTextSize(60);
		box.setNameboxColor(Color.BLACK);
		box.setNameboxPadding(20);
		vo.setGraphNameBox(box);
		return vo;
	}
}
