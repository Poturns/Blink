package kr.poturns.blink.external.tab;

import kr.poturns.blink.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SampleFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		int id = getArguments().getInt("title");
		View v = inflater.inflate(R.layout.fragment_sample, container, false);
		((TextView) v.findViewById(android.R.id.text1)).setText(String
				.valueOf(id));
		return v;
	}
}
