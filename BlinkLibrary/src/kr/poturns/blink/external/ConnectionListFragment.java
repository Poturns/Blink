package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.external.ConnectionFragment.BaseConnectionFragment;
import kr.poturns.blink.internal.comm.BlinkDevice;
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
import android.widget.TextView;

/** 현재 연결된 Device들을 ListView의 형태로 보여주는 Fragment */
class ConnectionListFragment extends BaseConnectionFragment {
	SwipeRefreshLayout mSwipeRefreshLayout;
	AbsListView mListView;
	TextView mConnectionTitleView;
	ArrayAdapter<BlinkDevice> mAdapter;
	boolean mRefresh = false;
	// 초기에는 Discovery를 먼저 보여주지만
	// 값 변화 감지를 위해 true로 설정
	/** 현재 리스트가 보여주는 관점이 연결(Connection)인지 여부 */
	private boolean mShowOnlyConnected = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View v = View.inflate(getActivity(),
				R.layout.res_blink_fragment_list_connection, null);
		mConnectionTitleView = (TextView) v.findViewById(android.R.id.title);

		mSwipeRefreshLayout = (SwipeRefreshLayout) v
				.findViewById(R.id.res_blink_swipe_container);
		mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
		mSwipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		checkAndRemoveHostDevice();

		mAdapter = new ArrayAdapter<BlinkDevice>(getActivity(),
				R.layout.res_blink_list_fragment_list_connection,
				android.R.id.text1, getDeviceList()) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				final BlinkDevice device = getItem(position);
				View v = super.getView(position, convertView, parent);

				TextView tv = (TextView) v.findViewById(android.R.id.text1);
				tv.setText(device.getName());

				tv.setCompoundDrawablesWithIntrinsicBounds(
						device.isConnected() ? R.drawable.res_blink_ic_action_device_access_bluetooth_connected
								: R.drawable.res_blink_ic_action_device_access_bluetooth,
						0, 0, 0);
				v.findViewById(android.R.id.button1).setOnClickListener(
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								connectOrDisConnectDevice(device);
							}
						});
				return v;
			}
		};

		// 현재 구동중인 장비 표시
		TextView hostView = (TextView) v.findViewById(android.R.id.text1);
		hostView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showBlinkDeviceInfoDialog(BlinkDevice.HOST);
			}
		});
		hostView.setText(BlinkDevice.HOST.getName());
		hostView.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.res_blink_ic_action_android, 0, 0, 0);

		mListView = (AbsListView) mSwipeRefreshLayout
				.findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setEmptyView(View.inflate(getActivity(),
				R.layout.res_blink_view_empty, null));
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				showBlinkDeviceInfoDialog((BlinkDevice) parent
						.getItemAtPosition(position));
			}
		});
		mListView.setEmptyView(v.findViewById(android.R.id.empty));
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.res_blink_fragment_list_connection, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		if (id == R.id.res_blink_action_list_fillter) {
			if (mShowOnlyConnected) {
				retainConnectedDevicesFromList();
				mConnectionTitleView.setText("CONNECTION");
			} else {
				obtainDiscoveryList();
				mConnectionTitleView.setText("DISCOVERY");
			}
			mShowOnlyConnected = !mShowOnlyConnected;
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

	@Override
	public void onDiscoveryFailed() {
		mRefresh = false;
		mSwipeRefreshLayout.setRefreshing(false);
	}

	@Override
	public void onDiscoveryFinished() {
		super.onDiscoveryFinished();
		mRefresh = false;
		mSwipeRefreshLayout.setRefreshing(false);
	}

	@Override
	public void onDeviceListChanged() {
		// checkAndRemoveHostDevice();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDeviceListLoadFailed() {
		super.onDeviceListLoadFailed();
		mShowOnlyConnected = false;
	}

	private void checkAndRemoveHostDevice() {
		if (getDeviceList().contains(BlinkDevice.HOST)) {
			getDeviceList().remove(BlinkDevice.HOST);
		}
	}

	@Override
	public void onDeviceConnected(BlinkDevice device) {
		super.onDeviceConnected(device);
		changeButtonMsg(device, "Disconnect");
	}

	@Override
	public void onDeviceDisconnected(BlinkDevice device) {
		super.onDeviceDisconnected(device);
		changeButtonMsg(device, "Connect");
	}

	// 연결 버튼의 문자열을 바꾼다.
	private void changeButtonMsg(BlinkDevice device, String msg) {
		int wantedPosition = getDeviceList().indexOf(device);
		if (wantedPosition == -1)
			return;
		int firstPosition = mListView.getFirstVisiblePosition();
		// -mListView.getHeaderViewsCount();
		int wantedChild = wantedPosition - firstPosition;

		if (wantedChild < 0 || wantedChild >= mListView.getChildCount()) {
			return;
		}
		try {
			View childView = mListView.getChildAt(wantedChild);
			((TextView) childView.findViewById(android.R.id.button1))
					.setText(msg);
			((TextView) childView.findViewById(android.R.id.text1))
					.setCompoundDrawablesRelativeWithIntrinsicBounds(
							device.isConnected() ? R.drawable.res_blink_ic_action_device_access_bluetooth_connected
									: R.drawable.res_blink_ic_action_device_access_bluetooth,
							0, 0, 0);
			mListView.postInvalidate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}