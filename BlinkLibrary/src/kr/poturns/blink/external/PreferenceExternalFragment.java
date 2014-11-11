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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Blink Library의 설정 변경을 하는 PreferenceFragment<br>
 * <br>
 * 변경 된 설정값은 Binder를 통해 Service에 전달된다.
 */
abstract class PreferenceExternalFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	static final String TAG = PreferenceExternalFragment.class.getSimpleName();
	/** '기기를 센터로 설정'의 Key */
	static final String KEY_EXTERNAL_SET_THIS_DEVICE_TO_HOST = "KEY_EXTERNAL_SET_THIS_DEVICE_TO_HOST";
	IServiceContolActivity mInterface;

	/** 가동중인 기기에 적절한 Fragment 객체를 반환한다. */
	static final PreferenceExternalFragment getFragment() {
		switch (PrivateUtil.DEVICE_TYPE) {
		case WAREABLE_WATCH:
			return new PreferenceWatchFragment();
		default:
			return new PreferenceHandheldFragment();
		}
	}

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

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
	final void ensurePreferenceDir() {
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
			mPreferenceDirField.set(mBase, mPreferenceDir);
			Log.d(TAG, "use : " + mPreferenceDir);
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
			mPreferenceDirField.set(mBase, null);
			Log.d(TAG, "restore : null ");
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

	/**
	 * 설정 화면에서 입력된 값을 반영한다.
	 */
	void bindPreferenceSummaryToValue() {
		SharedPreferences pref = getPreferenceScreen().getSharedPreferences();
		pref.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		int titleRes = preference.getTitleRes();
		if (titleRes == R.string.res_blink_preference_external_title_delete_database) {
			showDeleteDatabaseDialog();
			return true;
		} else if (titleRes == R.string.res_blink_preference_external_title_delete_database_device) {
			showDeleteDatabaseFromDeviceDialog();
			return true;
		} else
			return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	void showDeleteDatabaseDialog() {
		new AlertDialog.Builder(getActivity())
				.setTitle(
						R.string.res_blink_preference_external_title_delete_database)
				.setIcon(
						R.drawable.res_blink_ic_action_alerts_and_states_warning)
				.setMessage(R.string.res_blink_confirm_delete)
				.setNegativeButton(android.R.string.no, null)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								AsyncTask.THREAD_POOL_EXECUTOR
										.execute(new Runnable() {

											@Override
											public void run() {
												File dbDirectory = FileUtil
														.obtainExternalDirectory(FileUtil.EXTERNAL_ARCHIVE_DIRECTORY_NAME);
												boolean result = false;
												for (File file : dbDirectory
														.listFiles()) {
													result |= !file.delete();
												}
												final boolean finalResult = result;

												getActivity().runOnUiThread(
														new Runnable() {

															@Override
															public void run() {
																Toast.makeText(
																		getActivity(),
																		finalResult ? R.string.res_blink_fail
																				: R.string.res_blink_deleted,
																		Toast.LENGTH_SHORT)
																		.show();
															}
														});

												// 디렉토리 복구 && DB 파일 생성
												FileUtil.createExternalDirectory();
												new SqliteManagerExtended(
														getActivity());
											}
										});

							}
						}).create().show();
	}

	void showDeleteDatabaseFromDeviceDialog() {
		new AlertDialog.Builder(getActivity())
				.setTitle(
						R.string.res_blink_preference_external_title_delete_database_device)
				.setIcon(
						R.drawable.res_blink_ic_action_alerts_and_states_warning)
				.setMessage(R.string.res_blink_confirm_delete)
				.setNegativeButton(android.R.string.no, null)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								AsyncTask.THREAD_POOL_EXECUTOR
										.execute(new Runnable() {

											@Override
											public void run() {
												SqliteManagerExtended manager = new SqliteManagerExtended(
														getActivity());
												final boolean result = manager
														.removeCurrentDeviceData();
												manager.close();

												getActivity().runOnUiThread(
														new Runnable() {

															@Override
															public void run() {
																Toast.makeText(
																		getActivity(),
																		result ? R.string.res_blink_deleted
																				: R.string.res_blink_fail,
																		Toast.LENGTH_SHORT)
																		.show();
															}
														});
											}
										});

							}
						}).create().show();
	}

	/** KEY_EXTERNAL_SET_THIS_DEVICE_TO_HOST 의 변경이 한번만 일어나게 만들기 위한 변수 */
	boolean mCommit = false;

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (KEY_EXTERNAL_SET_THIS_DEVICE_TO_HOST.equals(key)) {
			boolean value = sharedPreferences.getBoolean(
					KEY_EXTERNAL_SET_THIS_DEVICE_TO_HOST, false);
			if (!mCommit) {
				mCommit = true;
				boolean result = mInterface.getServiceInteration()
						.grantMainIdentityFromUser(value);
				findPreference(KEY_EXTERNAL_SET_THIS_DEVICE_TO_HOST)
						.setDefaultValue(result);
				mCommit = false;
			}
		}
	}

	/** 설정에서 변경된 값을 Service에 전달한다. */
	final void sendPreferenceDataToService(String keyName) {
		mInterface.getServiceInteration().requestConfigurationChange(keyName);
	}

}

/** Handheld용 Fragment */
class PreferenceHandheldFragment extends PreferenceExternalFragment {
	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		addPreferencesFromResource(R.xml.res_blink_preference_external);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
}

/** Watch용 Fragment */
class PreferenceWatchFragment extends PreferenceExternalFragment implements
		SwipeListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ListView root = (ListView) inflater.inflate(
				R.layout.res_blink_view_listview, container, false);
		root.setPadding(30, 10, 30, 10);
		root.setClipToPadding(false);
		root.setAdapter(new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1) {
			private int[] mTexts = {
					R.string.res_blink_preference_external_title_delete_database,
					R.string.res_blink_preference_external_title_delete_database_device };

			@Override
			public int getCount() {
				return mTexts.length;
			}

			@Override
			public String getItem(int position) {
				return getContext().getString(mTexts[position]);
			}

			@Override
			public long getItemId(int position) {
				return mTexts[position];
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView v = (TextView) super.getView(position, convertView,
						parent);
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(
						getContext().getResources().getDrawableForDensity(
								R.drawable.res_blink_ic_action_action_delete,
								DisplayMetrics.DENSITY_HIGH), null, null, null);
				v.setPadding(30, 10, 30, 10);
				return v;
			}
		});
		root.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					showDeleteDatabaseDialog();
					break;
				case 1:
					showDeleteDatabaseFromDeviceDialog();
					break;
				}
			}
		});
		return root;
	}

	@Override
	void bindPreferenceSummaryToValue() {
	}

	@Override
	public boolean onSwipe(Direction direction) {
		switch (direction) {
		case LEFT_TO_RIGHT:
			if (getActivity() instanceof IServiceContolWatchActivity)
				((IServiceContolWatchActivity) getActivity())
						.returnToMain(null);
			return true;
		default:
			return false;
		}
	}
}
