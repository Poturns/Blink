package kr.poturns.blink.db_adv;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Blink 라이브러리에 관련된 모든 퍼미션을 관리한다.
 * @author Jiwon
 *
 */
public class BlinkPermission {
	public static final String ACCESS_DATABASE = "kr.poturns.blink.permission.ACCESS_DATABASE";
	public static final String ACCESS_SERVICE_DATABASE = "kr.poturns.blink.permission.ACCESS_SERVICE_DATABASE";
	
	public static boolean CheckPermission(Context context,String permission){
		return context.checkCallingPermission(permission)==PackageManager.PERMISSION_GRANTED ? true : false;
	}
	
	public static void PrintErrorAccessDatabase(){
		Log.e("Blink", "Access Database Error. You must to describe 'kr.poturns.blink.permission.ACCESS_DATABASE' permission.");
	}
	
	public static void PrintErrorAccessServiceDatabase(){
		Log.e("Blink", "Access Service Database Error. You must to describe 'kr.poturns.blink.permission.ACCESS_SERVICE_DATABASE' permission.");
	}
}
