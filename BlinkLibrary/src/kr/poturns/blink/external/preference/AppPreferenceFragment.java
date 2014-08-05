package kr.poturns.blink.external.preference;

import kr.poturns.blink.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class AppPreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	// private static final String TAG =
	// AppPreferenceFragment.class.getSimpleName();

	/** Blink Application의 내부 설정 SharedPreference의 이름 */
	public static final String INTERNAL_PREF_NAME = "pref_internal";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_app_dummy);
		bindPreferenceSummaryToValue();
	}

	@Override
	public PreferenceManager getPreferenceManager() {
		// global preference에서 바뀌었을지도 모르는 mPreferencesDir을 복구한다.
		super.getPreferenceManager().setSharedPreferencesName(
				INTERNAL_PREF_NAME);
		return super.getPreferenceManager();
	}

	@Override
	public void onPause() {
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	private void bindPreferenceSummaryToValue() {
		SharedPreferences pref = getPreferenceScreen().getSharedPreferences();
		pref.registerOnSharedPreferenceChangeListener(this);
		onSharedPreferenceChanged(pref, "sample");
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if ("sample".equals(key)) {
			Preference pref = findPreference(key);
			pref.setSummary(sharedPreferences.getString(key, "internal-test"));
		}
	}
}
