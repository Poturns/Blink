package kr.poturns.blink.external;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import kr.poturns.blink.R;
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
import android.bluetooth.BluetoothDevice;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
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
	/** BlinkLibrary를 구동하고 있는 장비를 나타내는 BlinkDevice객체 */
	BlinkDevice mHostDevice = BlinkDevice.HOST;
	ProgressDialog mProgressDialog;
	/** BlinkDevice 연결 작업 여부 */
	boolean mConnectionTasking = false;
	/** Bluetooth 사용 가능 여부 */
	boolean mBluetoothEnabled = true;
	/** BlinkDevice Discovery 작업 여부 */
	boolean mFetchTasking = false;
	/** UI를 표시하는 ChildFragment */
	IConnectionCallback mCurrentChildFragmentInterface;

	static final Handler sHandler = new Handler();

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

	/** {@link BlinkDevice}의 연결/연결해제 요청의 결과를 전달해주는 리스너 */
	public static interface DeviceConnectionResultListener {
		/**
		 * 연결 요청의 결과를 전달한다.
		 * 
		 * @param device
		 *            작업을 실시한 {@link BlinkDevice}
		 * @param connectionResult
		 *            연결 된 경우 true, 연결 해제된 경우 false
		 * @param isTaskFailed
		 *            작업의 실패 여부
		 */
		void onResult(BlinkDevice device, boolean connectionResult,
				boolean isTaskFailed);

	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		setHasOptionsMenu(true);
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

					// 기존에 discovery 된 장비들을 불러온다.
					try {
						for (BlinkDevice device : iSupport
								.obtainCurrentDiscoveryList()) {
							mDeviceList.add(device);
						}
						if (mCurrentChildFragmentInterface != null)
							mCurrentChildFragmentInterface
									.onDeviceListChanged();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onServiceDisconnected() {
					// Service와 연결이 끊기면 현재 Activity를 종료한다.
					Toast.makeText(activity,
							R.string.res_blink_blink_service_disabled,
							Toast.LENGTH_SHORT).show();
					mBlinkOperation = null;
					activity.finish();
				}

				@Override
				public void onServiceFailed() {
					// Service의 binding이 실패하면 현재 Activity를 종료한다.
					Toast.makeText(activity,
							R.string.res_blink_blink_service_failed,
							Toast.LENGTH_SHORT).show();
					mBlinkOperation = null;
					activity.finish();
				}

				@Override
				public void onDiscoveryStarted() {
					sHandler.post(new Runnable() {
						@Override
						public void run() {
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
							ConnectionFragment.this.mCurrentChildFragmentInterface
									.onDiscoveryFinished();
						}
					}, 1000);
				}
			};
			mActivityInterface.setServiceInteration(mInteraction);
			mInteraction.startService();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// ChildFragment의 container역할을 하는 View를 생성한다.
		return inflater.inflate(R.layout.res_blink_fragment_connection,
				container, false);
	}

	@Override
	public void onDestroy() {
		mInteraction.setOnBlinkEventBroadcast(null);
		getActivity().setProgressBarIndeterminateVisibility(false);
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
	final void connectOrDisConnectDevice(BlinkDevice device,
			DeviceConnectionResultListener l) {
		if (mConnectionTasking || mConnectionThread != null) {
			Toast.makeText(getActivity(), "already connection tasking",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (device.isConnected()) {
			mConnectionThread = new ConnectionTaskThread(device, false, l);
			mConnectionThread.start();
		} else {
			mConnectionThread = new ConnectionTaskThread(device, true, l);
			mConnectionThread.start();
		}
		mConnectionTasking = true;
		onPreLoading();
	}

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
			e.printStackTrace();
		}
		mDeviceList.clear();
		mFetchTasking = true;
		onPreLoading();
		if (!fetchDeviceListBluetoothInternal()) {
			onPostLoading();
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
	 * 현재 device 리스트에서 연결된 device만 남긴다.<br>
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

	/** BlinkLibrary를 구동하고 있는 장비에 관한 Dialog를 띄운다. */
	final void showHostDeviceInfomation() {
		showDialog(mHostDevice);
	}

	/** 비동기 작업 전 호출된다. */
	void onPreLoading() {
		if (mConnectionTasking)
			mProgressDialog.show();
		else
			getActivity().setProgressBarIndeterminateVisibility(true);
		mCurrentChildFragmentInterface.onPreLoading();
	}

	/** 비동기 작업 후 호출된다. */
	void onPostLoading() {
		if (mConnectionTasking) {
			if (mProgressDialog != null)
				mProgressDialog.dismiss();
			mConnectionTasking = false;
		} else if (getActivity() != null) {
			getActivity().setProgressBarIndeterminateVisibility(false);
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

	/** BlinkDevice의 연결 작업을 비동기적으로 수행하는 Thread */
	ConnectionTaskThread mConnectionThread;

	/** BlinkDevice의 연결 작업을 비동기적으로 수행하는 Thread */
	class ConnectionTaskThread extends Thread {
		private BlinkDevice mDevice;
		private boolean mIsConnectTask;
		private DeviceConnectionResultListener l;
		private AtomicBoolean mWating = new AtomicBoolean(true);
		private boolean mResult = false;
		private final String TAG = ConnectionTaskThread.class.getSimpleName();

		public ConnectionTaskThread(BlinkDevice device, boolean isConnectTask,
				final DeviceConnectionResultListener l) {
			this.mDevice = device;
			this.mIsConnectTask = isConnectTask;
			this.l = l;

			// 어떠한 경우에도 앱이 종료되면 이 Thread는 종료됨.
			setDaemon(true);
		}

		@Override
		public void run() {
			Log.d(TAG, "connection thread start!");
			try {
				if (mIsConnectTask) {
					mBlinkOperation.connectDevice(mDevice);
				} else {
					mBlinkOperation.disconnectDevice(mDevice);
				}
				mResult = true;
			} catch (Exception e) {
				e.printStackTrace();
				mResult = false;
			}
			mDevice = null;
			long startTime = System.currentTimeMillis();
			// wait
			while (mWating.get()) {
				try {
					synchronized (this) {
						wait(500);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 20초 기다린다.
				if (System.currentTimeMillis() - startTime > 20000) {
					sHandler.post(new Runnable() {

						@Override
						public void run() {
							ConnectionFragment.this.onPostLoading();
							Toast.makeText(
									getActivity(),
									R.string.res_blink_bluetooth_discovery_failed,
									Toast.LENGTH_SHORT).show();
							ConnectionFragment.this.mCurrentChildFragmentInterface
									.onDiscoveryFailed();
						}
					});
					Log.e(TAG, "connection callback not responding");
					ConnectionFragment.this.mConnectionThread = null;
					return;
				}
			}

			// must wake from onDeviceConnected() / onDeviceDisConnected()

			if (mDevice != null) {
				Log.d(TAG, "connection callback response");
				sHandler.post(new Runnable() {

					@Override
					public void run() {
						l.onResult(mDevice, mDevice.isConnected(), mResult);
						ConnectionFragment.this.onPostLoading();
					}
				});
			}

			Log.d(TAG, "Thread terminated");
			ConnectionFragment.this.mConnectionThread = null;
		}

		/** block된 Thread를 깨운다. */
		public void wakeUp(BlinkDevice device) {
			this.mDevice = device;
			mWating.getAndSet(false);
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
			mDevice = ConnectionFragment.this.mManager
					.obtainDevice(mBlinkDevice);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			getDialog().setTitle(mBlinkDevice.getName());

			final View v = inflater.inflate(
					R.layout.res_blink_dialog_fragment_connection_device_info,
					container, false);
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
							R.array.res_blink_dialog_connect_page_titles);
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
					.findViewById(R.id.res_blink_dialog_deviceinfo_viewpager);
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

		/**
		 * {@link ViewPager}또는 {@link TabHost}의 page를 이동한다.
		 * 
		 * @param position
		 *            움직일 page또는 tab의 인덱스
		 * @param isFromPager
		 *            이동 요청이 {@link ViewPager}로 부터 왔는지 여부
		 */
		protected void navigateTab(int position, boolean isFromPager) {
			if (isFromPager) {
				mTabHost.setCurrentTab(position);
			} else {
				mViewPager.setCurrentItem(position);
			}
		}

		@Override
		public void onResume() {
			super.onResume();
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

		/**
		 * {@link DeviceInfoDialogFragment#mBlinkDevice}의 Bluetooth 관련 정보를 보여주는
		 * Fragment
		 */
		private class BlinkDeviceInfoFragment extends Fragment {
			BlinkDevice mDevice = DeviceInfoDialogFragment.this.mBlinkDevice;

			@Override
			public View onCreateView(LayoutInflater inflater,
					ViewGroup container, Bundle savedInstanceState) {
				final View v = inflater
						.inflate(
								R.layout.res_blink_dialog_fragment_connection_bluetooth_info,
								container, false);
				((TextView) v
						.findViewById(R.id.res_blink_dialog_fragment_connection_macaddress))
						.setText(mDevice.getAddress());
				((TextView) v
						.findViewById(R.id.res_blink_dialog_fragment_connection_device_ego))
						.setText(mDevice.getIdentity().toString());
				Switch isBlinkSupport = (Switch) v
						.findViewById(R.id.res_blink_dialog_fragment_connection_blink_support);
				isBlinkSupport.setClickable(false);
				isBlinkSupport.setChecked(mDevice.isBlinkSupported());
				Switch isConnected = (Switch) v
						.findViewById(R.id.res_blink_dialog_fragment_connection_connection);
				isConnected.setChecked(mDevice.isConnected());
				isConnected
						.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(
									final CompoundButton buttonView,
									boolean isChecked) {
								ConnectionFragment.this
										.connectOrDisConnectDevice(
												mDevice,
												new DeviceConnectionResultListener() {

													@Override
													public void onResult(
															BlinkDevice device,
															boolean connectionResult,
															boolean isTaskFailed) {
														if (connectionResult)
															buttonView
																	.setChecked(true);
														else
															buttonView
																	.setChecked(false);
													}
												});
							}
						});
				Switch isAutoConnect = (Switch) v
						.findViewById(R.id.res_blink_dialog_fragment_connection_autoconnect);
				isAutoConnect
						.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								mDevice.setAutoConnect(isChecked);
							}
						});
				isAutoConnect.setChecked(mDevice.isAutoConnect());
				Switch isLESupported = (Switch) v
						.findViewById(R.id.res_blink_dialog_fragment_connection_ble);
				isLESupported.setClickable(false);
				isLESupported.setChecked(mDevice.isLESupported());
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

	/**
	 * ConnectionFragment의 위임 class, ConnectionFragment의 주요 기능을 처리한다.<br>
	 * '연결' 을 표시하려는 Fragment class는 이 class를 상속받아 구현하면 된다.
	 */
	abstract static class BaseConnectionFragment extends Fragment implements
			IConnectionCallback {
		private ConnectionFragment mParentFragment;

		@Override
		public void onDeviceListLoadFailed() {
		}

		@Override
		public void onPostLoading() {
			if (getActivity() != null)
				getActivity().setProgressBarIndeterminateVisibility(false);
		}

		@Override
		public void onPreLoading() {
		}

		/** 현재 유지되고 있는 BlinkDevice의 리스트를 얻는다. */
		List<BlinkDevice> getDeviceList() {
			return mParentFragment.mDeviceList;
		}

		/** BlinkLibrary를 구동하고 있는 장비를 나타내는 BlinkDevice객체를 얻는다.. */
		BlinkDevice getHostDevice() {
			return mParentFragment.mHostDevice;
		}

		/** BlinkLibrary를 구동하고 있는 장비에 관한 Dialog를 띄운다. */
		void showHostDeviceInfoDialog() {
			mParentFragment.showHostDeviceInfomation();
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
		 * Service를 통해Bluetooth Discovery를 시작해서 주변에 발견된 BlinkDevice의 list를
		 * 비동기적으로 가져온다. <br>
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
		 * @param l
		 *            작업 성공 후 호출 될 콜백
		 */
		void connectOrDisConnectDevice(BlinkDevice device,
				DeviceConnectionResultListener l) {
			mParentFragment.connectOrDisConnectDevice(device, l);
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			this.mParentFragment = (ConnectionFragment) getParentFragment();
			getActivity().setProgressBarIndeterminateVisibility(isFetching());
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			if (item.getItemId() == R.id.res_blink_action_connection_view_change) {
				changeFragment();
				return true;
			} else
				return super.onOptionsItemSelected(item);
		}

		/** 연결 요청 Thread 에게 연결 결과를 알린다. */
		private void wakeConnectionThread(BlinkDevice device) {
			if (mParentFragment.mConnectionThread != null) {
				mParentFragment.mConnectionThread.wakeUp(device);
			}
		}

		@Override
		public void onDeviceConnected(BlinkDevice device) {
			Log.d("ConnectionFragment", "onDeviceConnected : " + device);
			Toast.makeText(
					getActivity(),
					device.getName()
							+ getString(R.string.res_blink_device_connected),
					Toast.LENGTH_SHORT).show();
			wakeConnectionThread(device);
			onDeviceListChanged();
		}

		@Override
		public void onDeviceDisconnected(BlinkDevice device) {
			Log.d("ConnectionFragment", "onDeviceDisConnected : " + device);
			if (getActivity() != null)
				Toast.makeText(
						getActivity(),
						device.getName()
								+ getString(R.string.res_blink_device_disconnected),
						Toast.LENGTH_SHORT).show();
			wakeConnectionThread(device);
			onDeviceListChanged();
		}

		@Override
		public void onDeviceDiscovered(BlinkDevice device) {
			mParentFragment.mDeviceList.add(device);
			sHandler.postDelayed(new Runnable() {

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
		 * DeviceList에 HostDevice를 추가 할 것인지 결정한다.
		 * 
		 * @param show
		 *            true - 추가 / false - 추가하지 않음
		 */
		void showHostDeviceToList(boolean show) {
			if (show) {
				// 이미 있으면 새로 추가하지는 않음
				if (mParentFragment.mDeviceList
						.contains(mParentFragment.mHostDevice))
					return;
				mParentFragment.mDeviceList.add(0, mParentFragment.mHostDevice);
			} else {
				mParentFragment.mDeviceList.remove(mParentFragment.mHostDevice);
			}
		}

		/**
		 * 현재 {@link BaseConnectionFragment#fetchDeviceListFromBluetooth()} 가 작업
		 * 중 인지 여부를 반환한다.
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
		public void onDiscoveryFailed() {
			mParentFragment.mFetchTasking = false;
		}
	}
}
