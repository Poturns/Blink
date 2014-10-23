package kr.poturns.blink.demo.healthmanager;

import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.schema.Inbody;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

public class HistoryAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	public ArrayList<HistoryDomain> mHistoryDomainList = null;
	public List<Inbody> mInbodyList = null;
	private Context context;
	int history = 0x01;
	Gson gson = new Gson();
	public HistoryAdapter(Context context, ArrayList<HistoryDomain> mHistoryDomainList,int history,List<Inbody> mInbodyList) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		if(mHistoryDomainList==null)mHistoryDomainList = new ArrayList<HistoryDomain>();
		if(mInbodyList==null)mInbodyList = new ArrayList<Inbody>();
		this.mHistoryDomainList = mHistoryDomainList;
		this.history = history;
		this.mInbodyList = mInbodyList;
	}

	// Adapter가 관리할 Data의 개수를 설정 합니다.
	@Override
	public int getCount() {
		return mHistoryDomainList.size();
	}

	// Adapter가 관리하는 Data의 Item 의 Position을 <객체> 형태로 얻어 옵니다.
	@Override
	public HistoryDomain getItem(int position) {
		return mHistoryDomainList.get(position);
	}

	// Adapter가 관리하는 Data의 Item 의 position 값의 ID 를 얻어 옵니다.
	@Override
	public long getItemId(int position) {
		return position;
	}

	// ListView의 뿌려질 한줄의 Row를 설정 합니다.
	@Override
	public View getView(int position, View convertview, ViewGroup parent) {
		View v = inflater.inflate(R.layout.list_inbody_history_row, null);
		ImageView history_icon = (ImageView)v.findViewById(R.id.history_icon);
		TextView history_name = (TextView)v.findViewById(R.id.history_name);
		TextView history_date = (TextView)v.findViewById(R.id.history_date);
		
		history_icon.setImageResource(mHistoryDomainList.get(position).icon);
		history_name.setText(mHistoryDomainList.get(position).name);
		history_date.setText(mHistoryDomainList.get(position).date);
		
		return v;

	}

	@Override
	protected void finalize() throws Throwable {
		free();
		super.finalize();
	}

	private void free() {
		inflater = null;
	}

}