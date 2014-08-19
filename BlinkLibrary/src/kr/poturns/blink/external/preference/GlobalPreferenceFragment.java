package kr.poturns.blink.external.preference;

import java.io.File;
import java.lang.reflect.Field;

import kr.poturns.blink.R;
import kr.poturns.blink.external.IServiceContolActivity;
import kr.poturns.blink.util.FileUtil;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

public class GlobalPreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	private static final String TAG = GlobalPreferenceFragment.class
			.getSimpleName();
	/** '기기를 센터로 설정'의 Key */
	private static final String KEY_SET_THIS_DEVICE_TO_HOST = "KEY_SET_THIS_DEVICE_TO_HOST";
	private Bundle mResultBundle = new Bundle();

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		Log.d(TAG, "external pref path : " + FileUtil.EXTERNAL_PREF_FILE_PATH
				+ FileUtil.EXTERNAL_PREF_FILE_NAME);
		getPreferenceManager().setSharedPreferencesName(
				FileUtil.EXTERNAL_PREF_FILE_NAME);
		addPreferencesFromResource(R.xml.preference_global);
		bindPreferenceSummaryToValue();
	}

	@Override
	public PreferenceManager getPreferenceManager() {
		ensurePreferenceDir();
		return super.getPreferenceManager();
	}

	/**
	 * 현재 preference directory를 외부 directory로 변경한다. <br>
	 * <br>
	 * 실제 하는 작업은 reflection으로 ContextImpl의 mPreferencesDir을 바꿔치기 하는 것 이다.
	 * 
	 * @see ContextImpl
	 */
	private void ensurePreferenceDir() {
		try {
			Context mBase = getActivity().getBaseContext();
			Class<? extends Context> contextImplClass = mBase.getClass();

			// sharedPreference Directory
			Field mPreferenceDirField = contextImplClass
					.getDeclaredField("mPreferencesDir");
			mPreferenceDirField.setAccessible(true);
			File mPreferenceDir = FileUtil
					.obtainExternalDirectory(FileUtil.EXTERNAL_PREF_DIRECTORY_NAME);
			Log.d(TAG, "use : " + mPreferenceDir);
			mPreferenceDirField.set(mBase, mPreferenceDir);
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(TAG, "could not change mPreferenceDir");
		}
	}

	@Override
	public PreferenceScreen getPreferenceScreen() {
		ensurePreferenceDir();
		return super.getPreferenceScreen();
	}

	@Override
	public void onPause() {
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	/**
	 * 설정 화면에서 입력된 값을 반영한다.
	 */
	private void bindPreferenceSummaryToValue() {
		SharedPreferences pref = getPreferenceScreen().getSharedPreferences();
		pref.registerOnSharedPreferenceChangeListener(this);
		// onSharedPreferenceChanged(pref, "sample");
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		int titleRes = preference.getTitleRes();
		if (titleRes == R.string.preference_global_title_delete_database) {
			new AlertDialog.Builder(getActivity())
					.setMessage(R.string.confirm_delete)
					.setNegativeButton(android.R.string.no, null)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									File dbDirectory = FileUtil
											.obtainExternalDirectory(FileUtil.EXTERNAL_ARCHIVE_DIRECTORY_NAME);
									for (File file : dbDirectory.listFiles()) {
										file.delete();
									}
									Toast.makeText(getActivity(),
											R.string.deleted,
											Toast.LENGTH_SHORT).show();
								}
							}).create().show();
			return true;
		} else
			return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (KEY_SET_THIS_DEVICE_TO_HOST.equals(key)) {
			boolean isSetThisDeviceToHost = sharedPreferences.getBoolean(key,
					false);
			mResultBundle.putBoolean(KEY_SET_THIS_DEVICE_TO_HOST,
					isSetThisDeviceToHost);
			sendPreferenceDataToService(mResultBundle);
		}
	}

	/** 설정에서 변경된 값을 Service에 전달한다. */
	private void sendPreferenceDataToService(Bundle object) {
		getActivity().setResult(
				IServiceContolActivity.RESULT_SERVICE,
				new Intent().putExtra(IServiceContolActivity.RESULT_EXTRA,
						object));
	}
}
