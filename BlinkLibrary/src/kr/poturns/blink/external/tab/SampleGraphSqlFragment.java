package kr.poturns.blink.external.tab;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.db.SqliteManager;
import kr.poturns.blink.schema.Eye;
import kr.poturns.blink.R;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

public class SampleGraphSqlFragment extends Fragment {
	private static final String TAG = SampleGraphSqlFragment.class
			.getSimpleName();
	private ViewGroup mGraphView;
	private SqliteManager mSqliteManager;
	private SystemDatabaseObject mSystemDatabaseObject;
	private SharedPreferences mSharedPreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSqliteManager = new SqliteManager(getActivity());
		mSharedPreference = getActivity().getSharedPreferences("sample",
				Context.MODE_PRIVATE);
		getDatabase();
		registerMetaDataToSystemDatabase();
		registerMeasurementToDatabase();
	}

	/**
	 * 핸드폰의 정보와 Application의 Package를 이용하여<br>
	 * SystemDatabase를 얻는다
	 */
	private void getDatabase() {
		Log.d(TAG, "==============DB register============");
		Log.d(TAG, "device : " + Build.MANUFACTURER + "/" + Build.DEVICE);
		Log.d(TAG, "context : " + getActivity().getPackageName());
		Log.d(TAG, "=====================================");

		mSystemDatabaseObject = mSqliteManager.obtainSystemDatabase(
				Build.MANUFACTURER + "/" + Build.DEVICE, getActivity()
						.getPackageName());
	}

	/**
	 * 얻은 SystemDatabase에 Application의 <b>AppFunction</b>과 <br>
	 * <b>DeviceAppMeaurement</b> 메타 데이터를 등록한다.
	 */
	private void registerMetaDataToSystemDatabase() {
		if (!mSystemDatabaseObject.isExist) {
			mSystemDatabaseObject.addDeviceAppFunction("doFlash",
					"lights on/off");
			mSystemDatabaseObject.addDeviceAppMeasurement(Eye.class);
			mSqliteManager.registerSystemDatabase(mSystemDatabaseObject);
		}
	}

	/** 등록된 <b>AppFunction</b> */
	public void doFlash() {
		// TODO stub
	}

	/** 주어진 값을 이용해 Eye 값을 생성해낸다. */
	private Eye getEye(double l, double r) {
		Eye e = new Eye();
		e.left_sight = l;
		e.right_sight = r;
		return e;
	}

	/** SystemDatabase에 Measurement를 기록한다. */
	private void registerMeasurementToDatabase() {
		if (mSystemDatabaseObject.isExist
				&& !mSharedPreference.getBoolean("data", false)) {
			// 샘플데이터 생성
			List<Eye> eyes = makeSampleEyes(2.0);
			try {
				for (Eye eye : eyes) {
					mSqliteManager.registerMeasurementData(
							mSystemDatabaseObject, eye);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 한번만 기록
			mSharedPreference.edit().putBoolean("data", true).commit();
		} else {
			return;
		}
	}

	/** 주어진 값을 이용해 Eye 값들을 생성해낸다. */
	private List<Eye> makeSampleEyes(double seed) {
		List<Eye> eyes = new ArrayList<Eye>();
		int i = 10;
		double seed1 = seed, seed2 = seed * 2;
		while (i-- > 0) {
			eyes.add(getEye(seed1, seed2));
			seed1 += 0.1d;
			seed2 -= 0.2d;
		}
		return eyes;
	}

	/** SystemDatabase에 등록된 Meaurement의 값들을 얻어온다 */
	private List<Eye> obtainMeasurementDatabase() {
		try {
//			return mSqliteManager.obtainMeasurementData(Eye.class);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentLayout = inflater.inflate(R.layout.fragment_sample_graph,
				container, false);
		mGraphView = (ViewGroup) fragmentLayout
				.findViewById(R.id.fragment_sample_GraphView);
		mGraphView.addView(makeGraphView());
		return fragmentLayout;
	}

	/** Graph를 생성한다. */
	private View makeGraphView() {
		List<Eye> dbObjectList = obtainMeasurementDatabase();
		if (dbObjectList == null || dbObjectList.isEmpty()) {
			dbObjectList = makeSampleEyes(new Random(System.currentTimeMillis())
					.nextDouble() * 10);
		}

		// DB에서 얻어온 데이터를 적절히 변환한다.
		final int size = dbObjectList.size();
		GraphViewDataInterface[] leftEyeDatas = new GraphViewDataInterface[size];
		GraphViewDataInterface[] rightEyeDatas = new GraphViewDataInterface[size];
		for (int i = 0; i < size; i++) {
			leftEyeDatas[i] = new GraphViewData(i + 1,
					dbObjectList.get(i).left_sight);
			rightEyeDatas[i] = new GraphViewData(i + 1,
					dbObjectList.get(i).right_sight);
		}
		GraphViewSeries leftSeries = new GraphViewSeries("left-eye",
				new GraphViewSeriesStyle(Color.RED, 30), leftEyeDatas);
		GraphViewSeries rightSeries = new GraphViewSeries("right-eye",
				new GraphViewSeriesStyle(Color.BLUE, 5), rightEyeDatas);

		// 그래프 설정
		GraphView graphView;
		if (getArguments().getInt("title") == 7) {
			graphView = new LineGraphView(getActivity(), "Sample SQL - EYE");
		} else {
			graphView = new BarGraphView(getActivity(), "Sample SQL - EYE");
		}
		graphView.addSeries(leftSeries);
		graphView.addSeries(rightSeries);

		graphView.setShowLegend(true);
		graphView.setLegendAlign(LegendAlign.MIDDLE);
		graphView.getGraphViewStyle().setLegendWidth(200);
		graphView.getGraphViewStyle().setGridColor(Color.TRANSPARENT);
		return graphView;
	}

	private class GraphViewData implements GraphViewDataInterface {
		public double x, y;

		public GraphViewData(double x, double y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public double getX() {
			return x;
		}

		@Override
		public double getY() {
			return y;
		}
	}

}
