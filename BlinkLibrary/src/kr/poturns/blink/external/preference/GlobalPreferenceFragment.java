package kr.poturns.blink.external.preference;

import java.io.File;
import java.lang.reflect.Field;

import kr.poturns.blink.util.FileUtil;
import kr.poturns.blink.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class GlobalPreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	private static final String TAG = GlobalPreferenceFragment.class
			.getSimpleName();
	/** '기기를 센터로 설정'의 Key */
	private static final String KEY_SET_THIS_DEVICE_TO_HOST = "KEY_SET_THIS_DEVICE_TO_HOST";

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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (KEY_SET_THIS_DEVICE_TO_HOST.equals(key)) {
			boolean isSetThisDeviceToHost = sharedPreferences.getBoolean(key,
					false);
			sendPreferenceDataToService(key, isSetThisDeviceToHost);
		}
	}

	/** 설정에서 변경된 값을 Service에 전달한다. */
	private void sendPreferenceDataToService(String key, Object object) {

	}
}
