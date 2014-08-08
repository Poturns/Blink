package kr.poturns.blink.external.tab.dataview;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.DeviceAppList;
import kr.poturns.blink.external.IServiceContolActivity;
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
import android.widget.Toast;

public class ContentSelectFragment extends Fragment {
	BaseExpandableListAdapter mAdapter;
	Map<String, ? extends List<DeviceAppList>> mDeviceMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arg = getArguments();
		if (arg != null
				&& arg.getString(IServiceContolActivity.EXTRA_DEVICE) != null) {

			Fragment f = Fragment.instantiate(getActivity(),
					DataViewFragment.class.getName(), arg);
			getFragmentManager()
					.beginTransaction()
					.add(R.id.activity_main_fragment_content, f,
							DataViewFragment.class.getSimpleName())
					.addToBackStack(ContentSelectFragment.class.getSimpleName())
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.commit();
		} else {
			if (savedInstanceState != null) {
				// mDeviceMap = savedInstanceState.
				mDeviceMap = new Hashtable<String, List<DeviceAppList>>();
			} else {
				mDeviceMap = new Hashtable<String, List<DeviceAppList>>();
			}
			// mAdapter = new ContentAdapter(getActivity(), android.R.layout.,
			// childResId, mDeviceMap);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_content_select,
				container, false);
		ExpandableListView listView = (ExpandableListView) view
				.findViewById(R.id.fragment_content_select_list);
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
		Toast.makeText(getActivity(), "새로고침된 리스트를 보여줄 예정", Toast.LENGTH_SHORT)
				.show();
	}

	class ContentAdapter extends BaseExpandableListAdapter {
		private Map<String, ? extends List<DeviceAppList>> mDataMap;
		private String[] mKeyArray;
		// private Context mContext;
		private int mGroupResId, mChildResId;
		private LayoutInflater mInflater;

		public ContentAdapter(Context context, int groupResId, int childResId,
				Map<String, ? extends List<DeviceAppList>> map) {
			Set<String> keySet = map.keySet();
			mKeyArray = keySet.toArray(mKeyArray);
			this.mChildResId = childResId;
			this.mGroupResId = groupResId;
			// this.mContext = context;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getGroupCount() {
			return mDataMap.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mDataMap.get(mKeyArray[groupPosition]).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mDataMap.get(mKeyArray[groupPosition]);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mDataMap.get(mKeyArray[groupPosition]).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return groupPosition * 1000 + childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(mGroupResId, parent, false);
			} else {

			}

			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(mChildResId, parent, false);
			} else {

			}
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public void notifyDataSetChanged() {
			mKeyArray = mDataMap.keySet().toArray(mKeyArray);
			super.notifyDataSetChanged();
		}

	}
}
