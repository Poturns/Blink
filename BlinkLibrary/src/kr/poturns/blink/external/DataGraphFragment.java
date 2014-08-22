package kr.poturns.blink.external;

import java.util.List;
import java.util.Random;

import kr.poturns.blink.R;
import kr.poturns.blink.db.SqliteManagerExtended;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.graphview.BubbleGraphView;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraph;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraphVO;

/** 해당 Device의 App의 Measurement Data들을 line graph형태로 보여준다. */
class DataGraphFragment extends Fragment {
	private ViewGroup mGraphView;
	SqliteManagerExtended mManager;

	Device mDevice;
	App mApp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mManager = ((IServiceContolActivity) getActivity())
				.getDatabaseHandler();
		Bundle arg = getArguments();

		mDevice = BundleResolver.obtainDevice(arg);
		mApp = BundleResolver.obtainApp(arg);
		getActivity().getActionBar().setTitle(mDevice.Device);
		getActivity().getActionBar().setSubtitle(mApp.AppName);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentLayout = inflater.inflate(R.layout.fragment_sample_graph,
				container, false);
		mGraphView = (ViewGroup) fragmentLayout
				.findViewById(R.id.fragment_sample_GraphView);
		View graph = makeGraph();
		if (graph != null)
			mGraphView.addView(graph);
		return fragmentLayout;
	}

	private View makeGraph() {
		BubbleGraphVO vo = createBubbleGraphVO();
		if (vo != null)
			return new BubbleGraphView(getActivity(), vo);
		else
			return null;
	}

	private BubbleGraph makeBubbleGraph(Measurement measurement, int color) {
		List<MeasurementData> dataList = mManager
				.obtainMeasurementDataList(measurement);
		int size = dataList.size();
		if (size == 0)
			return null;
		float[] array = new float[size];
		float[] bubbles = new float[size];
		float defValue = 0f;
		for (int i = 0; i < size; i++) {
			array[i] = Float.valueOf(dataList.get(i).Data);
			bubbles[i] = Math.abs(defValue - array[i]) + 100f;
			defValue = array[i];
		}

		return new BubbleGraph(measurement.Description, color, array, bubbles);

	}

	private BubbleGraphVO createBubbleGraphVO() {
		List<Measurement> measurementList = mManager.obtainMesurementList(mApp);
		BubbleGraphVO ret = null;
		if (measurementList.isEmpty())
			return null;
		List<MeasurementData> dataList = mManager
				.obtainMeasurementDataList(measurementList.get(0));
		int size = dataList.size();
		String[] legendArr = new String[size];
		for (int i = 0; i < size; i++) {
			legendArr[i] = dataList.get(i).DateTime.substring(0, 4);
		}
		ret = new BubbleGraphVO(legendArr);
		ret.setAnimationDuration(1000);

		ret.setIsLineShow(true);
		ret.setIsAnimaionShow(true);
		Random random = new Random(System.currentTimeMillis());
		int graphCount = 0;
		for (Measurement measurement : measurementList) {
			int r = random.nextInt(256);
			int g = random.nextInt(256);
			int b = random.nextInt(256);
			BubbleGraph bg = makeBubbleGraph(measurement, Color.rgb(r, g, b));
			if (bg != null) {
				ret.add(bg);
				graphCount++;
			}
		}
		if (graphCount != 0)
			return ret;
		else
			return null;
	}
}
