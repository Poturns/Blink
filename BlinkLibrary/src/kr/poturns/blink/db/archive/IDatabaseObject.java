package kr.poturns.blink.db.archive;

/**
 * Database 테이블과 맵핑되는 클래스가 기본적으로 상속해야 할 인터페이스이다.
 * @author Jiwon
 *
 */
public interface IDatabaseObject {
	public boolean checkIntegrity();
}
