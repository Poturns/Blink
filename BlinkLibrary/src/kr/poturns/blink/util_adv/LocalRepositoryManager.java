package kr.poturns.blink.util_adv;

import java.io.File;

import android.os.Environment;

/**
 * Orchestra에서 리소스 파일의 저장소 관리를 담당하는 모듈.
 * 
 * <hr>
 * Orchestra
 * <ul>
 * 	<li> archive
 * 	<ul>
 * 		<li> 
 * 	</ul>	
 * 	<li> config
 * 	<ul>
 * 		<li> config-global
 * 		<li> config-#appHash
 * 	</ul>	
 * 	<li> system
 * 	<ul>
 * 		<li> 
 * 	</ul>	
 * </ul>
 * 
 * 
 * @author Myoungjin.Kim
 * @since 2014.08.06 [Blink_FileUtil]
 * 
 * @author Yeonho.Kim
 * @since 2014.08.15 [Blink_FileUtil]
 * @since 2014.11.16
 *
 */
public class LocalRepositoryManager {
	
	private static final String BASE_STORAGE_PATH;
	static {
		// TODO : 설정 값에 따라 기반 스토리지 위치를 변경할 수 있지 않을까.
		BASE_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	
	
	/** 
	 * Orchestra 라이브러리의 리소스가 저장되는 기본 디렉토리 명
	 * <br>
	 * The title of main directory that stores Orchestra-lib resources.
	 * 
	 */
	public static final String MAIN_DIR_TITLE = "Orchestra";
	
	/** 
	 * Orchestra 라이브러리의 기본 디렉토리 경로 
	 * <br>
	 * The path of main directory.
	 */
	public static final String MAIN_DIR_PATH = BASE_STORAGE_PATH 
												+ File.separatorChar 
												+ MAIN_DIR_TITLE;

	/** 
	 * Orchestra 라이브러리의 데이터베이스가 저장되는 디렉토리 명 
	 * <br>
	 * The title of sub-directory that stores Orchestra-lib databases.
	 */
	public static final String ARCHIVE_DIR_TITLE = "archive";
	
	/** 
	 * Orchestra 라이브러리의 데이터베이스 디렉토리 경로 
	 * <br>
	 * The path of archive sub-directory.
	 */
	public static final String ARCHIVE_DIR_PATH = MAIN_DIR_PATH
												+ File.separatorChar 
												+ ARCHIVE_DIR_TITLE;

	/** 
	 * Orchestra 라이브러리의 환경설정 값이 저장되는 디렉토리 명 
	 * <br>
	 * The title of sub-directory that stores Orchestra-lib configurations.
	 */
	public static final String CONFIG_DIR_TITLE = "config";
	
	/** 
	 * Orchestra 라이브러리의 환경설정 값 디렉토리 경로 
	 * <br>
	 * The path of config sub-directory.
	 */
	public static final String CONFIG_DIR_PATH = MAIN_DIR_PATH
												+ File.separatorChar 
												+ CONFIG_DIR_TITLE;
	
	/**
	 * Orchestra 라이브러리의 전역 환경설정 파일 명
	 * <br>
	 * The title of file that contains global-configurations.
	 */
	public static final String CONFIG_FILE_GLOBAL_TITLE = "pref_global";
	
	/**
	 * Orchestra 라이브러리의 전역 환경설정 파일 경로
	 * <br>
	 * The path of global-configuration file.
	 */
	public static final String CONFIG_FILE_GLOBAL_PATH = CONFIG_DIR_PATH
														+ File.pathSeparatorChar
														+ CONFIG_FILE_GLOBAL_TITLE;
	
	/** 
	 * Orchestra 라이브러리의 시스템 서비스 값이 저장되는 디렉토리 명
	 * <br>
	 * The title of sub-directory that stores Orchestra-lib system files.  
	 */
	public static final String SYSTEM_DIR_TITLE = "system";
	
	/** 
	 * Orchestra 라이브러리의 시스템  디렉토리 경로
	 * <br>
	 * The path of system sub-directory.
	 */
	public static final String SYSTEM_DIR_PATH = MAIN_DIR_PATH
												+ File.separatorChar 
												+ SYSTEM_DIR_TITLE;
	
	
}
