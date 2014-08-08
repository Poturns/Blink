package kr.poturns.blink.external.tab.logview;

import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.R;
import kr.poturns.blink.external.IServiceContolActivity;
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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;

public class LogViewFragment extends Fragment {
	ArrayAdapter<ExternalDeviceAppLog> mArrayAdapter;
	ExternalDeviceAppLog.LogComparator mLogComparator;
	ArrayList<ExternalDeviceAppLog> mLogList;
	String mCurrentDevice, mCurrentApp;
	LogHelper mLogHelper;
	int mPrevTitleViewSelectionId;
	int[] mTitleViewsIdArray = new int[] { R.id.fragment_logview_text_device,
			R.id.fragment_logview_text_app, R.id.fragment_logview_text_content,
			R.id.fragment_logview_text_datetime };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLogHelper = new LogHelper(getActivity());
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
		Bundle arg = getArguments();
		if (arg != null) {
			mCurrentDevice = arg.getString(IServiceContolActivity.EXTRA_DEVICE);
			mCurrentApp = arg
					.getString(IServiceContolActivity.EXTRA_DEVICE_APP);
		}
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
					}).startLoading();
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
				Toast.makeText(getActivity(), "검색 : " + query,
						Toast.LENGTH_SHORT).show();
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
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		if (id == R.id.action_refresh) {
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
					}).startLoading();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	private View.OnClickListener mTitleViewOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			final int id = v.getId();

			for (int i = 0; i < mTitleViewsIdArray.length; i++) {
				if (mTitleViewsIdArray[i] == id) {
					int compartorField = mLogComparator.mComparatorField;
					if (compartorField == ExternalDeviceAppLog
							.getComparatorFieldNumberByOrder(i))
						mLogComparator.mIsAsendingOrder = !mLogComparator.mIsAsendingOrder;
					else
						mLogComparator.mIsAsendingOrder = true;
					mLogComparator.mComparatorField = i;
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
							.getComparatorFieldNumberByOrder(i)));
					convertView.setTag(id, textViewArray[i]);
				}
			} else {
				for (int i = 0; i < mTitleViewsIdArray.length; i++) {
					textViewArray[i] = (TextView) convertView
							.getTag(mTitleViewsIdArray[i]);
					textViewArray[i].setText(item.getField(ExternalDeviceAppLog
							.getComparatorFieldNumberByOrder(i)));
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
				getActivity(), mLogHelper, mCurrentDevice, mCurrentApp);
		loader.registerListener(0, l);
		return loader;
	}

	class LogLoader extends AsyncTaskLoader<List<ExternalDeviceAppLog>> {
		String device, app;
		LogHelper helper;

		public LogLoader(Context context, LogHelper converter, String device,
				String app) {
			super(context);
			this.device = device;
			this.app = app;
			this.helper = converter;
		}

		@Override
		public List<ExternalDeviceAppLog> loadInBackground() {
			if (app != null && device != null) {
				return helper.getLogByApp(device, app, null, null);
			} else if (device != null) {
				return helper.getLogByDevice(device, null, null);
			} else {
				return helper.getLog(null, null);
			}
		}

	}

}
