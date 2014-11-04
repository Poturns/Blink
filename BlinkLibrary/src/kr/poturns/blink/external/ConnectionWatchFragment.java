package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.external.ConnectionFragment.BaseConnectionFragment;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 연결 상태를 Watch의 화면으로 보여주는 Fragment <br>
 * <br>
 * 화면에 표현할 정보는 '기기 리스트'<br>
 * 지원하는 기능은 '장비 연결', '새로고침(디스커버리)'
 */
class ConnectionWatchFragment extends BaseConnectionFragment {
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
				v.setBackgroundResource(item.isConnected() ? R.drawable.res_blink_drawable_rectangle_box_pressed
						: R.drawable.res_blink_drawable_rectangle_box);
				return v;
			}
		};
		mListView = (AbsListView) inflater.inflate(
				R.layout.res_blink_view_listview, container, false);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemLongClickListener(mItemClickListener);
		mListView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				Toast.makeText(getActivity(), "test", Toast.LENGTH_SHORT)
						.show();
				getActivity().finish();
				return true;
			}
		});
		return mListView;
	}

	@Override
	public BaseConnectionFragment getChangedFragment() {
		return this;
	}

	private AdapterView.OnItemLongClickListener mItemClickListener = new AdapterView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
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
	};
}
