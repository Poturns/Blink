package kr.poturns.blink.demo.healthmanager;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class HisotryActivity extends ListActivity{
	
	ArrayList<HistoryDomain> inbodyHisoryList = new ArrayList<HistoryDomain>();
	HistoryAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inbody_history_list);
		
		adapter = new HistoryAdapter(this,inbodyHisoryList); // 동적 리스트 관리 Adapter
        setListAdapter(adapter);
	}
}
