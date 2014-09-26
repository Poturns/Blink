package kr.poturns.blink.external;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handstudio.android.hzgrapherlib.graphview.BubbleGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraph;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraphVO;

/**
 * App의 Data들을 graph들로 보여주는 Fragment
 * 
 */
class DataViewFragment extends Fragment {
	Device mDevice;
	App mApp;
	Measurement mMeasurement;
	SqliteManagerExtended mManager;
	PrivateUtil.ViewPagerFragmentProxy fragmentProxy = new PrivateUtil.ViewPagerFragmentProxy() {
		@Override
		protected String[] getTitles() {
			return getResources().getStringArray(
					R.array.res_blink_dialog_data_page_titles);
		}

		@Override
		protected int getViewPagerCount() {
			return 2;
		}

		@Override
		protected Fragment getViewPagerPage(int position) {
			switch (position) {
			case 0:
				if (mManager.obtainMeasurementDataListSize(mMeasurement) == 0) {
					// 불러올 데이터가 없는 경우 측정 리스트 Fragment를 불러오게 한다.
					return new DataMeasurementDataListFragment();
				} else {
					return new DataMeasurementsLineGraphFragment();
				}
			default:
				return new DataMeasurementDataListFragment();
			}
		}

		@Override
		protected FragmentManager getFragmentManager() {
			return DataViewFragment.this.getChildFragmentManager();
		}

		@Override
		protected Activity getActivity() {
			return DataViewFragment.this.getActivity();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mManager = ((IServiceContolActivity) getActivity())
				.getDatabaseHandler();

		// 이 fragment에 진입했다는 것은, argument가 존재한다는 의미이다.
		Bundle arg = getArguments();

		mDevice = PrivateUtil.obtainDevice(arg);
		mApp = PrivateUtil.obtainApp(arg);
		mMeasurement = PrivateUtil.obtainMeasurement(arg);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return fragmentProxy.onCreateView(inflater, container,
				savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		fragmentProxy.onDestroyView();
		super.onDestroyView();
	}

	@Override
	public void onResume() {
		super.onResume();
		String title = mMeasurement.MeasurementName;
		if (title == null || title.length() < 1)
			title = PrivateUtil.obtainSplitMeasurementSchema(mMeasurement);
		getActivity().getActionBar().setTitle(title);
		getActivity().getActionBar().setSubtitle(mMeasurement.Measurement);
	}

	@Override
	public void onPause() {
		super.onPause();
		Bundle arg = getArguments();
		getActivity().getActionBar().setTitle(arg.getCharSequence("title"));
		getActivity().getActionBar().setSubtitle(
				arg.getCharSequence("subTitle"));
	}

	/** 해당 Device의 App의 Measurement의 Data들을 line graph형태로 보여준다. */
	private class DataMeasurementsLineGraphFragment extends Fragment {
		private ViewGroup mGraphView;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View fragmentLayout = inflater
					.inflate(
							R.layout.res_blink_fragment_dataview_measurement_data_graph,
							container, false);
			mGraphView = (ViewGroup) fragmentLayout
					.findViewById(R.id.res_blink_fragment_graph);
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
			List<MeasurementData> dataList = DataViewFragment.this.mManager
					.obtainMeasurementDataList(measurement);
			int size = dataList.size();
			if (size == 0)
				return null;
			float[] array = new float[size];
			float[] bubbles = new float[size];
			for (int i = 0; i < size; i++) {
				array[i] = Float.valueOf(dataList.get(i).Data);
				bubbles[i] = (float) Math.random() * 10f;
			}

			return new BubbleGraph(measurement.MeasurementName, color, array,
					bubbles);
		}

		private BubbleGraphVO createBubbleGraphVO() {
			BubbleGraphVO ret = null;
			List<MeasurementData> dataList = DataViewFragment.this.mManager
					.obtainMeasurementDataList(DataViewFragment.this.mMeasurement);
			int size = dataList.size();
			String[] legendArr = new String[size];
			for (int i = 0; i < size; i++) {
				// yyyy-mm-dd hh:MM:ss 형식으로 된 시간정보에서
				// mm-dd 만 추출
				legendArr[i] = dataList.get(i).DateTime.split(" ")[0]
						.replaceAll("^.*?-", "");
				if (legendArr[i].length() > 6)
					legendArr[i] = legendArr[i].substring(0, 5);
				if (PrivateUtil.isScreenSizeSmall(getActivity())) {
					if (size > 20)
						legendArr[i] = new String(legendArr[i].substring(4));
					else if (size > 10)
						legendArr[i] = new String(legendArr[i].substring(3));
					else if (size > 5)
						legendArr[i] = new String(legendArr[i].substring(1));
				} else {
					if (size > 100)
						legendArr[i] = new String(legendArr[i].substring(4));
					else if (size > 50)
						legendArr[i] = new String(legendArr[i].substring(3));
					else if (size > 30)
						legendArr[i] = new String(legendArr[i].substring(2));
					else if (size > 20)
						legendArr[i] = new String(legendArr[i].substring(1));
				}

			}
			ret = new BubbleGraphVO(legendArr);
			ret.setAnimationDuration(1000);
			ret.setXAxisTextSize(PrivateUtil.isScreenSizeSmall(getActivity()) ? 20
					: 40);
			ret.setYAxisTextSize(PrivateUtil.isScreenSizeSmall(getActivity()) ? 23
					: 40);
			ret.setIsLineShow(true);
			ret.setIsAnimaionShow(true);
			GraphNameBox nameBox = new GraphNameBox();
			nameBox.setNameboxTextSize(30);
			nameBox.setNameboxColor(Color.BLACK);
			ret.setGraphNameBox(nameBox);
			Random random = new Random(System.currentTimeMillis());
			int graphCount = 0;

			int r = random.nextInt(172) + 64;
			int g = random.nextInt(172) + 64;
			int b = random.nextInt(172) + 64;
			BubbleGraph bg = makeBubbleGraph(mMeasurement, Color.rgb(r, g, b));
			if (bg != null) {
				ret.add(bg);
				graphCount++;
			}

			if (graphCount != 0)
				return ret;
			else
				return null;
		}
	}

	/** 해당 Device의 App의 Measurement의 Data들의 목록을 보여준다. */
	private class DataMeasurementDataListFragment extends Fragment {
		List<MeasurementData> mMeasurementDataList;
		ArrayAdapter<MeasurementData> mAdapter;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mMeasurementDataList = new ArrayList<MeasurementData>();
			mMeasurementDataList
					.addAll(DataViewFragment.this.mManager
							.obtainMeasurementDataList(DataViewFragment.this.mMeasurement));
			Collections.sort(mMeasurementDataList,
					new Comparator<MeasurementData>() {
						@Override
						public int compare(MeasurementData lhs,
								MeasurementData rhs) {
							// 시간 기준, 내림차순 정렬
							return -lhs.DateTime.compareTo(rhs.DateTime);
						}
					});
			mAdapter = new ArrayAdapter<MeasurementData>(getActivity(),
					android.R.layout.simple_list_item_2, android.R.id.text1,
					mMeasurementDataList) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View v = super.getView(position, convertView, parent);
					v.setBackgroundResource(R.drawable.res_blink_selector_rectangle_box);
					MeasurementData item = getItem(position);
					((TextView) v.findViewById(android.R.id.text1))
							.setText(item.Data);
					((TextView) v.findViewById(android.R.id.text2))
							.setText(item.DateTime);
					return v;
				}
			};
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(
					R.layout.res_blink_fragment_dataview_measurement_data_list,
					container, false);
			ListView listView = (ListView) v.findViewById(android.R.id.list);
			listView.setAdapter(mAdapter);
			listView.setEmptyView(v.findViewById(android.R.id.empty));
			return v;
		}
	}
}
