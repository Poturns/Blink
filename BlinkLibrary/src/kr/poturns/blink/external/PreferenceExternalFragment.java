package kr.poturns.blink.external;

import java.io.File;
import java.lang.reflect.Field;

import kr.poturns.blink.R;
import kr.poturns.blink.util.FileUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

/**
 * Blink Library의 설정 변경을 하는 PreferenceFragment<br>
 * <br>
 * 변경 된 설정값은 Binder를 통해 Service에 전달된다.
 */
class PreferenceExternalFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	private static final String TAG = PreferenceExternalFragment.class
			.getSimpleName();
	/** '기기를 센터로 설정'의 Key */
	private static final String KEY_EXTERNAL_SET_THIS_DEVICE_TO_HOST = "KEY_EXTERNAL_SET_THIS_DEVICE_TO_HOST";
	IServiceContolActivity mInterface;
	String mOriginalPreferencePath;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof IServiceContolActivity) {
			mInterface = (IServiceContolActivity) activity;
		}
	}

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		Log.d(TAG, "external pref path : " + FileUtil.EXTERNAL_PREF_FILE_PATH
				+ FileUtil.EXTERNAL_PREF_FILE_NAME);
		getPreferenceManager().setSharedPreferencesName(
				FileUtil.EXTERNAL_PREF_FILE_NAME);
		addPreferencesFromResource(R.xml.res_blink_preference_external);
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
			Log.d(TAG, "will use : " + mPreferenceDir);
			String path = mPreferenceDirField.get(mBase).toString();
			if (!path.equals(mOriginalPreferencePath)) {
				mOriginalPreferencePath = path;
				mPreferenceDirField.set(mBase, mPreferenceDir);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(TAG, "could not change mPreferenceDir");
		}
	}

	@Override
	public void onDestroy() {
		try {
			Context mBase = getActivity().getBaseContext();
			Class<? extends Context> contextImplClass = mBase.getClass();

			// sharedPreference Directory
			Field mPreferenceDirField = contextImplClass
					.getDeclaredField("mPreferencesDir");
			mPreferenceDirField.setAccessible(true);
			File mPreferenceDir = new File(mOriginalPreferencePath);
			Log.d(TAG, "restore : " + mPreferenceDir);
			mPreferenceDirField.set(mBase, mPreferenceDir);
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(TAG, "could not change mPreferenceDir");
		}
		super.onDestroy();
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
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		int titleRes = preference.getTitleRes();
		if (titleRes == R.string.res_blink_preference_external_title_delete_database) {
			new AlertDialog.Builder(getActivity())
					.setTitle(titleRes)
					.setIcon(R.drawable.res_blink_ic_action_alerts_and_states_warning)
					.setMessage(R.string.res_blink_confirm_delete)
					.setNegativeButton(android.R.string.no, null)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									File dbDirectory = FileUtil
											.obtainExternalDirectory(FileUtil.EXTERNAL_ARCHIVE_DIRECTORY_NAME);
									boolean result = false;
									for (File file : dbDirectory.listFiles()) {
										result |= !file.delete();
									}
									if (!result) {
										Toast.makeText(getActivity(),
												R.string.res_blink_deleted,
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(getActivity(),
												R.string.res_blink_fail,
												Toast.LENGTH_SHORT).show();
									}
								}
							}).create().show();
			return true;
		} else if (titleRes == R.string.res_blink_preference_external_title_delete_database_device) {
			new AlertDialog.Builder(getActivity())
					.setTitle(titleRes)
					.setIcon(R.drawable.res_blink_ic_action_alerts_and_states_warning)
					.setMessage(R.string.res_blink_confirm_delete)
					.setNegativeButton(android.R.string.no, null)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									SqliteManagerExtended manager = new SqliteManagerExtended(
											getActivity());
									boolean result = manager
											.removeCurrentDeviceData();
									manager.close();
									if (result) {
										Toast.makeText(getActivity(),
												R.string.res_blink_deleted,
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(getActivity(),
												R.string.res_blink_fail,
												Toast.LENGTH_SHORT).show();
									}
								}
							}).create().show();
			return true;
		} else
			return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (KEY_EXTERNAL_SET_THIS_DEVICE_TO_HOST.equals(key)) {
			sendPreferenceDataToService(KEY_EXTERNAL_SET_THIS_DEVICE_TO_HOST);
		}
	}

	/** 설정에서 변경된 값을 Service에 전달한다. */
	private void sendPreferenceDataToService(String keyName) {
		mInterface.getServiceInteration().requestConfigurationChange(keyName);
	}
}
