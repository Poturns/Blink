package kr.poturns.blink.db.archive;

/**
 * Database 테이블과 맵핑되는 클래스가 기본적으로 상속해야 할 인터페이스
 * 저장하기 전에 Integrity(무결성)을 확인한다.
 * @author Jiwon
 *
 */
interface IDatabaseObject {
	public boolean checkIntegrity();
}
