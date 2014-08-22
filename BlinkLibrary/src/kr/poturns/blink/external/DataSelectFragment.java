package kr.poturns.blink.external;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import kr.poturns.blink.R;
import kr.poturns.blink.db.SqliteManagerExtended;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

/**
 * '측정 데이터' 메뉴 진입시 처음으로 보여지는 화면을 구성하는 Fragment<br>
 * <br>
 * Database 내부의 Device의 목록과 Device에 따른 App의 항목을 보여준다.
 */
class DataSelectFragment extends Fragment {
	BaseExpandableListAdapter mAdapter;
	Map<Device, List<App>> mDeviceMap = new Hashtable<Device, List<App>>();
	SqliteManagerExtended mManager;
	Device mDevice;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mManager = ((IServiceContolActivity) getActivity())
				.getDatabaseHandler();
		Bundle arg = getArguments();

		if (arg == null) {
			mDeviceMap = mManager.obtainDeviceMap();
			if (mDeviceMap.isEmpty() && savedInstanceState != null) {
				mDeviceMap = (Map<Device, List<App>>) savedInstanceState
						.getSerializable("map");
			}
			mAdapter = new ContentAdapter(getActivity(), mDeviceMap);
		} else if (BundleResolver.obtainApp(arg) == null) {
			mDeviceMap = new Hashtable<Device, List<App>>();
			mDeviceMap.put(mDevice, mManager.obtainAppList(mDevice));
		} else {// argument에 device와 app 모두 있는 경우
			changeFragment(arg);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("map", (Serializable) mDeviceMap);
	}

	void changeFragment(Bundle arg) {
		Fragment f = new DataViewFragment();
		f.setArguments(arg);
		getFragmentManager()
				.beginTransaction()
				.add(R.id.activity_main_fragment_content, f,
						DataViewFragment.class.getSimpleName())
				.addToBackStack(DataSelectFragment.class.getSimpleName())
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_content_select,
				container, false);
		ExpandableListView listView = (ExpandableListView) view
				.findViewById(R.id.fragment_content_select_list);
		listView.setEmptyView(view.findViewById(android.R.id.empty));
		if (mAdapter != null)
			listView.setAdapter(mAdapter);
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_content_select, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		if (id == R.id.action_refresh) {
			refreshDeviceList();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	void refreshDeviceList() {
		mDeviceMap.clear();
		mDeviceMap.putAll(mManager.obtainDeviceMap());
		mAdapter.notifyDataSetChanged();
	}

	class ContentAdapter extends ViewTagExpandableAdapter<Device, App> {

		public ContentAdapter(Context context,
				Map<Device, ? extends List<App>> map) {
			super(context, R.layout.list_fragment_content_select,
					R.layout.list_fragment_content_select, map);
		}

		@Override
		protected void createGroupView(int groupPosition, boolean isExpanded,
				ViewHolder h) {
			final Device device = (Device) getGroup(groupPosition);
			Holder holder = (Holder) h;
			holder.tv.setText(device.Device);
			holder.button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					changeFragment(BundleResolver.toBundle(device, null));
				}
			});
		}

		@Override
		protected ViewHolder getViewHolder(View v, boolean isGroup) {
			return new Holder(v);
		}

		@Override
		protected void createChildView(final int groupPosition,
				int childPosition, boolean isLastChild, ViewHolder h) {
			Holder holder = (Holder) h;
			final App item = (App) getChild(groupPosition, childPosition);
			holder.tv.setText(item.AppName);
			holder.button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					changeFragment(BundleResolver.toBundle(
							(Device) getGroup(groupPosition), item));
				}
			});
		}

		private class Holder implements ViewHolder {
			TextView tv;
			View button;

			public Holder(View v) {
				tv = (TextView) v.findViewById(android.R.id.text1);
				button = v.findViewById(android.R.id.button1);
			}
		}
	}
}
