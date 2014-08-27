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
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
 * 연결 관련 처리를 하는 Frament<br>
 * <br>
 * Activity의 '연결' 항목을 터치하면 Activity의 FrameLayout에 로드되는 Fragment 이다. <br>
 * <br>
 * BaseConnectionFragment에서의 BlinkDevice 와 그 List관련 처리는 ConnectionFragment가 대신
 * 하고, UI관련 처리는 BaseConnectionFragment를 상속받는 class가 위임 한다.<br>
 * */
final class ConnectionFragment extends Fragment {
	/** 현재 가지고 있는 BlinkDevice의 리스트 */
	List<BlinkDevice> mDeviceList = new CopyOnWriteArrayList<BlinkDevice>();
	/** Activity의 interface */
	IServiceContolActivity mActivityInterface;
	/** Binder interface */
	IInternalOperationSupport mBlinkOperation;
	/***/
	BlinkServiceInteraction mInteraction;
	/** Database manager */
	SqliteManagerExtended mManager;
	/** 이 Activity가 실행되고 있는 BlinkDevice */
	BlinkDevice mHostDevice;
	ProgressDialog mProgressDialog;
	/** BlinkDevice 연결 작업 여부 */
	boolean mConnectionTasking = false;
	/** Bluetooth 사용 가능 여부 */
	boolean mBluetoothEnabled = true;
	/** BlinkDevice Discovery 작업 여부 */
	boolean mFetchTasking = false;
	/** Bluetooth 연결을 감지하는 {@link BroadcastReceiver} */
	BroadcastReceiver mConnectionDetectReceiver = new BluetoothConnectionReceiver();
	/** UI를 표시하는 ChildFragment */
	IConnectionCallback mCurrentChildFragmentInterface;

	interface IConnectionCallback extends IBlinkEventBroadcast {
		/** 바뀌어질 Fragment를 얻는다. */
		BaseConnectionFragment getChangedFragment();

		/** Device 리스트의 변경 요청이 실패하였을 때, 호출된다 */
		void onDeviceListLoadFailed();

		/** 비동기 작업의 종료 후 호출된다 */
		void onPostLoading();

		/** 비동기 작업의 시작 전 호출된다 */
		void onPreLoading();

		/** Device 리스트가 변경되었을 때 호출된다. */
		void onDeviceListChanged();

		/** Device연결이 실패하였을 때 호출된다. */
		void onDeviceConnectionFailed(BlinkDevice device);

		/** Bluetooth Discovery가 종료되었을 때, 호출된다. */
		void onDiscoveryFinished();

		/** Bluetooth Discovery가 시작되었을 때, 호출된다. */
		void onDiscoveryStarted();
	}

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
		onFragmentChanged(new ConnectionCircularFragment());
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setMessage("Loading...");
		mProgressDialog.setCancelable(false);
		mManager = mActivityInterface.getDatabaseHandler();
		mInteraction = mActivityInterface.getServiceInteration();
		if (mInteraction != null) {
			mBlinkOperation = mActivityInterface.getInternalOperationSupport();
			mInteraction
					.setOnBlinkEventBroadcast(mCurrentChildFragmentInterface);
		} else {
			mInteraction = new BlinkServiceInteraction(activity,
					mCurrentChildFragmentInterface, null) {
				@Override
				public void onServiceConnected(
						IInternalOperationSupport iSupport) {
					mBlinkOperation = iSupport;
					mActivityInterface.setInternalOperationSupport(iSupport);
				}

				@Override
				public void onServiceDisconnected() {
					Toast.makeText(activity,
							"Blink Service was disconnected!!",
							Toast.LENGTH_SHORT).show();
					mBlinkOperation = null;
					activity.finish();
				}

				@Override
				public void onServiceFailed() {
					Toast.makeText(activity,
							"Blink Service connection was failed!!",
							Toast.LENGTH_SHORT).show();
					mBlinkOperation = null;
					activity.finish();
				}

				@Override
				public void onDiscoveryStarted() {
					mCurrentChildFragmentInterface.onDiscoveryStarted();
				}

				@Override
				public void onDiscoveryFinished() {
					mCurrentChildFragmentInterface.onDiscoveryFinished();
				}

			};
			mActivityInterface.setServiceInteration(mInteraction);
			mInteraction.startService();
		}
		if ((mHostDevice = mInteraction.obtainSelfDevice()) == null)
			mHostDevice = PrivateUtil.obtainHostDevice();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_connection, container, false);
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
		if (mFetchTasking) {
			Toast.makeText(getActivity(), "Discovery is already running.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		mDeviceList.clear();
		mFetchTasking = true;
		new DataLoaderTask(getActivity(), true).forceLoad();
		onPreLoading();
	}

	private final boolean fetchDeviceListBluetoothInternal() {
		try {
			mHostDevice = mBlinkOperation.obtainSelfDevice();
			if (mHostDevice == null)
				mHostDevice = PrivateUtil.obtainHostDevice();
			mBlinkOperation.startDiscovery(BluetoothDevice.DEVICE_TYPE_DUAL);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
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
		new DataLoaderTask(getActivity(), false).forceLoad();
		onPreLoading();
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

	void onPreLoading() {
		if (mConnectionTasking)
			mProgressDialog.show();
		else
			getActivity().setProgressBarIndeterminateVisibility(true);
		mCurrentChildFragmentInterface.onPreLoading();
	}

	void onPostLoading() {
		if (mConnectionTasking) {
			mProgressDialog.dismiss();
			mConnectionTasking = false;
		} else if (getActivity() != null) {
			getActivity().setProgressBarIndeterminateVisibility(false);
		}
		mCurrentChildFragmentInterface.onPostLoading();
	}

	/** Device 리스트의 변경이 실패하였을 때 호출된다. */
	void onDeviceListLoadFailed() {
		getActivity().setProgressBarIndeterminateVisibility(false);
		mCurrentChildFragmentInterface.onDeviceListLoadFailed();
	}

	void onFragmentChanged(BaseConnectionFragment callback) {
		this.mCurrentChildFragmentInterface = callback;
		if (mInteraction != null)
			mInteraction.setOnBlinkEventBroadcast(callback);
		// callback.setParentFragment(this);
		callback.setHasOptionsMenu(true);
		getChildFragmentManager().beginTransaction()
				.replace(R.id.fragment_connection_content, callback)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.commit();
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
							mFetchTasking = false;
							if (!data) {
								onPostLoading();
								onDeviceListLoadFailed();
							}
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
							onPostLoading();
							if (result && mIsConnectTask
									^ mDevice.isConnected()) {
								if (mIsConnectTask) {
									mCurrentChildFragmentInterface
											.onDeviceConnected(mDevice);
								} else {
									mCurrentChildFragmentInterface
											.onDeviceDisconnected(mDevice);
								}
								mCurrentChildFragmentInterface
										.onDeviceListChanged();
							} else {
								mCurrentChildFragmentInterface
										.onDeviceConnectionFailed(mDevice);
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
			mViewPager.setAdapter(new FragmentPagerAdapter(this
					.getChildFragmentManager()) {

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
			});
			return v;
		}

		protected void navigateTab(int position, boolean isFromPager) {
			if (isFromPager) {
				mTabHost.setCurrentTab(position);
			} else {
				mViewPager.setCurrentItem(position);
			}
		}

		private class BlinkDeviceInfoFragment extends Fragment {
			BlinkDevice mDevice = DeviceInfoDialogFragment.this.mBlinkDevice;

			@Override
			public View onCreateView(LayoutInflater inflater,
					ViewGroup container, Bundle savedInstanceState) {
				final View v = inflater.inflate(
						R.layout.dialog_fragment_connection_bluetooth_info,
						container, false);
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
			AlertDialog mAppDialog;

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
				View dialogView = View.inflate(getActivity(),
						R.layout.dialog_app, null);
				mAppDialog = new AlertDialog.Builder(getActivity())
						.setView(dialogView)
						.setPositiveButton(android.R.string.cancel, null)
						.create();
			}

			@Override
			public View onCreateView(LayoutInflater inflater,
					ViewGroup container, Bundle savedInstanceState) {
				final View v = inflater.inflate(
						R.layout.dialog_fragment_connection_db_info, container,
						false);
				ListView listView = (ListView) v
						.findViewById(android.R.id.list);
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						final App app = (App) parent
								.getItemAtPosition(position);
						mAppDialog.setTitle(app.AppName);
						mAppDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
								"Log", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										mActivityInterface.transitFragment(2,
												BundleResolver.toBundle(
														mDevice, app));
										((DialogFragment) DatabaseDeviceInfoFragment.this
												.getParentFragment()).dismiss();
									}
								});
						mAppDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
								"Data", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										mActivityInterface.transitFragment(1,
												BundleResolver.toBundle(
														mDevice, app));
										((DialogFragment) DatabaseDeviceInfoFragment.this
												.getParentFragment()).dismiss();
									}
								});
						mAppDialog.show();
					}
				});
				listView.setEmptyView(v.findViewById(android.R.id.empty));

				listView.setAdapter(mDialogListAdapter);
				return v;
			}
		}
	}

	/**
	 * ConnectionFragment의 위임 class, ConnectionFragment의 주요 기능을 처리한다.<br>
	 * '연결' 을 표시하려는 Fragment class는 이 class를 상속받아 구현하면 된다.
	 */
	abstract static class BaseConnectionFragment extends Fragment implements
			IConnectionCallback {
		private ConnectionFragment mParentFragment;

		@Override
		public void onDeviceListLoadFailed() {
			Toast.makeText(getActivity(), "operation failed!",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onPostLoading() {
			getActivity().setProgressBarIndeterminateVisibility(false);
		}

		@Override
		public void onPreLoading() {
		}

		List<BlinkDevice> getDeviceList() {
			return mParentFragment.mDeviceList;
		}

		BlinkDevice getHostDevice() {
			return mParentFragment.mHostDevice;
		}

		void showHostDeviceInfomation() {
			mParentFragment.showHostDeviceInfomation();
		}

		void showDialog(BlinkDevice device) {
			mParentFragment.showDialog(device);
		}

		void fetchDeviceListFromBluetooth() {
			mParentFragment.fetchDeviceListFromBluetooth();
		}

		void retainConnectedDevicesFromList() {
			mParentFragment.retainConnectedDevicesFromList();
		}

		void connectOrDisConnectDevice(BlinkDevice device) {
			mParentFragment.connectOrDisConnectDevice(device);
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			this.mParentFragment = (ConnectionFragment) getParentFragment();
			getActivity().setProgressBarIndeterminateVisibility(isFetching());
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			if (item.getItemId() == R.id.action_connection_view_change) {
				changeFragment();
				return true;
			} else
				return super.onOptionsItemSelected(item);
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
			getDeviceList().add(device);
			onDeviceListChanged();
		}

		@Override
		public void onDeviceConnectionFailed(BlinkDevice device) {
			Toast.makeText(getActivity(),
					device.getName() + " (Dis)connection failed!",
					Toast.LENGTH_SHORT).show();
		}

		void changeFragment() {
			mParentFragment.onFragmentChanged(getChangedFragment());
		}

		void showHostDeviceToList(boolean show) {
			if (show
					&& !mParentFragment.mDeviceList
							.contains(mParentFragment.mHostDevice)) {
				mParentFragment.mDeviceList.add(0, mParentFragment.mHostDevice);
			} else {
				mParentFragment.mDeviceList.remove(mParentFragment.mHostDevice);
			}
		}

		boolean isFetching() {
			return mParentFragment.mFetchTasking;
		}

		@Override
		public void onDiscoveryStarted() {
			getActivity().setProgressBarIndeterminateVisibility(true);
		}

		@Override
		public void onDiscoveryFinished() {
			getActivity().setProgressBarIndeterminateVisibility(false);
			onPostLoading();
			onDeviceListChanged();
			Toast.makeText(getActivity(), "Bluetooth discovery was finished.",
					Toast.LENGTH_SHORT).show();
		}

	}
}
