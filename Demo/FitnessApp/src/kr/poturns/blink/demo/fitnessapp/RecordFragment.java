package kr.poturns.blink.demo.fitnessapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.LineGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraph;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraphVO;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;

public class RecordFragment extends SwipeEventFragment {
	private SQLiteHelper mSqLiteHelper;
	private int mCurrentDisplayYear;
	private int mCurrentDisplayMonth;
	private String mCurrentDisplayDbTable = SQLiteHelper.TABLE_PUSH_UP;
	boolean mIsShowingHalfFirstOfData = true;

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
		if (getArguments() == null)
			setDefaultDate();
		else
			setDateFromArguments();

		drawGraph(mCurrentDisplayDbTable, mCurrentDisplayYear,
				mCurrentDisplayMonth);
	}

	@Override
	public void onResume() {
		super.onResume();
		SQLiteHelper.closeDB();
		mSqLiteHelper = SQLiteHelper.getInstance(getActivity());
	}

	private void setDateFromArguments() {
		Bundle arg = getArguments();
		mCurrentDisplayYear = arg.getInt("mCurrentDisplayYear");
		mCurrentDisplayMonth = arg.getInt("mCurrentDisplayMonth");
		mIsShowingHalfFirstOfData = arg.getBoolean("half");
		mCurrentDisplayDbTable = arg.getString("mCurrentDisplayDbTable");
	}

	private void setDefaultDate() {
		Calendar c = Calendar.getInstance();
		mCurrentDisplayYear = c.get(Calendar.YEAR);
		mCurrentDisplayMonth = c.get(Calendar.MONTH) + 1;
		mIsShowingHalfFirstOfData = true;
	}

	private void drawGraph(String tableName, int year, int month) {
		ViewGroup vg = (ViewGroup) getView();
		vg.removeAllViews();
		vg.addView(
				new LineGraphView(getActivity(), getLineGraphVo(tableName,
						year, month)), 0);
		vg.invalidate();
	}

	private void drawGraphWithNewFragment(int animIn, int animOut) {
		Bundle arg = new Bundle();
		arg.putInt("mCurrentDisplayYear", mCurrentDisplayYear);
		arg.putInt("mCurrentDisplayMonth", mCurrentDisplayMonth);
		arg.putBoolean("half", mIsShowingHalfFirstOfData);
		arg.putString("mCurrentDisplayDbTable", mCurrentDisplayDbTable);
		mActivityInterface.attachFragment(new RecordFragment(), arg, animIn,
				animOut);
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case UP_TO_DOWN:
			if (mIsShowingHalfFirstOfData) {
				mCurrentDisplayMonth++;
				if (mCurrentDisplayMonth > 12) {
					mCurrentDisplayMonth = 1;
					mCurrentDisplayYear++;
				}
			}
			drawGraphWithNewFragment(R.animator.slide_in_up,
					R.animator.slide_out_bottom);
			return true;
		case DOWN_TO_UP:
			if (!mIsShowingHalfFirstOfData) {
				mCurrentDisplayMonth--;
				if (mCurrentDisplayMonth < 1) {
					mCurrentDisplayMonth = 12;
					mCurrentDisplayYear--;
				}
			}
			drawGraphWithNewFragment(R.animator.slide_in_bottom,
					R.animator.slide_out_up);
			return true;
		case LEFT_TO_RIGHT:
			// XXX need fix : flow 말고 2d 형식으로 보여주어야 함
			if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP)) {
				mActivityInterface.returnToMain();
				return true;
			} else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP))
				mCurrentDisplayDbTable = SQLiteHelper.TABLE_PUSH_UP;
			else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SQUAT))
				mCurrentDisplayDbTable = SQLiteHelper.TABLE_SIT_UP;
			else if (mCurrentDisplayDbTable
					.equals(SQLiteHelper.TABLE_HEART_BEAT))
				mCurrentDisplayDbTable = SQLiteHelper.TABLE_SQUAT;
			setDefaultDate();
			drawGraphWithNewFragment(R.animator.slide_in_left,
					R.animator.slide_out_right);
			return true;
		case RIGHT_TO_LEFT:
			// XXX need fix : flow 말고 2d 형식으로 보여주어야 함
			if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_PUSH_UP))
				mCurrentDisplayDbTable = SQLiteHelper.TABLE_SIT_UP;
			else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SIT_UP))
				mCurrentDisplayDbTable = SQLiteHelper.TABLE_SQUAT;
			else if (mCurrentDisplayDbTable.equals(SQLiteHelper.TABLE_SQUAT))
				return true;
			// mCurrentDisplayDbTable = SQLiteHelper.TABLE_HEART_BEAT;
			setDefaultDate();
			drawGraphWithNewFragment(R.animator.slide_in_right,
					R.animator.slide_out_left);
			return true;
		default:
			break;
		}
		return false;
	}

	private LineGraphVO getLineGraphVo(String tableName, int year, int month) {
		List<Integer> list = mSqLiteHelper.select(tableName,
				String.valueOf(year), month);
		int size = mIsShowingHalfFirstOfData ? 15 : list.size() - 15;
		int startIndex = mIsShowingHalfFirstOfData ? 0 : 15;
		mIsShowingHalfFirstOfData = !mIsShowingHalfFirstOfData;
		List<LineGraph> arrGraph = new ArrayList<LineGraph>();
		float[] array = new float[size];
		for (int i = 0; i < array.length; i++)
			array[i] = list.get(i + startIndex);
		arrGraph.add(new LineGraph(tableName + "\n" + year + "-" + month,
				getResources().getColor(R.color.main), array));

		String[] legends = new String[size];
		for (int i = 1; i <= size; i++)
			legends[i - 1] = String.valueOf(i + startIndex);
		LineGraphVO vo = new LineGraphVO(legends, arrGraph);

		// set animation
		vo.setAnimation(new GraphAnimation(GraphAnimation.LINEAR_ANIMATION,
				GraphAnimation.DEFAULT_DURATION));
		// set graph name box
		GraphNameBox box = new GraphNameBox();
		box.setNameboxTextSize(50);
		vo.setGraphNameBox(box);

		// set draw graph region
		vo.setDrawRegion(true);
		int max = mSqLiteHelper.selectMax(tableName, String.valueOf(year),
				month);
		vo.setMaxValue(max + 20);
		vo.setIncrement(10);
		vo.setDrawRegion(true);
		return vo;
	}
}
