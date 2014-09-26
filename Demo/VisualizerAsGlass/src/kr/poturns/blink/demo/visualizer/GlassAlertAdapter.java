package kr.poturns.blink.demo.visualizer;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GlassAlertAdapter extends BaseAdapter {
  
	private final static long MESSAGE_APPEAR_TIME = 12000;
	private final Context CONTEXT;
	private final Queue<String> ALERT_LIST;
	
	public GlassAlertAdapter(Context context) {
		CONTEXT = context;
		ALERT_LIST = new LinkedList<String>();
	}

	@Override
	public int getCount() {
		return ALERT_LIST.size();
	}

	@Override
	public Object getItem(int position) {
		String[] items = new String[ALERT_LIST.size()];
		ALERT_LIST.toArray(items);
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) 
			convertView = LayoutInflater.from(CONTEXT).inflate(R.layout.glass_list_item, parent, false);
		
		TextView tv = (TextView) convertView.findViewById(R.id.glass_listitem_content);
		tv.setText((String) getItem(position));
		
		return convertView;
	}
	
	public void pushNewMessage(String message) {
		ALERT_LIST.add(message);
		notifyDataSetChanged();
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!ALERT_LIST.isEmpty()) {
					ALERT_LIST.remove();
					notifyDataSetChanged();
				}
			}
		}, MESSAGE_APPEAR_TIME);
	}
}
