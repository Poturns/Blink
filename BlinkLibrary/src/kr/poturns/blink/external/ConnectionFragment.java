package kr.poturns.blink.external;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

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
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 연결 정보를 보여주는 Abstract Frament<br>
 * */
abstract class ConnectionFragment extends Fragment implements
		IBlinkEventBroadcast {
	List<BlinkDevice> mDeviceList = new CopyOnWriteArrayList<BlinkDevice>();
	IServiceContolActivity mActivityInterface;
	/** 각 Device의 간략한 정보를 나타내는 Dialog */
	private AlertDialog mSimpleInfoDialog;
	ArrayAdapter<App> mDialogListAdapter;
	static final int NO_SELECTION = -1;
	int mCurrentListViewSelection = NO_SELECTION;
	IInternalOperationSupport mBlinkOperation;
	BlinkServiceInteraction mInteraction;
	SqliteManagerExtended mManager;
	BlinkDevice mHostDevice = BlinkDevice.obtainHostDevice();
	ProgressDialog mProgressDialog;
	//ReentrantLock mDeviceDataLock = new ReentrantLock();

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
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
			mInteraction = new BlinkServiceInteraction(activity, this) {
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		// dialog view inflates
		View mDialogContentView = View.inflate(getActivity(),
				R.layout.dialog_fragment_connection, null);
		ListView listView = (ListView) mDialogContentView
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
		listView.setEmptyView(mDialogContentView
				.findViewById(android.R.id.empty));
		mDialogListAdapter = new ArrayAdapter<App>(getActivity(),
				android.R.layout.simple_list_item_1) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				TextView tv = (TextView) v.findViewById(android.R.id.text1);
				tv.setText(getItem(position).AppName);
				return v;
			}
		};

		listView.setAdapter(mDialogListAdapter);

		mSimpleInfoDialog = new AlertDialog.Builder(getActivity())
				.setView(mDialogContentView)
				.setPositiveButton(android.R.string.ok, null)
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						mCurrentListViewSelection = NO_SELECTION;
					}
				}).create();
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
		final String[] titles = getResources().getStringArray(
				R.array.activity_sercive_control_menu_array);
		final Device device = mManager.obtainDevice(blinkDevice);
		final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int position;

				switch (which) {
				case DialogInterface.BUTTON_NEGATIVE: {
					position = 1; // data fragment
					break;
				}
				case DialogInterface.BUTTON_NEUTRAL: {
					position = 2; // log fragment
					break;
				}
				default:
					return;
				}
				App app = null;
				if (mCurrentListViewSelection != NO_SELECTION) {
					app = mDialogListAdapter.getItem(mCurrentListViewSelection);
				}
				mActivityInterface.transitFragment(position,
						BundleResolver.toBundle(device, app));
			}
		};
		mDialogListAdapter.clear();

		// 해당 device의 App List를 Dialog에 보여준다.
		mDialogListAdapter.addAll(mManager.obtainAppList(device));
		mSimpleInfoDialog.setTitle(blinkDevice.getName());
		mSimpleInfoDialog
				.setIcon(blinkDevice.isConnected() ? R.drawable.ic_action_device_access_bluetooth_connected
						: R.drawable.ic_action_device_access_bluetooth_connected);
		mSimpleInfoDialog.setButton(DialogInterface.BUTTON_NEGATIVE, titles[1],
				onClickListener);
		mSimpleInfoDialog.setButton(DialogInterface.BUTTON_NEUTRAL, titles[2],
				onClickListener);
		mSimpleInfoDialog.show();
	}

	/**
	 * 연결되지 않은 Device를 Host기기와 연결을 시도한다. 이미 연결된 Device인 경우, 연결 해제를 시도한다.<br>
	 * * 이 메소드는 실제 연결작업을 하는 것이 아니라, 비동기적으로 연결을 시도하는 것 이다.<br>
	 * <br>
	 * 작업이 완료되면 {@link #onDeviceConnected(BlinkDevice)}또는
	 * {@link #onDeviceDisconnected(BlinkDevice)}가 호출된다.
	 */
	final void connectOrDisConnectDevice(BlinkDevice device) {
		new ConnectionTask(getActivity(), device).forceLoad();
		onPreLoading();
	}

	@Override
	public void onDestroy() {
		mInteraction.setOnBlinkEventBroadcast(null);
		super.onDestroy();
	}

	/*private final boolean checkAndTryLock() {
		boolean result = mDeviceDataLock.isLocked();
		if (result) {
			onDeviceListLoadFailed(true);
		} else {
			mDeviceDataLock.lock();
		}
		return !result;
	}*/

	/**
	 * 주변에 발견된 BlinkDevice의 list를 가져온다. <br>
	 * <br>
	 * 작업에 성공하면 {@link #onDeviceListChanged()}, 실패하면
	 * {@link #onDeviceListLoadFailed()}가 호출된다.
	 */
	final void fetchDeviceListFromBluetooth() {
		//if (checkAndTryLock()) {
			new DataLoaderTask(getActivity(), true).forceLoad();
			onPreLoading();
		//}
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
		//if (checkAndTryLock()) {
			new DataLoaderTask(getActivity(), false).forceLoad();
			onPreLoading();
		//}
	}

	private final boolean retainConnectedDevicesFromListInternal() {
		boolean result = false;
		List<BlinkDevice> list = Collections.synchronizedList(mDeviceList);

		for (BlinkDevice device : list) {
			if (!device.isConnected()) {
				result |= !list.remove(device);
			}
		}

		result = !result;
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
		mProgressDialog.show();
	}

	void onPostLoading() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				/*try {
					mDeviceDataLock.unlock();
				} catch (Exception e) {
					e.printStackTrace();
				}*/
				mProgressDialog.dismiss();
			}
		});
	}

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
							onPostLoading();
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

	class ConnectionTask extends AsyncTaskLoader<BlinkDevice> {
		private BlinkDevice mDevice;

		public ConnectionTask(Context context, BlinkDevice device) {
			super(context);
			this.mDevice = device;
			this.registerListener(0,
					new AsyncTaskLoader.OnLoadCompleteListener<BlinkDevice>() {
						public void onLoadComplete(
								android.content.Loader<BlinkDevice> loader,
								BlinkDevice data) {
							onPostLoading();
							if (data == null) {
								onDeviceConnectionFailed(mDevice);
							} else {
								if (data.isConnected()) {
									onDeviceConnected(data);
								} else {
									onDeviceDisconnected(data);
								}
							}
							onDeviceListChanged();
							loader.abandon();
						}
					});
		}

		@Override
		public BlinkDevice loadInBackground() {
			try {
				if (mDevice.isConnected()) {
					mBlinkOperation.disconnectDevice(mDevice);
				} else {
					mBlinkOperation.connectDevice(mDevice);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				return null;
			}
			return mDevice;
		}
	}
}
