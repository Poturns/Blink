package kr.poturns.blink.external.preference;

import kr.poturns.blink.R;
import kr.poturns.blink.db.SqliteManagerExtended;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class InternalPreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	// private static final String TAG =
	// InternalPreferenceFragment.class.getSimpleName();

	/** Blink Application의 내부 설정 SharedPreference의 이름 */
	public static final String INTERNAL_PREF_NAME = "pref_internal";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_internal);
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
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		final int titleRes = preference.getTitleRes();
		if (titleRes == R.string.preference_internal_title_delete_database) {
			new AlertDialog.Builder(getActivity())
					.setTitle(titleRes)
					.setIcon(R.drawable.ic_action_alerts_and_states_warning)
					.setMessage(R.string.confirm_delete)
					.setNegativeButton(android.R.string.no, null)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO db내용 삭제
									SqliteManagerExtended manager = new SqliteManagerExtended(
											getActivity());
									boolean result = manager
											.removeCurrentAppData(getActivity());
									manager.close();
									if (result) {
										Toast.makeText(getActivity(),
												R.string.deleted,
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(getActivity(),
												R.string.fail,
												Toast.LENGTH_SHORT).show();
									}
									Toast.makeText(getActivity(),
											R.string.deleted,
											Toast.LENGTH_SHORT).show();
								}
							}).create().show();
			return true;
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
}
