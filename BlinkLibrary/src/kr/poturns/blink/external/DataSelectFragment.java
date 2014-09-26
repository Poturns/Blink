package kr.poturns.blink.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import kr.poturns.blink.db.archive.Function;
import kr.poturns.blink.db.archive.IDatabaseObject;
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
import android.widget.AdapterView;
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
		final View view = inflater.inflate(
				R.layout.res_blink_fragment_data_select, container, false);
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
					.replace(
							R.id.res_blink_fragment_content_select_recent_list,
							new RecentListFragment())
					.replace(
							R.id.res_blink_framgent_content_select_device_list,
							new DeviceMapFragment()).commit();
		}
		Bundle arg = getArguments();
		if (PrivateUtil.obtainApp(arg) != null) {
			showMeasurementList(arg, getActivity().getActionBar().getTitle(),
					getActivity().getActionBar().getSubtitle());
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (PrivateUtil.isScreenSizeSmall(getActivity())) {
			inflater.inflate(R.menu.res_blink_fragment_data_select, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.res_blink_action_refresh) {
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

	/** {@link DataViewFragment}로 전환한다. */
	void changeFragment(Bundle arg, CharSequence title, CharSequence subTitle) {
		Bundle bundle = new Bundle(arg);
		bundle.putCharSequence("title", title);
		bundle.putCharSequence("subTitle", subTitle);
		Fragment f = new DataViewFragment();
		f.setArguments(bundle);
		getFragmentManager().beginTransaction()
				.add(R.id.res_blink_activity_main_fragment_content, f, "1")
				.hide(this).show(f)
				.addToBackStack(DataSelectFragment.class.getSimpleName())
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();
		arg.clear();
	}

	void showMeasurementList(Bundle bundle, CharSequence title,
			CharSequence subTitle) {
		Bundle arg = new Bundle(bundle);
		arg.putCharSequence("title", title);
		arg.putCharSequence("subTitle", subTitle);
		Fragment f = new MeasurementListFragment();
		f.setArguments(arg);
		getFragmentManager().beginTransaction()
				.add(R.id.res_blink_activity_main_fragment_content, f, "1")
				.hide(this).show(f)
				.addToBackStack(DataSelectFragment.class.getSimpleName())
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();
	}

	/** 최근에 변경된 이력이 있는 Device와 App 를 리스트의 형태로 보여준다 */
	private class RecentListFragment extends Fragment {
		List<Measurement> mRecentMeasurementList;
		ArrayAdapter<Measurement> mRecentListAdapter;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mRecentMeasurementList = mManager
					.obtainRecentModifiedMeasurement(PrivateUtil
							.isScreenSizeSmall(getActivity()) ? 7 : 5);
			mRecentListAdapter = new ArrayAdapter<Measurement>(getActivity(),
					R.layout.res_blink_list_recent, android.R.id.text1,
					mRecentMeasurementList) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View v = super.getView(position, convertView, parent);
					v.setBackgroundResource(R.drawable.res_blink_drawable_rectangle_box);
					final Measurement measurement = getItem(position);
					final App app = mManager
							.obtainAppByMeasurement(measurement);
					final Device device = mManager.obtainDeviceByApp(app);

					((TextView) v.findViewById(android.R.id.text1))
							.setText(device.Device);
					TextView appTextView = (TextView) v
							.findViewById(R.id.res_blink_fragment_list_recent_app);
					appTextView.setText(app.AppName);
					appTextView
							.setCompoundDrawablesRelativeWithIntrinsicBounds(
									PrivateUtil.obtainAppIcon(app,
											getResources()), null, null, null);
					String title = measurement.MeasurementName;
					if (title == null)
						title = PrivateUtil
								.obtainSplitMeasurementSchema(measurement);
					((TextView) v
							.findViewById(R.id.res_blink_fragment_list_recent_measurement))
							.setText(title);
					((TextView) v
							.findViewById(R.id.res_blink_fragment_list_recent_datetime))
							.setText(mManager.obtainMeasurementDataDateTime(
									measurement).replace(" ", "\n"));
					v.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							changeFragment(PrivateUtil.toBundle(device, app,
									measurement), getActivity().getActionBar()
									.getTitle(), getActivity().getActionBar()
									.getSubtitle());
						}
					});
					return v;
				}
			};
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(
					R.layout.res_blink_fragment_data_recent_list, container,
					false);
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
					R.layout.res_blink_fragment_data_devicemap, container,
					false);
			ExpandableListView listView = (ExpandableListView) view
					.findViewById(android.R.id.list);
			listView.setEmptyView(view.findViewById(android.R.id.empty));
			listView.setAdapter(mAdapter);

			// 표현할 데이터가 작으면, ExpandableListView를 펼쳐서 보여준다.
			int groupSize = mAdapter.getGroupCount();
			if (groupSize < (PrivateUtil.isScreenSizeSmall(getActivity()) ? 4
					: 7)) {
				for (int i = 0; i < groupSize; i++) {
					listView.expandGroup(i, true);
				}
			}
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
						R.drawable.res_blink_ic_action_hardware_phone, 0, 0, 0);
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
				holder.tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
						PrivateUtil.obtainAppIcon(item, getResources()), null,
						null, null);
				convertView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						DataSelectFragment.this.showMeasurementList(PrivateUtil
								.toBundle((Device) getGroup(groupPosition),
										item), getActivity().getActionBar()
								.getTitle(), getActivity().getActionBar()
								.getSubtitle());
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

	/**
	 * 선택된 {@link App}의 {@link Measurement}와 {@link Function}의 리스트를 보여주는
	 * Fragment
	 */
	private static class MeasurementListFragment extends Fragment {
		Device mDevice;
		App mApp;
		ArrayList<IDatabaseObject> mDatabaseObjectList;
		ArrayAdapter<IDatabaseObject> mAdapter;
		SqliteManagerExtended mManager;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mManager = ((IServiceContolActivity) getActivity())
					.getDatabaseHandler();
			Bundle bundle = getArguments();
			mDevice = PrivateUtil.obtainDevice(bundle);
			mApp = PrivateUtil.obtainApp(bundle);
			mDatabaseObjectList = new ArrayList<IDatabaseObject>();
			mDatabaseObjectList.addAll(mManager.obtainFunctionList(mApp));
			mDatabaseObjectList.addAll(mManager.obtainMeasurementList(mApp));
			mAdapter = new ArrayAdapter<IDatabaseObject>(getActivity(),
					android.R.layout.simple_list_item_2, android.R.id.text1,
					mDatabaseObjectList) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View v = super.getView(position, convertView, parent);
					v.setBackgroundResource(R.drawable.res_blink_selector_rectangle_box);
					IDatabaseObject item = getItem(position);
					TextView head = (TextView) v
							.findViewById(android.R.id.text1);
					TextView tail = (TextView) v
							.findViewById(android.R.id.text2);
					if (item instanceof Measurement) {
						Measurement measurement = (Measurement) item;
						head.setText(measurement.MeasurementName);
						head.setCompoundDrawablesRelativeWithIntrinsicBounds(
								R.drawable.res_blink_ic_action_statistics_chart,
								0, 0, 0);
						tail.setText(measurement.Description
								+ "\nCount : "
								+ mManager
										.obtainMeasurementDataListSize(measurement));
					} else if (item instanceof Function) {
						Function function = (Function) item;
						head.setText(function.Function);
						head.setCompoundDrawablesRelativeWithIntrinsicBounds(
								R.drawable.res_blink_ic_action_action_function,
								0, 0, 0);
						tail.setText(function.Description);
					}
					return v;
				}
			};
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(
					R.layout.res_blink_fragment_dataview_measurement_data_list,
					container, false);
			ListView listView = (ListView) v.findViewById(android.R.id.list);
			listView.setAdapter(mAdapter);
			listView.setEmptyView(v.findViewById(android.R.id.empty));
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Object item = parent.getItemAtPosition(position);
					if (item instanceof Measurement) {
						changeFragment(PrivateUtil.toBundle(mDevice, mApp,
								(Measurement) item), getActivity()
								.getActionBar().getTitle(), getActivity()
								.getActionBar().getSubtitle());
					}
					//TODO Function를 나타내는 Item을 선택하였을 때,
					// Function 실행요청을 보내야 하나?
				}
			});
			return v;
		}

		@Override
		public void onResume() {
			getActivity().getActionBar().setTitle(mDevice.Device);
			getActivity().getActionBar().setSubtitle(mApp.AppName);
			super.onResume();
		}

		@Override
		public void onPause() {
			Bundle arg = getArguments();
			getActivity().getActionBar().setTitle(arg.getCharSequence("title"));
			getActivity().getActionBar().setSubtitle(
					arg.getCharSequence("subTitle"));
			super.onPause();
		}

		/** {@link DataViewFragment}로 전환한다. */
		void changeFragment(Bundle arg, CharSequence title,
				CharSequence subTitle) {
			Bundle bundle = new Bundle(arg);
			bundle.putCharSequence("title", title);
			bundle.putCharSequence("subTitle", subTitle);
			Fragment f = new DataViewFragment();
			f.setArguments(bundle);
			getFragmentManager()
					.beginTransaction()
					.add(R.id.res_blink_activity_main_fragment_content, f, "1")
					.hide(this)
					.show(f)
					.addToBackStack(
							MeasurementListFragment.class.getSimpleName())
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.commit();
			arg.clear();
		}
	}
}
