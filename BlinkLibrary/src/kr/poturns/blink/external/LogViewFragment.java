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
	ArrayAdapter<ExternalBlinkLog> mArrayAdapter;
	/** Log 정렬하는 {@link Comparator} */
	ExternalBlinkLog.LogComparator mLogComparator;
	/** Log 리스트 */
	ArrayList<ExternalBlinkLog> mLogList;
	/** 현재 정렬 기준이 되는 Device, App */
	Device mDevice;
	App mApp;
	SqliteManagerExtended mManager;
	int mPrevTitleViewSelectionId;
	int[] mTitleViewsIdArray = new int[] {
			R.id.res_blink_fragment_logview_text_device,
			R.id.res_blink_fragment_logview_text_app,
			R.id.res_blink_fragment_logview_text_content,
			R.id.res_blink_fragment_logview_text_datetime };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mManager = ((ServiceControlActivity) getActivity()).getInterface()
				.getDatabaseHandler();
		if (savedInstanceState != null) {
			mLogList = savedInstanceState.getParcelableArrayList("list");
		} else {
			mLogList = new ArrayList<ExternalBlinkLog>();
		}
		checkArgumentsFromOtherFragment();

		mArrayAdapter = new LogArrayAdapter(getActivity(),
				R.layout.res_blink_list_fragment_logview, mLogList);
		mLogComparator = new ExternalBlinkLog.LogComparator();
	}

	/** Fragment의 Argument를 확인하여 값이 있다면 해당 값에 적절한 Log를 불러온다. */
	private void checkArgumentsFromOtherFragment() {
		checkArgumentAndResolveData();
		if (mLogList.isEmpty()) {
			getLoader(
					new Loader.OnLoadCompleteListener<List<ExternalBlinkLog>>() {
						@Override
						public void onLoadComplete(
								Loader<List<ExternalBlinkLog>> loader,
								List<ExternalBlinkLog> data) {
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
			mDevice = PrivateUtil.Bundles.obtainDevice(arg);
			mApp = PrivateUtil.Bundles.obtainApp(arg);
			StringBuilder subTitle = new StringBuilder(mDevice.Device);
			if (mApp != null)
				subTitle.append(" / ").append(mApp.AppName);
			getActivity().getActionBar().setSubtitle(subTitle.toString());
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
		final View view = inflater.inflate(R.layout.res_blink_fragment_logview,
				container, false);
		View titleLayout = view
				.findViewById(R.id.res_blink_fragment_logview_table_title);
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
		inflater.inflate(R.menu.res_blink_fragment_logview, menu);
		final MenuItem searchMenu = menu.findItem(R.id.res_blink_action_search);

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
		searchView.setQueryHint(getText(R.string.res_blink_hint));
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
		if (id == R.id.res_blink_action_refresh) {
			// Log 새로고침
			getLoader(
					new Loader.OnLoadCompleteListener<List<ExternalBlinkLog>>() {
						@Override
						public void onLoadComplete(
								Loader<List<ExternalBlinkLog>> loader,
								List<ExternalBlinkLog> data) {
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
					if (mLogComparator.getComparatorField() == ExternalBlinkLog
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

	private class LogArrayAdapter extends ArrayAdapter<ExternalBlinkLog> {
		private int mResource;
		private Filter mFilter;

		public LogArrayAdapter(Context context, int resource,
				List<ExternalBlinkLog> objects) {
			super(context, resource, objects);
			this.mResource = resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView[] textViewArray = new TextView[ExternalBlinkLog.FIELD_SIZE];
			ExternalBlinkLog item = getItem(position);
			if (convertView == null) {
				convertView = View.inflate(getContext(), mResource, null);
				for (int i = 0; i < mTitleViewsIdArray.length; i++) {
					int id = mTitleViewsIdArray[i];
					textViewArray[i] = (TextView) convertView.findViewById(id);
					textViewArray[i].setText(item.getField(ExternalBlinkLog
							.getFieldConstantByOrder(i)));
					convertView.setTag(id, textViewArray[i]);
				}
			} else {
				for (int i = 0; i < mTitleViewsIdArray.length; i++) {
					textViewArray[i] = (TextView) convertView
							.getTag(mTitleViewsIdArray[i]);
					textViewArray[i].setText(item.getField(ExternalBlinkLog
							.getFieldConstantByOrder(i)));
				}
			}
			convertView
					.setBackgroundResource(R.drawable.res_blink_selector_rectangle_box);
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
				ArrayList<ExternalBlinkLog> filteringList = new ArrayList<ExternalBlinkLog>();
				final int size = getCount();
				for (int i = 0; i < size; i++) {
					ExternalBlinkLog item = getItem(i);
					// TODO 여기서 특정한 항목에 대한 Filtering을 할 수 있음
					for (int j = 0; j < ExternalBlinkLog.FIELD_SIZE; j++) {
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
					addAll((List<ExternalBlinkLog>) results.values);
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
	Loader<List<ExternalBlinkLog>> getLoader(
			Loader.OnLoadCompleteListener<List<ExternalBlinkLog>> l) {
		Loader<List<ExternalBlinkLog>> loader = new LogLoader(getActivity(),
				mDevice, mApp);
		loader.registerListener(0, l);
		return loader;
	}

	class LogLoader extends AsyncTaskLoader<List<ExternalBlinkLog>> {
		Device device;
		App app;

		public LogLoader(Context context, Device device, App app) {
			super(context);
			this.device = device;
			this.app = app;
		}

		@Override
		public List<ExternalBlinkLog> loadInBackground() {
			if (app != null) {
				return ExternalBlinkLog.convert(mManager.obtainLog(
						device.Device, app.AppName, null, null));
			} else if (device != null) {
				return ExternalBlinkLog.convert(mManager.obtainLog(
						device.Device, null, null));
			} else {
				return ExternalBlinkLog.convert(mManager.obtainLog());
			}
		}

	}
}