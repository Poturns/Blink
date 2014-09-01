package kr.poturns.blink.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Measurement;
import kr.poturns.blink.db.archive.MeasurementData;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.BubbleGraphView;
import com.handstudio.android.hzgrapherlib.graphview.CircleGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraph;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraphVO;
import com.handstudio.android.hzgrapherlib.vo.circlegraph.CircleGraph;
import com.handstudio.android.hzgrapherlib.vo.circlegraph.CircleGraphVO;

/**
 * App의 Data들을 graph들로 보여주는 Fragment
 * 
 */
class DataViewFragment extends Fragment {
	Device mDevice;
	App mApp;
	SqliteManagerExtended mManager;
	private TabHost mTabHost;
	private ViewPager mViewPager;

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(
				R.layout.dialog_fragment_connection_device_info, container,
				false);
		mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);
		mTabHost.setup();
		TabHost.TabContentFactory factory = new TabHost.TabContentFactory() {

			@Override
			public View createTabContent(String tag) {
				View v = new View(getActivity());
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				v.setTag(tag);
				return v;
			}
		};
		final String[] pageTitles = getResources().getStringArray(
				R.array.dialog_data_page_titles);
		int i = 0;
		for (String title : pageTitles) {
			mTabHost.addTab(mTabHost.newTabSpec(String.valueOf(i++))
					.setIndicator(title).setContent(factory));
		}

		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				navigateTab(Integer.valueOf(tabId), false);
			}
		});
		mViewPager = (ViewPager) v
				.findViewById(R.id.dialog_deviceinfo_viewpager);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						navigateTab(position, true);
					}
				});
		mViewPager.setAdapter(new FragmentPagerAdapter(this
				.getChildFragmentManager()) {

			@Override
			public int getCount() {
				return 3;
			}

			@Override
			public Fragment getItem(int position) {
				switch (position) {
				case 2:
					return new DataMeasurementListFragment();
				case 1:
					return new DataMeasurementsLineGraphFragment();
				default:
					return new DataMeasurementsPieFragment();
				}
			}
		});
		return v;
	}

	protected void navigateTab(int position, boolean isFromPager) {
		if (isFromPager) {
			mTabHost.setCurrentTab(position);
		} else {
			mViewPager.setCurrentItem(position);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().getActionBar().setTitle(mDevice.Device);
		getActivity().getActionBar().setSubtitle(mApp.AppName);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().getActionBar().setTitle(null);
		getActivity().getActionBar().setSubtitle(null);
	}

	/** 현재 App의 MeasurementData들이 차지하는 비율을 파이 그래프 형태로 보여준다. */
	class DataMeasurementsPieFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			final View view = inflater.inflate(R.layout.fragment_dataview,
					container, false);
			CircleGraphVO vo = makePieGraph();
			if (vo != null)
				((ViewGroup) view).addView(new CircleGraphView(getActivity(),
						vo));
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
				graphItemList.add(new CircleGraph(measurement.Measurement,
						Color.rgb(r, g, b), mManager.obtainMeasurementDataList(
								measurement).size()));
			}
			if (graphItemList.isEmpty())
				return null;
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
			vo.setAnimation(new GraphAnimation(GraphAnimation.LINEAR_ANIMATION,
					10));
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

	/** 해당 Device의 App의 Measurement Data들을 line graph형태로 보여준다. */
	class DataMeasurementsLineGraphFragment extends Fragment {
		private ViewGroup mGraphView;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mManager = ((IServiceContolActivity) getActivity())
					.getDatabaseHandler();
			Bundle arg = getArguments();

			mDevice = BundleResolver.obtainDevice(arg);
			mApp = BundleResolver.obtainApp(arg);

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View fragmentLayout = inflater.inflate(
					R.layout.fragment_sample_graph, container, false);
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

			return new BubbleGraph(measurement.Description, color, array,
					bubbles);

		}

		private BubbleGraphVO createBubbleGraphVO() {
			List<Measurement> measurementList = mManager
					.obtainMesurementList(mApp);
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
				BubbleGraph bg = makeBubbleGraph(measurement,
						Color.rgb(r, g, b));
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

	class DataMeasurementListFragment extends Fragment {
		List<Measurement> mMeasurementList;
		ArrayAdapter<Measurement> mAdapter;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mMeasurementList = new ArrayList<Measurement>();
			mMeasurementList.addAll(mManager.obtainMesurementList(mApp));
			mAdapter = new ArrayAdapter<Measurement>(getActivity(),
					android.R.layout.simple_list_item_1, mMeasurementList) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View v = super.getView(position, convertView, parent);
					Measurement item = getItem(position);
					((TextView) v.findViewById(android.R.id.text1))
							.setText(item.Measurement);
					return v;
				}
			};
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.dialog_fragment_connection_db_info,
					container, false);
			ListView listView = (ListView) v.findViewById(android.R.id.list);
			listView.setAdapter(mAdapter);
			listView.setEmptyView(v.findViewById(android.R.id.empty));
			return v;
		}
	}
}
