package kr.poturns.blink.external;

import java.io.File;
import java.lang.reflect.Field;

import kr.poturns.blink.util.FileUtil;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * BLINK Library의 설정 관련 작업을 수행하는 클래스<br>
 * <br>
 * 이 클래스는 생성자를 호출한 ContextWrapper의 mPreferenceDir을 바꿔치기 하는 방식으로 동작한다. <br>
 * <br>
 * Application-private 한 Preference 작업을 할 필요가 있다면
 * {@link #restorePreferenceDir()}를 호출하여 mPreferenceDir을 원상복귀 시킬 필요가 있다.
 * */
public class PrefUtil {
	/** '기기를 MAIN으로 설정'의 Key */
	public static final String KEY_EXTERNAL_SET_THIS_DEVICE_TO_MAIN = "KEY_EXTERNAL_SET_THIS_DEVICE_TO_MAIN";
	private static final String TAG = PrefUtil.class.getSimpleName();
	private ContextWrapper mContext;

	public PrefUtil(ContextWrapper context) {
		mContext = context;
		ensurePreferenceDir();
	}

	/**
	 * 현재 preference directory를 외부 directory로 변경한다. <br>
	 * <br>
	 * 실제 하는 작업은 reflection으로 ContextImpl의 mPreferencesDir을 바꿔치기 하는 것 이다.
	 * 
	 * @see ContextImpl
	 */
	final boolean ensurePreferenceDir() {
		try {
			Context mBase = mContext.getBaseContext();
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
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(TAG, "could not change mPreferenceDir");
			return false;
		}
	}

	/** 설정 디렉토리를 원상복귀 시킨다. */
	public final boolean restorePreferenceDir() {
		try {
			Context mBase = mContext.getBaseContext();
			Class<? extends Context> contextImplClass = mBase.getClass();

			// sharedPreference Directory
			Field mPreferenceDirField = contextImplClass
					.getDeclaredField("mPreferencesDir");
			mPreferenceDirField.setAccessible(true);
			mPreferenceDirField.set(mBase, null);
			Log.d(TAG, "restore : null ");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(TAG, "could not change mPreferenceDir");
			return false;
		}
	}

	public final SharedPreferences getSharedPreferences() {
		return mContext.getSharedPreferences(
				FileUtil.EXTERNAL_PREF_FILE_NAME,
				Context.MODE_MULTI_PROCESS);
	}

	@Override
	protected void finalize() throws Throwable {
		// 여기서 이 동작을 수행하면 mPreferenceDir이 동시접근될 가능성이 있음.
		// restorePreferenceDir();
		super.finalize();
	}

}
