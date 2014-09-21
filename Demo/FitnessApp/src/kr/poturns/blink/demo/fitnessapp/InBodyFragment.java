package kr.poturns.blink.demo.fitnessapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;

public class InBodyFragment extends SwipeEventFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_inbody, container, false);
		return v;
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT:
			mActivityInterface.returnToMain();
			break;

		default:
			break;
		}
		return false;
	}

}
