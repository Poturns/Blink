package kr.poturns.blink.external;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.internal.DeviceAnalyzer.Identity;
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
import android.bluetooth.BluetoothDevice;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/** ConnectionFragment의 UI와 작업의 Callback을 처리하는 interface */
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

	/** Bluetooth Discovery가 종료되었을 때, 호출된다. */
	void onDiscoveryFinished();

	/** Bluetooth Discovery가 시작되었을 때, 호출된다. */
	void onDiscoveryStarted();

	/** Bluetooth Discovery가 실패하였을 때, 호출된다. */
	void onDiscoveryFailed();
}

/**
 * 연결 관련 처리를 하는 Frament<br>
 * <br>
 * Activity의 '연결' 항목을 터치하면 Activity의 FrameLayout에 로드되는 Fragment 이다. <br>
 * <br>
 * BaseConnectionFragment에서의 BlinkDevice 와 그 List관련 처리는 ConnectionFragment가 대신
 * 하고, UI관련 처리는 BaseConnectionFragment를 상속받는 class가 위임 한다.<br>
 * */
abstract class ConnectionFragment extends Fragment {
	public static final ConnectionFragment getFragment() {
		switch (PrivateUtil.DEVICE_TYPE) {
		case WAREABLE_WATCH:
			return new ConnectionWearableFragment();
		default:
			return new ConnectionHandHeldFragment();
		}
	}

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
	/** Blink network의 연결 중심 장비 */
	BlinkDevice mCenterDevice;
	ProgressDialog mProgressDialog;
	/** Bluetooth 사용 가능 여부 */
	boolean mBluetoothEnabled = true;
	/** BlinkDevice Discovery 작업 여부 */
	boolean mFetchTasking = false;
	/** UI를 표시하는 ChildFragment */
	IConnectionCallback mCurrentChildFragmentInterface;

	static final Handler sHandler = new Handler();
	/** ProgressDialog가 최대로 보여질 시간 */
	private static final long PROGRESS_WATING_TIME = 10 * 1000;

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		setHasOptionsMenu(true);
		if (activity instanceof IServiceContolActivity) {
			mActivityInterface = (IServiceContolActivity) activity;
		}
		mProgressDialog = new ProgressDialog(activity);
		mProgressDialog.setMessage("Loading...");
		mProgressDialog.setCancelable(false);
		mManager = mActivityInterface.getDatabaseHandler();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInteraction = mActivityInterface.getServiceInteration();
		if (mInteraction != null) {
			mBlinkOperation = mActivityInterface.getInternalOperationSupport();
			mInteraction
					.setOnBlinkEventBroadcast(mCurrentChildFragmentInterface);
		} else {
			initInteraction();
		}
	}

	private void initInteraction() {
		mInteraction = new BlinkServiceInteraction(getActivity(),
				mCurrentChildFragmentInterface, null) {
			@Override
			public void onServiceConnected(IInternalOperationSupport iSupport) {
				mBlinkOperation = iSupport;
				mActivityInterface.setInternalOperationSupport(iSupport);

				// 기존에 discovery 된 장비들을 불러온다.
				try {
					if (mDeviceList.isEmpty()) {
						for (BlinkDevice device : iSupport
								.obtainCurrentDiscoveryList()) {
							mDeviceList.add(device);
						}
					}
					if (mCurrentChildFragmentInterface != null)
						mCurrentChildFragmentInterface.onDeviceListChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onServiceDisconnected() {
				Toast.makeText(getActivity(),
						R.string.res_blink_blink_service_disabled,
						Toast.LENGTH_SHORT).show();
				mBlinkOperation = null;
			}

			@Override
			public void onServiceFailed() {
				// Service의 binding이 실패하면 현재 Activity를 종료한다.
				Toast.makeText(getActivity(),
						R.string.res_blink_blink_service_failed,
						Toast.LENGTH_SHORT).show();
				mBlinkOperation = null;
				getActivity().finish();
			}

			@Override
			public void onDiscoveryStarted() {
				sHandler.post(new Runnable() {
					@Override
					public void run() {
						onPreLoading(PROGRESS_OPT_ACTION_BAR);
						ConnectionFragment.this.mCurrentChildFragmentInterface
								.onDiscoveryStarted();
					}
				});
			}

			@Override
			public void onDiscoveryFinished() {
				sHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						onPostLoading(PROGRESS_OPT_ACTION_BAR);
						ConnectionFragment.this.mCurrentChildFragmentInterface
								.onDiscoveryFinished();
					}
				}, 1000);
			}
		};
		mActivityInterface.setServiceInteration(mInteraction);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// ChildFragment의 container역할을 하는 View를 생성한다.
		return inflater.inflate(R.layout.res_blink_fragment_connection,
				container, false);
	}

	@Override
	public void onResume() {
		if (mInteraction != null) {
			initInteraction();
		}
		mInteraction.startBroadcastReceiver();
		if (!mInteraction.isBinding()) {
			mInteraction.startService();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if (mInteraction != null) {
			mInteraction.stopBroadcastReceiver();
			mInteraction.stopService();
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		mInteraction.setOnBlinkEventBroadcast(null);
		getActivity().setProgressBarIndeterminateVisibility(false);
		mBlinkOperation = null;
		mManager = null;
		mDeviceList = null;
		mActivityInterface = null;
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
	final void connectOrDisConnectDevice(final BlinkDevice device) {
		final boolean isConnection = device.isConnected();
		AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if (isConnection) {
						mBlinkOperation.disconnectDevice(device);
					} else {
						mBlinkOperation.connectDevice(device);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		sHandler.removeCallbacks(mProgressDismissAction);
		sHandler.postDelayed(mProgressDismissAction, PROGRESS_WATING_TIME);
		onPreLoading(PROGRESS_OPT_DIALOG);
	}

	/** (실행중인) ProgressDialog를 dismiss하는 Action */
	Runnable mProgressDismissAction = new Runnable() {

		@Override
		public void run() {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mCurrentChildFragmentInterface.onDeviceListChanged();
			}
		}
	};

	/**
	 * Bluetooth Discovery를 시작해서 주변에 발견된 BlinkDevice의 list를 비동기적으로 가져온다. <br>
	 * <br>
	 * 작업에 성공하면 {@link IConnectionCallback#onDiscoveryFinished()}, <br>
	 * 실패하면 {@link IConnectionCallback#onDeviceListLoadFailed()}가 호출된다. <br>
	 * <br>
	 * 작업이 이미 진행 중 이거나, Bluetooth가 비활성화 되어있으면 {@link Toast}를 띄운다.
	 */
	final void fetchDeviceListFromBluetooth() {
		// Bluetooth가 사용 가능하지 않은 경우
		if (!mBluetoothEnabled
				|| !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			Toast.makeText(getActivity(),
					R.string.res_blink_bluetooth_disabled, Toast.LENGTH_SHORT)
					.show();
			mCurrentChildFragmentInterface.onDiscoveryFailed();
			return;
		}

		// 이미 discovery 중인 경우
		if (mFetchTasking) {
			Toast.makeText(getActivity(),
					R.string.res_blink_discovery_is_running, Toast.LENGTH_SHORT)
					.show();
			mCurrentChildFragmentInterface.onDiscoveryFailed();
			return;
		}

		// 혹여나 시행중인 discovery 종료
		try {
			mBlinkOperation.stopDiscovery();
		} catch (RemoteException e) {
		}
		mDeviceList.clear();
		mFetchTasking = true;
		if (!fetchDeviceListBluetoothInternal()) {
			onPostLoading(PROGRESS_OPT_ACTION_BAR);
			mCurrentChildFragmentInterface.onDeviceListLoadFailed();
		}
	}

	/**
	 * 비 동기적으로 실행될 {@link ConnectionFragment#fetchDeviceListFromBluetooth()}<br>
	 * 실제 작업을 수행한다.
	 */
	private final boolean fetchDeviceListBluetoothInternal() {
		try {
			mBlinkOperation.startDiscovery(BluetoothDevice.DEVICE_TYPE_DUAL);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 연결된 device만 가져온다.<br>
	 * <br>
	 * * 작업에 성공하면 {@link #onDeviceListChanged()} , 실패하면
	 * {@link #onDeviceListLoadFailed()}가 호출된다.
	 */
	final void retainConnectedDevicesFromList() {
		// onPreLoading();
		if (retainConnectedDevicesFromListInternal()) {
			mCurrentChildFragmentInterface.onDeviceListChanged();
		} else {
			mCurrentChildFragmentInterface.onDeviceListLoadFailed();
		}
		// onPostLoading();
	}

	/**
	 * 비 동기적으로 실행될 {@link ConnectionFragment#retainConnectedDevicesFromList()}<br>
	 * 실제 작업을 수행한다.
	 */
	private final boolean retainConnectedDevicesFromListInternal() {
		try {
			mDeviceList.clear();
			for (BlinkDevice device : mBlinkOperation
					.obtainConnectedDeviceList()) {
				mDeviceList.add(device);
			}
			mCurrentChildFragmentInterface.onDeviceListChanged();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/** 현재 DeviceList에 이전에 Discovery된 Device의 목록을 유지한다. */
	final void obtainDiscoveryList() {
		try {
			BlinkDevice[] devices = mBlinkOperation
					.obtainCurrentDiscoveryList();
			mDeviceList.clear();
			for (BlinkDevice device : devices) {
				mDeviceList.add(device);
			}
			mCurrentChildFragmentInterface.onDeviceListChanged();
		} catch (RemoteException e) {
			e.printStackTrace();
			mCurrentChildFragmentInterface.onDiscoveryFailed();
		}
	}

	/** 비동기 작업 전 호출된다. */
	void onPreLoading(int options) {
		if ((options & PROGRESS_OPT_DIALOG) == PROGRESS_OPT_DIALOG) {
			if (mProgressDialog != null)
				mProgressDialog.show();
		}
		if ((options & PROGRESS_OPT_ACTION_BAR) == PROGRESS_OPT_ACTION_BAR) {
			if (getActivity() != null) {
				getActivity().setProgressBarIndeterminateVisibility(true);
			}
		}
		mCurrentChildFragmentInterface.onPreLoading();
	}

	// Progress를 어떻게 나타낼것인가에 대한 옵션 
	public static final int PROGRESS_OPT_NONE = 0x00000001;
	public static final int PROGRESS_OPT_DIALOG = PROGRESS_OPT_NONE << 1;
	public static final int PROGRESS_OPT_ACTION_BAR = PROGRESS_OPT_DIALOG << 1;

	/** 비동기 작업 후 호출된다. */
	void onPostLoading(int options) {
		if ((options & PROGRESS_OPT_DIALOG) == PROGRESS_OPT_DIALOG) {
			if (mProgressDialog != null)
				mProgressDialog.dismiss();
		}
		if ((options & PROGRESS_OPT_ACTION_BAR) == PROGRESS_OPT_ACTION_BAR) {
			if (getActivity() != null) {
				getActivity().setProgressBarIndeterminateVisibility(false);
			}
		}
		if (mCurrentChildFragmentInterface != null)
			mCurrentChildFragmentInterface.onPostLoading();
	}

	/**
	 * ChildFragment를 변경할 때 호출한다.
	 * 
	 * @param callback
	 *            UI를 보여줄 ChildFragment
	 */
	void onFragmentChanged(BaseConnectionFragment callback) {
		this.mCurrentChildFragmentInterface = callback;
		if (mInteraction != null)
			mInteraction.setOnBlinkEventBroadcast(callback);
		// callback.setParentFragment(this);
		callback.setHasOptionsMenu(true);
		getChildFragmentManager().beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(R.id.res_blink_fragment_connection_content, callback)
				.commit();
	}

	/** BlinkDevice의 정보를 보여주는 DialogFragment */
	private class DeviceInfoDialogFragment extends DialogFragment {
		BlinkDevice mBlinkDevice;
		Device mDevice;
		private PrivateUtil.ViewPagerFragmentProxy mFragmentProxy = new PrivateUtil.ViewPagerFragmentProxy() {

			@Override
			protected Fragment getViewPagerPage(int position) {
				switch (position) {
				case 1:
					return new DatabaseDeviceInfoFragment();
				default:
					return new BlinkDeviceInfoFragment();
				}
			}

			@Override
			protected String[] getTitles() {
				return DeviceInfoDialogFragment.this.getResources()
						.getStringArray(
								R.array.res_blink_dialog_connect_page_titles);
			}

			@Override
			protected FragmentManager getFragmentManager() {
				return DeviceInfoDialogFragment.this.getChildFragmentManager();
			}

			@Override
			protected Activity getActivity() {
				return DeviceInfoDialogFragment.this.getActivity();
			}
		};

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mBlinkDevice = getArguments().getParcelable("blinkDevice");
			mDevice = ConnectionFragment.this.mManager
					.obtainDevice(mBlinkDevice);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return mFragmentProxy.onCreateView(inflater, container,
					savedInstanceState);
		}

		@Override
		public void onResume() {
			super.onResume();
			getDialog().setTitle(mBlinkDevice.getName());
			Point size = new Point();
			getActivity().getWindow().getDecorView().getDisplay().getSize(size);
			// Dialog의 크기를 결정한다.
			if (PrivateUtil.isScreenSizeSmall(getActivity())) {
				getDialog().getWindow().setLayout(size.x / 19 * 16,
						size.y / 19 * 15);
			} else {
				getDialog().getWindow().setLayout(size.x / 19 * 11,
						size.y / 19 * 12);
			}
		}

		@Override
		public void onDestroyView() {
			mFragmentProxy.onDestroyView();
			super.onDestroyView();
		}

		/**
		 * {@link DeviceInfoDialogFragment#mBlinkDevice}의 Bluetooth 관련 정보를 보여주는
		 * Fragment
		 */
		private class BlinkDeviceInfoFragment extends Fragment {
			BlinkDevice mDevice = DeviceInfoDialogFragment.this.mBlinkDevice;
			private static final int TYPE_TEXT = 0x01;
			private static final int TYPE_SWITCH = 0x02;

			BaseAdapter mAdapter = new BaseAdapter() {

				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					switch (getItemViewType(position)) {
					case TYPE_TEXT:
						return makeTextView(position, convertView, parent);
					case TYPE_SWITCH:
						return makeSwitchView(position, convertView, parent);
					}
					return null;
				}

				private View makeTextView(int position, View convertView,
						ViewGroup parent) {
					TextView title, content;

					if (convertView == null) {
						convertView = View.inflate(getActivity(),
								R.layout.res_blink_list_bluetooth_text, null);
						title = (TextView) convertView
								.findViewById(R.id.res_blink_list_bluetooth_title);
						content = (TextView) convertView
								.findViewById(R.id.res_blink_list_bluetooth_content);

						convertView.setTag(R.id.res_blink_list_bluetooth_title,
								title);
						convertView.setTag(
								R.id.res_blink_list_bluetooth_content, content);
					} else {
						title = (TextView) convertView
								.getTag(R.id.res_blink_list_bluetooth_title);
						content = (TextView) convertView
								.getTag(R.id.res_blink_list_bluetooth_content);
					}
					switch (position) {
					case 0:
						title.setText(R.string.res_blink_blink_state_macaddress);
						content.setText(mDevice.getAddress());
						break;
					case 1:
						title.setText(R.string.res_blink_blink_state_type);
						content.setText(mDevice.getIdentity().toString());
						break;
					}
					return convertView;
				}

				private View makeSwitchView(int position, View convertView,
						ViewGroup parent) {
					TextView title;
					Switch content;
					if (convertView == null) {
						convertView = View.inflate(getActivity(),
								R.layout.res_blink_list_bluetooth_switch, null);
						title = (TextView) convertView
								.findViewById(R.id.res_blink_list_bluetooth_title);
						content = (Switch) convertView
								.findViewById(R.id.res_blink_list_bluetooth_content);

						convertView.setTag(R.id.res_blink_list_bluetooth_title,
								title);
						convertView.setTag(
								R.id.res_blink_list_bluetooth_content, content);
					} else {
						title = (TextView) convertView
								.getTag(R.id.res_blink_list_bluetooth_title);
						content = (Switch) convertView
								.getTag(R.id.res_blink_list_bluetooth_content);
					}
					switch (position) {
					case 2:
						title.setText(R.string.res_blink_blink_state_support_library);
						content.setClickable(false);
						content.setChecked(mDevice.isBlinkSupported());
						break;
					case 3:
						title.setText(R.string.res_blink_blink_state_connection);
						content.setChecked(mDevice.isConnected());
						content.setClickable(false);
						break;
					case 4:
						title.setText(R.string.res_blink_blink_state_auto_connect);
						content.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								mDevice.setAutoConnect(isChecked);
							}
						});
						content.setChecked(mDevice.isAutoConnect());
						break;
					case 5:
						title.setText(R.string.res_blink_blink_state_ble);
						content.setClickable(false);
						content.setChecked(mDevice.isLESupported());
						break;
					}
					return convertView;
				}

				@Override
				public long getItemId(int position) {
					return 0;
				}

				@Override
				public int getItemViewType(int position) {
					if (position < 2)
						return TYPE_TEXT;
					else
						return TYPE_SWITCH;
				}

				@Override
				public Object getItem(int position) {
					return null;
				}

				@Override
				public int getCount() {
					return 6;
				}
			};

			@Override
			public View onCreateView(LayoutInflater inflater,
					ViewGroup container, Bundle savedInstanceState) {
				final View v = inflater
						.inflate(
								R.layout.res_blink_dialog_fragment_connection_bluetooth_info,
								container, false);
				ListView listView = (ListView) v
						.findViewById(android.R.id.list);
				listView.setAdapter(mAdapter);
				listView.setDividerHeight(30);
				listView.setDivider(null);
				return v;
			}
		}

		/**
		 * BlinkDatabase를 조회하여 {@link DeviceInfoDialogFragment#mBlinkDevice}의
		 * 정보를 보여주는 Fragment
		 */
		private class DatabaseDeviceInfoFragment extends Fragment {
			ArrayAdapter<App> mDialogListAdapter;
			List<App> mAppList;

			@Override
			public void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				mAppList = ConnectionFragment.this.mManager
						.obtainAppList(mDevice);
				mDialogListAdapter = new ArrayAdapter<App>(getActivity(),
						R.layout.res_blink_list_app, android.R.id.text1,
						mAppList) {
					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
						final App app = getItem(position);
						View v = super.getView(position, convertView, parent);

						TextView tv = (TextView) v
								.findViewById(android.R.id.text1);
						tv.setText(app.AppName);
						tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
								PrivateUtil.obtainAppIcon(app, getResources()),
								null, null, null);

						v.findViewById(
								R.id.res_blink_fragment_connection_db_info)
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {
										ConnectionFragment.this.mActivityInterface.transitFragment(
												1, PrivateUtil.toBundle(
														mDevice, app));
										((DialogFragment) DatabaseDeviceInfoFragment.this
												.getParentFragment()).dismiss();
									}
								});
						v.findViewById(
								R.id.res_blink_fragment_connection_log_info)
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {
										ConnectionFragment.this.mActivityInterface.transitFragment(
												2, PrivateUtil.toBundle(
														mDevice, app));
										((DialogFragment) DatabaseDeviceInfoFragment.this
												.getParentFragment()).dismiss();
									}
								});
						return v;
					}
				};
			}

			@Override
			public View onCreateView(LayoutInflater inflater,
					ViewGroup container, Bundle savedInstanceState) {
				final View v = inflater.inflate(
						R.layout.res_blink_dialog_fragment_connection_db_info,
						container, false);
				ListView listView = (ListView) v
						.findViewById(android.R.id.list);
				listView.setEmptyView(v.findViewById(android.R.id.empty));

				listView.setAdapter(mDialogListAdapter);
				return v;
			}
		}
	}

}

class ConnectionHandHeldFragment extends ConnectionFragment {
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		onFragmentChanged(new ConnectionCircularFragment());
	}
}

class ConnectionWearableFragment extends ConnectionFragment implements
		SwipeListener {
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		onFragmentChanged(new ConnectionWatchFragment());
	}

	@Override
	public boolean onSwipe(Direction direction) {
		if (mCurrentChildFragmentInterface instanceof SwipeListener) {
			return ((SwipeListener) mCurrentChildFragmentInterface)
					.onSwipe(direction);
		} else
			return false;
	}

	// Wearable에서 ActionBar에 바로 ProgressBar를 보여주는 것은
	// 실행 불가능 하기 때문에, 무조건 Dialog를 보여준다.
	@Override
	void onPreLoading(int options) {
		if ((options & PROGRESS_OPT_NONE) != PROGRESS_OPT_NONE) {
			if (mProgressDialog != null)
				mProgressDialog.show();
		}
	}

	@Override
	void onPostLoading(int options) {
		if ((options & PROGRESS_OPT_NONE) != PROGRESS_OPT_NONE) {
			if (mProgressDialog != null)
				mProgressDialog.dismiss();
		}
	}
}

/**
 * ConnectionFragment의 위임 class, ConnectionFragment의 주요 기능을 처리한다.<br>
 * '연결' 을 표시하려는 Fragment class는 이 class를 상속받아 구현하면 된다.
 */
abstract class BaseConnectionFragment extends Fragment implements
		IConnectionCallback {
	private ConnectionFragment mParentFragment;

	@Override
	public void onDeviceListLoadFailed() {
	}

	@Override
	public void onPostLoading() {
	}

	@Override
	public void onPreLoading() {
	}

	/** 현재 유지되고 있는 BlinkDevice의 리스트를 얻는다. */
	List<BlinkDevice> getDeviceList() {
		return mParentFragment.mDeviceList;
	}

	/** BlinkLibrary를 구동하고 있는 장비를 나타내는 BlinkDevice객체를 얻는다. */
	BlinkDevice getHostDevice() {
		return BlinkDevice.HOST;
	}

	/** Blink network의 연결 중심 장비 객체를 얻는다. */
	public BlinkDevice getCenterDevice() {
		return mParentFragment.mCenterDevice;
	}

	/**
	 * 해당 {@link BlinkDevice}에 관한 Dialog를 띄운다.
	 * 
	 * @param device
	 *            정보를 보려는 {@link BlinkDevice}
	 */
	void showBlinkDeviceInfoDialog(BlinkDevice device) {
		mParentFragment.showDialog(device);
	}

	/**
	 * Service를 통해Bluetooth Discovery를 시작해서 주변에 발견된 BlinkDevice의 list를 비동기적으로
	 * 가져온다. <br>
	 * <br>
	 * {@link BlinkDevice}가 Discovery될 때마다
	 * {@link BaseConnectionFragment#onDeviceDiscovered(BlinkDevice)}가 호출된다. <br>
	 * <br>
	 * 작업에 성공하면 {@link IConnectionCallback#onDiscoveryFinished()}, <br>
	 * 실패하면 {@link IConnectionCallback#onDeviceListLoadFailed()}가 호출된다. <br>
	 * <br>
	 * 작업이 이미 진행 중 이거나, Bluetooth가 비활성화 되어있으면 {@link Toast}를 띄운다.
	 */
	void fetchDeviceListFromBluetooth() {
		mParentFragment.fetchDeviceListFromBluetooth();
	}

	/** 현재 DeviceList에 이전에 Discovery된 Device의 목록을 유지한다. */
	void obtainDiscoveryList() {
		mParentFragment.obtainDiscoveryList();
	}

	/** 현재 DeviceList에서 연결된 Device만 남긴다. */
	void retainConnectedDevicesFromList() {
		mParentFragment.retainConnectedDevicesFromList();
	}

	/**
	 * 현재 기기와 {@link BlinkDevice}를 연결/연결해제 한다.
	 * 
	 * @param device
	 *            연결/연결 해제할 Device
	 */
	void connectOrDisConnectDevice(BlinkDevice device) {
		mParentFragment.connectOrDisConnectDevice(device);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mParentFragment = (ConnectionFragment) getParentFragment();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.res_blink_action_connection_view_change) {
			changeFragment();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDeviceConnected(final BlinkDevice device) {
		device.setConnected(true);
		logAndPostAboutConnection(device, "onDeviceConnected : ",
				R.string.res_blink_device_connected);
	}

	@Override
	public void onDeviceDisconnected(final BlinkDevice device) {
		device.setConnected(false);
		logAndPostAboutConnection(device, "onDeviceDisConnected : ",
				R.string.res_blink_device_disconnected);
	}

	/*
	 * 장비 연결 상태에 대한 정보를 로그캣에 남긴다.<br> UI Thread에서는 Toast를 남기고,
	 * onDeviceListChanged()를 호출하도록 한다.
	 */
	private void logAndPostAboutConnection(final BlinkDevice device,
			String logMsg, final int toastTextRes) {
		ConnectionFragment.sHandler
				.removeCallbacks(mParentFragment.mProgressDismissAction);
		Log.d("ConnectionFragment", logMsg + device);
		ConnectionFragment.sHandler.post(new Runnable() {
			@Override
			public void run() {
				mParentFragment.mProgressDialog.dismiss();
				Toast.makeText(
						mParentFragment.getActivity(),
						device.getName()
								+ mParentFragment.getString(toastTextRes),
						Toast.LENGTH_SHORT).show();
				onDeviceListChanged();
			}
		});
	}

	@Override
	public void onDeviceDiscovered(BlinkDevice device) {
		if (!mParentFragment.mDeviceList.contains(device))
			mParentFragment.mDeviceList.add(device);
		ConnectionFragment.sHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				onDeviceListChanged();
			}
		}, 100);
	}

	/** 다른 Fragment로 UI를 변경한다. */
	void changeFragment() {
		mParentFragment.onFragmentChanged(getChangedFragment());
	}

	/**
	 * 현재 {@link BaseConnectionFragment#fetchDeviceListFromBluetooth()} 가 작업 중
	 * 인지 여부를 반환한다.
	 */
	final boolean isFetching() {
		return mParentFragment.mFetchTasking;
	}

	@Override
	public void onDiscoveryStarted() {
		getActivity().setProgressBarIndeterminateVisibility(true);
	}

	@Override
	public void onDiscoveryFinished() {
		mParentFragment.mFetchTasking = false;
		onPostLoading();
		obtainDiscoveryList();
		onDeviceListChanged();
		Toast.makeText(getActivity(),
				R.string.res_blink_bluetooth_discovery_finished,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDeviceListChanged() {
		List<BlinkDevice> deviceList = mParentFragment.mDeviceList;
		final int size = deviceList.size();

		mParentFragment.mCenterDevice = null;
		for (int i = 0; i < size; i++) {
			BlinkDevice device = deviceList.get(i);
			if (device.getIdentity() == Identity.MAIN) {
				mParentFragment.mCenterDevice = device;
				// 리스트에서 Main device 제거
				// deviceList.remove(device);
				break;
			}
		}
		if (mParentFragment.mCenterDevice == null) {
			mParentFragment.mCenterDevice = BlinkDevice.HOST.getIdentity() == Identity.MAIN ? BlinkDevice.HOST
					: null;
		}

		// Center device와 Host device가 같지 않으면
		// 리스트에 Host device 추가
		// if (!BlinkDevice.HOST.getAddress().equals(
		// mParentFragment.mCenterDevice)) {
		// deviceList.add(BlinkDevice.HOST);
		// } else {
		// deviceList.remove(BlinkDevice.HOST);
		// }
	}

	@Override
	public void onDiscoveryFailed() {
		mParentFragment.mFetchTasking = false;
	}
}
