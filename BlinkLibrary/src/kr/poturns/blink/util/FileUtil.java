package kr.poturns.blink.util;

import java.io.File;

import android.os.Environment;
import android.util.Log;

public final class FileUtil {
	/** Blink Application의 외부 파일이 저장되는 디렉토리의 이름 */
	public static final String EXTERNAL_DIRECTORY_NAME = "Blink";
	/** Blink Application의 외부 설정파일이 저장되는 디렉토리의 경로 */
	public static final String EXTERNAL_DIRECTORY_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/"
			+ EXTERNAL_DIRECTORY_NAME + "/";
	/** Blink Application의 외부 설정파일이 저장되는 디렉토리의 이름 */
	public static final String EXTERNAL_PREF_DIRECTORY_NAME = "preference";
	/** Blink Application의 외부 설정파일이 저장되는 디렉토리의 경로 */
	public static final String EXTERNAL_PREF_DIRECTORY_PATH = EXTERNAL_DIRECTORY_PATH
			+ EXTERNAL_PREF_DIRECTORY_NAME + "/";
	/**
	 * Blink Application의 중요한 설정을 저장하는 파일 이름 <br>
	 * <br>
	 * <br>
	 * 실제 저장되는 경로는 {@code  FileUtil.EXTERNAL_PREF_FILE_PATH}를 통해 얻을 수 있다.
	 */
	public static final String EXTERNAL_PREF_FILE_NAME = "pref_global";
	/**
	 * Blink Application의 중요한 설정을 저장하는 외부 디렉토리 경로 <br>
	 * 해당 경로는 {@code FileUtil.EXTERNAL_DIRECTORY_PATH+"/"} 이다. <br>
	 * 
	 * <br>
	 * <b>* 실제 저장된 파일의 이름은 {@code pref_global.xml}이다.</b>
	 * 
	 * @see Environment
	 */
	public static final String EXTERNAL_PREF_FILE_PATH = EXTERNAL_PREF_DIRECTORY_PATH
			+ EXTERNAL_PREF_FILE_NAME;
	/** DB가 저장되는 외부 폴더 이름 */
	public static final String EXTERNAL_ARCHIVE_DIRECTORY_NAME = "archive";
	/** DB가 저장되는 외부 폴더 경로 */
	public static final String EXTERNAL_ARCHIVE_DIRECTORY_PATH = EXTERNAL_DIRECTORY_PATH
			+ EXTERNAL_ARCHIVE_DIRECTORY_NAME;

	/** Blink의 시스템 서비스와 관련된 파일들이 기록되는 디렉토리의 이름 */
	public static final String EXTERNAL_SYSTEM_DIRECTORY_NAME = "System";
	/** Blink의 시스템 서비스와 관련된 파일들이 기록되는 디렉토리의 경로 */
	public static final String EXTERNAL_SYSTEM_DIRECTORY_PATH = EXTERNAL_DIRECTORY_PATH
			+ EXTERNAL_SYSTEM_DIRECTORY_NAME;

	/**
	 * Blink Library에 필요한 디렉토리들을 <b>sdcard0</b>에 생성한다.
	 * 
	 * @see Environment#getExternalStorageDirectory()
	 */
	public static final void createExternalDirectory() {
		final String TAG = "FileUtil";

		// 외부 폴더 생성
		final File externalFile = new File(
				Environment.getExternalStorageDirectory(),
				EXTERNAL_DIRECTORY_NAME);
		if (externalFile.mkdir() || externalFile.isDirectory()) {
			Log.d(TAG, "external dir created, : " + externalFile);

			// 외부 디렉토리들을 생성한다.
			final String[] names = new String[] { EXTERNAL_PREF_DIRECTORY_NAME,
					EXTERNAL_ARCHIVE_DIRECTORY_NAME,
					EXTERNAL_SYSTEM_DIRECTORY_NAME };
			for (final String name : names) {
				File externalSubFile = new File(externalFile, name);
				if (externalSubFile.mkdir() || externalSubFile.isDirectory()) {
					Log.d(TAG, name + " created, : " + externalSubFile);
				} else {
					Log.d(TAG, name + "could not create");
				}
			}
		} else {
			Log.d(TAG, "external dir could not create : " + externalFile);
		}
	}

	/**
	 * <b>sdcard0</b> 에 생성된 외부 폴더를 얻어온다.
	 * 
	 * @param name
	 *            <li> {@link FileUtil#EXTERNAL_DIRECTORY_NAME}</li> <li>
	 *            {@link FileUtil#EXTERNAL_SYSTEM_DIRECTORY_NAME}</li> <li>
	 *            {@link FileUtil#EXTERNAL_ARCHIVE_DIRECTORY_NAME}</li> <li>
	 *            {@link FileUtil#EXTERNAL_PREF_DIRECTORY_NAME}</li><br>
	 *            위의 넷 중의 하나가 인자로 들어와야 한다.
	 * 
	 * @return name에 해당하는 File, 해당하는 File이 없으면 null
	 */
	public static final File obtainExternalDirectory(final String name) {
		if (EXTERNAL_DIRECTORY_NAME.equals(name)) {
			return new File(EXTERNAL_DIRECTORY_PATH);
		} else if (EXTERNAL_SYSTEM_DIRECTORY_NAME.equals(name)) {
			return new File(EXTERNAL_SYSTEM_DIRECTORY_PATH);
		} else if (EXTERNAL_ARCHIVE_DIRECTORY_NAME.equals(name)) {
			return new File(EXTERNAL_ARCHIVE_DIRECTORY_PATH);
		} else if (EXTERNAL_PREF_DIRECTORY_NAME.equals(name)) {
			return new File(EXTERNAL_PREF_DIRECTORY_PATH);
		} else
			return null;
	}
}
