package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeEventFragment;
import kr.poturns.blink.demo.fitnesswear.R;
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
				switch (((HomeAdapter.ViewHolder) view).position) {
				case 0:
					mActivityInterface.attachFragment(new InBodyFragment(),
							null);
					break;
				case 1:
					mActivityInterface.attachFragment(new FitnessFragment(),
							null);
					break;
				case 2:
					mActivityInterface.attachFragment(new RecordFragment(),
							null);
					break;
				case 3:
					mActivityInterface.attachFragment(new SettingFragment(),
							null);
					break;
				case 4:
					mActivityInterface.attachFragment(
							new FunctionTestFragment(), null);
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
		String[] mItems;
		LayoutInflater mInflater;

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
			h.position = position;
			h.textView.setText(mItems[position]);
			switch (position) {
			case 0:
				h.imageView
						.setImageResource(R.drawable.ic_action_image_timer_auto);
				break;
			case 1:
				h.imageView
						.setImageResource(R.drawable.ic_action_health_dumbbell);
				break;
			case 2:
				h.imageView
						.setImageResource(R.drawable.ic_action_statistics_statistics);
				break;
			case 3:
				h.imageView
						.setImageResource(R.drawable.ic_action_setting_setup);
				break;
			case 4:
				h.imageView.setImageResource(R.drawable.ic_action_image_camera);
				break;
			default:
				break;
			}
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new ViewHolder(mInflater.inflate(R.layout.list_home, parent,
					false));
		}

		static class ViewHolder extends WearableListView.ViewHolder {
			TextView textView;
			ImageView imageView;
			int position;

			public ViewHolder(View itemView) {
				super(itemView);
				textView = (TextView) itemView.findViewById(R.id.name);
				imageView = (ImageView) itemView.findViewById(R.id.circle);
			}

		}
	}
}