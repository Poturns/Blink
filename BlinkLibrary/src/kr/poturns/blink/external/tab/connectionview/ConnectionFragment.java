package kr.poturns.blink.external.tab.connectionview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.external.DBHelper;
import kr.poturns.blink.external.IServiceContolActivity;
import kr.poturns.blink.internal.comm.BlinkDevice;
import kr.poturns.blink.internal.comm.BlinkServiceInteraction;
import kr.poturns.blink.internal.comm.IBlinkEventBroadcast;
import kr.poturns.blink.internal.comm.IInternalOperationSupport;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 연결 정보를 보여주는 Abstract Frament<br>
 * */
public abstract class ConnectionFragment extends Fragment implements
		IBlinkEventBroadcast {
	protected ArrayList<BlinkDevice> mDeviceList = new ArrayList<BlinkDevice>();
	protected IServiceContolActivity mActivityInterface;
	private DBHelper mHelper;
	/** 각 Device의 간략한 정보를 나타내는 Dialog */
	private AlertDialog mSimpleInfoDialog;
	protected ArrayAdapter<String> mDialogListAdapter;
	protected static final int NO_SELECTION = -1;
	protected int mCurrentListViewSelection = NO_SELECTION;
	static IInternalOperationSupport mBlinkOperation;
	BlinkServiceInteraction mInteraction;

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		if (activity instanceof IServiceContolActivity) {
			mActivityInterface = (IServiceContolActivity) activity;
		}
		mInteraction = mActivityInterface.getServiceInteration();
		// mBlinkOperation = mActivityInterface.getOperationSupport();
		if (mInteraction != null) {
			mInteraction.setOnBlinkEventBroadcast(this);

		} else {
			mInteraction = new BlinkServiceInteraction(activity, this) {
				@Override
				public void onServiceConnected(
						IInternalOperationSupport iSupport) {
					mBlinkOperation = iSupport;
					if (mDeviceList != null && !mDeviceList.isEmpty()) {
						mDeviceList.clear();
					}
					try {
						fetchDeviceListFromBluetooth();
					} catch (RemoteException e) {
						e.printStackTrace();
						Toast.makeText(activity,
								"Blink Service connection failed!!",
								Toast.LENGTH_SHORT).show();
					}
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
			mInteraction.startService();
		}
	}

	/** Device 리스트가 변경되었을 때 호출된다. */
	protected abstract void onDeviceListChanged();

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (savedInstanceState != null) {
			mDeviceList
					.addAll((Collection<? extends BlinkDevice>) savedInstanceState
							.getParcelableArrayList("list"));
		}

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
		mDialogListAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1);

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

	protected final void refreshDB() {
		mHelper.refresh(getActivity());
	}

	protected final void fetchDeviceListFromBluetooth() throws RemoteException {
		BlinkDevice[] devices = mBlinkOperation.obtainCurrentDiscoveryList();
		// 만약 devices를 얻어오는 과정에서 Exception이 발생한다면
		// mDeviceList는 초기화 되지 않는다.
		mDeviceList.clear();
		for (BlinkDevice device : devices) {
			mDeviceList.add(device);
		}
		onDeviceListChanged();
	}

	protected final List<SystemDatabaseObject> getSystemDatabaseObjectByDevice(
			String device) {
		return mHelper.getSystemDatabaseObjectByDevice(device);
	}

	protected final List<String> getDeviceAppList(String device) {
		ArrayList<String> deviceAppList = new ArrayList<String>();
		for (SystemDatabaseObject obj : mHelper
				.getSystemDatabaseObjectByDevice(device)) {
			deviceAppList.add(obj.mApp.PackageName);
		}
		return deviceAppList;
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("list", mDeviceList);
	}

	/** 해당 Device에 대한 정보를 Dialog형식으로 보여준다. */
	protected final void showDialog(final BlinkDevice device) {
		final String[] titles = getResources().getStringArray(
				R.array.activity_sercive_control_menu_array);
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
				final Bundle bundle = new Bundle();
				bundle.putString(IServiceContolActivity.EXTRA_DEVICE,
						device.getName());
				if (mCurrentListViewSelection != NO_SELECTION) {
					bundle.putString(IServiceContolActivity.EXTRA_DEVICE_APP,
							mDialogListAdapter
									.getItem(mCurrentListViewSelection));
				}
				mActivityInterface.transitFragment(position, bundle);
			}
		};
		mDialogListAdapter.clear();
		// TODO get app List of device
		// mDialogListAdapter.addAll(getDeviceAppList(device));
		mSimpleInfoDialog.setTitle(device.getName());
		mSimpleInfoDialog.setButton(DialogInterface.BUTTON_NEGATIVE, titles[1],
				onClickListener);
		mSimpleInfoDialog.setButton(DialogInterface.BUTTON_NEUTRAL, titles[2],
				onClickListener);
		mSimpleInfoDialog.show();
	}

	/** 연결되지 않은 Device를 Host기기와 연결을 시도한다. */
	protected final boolean conectDevice(BlinkDevice device) {
		try {
			mBlinkOperation.connectDevice(device);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void onDestroy() {
		cancelCurrentRefreshOperation();
		super.onDestroy();
	}

	/** 현재 device 리스트에서 연결된 device만 남긴다. */
	protected final void retainConnectedDevicesFromList() {
		for (BlinkDevice device : mDeviceList) {
			if (!device.isConnected()) {
				mDeviceList.remove(device);
			}
		}
	}

	protected final void cancelCurrentRefreshOperation() {

	}

	@Override
	public void onDeviceConnected(BlinkDevice device) {
	}

	@Override
	public void onDeviceDisconnected(BlinkDevice device) {
	}

	@Override
	public void onDeviceDiscovered(BlinkDevice device) {
	}
}
