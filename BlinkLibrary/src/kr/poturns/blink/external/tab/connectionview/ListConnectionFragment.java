package kr.poturns.blink.external.tab.connectionview;

import java.util.ArrayList;
import java.util.Random;

import kr.poturns.blink.R;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/** 현재 연결된 Device들을 ListView의 형태로 보여주는 Fragment */
public class ListConnectionFragment extends Fragment {
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private ArrayAdapter<String> mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mSwipeRefreshLayout = (SwipeRefreshLayout) View.inflate(getActivity(),
				R.layout.fragment_list_connection, null);
		mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
		mSwipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		mAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, generateViews());
		ListView listView = (ListView) mSwipeRefreshLayout
				.findViewById(android.R.id.list);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(mOnItemClickListener);

		return mSwipeRefreshLayout;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_list_connection, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		if (id == R.id.action_connection_view_change) {
			getChildFragmentManager()
					.beginTransaction()
					.add(R.id.activity_main_fragment_content,
							Fragment.instantiate(getActivity(),
									CircularConnectionFragment.class.getName()))
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
			return true;
		} else if (id == R.id.action_list_fillter) {
			boolean check;
			if ((check = !item.isChecked()))
				fillterDevice();

			item.setChecked(check);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void fillterDevice() {
		mAdapter.clear();
		mAdapter.addAll(generateViews());
		mAdapter.notifyDataSetChanged();
	}

	private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {

		@Override
		public void onRefresh() {
			new Thread() {
				public void run() {
					mSwipeRefreshLayout.postDelayed(new Runnable() {

						@Override
						public void run() {
							mAdapter.clear();
							mAdapter.addAll(generateViews());
							mAdapter.notifyDataSetChanged();
							mSwipeRefreshLayout.setRefreshing(false);
							Toast.makeText(getActivity(),
									"connection refresh!", Toast.LENGTH_SHORT)
									.show();
						}
					}, 5000);
				}
			}.start();
		}
	};

	private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Toast.makeText(getActivity(), position + "selected!",
					Toast.LENGTH_SHORT).show();
		}
	};

	protected ArrayList<String> generateViews() {
		ArrayList<String> list = new ArrayList<String>();
		int size = new Random(System.currentTimeMillis()).nextInt(11) + 3;
		for (int i = 0; i < size; i++) {
			list.add("Device " + i);
		}
		return list;
	}
}
