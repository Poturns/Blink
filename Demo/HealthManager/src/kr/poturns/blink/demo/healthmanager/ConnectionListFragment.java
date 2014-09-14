package kr.poturns.blink.demo.healthmanager;

import kr.poturns.blink.R;
import kr.poturns.blink.demo.healthmanager.ConnectionFragment.BaseConnectionFragment;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/** 현재 연결된 Device들을 ListView의 형태로 보여주는 Fragment */
class ConnectionListFragment extends BaseConnectionFragment {
	SwipeRefreshLayout mSwipeRefreshLayout;
	ArrayAdapter<BlinkDevice> mAdapter;
	boolean mRefresh = false;
	MenuItem mRetainOperationItem;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		mSwipeRefreshLayout = (SwipeRefreshLayout) View.inflate(getActivity(),
				R.layout.fragment_list_connection, null);
		mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
		mSwipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		showHostDeviceToList(true);
		mAdapter = new ArrayAdapter<BlinkDevice>(getActivity(),
				android.R.layout.simple_list_item_1, getDeviceList()) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				BlinkDevice device = getItem(position);
				View v = super.getView(position, convertView, parent);
				TextView tv = (TextView) v.findViewById(android.R.id.text1);
				tv.setText(device.getName());
				tv.setCompoundDrawablesWithIntrinsicBounds(
						device.isConnected() ? R.drawable.ic_action_device_access_bluetooth_connected
								: R.drawable.ic_action_device_access_bluetooth,
						0, 0, 0);
				return v;
			}
		};
		ListView listView = (ListView) mSwipeRefreshLayout
				.findViewById(android.R.id.list);
		listView.setAdapter(mAdapter);
		listView.setEmptyView(View.inflate(getActivity(), R.layout.emptyview,
				null));
		listView.setOnItemClickListener(mOnItemClickListener);
		return mSwipeRefreshLayout;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_list_connection, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		if (id == R.id.action_list_fillter) {
			if (mRetainOperationItem != null)
				mRetainOperationItem = item;
			boolean check;
			if ((check = !item.isChecked()))
				retainConnectedDevicesFromList();
			else
				fetchDeviceListFromBluetooth();
			item.setChecked(check);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public BaseConnectionFragment getChangedFragment() {
		return new ConnectionCircularFragment();
	}

	private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {

		@Override
		public void onRefresh() {
			mRefresh = true;
			fetchDeviceListFromBluetooth();
		}
	};

	private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			showBlinkDeviceInfoDialog(mAdapter.getItem(position));
		}
	};

	@Override
	public void onDeviceListChanged() {
		if (mRefresh) {
			mRefresh = false;
			mSwipeRefreshLayout.setRefreshing(false);
			Toast.makeText(getActivity(), "connection refresh!",
					Toast.LENGTH_SHORT).show();
		}
		showHostDeviceToList(true);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDeviceListLoadFailed() {
		super.onDeviceListLoadFailed();
		mRetainOperationItem.setChecked(!mRetainOperationItem.isChecked());
	}
}