package kr.poturns.blink.external.tab.connectionview;

import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.external.DBHelper;
import kr.poturns.blink.external.IServiceContolActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 연결 정보를 보여주는 Abstract Frament<br>
 * */
public abstract class ConnectionFragment extends Fragment {
	protected ArrayList<String> mDeviceList = new ArrayList<String>();
	protected IServiceContolActivity mActivityInterface;
	private DBHelper mHelper;
	/** 각 Device의 간략한 정보를 나타내는 Dialog */
	private AlertDialog mSimpleInfoDialog;
	protected ArrayAdapter<String> mDialogListAdapter;
	protected static final int NO_SELECTION = -1;
	protected int mCurrentListViewSelection = NO_SELECTION;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof IServiceContolActivity) {
			mActivityInterface = (IServiceContolActivity) activity;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mDeviceList = new ArrayList<String>();
		mHelper = DBHelper.getInstance(getActivity());
		if (savedInstanceState != null) {
			mDeviceList.addAll(savedInstanceState.getStringArrayList("list"));
		} else {
			fetchDeviceListFromDB();
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

	protected final void fetchDeviceListFromDB() {
		mHelper.refresh(getActivity());
		mDeviceList.clear();
		mDeviceList.addAll(mHelper.getDeviceSet());
	}

	protected final void fetchDeviceListFromBluetooth() {

	}

	protected final List<SystemDatabaseObject> getSystemDatabaseObjectByDevice(
			String device) {
		return mHelper.getSystemDatabaseObjectByDevice(device);
	}

	protected final List<String> getDeviceAppList(String device) {
		ArrayList<String> deviceAppList = new ArrayList<String>();
		for (SystemDatabaseObject obj : mHelper
				.getSystemDatabaseObjectByDevice(device)) {
			deviceAppList.add(obj.mDeviceApp.App);
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
		outState.putStringArrayList("list", mDeviceList);
	}

	/** 해당 Device에 대한 정보를 Dialog형식으로 보여준다. */
	protected final void showDialog(final String device) {
		final String[] titles = getResources().getStringArray(
				R.array.activity_sercive_control_menu_array);
		final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int position;

				switch (which) {
				case DialogInterface.BUTTON_NEGATIVE: {
					position = 1;
					break;
				}
				case DialogInterface.BUTTON_NEUTRAL: {
					position = 2;
					break;
				}
				default:
					return;
				}
				final Bundle bundle = new Bundle();
				bundle.putString(IServiceContolActivity.EXTRA_DEVICE, device);
				if (mCurrentListViewSelection != NO_SELECTION) {
					bundle.putString(IServiceContolActivity.EXTRA_DEVICE_APP,
							mDialogListAdapter
									.getItem(mCurrentListViewSelection));
				}
				mActivityInterface.transitFragment(position, bundle);
			}
		};
		mDialogListAdapter.clear();
		mDialogListAdapter.addAll(getDeviceAppList(device));
		mSimpleInfoDialog.setTitle(device);
		mSimpleInfoDialog.setButton(DialogInterface.BUTTON_NEGATIVE, titles[1],
				onClickListener);
		mSimpleInfoDialog.setButton(DialogInterface.BUTTON_NEUTRAL, titles[2],
				onClickListener);
		mSimpleInfoDialog.show();
	}

	/** 연결되지 않은 Device를 Host기기와 연결을 시도한다. */
	protected final boolean conectDevice(String device) {
		return false;
	}

	@Override
	public void onDestroy() {
		cancelCurrentRefreshOperation();
		super.onDestroy();
	}

	protected final void cancelCurrentRefreshOperation() {

	}
}
