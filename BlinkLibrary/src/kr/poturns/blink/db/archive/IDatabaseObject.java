package kr.poturns.blink.db.archive;

/**
 * Database 테이블과 맵핑되는 클래스가 기본적으로 상속해야 할 인터페이스이다. <br>
 * <br>
 * Blink Database에 삽입 되는 객체를 의미한다.
 * 
 * @author Jiwon
 * 
 */
public interface IDatabaseObject {
	/**
	 * Database에 객체를 삽입 하기 전 객체의 무결성을 검사한다.
	 * 
	 * @return {@code true} - 무결성에 하자가 없는 경우<br>
	 *         <t> {@code false} - 무결성에 하자가 있는 경우 (Database에 삽입되지 않는다.)
	 */
	public boolean checkIntegrity();
}
