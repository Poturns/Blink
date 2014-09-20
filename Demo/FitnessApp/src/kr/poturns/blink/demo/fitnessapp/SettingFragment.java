package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeListener;
import kr.poturns.blink.demo.fitnessapp.measurement.FitnessUtil;
import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.ListView;
import android.widget.Toast;

public class SettingFragment extends PreferenceFragment implements
		SwipeListener {
	ActivityInterface mActivityInterface;
	public static final String KEY_ALERT_HEART_BEAT_IMPACT = "KEY_ALERT_HEART_BEAT_IMPACT";
	public static final String KEY_DELETE_TRAINING_DATA = "KEY_DELETE_TRAINING_DATA";
	public static final String KEY_INBODY_DATA = "KEY_INBODY_DATA";

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivityInterface = (ActivityInterface) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		bindPreferenceSummaryToValue();
	}

	private void bindPreferenceSummaryToValue() {
		CheckBoxPreference preference = (CheckBoxPreference) findPreference(KEY_ALERT_HEART_BEAT_IMPACT);
		preference.setChecked(getPreferenceScreen().getSharedPreferences()
				.getBoolean(KEY_ALERT_HEART_BEAT_IMPACT, false));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ListView listView = (ListView) getView()
				.findViewById(android.R.id.list);
		listView.setDivider(getResources().getDrawable(
				android.R.color.transparent));
		listView.setPaddingRelative(10, 30, 10, 30);
		listView.setDividerHeight(60);
		listView.setBackground(getResources().getDrawable(
				R.drawable.image_sunset));
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		String key = preference.getKey();
		if (key.equals(KEY_DELETE_TRAINING_DATA)) {
			SQLiteHelper.getInstance(getActivity()).dropAllTable();
			Toast.makeText(getActivity(), "삭제했습니다.", 1000).show();
			return true;
		} else if (key.equals(KEY_INBODY_DATA)) {
			if (getActivity().deleteFile(
					"/data/data/kr.poturns.blink.demo.fitnessapp/"
							+ FitnessUtil.FILE_INBODY)) {
				Toast.makeText(getActivity(), "삭제했습니다.", 1000).show();
			} else {
				Toast.makeText(getActivity(), "실패했습니다.", 1000).show();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onSwipe(Direction direction) {
		if (direction == Direction.LEFT_TO_RIGHT) {
			mActivityInterface.returnToMain();
			return true;
		}
		return false;
	}

}
