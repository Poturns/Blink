package kr.poturns.blink.external.tab.dataview;

import kr.poturns.blink.R;
import kr.poturns.blink.external.IServiceContolActivity;
import kr.poturns.blink.external.tab.SampleGraphFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DataViewFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String device = getArguments().getString(
				IServiceContolActivity.EXTRA_DEVICE);
		Bundle b = new Bundle();
		int i;
		try {
			i = Integer.valueOf(device.substring(device.length() - 1));
		} catch (Exception e) {
			i = 0;
		}
		b.putAll(getArguments());
		b.putInt("title", i);
		Fragment f = Fragment.instantiate(getActivity(),
				SampleGraphFragment.class.getName(), b);
		getFragmentManager()
				.beginTransaction()
				.add(R.id.activity_main_fragment_content, f,
						SampleGraphFragment.class.getSimpleName())
				.addToBackStack(DataViewFragment.class.getSimpleName())
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_dataview,
				container, false);
		return view;
	}
}
