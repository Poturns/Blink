/**
 * TODO
 * BlinkServiceInteraction.Remote.obtainMeasurementData(Class, int) <br>
 * 2014-09-25 현재 구현 된 상태에서 <br>
 * 이 메소드는 인자로 주어지는 클래스가 Blink Library schema package에 정의되어 있지 않으면<br>
 * 해당 클래스 데이터를 얻어오지 못한다.<br>
 * <br>
 * 
 * 따라서 앞으로는 DefaultSchema class를 인터페이스로 바꾸고<br>
 * 위의 메소드와 DB package, Service에서 Class object 대신 ClassUtil.obtainClassSchema()<br>
 * 를 사용할 예정
 * @author Myungjin
 */
/**
 * 
 *
 */
package kr.poturns.blink.schema;