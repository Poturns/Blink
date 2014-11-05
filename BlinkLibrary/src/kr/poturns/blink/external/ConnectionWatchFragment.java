package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * 연결 상태를 Watch의 화면으로 보여주는 Fragment <br>
 * <br>
 * 화면에 표현할 정보는 '기기 리스트'<br>
 * 지원하는 기능은 '장비 연결', '새로고침(디스커버리)'
 */
class ConnectionWatchFragment extends BaseConnectionFragment implements
		SwipeListener {
	AbsListView mListView;
	ArrayAdapter<BlinkDevice> mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mAdapter = new ArrayAdapter<BlinkDevice>(getActivity(),
				android.R.layout.simple_list_item_1, android.R.id.text1,
				getDeviceList()) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				BlinkDevice item = getItem(position);
				((TextView) v).setText(item.getName());
				((TextView) v).setTextColor(item.isConnected() ? Color.WHITE
						: Color.BLACK);
				v.setBackgroundResource(item.isConnected() ? R.drawable.res_blink_drawable_rectangle_box_pressed
						: R.drawable.res_blink_drawable_rectangle_box);
				return v;
			}
		};
		View root = inflater.inflate(
				R.layout.res_blink_fragment_connection_watch, container, false);
		mListView = (AbsListView) root.findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemLongClickListener(mItemClickListener);
		TextView empty = (TextView) root.findViewById(android.R.id.empty);
		empty.setText("click here to refresh");
		empty.setCompoundDrawablesRelativeWithIntrinsicBounds(
				R.drawable.res_blink_ic_action_navigation_refresh, 0, 0, 0);
		empty.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				fetchDeviceListFromBluetooth();
			}
		});
		mListView.setEmptyView(empty);
		return root;
	}

	@Override
	public BaseConnectionFragment getChangedFragment() {
		return this;
	}

	private AdapterView.OnItemLongClickListener mItemClickListener = new AdapterView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			//TODO Horizontal ViewPager 개념을 이용해서
			//( 확인(dismiss) )- device 정보 - 연결 - 연결 해제 
			// 형태로 보여주기
			BlinkDevice device = (BlinkDevice) parent
					.getItemAtPosition(position);
			connectOrDisConnectDevice(device);
			return true;
		}
	};

	@Override
	public void onDeviceListChanged() {
		super.onDeviceListChanged();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT:
			if (getActivity() instanceof IServiceContolWatchActivity)
				((IServiceContolWatchActivity) getActivity())
						.returnToMain(null);
			return true;
		default:
			return false;
		}
	}
}
