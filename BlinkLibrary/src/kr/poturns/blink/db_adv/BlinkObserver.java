package kr.poturns.blink.db_adv;

import android.content.ContentResolver;
import android.content.Context;

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
	
	public void registerObserver(){
		
	}
	
	public void unregisterObserver(){
		
	}
}
