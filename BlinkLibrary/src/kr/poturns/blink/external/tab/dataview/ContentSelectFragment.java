package kr.poturns.blink.external.tab.dataview;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.SystemDatabaseObject;
import kr.poturns.blink.external.DBHelper;
import kr.poturns.blink.external.IServiceContolActivity;
import kr.poturns.blink.external.ViewTagExpandableAdapter;
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

public class ContentSelectFragment extends Fragment {
	BaseExpandableListAdapter mAdapter;
	Map<String, List<SystemDatabaseObject>> mDeviceMap;
	DBHelper mHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arg = getArguments();
		mHelper = DBHelper.getInstance(getActivity());
		if (arg != null) {
			changeFragment(arg);
		}
		if (savedInstanceState != null) {
			// mDeviceMap = savedInstanceState.
			mDeviceMap = new Hashtable<String, List<SystemDatabaseObject>>();
		} else {
			mDeviceMap = mHelper.getDeviceMap();
		}
		mAdapter = new ContentAdapter(getActivity(),
				android.R.layout.simple_expandable_list_item_1,
				android.R.layout.simple_expandable_list_item_1, mDeviceMap);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	protected void changeFragment(Bundle arg) {
		Fragment f = Fragment.instantiate(getActivity(),
				DataViewFragment.class.getName(), arg);
		getFragmentManager()
				.beginTransaction()
				.add(R.id.activity_main_fragment_content, f,
						DataViewFragment.class.getSimpleName())
				.addToBackStack(ContentSelectFragment.class.getSimpleName())
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
		mHelper.refresh(getActivity());
		mDeviceMap.clear();
		mDeviceMap.putAll(mHelper.getDeviceMap());
		mAdapter.notifyDataSetChanged();
	}

	class ContentAdapter extends
			ViewTagExpandableAdapter<String, SystemDatabaseObject> {

		public ContentAdapter(Context context, int groupResId, int childResId,
				Map<String, ? extends List<SystemDatabaseObject>> map) {
			super(context, groupResId, childResId, map);
		}

		@Override
		protected void createGroupView(int groupPosition, boolean isEcpanded,
				ViewHolder h) {
			((Holder) h).tv.setText(getGroup(groupPosition).toString());
		}

		@Override
		protected ViewHolder getViewHolder(View v, boolean isGroup) {
			return new Holder(v);
		}

		@Override
		protected void createChildView(int groupPosition, int childPosition,
				boolean isLastChild, ViewHolder h) {
			Holder holder = (Holder) h;
			final SystemDatabaseObject item = (SystemDatabaseObject) getChild(
					groupPosition, childPosition);
			holder.tv.setText(item.mApp.PackageName);
			holder.tv.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Bundle b = new Bundle();
					b.putString(IServiceContolActivity.EXTRA_DEVICE,
							item.mDevice.Device);
					b.putString(IServiceContolActivity.EXTRA_DEVICE_APP,
							item.mApp.PackageName);
					changeFragment(b);
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
