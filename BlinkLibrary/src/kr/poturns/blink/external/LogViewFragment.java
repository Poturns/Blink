package kr.poturns.blink.external;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import kr.poturns.blink.R;
import kr.poturns.blink.db.archive.App;
import kr.poturns.blink.db.archive.Device;
import android.app.Fragment;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

/** Blink Database에 기록된 Log를 보여주는 Fragment */
class LogViewFragment extends Fragment {
	/** ListAdapter */
	ArrayAdapter<ExternalDeviceAppLog> mArrayAdapter;
	/** Log 정렬하는 {@link Comparator} */
	ExternalDeviceAppLog.LogComparator mLogComparator;
	/** Log 리스트 */
	ArrayList<ExternalDeviceAppLog> mLogList;
	/** 현재 정렬 기준이 되는 Device, App */
	Device mDevice;
	App mApp;
	SqliteManagerExtended mManager;
	int mPrevTitleViewSelectionId;
	int[] mTitleViewsIdArray = new int[] { R.id.fragment_logview_text_device,
			R.id.fragment_logview_text_app, R.id.fragment_logview_text_content,
			R.id.fragment_logview_text_datetime };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mManager = ((IServiceContolActivity) getActivity())
				.getDatabaseHandler();
		if (savedInstanceState != null) {
			mLogList = savedInstanceState.getParcelableArrayList("list");
		} else {
			mLogList = new ArrayList<ExternalDeviceAppLog>();
		}
		checkArgumentsFromOtherFragment();

		mArrayAdapter = new LogArrayAdapter(getActivity(),
				R.layout.list_fragment_logview, mLogList);
		mLogComparator = new ExternalDeviceAppLog.LogComparator();
	}

	/** Fragment의 Argument를 확인하여 값이 있다면 해당 값에 적절한 Log를 불러온다. */
	private void checkArgumentsFromOtherFragment() {
		checkArgumentAndResolveData();
		if (mLogList.isEmpty()) {
			getLoader(
					new Loader.OnLoadCompleteListener<List<ExternalDeviceAppLog>>() {
						@Override
						public void onLoadComplete(
								Loader<List<ExternalDeviceAppLog>> loader,
								List<ExternalDeviceAppLog> data) {
							mArrayAdapter.clear();
							mArrayAdapter.addAll(data);
							mArrayAdapter.notifyDataSetChanged();
							loader.abandon();
						}
					}).forceLoad();
		}
	}

	void checkArgumentAndResolveData() {
		Bundle arg = getArguments();
		if (arg != null && !arg.isEmpty()) {
			mDevice = BundleResolver.obtainDevice(arg);
			mApp = BundleResolver.obtainApp(arg);
			StringBuilder subTitle = new StringBuilder(mDevice.Device);
			if (mApp != null)
				subTitle.append(" / ").append(mApp.AppName);
			getActivity().getActionBar().setSubtitle(subTitle.toString());
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			checkArgumentAndResolveData();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("list", mLogList);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_logview,
				container, false);
		View titleLayout = view.findViewById(R.id.fragment_logview_table_title);
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setAdapter(mArrayAdapter);
		listView.setEmptyView(view.findViewById(android.R.id.empty));
		for (int id : mTitleViewsIdArray) {
			titleLayout.findViewById(id).setOnClickListener(
					mTitleViewOnClickListener);
		}
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_logview, menu);
		final MenuItem searchMenu = menu.findItem(R.id.action_search);

		SearchView searchView = (SearchView) searchMenu.getActionView();
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				mArrayAdapter.getFilter().filter(query);
				searchMenu.collapseActionView();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return true;
			}
		});
		searchView.setSubmitButtonEnabled(true);
		searchView.setQueryHint(getText(R.string.hint));
		searchMenu.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		getActivity().getActionBar().setSubtitle(null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		if (id == R.id.action_refresh) {
			// Log 새로고침
			getLoader(
					new Loader.OnLoadCompleteListener<List<ExternalDeviceAppLog>>() {
						@Override
						public void onLoadComplete(
								Loader<List<ExternalDeviceAppLog>> loader,
								List<ExternalDeviceAppLog> data) {
							mArrayAdapter.clear();
							mArrayAdapter.addAll(data);
							mArrayAdapter.notifyDataSetChanged();
							loader.abandon();
						}
					}).forceLoad();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	/** 제목을 나타내는 View에 등록되어 터치하면 List를 정렬하게 한다. */
	private View.OnClickListener mTitleViewOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			final int id = v.getId();
			for (int i = 0; i < mTitleViewsIdArray.length; i++) {
				// 현재 터치된 View 탐지
				if (mTitleViewsIdArray[i] == id) {
					// 현재 정렬하려는 Field가 이전에 정렬한 Field와 같은 경우
					// 현재 정렬 순서의 역순으로 정렬시킨다.
					// (정렬을 처음으로 시도하는 경우, mComparatorField 값이 0이므로 무조건 오름차순으로
					// 정렬한다.)
					if (mLogComparator.getComparatorField() == ExternalDeviceAppLog
							.getFieldConstantByOrder(i))
						mLogComparator.setOrder(!mLogComparator.getOrder());
					else
						mLogComparator.setOrder(true);
					mLogComparator.setComparatorField(i);
					break;
				}
			}
			mArrayAdapter.sort(mLogComparator);
		}
	};

	private class LogArrayAdapter extends ArrayAdapter<ExternalDeviceAppLog> {
		private int mResource;
		private Filter mFilter;

		public LogArrayAdapter(Context context, int resource,
				List<ExternalDeviceAppLog> objects) {
			super(context, resource, objects);
			this.mResource = resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView[] textViewArray = new TextView[ExternalDeviceAppLog.FIELD_SIZE];
			ExternalDeviceAppLog item = getItem(position);
			if (convertView == null) {
				convertView = View.inflate(getContext(), mResource, null);
				for (int i = 0; i < mTitleViewsIdArray.length; i++) {
					int id = mTitleViewsIdArray[i];
					textViewArray[i] = (TextView) convertView.findViewById(id);
					textViewArray[i].setText(item.getField(ExternalDeviceAppLog
							.getFieldConstantByOrder(i)));
					convertView.setTag(id, textViewArray[i]);
				}
			} else {
				for (int i = 0; i < mTitleViewsIdArray.length; i++) {
					textViewArray[i] = (TextView) convertView
							.getTag(mTitleViewsIdArray[i]);
					textViewArray[i].setText(item.getField(ExternalDeviceAppLog
							.getFieldConstantByOrder(i)));
				}
			}

			return convertView;
		}

		@Override
		public Filter getFilter() {
			if (mFilter == null)
				mFilter = new LogFilter();
			return mFilter;
		}

		private class LogFilter extends Filter {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				ArrayList<ExternalDeviceAppLog> filteringList = new ArrayList<ExternalDeviceAppLog>();
				final int size = getCount();
				for (int i = 0; i < size; i++) {
					ExternalDeviceAppLog item = getItem(i);
					// TODO 여기서 특정한 항목에 대한 Filtering을 할 수 있음
					for (int j = 0; j < ExternalDeviceAppLog.FIELD_SIZE; j++) {
						if (item.fieldArray[j]
								.startsWith(constraint.toString()))
							filteringList.add(item);
					}
				}
				FilterResults results = new FilterResults();
				results.count = filteringList.size();
				results.values = filteringList;
				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {

				if (results.count > 0) {
					clear();
					addAll((List<ExternalDeviceAppLog>) results.values);
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}

			}
		}
	}

	/**
	 * 비동기 작업을 하는 Loader 객체를 얻는다.
	 * 
	 * @param l
	 *            작업이 끝난 후, 실행 될 리스너
	 * @return {@link Loader}
	 */
	Loader<List<ExternalDeviceAppLog>> getLoader(
			Loader.OnLoadCompleteListener<List<ExternalDeviceAppLog>> l) {
		Loader<List<ExternalDeviceAppLog>> loader = new LogLoader(
				getActivity(), mDevice, mApp);
		loader.registerListener(0, l);
		return loader;
	}

	class LogLoader extends AsyncTaskLoader<List<ExternalDeviceAppLog>> {
		Device device;
		App app;

		public LogLoader(Context context, Device device, App app) {
			super(context);
			this.device = device;
			this.app = app;
		}

		@Override
		public List<ExternalDeviceAppLog> loadInBackground() {
			if (app != null) {
				return ExternalDeviceAppLog.convert(mManager.obtainLog(
						device.Device, app.AppName, null, null));
			} else if (device != null) {
				return ExternalDeviceAppLog.convert(mManager.obtainLog(
						device.Device, null, null));
			} else {
				return ExternalDeviceAppLog.convert(mManager.obtainLog());
			}
		}

	}

}
