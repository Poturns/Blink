package kr.poturns.blink.demo.healthmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.schema.HeartBeat;
import kr.poturns.blink.schema.Inbody;
import kr.poturns.blink.schema.PushUp;
import kr.poturns.blink.schema.SitUp;
import kr.poturns.blink.schema.Squat;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

public class HisotryActivity extends ListActivity implements OnItemClickListener, OnClickListener{
	public static int HISTORY_INBODY = 0x01;
	public static int HISTORY_EXERCISE = 0x02;
	public static int HISTORY_HEART = 0x03;
	int history;
	
	BlinkServiceInteraction mBlinkServiceInteraction;
	Gson gson = new Gson();
	ArrayList<HistoryDomain> inbodyHisoryList = new ArrayList<HistoryDomain>();
	HistoryAdapter adapter;
	List<Inbody> mInbodyList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inbody_history_list);
		HealthManagerApplication mHealthManagerApplication = (HealthManagerApplication)getApplication();
		mBlinkServiceInteraction = mHealthManagerApplication.getBlinkServiceInteraction();
		Intent intent = getIntent();
		history = intent.getIntExtra("HISTORY", 0x01);
		
		ListView lv = getListView();
        lv.setOnItemClickListener(this);
        ((ImageView)findViewById(R.id.refresh)).setOnClickListener(this);
        adapter = new HistoryAdapter(this,null,history,null); // 동적 리스트 관리 Adapter
        setListAdapter(adapter);
        
        ObtainItem();
	}
	@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    // TODO Auto-generated method stub
	    if(history!=0x01)return;
	    
		Intent intent = new Intent(HisotryActivity.this, InbodyDetailActivity.class);
		intent.putExtra("Inbody", gson.toJson(mInbodyList.get(position)));
		startActivity(intent);
    }
	
	public class DateCompare implements Comparator<Inbody> {
		public int compare(Inbody arg0, Inbody arg1) {
			// TODO Auto-generated method stub
			return arg1.DateTime.compareTo(arg0.DateTime);
		}
 
	}
	
	public class HistoryDomainCompare implements Comparator<HistoryDomain> {
		public int compare(HistoryDomain arg0, HistoryDomain arg1) {
			// TODO Auto-generated method stub
			return arg1.date.compareTo(arg0.date);
		}
 
	}
	
	private void ObtainItem(){
		HistoryDomain tHistoryDomain;
		mInbodyList = null;
		inbodyHisoryList.clear();
		if(history==0x01){
			((TextView)findViewById(R.id.history_subtitle)).setText("Inbody History");
			mInbodyList = mBlinkServiceInteraction.local.obtainMeasurementData(Inbody.class);
			Collections.sort(mInbodyList,new DateCompare());
			Inbody mInbody;
			for(int i=0;i<mInbodyList.size();i++){
				
				tHistoryDomain = new HistoryDomain();
				mInbody = mInbodyList.get(i);
				if(mInbody.type==null)tHistoryDomain.icon = R.drawable.fatperson_white;
				else if(mInbody.type.equals("비만형"))tHistoryDomain.icon = R.drawable.fatperson_white;
				else if(mInbody.type.equals("평균형"))tHistoryDomain.icon = R.drawable.avgperson_white;
				else if(mInbody.type.equals("근육형"))tHistoryDomain.icon = R.drawable.musclebodytype;
				
				tHistoryDomain.name = mInbody.weight+"("+mInbody.needweight+")";
				tHistoryDomain.date = mInbody.DateTime;
				inbodyHisoryList.add(tHistoryDomain);
			}
		}else if(history==0x02){
			((TextView)findViewById(R.id.history_subtitle)).setText("Excercise History");
			List<PushUp> mPushUpList = mBlinkServiceInteraction.local.obtainMeasurementData(PushUp.class);
			List<Squat> mSquatList = mBlinkServiceInteraction.local.obtainMeasurementData(Squat.class);
			List<SitUp> mSitUpList = mBlinkServiceInteraction.local.obtainMeasurementData(SitUp.class);
			PushUp mPushUp;
			for(int i=0;i<mPushUpList.size();i++){
				mPushUp = mPushUpList.get(i);
				tHistoryDomain = new HistoryDomain();
				tHistoryDomain.icon = R.drawable.ic_action_health_push_up;
				tHistoryDomain.name = mPushUp.count+"회";
				tHistoryDomain.date = mPushUp.DateTime;
				inbodyHisoryList.add(tHistoryDomain);
			}
			Squat mSquat;
			for(int i=0;i<mSquatList.size();i++){
				mSquat = mSquatList.get(i);
				tHistoryDomain = new HistoryDomain();
				tHistoryDomain.icon = R.drawable.ic_action_health_squat;
				tHistoryDomain.name = mSquat.count+"회";
				tHistoryDomain.date = mSquat.DateTime;
				inbodyHisoryList.add(tHistoryDomain);
			}
			SitUp mSitUp;
			for(int i=0;i<mSitUpList.size();i++){
				mSitUp = mSitUpList.get(i);
				tHistoryDomain = new HistoryDomain();
				tHistoryDomain.icon = R.drawable.ic_action_health_sit_up;
				tHistoryDomain.name = mSitUp.count+"회";
				tHistoryDomain.date = mSitUp.DateTime;
				inbodyHisoryList.add(tHistoryDomain);
			}
			Collections.sort(inbodyHisoryList,new HistoryDomainCompare());
			
		}else if(history==0x03){
			((TextView)findViewById(R.id.history_subtitle)).setText("Heart History");
			List<HeartBeat> mHeartBeatList = mBlinkServiceInteraction.local.obtainMeasurementData(HeartBeat.class);
			HeartBeat mHeartBeat;
			for(int i=0;i<mHeartBeatList.size();i++){
				mHeartBeat = mHeartBeatList.get(i);
				tHistoryDomain = new HistoryDomain();
				tHistoryDomain.icon = R.drawable.ic_action_health_heart;
				tHistoryDomain.name = "분당 "+mHeartBeat.bpm+"번";
				tHistoryDomain.date = mHeartBeat.DateTime;
				inbodyHisoryList.add(tHistoryDomain);
			}
			Collections.sort(inbodyHisoryList,new HistoryDomainCompare());
		}
		adapter.mHistoryDomainList = inbodyHisoryList;
		adapter.mInbodyList = mInbodyList;
		adapter.notifyDataSetChanged();
	}
	@Override
    public void onClick(View arg0) {
	    // TODO Auto-generated method stub
		Log.i("test", "onClick");
		ObtainItem();
    }
}
