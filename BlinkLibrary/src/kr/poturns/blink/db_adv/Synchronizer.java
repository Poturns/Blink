package kr.poturns.blink.db_adv;

/**
 * 동기화와 관련된 처리를 하는 클래스
 * @author Jiwon
 *
 */
public class Synchronizer {
	private BlinkServiceDatabase mBlinkServiceDatabase;
	
	public void Synchronizer(BlinkServiceDatabase database){
		mBlinkServiceDatabase = database;
	}
}
