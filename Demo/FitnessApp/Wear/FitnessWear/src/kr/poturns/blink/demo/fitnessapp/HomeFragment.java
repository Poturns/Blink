package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import kr.poturns.blink.demo.fitnesswear.R;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/** 웨어러블 메인 화면 */
public class HomeFragment extends SwipeEventFragment {
	/** 웨어러블 메인 화면 리스트를 터치하면 이동될 화면 Fragment의 이름 */
	static final String[] FRAGMENT_NAMES = { InBodyFragment.class.getName(),
			FitnessFragment.class.getName(), RecordFragment.class.getName(),
			HeartBeatFragment.class.getName(),
			FunctionTestFragment.class.getName(),
			SettingFragment.class.getName() };
	/** 웨어러블 메인 화면 리스트에서 보여질 아이콘의 목록 */
	static final int[] LIST_ICONS = { R.drawable.ic_action_image_timer_auto,
			R.drawable.ic_action_health_dumbbell,
			R.drawable.ic_action_statistics_statistics,
			R.drawable.ic_action_health_heart_white,
			R.drawable.ic_action_image_camera,
			R.drawable.ic_action_setting_setup };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_home, container, false);
		WearableListView listView = (WearableListView) v
				.findViewById(android.R.id.list);
		listView.setAdapter(new HomeAdapter(getActivity()));
		listView.setClickListener(new WearableListView.ClickListener() {

			@Override
			public void onClick(WearableListView.ViewHolder view) {
				mActivityInterface.attachFragment(
						Fragment.instantiate(getActivity(),
								FRAGMENT_NAMES[view.getPosition()]), null);
			}

			@Override
			public void onTopEmptyRegionClick() {
			}

		});
		return v;
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT:
			getActivity().finish();
			return true;
		default:
			return false;
		}
	}

	private static class HomeAdapter extends WearableListView.Adapter {
		private String[] mItems;
		private LayoutInflater mInflater;

		public HomeAdapter(Context context) {
			mItems = context.getResources().getStringArray(R.array.title_entry);
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
			return new ViewHolder(mInflater.inflate(R.layout.list_home, parent,
					false));
		}

		static class ViewHolder extends WearableListView.ViewHolder {
			TextView textView;
			ImageView imageView;

			public ViewHolder(View itemView) {
				super(itemView);
				textView = (TextView) itemView.findViewById(R.id.name);
				imageView = (ImageView) itemView.findViewById(R.id.circle);
			}
		}
	}
}