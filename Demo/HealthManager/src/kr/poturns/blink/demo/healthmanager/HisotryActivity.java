package kr.poturns.blink.demo.healthmanager;

import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.demo.healthmanager.schema.InbodyDomain;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;

public class HisotryActivity extends ListActivity{
	public static int HISTORY_INBODY = 0x01;
	public static int HISTORY_EXERCISE = 0x02;
	public static int HISTORY_HEART = 0x03;
	int history;
	
	BlinkServiceInteraction mBlinkServiceInteraction;
	
	ArrayList<HistoryDomain> inbodyHisoryList = new ArrayList<HistoryDomain>();
	HistoryAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inbody_history_list);
		HealthManagerApplication mHealthManagerApplication = (HealthManagerApplication)getApplication();
		mBlinkServiceInteraction = mHealthManagerApplication.getBlinkServiceInteraction();
		Intent intent = getIntent();
		history = intent.getIntExtra("HISTORY", 0x01);
		
		if(history==0x01){
			List<InbodyDomain> mInbodyDomain = mBlinkServiceInteraction.local.obtainMeasurementData(InbodyDomain.class);
		}else if(history==0x02){
			
		}else if(history==0x03){
			
		}
		
			
		adapter = new HistoryAdapter(this,inbodyHisoryList); // 동적 리스트 관리 Adapter
        setListAdapter(adapter);
	}
}
