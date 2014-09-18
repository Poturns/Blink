package kr.poturns.blink.demo.healthmanager;

import java.util.ArrayList;

import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.schema.DefaultSchema;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class ListActivity extends Activity implements OnClickListener{
	BlinkServiceInteraction mBlinkServiceInteraction;
	ArrayList<DefaultSchema> data;
	ListView mListView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		mBlinkServiceInteraction = ((HealthManagerApplication)getApplicationContext()).getmBlinkServiceInteraction();
		mListView = (ListView)findViewById(R.id.activity_list_listview);
		
		data = new ArrayList<DefaultSchema>();
		mListView.setAdapter(new HealthManagerAdapter());
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button_setting:
			mBlinkServiceInteraction.openControlActivity();
			break;
			
		default:
			break;
		}
	}
	
	class HealthManagerAdapter extends BaseAdapter {

		LayoutInflater mLayoutInflater;
		
		public HealthManagerAdapter(){
			mLayoutInflater = (LayoutInflater)ListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView==null){
				convertView=mLayoutInflater.inflate(R.layout.list_row, parent,false);
			}
			return convertView;
		}
		
	}
}
