package kr.poturns.blink.demo.healthmanager;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;








import java.util.Set;
import java.util.TreeMap;

import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.schema.Inbody;
import kr.poturns.blink.schema.PushUp;
import kr.poturns.blink.schema.SitUp;
import kr.poturns.blink.schema.Squat;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.google.common.primitives.Floats;
import com.google.gson.Gson;
import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.LineGraphView;

import kr.poturns.blink.demo.healthmanager.util.*;

import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraph;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraphVO;

import dalvik.annotation.TestTarget;

public class RecordActivity extends Activity {

	private ViewGroup layoutGraphView;
	private ViewGroup layoutGraphView2;
	private ViewGroup layoutGraphView3;
	Gson gson = new Gson();
	List<Inbody> mInbodyList;
	BlinkServiceInteraction mBlinkServiceInteraction;
	String[] inbodyDates;
	float [] inbodyMuscles;
	float [] inbodyFats;
	float [] inbodyKgs;
	
	float [] todayExercise = null;
	TreeMap<String, Integer> exerciseHashMap;
	ArrayList exerciseDateList;
	ArrayList todayCaloriesList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		HealthManagerApplication mHealthManagerApplication = (HealthManagerApplication)getApplication();
		mBlinkServiceInteraction = mHealthManagerApplication.getBlinkServiceInteraction();
		
		layoutGraphView = (ViewGroup) findViewById(R.id.layoutGraphView);
		layoutGraphView2 = (ViewGroup) findViewById(R.id.layoutGraphView2);
		layoutGraphView3 = (ViewGroup) findViewById(R.id.layoutGraphView3);
		exerciseDateList = new ArrayList();
		todayCaloriesList = new ArrayList();
		exerciseHashMap = new TreeMap<String, Integer>();
		setLineGraph();
		
	}

	private void setLineGraph() {
		//all setting
		LineGraphVO vo = makeInbodyGraph();
		LineGraphVO vo2 = makeExerciseGraph();
		//default setting
	//	LineGraphVO vo = makeLineGraphDefaultSetting();
		
		layoutGraphView.addView(new LineGraphView(this, vo));
		
		layoutGraphView2.addView(new LineGraphView(this, vo2));
	}
	private void getAllExercises(){
		List<PushUp> mPushUpList = null;
		List<Squat> mSquatList =null;
		List<SitUp> mSitUpList = null;
		mPushUpList = mBlinkServiceInteraction.local.obtainMeasurementData(PushUp.class);
		 mSquatList = mBlinkServiceInteraction.local.obtainMeasurementData(Squat.class);
		 mSitUpList = mBlinkServiceInteraction.local.obtainMeasurementData(SitUp.class);
		 int todayExerciseCalories = 0;
		// int today
		 PushUp mPushUp;
		 for(int i=0;i<mPushUpList.size();i++){
				mPushUp = mPushUpList.get(i);
				String date = mPushUp.DateTime;
				date = date.split(" ")[0];
				if(exerciseHashMap.containsKey(date)){
					int tmp = exerciseHashMap.get(date);
					exerciseHashMap.put(date, tmp+(mPushUp.count*4));
				}
				else{
					exerciseHashMap.put(date, mPushUp.count*4);
				}
			//	mPushUp.count;
			}
			Squat mSquat;
			for(int i=0;i<mSquatList.size();i++){
				mSquat = mSquatList.get(i);
				String date = mSquat.DateTime;
				date = date.split(" ")[0];
				if(exerciseHashMap.containsKey(date)){
					int tmp = exerciseHashMap.get(date);
					exerciseHashMap.put(date, tmp+(mSquat.count*4));
				}
				else{
					exerciseHashMap.put(date, mSquat.count*4);
			}
			}
			SitUp mSitUp;
			for(int i=0;i<mSitUpList.size();i++){
				mSitUp = mSitUpList.get(i);
				String date = mSitUp.DateTime;
				date = date.split(" ")[0];
				if(exerciseHashMap.containsKey(date)){
					int tmp = exerciseHashMap.get(date);
					exerciseHashMap.put(date, tmp+(mSitUp.count*4));
				}
				else{
					exerciseHashMap.put(date,mSitUp.count*4);
			}
			}
		 
	}
	private void getAllInbodyDomains(){
		
		mInbodyList = mBlinkServiceInteraction.local.obtainMeasurementData(Inbody.class);
		Collections.sort(mInbodyList,new DateCompare());
		Inbody mInbody;
		inbodyDates = new String[mInbodyList.size()];
		inbodyMuscles = new float[mInbodyList.size()];
		inbodyFats= new float[mInbodyList.size()];
		inbodyKgs= new float[mInbodyList.size()];
		for(int i=0;i<mInbodyList.size();i++){
			mInbody = mInbodyList.get(i);
			
			inbodyDates[i] = mInbody.DateTime;
			inbodyFats[i] = mInbody.fat;
			inbodyMuscles[i] = mInbody.muscle;
			inbodyKgs[i] = mInbody.weight;
			
		}
	}
	/**
	 * make simple line graph
	 * @return
	 */
	private LineGraphVO makeExerciseGraph()
	{
		getAllExercises();
		Log.d("RecordActivity", "getAllExercise is completed");
		int paddingBottom 	= LineGraphVO.DEFAULT_PADDING;
		int paddingTop 		= LineGraphVO.DEFAULT_PADDING;
		int paddingLeft 	= LineGraphVO.DEFAULT_PADDING;
		int paddingRight 	= LineGraphVO.DEFAULT_PADDING;


		int marginTop 		= LineGraphVO.DEFAULT_MARGIN_TOP;
		int marginRight 	= LineGraphVO.DEFAULT_MARGIN_RIGHT;

		List<LineGraph> arrGraph 		= new ArrayList<LineGraph>();

		
		Set<String> keys = exerciseHashMap.keySet();
		String[] keyStrings = null;
		if(keys.size()==1){
			String[] keyStrings_tmp =  keys.toArray(new String[keys.size()]);
			keyStrings = new String[keyStrings_tmp.length+1];
			for(int i=0; i<keyStrings.length; i++){
				if(i==0){
					keyStrings[i]="0";
				
				}
				else{
					keyStrings[i] = keyStrings_tmp[i-1];
				}
			}
			}
		else{
			keyStrings = keys.toArray(new String[keys.size()]);
		}
		int maxValue = 0;
		int increment = 0;
		
		if(keys.size() != 0){
		
		int maxvalue = 0;
		todayExercise = new float[keyStrings.length];
		Log.d("RecordActivity", "keyStrings 길이="+keyStrings.length);
		for(int i=0; i<keyStrings.length; i++){
			if(i!=0){
				Log.d("RecordActivity", "KeyString["+i+"]="+keyStrings[i]);
			todayExercise[i] =  exerciseHashMap.get(keyStrings[i]);
			Log.d("RecordActivity", "총 운동량="+todayExercise[i]);
			Log.d("RecordActivity", "날짜="+keyStrings[i]);
			}
			else{
				todayExercise[i]=0;
			}
		}
		
		for(int i=0; i<todayExercise.length; i++){
			if(maxvalue < todayExercise[i]){
				maxvalue = (int) todayExercise[i];
			}
		}
		
		maxValue 		= maxvalue+20;
		increment 		= maxvalue/5;
		//List b = Arrays.asList(ArrayUtils.toObject(a));t
		arrGraph.add(new LineGraph("총 운동량(kcal)", 0xaa66ff33, todayExercise));
		}/*
		else{
			Log.d("RecordActivity", "There are no exercise datas");
			keyStrings = new String[1];
			
			todayExercise = new float[1];
			arrGraph.add(new LineGraph("총 운동량", 0xaa66ff33, todayExercise));
			maxValue = 0;
			increment = 0;
		}*/
		
		Log.d("RecordActivity", "maxvalue = "+maxValue);
		Log.d("RecordActivity", "increment = "+increment);
		Log.d("RecordActivity", "keyStrings[0] = "+keyStrings[0]);
		//Log.d("RecordActivity", " = "+maxValue);
		LineGraphVO vo = new LineGraphVO(
				paddingBottom, paddingTop, paddingLeft, paddingRight,
				marginTop, marginRight, maxValue, increment, keyStrings, arrGraph, R.drawable.blackbackground2);

		vo.setAnimation(new GraphAnimation(GraphAnimation.LINEAR_ANIMATION, GraphAnimation.DEFAULT_DURATION));
	
		GraphNameBox gnb = new GraphNameBox();
		gnb.setNameboxColor(Color.WHITE);
		gnb.setNameboxTextColor(Color.WHITE);
		vo.setGraphNameBox(gnb);
		vo.setAxisLineColor(Color.WHITE);
		vo.setBaseLineColor(Color.WHITE);
		vo.setAxisLineWidth(8);
		vo.setBaseLineWidth(5);
		vo.setMarkTextColor(Color.WHITE);
		vo.setMarkTextSize(25);
		
		Log.d("RecordActivity", "makeExerciseGraph is completed");
		return vo;
	}
	private LineGraphVO makeLineGraphDefaultSetting() {
		Inbody mInbody = null;
		getAllInbodyDomains();
		//mInbody = gson.fromJson(getIntent().getStringExtra("Inbody"), Inbody.class);
		Date date = new Date();
		int year = date.getYear();
		int month = date. getMonth();
		int day = date.getDay();
		/*String yearNmonth = year+":"+month+":";
		String[] legendArr 	= {yearNmonth+(day+1),yearNmonth+(day+2),yearNmonth+(day+3),yearNmonth+(day+4)};
		
		float[] graph1 		= {500,100,300,200,100};
		float[] graph2 		= {000,100,200,100,200};
		float[] graph3 		= {200,500,300,400,000};
		*/
		List<LineGraph> arrGraph 		= new ArrayList<LineGraph>();
		arrGraph.add(new LineGraph("몸무게", 0xaa66ff33, inbodyFats));
		arrGraph.add(new LineGraph("근육량", 0xaa00ffff,inbodyMuscles));
		arrGraph.add(new LineGraph("지방량", 0xaaff0066,inbodyKgs));
		
		LineGraphVO vo = new LineGraphVO(inbodyDates, arrGraph);
		return vo;
	}

	/**
	 * make line graph using options
	 * @return
	 */
	private LineGraphVO makeInbodyGraph() {
		//BASIC LAYOUT SETTING
		//padding
		getAllInbodyDomains();
		int paddingBottom 	= LineGraphVO.DEFAULT_PADDING;
		int paddingTop 		= LineGraphVO.DEFAULT_PADDING;
		int paddingLeft 	= LineGraphVO.DEFAULT_PADDING;
		int paddingRight 	= LineGraphVO.DEFAULT_PADDING;

		//graph margin
		int marginTop 		= LineGraphVO.DEFAULT_MARGIN_TOP;
		int marginRight 	= LineGraphVO.DEFAULT_MARGIN_RIGHT;
	
		//max value
		//int maxValue 		= 120;

		//increment
		//int increment 		= LineGraphVO.DEFAULT_INCREMENT;
		
		//GRAPH SETTING
		String[] legendArr 	= {"1","2","3","4","5"};
		float[] graph1 		= {500,100,300,200,100};
		float[] graph2 		= {000,100,200,100,200};
		float[] graph3 		= {200,500,300,400,000};
		
		List<LineGraph> arrGraph 		= new ArrayList<LineGraph>();
		int maxvalue = 0;
		for(int i=0; i< inbodyKgs.length; i++){
			if(maxvalue < inbodyKgs[i]){
				maxvalue = (int) inbodyKgs[i];
			}
		}
		int maxValue 		= 120;
		int increment 		= 25;
		
		
		Arrays.sort(inbodyDates);
		List<Float> floatList = Floats.asList(inbodyKgs);
		Collections.sort(floatList, Collections.reverseOrder());
		int i = 0;

		for (Float f : floatList) {
		    inbodyKgs[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
		}
		List<Float> floatList2 = Floats.asList(inbodyMuscles);
		Collections.sort(floatList2, Collections.reverseOrder());
		i = 0;

		for (Float f : floatList2) {
		    inbodyMuscles[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
		}
		List<Float> floatList3 = Floats.asList(inbodyFats);
		Collections.sort(floatList3, Collections.reverseOrder());
		 i = 0;

		for (Float f : floatList3) {
		    inbodyFats[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
		}

		arrGraph.add(new LineGraph("몸무게(kg)", 0xaa66ff33, inbodyKgs));
		arrGraph.add(new LineGraph("근육량(kg)", 0xaa00ffff,inbodyMuscles));
		arrGraph.add(new LineGraph("지방량(kg)", 0xaaff0066,inbodyFats));
		LineGraphVO vo = new LineGraphVO(
				paddingBottom, paddingTop, paddingLeft, paddingRight,
				marginTop, marginRight, maxValue, increment, inbodyDates, arrGraph, R.drawable.blackbackground2);
		
		//vo.setLineColor(Color.WHITE);
	   
	//	vo.set
		//set animation
		vo.setAnimation(new GraphAnimation(GraphAnimation.LINEAR_ANIMATION, GraphAnimation.DEFAULT_DURATION));
		//set graph name box
		GraphNameBox gnb = new GraphNameBox();
		gnb.setNameboxColor(Color.WHITE);
		gnb.setNameboxTextColor(Color.WHITE);
		vo.setGraphNameBox(gnb);
		vo.setAxisLineColor(Color.WHITE);
		vo.setBaseLineColor(Color.WHITE);
		vo.setAxisLineWidth(8);
		vo.setBaseLineWidth(5);
		vo.setMarkTextColor(Color.WHITE);
		vo.setMarkTextSize(25);

		return vo;
	}
}

