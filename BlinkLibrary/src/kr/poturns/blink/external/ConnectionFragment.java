package kr.poturns.blink.external;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import kr.poturns.blink.R;
import kr.poturns.blink.db.SqliteManagerExtended;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IBlinkEventBroadcast;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 연결 정보를 보여주는 Abstract Frament<br>
 * */
abstract class ConnectionFragment extends Fragment implements
		IBlinkEventBroadcast {
	List<BlinkDevice> mDeviceList = new CopyOnWriteArrayList<BlinkDevice>();
	IServiceContolActivity mActivityInterface;
	IInternalOperationSupport mBlinkOperation;
	BlinkServiceInteraction mInteraction;
	SqliteManagerExtended mManager;
	BlinkDevice mHostDevice = BlinkDevice.obtainHostDevice();
	ProgressDialog mProgressDialog;
	boolean mConnectionTasking = false, mBluetoothEnabled = true;
	BroadcastReceiver mConnectionDetectReceiver = new BluetoothConnectionReceiver();

	// ReentrantLock mDeviceDataLock = new ReentrantLock();

	/** Android 기기의 Bluetooth 연결 상태를 감지하기 위한 {@link BroadcastReceiver} */
	class BluetoothConnectionReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothAdapter.ACTION_STATE_CHANGED
					.equals(intent.getAction())) {
				int state = intent.getExtras().getInt(
						BluetoothAdapter.EXTRA_STATE);
				switch (state) {
				case BluetoothAdapter.STATE_ON:
					mBluetoothEnabled = true;
					fetchDeviceListFromBluetooth();
					break;
				default:
					mBluetoothEnabled = false;
					break;
				}
			}
		}
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		setHasOptionsMenu(true);
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		activity.registerReceiver(mConnectionDetectReceiver, filter);
		if (activity instanceof IServiceContolActivity) {
			mActivityInterface = (IServiceContolActivity) activity;
		}
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setMessage("Loading...");
		mProgressDialog.setCancelable(false);
		mManager = mActivityInterface.getDatabaseHandler();
		mInteraction = mActivityInterface.getServiceInteration();
		if (mInteraction != null) {
			mBlinkOperation = mActivityInterface.getInternalOperationSupport();
			mInteraction.setOnBlinkEventBroadcast(this);
		} else {
			mInteraction = new BlinkServiceInteraction(activity, this, null) {
				@Override
				public void onServiceConnected(
						IInternalOperationSupport iSupport) {
					mBlinkOperation = iSupport;
					mActivityInterface.setInternalOperationSupport(iSupport);
					if (mDeviceList != null && !mDeviceList.isEmpty()) {
						mDeviceList.clear();
					}
					fetchDeviceListFromBluetooth();
				}

				@Override
				public void onServiceDisconnected() {

				}

				@Override
				public void onServiceFailed() {
					Toast.makeText(activity,
							"Blink Service connection failed!!",
							Toast.LENGTH_SHORT).show();
					mBlinkOperation = null;
					activity.finish();
				}
			};
			mActivityInterface.setServiceInteration(mInteraction);
			mInteraction.startService();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		fetchDeviceListFromBluetooth();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		if (id == R.id.action_connection_view_change) {
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.activity_main_fragment_content,
							getChangeFragment())
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	protected abstract Fragment getChangeFragment();

	/** 해당 Device에 대한 정보를 Dialog형식으로 보여준다. */
	final void showDialog(final BlinkDevice blinkDevice) {
		FragmentManager childManager = getChildFragmentManager();
		DialogFragment dialog;
		if ((dialog = (DialogFragment) childManager.findFragmentByTag("dialog")) == null) {
			dialog = new DeviceInfoDialogFragment();
		}
		Bundle bundle = new Bundle();
		bundle.putParcelable("blinkDevice", blinkDevice);
		dialog.setArguments(bundle);
		dialog.show(childManager, "dialog");
	}

	/**
	 * 연결되지 않은 Device를 Host기기와 연결을 시도한다. 이미 연결된 Device인 경우, 연결 해제를 시도한다.<br>
	 * * 이 메소드는 실제 연결작업을 하는 것이 아니라, 비동기적으로 연결을 시도하는 것 이다.<br>
	 * <br>
	 * 작업이 완료되면 {@link #onDeviceConnected(BlinkDevice)}또는
	 * {@link #onDeviceDisconnected(BlinkDevice)}가 호출된다.
	 */
	final void connectOrDisConnectDevice(BlinkDevice device) {
		new ConnectionTask(getActivity(), device, !device.isConnected())
				.forceLoad();
		mConnectionTasking = true;
		onPreLoading();
	}

	@Override
	public void onDestroy() {
		mInteraction.setOnBlinkEventBroadcast(null);
		getActivity().setProgressBarIndeterminateVisibility(false);
		getActivity().unregisterReceiver(mConnectionDetectReceiver);
		mConnectionDetectReceiver = null;
		mBlinkOperation = null;
		mManager = null;
		mDeviceList = null;
		mActivityInterface = null;
		mHostDevice = null;
		mProgressDialog = null;
		super.onDestroy();
	}

	/*
	 * private final boolean checkAndTryLock() { boolean result =
	 * mDeviceDataLock.isLocked(); if (result) { onDeviceListLoadFailed(true); }
	 * else { mDeviceDataLock.lock(); } return !result; }
	 */

	/**
	 * 주변에 발견된 BlinkDevice의 list를 가져온다. <br>
	 * <br>
	 * 작업에 성공하면 {@link #onDeviceListChanged()}, 실패하면
	 * {@link #onDeviceListLoadFailed()}가 호출된다.
	 */
	final void fetchDeviceListFromBluetooth() {
		if (!mBluetoothEnabled) {
			Toast.makeText(getActivity(), "Bluetooth was not enabled.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		// if (checkAndTryLock()) {
		new DataLoaderTask(getActivity(), true).forceLoad();
		onPreLoading();
		// }
	}

	private final boolean fetchDeviceListBluetoothInternal() {
		mHostDevice = BlinkDevice.obtainHostDevice();
		BlinkDevice[] devices;
		try {
			devices = mBlinkOperation.obtainCurrentDiscoveryList();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		mDeviceList.clear();
		for (BlinkDevice device : devices) {
			mDeviceList.add(device);
		}
		return true;
	}

	/**
	 * 현재 device 리스트에서 연결된 device만 남긴다.<br>
	 * <br>
	 * * 작업에 성공하면 {@link #onDeviceListChanged()} , 실패하면
	 * {@link #onDeviceListLoadFailed()}가 호출된다.
	 */
	final void retainConnectedDevicesFromList() {
		// if (checkAndTryLock()) {
		new DataLoaderTask(getActivity(), false).forceLoad();
		onPreLoading();
		// }
	}

	private final boolean retainConnectedDevicesFromListInternal() {
		boolean result = true;
		List<BlinkDevice> list = Collections.synchronizedList(mDeviceList);

		for (BlinkDevice device : list) {
			if (!device.isConnected()) {
				result &= list.remove(device);
			}
		}

		// removed sucessfully!
		if (result) {
			mDeviceList.clear();
			mDeviceList.addAll(list);
		}

		return result;
	}

	final void showHostDeviceInfomation() {
		showDialog(mHostDevice);
	}

	@Override
	public void onDeviceConnected(BlinkDevice device) {
		Toast.makeText(getActivity(),
				device.getName() + getString(R.string.device_connected),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDeviceDisconnected(BlinkDevice device) {
		Toast.makeText(getActivity(),
				device.getName() + getString(R.string.device_disconnected),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDeviceDiscovered(BlinkDevice device) {
		Toast.makeText(getActivity(), device.getName() + " discoverd!",
				Toast.LENGTH_SHORT).show();
	}

	void onDeviceConnectionFailed(BlinkDevice device) {
		Toast.makeText(getActivity(),
				device.getName() + " (Dis)connection failed!",
				Toast.LENGTH_SHORT).show();
	}

	void onPreLoading() {
		if (mConnectionTasking)
			mProgressDialog.show();
		else
			getActivity().setProgressBarIndeterminateVisibility(true);
	}

	void onPostLoading(Context context) {
		/*
		 * try { mDeviceDataLock.unlock(); } catch (Exception e) {
		 * e.printStackTrace(); }
		 */
		if (mConnectionTasking) {
			mProgressDialog.dismiss();
			mConnectionTasking = false;
		} else if (getActivity() != null) {
			getActivity().setProgressBarIndeterminateVisibility(false);
		}
	}

	/** Service를 통해 BlinkDevice의 리스트를 얻어오는 작업을 비동기적으로 수행하는 Loader */
	class DataLoaderTask extends AsyncTaskLoader<Boolean> {
		private boolean mIsFetch;

		public DataLoaderTask(Context context, boolean isFetch) {
			super(context);
			this.mIsFetch = isFetch;
			this.registerListener(0,
					new AsyncTaskLoader.OnLoadCompleteListener<Boolean>() {
						public void onLoadComplete(
								android.content.Loader<Boolean> loader,
								Boolean data) {
							onPostLoading(loader.getContext());
							if (data)
								onDeviceListChanged();
							else
								onDeviceListLoadFailed(false);
							loader.abandon();
						}
					});
		}

		@Override
		public Boolean loadInBackground() {
			if (mIsFetch)
				return fetchDeviceListBluetoothInternal();
			else
				return retainConnectedDevicesFromListInternal();
		}
	}

	/** Device 리스트가 변경되었을 때 호출된다. */
	abstract void onDeviceListChanged();

	/** Device 리스트의 변경이 실패하였을 때 호출된다. */
	void onDeviceListLoadFailed(boolean isFailedByConcurrentTask) {
		Toast.makeText(getActivity(), "operation failed!", Toast.LENGTH_SHORT)
				.show();
	}

	/** BlinkDevice의 연결 작업을 비동기적으로 수행하는 Loader */
	class ConnectionTask extends AsyncTaskLoader<Boolean> {
		private BlinkDevice mDevice;
		private boolean mIsConnectTask;

		public ConnectionTask(Context context, BlinkDevice device,
				boolean isConnectTask) {
			super(context);
			this.mIsConnectTask = isConnectTask;
			this.mDevice = device;
			this.registerListener(0,
					new AsyncTaskLoader.OnLoadCompleteListener<Boolean>() {
						public void onLoadComplete(
								android.content.Loader<Boolean> loader,
								Boolean result) {
							onPostLoading(loader.getContext());
							if (result && mIsConnectTask
									^ mDevice.isConnected()) {
								if (mIsConnectTask) {
									onDeviceConnected(mDevice);
								} else {
									onDeviceDisconnected(mDevice);
								}
								onDeviceListChanged();
							} else {
								onDeviceConnectionFailed(mDevice);
							}

							loader.abandon();
						}
					});
		}

		@Override
		public Boolean loadInBackground() {
			try {
				if (mIsConnectTask) {
					mBlinkOperation.connectDevice(mDevice);
				} else {
					mBlinkOperation.disconnectDevice(mDevice);
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	/** BlinkDevice의 정보를 보여주는 DialogFragment */
	private class DeviceInfoDialogFragment extends DialogFragment {
		BlinkDevice mBlinkDevice;
		Device mDevice;
		private TabHost mTabHost;
		private ViewPager mViewPager;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mBlinkDevice = getArguments().getParcelable("blinkDevice");
			mDevice = mManager.obtainDevice(mBlinkDevice);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			getDialog().setTitle(mBlinkDevice.getName());
			Point size = new Point();
			getActivity().getWindow().getDecorView().getDisplay().getSize(size);

			if (PrivateUtil.isScreenSizeSmall(getActivity())) {
				getDialog().getWindow().setLayout(size.x / 19 * 10,
						size.y / 19 * 10);
			} else {
				getDialog().getWindow().setLayout(size.x / 19 * 5,
						size.y / 19 * 4);
			}

			final View v = inflater.inflate(
					R.layout.dialog_fragment_connection_device_info, container,
					false);
			mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);
			mTabHost.setup();
			TabHost.TabContentFactory factory = new TabHost.TabContentFactory() {

				@Override
				public View createTabContent(String tag) {
					View v = new View(
							DeviceInfoDialogFragment.this.getActivity());
					v.setMinimumWidth(0);
					v.setMinimumHeight(0);
					v.setTag(tag);
					return v;
				}
			};
			final String[] pageTitles = DeviceInfoDialogFragment.this
					.getResources().getStringArray(
							R.array.dialog_connect_page_titles);
			int i = 0;
			for (String title : pageTitles) {
				mTabHost.addTab(mTabHost.newTabSpec(String.valueOf(i++))
						.setIndicator(title).setContent(factory));
			}

			mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
				@Override
				public void onTabChanged(String tabId) {
					navigateTab(Integer.valueOf(tabId), false);
				}
			});
			mViewPager = (ViewPager) v
					.findViewById(R.id.dialog_deviceinfo_viewpager);
			mViewPager
					.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
						@Override
						public void onPageSelected(int position) {
							navigateTab(position, true);
						}
					});
			mViewPager.setAdapter(new ViewPagerAdapter(
					getChildFragmentManager()));
			return v;
		}

		protected void navigateTab(int position, boolean isFromPager) {
			if (isFromPager) {
				mTabHost.setCurrentTab(position);
			} else {
				mViewPager.setCurrentItem(position);
			}
		}

		private class ViewPagerAdapter extends FragmentPagerAdapter {
			public ViewPagerAdapter(FragmentManager fm) {
				super(fm);
			}

			@Override
			public int getCount() {
				return 2;
			}

			@Override
			public Fragment getItem(int position) {
				switch (position) {
				case 1:
					return new DatabaseDeviceInfoFragment();
				default:
					return new BlinkDeviceInfoFragment();
				}
			}
		}

		private class BlinkDeviceInfoFragment extends Fragment {
			BlinkDevice mDevice = DeviceInfoDialogFragment.this.mBlinkDevice;

			@Override
			public View onCreateView(LayoutInflater inflater,
					ViewGroup container, Bundle savedInstanceState) {
				final View v = inflater.inflate(
						R.layout.dialog_fragment_connection_db_info, container,
						false);
				((TextView) v
						.findViewById(R.id.dialog_fragment_connection_macaddress))
						.setText(mDevice.getAddress());
				((TextView) v
						.findViewById(R.id.dialog_fragment_connection_device_ego))
						.setText(mDevice.getIdentity().toString());
				((TextView) v
						.findViewById(R.id.dialog_fragment_connection_blink_support))
						.setText(String.valueOf(mDevice.isBlinkSupported()));
				((TextView) v
						.findViewById(R.id.dialog_fragment_connection_discoverd))
						.setText(String.valueOf(mDevice.isDiscovered()));
				((TextView) v
						.findViewById(R.id.dialog_fragment_connection_connection))
						.setText(String.valueOf(mDevice.isConnected()));
				((TextView) v
						.findViewById(R.id.dialog_fragment_connection_autoconnect))
						.setText(String.valueOf(mDevice.isAutoConnect()));
				((TextView) v.findViewById(R.id.dialog_fragment_connection_ble))
						.setText(String.valueOf(mDevice.isLESupported()));
				return v;
			}
		}

		private class DatabaseDeviceInfoFragment extends Fragment {
			ArrayAdapter<App> mDialogListAdapter;
			List<App> mAppList;
			static final int NO_SELECTION = -1;
			int mCurrentListViewSelection = NO_SELECTION;

			@Override
			public void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				mAppList = mManager.obtainAppList(mDevice);
				mDialogListAdapter = new ArrayAdapter<App>(getActivity(),
						android.R.layout.simple_list_item_1, mAppList) {
					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
						View v = super.getView(position, convertView, parent);
						TextView tv = (TextView) v
								.findViewById(android.R.id.text1);
						tv.setText(getItem(position).AppName);
						return v;
					}
				};
			}

			@Override
			public View onCreateView(LayoutInflater inflater,
					ViewGroup container, Bundle savedInstanceState) {
				final View v = inflater.inflate(
						R.layout.dialog_fragment_connection, container, false);
				ListView listView = (ListView) v
						.findViewById(android.R.id.list);
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if (mCurrentListViewSelection == position) {
							mCurrentListViewSelection = NO_SELECTION;
						} else
							mCurrentListViewSelection = position;
					}
				});
				listView.setEmptyView(v.findViewById(android.R.id.empty));

				listView.setAdapter(mDialogListAdapter);
				return v;
			}
			// mCurrentListViewSelection = NO_SELECTION;
		}
	}
}
