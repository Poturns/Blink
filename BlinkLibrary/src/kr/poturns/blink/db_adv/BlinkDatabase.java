package kr.poturns.blink.db_adv;

import android.content.Context;

/**
 * BlinkDatabase에 관한 모든 작업을 총괄하는 클래스
 * @author Jiwon
 *
 */
public class BlinkDatabase {
	protected Context context;
	private static BlinkDatabase mBlinkDatabase;
	
	/**
	 * 데이터베이스에서 사용될 기능을 제공하는 객체들
	 */
	public SchemaConverter CONVERTER;
	
	protected BlinkDatabase(Context context){
		this.context = context;
		CONVERTER = new SchemaConverter(this);
	}
	
	public static BlinkDatabase getInstance(Context context){
		/**
		 * permission check
		 */
		if(!BlinkPermission.CheckPermission(context, BlinkPermission.ACCESS_DATABASE)){
			BlinkPermission.PrintErrorAccessDatabase();
			return null;
		}
		
		if(mBlinkDatabase==null){
			mBlinkDatabase = new BlinkDatabase(context);
		}
		return mBlinkDatabase;
	}
	
	public void registerObserver(){
		
	}
}
