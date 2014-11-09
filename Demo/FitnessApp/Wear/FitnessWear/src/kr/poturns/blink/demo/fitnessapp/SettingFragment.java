package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import kr.poturns.blink.demo.fitnesswear.R;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 설정값을 관리하는 fragment
 * 
 * @author Myungjin.Kim
 */
public class SettingFragment extends SwipeEventFragment {
	public static final String KEY_MEASURE_HEARTBEAT = "KEY_MEASURE_HEARTBEAT";
	public static final String KEY_DELETE_TRAINING_DATA = "KEY_DELETE_TRAINING_DATA";
	public static final String KEY_INBODY_DATA = "KEY_INBODY_DATA";
	public static final String KEY_LOAD_CONTROL = "KEY_LOAD_CONTROL";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_home, container,
				false);
		v.setBackgroundResource(R.drawable.image_sunset);
		WearableListView listView = (WearableListView) v
				.findViewById(android.R.id.list);
		listView.setAdapter(new SettingListViewAdapter(getActivity()));
		listView.setClickListener(new WearableListView.ClickListener() {

			@Override
			public void onClick(WearableListView.ViewHolder view) {
				switch (view.getPosition()) {
				case 0:
					deleteFitnessData();
					break;
				case 1:
					deleteInbodyData();
					break;
				case 2:
					openControlActivity();
					break;
				default:
					break;
				}
			}

			@Override
			public void onTopEmptyRegionClick() {
			}

		});
		return v;
	}

	private void deleteFitnessData() {
		AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {

			@Override
			public void run() {
				SQLiteHelper.getInstance(getActivity()).dropAllTable();
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(getActivity(), "삭제했습니다.",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	private void deleteInbodyData() {
		if (getActivity().deleteFile(FitnessUtil.FILE_INBODY)) {
			Toast.makeText(getActivity(), "삭제했습니다.", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getActivity(), "실패했습니다.", Toast.LENGTH_SHORT).show();
		}
	}

	private void openControlActivity() {
		mActivityInterface.getBlinkServiceInteraction().openControlActivity();
	}

	@Override
	public boolean onSwipe(Direction direction) {
		if (direction == Direction.LEFT_TO_RIGHT) {
			mActivityInterface.returnToMain();
			return true;
		}
		return false;
	}

	private static class SettingListViewAdapter extends
			WearableListView.Adapter {
		private String[] mItems;
		private LayoutInflater mInflater;
		private static int[] LIST_ICONS = { R.drawable.ic_action_action_delete,
				R.drawable.ic_action_action_delete,
				R.drawable.ic_action_device_bluetooth_connected };

		public SettingListViewAdapter(Context context) {
			mItems = new String[] { "운동자료 삭제", "인바디 삭제", "Blink 관리화면" };
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getItemCount() {
			return mItems.length;
		}

		@Override
		public void onBindViewHolder(WearableListView.ViewHolder vh,
				int position) {
			ViewHolder h = (ViewHolder) vh;
			h.textView.setText(mItems[position]);
			h.imageView.setImageResource(LIST_ICONS[position]);
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new ViewHolder(mInflater.inflate(R.layout.list_preference,
					parent, false));
		}

		static class ViewHolder extends WearableListView.ViewHolder {
			TextView textView;
			CircledImageView imageView;

			public ViewHolder(View itemView) {
				super(itemView);
				textView = (TextView) itemView.findViewById(R.id.name);
				imageView = (CircledImageView) itemView
						.findViewById(R.id.circle);
			}
		}
	}
}
