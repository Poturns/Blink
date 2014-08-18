package kr.poturns.blink.external.tab;

import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.R;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.BubbleGraphView;
import com.handstudio.android.hzgrapherlib.graphview.CircleGraphView;
import com.handstudio.android.hzgrapherlib.graphview.CurveGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraph;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraphVO;
import com.handstudio.android.hzgrapherlib.vo.circlegraph.CircleGraph;
import com.handstudio.android.hzgrapherlib.vo.circlegraph.CircleGraphVO;
import com.handstudio.android.hzgrapherlib.vo.curvegraph.CurveGraph;
import com.handstudio.android.hzgrapherlib.vo.curvegraph.CurveGraphVO;

public class SampleGraphFragment extends Fragment {
	private ViewGroup mGraphView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentLayout = inflater.inflate(R.layout.fragment_sample_graph,
				container, false);
		mGraphView = (ViewGroup) fragmentLayout
				.findViewById(R.id.fragment_sample_GraphView);
		mGraphView.addView(makeGraph(getArguments().getInt("title")));
		return fragmentLayout;
	}

	private View makeGraph(int position) {
		switch (position) {
		case 4:
			return new CurveGraphView(getActivity(), makeCurveGraphAllSetting());
		case 5:
			return new CircleGraphView(getActivity(),
					makeLineGraphAllSetting(true));
		case 6:
			return new CircleGraphView(getActivity(),
					makeLineGraphAllSetting(false));
		default:
			return new BubbleGraphView(getActivity(), createBubbleGraphVO());
		}
	}

	private BubbleGraphVO createBubbleGraphVO() {
		BubbleGraphVO ret = null;

		String[] legendArr = { "2008", "2009", "2010", "2011", "2012" };
		ret = new BubbleGraphVO(legendArr);

		// ret.setGraphBG(R.drawable.back);
		ret.setAnimationDuration(1000);

		ret.setIsLineShow(true);
		ret.setIsAnimaionShow(true);

		float[] coordArr = { 20.0f, 35.0f, 50.0f, 104.0f, 50.0f };
		float[] sizeArr = { 20.0f, 15.0f, 20.0f, 25.0f, 30.0f };
		ret.add(new BubbleGraph("Github", Color.rgb(255, 45, 2), coordArr,
				sizeArr));

		float[] coordArr2 = { 30.0f, 40.0f, 15.0f, 21.0f, 80.0f };
		float[] sizeArr2 = { 20.0f, 25.0f, 33.0f, 25.0f, 30.0f };
		ret.add(new BubbleGraph("SourceForge", Color.CYAN, coordArr2, sizeArr2));

		float[] coordArr3 = { 84.0f, 60.0f, 75.0f, 88.0f, 92.0f };
		float[] sizeArr3 = { 15.0f, 60.0f, 20.0f, 23.0f, 25.0f };
		ret.add(new BubbleGraph("Google group", Color.YELLOW, coordArr3,
				sizeArr3));

		return ret;
	}

	private CircleGraphVO makeLineGraphAllSetting(boolean pie) {
		// radius setting
		List<CircleGraph> arrGraph = new ArrayList<CircleGraph>();

		arrGraph.add(new CircleGraph("android", Color.parseColor("#3366CC"), 1));
		arrGraph.add(new CircleGraph("ios", Color.parseColor("#DC3912"), 1));
		arrGraph.add(new CircleGraph("tizen", Color.parseColor("#FF9900"), 1));
		arrGraph.add(new CircleGraph("HTML", Color.parseColor("#109618"), 1));
		arrGraph.add(new CircleGraph("C", Color.parseColor("#990099"), 3));

		CircleGraphVO vo = new CircleGraphVO(arrGraph);

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

		vo.setPieChart(pie);

		GraphNameBox graphNameBox = new GraphNameBox();

		// nameBox
		graphNameBox.setNameboxMarginTop(25);
		graphNameBox.setNameboxMarginRight(25);

		vo.setGraphNameBox(graphNameBox);

		return vo;
	}

	private CurveGraphVO makeCurveGraphAllSetting() {
		// GRAPH SETTING
		String[] legendArr = { "2014-01-01", "2014-02-01", "2014-03-03",
				"2014-04-01", "2014-05-01" };
		float[] graph1 = { 500, 100, 300, 200, 100 };
		float[] graph2 = { 000, 100, 200, 100, 200 };

		List<CurveGraph> arrGraph = new ArrayList<CurveGraph>();

		arrGraph.add(new CurveGraph("Right-Sight", Color.GREEN, graph1));
		arrGraph.add(new CurveGraph("Left-Sight", Color.GRAY, graph2));

		CurveGraphVO vo = new CurveGraphVO(legendArr, arrGraph);
		// set animation
		vo.setAnimation(new GraphAnimation(
				GraphAnimation.CURVE_REGION_ANIMATION_1, 1000));
		// set graph name box
		vo.setGraphNameBox(new GraphNameBox());
		return vo;
	}
}
