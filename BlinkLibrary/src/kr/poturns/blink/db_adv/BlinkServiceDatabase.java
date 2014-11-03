package kr.poturns.blink.db_adv;

import android.content.Context;

/**
 * 서비스에서 사용할 Database 접근 클래스
 * @author Jiwon
 *
 */
public class BlinkServiceDatabase extends BlinkDatabase {
	private static BlinkServiceDatabase mBlinkServiceDatabase;
	
	/**
	 * 서비스에서 사용될 데이터베이스 기능을 제공하는 객체들
	 */
	private Synchronizer mSynchronizer;
	
	private BlinkServiceDatabase(Context context) {
	    super(context);
	    // TODO Auto-generated constructor stub
    }
	
	public static BlinkServiceDatabase getInstance(Context context){
		/**
		 * permission check
		 */
		if(!BlinkPermission.CheckPermission(context, BlinkPermission.ACCESS_SERVICE_DATABASE)){
			BlinkPermission.PrintErrorAccessServiceDatabase();
			return null;
		}
		
		if(mBlinkServiceDatabase==null){
			mBlinkServiceDatabase = new BlinkServiceDatabase(context);
		}
		return mBlinkServiceDatabase;
	}
}
