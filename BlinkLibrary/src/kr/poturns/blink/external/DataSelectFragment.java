package kr.poturns.blink.external;

import java.util.Date;
import java.util.List;
import java.util.Map;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Measurement;
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
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * '측정 데이터' 메뉴 진입시 처음으로 보여지는 화면을 구성하는 Fragment<br>
 * 
 * <br>
 * <br>
 * 
 * 보여지는 항목은 크게 두 부분으로 구성되어 있다.<br>
 * 
 * <br>
 * <li>최근에 변경된 이력이 있는 Device와 App 를 리스트의 형태로 보여준다</li> <br>
 * <li>Database 내부의 Device의 목록과 Device에 따른 App의 항목을 보여준다.</li> <br>
 * 
 */
class DataSelectFragment extends Fragment {
	SqliteManagerExtended mManager;
	Device mDevice;
	/** 현재 UI에 보이는 Fragment가 RecentList인지 여부 */
	boolean mShowRecent = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mManager = ((IServiceContolActivity) getActivity())
				.getDatabaseHandler();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_data_select,
				container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (PrivateUtil.isScreenSizeSmall(getActivity())) {
			changeInternalFragment();
		} else {
			getChildFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_content_select_recent_list,
							new RecentListFragment())
					.replace(R.id.framgent_content_select_device_list,
							new DeviceMapFragment()).commit();
		}
		Bundle arg = getArguments();
		if (BundleResolver.obtainApp(arg) != null) {
			changeFragment(arg);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (PrivateUtil.isScreenSizeSmall(getActivity())) {
			inflater.inflate(R.menu.fragment_data_select, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			changeInternalFragment();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	/** 현재 UI에 표시되는 Fragment를 바꾼다. 오직 작은 화면을 가진 기기에서만 호출된다. */
	private void changeInternalFragment() {
		if (mShowRecent) {
			getChildFragmentManager().beginTransaction()
					.replace(android.R.id.content, new DeviceMapFragment())
					.commit();
		} else {
			getChildFragmentManager().beginTransaction()
					.replace(android.R.id.content, new RecentListFragment())
					.commit();
		}
		mShowRecent = !mShowRecent;
	}

	void changeFragment(Bundle arg) {
		Bundle bundle = new Bundle(arg);
		Fragment f = new DataViewFragment();
		f.setArguments(bundle);
		getFragmentManager().beginTransaction()
				.add(R.id.activity_main_fragment_content, f, "1").hide(this)
				.show(f)
				.addToBackStack(DataSelectFragment.class.getSimpleName())
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();
		arg.clear();
	}

	/** 최근에 변경된 이력이 있는 Device와 App 를 리스트의 형태로 보여준다 */
	private class RecentListFragment extends Fragment {
		List<Measurement> mRecentMeasurementList;
		ArrayAdapter<Measurement> mRecentListAdapter;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			long timeBefore = 1000 * 60 * 60 * 24 * 7; // l week
			mRecentMeasurementList = mManager.obtainRecentModifiedMeasurement(
					new Date(System.currentTimeMillis() - timeBefore), -1);
			mRecentListAdapter = new ArrayAdapter<Measurement>(getActivity(),
					android.R.layout.simple_list_item_1, mRecentMeasurementList) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View v = super.getView(position, convertView, parent);
					((TextView) v.findViewById(android.R.id.text1))
							.setText(PrivateUtil
									.obtainSplitMeasurementSchema(getItem(position)));
					return v;
				}
			};
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.fragment_data_recent_list,
					container, false);
			ListView listView = (ListView) v.findViewById(android.R.id.list);
			listView.setAdapter(mRecentListAdapter);
			listView.setEmptyView(v.findViewById(android.R.id.empty));
			return v;
		}
	}

	/** Database 내부의 Device의 목록과 Device에 따른 App의 항목을 Map의 형태로 보여준다. */
	private class DeviceMapFragment extends Fragment {
		BaseExpandableListAdapter mAdapter;
		Map<Device, List<App>> mDeviceMap;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mDeviceMap = mManager.obtainDeviceMap();
			mAdapter = new ContentAdapter(getActivity(), mDeviceMap);
			final View view = inflater.inflate(
					R.layout.fragment_data_devicemap, container, false);
			ExpandableListView listView = (ExpandableListView) view
					.findViewById(android.R.id.list);
			listView.setEmptyView(view.findViewById(android.R.id.empty));
			listView.setAdapter(mAdapter);
			return view;
		}

		class ContentAdapter extends ViewHolderExpandableAdapter<Device, App> {

			public ContentAdapter(Context context,
					Map<Device, ? extends List<App>> map) {
				super(context, android.R.layout.simple_expandable_list_item_1,
						android.R.layout.simple_dropdown_item_1line, map);
			}

			@Override
			protected void createGroupView(int groupPosition,
					boolean isExpanded, View convertView, ViewHolder h) {
				final Device device = (Device) getGroup(groupPosition);
				Holder holder = (Holder) h;
				holder.tv.setText(device.Device);
				holder.tv.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.ic_action_hardware_phone, 0, 0, 0);
			}

			@Override
			protected ViewHolder getViewHolder(View v, boolean isGroup) {
				return new Holder(v);
			}

			@Override
			protected void createChildView(final int groupPosition,
					int childPosition, boolean isLastChild, View convertView,
					ViewHolder h) {
				Holder holder = (Holder) h;
				final App item = (App) getChild(groupPosition, childPosition);
				holder.tv.setText(item.AppName);
				convertView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						DataSelectFragment.this.changeFragment(BundleResolver
								.toBundle((Device) getGroup(groupPosition),
										item));
					}
				});
			}

			private class Holder implements ViewHolder {
				TextView tv;

				public Holder(View v) {
					tv = (TextView) v.findViewById(android.R.id.text1);
				}
			}
		}
	}
}
