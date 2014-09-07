package kr.poturns.blink.demo.healthmanager;

import java.util.ArrayList;
import java.util.List;

import kr.poturns.blink.demo.healthmanager.R;
import kr.poturns.blink.demo.healthmanager.ConnectionFragment.BaseConnectionFragment;
import kr.poturns.blink.demo.healthmanager.CircularViewHelper.OnDragAndDropListener;
import kr.poturns.blink.demo.healthmanager.ConnectionFragment.DeviceConnectionResultListener;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.content.Context;
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
final class ConnectionCircularFragment extends BaseConnectionFragment {
	/** layout 하단의 submenu 역할을 하는 SlidingDrawer */
	private SlidingDrawer mSlidingDrawer;
	private SeekBar mSeekBar;
	CircularViewHelper mCircularHelper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup viewGroup = (ViewGroup) View.inflate(getActivity(),
				R.layout.fragment_circular_connection, null);
		showHostDeviceToList(false);
		mCircularHelper = new CircularViewHelper(viewGroup, android.R.id.text1) {
			@Override
			protected View getView(Context context, int position, Object object) {
				TextView view = (TextView) View.inflate(context,
						R.layout.view_textview, null);
				BlinkDevice device = (BlinkDevice) object;
				view.setCompoundDrawablesWithIntrinsicBounds(
						0,
						device.isConnected() ? R.drawable.ic_action_device_access_bluetooth_connected
								: R.drawable.ic_action_device_access_bluetooth,
						0, 0);
				String name = device.getName();
				// TODO 현재 ChildView 크기 만큼, 표시되는 이름 길이 줄이기
				// int size = getSize();
				// if(name!=null && device.getName().length())
				view.setText(name);
				view.setOnClickListener(mOnClickListener);
				return view;
			}
		};
		TextView hostView = (TextView) mCircularHelper.getCenterView();
		hostView.setText(getHostDevice().getName());
		hostView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showHostDeviceInfoDialog();
			}
		});
		hostView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				fetchDeviceListFromBluetooth();
				return true;
			}
		});
		mCircularHelper.setOnDragAndDropListener(mDragAndDropListener);
		mCircularHelper.drawCircularView(getDeviceList());

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
					obtainDiscoveryList();
					seekBar.setProgress(0);
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
			if (!((BlinkDevice) mCircularHelper.getViewTag(i)).isConnected())
				v.setAlpha(((float) (100 - percent) / 100f));
		}
	}

	ArrayList<View> generateViews() {
		ArrayList<View> list = new ArrayList<View>();
		int size = getDeviceList().size();
		for (int i = 0; i < size; i++) {
			TextView view = (TextView) View.inflate(getActivity(),
					R.layout.view_textview, null);
			BlinkDevice device = getDeviceList().get(i);
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
	public BaseConnectionFragment getChangedFragment() {
		return new ConnectionListFragment();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_circular_connection, menu);
	}

	private OnDragAndDropListener mDragAndDropListener = new OnDragAndDropListener() {

		@Override
		public void onDrop(View view) {
			connectOrDisConnectDevice(
					(BlinkDevice) mCircularHelper.getViewTag(view),
					new DeviceConnectionResultListener() {
						@Override
						public void onResult(BlinkDevice device,
								boolean connectionResult, boolean isTaskFailed) {
							if (isTaskFailed)
								Toast.makeText(getActivity(),
										"connection task was failed!",
										Toast.LENGTH_SHORT).show();
							else {
								if (connectionResult)
									Toast.makeText(
											getActivity(),
											device.getName()
													+ " was connected!",
											Toast.LENGTH_SHORT).show();
								else
									Toast.makeText(
											getActivity(),
											device.getName()
													+ " was disconnected!",
											Toast.LENGTH_SHORT).show();
							}
						}
					});
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
			((TextView) center).setText(getHostDevice().getName());
		}
	};
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(final View v) {
			final BlinkDevice device = (BlinkDevice) mCircularHelper
					.getViewTag(v);
			showBlinkDeviceInfoDialog(device);
		}
	};

	@Override
	public void onDeviceListChanged() {
		mCircularHelper.drawCircularView(getDeviceList());
	}

}