package kr.poturns.blink.external;

import java.util.List;

import kr.poturns.blink.R;
import kr.poturns.blink.external.CircularViewHelper.OnDragAndDropListener;
import kr.poturns.blink.external.ConnectionFragment.BaseConnectionFragment;
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

@SuppressWarnings("deprecation")
/** Bluetooth Device의 연결 상태를 그래픽으로 나타내는 Fragment 클래스*/
final class ConnectionCircularFragment extends BaseConnectionFragment {
	/** layout 하단의 submenu 역할을 하는 SlidingDrawer */
	private SlidingDrawer mSlidingDrawer;
	private SeekBar mSeekBar;
	CircularViewHelper mCircularHelper;
	/**
	 * 장비 리스트 변경 후, SeekBar의 값을 변경할 때 참조하는 변수<br>
	 * <li>0 : 변경 안함</li> <li>1 : Max</li> <li>2 : Min</li>
	 */
	int mSetSeekBarValueMax = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		checkAndPutHostDevice();
		ViewGroup viewGroup = (ViewGroup) View.inflate(getActivity(),
				R.layout.res_blink_fragment_circular_connection, null);
		// TODO Center View 를 Host device가 아닌 BLINK network에서 "Center" device로?
		// XXX 테스트 필요
		mCircularHelper = new CircularViewHelper(viewGroup) {
			@Override
			protected View getView(Context context, int position, Object object) {
				return drawView(context, (BlinkDevice) object, false);
			}

			@Override
			protected View createCenterView(Context context, Object object) {
				if (object == null)
					return null;
				return drawView(context, (BlinkDevice) object, true);
			}

			private View drawView(Context context, BlinkDevice device,
					boolean isCenter) {
				TextView view = (TextView) View.inflate(context,
						R.layout.res_blink_view_circular, null);
				if (device.getAddress().equals(getHostDevice().getAddress())) {
					view.setCompoundDrawablesWithIntrinsicBounds(0,
							R.drawable.res_blink_ic_action_android, 0, 0);
				} else {
					view.setCompoundDrawablesWithIntrinsicBounds(
							0,
							device.isConnected() ? R.drawable.res_blink_ic_action_device_access_bluetooth_connected
									: R.drawable.res_blink_ic_action_device_access_bluetooth,
							0, 0);
				}
				String name = device.getName();
				if (name == null || name.equals(""))
					name = "NoName";
				// TODO 현재 ChildView 크기 만큼, 표시되는 이름 길이 줄이기
				// int size = getSize();
				// if(name!=null && device.getName().length())
				view.setText(name);
				if (isCenter) {
					view.setOnLongClickListener(mCenterViewLongClick);
				}
				view.setOnClickListener(mOnClickListener);

				return view;
			}
		};
		mCircularHelper.setOnDragAndDropListener(mDragAndDropListener);
		mCircularHelper.setCenterViewFromObject(getCenterDevice());
		mCircularHelper.drawCircularView(getDeviceList());
		mSlidingDrawer = (SlidingDrawer) viewGroup
				.findViewById(R.id.res_blink_fragment_circular_sliding_drawer);
		mSlidingDrawer.animateOpen();
		mSeekBar = (SeekBar) mSlidingDrawer
				.findViewById(R.id.res_blink_fragment_circular_seekbar);
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();
				if (progress > 50) { // connected device
					mSetSeekBarValueMax = 1;
					retainConnectedDevicesFromList();
				} else {
					mSetSeekBarValueMax = 2;
					obtainDiscoveryList();
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

	/** 연결되지 않은 Device를 나타내는 View의 alpha값을 변경한다. */
	void setFilteringViewAlpha(int percent) {
		List<View> list = mCircularHelper.getChildViews();
		int size = list.size();

		for (int i = 0; i < size; i++) {
			View v = list.get(i);
			if (((BlinkDevice) mCircularHelper.getViewTag(i)).isConnected()) {
				v.setAlpha(1);
			} else
				v.setAlpha(((float) (100 - percent) / 100f));
		}
	}

	@Override
	public BaseConnectionFragment getChangedFragment() {
		return new ConnectionListFragment();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.res_blink_fragment_circular_connection, menu);
	}

	private OnDragAndDropListener mDragAndDropListener = new OnDragAndDropListener() {

		@Override
		public void onDrop(View view) {
			connectOrDisConnectDevice((BlinkDevice) mCircularHelper
					.getViewTag(view));
		}

		@Override
		public void onStartDrag(View view, View center) {
			view.setBackgroundResource(R.drawable.res_blink_drawable_rounded_circle_gray);
			if (center != null) {
				TextView centerView = (TextView) center;
				centerView
						.setText(getString(((BlinkDevice) mCircularHelper
								.getViewTag(view)).isConnected() ? R.string.res_blink_drop_to_connect
								: R.string.res_blink_drop_to_disconnect));
				centerView
						.setBackgroundResource(R.drawable.res_blink_drawable_rounded_circle_border);
			}
		}

		@Override
		public void onDropEnd(View view, View center) {
			view.setBackgroundResource(R.drawable.res_blink_drawable_rounded_circle);
			if (center != null) {
				center.setBackgroundResource(R.drawable.res_blink_drawable_rounded_circle);
				((TextView) center).setText(getHostDevice().getName());
			}
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

	private View.OnLongClickListener mCenterViewLongClick = new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			mSetSeekBarValueMax = 2;
			fetchDeviceListFromBluetooth();
			return true;
		}
	};

	@Override
	public void onDeviceListChanged() {
		super.onDeviceListChanged();
		mCircularHelper.setCenterViewFromObject(getCenterDevice());
		checkAndPutHostDevice();
		mCircularHelper.drawCircularView(getDeviceList());

		switch (mSetSeekBarValueMax) {
		case 1:
			mSeekBar.setProgress(100);
			break;
		case 2:
			mSeekBar.setProgress(0);
			break;
		default:
			return;
		}
		mSetSeekBarValueMax = 0;
	}

	private void checkAndPutHostDevice() {
		if (!getDeviceList().contains(BlinkDevice.HOST)) {
			getDeviceList().add(BlinkDevice.HOST);
		}
	}
}