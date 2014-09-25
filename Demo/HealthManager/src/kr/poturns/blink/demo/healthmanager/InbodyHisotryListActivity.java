package kr.poturns.blink.demo.healthmanager;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class InbodyHisotryListActivity extends ListActivity{
	
	ArrayList<InbodyHistoryListDomain> inbodyHisoryList = new ArrayList<InbodyHistoryListDomain>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inbody_history_list);
	}
}
