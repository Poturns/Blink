package kr.poturns.blink.demo.fitnessapp;

import kr.poturns.blink.demo.fitnessapp.MainActivity.SwipeListener;
import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

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
		SwitchPreference preference = (SwitchPreference) findPreference(KEY_ALERT_HEART_BEAT_IMPACT);
		preference.setChecked(getPreferenceScreen().getSharedPreferences()
				.getBoolean(KEY_ALERT_HEART_BEAT_IMPACT, false));
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		String key = preference.getKey();
		if (key.equals(KEY_DELETE_TRAINING_DATA)) {

			return true;
		} else if (key.equals(KEY_INBODY_DATA)) {

			return true;
		}
		return false;
	}

	@Override
	public boolean onSwipe(Direction direction) {
		return false;
	}

}
