package kr.poturns.blink.external;

import java.util.ArrayList;
import java.util.Random;

import kr.poturns.blink.R;
import kr.poturns.blink.db.SqliteManagerExtended;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Measurement;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.CircleGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.circlegraph.CircleGraph;
import com.handstudio.android.hzgrapherlib.vo.circlegraph.CircleGraphVO;

/**
 * data graph를 보여주는 Fragment의 container역할을 하는 Fragment <br>
 * 
 * 현재 App의 MeasurementData들이 차지하는 비율을 파이 그래프 형태로 보여준다.
 */
class DataViewFragment extends Fragment {
	Device mDevice;
	App mApp;
	SqliteManagerExtended mManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mManager = ((IServiceContolActivity) getActivity())
				.getDatabaseHandler();

		// 이 fragment에 진입했다는 것은, argument가 존재한다는 의미이다.
		Bundle arg = getArguments();

		mDevice = BundleResolver.obtainDevice(arg);
		mApp = BundleResolver.obtainApp(arg);
	}

	void changeFramgnet(Bundle bundle) {
		Fragment f = new DataGraphFragment();
		f.setArguments(bundle);
		getChildFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, f,
						DataGraphFragment.class.getSimpleName())
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_dataview,
				container, false);
		((ViewGroup) view).addView(new CircleGraphView(getActivity(),
				makePieGraph()));
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDevice != null)
			getActivity().getActionBar().setSubtitle(mDevice.Device);
	}

	@Override
	public void onPause() {
		getActivity().getActionBar().setSubtitle(null);
		super.onPause();
	}

	private CircleGraphVO makePieGraph() {
		ArrayList<CircleGraph> graphItemList = new ArrayList<CircleGraph>();
		Random random = new Random(System.currentTimeMillis());
		for (Measurement measurement : mManager.obtainMesurementList(mApp)) {
			int r = random.nextInt(256);
			int g = random.nextInt(256);
			int b = random.nextInt(256);
			graphItemList.add(new CircleGraph(measurement.Measurement, Color
					.rgb(r, g, b), mManager.obtainMeasurementDataList(
					measurement).size()));
		}

		graphItemList.add(new CircleGraph("android", Color
				.parseColor("#3366CC"), 1));
		graphItemList
				.add(new CircleGraph("ios", Color.parseColor("#DC3912"), 1));
		graphItemList.add(new CircleGraph("tizen", Color.parseColor("#FF9900"),
				1));
		graphItemList.add(new CircleGraph("HTML", Color.parseColor("#109618"),
				1));
		graphItemList.add(new CircleGraph("C", Color.parseColor("#990099"), 3));
		CircleGraphVO vo = new CircleGraphVO(graphItemList);

		// circle Line
		vo.setLineColor(Color.WHITE);

		// set text setting
		vo.setTextColor(Color.WHITE);
		vo.setTextSize(20);

		// set circle center move X ,Y
		vo.setCenterX(0);
		vo.setCenterY(0);

		// set animation
		vo.setAnimation(new GraphAnimation(GraphAnimation.LINEAR_ANIMATION, 10));
		// set graph name box

		vo.setPieChart(true);

		GraphNameBox graphNameBox = new GraphNameBox();

		// nameBox
		graphNameBox.setNameboxMarginTop(25);
		graphNameBox.setNameboxMarginRight(25);

		vo.setGraphNameBox(graphNameBox);

		return vo;
	}
}
