package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeListener;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 설정값을 관리하는 fragment
 * 
 * @author Myungjin.Kim
 */
public class SettingFragment extends PreferenceFragment implements
		SwipeListener, OnSharedPreferenceChangeListener {
	ActivityInterface mActivityInterface;
	public static final String KEY_MEASURE_HEARTBEAT = "KEY_MEASURE_HEARTBEAT";
	public static final String KEY_DELETE_TRAINING_DATA = "KEY_DELETE_TRAINING_DATA";
	public static final String KEY_INBODY_DATA = "KEY_INBODY_DATA";
	public static final String KEY_LOAD_CONTROL = "KEY_LOAD_CONTROL";

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
		CheckBoxPreference preference = (CheckBoxPreference) findPreference(KEY_MEASURE_HEARTBEAT);
		preference.setChecked(getPreferenceScreen().getSharedPreferences()
				.getBoolean(KEY_MEASURE_HEARTBEAT, false));
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
	public void onResume() {
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
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
		} else if (key.equals(KEY_LOAD_CONTROL)) {
			mActivityInterface.getBlinkServiceInteraction()
					.openControlActivity();
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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(KEY_MEASURE_HEARTBEAT)) {
			boolean start = sharedPreferences.getBoolean(KEY_MEASURE_HEARTBEAT,
					false);
			Intent intent = new Intent(getActivity(), HeartBeatService.class);
			if (start) {
				getActivity().startService(intent);
			} else {
				getActivity().stopService(intent);
			}
		}
	}

}
