package kr.poturns.blink.util;

import java.io.File;

import android.os.Environment;
import android.util.Log;

/**
 * Blink Service에서 사용되는 디렉토리 관련 기능이 정의되어있는 유틸 클래스. <br>
 * <br>
 * <li>{@link FileUtil#createExternalDirectory()} : <b>Blink</b> 디렉토리를 생성한다.</li>
 * <br>
 * <li>{@link FileUtil#obtainExternalDirectory(String)} : <b>Blink</b> 및 그 하위
 * 디렉토리를 나타내는 {@code File}객체를 얻는다.</li><br>
 * <br>
 * 
 * @author Myoungjin.Kim
 * @author Yeonho.Kim
 * 
 */
public final class FileUtil {
	/** Blink Application의 외부 파일이 저장되는 디렉토리의 이름 */
	public static final String EXTERNAL_DIRECTORY_NAME = "Blink";
	/** Blink Application의 외부 설정파일이 저장되는 디렉토리의 경로 */
	public static final String EXTERNAL_DIRECTORY_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separatorChar + EXTERNAL_DIRECTORY_NAME;
	/** Blink Application의 외부 설정파일이 저장되는 디렉토리의 이름 */
	public static final String EXTERNAL_PREF_DIRECTORY_NAME = "preference";
	/** Blink Application의 외부 설정파일이 저장되는 디렉토리의 경로 */
	public static final String EXTERNAL_PREF_DIRECTORY_PATH = EXTERNAL_DIRECTORY_PATH
			+ File.separatorChar + EXTERNAL_PREF_DIRECTORY_NAME;
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
			+ File.separatorChar + EXTERNAL_PREF_FILE_NAME;
	/** DB가 저장되는 외부 폴더 이름 */
	public static final String EXTERNAL_ARCHIVE_DIRECTORY_NAME = "archive";
	/** DB가 저장되는 외부 폴더 경로 */
	public static final String EXTERNAL_ARCHIVE_DIRECTORY_PATH = EXTERNAL_DIRECTORY_PATH
			+ File.separatorChar + EXTERNAL_ARCHIVE_DIRECTORY_NAME;

	/** Blink의 시스템 서비스와 관련된 파일들이 기록되는 디렉토리의 이름 */
	public static final String EXTERNAL_SYSTEM_DIRECTORY_NAME = "system";
	/** Blink의 시스템 서비스와 관련된 파일들이 기록되는 디렉토리의 경로 */
	public static final String EXTERNAL_SYSTEM_DIRECTORY_PATH = EXTERNAL_DIRECTORY_PATH
			+ File.separatorChar + EXTERNAL_SYSTEM_DIRECTORY_NAME;

	/** 탐색한 디바이스에 관한 정보를 남겨두는 디렉토리 이름 */
	public static final String EXTERNAL_SYSTEM_DEVICE_REPOSITORY_NAME = "dev";
	/** 탐색한 디바이스에 관한 정보를 남겨두는 디렉토리 경로 */
	public static final String EXTERNAL_SYSTEM_DEVICE_REPOSITORY_PATH = EXTERNAL_SYSTEM_DIRECTORY_PATH
			+ File.separatorChar + EXTERNAL_SYSTEM_DEVICE_REPOSITORY_NAME;
	private static final String TAG = "FileUtil";

	/**
	 * Blink Library에 필요한 디렉토리들을 <b>sdcard0</b>에 생성한다.
	 * 
	 * @see Environment#getExternalStorageDirectory()
	 */
	public static final void createExternalDirectory() {
		// 최상위 외부 폴더 생성 (Blink)
		final File externalFile = new File(
				Environment.getExternalStorageDirectory(),
				EXTERNAL_DIRECTORY_NAME);
		Log.d(TAG, "external dir : " + externalFile.getAbsolutePath());

		if (externalFile.mkdir() || externalFile.isDirectory()) {
			Log.d(TAG, "external dir created, : " + externalFile);

			// Blink 디렉토리의 하위 디렉토리들을 생성한다.
			final String[] childFileNames = new String[] {
					EXTERNAL_PREF_DIRECTORY_NAME,
					EXTERNAL_ARCHIVE_DIRECTORY_NAME,
					EXTERNAL_SYSTEM_DIRECTORY_NAME };
			for (final String name : childFileNames) {
				File externalSubFile = new File(externalFile, name);
				if (externalSubFile.mkdir() || externalSubFile.isDirectory()) {
					Log.d(TAG, name + " created, : " + externalSubFile);
					createSubDirectory(externalSubFile);
				} else {
					Log.d(TAG, name + "could not create");
				}
			}
		} else {
			Log.d(TAG, "external dir could not create : " + externalFile);
		}
	}

	/**
	 * <b>root</b> 디렉토리 내부에 파일을 생성한다.
	 * 
	 * @param root
	 *            생성할 디렉토리의 상위 디렉토리
	 */
	private static void createSubDirectory(File root) {
		// System의 하위 디렉토리들을 생성한다.
		if (root.getPath().equals(EXTERNAL_SYSTEM_DIRECTORY_PATH)) {
			final String[] detailNames = new String[] { EXTERNAL_SYSTEM_DEVICE_REPOSITORY_NAME };
			for (final String path : detailNames) {
				File externalSubFile = new File(root, path);
				if (externalSubFile.mkdirs() || externalSubFile.isDirectory()) {
					Log.d(TAG, path + " created, : " + externalSubFile);
				} else {
					Log.d(TAG, path + "could not create");
				}
			}
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
	 *            <li>{@link FileUtil#EXTERNAL_SYSTEM_DEVICE_REPOSITORY_NAME}</li>
	 * <br>
	 *            위의 리스트 중 하나가 인자로 들어와야 한다.<br>
	 * 
	 * @return name에 해당하는 {@code File}, 해당하는 {@code File}이 없으면 {@code null}
	 */
	public static final File obtainExternalDirectory(final String name) {
		if (EXTERNAL_DIRECTORY_NAME.equals(name)) {
			return new File(EXTERNAL_DIRECTORY_PATH);

		} else if (EXTERNAL_SYSTEM_DIRECTORY_NAME.equals(name)) {
			return new File(EXTERNAL_SYSTEM_DIRECTORY_PATH);

		} else if (EXTERNAL_SYSTEM_DEVICE_REPOSITORY_NAME.equals(name)) {
			return new File(EXTERNAL_SYSTEM_DEVICE_REPOSITORY_PATH);

		} else if (EXTERNAL_ARCHIVE_DIRECTORY_NAME.equals(name)) {
			return new File(EXTERNAL_ARCHIVE_DIRECTORY_PATH);

		} else if (EXTERNAL_PREF_DIRECTORY_NAME.equals(name)) {
			return new File(EXTERNAL_PREF_DIRECTORY_PATH);

		} else
			return null;
	}
}
