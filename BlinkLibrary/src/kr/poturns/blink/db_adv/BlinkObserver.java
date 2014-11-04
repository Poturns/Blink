package kr.poturns.blink.db_adv;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;

/**
 * Blink와 관련된 이벤트를 등록하는 매소드
 * @author Jiwon
 *
 */
public class BlinkObserver {
	Context context;
	ContentResolver mContentResolver;
	
	public BlinkObserver(Context context){
		this.context = context;
		mContentResolver = context.getContentResolver();
	}
	
	public void registerObserver(ContentObserver observer){
		mContentResolver.registerContentObserver(uri, false, observer);
	}
	
	public void unregisterObserver(){
		
	}
}
