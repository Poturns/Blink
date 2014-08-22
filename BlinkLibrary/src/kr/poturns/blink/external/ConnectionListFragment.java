package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/** 현재 연결된 Device들을 ListView의 형태로 보여주는 Fragment */
class ConnectionListFragment extends ConnectionFragment {
	SwipeRefreshLayout mSwipeRefreshLayout;
	ArrayAdapter<BlinkDevice> mAdapter;
	TextView mHeaderView;
	boolean mRemoved = false, mRefresh = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mSwipeRefreshLayout = (SwipeRefreshLayout) View.inflate(getActivity(),
				R.layout.fragment_list_connection, null);
		mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
		mSwipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		mAdapter = new ArrayAdapter<BlinkDevice>(getActivity(),
				android.R.layout.simple_list_item_1, mDeviceList) {
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
		mHeaderView = (TextView) View.inflate(getActivity(),
				R.layout.emptyview, null);
		mHeaderView.setText(mHostDevice.getName());
		mHeaderView.setCompoundDrawables(null, null, null, null);
		mHeaderView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showHostDeviceInfomation();
			}
		});
		ListView listView = (ListView) mSwipeRefreshLayout
				.findViewById(android.R.id.list);
		listView.setAdapter(mAdapter);
		listView.setEmptyView(View.inflate(getActivity(), R.layout.emptyview,
				null));
		listView.setOnItemClickListener(mOnItemClickListener);
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (!mRemoved) {
					((ListView) view).removeHeaderView(mHeaderView);
					mRemoved = true;
				}
				if (mRemoved
						&& scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					((ListView) view).addHeaderView(mHeaderView);
					mRemoved = false;
				}
			}
		});
		listView.addHeaderView(mHeaderView);
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
			boolean check;
			if ((check = !item.isChecked()))
				retainConnectedDevicesFromList();
			item.setChecked(check);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Fragment getChangeFragment() {
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
			showDialog(mAdapter.getItem(position));
		}
	};

	@Override
	protected void onDeviceListChanged() {
		if (mRefresh) {
			mRefresh = false;
			mSwipeRefreshLayout.setRefreshing(false);
			Toast.makeText(getActivity(), "connection refresh!",
					Toast.LENGTH_SHORT).show();
		}
		mAdapter.notifyDataSetChanged();
	}
}
