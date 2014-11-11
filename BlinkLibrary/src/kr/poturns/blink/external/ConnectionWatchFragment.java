package kr.poturns.blink.external;

import kr.poturns.blink.R;
import kr.poturns.blink.internal.comm.BlinkDevice;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 연결 상태를 Watch의 화면으로 보여주는 Fragment <br>
 * <br>
 * 화면에 표현할 정보는 '기기 리스트'<br>
 * 지원하는 기능은 '장비 연결', '새로고침(디스커버리)'
 */
class ConnectionWatchFragment extends BaseConnectionFragment implements
		SwipeListener, OnTitleBarLongClickListener {
	AbsListView mListView;
	ArrayAdapter<BlinkDevice> mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mAdapter = new ArrayAdapter<BlinkDevice>(getActivity(),
				android.R.layout.simple_list_item_1, android.R.id.text1,
				getDeviceList()) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				BlinkDevice item = getItem(position);

				TextView tv = (TextView) v;
				tv.setText(item.getName());
				tv.setTextColor(item.isConnected() ? Color.WHITE : Color.BLACK);
				tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
						PrivateUtil.getBlinkDeviceTypeIcon(item), 0, 0, 0);
				v.setBackgroundResource(item.isConnected() ? R.drawable.res_blink_drawable_rectangle_box_pressed
						: R.drawable.res_blink_drawable_rectangle_box);
				return v;
			}
		};
		View root = inflater.inflate(
				R.layout.res_blink_fragment_connection_watch, container, false);
		mListView = (AbsListView) root.findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mItemClickListener);
		TextView empty = (TextView) root.findViewById(android.R.id.empty);
		empty.setTextSize(16);
		empty.setText("Click Here or\n\nLong Click Title\n\nto Refresh");
		empty.setCompoundDrawablesRelativeWithIntrinsicBounds(
				R.drawable.res_blink_ic_action_navigation_refresh, 0, 0, 0);
		empty.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				fetchDeviceListFromBluetooth();
			}
		});
		mListView.setEmptyView(empty);
		return root;
	}

	@Override
	public BaseConnectionFragment getChangedFragment() {
		return this;
	}

	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			BlinkDevice device = (BlinkDevice) parent
					.getItemAtPosition(position);
			show(getChildFragmentManager(), device);
		}
	};

	@Override
	public void onDeviceListChanged() {
		super.onDeviceListChanged();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT:
			if (getActivity() instanceof IServiceContolWatchActivity)
				((IServiceContolWatchActivity) getActivity())
						.returnToMain(null);
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onTitleViewLongClick(View titleView) {
		fetchDeviceListFromBluetooth();
		return true;
	}

	public void show(FragmentManager manager, BlinkDevice device) {
		DialogFragment dialog;
		if ((dialog = (DialogFragment) manager.findFragmentByTag("dialog")) == null) {
			dialog = new BlinkDeviceInfoDialogFragment();
		}
		Bundle bundle = new Bundle();
		bundle.putParcelable("blinkDevice", device);
		dialog.setArguments(bundle);
		dialog.show(manager, "dialog");
	}

	private class BlinkDeviceInfoDialogFragment extends DialogFragment {
		private BlinkDevice mBlinkDevice;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mBlinkDevice = getArguments().getParcelable("blinkDevice");
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			ViewPager root = (ViewPager) inflater.inflate(
					R.layout.res_bllink_view_viewpager, container, false);
			root.setAdapter(new DialogPagerAdapter(getChildFragmentManager()));
			return root;
		}

		private class DialogPagerAdapter extends FragmentPagerAdapter {

			public DialogPagerAdapter(FragmentManager fm) {
				super(fm);
			}

			@Override
			public int getCount() {
				// 간략 정보, 연결/연결해제, 즐겨찾기 등록/등록해제
				return 3;
			}

			@Override
			public Fragment getItem(final int position) {
				switch (position) {
				case 0:
					// 간략 정보
					return new InfoFragment();
				default:
					// 연결/연결해제, 즐겨찾기 등록/등록해제
					return new Fragment() {
						@Override
						public View onCreateView(LayoutInflater inflater,
								ViewGroup container, Bundle savedInstanceState) {
							final View rootView = inflater
									.inflate(
											R.layout.res_blink_dialog_fragment_connection_watch_twostate,
											container, false);
							final ImageView button = (ImageView) rootView
									.findViewById(android.R.id.button1);
							final TextView title = (TextView) rootView
									.findViewById(android.R.id.text1);
							boolean enable;
							int textRes, iconRes;
							// 연결/연결해제
							if (position == 1) {
								enable = mBlinkDevice.isConnected();
								textRes = enable ? R.string.res_blink_connection_disconnect
										: R.string.res_blink_connection_connect;
							}
							// 즐겨찾기 등록/등록해제
							else {
								enable = false;
								textRes = enable ? R.string.res_blink_connection_favorite_unregister
										: R.string.res_blink_connection_favorite_register;
							}
							iconRes = enable ? R.drawable.res_blink_ic_action_content_remove
									: R.drawable.res_blink_ic_action_content_add;
							title.setText(textRes);
							button.setImageDrawable(getResources()
									.getDrawableForDensity(iconRes,
											DisplayMetrics.DENSITY_HIGH));
							button.setBackgroundResource(enable ? R.drawable.res_blink_drawable_rounded_circle_red
									: R.drawable.res_blink_drawable_rounded_circle_green);
							button.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									if (position == 1) {
										if (mBlinkDevice.isBlinkSupported()) {
											connectOrDisConnectDevice(
													mBlinkDevice,
													new Runnable() {
														@Override
														public void run() {
															boolean connected = mBlinkDevice
																	.isConnected();
															title.setText(connected ? R.string.res_blink_connection_disconnect
																	: R.string.res_blink_connection_connect);
															button.setBackgroundResource(connected ? R.drawable.res_blink_drawable_rounded_circle_red
																	: R.drawable.res_blink_drawable_rounded_circle_green);
															button.setImageDrawable(getResources()
																	.getDrawableForDensity(
																			connected ? R.drawable.res_blink_ic_action_content_remove
																					: R.drawable.res_blink_ic_action_content_add,
																			DisplayMetrics.DENSITY_HIGH));
														}
													});
										} else {
											Toast.makeText(
													getActivity(),
													"Device does not support Blink Library",
													Toast.LENGTH_SHORT).show();
										}
									} else {
										// register favorite
										Toast.makeText(getActivity(),
												"Not supported yet",
												Toast.LENGTH_SHORT).show();
									}
								}
							});
							return rootView;
						}
					};
				}
			}
		}

		private class InfoFragment extends Fragment {
			@Override
			public View onCreateView(LayoutInflater inflater,
					ViewGroup container, Bundle savedInstanceState) {
				final View rootView = inflater
						.inflate(
								R.layout.res_blink_dialog_fragment_connection_watch_bluetooth,
								container, false);
				TextView title = (TextView) rootView
						.findViewById(android.R.id.title);
				title.setText(mBlinkDevice.getName());
				title.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						BlinkDeviceInfoDialogFragment.this.dismiss();
					}
				});
				TextView macAddress = (TextView) rootView
						.findViewById(android.R.id.text1);
				macAddress.setText(mBlinkDevice.getAddress());
				TextView type = (TextView) rootView
						.findViewById(android.R.id.text2);
				type.setText(mBlinkDevice.getIdentity().toString());
				return rootView;
			}
		}
	}

}
