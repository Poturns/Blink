package kr.poturns.blink.db.archive;

/**
 * SyncMeasurementData 테이블과 맵핑되는 클래스
 * 
 * <br>
 * <br>
 * 
 * 각 BlinkService가 동기화 될 때 사용한다.
 * 
 * @author Jiwon
 * 
 */
public class SyncMeasurementData {
	public int DeviceId;
	public int MeasurementDataId;
	public String DateTime;
}
