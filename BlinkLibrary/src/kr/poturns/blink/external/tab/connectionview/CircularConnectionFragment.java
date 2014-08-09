package kr.poturns.blink.external.tab.connectionview;

import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.R;
import kr.poturns.blink.external.tab.connectionview.CircularViewHelper.OnDragAndDropListener;
import kr.poturns.blink.external.tab.connectionview.CircularViewHelper.ViewInfoTag;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public final class CircularConnectionFragment extends ConnectionFragment {
	/** 원형으로 배치되는 View들의 parent View */
	private ViewGroup mViewGroup;
	/** 가운데에 배치되는 View */
	private TextView mHostView;
	/** layout 하단의 submenu 역할을 하는 SlidingDrawer */
	private SlidingDrawer mSlidingDrawer;
	private SeekBar mSeekBar;
	protected CircularViewHelper mCircularHelper;
	protected static final String HOST_DEVICE = Build.DEVICE;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mViewGroup = (ViewGroup) View.inflate(getActivity(),
				R.layout.fragment_circular_connection, null);
		mCircularHelper = new CircularViewHelper(mViewGroup, android.R.id.text1);
		mHostView = (TextView) mCircularHelper.getCenterView();
		mHostView.setText(HOST_DEVICE);
		mHostView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				fetchDeviceListFromDB();
				mCircularHelper.addChildViews(generateViews());
				mCircularHelper.drawCircularView();
				Toast.makeText(getActivity(), "connection refresh!",
						Toast.LENGTH_SHORT).show();
				mSeekBar.setProgress(0);
				return true;
			}
		});
		mCircularHelper.setOnDragAndDropListener(mDragAndDropListener);
		mCircularHelper.addChildViews(generateViews());
		mCircularHelper.drawCircularView();

		mSlidingDrawer = (SlidingDrawer) mViewGroup
				.findViewById(R.id.fragment_circular_sliding_drawer);
		mSeekBar = (SeekBar) mSlidingDrawer
				.findViewById(R.id.fragment_circular_seekbar);
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				setFilteringViewAlpha(progress);
			}
		});

		return mViewGroup;
	}

	protected void setFilteringViewAlpha(int percent) {
		List<View> list = mCircularHelper.getChildViews();
		int size = list.size();

		for (int i = 0; i < size; i++) {
			View v = list.get(i);
			if (checkDeviceFilteringCondition(i))
				v.setAlpha(((float) (100 - percent) / 100f));
		}
	}

	private boolean checkDeviceFilteringCondition(int i) {
		return i % 5 == 0;
	}

	protected ArrayList<View> generateViews() {
		ArrayList<View> list = new ArrayList<View>();
		int size = mDeviceList.size();
		for (int i = 0; i < size; i++) {
			TextView view = (TextView) View.inflate(getActivity(),
					R.layout.view_textview, null);
			String device = mDeviceList.get(i);
			view.setText(device);
			view.setOnClickListener(mOnClickListener);
			view.setTag(device);
			list.add(view);
		}
		return list;
	}

	@Override
	protected Fragment getChangeFragment() {
		return Fragment.instantiate(getActivity(),
				ListConnectionFragment.class.getName());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_circular_connection, menu);
	}

	private OnDragAndDropListener mDragAndDropListener = new OnDragAndDropListener() {

		@Override
		public void onDrop(View view) {
			Toast.makeText(getActivity(),
					view.getTag().toString() + " connected successful!",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStartDrag(View view, View center) {
			view.setBackgroundResource(R.drawable.drawable_rounded_circle_gray);
			((TextView) center).setText("Drop here to connect");
			((TextView) center)
					.setBackgroundResource(R.drawable.drawable_rounded_circle_border);
		}

		@Override
		public void onDropEnd(View view, View center) {
			center.setBackgroundResource(R.drawable.drawable_rounded_circle);
			view.setBackgroundResource(R.drawable.drawable_rounded_circle);
			((TextView) center).setText(HOST_DEVICE);
		}
	};
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(final View v) {
			final String device = ((ViewInfoTag) v.getTag()).mTag.toString();
			showDialog(device);
		}
	};

}
