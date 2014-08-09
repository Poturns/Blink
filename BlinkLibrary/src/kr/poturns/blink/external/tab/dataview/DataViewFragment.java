package kr.poturns.blink.external.tab.dataview;

import kr.poturns.blink.R;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/** data graph를 보여주는 Fragment의 container역할을 하는 Fragment */
public class DataViewFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arg = getArguments();
		if (arg != null) {
			Fragment f = Fragment.instantiate(getActivity(),
					GraphFragment.class.getName(), arg);
			getChildFragmentManager()
					.beginTransaction()
					.replace(android.R.id.content, f,
							GraphFragment.class.getSimpleName())
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.commit();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_dataview,
				container, false);
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//getActivity().getActionBar().setTitle();
	}
}
