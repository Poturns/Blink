package kr.poturns.blink.external;

import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.R;
import kr.poturns.blink.external.CircularViewHelper.OnDragAndDropListener;
import kr.poturns.blink.external.CircularViewHelper.ViewInfoTag;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.app.Fragment;
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
final class ConnectionCircularFragment extends ConnectionFragment {
	/** layout 하단의 submenu 역할을 하는 SlidingDrawer */
	private SlidingDrawer mSlidingDrawer;
	private SeekBar mSeekBar;
	CircularViewHelper mCircularHelper;
	boolean mIsRefresh = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup viewGroup = (ViewGroup) View.inflate(getActivity(),
				R.layout.fragment_circular_connection, null);
		mCircularHelper = new CircularViewHelper(viewGroup, android.R.id.text1);
		TextView hostView = (TextView) mCircularHelper.getCenterView();
		hostView.setText(mHostDevice.getName());
		hostView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showHostDeviceInfomation();
			}
		});
		hostView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				mIsRefresh = true;
				fetchDeviceListFromBluetooth();
				return true;
			}
		});
		mCircularHelper.setOnDragAndDropListener(mDragAndDropListener);
		mCircularHelper.addChildViews(generateViews());
		mCircularHelper.drawCircularView();

		mSlidingDrawer = (SlidingDrawer) viewGroup
				.findViewById(R.id.fragment_circular_sliding_drawer);
		mSeekBar = (SeekBar) mSlidingDrawer
				.findViewById(R.id.fragment_circular_seekbar);
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();
				if (progress > 50) { // connected device
					retainConnectedDevicesFromList();
					seekBar.setProgress(100);
				} else {
					mIsRefresh = true;
					fetchDeviceListFromBluetooth();
				}
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

		return viewGroup;
	}

	void setFilteringViewAlpha(int percent) {
		List<View> list = mCircularHelper.getChildViews();
		int size = list.size();

		for (int i = 0; i < size; i++) {
			View v = list.get(i);
			if (!((BlinkDevice) ((ViewInfoTag) v.getTag()).mTag).isConnected())
				v.setAlpha(((float) (100 - percent) / 100f));
		}
	}

	ArrayList<View> generateViews() {
		ArrayList<View> list = new ArrayList<View>();
		int size = mDeviceList.size();
		for (int i = 0; i < size; i++) {
			TextView view = (TextView) View.inflate(getActivity(),
					R.layout.view_textview, null);
			BlinkDevice device = mDeviceList.get(i);
			view.setCompoundDrawablesWithIntrinsicBounds(
					0,
					device.isConnected() ? R.drawable.ic_action_device_access_bluetooth_connected
							: R.drawable.ic_action_device_access_bluetooth, 0,
					0);
			view.setText(device.getName());
			view.setOnClickListener(mOnClickListener);
			view.setTag(device);
			list.add(view);
		}
		return list;
	}

	@Override
	protected Fragment getChangeFragment() {
		return new ConnectionListFragment();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_circular_connection, menu);
	}

	private OnDragAndDropListener mDragAndDropListener = new OnDragAndDropListener() {

		@Override
		public void onDrop(View view) {
			connectOrDisConnectDevice((BlinkDevice) ((ViewInfoTag) view
					.getTag()).mTag);
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
			((TextView) center).setText(mHostDevice.getName());
		}
	};
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(final View v) {
			final BlinkDevice device = (BlinkDevice) ((ViewInfoTag) v.getTag()).mTag;
			showDialog(device);
		}
	};

	@Override
	protected void onDeviceListChanged() {
		if (mIsRefresh) {
			Toast.makeText(getActivity(), "connection refresh!",
					Toast.LENGTH_SHORT).show();
			mSeekBar.setProgress(0);
			mIsRefresh = false;
		}
		mCircularHelper.addChildViews(generateViews());
		mCircularHelper.drawCircularView();
	}

}
