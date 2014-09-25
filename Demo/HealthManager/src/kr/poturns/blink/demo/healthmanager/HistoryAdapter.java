package kr.poturns.blink.demo.healthmanager;

import java.util.ArrayList;

import kr.poturns.blink.demo.healthmanager.schema.InbodyDomain;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private ArrayList<HistoryDomain> mHistoryDomainList = null;

	public HistoryAdapter(Context c, ArrayList<HistoryDomain> mHistoryDomainList) {
		this.inflater = LayoutInflater.from(c);
		this.mHistoryDomainList = mHistoryDomainList;
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