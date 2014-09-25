package com.example.auctionrealtimetest;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class HealthManagerListActivity extends Activity{
	
	private RelativeLayout inbodyRelativeLayout;
	private RelativeLayout exerciseRelativeLayout;
	private RelativeLayout heartRelativeLayout;
	 @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.activity_healthmanager_list);
		 
		 inbodyRelativeLayout = (RelativeLayout)findViewById(R.id.indobyRelativeLayout);
		 exerciseRelativeLayout = (RelativeLayout)findViewById(R.id.exerciseRelativeLayout);
		 heartRelativeLayout= (RelativeLayout)findViewById(R.id.heartRelativeLayout);
		 
		 inbodyRelativeLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(HealthManagerListActivity.this, InbodyHisotryListActivity.class);
			}
		});
	   
	}
}
