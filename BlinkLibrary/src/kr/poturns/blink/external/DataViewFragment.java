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
	Measurement mMeasurement;
	SqliteManagerExtended mManager;
	private TabHost mTabHost;
	private ViewPager mViewPager;
	ArrayList<Fragment> mFragmentList = new ArrayList<Fragment>(3);

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
		// mFragmentList.add(new DataMeasurementsPieFragment());
		mFragmentList.add(new DataMeasurementsLineGraphFragment());
		mFragmentList.add(new DataMeasurementDataListFragment());
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
		mViewPager.setOffscreenPageLimit(mFragmentList.size());
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
				return mFragmentList.size();
			}

			@Override
			public Fragment getItem(int position) {
				return mFragmentList.get(position);
			}
		});
		return v;
	}

	/**
	 * {@link ViewPager}또는 {@link TabHost}의 page를 이동한다.
	 * 
	 * @param position
	 *            움직일 page또는 tab의 인덱스
	 * @param isFromPager
	 *            이동 요청이 {@link ViewPager}로 부터 왔는지 여부
	 */
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
		getActivity().getActionBar().setTitle(mMeasurement.MeasurementName);
		getActivity().getActionBar().setSubtitle(mMeasurement.Type);
	}

	@Override
	public void onPause() {
		super.onPause();
		Bundle arg = getArguments();
		getActivity().getActionBar().setTitle(arg.getCharSequence("title"));
		getActivity().getActionBar().setSubtitle(
				arg.getCharSequence("subTitle"));
	}

	@Override
	public void onDestroyView() {
		mTabHost = null;
		mViewPager = null;
		super.onDestroyView();
	}

	/** 현재 App의 MeasurementData들이 차지하는 비율을 파이 그래프 형태로 보여준다. */
	private class DataMeasurementsPieFragment extends Fragment {
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

		private CircleGraphVO makePieGraph() {
			ArrayList<CircleGraph> graphItemList = new ArrayList<CircleGraph>();
			Random random = new Random(System.currentTimeMillis());
			Measurement measurement = DataViewFragment.this.mMeasurement;
			int r = random.nextInt(128) + 128;
			int g = random.nextInt(128) + 128;
			int b = random.nextInt(128) + 128;
			graphItemList.add(new CircleGraph(measurement.MeasurementName,
					Color.rgb(r, g, b), DataViewFragment.this.mManager
							.obtainMeasurementDataListSize(measurement)));

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

	/** 해당 Device의 App의 Measurement의 Data들을 line graph형태로 보여준다. */
	private class DataMeasurementsLineGraphFragment extends Fragment {
		private ViewGroup mGraphView;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View fragmentLayout = inflater.inflate(R.layout.fragment_graph,
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
			List<MeasurementData> dataList = DataViewFragment.this.mManager
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
			BubbleGraphVO ret = null;
			List<MeasurementData> dataList = DataViewFragment.this.mManager
					.obtainMeasurementDataList(DataViewFragment.this.mMeasurement);
			int size = dataList.size();
			String[] legendArr = new String[size];
			for (int i = 0; i < size; i++) {
				legendArr[i] = dataList.get(i).DateTime.split(" ")[0]
						.replaceAll("^.*?-", "");
				if (legendArr[i].length() > 6)
					legendArr[i] = legendArr[i].substring(0, 5);
			}
			ret = new BubbleGraphVO(legendArr);
			ret.setAnimationDuration(1000);

			ret.setIsLineShow(true);
			ret.setIsAnimaionShow(true);
			Random random = new Random(System.currentTimeMillis());
			int graphCount = 0;

			int r = random.nextInt(128) + 128;
			int g = random.nextInt(128) + 128;
			int b = random.nextInt(128) + 128;
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
					R.layout.dialog_fragment_connection_db_info, container,
					false);
			ListView listView = (ListView) v.findViewById(android.R.id.list);
			listView.setAdapter(mAdapter);
			listView.setEmptyView(v.findViewById(android.R.id.empty));
			return v;
		}
	}
}
