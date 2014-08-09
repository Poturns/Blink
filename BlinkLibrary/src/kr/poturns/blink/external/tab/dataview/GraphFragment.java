package kr.poturns.blink.external.tab.dataview;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.DeviceAppMeasurement;
import kr.poturns.blink.db.archive.MeasurementData;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.external.DBHelper;
import kr.poturns.blink.external.IServiceContolActivity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.graphview.BubbleGraphView;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraph;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraphVO;

public class GraphFragment extends Fragment {
	private ViewGroup mGraphView;
	DBHelper mHelper;
	SystemDatabaseObject mDBObject;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = DBHelper.getInstance(getActivity());
		Bundle arg = getArguments();
		String device = arg.getString(IServiceContolActivity.EXTRA_DEVICE);
		String app = arg.getString(IServiceContolActivity.EXTRA_DEVICE_APP);
		getActivity().getActionBar().setTitle(device);
		getActivity().getActionBar().setSubtitle(app);
		mDBObject = mHelper.getSystemDatabaseObjectByApp(device, app);
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
		return new BubbleGraphView(getActivity(), createBubbleGraphVO());
	}

	private BubbleGraph makeBubbleGraph(DeviceAppMeasurement mesurement,
			int color) {
		ArrayList<DeviceAppMeasurement> list = new ArrayList<DeviceAppMeasurement>();
		list.add(mesurement);
		List<MeasurementData> dataList = mHelper.getManager()
				.obtainMeasurementData(list, null, null);
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

		return new BubbleGraph(mesurement.Description, color, array, bubbles);

	}

	private BubbleGraphVO createBubbleGraphVO() {
		ArrayList<DeviceAppMeasurement> list = new ArrayList<DeviceAppMeasurement>();
		list.add(mDBObject.mDeviceAppMeasurementList.get(0));
		BubbleGraphVO ret = null;
		List<MeasurementData> dataList = mHelper.getManager()
				.obtainMeasurementData(list, null, null);
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
		for (DeviceAppMeasurement measurement : mDBObject.mDeviceAppMeasurementList) {
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
